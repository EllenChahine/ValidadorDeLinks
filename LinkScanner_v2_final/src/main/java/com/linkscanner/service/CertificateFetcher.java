package com.linkscanner.service;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Base64;

public class CertificateFetcher {

    public static class FetchedCert {
        public String pem;
        public String sha256Hex;
        public String pubKeySha256;
        public String subjectDN;
        public String issuerDN;
        public LocalDateTime notBefore;
        public LocalDateTime notAfter;
        public X509Certificate cert;
    }

    public static FetchedCert fetch(String host, int port, String sniHost) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new X509TrustManager(){
            public void checkClientTrusted(X509Certificate[] c, String s){}
            public void checkServerTrusted(X509Certificate[] c, String s){}
            public X509Certificate[] getAcceptedIssuers(){ return new X509Certificate[0]; }
        }}, null);

        SSLSocketFactory factory = ctx.getSocketFactory();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            SSLParameters params = socket.getSSLParameters();
            if (sniHost != null) {
                params.setServerNames(java.util.List.of(new SNIHostName(sniHost)));
            }
            socket.setSSLParameters(params);
            socket.setSoTimeout(10000);
            socket.startHandshake();
            SSLSession session = socket.getSession();
            java.security.cert.Certificate[] peerCerts = session.getPeerCertificates();
            if (peerCerts == null || peerCerts.length == 0) throw new RuntimeException("No certs");
            X509Certificate leaf = (X509Certificate) peerCerts[0];

            byte[] der = leaf.getEncoded();
            String pem = toPem(der);
            String sha256Hex = sha256Hex(der);
            String pubKeySha256 = sha256Hex(leaf.getPublicKey().getEncoded());

            FetchedCert out = new FetchedCert();
            out.pem = pem;
            out.sha256Hex = sha256Hex;
            out.pubKeySha256 = pubKeySha256;
            out.subjectDN = leaf.getSubjectX500Principal().getName();
            out.issuerDN = leaf.getIssuerX500Principal().getName();
            out.notBefore = LocalDateTime.ofInstant(leaf.getNotBefore().toInstant(), ZoneId.systemDefault());
            out.notAfter = LocalDateTime.ofInstant(leaf.getNotAfter().toInstant(), ZoneId.systemDefault());
            out.cert = leaf;
            return out;
        }
    }

    private static String toPem(byte[] der) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----\n";
    }

    private static String sha256Hex(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

package com.linkscanner.service;

import com.linkscanner.model.BankCertificate;
import com.linkscanner.repository.BankCertificateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificateService {

    private final BankCertificateRepository repo;

    public CertificateService(BankCertificateRepository repo) {
        this.repo = repo;
    }

    // ---------- Helpers ----------
    private static String normalizeHost(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase();

        // Remove esquema se veio por engano
        if (s.startsWith("http://") || s.startsWith("https://")) {
            s = s.replaceFirst("^https?://", "");
        }

        // Remove caminho e query (fica só o host[:porta])
        int slash = s.indexOf('/');
        if (slash >= 0) s = s.substring(0, slash);

        // Remove porta
        int colon = s.indexOf(':');
        if (colon >= 0) s = s.substring(0, colon);

        // Remove www.
        if (s.startsWith("www.")) s = s.substring(4);

        return s;
    }

    private static void log(String msg) {
        System.out.println("[CertificateService] " + msg);
    }

    // ---------- Persistência do certificado (uma vez por domínio/subdomínio) ----------
    @Transactional
    public BankCertificate fetchAndStore(String bankName, String domain) throws Exception {
        String host = normalizeHost(domain);
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Domínio inválido.");
        }

        log("Iniciando fetch para host: " + host + " (bank=" + bankName + ")");

        CertificateFetcher.FetchedCert f;
        try {
            f = CertificateFetcher.fetch(host, 443, host);
        } catch (Exception e) {
            log("Erro ao buscar certificado: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw e;
        }
        if (f == null) throw new RuntimeException("Fetcher retornou nulo para " + host);

        // Evita duplicar por fingerprint (se já existir, retorna o existente)
        List<BankCertificate> existing = repo.findBySha256Fingerprint(f.sha256Hex);
        if (!existing.isEmpty()) {
            log("Cert já existente (fingerprint) para " + host + " -> id=" + existing.get(0).getId());
            return existing.get(0);
        }

        BankCertificate b = new BankCertificate();
        b.setBankName(bankName);
        b.setDomain(host);
        b.setPem(f.pem);
        b.setSha256Fingerprint(f.sha256Hex);
        b.setPublicKeySha256(f.pubKeySha256);
        b.setSubjectDn(f.subjectDN);
        b.setIssuerDn(f.issuerDN);
        b.setNotBefore(f.notBefore);
        b.setNotAfter(f.notAfter);
        b.setRetrievedAt(LocalDateTime.now());
        b.setSourceUrl("https://" + host);

        BankCertificate saved = repo.save(b);
        log("Cert salvo: id=" + saved.getId() + " host=" + host);
        return saved;
    }

    // ---------- Validação de qualquer link do domínio/subdomínio ----------
    @Transactional(readOnly = true)
    public boolean validateUrlAgainstStore(String url) throws Exception {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL inválida.");
        }

        // Extrai host da URL (se vier sem esquema, tenta normalizar)
        String host;
        try {
            URI uri = new URI(url);
            host = uri.getHost();
            if (host == null) {
                // Sem esquema? tenta forçar https://
                uri = new URI("https://" + url);
                host = uri.getHost();
            }
        } catch (Exception ignore) {
            // Ex: usuário digitou só "itau.com.br"
            host = url;
        }

        host = normalizeHost(host);
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("URL inválida (host ausente).");
        }

        log("Validando URL: " + url + " | host normalizado: " + host);

        // Busca o certificado do host informado (online)
        CertificateFetcher.FetchedCert f;
        try {
            f = CertificateFetcher.fetch(host, 443, host);
        } catch (Exception e) {
            log("Erro ao buscar certificado do host " + host + ": " + e.getMessage());
            return false;
        }
        if (f == null) {
            log("Fetcher retornou nulo para " + host);
            return false;
        }

        // Compara contra TODOS os certificados salvos:
        // host deve ser igual ao domínio salvo OU terminar com ".dominioSalvo"
        // E fingerprint OU chave pública devem bater
        List<BankCertificate> all = repo.findAll();
        for (BankCertificate b : all) {
            String savedDomain = normalizeHost(b.getDomain());
            if (savedDomain == null || savedDomain.isEmpty()) continue;

            boolean sameOrSubdomain = host.equals(savedDomain) || host.endsWith("." + savedDomain);
            if (!sameOrSubdomain) continue;

            boolean fpOk  = f.sha256Hex != null && f.sha256Hex.equalsIgnoreCase(b.getSha256Fingerprint());
            boolean pkOk  = f.pubKeySha256 != null && f.pubKeySha256.equalsIgnoreCase(b.getPublicKeySha256());

            if (fpOk || pkOk) {
                log("VALIDADO ✅ host=" + host + " match=" + (fpOk ? "fingerprint" : "pubkey") + " domainBase=" + savedDomain);
                return true;
            }
        }

        log("NÃO VALIDADO ⚠️ host=" + host + " não pertence a nenhum domínio salvo com certificado correspondente.");
        return false;
    }
}

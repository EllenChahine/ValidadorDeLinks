package com.linkscanner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "bank_certificates",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"domain", "sha256_fingerprint"})
    }
)
public class BankCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;

    private String domain;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String pem;

    @Column(name = "sha256_fingerprint")
    private String sha256Fingerprint;

    @Column(name = "public_key_sha256")
    private String publicKeySha256;

    @Column(name = "subject_dn", length = 1000)
    private String subjectDn;

    @Column(name = "issuer_dn", length = 1000)
    private String issuerDn;

    @Column(name = "not_before")
    private LocalDateTime notBefore;

    @Column(name = "not_after")
    private LocalDateTime notAfter;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;

    @Column(name = "source_url")
    private String sourceUrl;

    // ===== Getters e Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getPem() { return pem; }
    public void setPem(String pem) { this.pem = pem; }

    public String getSha256Fingerprint() { return sha256Fingerprint; }
    public void setSha256Fingerprint(String sha256Fingerprint) { this.sha256Fingerprint = sha256Fingerprint; }

    public String getPublicKeySha256() { return publicKeySha256; }
    public void setPublicKeySha256(String publicKeySha256) { this.publicKeySha256 = publicKeySha256; }

    public String getSubjectDn() { return subjectDn; }
    public void setSubjectDn(String subjectDn) { this.subjectDn = subjectDn; }

    public String getIssuerDn() { return issuerDn; }
    public void setIssuerDn(String issuerDn) { this.issuerDn = issuerDn; }

    public LocalDateTime getNotBefore() { return notBefore; }
    public void setNotBefore(LocalDateTime notBefore) { this.notBefore = notBefore; }

    public LocalDateTime getNotAfter() { return notAfter; }
    public void setNotAfter(LocalDateTime notAfter) { this.notAfter = notAfter; }

    public LocalDateTime getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(LocalDateTime retrievedAt) { this.retrievedAt = retrievedAt; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}

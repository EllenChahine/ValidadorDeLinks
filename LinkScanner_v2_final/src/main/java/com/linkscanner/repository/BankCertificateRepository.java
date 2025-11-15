package com.linkscanner.repository;

import com.linkscanner.model.BankCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BankCertificateRepository extends JpaRepository<BankCertificate, Long> {
    Optional<BankCertificate> findByDomainAndSha256Fingerprint(String domain, String sha256Fingerprint);
    List<BankCertificate> findByDomain(String domain);
    List<BankCertificate> findBySha256Fingerprint(String sha256Fingerprint);
}

CREATE DATABASE IF NOT EXISTS linkscanner_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE linkscanner_db;
CREATE TABLE IF NOT EXISTS bank_certificates (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bank_name VARCHAR(200),
  domain VARCHAR(255) NOT NULL,
  pem LONGTEXT,
  sha256_fingerprint CHAR(64),
  public_key_sha256 CHAR(64),
  subject_dn VARCHAR(1000),
  issuer_dn VARCHAR(1000),
  not_before DATETIME,
  not_after DATETIME,
  retrieved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  source_url VARCHAR(500),
  UNIQUE KEY ux_domain_fingerprint (domain, sha256_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

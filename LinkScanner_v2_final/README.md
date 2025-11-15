Java: 17
Spring Boot: 3.1.x
MySQL: local

Como usar:
1) Inicie o MySQL e crie o DB:
   CREATE DATABASE linkscanner_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

2) Build & run:
   mvn clean package
   mvn spring-boot:run

Frontend:
 - Open http://localhost:8080/

Endpoints:
 - POST /api/certs/fetch?bankName=Itaú&domain=itau.com.br
 - GET  /api/certs/validate?url=https://www.itau.com.br

Observações:
 - O CertificateFetcher conecta ao host de destino para obter o certificado (usa um TrustManager permissivo para extração).
 - Não armazene chaves privadas. Apenas certificados públicos são armazenados.


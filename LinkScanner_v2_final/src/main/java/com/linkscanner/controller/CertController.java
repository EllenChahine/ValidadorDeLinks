package com.linkscanner.controller;

import com.linkscanner.model.BankCertificate;
import com.linkscanner.service.CertificateService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/certs")
public class CertController {

    private final CertificateService service;

    public CertController(CertificateService service) {
        this.service = service;
    }

    // Aceita GET e POST; aceita params na query ou em JSON no body
    @RequestMapping(value = {"/fetch", "/fetch/"}, method = { RequestMethod.GET, RequestMethod.POST })
    public BankCertificate fetchAndStore(
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String domain,
            @RequestBody(required = false) Map<String, String> body
    ) throws Exception {
        if ((bankName == null || domain == null) && body != null) {
            bankName = body.get("bankName");
            domain = body.get("domain");
        }
        if (bankName == null || domain == null) {
            throw new IllegalArgumentException("Parâmetros 'bankName' e 'domain' são obrigatórios.");
        }
        return service.fetchAndStore(bankName, domain);
    }

    // GET para validação
    @GetMapping({"/validate", "/validate/"})
    public Map<String, Object> validateUrl(@RequestParam String url) throws Exception {
        boolean ok = service.validateUrlAgainstStore(url);
        return Map.of("url", url, "trusted", ok);
    }
}

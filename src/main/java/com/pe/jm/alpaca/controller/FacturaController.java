package com.pe.jm.alpaca.controller;

import com.pe.jm.alpaca.dto.FacturaRequest;
import com.pe.jm.alpaca.dto.FacturaResponse;
import com.pe.jm.alpaca.service.FacturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping("/registrarFactura")
    public CompletableFuture<ResponseEntity<FacturaResponse>> registrarFactura(
            @RequestBody FacturaRequest request) {

        return facturaService.procesarFactura(request)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .exceptionally(ex -> {
                    FacturaResponse errorResponse = FacturaResponse.builder()
                            .success(false)
                            .message("Error inesperado: " + ex.getMessage())
                            .build();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }
}

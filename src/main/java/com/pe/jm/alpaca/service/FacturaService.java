package com.pe.jm.alpaca.service;

import com.pe.jm.alpaca.dto.FacturaRequest;
import com.pe.jm.alpaca.dto.FacturaResponse;
import com.pe.jm.alpaca.model.AnticipoAplicado;
import com.pe.jm.alpaca.model.Cliente;
import com.pe.jm.alpaca.model.DetalleFactura;
import com.pe.jm.alpaca.model.Factura;
import com.pe.jm.alpaca.model.Vendedor;
import com.pe.jm.alpaca.repository.AnticipoAplicadoRepository;
import com.pe.jm.alpaca.repository.ClienteRepository;
import com.pe.jm.alpaca.repository.DetalleFacturaRepository;
import com.pe.jm.alpaca.repository.FacturaRepository;
import com.pe.jm.alpaca.repository.VendedorRepository;
import io.vertx.core.Future;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final AnticipoAplicadoRepository anticipoAplicadoRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FacturaService(FacturaRepository facturaRepository,
                          DetalleFacturaRepository detalleFacturaRepository,
                          AnticipoAplicadoRepository anticipoAplicadoRepository,
                          ClienteRepository clienteRepository,
                          VendedorRepository vendedorRepository) {
        this.facturaRepository = facturaRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.anticipoAplicadoRepository = anticipoAplicadoRepository;
        this.clienteRepository = clienteRepository;
        this.vendedorRepository = vendedorRepository;
    }

    public CompletableFuture<FacturaResponse> procesarFactura(FacturaRequest request) {
        FacturaRequest.FacturaData facturaData = request.getFactura();

        // Convertir y crear la factura
        Factura factura = convertirAFactura(facturaData);

        // Procesar la factura
        CompletableFuture<FacturaResponse> future = new CompletableFuture<>();

        // Primero validar y crear vendedor si no existe
        Long rucVendedor = Long.parseLong(facturaData.getRucEmisor());
        Long rucCliente = Long.parseLong(facturaData.getRuc());

                validarYCrearVendedor(rucVendedor)
                .compose(v -> validarYCrearCliente(rucCliente))
                .compose(v -> facturaRepository.insert(factura))
                 .compose(result -> {
                    // Procesar detalles y anticipos
                    return procesarDetallesYAnticipos(facturaData, factura);
                })
                .onSuccess(v -> {
                    FacturaResponse response = FacturaResponse.builder()
                            .success(true)
                            .message("Factura procesada exitosamente")
                            .codigoFactura(factura.getCodigoFactura())
                            .rucVendedor(factura.getRucVendedor())
                            .build();
                    future.complete(response);
                })
                .onFailure(error -> {
                    FacturaResponse response = FacturaResponse.builder()
                            .success(false)
                            .message("Error al procesar factura: " + error.getMessage())
                            .build();
                    future.complete(response);
                });

        return future;
    }

    private Future<Void> validarYCrearVendedor(Long rucVendedor) {
        return vendedorRepository.existsByRuc(rucVendedor)
                .compose(existe -> {
                    if (!existe) {
                        return Future.failedFuture("El vendedor con RUC " + rucVendedor + " no existe en el sistema");
                    }
                    return Future.succeededFuture();
                });
    }

    private Future<Void> validarYCrearCliente(Long rucCliente) {
        return clienteRepository.existsByRuc(rucCliente)
                .compose(existe -> {
                    if (!existe) {
                        return Future.failedFuture("El cliente con RUC " + rucCliente + " no existe en el sistema");
                    }
                    return Future.succeededFuture();
                });
    }

    private Factura convertirAFactura(FacturaRequest.FacturaData facturaData) {
        // Convertir fecha de formato dd/MM/yyyy a LocalDate
        LocalDate fechaEmision = LocalDate.parse(facturaData.getFechaEmision(), DATE_FORMATTER);

        // Obtener el string de facturaAnticipo usando un método separado
        String facturaAnticipoStr = obtenerFacturaAnticipoStr(facturaData);

        return Factura.builder()
            .rucVendedor(Long.parseLong(facturaData.getRucEmisor()))
            .codigoFactura(facturaData.getSerieNumero())
            .fechaEmision(fechaEmision)
            .rucCliente(Long.parseLong(facturaData.getRuc()))
            .formaPago("Contado")
            .facturaAnticipo(facturaAnticipoStr)
            .tipoMoneda(facturaData.getMoneda())
            .subTotalVentas(BigDecimal.valueOf(facturaData.getTotales().getSubTotalVentas()))
            .anticipos(BigDecimal.valueOf(facturaData.getTotales().getAnticipos()))
            .valorVenta(BigDecimal.valueOf(facturaData.getTotales().getValorVenta()))
            .montoDetraccion(BigDecimal.valueOf(facturaData.getMontoDetraccion()))
            .observacion(facturaData.getObservacion())
            .codBienServicio(facturaData.getBienServicioCodigo())
            .codMedioPago(facturaData.getMedioPagoCodigo())
            .nroCtaBancoNacion(facturaData.getNumeroCuentaBancoNacion())
            .porcentajeDetraccion(BigDecimal.valueOf(facturaData.getPorcentajeDetraccion()))
            .importeTotal(BigDecimal.valueOf(facturaData.getTotales().getImporteTotal()))
            .tipo(facturaData.getTipoFactura())
            .anulacion(
                java.util.Optional.ofNullable(facturaData.getEstadoFactura())
                    .filter(e -> e.equals("ANULADO"))
                    .map(e -> "1")
                    .orElse(null)
                )
            .build();
    }

    private String obtenerFacturaAnticipoStr(FacturaRequest.FacturaData facturaData) {
        if (facturaData.getFacturaAnticipo() != null && !facturaData.getFacturaAnticipo().isEmpty()) {
            return facturaData.getFacturaAnticipo().stream()
                .map(FacturaRequest.FacturaAnticipoItem::getNumero)
                .filter(numero -> numero != null && !numero.isEmpty())
                .collect(Collectors.joining(", "));
        }
        return null;
    }

    private Future<Void> procesarDetallesYAnticipos(FacturaRequest.FacturaData facturaData, Factura factura) {
        List<Future<Long>> futures = new ArrayList<>();

        // Obtener lista de números de facturas de anticipo
        List<String> numerosAnticipos = new ArrayList<>();
        if (facturaData.getFacturaAnticipo() != null && !facturaData.getFacturaAnticipo().isEmpty()) {
            numerosAnticipos = facturaData.getFacturaAnticipo().stream()
                    .map(FacturaRequest.FacturaAnticipoItem::getNumero)
                    .filter(numero -> numero != null && !numero.isEmpty())
                    .collect(Collectors.toList());
        }

        // Procesar cada detalle
        for (FacturaRequest.DetalleItem detalleItem : facturaData.getDetalle()) {
            String descripcion = detalleItem.getDescripcion();

            // Verificar si la descripción contiene algún número de anticipo del array facturaAnticipo[].numero
            boolean esAnticipo = false;
            String numeroAnticipoEncontrado = null;

            if (descripcion != null && !numerosAnticipos.isEmpty()) {
                for (String numeroAnticipo : numerosAnticipos) {
                    if (descripcion.contains(numeroAnticipo)) {
                        esAnticipo = true;
                        numeroAnticipoEncontrado = numeroAnticipo;
                        break;
                    }
                }
            }

            if (esAnticipo) {
                // Es un anticipo - guardar en AA_ANTICIPO_APLICADO
                AnticipoAplicado anticipo = AnticipoAplicado.builder()
                        .ventaRucVendedor(factura.getRucVendedor())
                        .ventaCodigoFactura(factura.getCodigoFactura())
                        .anticipoRucVendedor(factura.getRucVendedor())
                        .anticipoCodigoFactura(numeroAnticipoEncontrado)
                        .montoAplicado(BigDecimal.valueOf(detalleItem.getValorUnitario()))
                        .build();
 
                futures.add(anticipoAplicadoRepository.insert(anticipo));
            }
                
                DetalleFactura detalle = DetalleFactura.builder()
                        .rucVendedor(factura.getRucVendedor())
                        .codigoFactura(factura.getCodigoFactura())
                        .idProducto(null)
                        .cantidad(BigDecimal.valueOf(detalleItem.getCantidad()))
                        .unidadMedida(detalleItem.getUnidadMedida())
                        .codigo(null)
                        .descripcion(descripcion)
                        .valorUnitario(BigDecimal.valueOf(detalleItem.getValorUnitario()))
                        .build();

                futures.add(detalleFacturaRepository.insert(detalle));
            
        }

        // Esperar a que todos los inserts terminen
        return Future.all(futures).mapEmpty();
    }
}

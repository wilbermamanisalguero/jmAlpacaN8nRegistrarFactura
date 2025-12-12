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
        // ========== FASE 1: PREPARACIÓN ==========
        // Obtener lista de números de facturas de anticipo
        List<String> numerosAnticipos = obtenerNumerosAnticipos(facturaData);

        // Listas para almacenar los objetos que se insertarán DESPUÉS de validar
        List<AnticipoAplicado> anticiposParaInsertar = new ArrayList<>();
        List<DetalleFactura> detallesParaInsertar = new ArrayList<>();

        // ========== FASE 2: CALCULAR TOTALES ==========
        BigDecimal anticiposAplicados = BigDecimal.ZERO;
        BigDecimal subTotalVentas = BigDecimal.ZERO;

        // Procesar cada detalle para calcular totales y preparar objetos
        for (FacturaRequest.DetalleItem detalleItem : facturaData.getDetalle()) {
            String descripcion = detalleItem.getDescripcion();

            // Convertir a BigDecimal ANTES de operar (evita pérdida de precisión)
            BigDecimal cantidad = BigDecimal.valueOf(detalleItem.getCantidad());
            BigDecimal valorUnitario = BigDecimal.valueOf(detalleItem.getValorUnitario());
            BigDecimal valorLinea = cantidad.multiply(valorUnitario);

            // Verificar si es un anticipo
            String numeroAnticipoEncontrado = buscarNumeroAnticipo(descripcion, numerosAnticipos);
            boolean esAnticipo = numeroAnticipoEncontrado != null;

            if (esAnticipo) {
                // Acumular anticipos aplicados (usar valorUnitario directamente para anticipos)
                anticiposAplicados = anticiposAplicados.add(valorUnitario);

                // Preparar objeto anticipo (NO insertar todavía)
                AnticipoAplicado anticipo = AnticipoAplicado.builder()
                        .ventaRucVendedor(factura.getRucVendedor())
                        .ventaCodigoFactura(factura.getCodigoFactura())
                        .anticipoRucVendedor(factura.getRucVendedor())
                        .anticipoCodigoFactura(numeroAnticipoEncontrado)
                        .montoAplicado(valorUnitario)
                        .build();
                anticiposParaInsertar.add(anticipo);
            } else {
                // Acumular subtotal de ventas (usar cantidad * valorUnitario con precisión correcta)
                subTotalVentas = subTotalVentas.add(valorLinea);
            }

            // Preparar objeto detalle (NO insertar todavía)
            DetalleFactura detalle = DetalleFactura.builder()
                    .rucVendedor(factura.getRucVendedor())
                    .codigoFactura(factura.getCodigoFactura())
                    .idProducto(null)
                    .cantidad(cantidad)
                    .unidadMedida(detalleItem.getUnidadMedida())
                    .codigo(null)
                    .descripcion(descripcion)
                    .valorUnitario(valorUnitario)
                    .build();
            detallesParaInsertar.add(detalle);
        }

        // ========== FASE 3: VALIDAR TOTALES ==========
        Future<Void> validacionFuture = validarTotalesSegunTipo(
            factura,
            facturaData,
            anticiposAplicados,
            subTotalVentas
        );

        // Si la validación falla, retornar el error SIN insertar nada
        if (validacionFuture != null) {
            return validacionFuture;
        }

        // ========== FASE 4: INSERTAR (solo si todas las validaciones pasaron) ==========
        return insertarDetallesYAnticipos(anticiposParaInsertar, detallesParaInsertar);
    }

    /**
     * Obtiene la lista de números de anticipos desde los datos de la factura
     */
    private List<String> obtenerNumerosAnticipos(FacturaRequest.FacturaData facturaData) {
        if (facturaData.getFacturaAnticipo() == null || facturaData.getFacturaAnticipo().isEmpty()) {
            return new ArrayList<>();
        }
        return facturaData.getFacturaAnticipo().stream()
                .map(FacturaRequest.FacturaAnticipoItem::getNumero)
                .filter(numero -> numero != null && !numero.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Busca si la descripción contiene algún número de anticipo
     * @return El número de anticipo encontrado, o null si no se encuentra
     */
    private String buscarNumeroAnticipo(String descripcion, List<String> numerosAnticipos) {
        if (descripcion == null || numerosAnticipos.isEmpty()) {
            return null;
        }

        for (String numeroAnticipo : numerosAnticipos) {
            if (descripcion.contains(numeroAnticipo)) {
                return numeroAnticipo;
            }
        }
        return null;
    }

    /**
     * Valida los totales según el tipo de factura (VENTA o ANTICIPO)
     * @return Future.failedFuture si hay error de validación, null si todo está correcto
     */
    private Future<Void> validarTotalesSegunTipo(
            Factura factura,
            FacturaRequest.FacturaData facturaData,
            BigDecimal anticiposAplicados,
            BigDecimal subTotalVentas) {

        if ("VENTA".equals(factura.getTipo())) {
            return validarFacturaVenta(factura, anticiposAplicados, subTotalVentas);
        }

        if ("ANTICIPO".equals(factura.getTipo())) {
            return validarFacturaAnticipo(factura, facturaData);
        }

        return null; // No hay validaciones para otros tipos
    }

    /**
     * Valida una factura de tipo VENTA
     */
    private Future<Void> validarFacturaVenta(
            Factura factura,
            BigDecimal anticiposAplicados,
            BigDecimal subTotalVentas) {

        // Validación 1: Suma de anticipos aplicados debe coincidir con campo anticipos
        if (anticiposAplicados.abs().compareTo(factura.getAnticipos()) != 0) {
            return Future.failedFuture(
                String.format("Error en factura de tipo VENTA: La suma de anticipos aplicados (%s) " +
                    "no coincide con el campo anticipos de la factura (%s).",
                    anticiposAplicados.abs(), factura.getAnticipos())
            );
        }

        // Validación 2: Subtotal de ventas debe coincidir
        if (subTotalVentas.compareTo(factura.getSubTotalVentas()) != 0) {
            return Future.failedFuture(
                String.format("Error en factura de tipo VENTA: El subtotal de ventas calculado (%s) " +
                    "no coincide con el campo subtotalVentas de la factura (%s).",
                    subTotalVentas, factura.getSubTotalVentas())
            );
        }

        // Validación 3: Valor de venta e importe total deben coincidir
        BigDecimal valorVentaCalculado = subTotalVentas.subtract(anticiposAplicados.abs());

        if (factura.getValorVenta().compareTo(valorVentaCalculado) != 0 ||
            factura.getImporteTotal().compareTo(valorVentaCalculado) != 0) {
            return Future.failedFuture(
                String.format("Error en factura de tipo VENTA: El valor de venta calculado (%s) " +
                    "no coincide con el campo valorVenta (%s) o el importeTotal (%s) de la factura.",
                    valorVentaCalculado, factura.getValorVenta(), factura.getImporteTotal())
            );
        }

        return null; // Todas las validaciones pasaron
    }

    /**
     * Valida una factura de tipo ANTICIPO
     */
    private Future<Void> validarFacturaAnticipo(
            Factura factura,
            FacturaRequest.FacturaData facturaData) {

        if (facturaData.getDetalle() == null || facturaData.getDetalle().isEmpty()) {
            return null; // No hay detalles para validar
        }

        BigDecimal valorUnitarioPrimerDetalle =
            BigDecimal.valueOf(Math.abs(facturaData.getDetalle().get(0).getValorUnitario()));

        // Validar que subtotalVentas == importeTotal == valorUnitario del primer detalle
        if (factura.getSubTotalVentas().compareTo(factura.getImporteTotal()) != 0 ||
            factura.getSubTotalVentas().compareTo(valorUnitarioPrimerDetalle) != 0) {
            return Future.failedFuture(
                String.format("Error en factura de tipo ANTICIPO: El subtotal y el importe total " +
                    "deben ser iguales al valor del detalle. Valores: valorDetalle=%s, " +
                    "importeTotal=%s, subTotalVentas=%s",
                    valorUnitarioPrimerDetalle, factura.getImporteTotal(), factura.getSubTotalVentas())
            );
        }

        return null; // Validación exitosa
    }

    /**
     * Inserta todos los detalles y anticipos en la base de datos
     */
    private Future<Void> insertarDetallesYAnticipos(
            List<AnticipoAplicado> anticiposParaInsertar,
            List<DetalleFactura> detallesParaInsertar) {

        List<Future<Long>> futures = new ArrayList<>();

        // Insertar todos los anticipos
        for (AnticipoAplicado anticipo : anticiposParaInsertar) {
            futures.add(anticipoAplicadoRepository.insert(anticipo));
        }

        // Insertar todos los detalles
        for (DetalleFactura detalle : detallesParaInsertar) {
            futures.add(detalleFacturaRepository.insert(detalle));
        }

        // Esperar a que todos los inserts terminen
        return Future.all(futures).mapEmpty();
    }
}

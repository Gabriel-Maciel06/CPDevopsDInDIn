package br.com.fiap.dimdim.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check público usado pelo widget de telemetria do front.
 * Não expõe versão do SQL Server nem mensagens de exceção (podem conter host/usuário);
 * o detalhe do erro vai para o log, que é coletado pelo Application Insights.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Monitoramento e liveness probe da aplicação na nuvem")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url:unknown}")
    private String datasourceUrl;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @Operation(summary = "Verificar integridade do Web App, Banco Azure SQL e Telemetria")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("servico", "DimDim Web App PaaS");
        status.put("timestamp", LocalDateTime.now());
        status.put("ambiente", System.getenv("WEBSITE_SITE_NAME") != null ? "Microsoft Azure App Service" : "Local / Sandbox");

        boolean appInsightsAtivo = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING") != null
                || System.getenv("APPINSIGHTS_INSTRUMENTATIONKEY") != null;
        status.put("applicationInsightsAtivo", appInsightsAtivo);

        try {
            long start = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long latency = System.currentTimeMillis() - start;

            status.put("bancoStatus", "CONECTADO");
            status.put("bancoTipo", datasourceUrl.startsWith("jdbc:sqlserver") ? "Azure SQL Database (MSSQL)" : "H2 Database (Local Dev)");
            status.put("bancoLatenciaMs", latency);
        } catch (Exception e) {
            log.error("Falha no health check do banco de dados", e);
            status.put("bancoStatus", "ERRO_CONEXAO");
            status.put("bancoErro", "Banco de dados indisponível");
        }

        return ResponseEntity.ok(status);
    }
}

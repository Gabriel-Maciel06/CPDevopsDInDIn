package br.com.fiap.dimdim.controller;

import br.com.fiap.dimdim.dto.ResumoFinanceiroDto;
import br.com.fiap.dimdim.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Métricas financeiras consolidadas do DimDim")
public class DashboardController {

    private final TransacaoService transacaoService;

    public DashboardController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Obter resumo consolidado das finanças (receitas, despesas, saldo e contadores)")
    public ResponseEntity<ResumoFinanceiroDto> obterResumo() {
        return ResponseEntity.ok(transacaoService.obterResumoFinanceiro());
    }
}

package br.com.fiap.dimdim.controller;

import br.com.fiap.dimdim.dto.TransacaoRequestDto;
import br.com.fiap.dimdim.dto.TransacaoResponseDto;
import br.com.fiap.dimdim.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transações", description = "CRUD de Transações Financeiras no Azure SQL")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as transações")
    public ResponseEntity<List<TransacaoResponseDto>> listarTodas() {
        return ResponseEntity.ok(transacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    public ResponseEntity<TransacaoResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar transações filtradas por ID da categoria")
    public ResponseEntity<List<TransacaoResponseDto>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(transacaoService.listarPorCategoria(categoriaId));
    }

    @PostMapping
    @Operation(summary = "Criar nova transação")
    public ResponseEntity<TransacaoResponseDto> criar(@Valid @RequestBody TransacaoRequestDto dto) {
        TransacaoResponseDto criada = transacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação existente")
    public ResponseEntity<TransacaoResponseDto> atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoRequestDto dto) {
        return ResponseEntity.ok(transacaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir transação por ID")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        transacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

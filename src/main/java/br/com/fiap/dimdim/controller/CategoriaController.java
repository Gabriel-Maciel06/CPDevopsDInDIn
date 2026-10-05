package br.com.fiap.dimdim.controller;

import br.com.fiap.dimdim.dto.CategoriaRequestDto;
import br.com.fiap.dimdim.dto.CategoriaResponseDto;
import br.com.fiap.dimdim.model.TipoOperacao;
import br.com.fiap.dimdim.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "CRUD de Categorias Financeiras no Azure SQL")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public ResponseEntity<List<CategoriaResponseDto>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoriaResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar categorias por tipo (RECEITA ou DESPESA)")
    public ResponseEntity<List<CategoriaResponseDto>> listarPorTipo(@PathVariable TipoOperacao tipo) {
        return ResponseEntity.ok(categoriaService.listarPorTipo(tipo));
    }

    @PostMapping
    @Operation(summary = "Criar nova categoria")
    public ResponseEntity<CategoriaResponseDto> criar(@Valid @RequestBody CategoriaRequestDto dto) {
        CategoriaResponseDto criada = categoriaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria existente")
    public ResponseEntity<CategoriaResponseDto> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDto dto) {
        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria por ID")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

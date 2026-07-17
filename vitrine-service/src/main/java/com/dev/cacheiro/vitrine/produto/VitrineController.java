package com.dev.cacheiro.vitrine.produto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vitrine")
public class VitrineController {

    private final VitrineService service;


    @GetMapping
    public List<ProdutoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProdutoResponse detalhar(@PathVariable Long id) {
        return service.buscar(id);
    }


}

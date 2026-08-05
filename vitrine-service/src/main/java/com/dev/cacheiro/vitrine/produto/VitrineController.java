package com.dev.cacheiro.vitrine.produto;

import com.dev.cacheiro.vitrine.docs.ApiDetalharVitrine;
import com.dev.cacheiro.vitrine.docs.ApiListarVitrine;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vitrine")
@Tag(name = "Vitrine", description = "Leitura cacheada do catálogo — somente leitura")
public class VitrineController {

    private final VitrineService service;


    @GetMapping
    @ApiListarVitrine
    public List<ProdutoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @ApiDetalharVitrine
    public ProdutoResponse detalhar(@PathVariable Long id) {
        return service.buscar(id);
    }


}

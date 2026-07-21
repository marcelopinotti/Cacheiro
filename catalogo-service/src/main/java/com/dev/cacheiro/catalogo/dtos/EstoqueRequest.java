package com.dev.cacheiro.catalogo.dtos;

// delta negativo baixa estoque; positivo devolve (cancelamento)
public record EstoqueRequest(int delta) {}

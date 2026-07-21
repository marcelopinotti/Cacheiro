package main

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

var transicoes = map[string][]string{
	"CRIADO": {"PAGO", "CANCELADO"},
	"PAGO":   {"ENVIADO", "CANCELADO"},
}

func transicaoValida(de, para string) bool {
	for _, p := range transicoes[de] {
		if p == para {
			return true
		}
	}
	return false
}

func (a *App) criarPedido(w http.ResponseWriter, r *http.Request) {

	var req struct {
		ProdutoID  int64 `json:"produtoId"`
		Quantidade int   `json:"quantidade"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "body inválido: formato JSON", http.StatusBadRequest)
		return
	}
	if req.Quantidade <= 0 {
		http.Error(w, "quantidade deve ser > 0", http.StatusBadRequest)
		return
	}

	produto, err := a.catalogo.BuscarProduto(req.ProdutoID)
	if errors.Is(err, ErrProdutoNaoExiste) {
		http.Error(w, "produto não existe", http.StatusUnprocessableEntity)
		return
	}
	if err != nil {
		http.Error(w, "catálogo indisponível", http.StatusBadGateway)
		return
	}

	if err := a.catalogo.AjustarEstoque(req.ProdutoID, -req.Quantidade); err != nil {
		if errors.Is(err, ErrEstoqueInsuficiente) {
			http.Error(w, "estoque insuficiente", http.StatusConflict)
			return
		}
		http.Error(w, "catálogo indisponível", http.StatusBadGateway)
		return
	}

	p := &Pedido{ProdutoID: req.ProdutoID, Quantidade: req.Quantidade, PrecoUnitario: produto.Preco.String()}
	if err := inserirPedido(r.Context(), a.pedidos, p); err != nil {
		a.catalogo.AjustarEstoque(req.ProdutoID, +req.Quantidade)
		http.Error(w, "erro ao gravar pedido", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(p)
}

func (a *App) listarPedidos(w http.ResponseWriter, r *http.Request) {
	pedidos, err := listarPedidos(r.Context(), a.pedidos)
	if err != nil {
		http.Error(w, "erro ao listar pedidos", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(pedidos)
}

func (a *App) buscarPedido(w http.ResponseWriter, r *http.Request) {

	id, err := bson.ObjectIDFromHex(r.PathValue("id"))
	if err != nil {
		http.Error(w, "pedido não encontrado", http.StatusNotFound)
		return
	}
	p, err := buscarPedido(r.Context(), a.pedidos, id)
	if errors.Is(err, mongo.ErrNoDocuments) {
		http.Error(w, "pedido não encontrado", http.StatusNotFound)
		return
	}
	if err != nil {
		http.Error(w, "erro ao buscar pedido", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(p)
}

func (a *App) atualizarStatus(writer http.ResponseWriter, request *http.Request) {
	id, err := bson.ObjectIDFromHex(request.PathValue("id"))
	if err != nil {
		http.Error(writer, "pedido não encontrado", http.StatusNotFound)
		return
	}
	var req struct {
		Status string `json:"status"`
	}
	if err := json.NewDecoder(request.Body).Decode(&req); err != nil {
		http.Error(writer, "body inválido: formato JSON", http.StatusBadRequest)
		return
	}

	p, err := buscarPedido(request.Context(), a.pedidos, id)
	if errors.Is(err, mongo.ErrNoDocuments) {
		http.Error(writer, "pedido não encontrado", http.StatusNotFound)
		return
	}
	if err != nil {
		http.Error(writer, "erro ao buscar pedido", http.StatusInternalServerError)
		return
	}

	if !transicaoValida(p.Status, req.Status) {
		http.Error(writer, "transição de status inválida", http.StatusConflict)
		return
	}

	if req.Status == "CANCELADO" {
		if err := a.catalogo.AjustarEstoque(p.ProdutoID, +p.Quantidade); err != nil {
			http.Error(writer, "catálogo indisponível", http.StatusBadGateway)
			return
		}
	}

	if err := atualizarStatus(request.Context(), a.pedidos, id, req.Status); err != nil {
		http.Error(writer, "erro ao atualizar status", http.StatusInternalServerError)
		return
	}
	writer.WriteHeader(http.StatusNoContent)
}

func (a *App) healthz(writer http.ResponseWriter, request *http.Request) {
	context, cancel := context.WithTimeout(request.Context(), 2*time.Second)
	defer cancel()
	if err := a.pedidos.Database().Client().Ping(context, nil); err != nil {
		http.Error(writer, "mongo indisponível", http.StatusServiceUnavailable)
		return
	}
	writer.Write([]byte("ok"))
}

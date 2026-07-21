package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"time"
)

var ErrProdutoNaoExiste = errors.New("produto não existe no catálogo")
var ErrEstoqueInsuficiente = errors.New("estoque insuficiente")

type CatalogoClient struct {
	base string
	http *http.Client
}

func NewCatalogoClient(base string) *CatalogoClient {
	// sempre com timeout o default do http.Client é infinito
	return &CatalogoClient{base: base, http: &http.Client{Timeout: 3 * time.Second}}
}

type Produto struct {
	Preco json.Number `json:"preco"`
}

func (catalogo *CatalogoClient) BuscarProduto(id int64) (*Produto, error) {
	resp, err := catalogo.http.Get(fmt.Sprintf("%s/api/produtos/%d", catalogo.base, id))
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, ErrProdutoNaoExiste
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("catálogo devolveu %s", resp.Status)
	}

	var p Produto
	if err := json.NewDecoder(resp.Body).Decode(&p); err != nil {
		return nil, err
	}
	return &p, nil
}

func (catalogo *CatalogoClient) AjustarEstoque(id int64, delta int) error {
	body, _ := json.Marshal(map[string]int{"delta": delta})
	req, err := http.NewRequest(http.MethodPatch,
		fmt.Sprintf("%s/api/produtos/%d/estoque", catalogo.base, id), bytes.NewReader(body))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := catalogo.http.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	switch resp.StatusCode {
	case http.StatusNoContent:
		return nil
	case http.StatusConflict:
		return ErrEstoqueInsuficiente
	case http.StatusNotFound:
		return ErrProdutoNaoExiste
	default:
		return fmt.Errorf("catálogo devolveu %s", resp.Status)
	}
}

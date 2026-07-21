package main

import (
	"context"
	"log"
	"net/http"
	"os"

	"github.com/prometheus/client_golang/prometheus/promhttp"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

// getenv com valor default — substitui o application.yaml para 2 variáveis
func getenv(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}

type App struct {
	pedidos  *mongo.Collection
	catalogo *CatalogoClient
}

func main() {
	client, err := mongo.Connect(options.Client().ApplyURI(
		getenv("MONGO_URL", "mongodb://localhost:27017")))
	if err != nil {
		log.Fatal(err)
	}
	defer client.Disconnect(context.Background())

	app := &App{
		pedidos:  client.Database("pedidos").Collection("pedidos"),
		catalogo: NewCatalogoClient(getenv("CATALOGO_URL", "http://localhost:8081")),
	}

	mux := http.NewServeMux()
	mux.HandleFunc("POST /api/produtos", app.criarPedido)
	mux.HandleFunc("GET /api/produtos", app.listarPedidos)
	mux.HandleFunc("GET /api/produtos/{id}", app.buscarPedido)
	mux.HandleFunc("PATCH /api/produtos/{id}/status", app.atualizarStatus)
	mux.HandleFunc("GET /healthz", app.healthz)
	mux.Handle("GET /metrics", promhttp.Handler())

	log.Println("pedidos-service ouvindo em :8082")
	log.Fatal(http.ListenAndServe(":8082", mux))
}

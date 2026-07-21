package main

import (
	"context"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

type Pedido struct {
	ID            bson.ObjectID `bson:"_id,omitempty" json:"id"`
	ProdutoID     int64         `bson:"produtoId"     json:"produtoId"`
	Quantidade    int           `bson:"quantidade"    json:"quantidade"`
	PrecoUnitario string        `bson:"precoUnitario" json:"precoUnitario"` // nunca float para dinheiro
	Status        string        `bson:"status"        json:"status"`
	CriadoEm      time.Time     `bson:"criadoEm"      json:"criadoEm"`
	AtualizadoEm  time.Time     `bson:"atualizadoEm"  json:"atualizadoEm"`
}

func inserirPedido(context context.Context, col *mongo.Collection, pedido *Pedido) error {
	pedido.Status = "CRIADO"
	pedido.CriadoEm = time.Now().UTC()
	pedido.AtualizadoEm = pedido.CriadoEm
	res, err := col.InsertOne(context, pedido) // omitempty no _id: o Mongo gera o id
	if err != nil {
		return err
	}
	pedido.ID = res.InsertedID.(bson.ObjectID)
	return nil
}

func buscarPedido(context context.Context, col *mongo.Collection, id bson.ObjectID) (*Pedido, error) {
	var p Pedido
	err := col.FindOne(context, bson.M{"_id": id}).Decode(&p)
	return &p, err
}

func listarPedidos(context context.Context, col *mongo.Collection) ([]Pedido, error) {
	cur, err := col.Find(context, bson.M{})
	if err != nil {
		return nil, err
	}
	pedidos := []Pedido{}
	return pedidos, cur.All(context, &pedidos)
}

func atualizarStatus(context context.Context, col *mongo.Collection, id bson.ObjectID, status string) error {
	_, err := col.UpdateByID(context, id, bson.M{
		"$set": bson.M{"status": status, "atualizadoEm": time.Now().UTC()},
	})
	return err
}

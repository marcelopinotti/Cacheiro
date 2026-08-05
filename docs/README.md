# Documentação das APIs

Cada serviço publica o próprio Swagger. Suba com `docker compose up` e abra:

| Serviço | Swagger UI | Spec |
|---|---|---|
| catalogo-service | http://localhost:8081/swagger-ui.html | `/v3/api-docs` |
| vitrine-service | http://localhost:8080/swagger-ui.html | `/v3/api-docs` |
| pedido-service | http://localhost:8082/swagger/index.html | `/swagger/doc.json` |

## Onde ficam as anotações

Os serviços Java não sujam o controller com `@Operation`: cada endpoint tem uma
anotação composta própria, uma por arquivo, no pacote `docs` do serviço.

- `catalogo-service/src/main/java/com/dev/cacheiro/catalogo/docs/` — `@ApiListarProdutos`, `@ApiBuscarProduto`, `@ApiCriarProduto`, `@ApiAtualizarProduto`, `@ApiAjustarEstoque`, `@ApiDeletarProduto`
- `vitrine-service/src/main/java/com/dev/cacheiro/vitrine/docs/` — `@ApiListarVitrine`, `@ApiDetalharVitrine`

O springdoc resolve isso via `AnnotatedElementUtils.findMergedAnnotation`, que
enxerga meta-anotações — por isso o `@Operation` dentro do `@interface` funciona.

O `@Info` de cada serviço fica na classe `*Application`.

## pedido-service (Go)

Go não tem anotação, então são comentários `@Summary`/`@Router` do
[swaggo](https://github.com/swaggo/swag) em cima de cada handler em
`handlers.go`. O spec em `pedido-service/docs/` é **gerado** — depois de mexer
nos comentários, rode:

```sh
cd pedido-service && swag init -o docs
```

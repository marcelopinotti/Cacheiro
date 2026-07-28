# frontend — painel do Cacheiro

Angular 20 (standalone + signals). Não é uma loja: é um **painel de operação** para *ver* o
cache trabalhando. Cada requisição que o navegador faz aparece cronometrada no trilho da
direita, e a latência denuncia se o dado veio do Redis ou se foi até o catálogo.

Roda em `http://localhost:4200`.

---

## Como subir

O Docker Desktop precisa estar rodando (o daemon, não só instalado).

### Tudo em container

```bash
docker compose up -d --build
```

Sobe redis, postgres, mongo, os três serviços, prometheus, grafana **e este frontend**.
O `.env` da raiz já tem o que o compose precisa.

### Dev com hot reload

```bash
docker compose up -d --build --scale frontend=0   # tudo, menos o container do front
cd frontend && npm start
```

O `--scale frontend=0` evita a briga pela porta 4200. Se o `npm start` reclamar de porta
ocupada, é o container ainda de pé: `docker compose stop frontend`.

### Scripts

| comando | o que faz |
|---|---|
| `npm start` | dev server em `:4200`, com o proxy do `proxy.conf.json` |
| `npm run build` | build de produção em `dist/frontend/browser` |
| `npm run watch` | build de desenvolvimento em modo watch |
| `npm test` | karma/jasmine — **não há testes escritos**, e o karma precisa de um Chrome |

---

## Por que não existe CORS em lugar nenhum

Os três serviços vivem em portas diferentes (`8080`, `8081`, `8082`). Um navegador
chamando as três direto exigiria configurar CORS em cada um — mexer no Spring do
`vitrine-service`, no do `catalogo-service` e no `mux` do `pedido-service`.

Em vez disso, **tudo passa por um proxy de mesma origem**. O front só conhece caminhos
relativos (`/api/vitrine`), e quem roteia é:

| ambiente | quem faz o proxy | arquivo |
|---|---|---|
| dev | o dev server do Angular | `proxy.conf.json` |
| produção | nginx | `nginx.conf` |

```
/api/vitrine   →  vitrine-service:8080
/api/produtos  →  catalogo-service:8081
/api/pedido    →  pedido-service:8082
```

Os três prefixos são disjuntos, então o roteamento é literal — sem regex, sem reescrita.
**Nenhum arquivo Java ou Go foi tocado para o front existir.**

---

## Mapa de arquivos

### Infra

| arquivo | o que é |
|---|---|
| `proxy.conf.json` | as três regras de proxy do `ng serve`. Ligado ao build em `angular.json` → `architect.serve.options.proxyConfig` |
| `nginx.conf` | mesmas três regras em produção, mais o `try_files` que faz as rotas do Angular (`/pedidos`, `/catalogo`…) caírem no `index.html` |
| `Dockerfile` | dois estágios: `node:20.20-alpine` compila, `nginx:1.27-alpine` serve. Tags fixas, nada de `:latest` |
| `.dockerignore` | mantém `node_modules`, `dist` e `.angular` fora do contexto de build |
| `angular.json` | config do CLI. A única linha editada à mão foi o `proxyConfig` |

### Base do app

| arquivo | o que é |
|---|---|
| `src/index.html` | casca HTML. Só o `<title>` e o `lang="pt-BR"` mudaram |
| `src/styles.css` | **todo o visual mora aqui**: as variáveis CSS (cor, fonte, espaço) e as classes compartilhadas (`.painel`, `.tabela`, `.btn`, `.campo`, `.selo`). Os componentes quase não têm CSS próprio por causa disso |
| `src/main.ts` | bootstrap. Intocado |
| `src/app/app.config.ts` | providers da aplicação. Foi aqui que entrou o `provideHttpClient(withFetch())` |
| `src/app/app.routes.ts` | as quatro rotas, todas `loadComponent` (lazy). `/` redireciona para `/vitrine`, e qualquer rota desconhecida também |
| `src/app/app.ts` | a casca visível: barra de navegação no topo e **o trilho de requisições à direita**. O trilho lê direto os sinais do `CacheiroApi`, então qualquer tela alimenta ele sem precisar avisar ninguém |

### O núcleo

**`src/app/cacheiro-api.ts`** — o único arquivo que fala HTTP. Tudo passa por um método
privado `pedir()`, que:

1. marca `performance.now()` antes e depois;
2. registra a chamada num sinal `_chamadas` (método, caminho, ms, status, erro);
3. normaliza a mensagem de erro — o Java devolve JSON (`{"erro": "..."}`), o Go devolve
   texto puro, e uma queda de rede vira status `0`. `mensagemDeErro()` cobre os três.

Expõe também:

- `chamadas` — o log que o trilho renderiza;
- `ultimaMs` — a latência da chamada mais recente, que as telas leem logo depois do `await`;
- `taxaHit` — % das leituras da vitrine que vieram do cache;
- `LIMIAR_MS = 150` — a fronteira entre "cache" e "origem";
- `moeda` — um `Intl.NumberFormat` pt-BR, reaproveitado em todas as telas.

Os métodos públicos são um por endpoint, agrupados por serviço, e os tipos (`Produto`,
`Pedido`) espelham os DTOs do backend.

### Componentes

| arquivo | o que é |
|---|---|
| `latencia.ts` | o selo `312ms ███ origem`. Recebe `ms` como signal input; deriva cor, rótulo e largura da barra por `computed()`. A barra satura em 400ms |
| `produto-linha.ts` | uma linha da tabela da vitrine. Seletor de atributo (`tr[produto]`) para a tabela continuar sendo uma `<table>` de verdade — importa para leitor de tela. Emite `detalhar` e `pedir` via `output()` |
| `vitrine-page.ts` | a tela principal. Lista, detalhe e o botão "pedir 1" |
| `pedidos-page.ts` | formulário de pedido + tabela com as transições de status válidas |
| `catalogo-page.ts` | CRUD do catálogo + ajuste rápido de estoque (`−1` / `+1`) |
| `metricas-page.ts` | o dashboard do Grafana num iframe |

Todos seguem o mesmo padrão: `ChangeDetectionStrategy.OnPush`, estado em `signal()`,
template e estilo inline no decorator (um arquivo por componente, não três).

---

## O que cada tela demonstra

### `/vitrine` — cache-aside

A leitura passa pela vitrine, que tenta o Redis antes do catálogo.

1. Clique num produto: primeira leitura vai à origem, ~300ms, marca **origem**.
2. Clique em "ler de novo": veio do Redis, poucos ms, marca **cache**.
3. Espere 45s (TTL do detalhe) e leia de novo: volta a ser origem.
4. Clique em "pedir 1": o estoque baixa **pelo catálogo**, que publica a invalidação. A
   releitura logo em seguida já sai cara de novo, sem esperar o TTL — é a invalidação
   ativa funcionando.

### `/pedidos` — saga por compensação

Criar um pedido reserva estoque no catálogo por HTTP e só então grava no MongoDB; se a
gravação falha, o estoque volta. Cancelar devolve pela mesma via. As transições
(`CRIADO → PAGO | CANCELADO`, `PAGO → ENVIADO | CANCELADO`) estão duplicadas no front só
para esconder botão impossível — quem decide é o `pedido-service`, que devolve 409.

### `/catalogo` — invalidação por pub/sub

Fonte da verdade. Toda escrita aqui publica no canal `produtos:invalidacao` depois do
commit, e a vitrine apaga as chaves. Mude um preço, volte para a vitrine: o dado novo
aparece na hora.

As leituras desta tela sempre marcam **origem** — e está certo: ela *é* a origem, com os
300ms simulados. Não passa pelo Redis.

### `/metricas` — o Grafana embutido

O mesmo dashboard de `observability/grafana-dashboard.json`. Para o iframe funcionar, o
compose ganhou três variáveis no serviço `grafana`:

```yaml
GF_SECURITY_ALLOW_EMBEDDING: "true"
GF_AUTH_ANONYMOUS_ENABLED: "true"
GF_AUTH_ANONYMOUS_ORG_ROLE: Viewer
```

Se o iframe vier em branco, o container subiu antes dessa mudança — recrie ele.

Vale notar a diferença: o trilho da direita mede o que **este navegador** viu; o Grafana
mostra o que o Prometheus raspou dos serviços. Os números não batem, e não deveriam.

---

## Decisões e limitações

**Hit/miss é inferido, não medido.** O front não tem como saber se a vitrine acertou o
cache — ele chuta pela latência (`< 150ms` = cache). Funciona porque o catálogo tem 300ms
simulados, mas é heurística: uma rede lenta ou um catálogo mais rápido confundem a
leitura. O caminho certo é a vitrine devolver um header `X-Cache: HIT|MISS`, e aí
`latencia.ts` lê em vez de adivinhar. Marcado com `ponytail:` no `cacheiro-api.ts`.

**Atrás do nginx, o rate limit vira um balde só.** O `RateLimitFilter` conta por
`request.getRemoteAddr()`, que em produção é sempre o IP do nginx — os 100 req/min passam
a ser compartilhados pelo site inteiro, não por visitante. O `nginx.conf` já manda
`X-Forwarded-For`; falta o filtro lê-lo. Em dev não acontece, porque o dev server não
mascara o IP. Marcado com `ponytail:` no `nginx.conf`.

**Angular 20, não 22.** O CLI atual exige Node ≥ 22.22 e a máquina tem 20.20.2. O 20 tem
tudo que o projeto usa — standalone, signal inputs, `output()`, `@if`/`@for`, `host`
object. Se o Node subir, dá para migrar.

**Sem testes.** O `ng new` rodou com `--skip-tests`. O karma está instalado mas precisa de
um Chrome, que não existe neste WSL. A lógica que mais mereceria um teste é
`mensagemDeErro()`, com seus três formatos de erro.

**Tema único.** Claro, sem dark mode. Um a menos para manter.

---

## Coisas que faltam (por escolha, não por esquecimento)

- **Header `X-Cache`** na vitrine — troca a heurística por medição. É a melhoria com maior
  retorno da lista.
- **Botão "N leituras simultâneas"** para demonstrar o lock anti-stampede. São ~8 linhas,
  mas 10 cliques comem 10% do rate limit de 100/min — precisa pensar no orçamento antes.
- **Atualização automática** do trilho/tabelas por polling ou SSE. Hoje tudo é sob demanda,
  e isso é uma vantagem: nada acontece que você não tenha mandado acontecer.

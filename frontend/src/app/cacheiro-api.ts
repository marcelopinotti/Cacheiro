import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface Produto {
  id: number;
  nome: string;
  descricao: string;
  preco: number;
  estoque: number;
}

export type ProdutoEntrada = Omit<Produto, 'id'>;

export interface Pedido {
  id: string;
  produtoId: number;
  quantidade: number;
  precoUnitario: string;
  status: 'CRIADO' | 'PAGO' | 'ENVIADO' | 'CANCELADO';
  criadoEm: string;
  atualizadoEm: string;
}

export interface Chamada {
  seq: number;
  metodo: string;
  caminho: string;
  ms: number;
  status: number;
  erro?: string;
}

/** Acima disto a leitura quase certamente foi até o catálogo (300ms simulados lá). */
export const LIMIAR_MS = 150;

const MAX_LOG = 60;

@Injectable({ providedIn: 'root' })
export class CacheiroApi {
  private readonly http = inject(HttpClient);
  private seq = 0;

  private readonly _chamadas = signal<Chamada[]>([]);
  readonly chamadas = this._chamadas.asReadonly();

  readonly total = computed(() => this._chamadas().length);

  /** Latência da chamada mais recente — as telas leem logo depois do `await`. */
  readonly ultimaMs = computed(() => this._chamadas()[0]?.ms);

  
  readonly leiturasVitrine = computed(() =>
    this._chamadas().filter((c) => c.caminho.startsWith('/api/vitrine') && c.status < 400),
  );

  readonly taxaHit = computed(() => {
    const leituras = this.leiturasVitrine();
    if (!leituras.length) return null;
    const hits = leituras.filter((c) => c.ms < LIMIAR_MS).length;
    return Math.round((hits / leituras.length) * 100);
  });

  limparLog() {
    this._chamadas.set([]);
  }

  // 8080

  listarVitrine() {
    return this.pedir<Produto[]>('GET', '/api/vitrine');
  }

  detalharVitrine(id: number) {
    return this.pedir<Produto>('GET', `/api/vitrine/${id}`);
  }

  // 8081

  listarProdutos() {
    return this.pedir<Produto[]>('GET', '/api/produtos');
  }

  criarProduto(dados: ProdutoEntrada) {
    return this.pedir<Produto>('POST', '/api/produtos', dados);
  }

  atualizarProduto(id: number, dados: ProdutoEntrada) {
    return this.pedir<Produto>('PUT', `/api/produtos/${id}`, dados);
  }

  ajustarEstoque(id: number, delta: number) {
    return this.pedir<void>('PATCH', `/api/produtos/${id}/estoque`, { delta });
  }

  excluirProduto(id: number) {
    return this.pedir<void>('DELETE', `/api/produtos/${id}`);
  }

  // 8082

  listarPedidos() {
    return this.pedir<Pedido[]>('GET', '/api/pedido');
  }

  criarPedido(produtoId: number, quantidade: number) {
    return this.pedir<Pedido>('POST', '/api/pedido', { produtoId, quantidade });
  }

  mudarStatus(id: string, status: Pedido['status']) {
    return this.pedir<void>('PATCH', `/api/pedido/${id}/status`, { status });
  }

 
  private async pedir<T>(metodo: string, caminho: string, body?: unknown): Promise<T> {
    const inicio = performance.now();
    try {
      const resposta = await firstValueFrom(
        this.http.request<T>(metodo, caminho, { body, responseType: 'json' }),
      );
      this.registrar({ metodo, caminho, ms: decorrido(inicio), status: 200 });
      return resposta;
    } catch (e) {
      const err = e as HttpErrorResponse;
      const erro = mensagemDeErro(err);
      this.registrar({ metodo, caminho, ms: decorrido(inicio), status: err.status, erro });
      throw new Error(erro);
    }
  }

  private registrar(chamada: Omit<Chamada, 'seq'>) {
    this._chamadas.update((atual) => [{ seq: ++this.seq, ...chamada }, ...atual].slice(0, MAX_LOG));
  }
}

const decorrido = (inicio: number) => Math.round(performance.now() - inicio);

// Java devolve JSON, o Go devolve texto puro 
export function mensagemDeErro(err: HttpErrorResponse): string {
  if (err.status === 0) return 'serviço fora do ar';
  const corpo = err.error;
  if (typeof corpo === 'string' && corpo.trim()) return corpo.trim();
  if (corpo && typeof corpo === 'object') {
    const texto = corpo.erro ?? corpo.message ?? corpo.detail;
    if (typeof texto === 'string' && texto) return texto;
  }
  return `${err.status} ${err.statusText}`;
}

export const moeda = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

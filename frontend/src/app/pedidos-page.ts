import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CacheiroApi, Pedido, Produto, moeda } from './cacheiro-api';

/** Mesmas transições que o pedido-service aceita — o resto ele recusa com 409. */
const TRANSICOES: Record<string, Pedido['status'][]> = {
  CRIADO: ['PAGO', 'CANCELADO'],
  PAGO: ['ENVIADO', 'CANCELADO'],
};

@Component({
  selector: 'app-pedidos-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  template: `
    <p class="nota">
      Criar um pedido reserva o estoque no catálogo por HTTP e só depois grava no MongoDB. Se a gravação falha, o estoque volta.
       Cancelar devolve pelo mesmo caminho. O pedido-service não fala com o Redis em momento 
       nenhum: como a escrita passa pelo catálogo, a invalidação do cache já vem junto.
    </p>

    @if (erro(); as e) {
      <p class="aviso-erro" role="alert">{{ e }}</p>
    }

    <section class="painel">
      <header><h2>novo pedido</h2></header>
      <div class="conteudo">
        <form class="formulario" (ngSubmit)="criar()">
          <p class="grupo grande">
            <label class="campo-rotulo" for="produto">produto</label>
            <select id="produto" class="campo" name="produto" [(ngModel)]="produtoId">
              @for (p of produtos(); track p.id) {
                <option [ngValue]="p.id" [disabled]="p.estoque < 1">
                  #{{ p.id }} · {{ p.nome }} — {{ formatar(p.preco) }} ({{ p.estoque }} em estoque)
                </option>
              } @empty {
                <option [ngValue]="null" disabled>nenhum produto disponível</option>
              }
            </select>
          </p>
          <p class="grupo">
            <label class="campo-rotulo" for="qtd">quantidade</label>
            <input
              id="qtd"
              class="campo"
              type="number"
              min="1"
              required
              name="qtd"
              [(ngModel)]="quantidade"
            />
          </p>
          <p class="grupo acao">
            <button type="submit" class="btn btn-forte" [disabled]="ocupado() || !produtoId">
              criar pedido
            </button>
          </p>
        </form>
      </div>
    </section>

    <section class="painel">
      <header>
        <h2>pedidos</h2>
        <button type="button" class="btn" [disabled]="ocupado()" (click)="carregar()">
          recarregar
        </button>
      </header>

      <table class="tabela">
        <thead>
          <tr>
            <th scope="col">pedido</th>
            <th scope="col">produto</th>
            <th scope="col" class="num">qtd</th>
            <th scope="col" class="num">unitário</th>
            <th scope="col" class="num">total</th>
            <th scope="col">status</th>
            <th scope="col">criado</th>
            <th scope="col" class="acoes">ações</th>
          </tr>
        </thead>
        <tbody>
          @for (p of pedidos(); track p.id) {
            <tr>
              <td class="id">{{ p.id.slice(-6) }}</td>
              <td>{{ nomeDoProduto(p.produtoId) }}</td>
              <td class="num">{{ p.quantidade }}</td>
              <td class="num">{{ formatar(+p.precoUnitario) }}</td>
              <td class="num">{{ formatar(+p.precoUnitario * p.quantidade) }}</td>
              <td>
                <span class="selo" [class]="'estado-' + p.status.toLowerCase()">{{ p.status }}</span>
              </td>
              <td class="quando">{{ quando(p.criadoEm) }}</td>
              <td class="acoes">
                @for (destino of destinos(p.status); track destino) {
                  <button
                    type="button"
                    class="btn"
                    [class.btn-perigo]="destino === 'CANCELADO'"
                    [disabled]="ocupado()"
                    (click)="mudar(p, destino)"
                  >
                    {{ destino.toLowerCase() }}
                  </button>
                } @empty {
                  <span class="vazio">—</span>
                }
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="8" class="vazio">
                {{ ocupado() ? 'carregando…' : 'nenhum pedido ainda' }}
              </td>
            </tr>
          }
        </tbody>
      </table>
    </section>
  `,
  styles: `
    .formulario {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 10px 12px;
      align-items: end;
    }
    .grupo { margin: 0; }
    .grupo.grande { grid-column: span 2; }
    .grupo.acao { grid-column: 1 / -1; }
    .id { font-family: var(--mono); font-size: 11px; color: var(--tinta-3); }
    .quando { font-family: var(--mono); font-size: 11px; color: var(--tinta-3); white-space: nowrap; }
    .estado-criado { color: var(--tinta-2); }
    .estado-pago { color: var(--hit); }
    .estado-enviado { color: #2b4f7a; }
    .estado-cancelado { color: var(--erro); }
    @media (max-width: 720px) {
      .formulario { grid-template-columns: minmax(0, 1fr); }
      .grupo.grande { grid-column: auto; }
    }
  `,
})
export class PedidosPage implements OnInit {
  private readonly api = inject(CacheiroApi);

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly produtos = signal<Produto[]>([]);
  protected readonly ocupado = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected produtoId: number | null = null;
  protected quantidade = 1;

  private readonly porId = computed(() => new Map(this.produtos().map((p) => [p.id, p.nome])));

  protected readonly formatar = (v: number) => moeda.format(v);
  protected readonly destinos = (status: Pedido['status']) => TRANSICOES[status] ?? [];

  ngOnInit() {
    this.carregar();
  }

  protected nomeDoProduto(id: number) {
    return this.porId().get(id) ?? `#${id}`;
  }

  protected quando(iso: string) {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'medium' });
  }

  protected async carregar() {
    await this.executar(async () => {
      const [pedidos, produtos] = await Promise.all([
        this.api.listarPedidos(),
        this.api.listarVitrine(),
      ]);
      this.pedidos.set(pedidos);
      this.produtos.set(produtos);
      this.produtoId ??= produtos.find((p) => p.estoque > 0)?.id ?? null;
    });
  }

  protected async criar() {
    if (!this.produtoId || this.quantidade < 1) return;
    const ok = await this.executar(async () => {
      await this.api.criarPedido(this.produtoId!, Number(this.quantidade));
    });
    if (ok) await this.carregar();
  }

  protected async mudar(pedido: Pedido, status: Pedido['status']) {
    const ok = await this.executar(async () => {
      await this.api.mudarStatus(pedido.id, status);
    });
    if (ok) await this.carregar();
  }

  /** Devolve se deu certo: quem recarrega depois não pode apagar a mensagem de erro. */
  private async executar(acao: () => Promise<void>): Promise<boolean> {
    this.ocupado.set(true);
    this.erro.set(null);
    try {
      await acao();
      return true;
    } catch (e) {
      this.erro.set((e as Error).message);
      return false;
    } finally {
      this.ocupado.set(false);
    }
  }
}

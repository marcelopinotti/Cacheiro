import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CacheiroApi, Produto, moeda } from './cacheiro-api';
import { Latencia } from './latencia';
import { ProdutoLinha } from './produto-linha';

@Component({
  selector: 'app-vitrine-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Latencia, ProdutoLinha],
  template: `
    <p class="nota">
      Toda leitura passa pela vitrine, que procura primeiro no Redis e só vai ao catálogo se não achar.
       A primeira leitura de um produto demora uns 300ms simulados. As próximas saem do cache até o TTL vencer: 45s no detalhe, 20s na lista.
        Quando você pede 1, o catálogo baixa o estoque e publica a invalidação, então a leitura seguinte volta a demorar.
    </p>

    @if (erro(); as e) {
      <p class="aviso-erro" role="alert">{{ e }}</p>
    }

    <section class="painel">
      <header>
        <h2>produtos</h2>
        <span class="cabecalho-direita">
          <app-latencia [ms]="msLista()" />
          <button type="button" class="btn" [disabled]="ocupado()" (click)="carregar()">
            reler lista
          </button>
        </span>
      </header>

      <table class="tabela">
        <thead>
          <tr>
            <th scope="col">id</th>
            <th scope="col">produto</th>
            <th scope="col" class="num">preço</th>
            <th scope="col" class="num">estoque</th>
            <th scope="col">última leitura</th>
            <th scope="col"><span class="oculto">ações</span></th>
          </tr>
        </thead>
        <tbody>
          @for (p of produtos(); track p.id) {
            <tr
              [produto]="p"
              [ms]="leituras()[p.id]"
              [selecionada]="detalhe()?.id === p.id"
              [ocupada]="ocupado()"
              (detalhar)="detalhar($event)"
              (pedir)="pedir($event)"
            ></tr>
          } @empty {
            <tr>
              <td colspan="6" class="vazio">
                {{ ocupado() ? 'carregando…' : 'nenhum produto na vitrine' }}
              </td>
            </tr>
          }
        </tbody>
      </table>
    </section>

    @if (detalhe(); as d) {
      <section class="painel">
        <header>
          <h2>detalhe · vitrine:produto:{{ d.id }}</h2>
          <span class="cabecalho-direita">
            <app-latencia [ms]="leituras()[d.id]" />
            <button type="button" class="btn" [disabled]="ocupado()" (click)="detalhar(d.id)">
              ler de novo
            </button>
          </span>
        </header>
        <div class="conteudo detalhe">
          <dl>
            <dt>nome</dt>
            <dd>{{ d.nome }}</dd>
            <dt>descrição</dt>
            <dd>{{ d.descricao || '—' }}</dd>
            <dt>preço</dt>
            <dd class="mono">{{ formatar(d.preco) }}</dd>
            <dt>estoque</dt>
            <dd class="mono">{{ d.estoque }}</dd>
          </dl>
          <p class="dica">
            A segunda deve vir do cache. Espere 45s ou edite o produto no
            catálogo e a chave morre
          </p>
        </div>
      </section>
    }
  `,
  styles: `
    .cabecalho-direita { display: flex; align-items: center; gap: 12px; }
    .oculto {
      position: absolute;
      width: 1px;
      height: 1px;
      overflow: hidden;
      clip-path: inset(50%);
    }
    dl {
      margin: 0;
      display: grid;
      grid-template-columns: 7rem minmax(0, 1fr);
      gap: 4px 16px;
      font-size: 13px;
    }
    dt {
      font-size: 10px;
      font-weight: 600;
      letter-spacing: 0.09em;
      text-transform: uppercase;
      color: var(--tinta-3);
      padding-top: 2px;
    }
    dd { margin: 0; }
    .mono { font-family: var(--mono); }
    .dica { margin: 14px 0 0; font-size: 12px; color: var(--tinta-3); max-width: 70ch; }
  `,
})
export class VitrinePage implements OnInit {
  private readonly api = inject(CacheiroApi);

  protected readonly produtos = signal<Produto[]>([]);
  protected readonly detalhe = signal<Produto | null>(null);
  protected readonly leituras = signal<Record<number, number>>({});
  protected readonly msLista = signal<number | undefined>(undefined);
  protected readonly ocupado = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formatar = (v: number) => moeda.format(v);

  ngOnInit() {
    this.carregar();
  }

  protected async carregar() {
    await this.executar(async () => {
      this.produtos.set(await this.api.listarVitrine());
      this.msLista.set(this.api.ultimaMs());
    });
  }

  protected async detalhar(id: number) {
    await this.executar(async () => {
      const produto = await this.api.detalharVitrine(id);
      this.leituras.update((atual) => ({ ...atual, [id]: this.api.ultimaMs()! }));
      this.detalhe.set(produto);
    });
  }

  protected async pedir(id: number) {
    const ok = await this.executar(async () => {
      await this.api.criarPedido(id, 1);
      
      this.produtos.set(await this.api.listarVitrine());
      this.msLista.set(this.api.ultimaMs());
    });
    if (ok && this.detalhe()?.id === id) await this.detalhar(id);
  }

  /** Devolve se deu certo*/
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

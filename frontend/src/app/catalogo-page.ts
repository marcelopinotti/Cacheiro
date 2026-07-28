import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CacheiroApi, Produto, ProdutoEntrada, moeda } from './cacheiro-api';
import { Latencia } from './latencia';

interface Rascunho extends ProdutoEntrada {
  id: number | null;
}

const vazio = (): Rascunho => ({ id: null, nome: '', descricao: '', preco: 0, estoque: 0 });

@Component({
  selector: 'app-catalogo-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, Latencia],
  template: `
    <p class="nota">
  Esta é a fonte da verdade, direto no PostgreSQL. Toda escrita aqui publica o id no canal
  <code>produtos:invalidacao</code> depois do commit, e a vitrine apaga
  <code>vitrine:produto:&#123;id&#125;</code> e <code>vitrine:produtos:all</code>. Mude um preço
  e volte para a vitrine: o dado novo aparece sem esperar o TTL. Como esta tela é a origem, as
  leituras dela sempre marcam <em>origem</em> e levam os 300ms simulados.
</p>

    @if (erro(); as e) {
      <p class="aviso-erro" role="alert">{{ e }}</p>
    }

    <section class="painel">
      <header>
        <h2>{{ editandoId() === null ? 'novo produto' : 'editando #' + editandoId() }}</h2>
        @if (editandoId() !== null) {
          <button type="button" class="btn" (click)="limpar()">cancelar</button>
        }
      </header>
      <div class="conteudo">
        <form class="formulario" (ngSubmit)="salvar()">
          <p class="grupo grande">
            <label class="campo-rotulo" for="nome">nome</label>
            <input id="nome" class="campo" required [(ngModel)]="rascunho.nome" name="nome" />
          </p>
          <p class="grupo grande">
            <label class="campo-rotulo" for="descricao">descrição</label>
            <input id="descricao" class="campo" [(ngModel)]="rascunho.descricao" name="descricao" />
          </p>
          <p class="grupo">
            <label class="campo-rotulo" for="preco">preço</label>
            <input
              id="preco"
              class="campo"
              type="number"
              min="0"
              step="0.01"
              required
              [(ngModel)]="rascunho.preco"
              name="preco"
            />
          </p>
          <p class="grupo">
            <label class="campo-rotulo" for="estoque">estoque</label>
            <input
              id="estoque"
              class="campo"
              type="number"
              min="0"
              required
              [(ngModel)]="rascunho.estoque"
              name="estoque"
            />
          </p>
          <p class="grupo acao">
            <button type="submit" class="btn btn-forte" [disabled]="ocupado()">
              {{ editandoId() === null ? 'criar' : 'salvar' }}
            </button>
          </p>
        </form>
      </div>
    </section>

    <section class="painel">
      <header>
        <h2>catálogo</h2>
        <span class="cabecalho-direita">
          <app-latencia [ms]="msLista()" />
          <button type="button" class="btn" [disabled]="ocupado()" (click)="carregar()">
            recarregar
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
            <th scope="col" class="acoes">ações</th>
          </tr>
        </thead>
        <tbody>
          @for (p of produtos(); track p.id) {
            <tr [class.selecionada]="editandoId() === p.id">
              <td class="id">{{ p.id }}</td>
              <td>
                {{ p.nome }}
                @if (p.descricao) {
                  <span class="descricao">{{ p.descricao }}</span>
                }
              </td>
              <td class="num">{{ formatar(p.preco) }}</td>
              <td class="num" [class.esgotado]="p.estoque < 1">{{ p.estoque }}</td>
              <td class="acoes">
                <button
                  type="button"
                  class="btn"
                  [disabled]="ocupado() || p.estoque < 1"
                  [attr.aria-label]="'diminuir estoque de ' + p.nome"
                  (click)="ajustar(p.id, -1)"
                >
                  −1
                </button>
                <button
                  type="button"
                  class="btn"
                  [disabled]="ocupado()"
                  [attr.aria-label]="'aumentar estoque de ' + p.nome"
                  (click)="ajustar(p.id, 1)"
                >
                  +1
                </button>
                <button type="button" class="btn" [disabled]="ocupado()" (click)="editar(p)">
                  editar
                </button>
                <button
                  type="button"
                  class="btn btn-perigo"
                  [disabled]="ocupado()"
                  (click)="excluir(p)"
                >
                  excluir
                </button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="5" class="vazio">
                {{ ocupado() ? 'carregando…' : 'catálogo vazio' }}
              </td>
            </tr>
          }
        </tbody>
      </table>
    </section>
  `,
  styles: `
    .cabecalho-direita { display: flex; align-items: center; gap: 12px; }
    .formulario {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 10px 12px;
      align-items: end;
    }
    .grupo { margin: 0; }
    .grupo.grande { grid-column: span 2; }
    .grupo.acao { grid-column: 1 / -1; }
    .id { font-family: var(--mono); font-size: 11px; color: var(--tinta-3); }
    .descricao { display: block; font-size: 12px; color: var(--tinta-3); }
    .esgotado { color: var(--erro); }
    code { font-family: var(--mono); font-size: 12px; background: var(--realce); padding: 0 3px; }
    @media (max-width: 720px) {
      .formulario { grid-template-columns: minmax(0, 1fr); }
      .grupo.grande { grid-column: auto; }
    }
  `,
})
export class CatalogoPage implements OnInit {
  private readonly api = inject(CacheiroApi);

  protected readonly produtos = signal<Produto[]>([]);
  /** Formulário template-driven: o ngModel escreve direto aqui, sem sinal no meio. */
  protected rascunho: Rascunho = vazio();
  protected readonly editandoId = signal<number | null>(null);
  protected readonly msLista = signal<number | undefined>(undefined);
  protected readonly ocupado = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formatar = (v: number) => moeda.format(v);

  ngOnInit() {
    this.carregar();
  }

  protected async carregar() {
    await this.executar(async () => {
      this.produtos.set(await this.api.listarProdutos());
      this.msLista.set(this.api.ultimaMs());
    });
  }

  protected editar(p: Produto) {
    this.rascunho = { ...p };
    this.editandoId.set(p.id);
  }

  protected limpar() {
    this.rascunho = vazio();
    this.editandoId.set(null);
  }

  protected async salvar() {
    const { id, ...dados } = this.rascunho;
    if (!dados.nome.trim()) {
      this.erro.set('nome é obrigatório');
      return;
    }
    await this.executar(async () => {
      if (id === null) await this.api.criarProduto(dados);
      else await this.api.atualizarProduto(id, dados);
      this.limpar();
      this.produtos.set(await this.api.listarProdutos());
      this.msLista.set(this.api.ultimaMs());
    });
  }

  protected async ajustar(id: number, delta: number) {
    await this.executar(async () => {
      await this.api.ajustarEstoque(id, delta);
      this.produtos.set(await this.api.listarProdutos());
      this.msLista.set(this.api.ultimaMs());
    });
  }

  protected async excluir(p: Produto) {
    if (!confirm(`Excluir "${p.nome}" do catálogo?`)) return;
    await this.executar(async () => {
      await this.api.excluirProduto(p.id);
      if (this.editandoId() === p.id) this.limpar();
      this.produtos.set(await this.api.listarProdutos());
      this.msLista.set(this.api.ultimaMs());
    });
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

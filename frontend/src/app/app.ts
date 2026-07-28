import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CacheiroApi, LIMIAR_MS } from './cacheiro-api';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topo">
      <span class="marca">cacheiro</span>
      <nav aria-label="Seções">
        <a routerLink="/vitrine" routerLinkActive="ativo">vitrine</a>
        <a routerLink="/pedidos" routerLinkActive="ativo">pedidos</a>
        <a routerLink="/catalogo" routerLinkActive="ativo">catálogo</a>
        <a routerLink="/metricas" routerLinkActive="ativo">métricas</a>
      </nav>
      <span class="legenda">
        <i class="ponto hit"></i> cache
        <i class="ponto miss"></i> origem &ge;{{ limiar }}ms
      </span>
    </header>

    <div class="corpo">
      <main><router-outlet /></main>

      <aside class="trilho" aria-label="Registro de requisições">
        <header>
          <h2>requisições</h2>
          <button type="button" class="btn" (click)="api.limparLog()" [disabled]="!api.total()">
            limpar
          </button>
        </header>

        @if (api.taxaHit(); as taxa) {
          <p class="resumo">
            <strong>{{ taxa }}%</strong> das {{ api.leiturasVitrine().length }} leituras da vitrine
            vieram do cache
          </p>
        }

        <ol class="registro">
          @for (c of api.chamadas(); track c.seq) {
            <li [class.falha]="c.status >= 400">
              <span class="metodo">{{ c.metodo }}</span>
              <span class="caminho">{{ c.caminho }}</span>
              <span class="ms" [class.origem]="c.ms >= limiar">{{ c.ms }}ms</span>
              <span class="status">{{ c.status }}</span>
              @if (c.erro) {
                <span class="detalhe">{{ c.erro }}</span>
              }
            </li>
          } @empty {
            <li class="vazio">nenhuma chamada ainda</li>
          }
        </ol>
      </aside>
    </div>
  `,
  styles: `
    .topo {
      position: sticky;
      top: 0;
      z-index: 2;
      display: flex;
      align-items: center;
      gap: 24px;
      height: 44px;
      padding: 0 16px;
      background: var(--superficie);
      border-bottom: 1px solid var(--linha-forte);
    }

    .marca {
      font-family: var(--mono);
      font-size: 13px;
      font-weight: 600;
      letter-spacing: 0.02em;
    }

    nav { display: flex; gap: 18px; }

    nav a {
      color: var(--tinta-2);
      font-size: 13px;
      text-decoration: none;
      padding: 2px 0;
      border-bottom: 2px solid transparent;
    }
    nav a:hover { color: var(--tinta); }
    nav a.ativo { color: var(--tinta); border-bottom-color: var(--tinta); }

    .legenda {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--tinta-3);
      font-size: 11px;
      font-family: var(--mono);
    }
    .ponto { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
    .ponto.hit { background: var(--hit); }
    .ponto.miss { background: var(--miss); margin-left: 6px; }

    .corpo {
      display: grid;
      grid-template-columns: minmax(0, 1fr) 320px;
      align-items: start;
    }

    main { padding: 20px 16px 60px; min-width: 0; }

    .trilho {
      position: sticky;
      top: 44px;
      height: calc(100vh - 44px);
      display: flex;
      flex-direction: column;
      background: var(--superficie);
      border-left: 1px solid var(--linha);
    }

    .trilho > header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      border-bottom: 1px solid var(--linha);
    }

    .trilho h2 {
      margin: 0;
      font-size: 10px;
      font-weight: 600;
      letter-spacing: 0.09em;
      text-transform: uppercase;
      color: var(--tinta-3);
    }

    .resumo {
      margin: 0;
      padding: 8px 12px;
      border-bottom: 1px solid var(--linha);
      font-size: 12px;
      color: var(--tinta-2);
      background: var(--realce);
    }
    .resumo strong { font-family: var(--mono); color: var(--tinta); }

    .registro {
      flex: 1;
      overflow-y: auto;
      margin: 0;
      padding: 0;
      list-style: none;
      font-family: var(--mono);
      font-size: 11px;
    }

    .registro li {
      display: grid;
      grid-template-columns: 3.2rem minmax(0, 1fr) 3.4rem 2rem;
      gap: 6px;
      align-items: baseline;
      padding: 4px 12px;
      border-bottom: 1px solid #f1efe9;
      color: var(--tinta-2);
    }

    .metodo { color: var(--tinta-3); }
    .caminho { color: var(--tinta); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .ms { text-align: right; color: var(--hit); font-variant-numeric: tabular-nums; }
    .ms.origem { color: var(--miss); }
    .status { text-align: right; color: var(--tinta-3); }
    .detalhe { grid-column: 2 / -1; color: var(--erro); white-space: normal; }
    .registro li.falha .ms,
    .registro li.falha .status { color: var(--erro); }
    .registro li.vazio { display: block; color: var(--tinta-3); }

    @media (max-width: 900px) {
      .corpo { grid-template-columns: minmax(0, 1fr); }
      .trilho {
        position: static;
        height: auto;
        max-height: 280px;
        border-left: 0;
        border-top: 1px solid var(--linha);
      }
    }
  `,
})
export class App {
  protected readonly api = inject(CacheiroApi);
  protected readonly limiar = LIMIAR_MS;
}

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { LIMIAR_MS } from './cacheiro-api';


const ESCALA_MS = 400;

@Component({
  selector: 'app-latencia',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class.origem]': 'lento()',
    '[attr.title]': 'legenda()',
  },
  template: `
    @if (ms() === undefined) {
      <span class="vazio">—</span>
    } @else {
      <span class="valor">{{ ms() }}ms</span>
      <span class="trilha" aria-hidden="true">
        <span class="preenchimento" [style.width.%]="largura()"></span>
      </span>
      <span class="origem-rotulo">{{ rotulo() }}</span>
    }
  `,
  styles: `
    :host {
      display: grid;
      grid-template-columns: 4.5rem 1fr 3.5rem;
      align-items: center;
      gap: 8px;
      color: var(--hit);
      font-family: var(--mono);
      font-size: 12px;
      min-width: 190px;
    }
    :host.origem { color: var(--miss); }
    .valor { font-variant-numeric: tabular-nums; text-align: right; }
    .trilha { height: 6px; background: var(--realce); border: 1px solid var(--linha); }
    .preenchimento { display: block; height: 100%; background: currentColor; }
    .origem-rotulo { font-size: 10px; letter-spacing: 0.06em; text-transform: uppercase; }
    .vazio { grid-column: 1 / -1; text-align: right; }
  `,
})
export class Latencia {
  readonly ms = input<number>();

  protected readonly lento = computed(() => (this.ms() ?? 0) >= LIMIAR_MS);
  protected readonly rotulo = computed(() => (this.lento() ? 'origem' : 'cache'));
  protected readonly largura = computed(() =>
    Math.min(100, Math.round(((this.ms() ?? 0) / ESCALA_MS) * 100)),
  );
  protected readonly legenda = computed(() =>
    this.lento()
      ? `${this.ms()}ms — miss: foi até o catálogo`
      : `${this.ms()}ms — hit: veio do Redis`,
  );
}

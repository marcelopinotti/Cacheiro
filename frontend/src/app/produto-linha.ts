import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Produto, moeda } from './cacheiro-api';
import { Latencia } from './latencia';

@Component({
  selector: 'tr[produto]',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Latencia],
  host: {
    '[class.selecionada]': 'selecionada()',
  },
  template: `
    <td class="id">{{ produto().id }}</td>
    <td>
      <button type="button" class="link" (click)="detalhar.emit(produto().id)">
        {{ produto().nome }}
      </button>
    </td>
    <td class="num">{{ precoFormatado() }}</td>
    <td class="num" [class.esgotado]="esgotado()">{{ produto().estoque }}</td>
    <td><app-latencia [ms]="ms()" /></td>
    <td class="acoes">
      <button
        type="button"
        class="btn"
        [disabled]="esgotado() || ocupada()"
        (click)="pedir.emit(produto().id)"
      >
        pedir 1
      </button>
    </td>
  `,
  styles: `
    .id { font-family: var(--mono); font-size: 11px; color: var(--tinta-3); }
    .esgotado { color: var(--erro); }
  `,
})
export class ProdutoLinha {
  readonly produto = input.required<Produto>();
  readonly ms = input<number>();
  readonly selecionada = input(false);
  readonly ocupada = input(false);

  readonly detalhar = output<number>();
  readonly pedir = output<number>();

  protected readonly esgotado = computed(() => this.produto().estoque < 1);
  protected readonly precoFormatado = computed(() => moeda.format(this.produto().preco));
}

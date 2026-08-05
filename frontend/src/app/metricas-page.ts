import { ChangeDetectionStrategy, Component } from '@angular/core';


@Component({
  selector: 'app-metricas-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p class="nota">
  É o mesmo dashboard provisionado em <code>observability/grafana-dashboard.json</code>, com hit ratio,
  p95 por rota, throughput e 429/s. O registro à direita mede o que <em>este navegador</em> viu.
  Aqui embaixo é o que o Prometheus raspou dos serviços.
</p>

    <section class="painel">
      <header>
        <h2>grafana · cacheiro-vitrine</h2>
        <a class="btn" href="/grafana/d/cacheiro-vitrine" target="_blank" rel="noopener">
          abrir no grafana
        </a>
      </header>
      <iframe
        title="Dashboard Cacheiro no Grafana"
        src="/grafana/d/cacheiro-vitrine/cacheiro-vitrine?orgId=1&kiosk&refresh=10s&from=now-15m&to=now"
      ></iframe>
    </section>

    <p class="nota">
      Em branco? O Grafana só aceita ser embutido com
      <code>GF_SECURITY_ALLOW_EMBEDDING=true</code> e login anônimo — os dois já estão no
      <code>docker-compose.yaml</code>. Se você subiu antes dessa mudança, recrie o container.
    </p>
  `,
  styles: `
    iframe { display: block; width: 100%; height: 78vh; min-height: 520px; border: 0; }
    .btn { text-decoration: none; }
    code { font-family: var(--mono); font-size: 12px; background: var(--realce); padding: 0 3px; }
  `,
})
export class MetricasPage {}

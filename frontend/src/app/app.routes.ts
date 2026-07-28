import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'vitrine' },
  {
    path: 'vitrine',
    title: 'vitrine · cacheiro',
    loadComponent: () => import('./vitrine-page').then((m) => m.VitrinePage),
  },
  {
    path: 'pedidos',
    title: 'pedidos · cacheiro',
    loadComponent: () => import('./pedidos-page').then((m) => m.PedidosPage),
  },
  {
    path: 'catalogo',
    title: 'catálogo · cacheiro',
    loadComponent: () => import('./catalogo-page').then((m) => m.CatalogoPage),
  },
  {
    path: 'metricas',
    title: 'métricas · cacheiro',
    loadComponent: () => import('./metricas-page').then((m) => m.MetricasPage),
  },
  { path: '**', redirectTo: 'vitrine' },
];

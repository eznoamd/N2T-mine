/**
 * Sistema de Circuito Integrado (CI).
 *
 * <p>Cada design de CI vive numa sala dentro da dimensao {@code n2t-mine:ic_world}.
 * O modelo central e <b>mestre + instancia</b>:
 *
 * <ul>
 *   <li><b>Mestre</b>: uma sala editavel por design. E onde o jogador entra ao
 *       clicar num bloco de CI e monta o circuito.</li>
 *   <li><b>Instancia</b>: um clone fisico do mestre, um por bloco colocado, que
 *       roda com o I/O daquele bloco especifico. Ao sair do mestre, todas as
 *       instancias daquele design sao reclonadas (a edicao propaga).</li>
 * </ul>
 *
 * <p>Organizacao dos subpacotes:
 * <ul>
 *   <li>{@code ic} — blocos, block entities, estado das salas e registros.</li>
 *   <li>{@code ic.net} — pacotes de rede cliente-servidor.</li>
 *   <li>{@code ic.recipe} — receita especial de copia de CI.</li>
 *   <li>{@code ic.screen} — menu do bloco de saida (lado servidor/compartilhado).</li>
 * </ul>
 */
package com.enzo.n2tmine.ic;

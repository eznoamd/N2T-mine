# Sistema de CI (Circuito Integrado) — MVP

## Como integrar no seu projeto

1. Copie a pasta `src/main/java/com/enzo/n2tmine/ic/` inteira pra dentro do
   seu `src/main/java/com/enzo/n2tmine/` já existente.

2. Copie tudo que está dentro de `src/main/resources/` deste pacote pra
   dentro do `src/main/resources/` do seu projeto (mescla as pastas
   `data/n2t-mine/` e `assets/n2t-mine/` com o que já existe).

3. Abra o arquivo `N2TMine.java.novo` incluído aqui, compare com o seu
   `N2TMine.java` atual, e adicione as partes novas (basicamente as duas
   linhas `ModIcBlocks.initialize()` / `ModIcBlockEntities.initialize()`
   dentro do `onInitialize()`, mais o `import` correspondente).

4. Rode:
   ```bash
   ./gradlew build
   ```
   **Muito provavelmente vai dar erro de compilação em uma ou duas linhas**
   — os métodos mais arriscados são:
   - `onUse(...)` em `IcBlock.java` e `IcExitBlock.java`
   - `player.teleport(...)` em `IcBlockEntity.java` e `IcExitBlockEntity.java`

   Isso é esperado: essas assinaturas mudam com frequência entre versões do
   Minecraft e eu não tenho como compilar de verdade daqui. **Me manda o
   erro exato do terminal** que eu ajusto rapidinho — geralmente é só trocar
   o nome/ordem de um parâmetro.

## Como testar

1. Crie um mundo **novo** (mundos já existentes podem não reconhecer a
   dimensão customizada até você recarregar o mundo pelo menos uma vez após
   o mod ser carregado)
2. Pegue o `IC Block` na aba de Redstone do inventário criativo
3. Coloque ele no chão
4. Clique com o botão direito nele → deve te teleportar pra dentro de uma
   sala vazia (paredes de stone bricks)
5. Dentro da sala, as paredes Norte/Sul/Leste/Oeste têm um bloco azul
   (lápis) no meio — é a "porta" que representa aquela face do bloco lá fora
6. Coloque um bloco de redstone dust ligando duas portas (ex: da porta
   Norte até a porta Sul) pra simular um fio passando direto
7. O bloco dourado no centro do chão te teleporta de volta pra fora
8. Lá fora: coloque uma alavanca numa face do IC Block e uma lâmpada de
   redstone na face oposta — se você ligou as portas correspondentes lá
   dentro com um fio de redstone, a lâmpada deve acender quando você puxar
   a alavanca

## Limitações conhecidas desse MVP (de propósito, pra manter simples)

- Só as 4 direções horizontais (Norte/Sul/Leste/Oeste) têm porta. Cima/baixo
  não foram implementados ainda (dá pra adicionar depois, seguindo o mesmo
  padrão das outras 4).
- A sincronização é por *polling* a cada tick, não por evento — funciona
  bem pra poucos blocos de CI, mas se você criar centenas deles pode começar
  a pesar um pouco no desempenho do servidor.
- Não há proteção contra múltiplos jogadores usando o mesmo CI ao mesmo
  tempo — cada CI tem exatamente uma sala fixa.
- Sem GUI nenhuma — a "programação" do circuito é 100% feita fisicamente
  com redstone de verdade dentro da sala, o que é exatamente o que você
  pediu.
- As texturas são só placeholders (blocos de ferro/lápis/ouro reaproveitados)
  — troque pelos seus próprios modelos/texturas quando quiser.

## Próximos passos possíveis (depois que isso estiver rodando)

- Adicionar portas verticais (cima/baixo)
- Fazer a sala ser maior/configurável por tamanho
- Adicionar um item que, ao segurar, mostra info da sala (tipo um "medidor")
- Adicionar suporte a múltiplos jogadores simultâneos numa mesma sala
- Trocar o polling por notificação orientada a eventos (mais complexo, só
  vale a pena se performance virar um problema real)

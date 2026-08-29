# UNIMOVE — Plano de reestruturação de produto, UX e interface

## 1. Resumo executivo

O UNIMOVE não deve se apresentar como um conjunto de cadastros. Ele deve ser percebido como o sistema que transforma demanda recorrente de estudantes em viagens universitárias previsíveis, fáceis de agendar e simples de operar.

A proposta é organizar o produto em três experiências conectadas, mas específicas:

1. **Aluno — mobile first:** descobrir, agendar e acompanhar uma viagem com o mínimo de incerteza.
2. **Condutor — mobile operacional:** saber o que fazer agora, consultar rota e passageiros e avançar a viagem.
3. **Gestor — desktop first:** planejar oferta, acompanhar demanda, alocar recursos e resolver exceções.

A direção recomendada é uma evolução de produto, e não apenas uma troca de cores. Ela combina nova arquitetura de informação, jornadas redesenhadas, interface integralmente em inglês, identidade visual própria, componentes reutilizáveis e ajustes no domínio/API. O planejamento pode permanecer documentado em português, mas todo conteúdo exibido pelo sistema deve seguir o padrão de idioma definido para o produto.

## 2. Leitura do produto atual

### 2.1 Modelo funcional encontrado

O fluxo central implementado é:

`Lista aberta por data/destino → reserva do aluno → fechamento da demanda → geração da viagem → alocação de condutor e veículo → consulta da viagem`

Entidades principais:

- usuário com papel de administrador, aluno ou condutor;
- aluno associado a universidade e ponto de embarque preferencial;
- condutor com habilitação e validade;
- universidade, ponto de embarque e veículo;
- lista de interesse com data, janela de reserva e horários;
- reserva com modalidade (ida, volta ou ida e volta), destino e embarque;
- viagem gerada a partir da lista, com condutor, veículo, status e passageiros.

### 2.2 Superfícies atuais

**Aluno**

- home ainda sem conteúdo;
- listas de interesse e criação de reserva em modal;
- tela conjunta de reservas e viagens;
- perfil.

**Condutor**

- home ainda sem conteúdo;
- lista de viagens;
- detalhe com uma tabela simples de passageiros;
- perfil.

**Administrador**

- home ainda sem conteúdo;
- CRUDs de usuários, universidades, pontos, veículos e listas;
- geração de viagem a partir de uma lista;
- edição de condutor e veículo da viagem;
- consulta de reservas e passageiros.

### 2.3 Diagnóstico do estado atual

#### Problemas de produto e arquitetura da informação

- A navegação administrativa é uma lista plana de entidades, não um fluxo de trabalho.
- “Interest List” é linguagem interna do sistema; para o aluno, a intenção real é “encontrar e agendar uma viagem”.
- Reservas e viagens aparecem como conceitos paralelos, embora sejam etapas da mesma jornada.
- A recorrência, que é central à proposta do produto, ainda não aparece como recurso de planejamento ou repetição.
- Aluno e condutor compartilham quase a mesma estrutura mobile, embora tenham objetivos completamente diferentes.
- As homes vazias desperdiçam o melhor ponto para orientar cada papel.
- O gestor precisa navegar por páginas separadas para entender demanda, gerar viagem e alocar recursos.

#### Problemas de interação

- A criação da reserva acontece em um modal longo, com pouca sensação de progresso e pouco resumo antes da confirmação.
- O ponto de embarque preferencial é usado implicitamente pela API, mas não fica claro na confirmação do aluno.
- A possibilidade de editar ou cancelar antes do fechamento é informada, mas não está convertida em ação evidente.
- Estados importantes dependem quase somente de cor e de textos técnicos.
- Tabelas são reutilizadas na experiência do condutor, mesmo em uma tela mobile.
- Ações administrativas perigosas ficam próximas de ações comuns e aparecem como links de texto.
- Filtros por data, destino, status e situação operacional são insuficientes.
- Não há estados vazios instrutivos, skeletons, feedback persistente ou orientação de próximo passo consistente.

#### Problemas visuais e de conteúdo

- A aparência é majoritariamente a combinação padrão de Tailwind e Material, sem uma assinatura reconhecível.
- A hierarquia entre informação principal, metadados e ações é fraca.
- Há mistura de inglês e português e textos provisórios; nesta fase, o inglês deve ser o único idioma da interface.
- Datas e horários são exibidos como dados crus, sem contexto como “Today”, “Tomorrow” ou “Bookings close at 4:00 PM”.
- Cartões repetem pares de rótulo/valor sem construir uma leitura visual de rota.
- O azul e os cinzas são usados genericamente, sem semântica consistente.
- Densidade, espaçamento, raios e pesos tipográficos variam entre telas.

## 3. Públicos e resultados esperados

### 3.1 Aluno

**Contexto:** usa o celular, muitas vezes com pressa, uma mão livre e conexão instável. Quer segurança de que terá transporte.

**Perguntas que a interface deve responder:**

- Tem viagem para minha universidade no dia desejado?
- Até quando posso reservar?
- Qual é meu horário e ponto de embarque?
- Minha vaga está confirmada?
- Houve alguma mudança?

**Resultado ideal:** reserva confirmada em menos de um minuto, sem precisar compreender o modelo interno do sistema.

### 3.2 Condutor

**Contexto:** opera em mobilidade. Precisa de informação objetiva, alta legibilidade e poucas decisões por tela.

**Perguntas que a interface deve responder:**

- Qual é minha próxima viagem?
- Que veículo devo usar?
- Onde e quando começo?
- Quem são os passageiros e onde embarcam?
- Existe alguma pendência operacional?

**Resultado ideal:** conduzir a operação do dia sem recorrer a listas externas ou mensagens dispersas.

### 3.3 Gestor/organizador

**Contexto:** trabalha principalmente no desktop, compara muitos dados e lida com exceções.

**Perguntas que a interface deve responder:**

- Quais datas ainda precisam de planejamento?
- Onde a demanda está acima ou abaixo da capacidade?
- Quais viagens estão sem veículo ou condutor?
- O que exige ação agora?
- Qual foi o desempenho recente da operação?

**Resultado ideal:** planejar e acompanhar viagens por exceção, com menos navegação e menos conferência manual.

## 4. Princípios da nova experiência

1. **Tarefa antes da entidade:** usar verbos e intenções do usuário.
2. **Próxima ação sempre visível:** cada tela deve deixar claro o que acontece agora.
3. **Confiança operacional:** status, prazo, local e horário nunca podem ser ambíguos.
4. **Um produto, três ritmos:** identidade comum, densidade e interação específicas por papel.
5. **Recorrência por padrão:** duplicar e programar rotinas deve ser mais fácil que cadastrar do zero.
6. **Exceções em destaque:** o gestor deve enxergar pendências antes de registros saudáveis.
7. **Inglês consistente em toda a interface:** navegação, rótulos, estados, validações, feedback e mensagens de erro devem usar a mesma convenção de inglês. Formatos de data e horário devem ser definidos separadamente conforme o mercado inicial, sem misturar idioma e localização.
8. **Acessível e resiliente:** contraste AA, alvos de toque de ao menos 44 px, foco visível, informação além da cor e bom comportamento com rede lenta.

## 5. Nova arquitetura de informação

### 5.1 Aplicativo do aluno

Barra inferior recomendada:

- **Home**
- **Book a Trip**
- **My Trips**
- **Profile**

“Interest Lists” deixa de ser uma seção visível ao aluno. Ela continua existindo no domínio, mas é apresentada como **Available Trips**.

### 5.2 Aplicativo do condutor

Barra inferior recomendada:

- **Today**
- **Schedule**
- **Alerts** (quando houver infraestrutura)
- **Profile**

### 5.3 Painel administrativo

Navegação agrupada:

- **Overview**
- **Operations**
  - Trip Schedule
  - Demand & Bookings
  - Issues
- **People**
  - Students
  - Drivers
- **Resources**
  - Vehicles
  - Universities
  - Boarding Stops
- **Settings**
  - Users & Access
  - System Preferences

No desktop, a agenda e o detalhe operacional devem ser o centro do produto. Cadastros de apoio ficam em segundo nível.

## 6. Jornadas propostas

### 6.1 Aluno — agendar

1. A home mostra a próxima viagem e um CTA “Book Another Trip”.
2. A tela de disponibilidade abre em “Upcoming Days”, já considerando a universidade do perfil.
3. Cada opção é um cartão de rota com data, destino, ida, volta, prazo e disponibilidade/status.
4. Ao tocar, abre uma página de detalhe, não um modal sobrecarregado.
5. O aluno escolhe “Round Trip”, “Outbound Only” ou “Return Only” em cartões de seleção claros.
6. Confirma ou troca o ponto de embarque sugerido.
7. Revisa um resumo único: data, trecho, horários, embarque e prazo para alteração.
8. Confirma e recebe uma tela de sucesso com atalho para “My Trips”.

### 6.2 Aluno — acompanhar e alterar

- Separar **Upcoming** e **History**.
- Mostrar a reserva e a viagem confirmada como um único objeto de jornada; detalhes técnicos mudam conforme o estágio.
- Exibir contagem regressiva contextual: “You can make changes until 4:00 PM today”.
- Oferecer “Change Trip or Boarding Stop” e “Cancel Booking” quando permitido.
- Quando veículo ou condutor forem definidos, atualizar o mesmo cartão em vez de criar outra seção mental.

### 6.3 Condutor — executar o dia

1. Home “Today” com próxima viagem, horário, veículo, lotação e CTA “View Operation”.
2. Detalhe com resumo da rota e passageiros agrupados por ponto de embarque.
3. Ações futuras recomendadas: iniciar embarque, marcar presença/ausência, iniciar viagem e concluir.
4. Contatos e exceções ficam acessíveis sem expor dados desnecessários.

As ações operacionais exigem evolução do backend; na primeira versão visual, o detalhe pode ser somente consulta, mas já deve usar a estrutura correta.

### 6.4 Gestor — planejar e operar

1. Visão geral abre com pendências: listas encerrando, viagens sem alocação, excesso de demanda e documentos vencendo.
2. Agenda oferece visualização por dia/semana, com filtros e estado operacional.
3. O detalhe de demanda combina resumo, ocupação, divisão por trecho/ponto e reservas.
4. “Generate Trip” vira uma etapa guiada que já sugere veículo e condutor elegíveis.
5. O detalhe da viagem reúne visão geral, passageiros, alocação e histórico de mudanças.
6. Operações recorrentes podem ser duplicadas ou criadas por modelo semanal.

## 7. Telas-chave da nova versão

### 7.1 Home do aluno

- saudação curta e contexto do dia;
- cartão principal “Your Next Trip” com linha visual de rota;
- estado da reserva em linguagem natural;
- CTA primário “View Details” ou “Book a Trip”;
- bloco compacto de próximos agendamentos;
- aviso acionável quando perfil ou ponto de embarque estiver incompleto.

### 7.2 Disponibilidade

- cabeçalho “When Do You Need to Travel?”;
- filtro horizontal por data e filtro secundário por universidade;
- grupos “Today”, “Tomorrow” e datas seguintes;
- cartões com prazo e estado;
- estado vazio com alternativa clara, não apenas “No data”.

### 7.3 Detalhe/checkout da reserva

- cabeçalho com data e destino;
- diagrama vertical de rota;
- seleção de trecho;
- seleção explícita de embarque;
- resumo fixo próximo ao CTA;
- botão primário fixado na área inferior respeitando safe area;
- confirmação final objetiva, sem repetir informações desnecessárias.

### 7.4 Visão geral administrativa

- KPIs: viagens hoje, reservas abertas, assentos planejados e pendências;
- fila “Needs Attention” como primeiro bloco;
- agenda dos próximos sete dias;
- ocupação por destino;
- atalhos: “Plan Dates”, “Create One-off Trip” e “Add Person”.

### 7.5 Agenda administrativa

- alternância tabela/calendário;
- filtros persistentes por período, destino, status, condutor e veículo;
- colunas orientadas à operação: data/hora, rota, demanda/lotação, alocação e status;
- ações em menu contextual;
- seleção em lote quando fizer sentido;
- estado salvo na URL para compartilhamento e retorno.

### 7.6 Detalhe operacional

Cabeçalho com rota, data, status e ação principal. Abaixo:

- resumo integrado de demanda, capacidade, recursos e pendências, evitando um cartão para cada dado;
- estágio atual em destaque; a timeline completa só deve aparecer quando ajudar uma decisão ou permitir uma ação;
- abas **Overview**, **Passengers**, **Assignment** e **History**;
- alertas contextuais, como capacidade insuficiente ou ausência de condutor.

## 8. Direção visual

### Conceito: “mobilidade universitária confiável”

A interface deve parecer jovem sem ser infantil e operacional sem ser burocrática. A referência conceitual é uma combinação de sinalização de transporte, ritmo de agenda e vida universitária.

### Paleta sugerida

- **Navy profundo `#102A43`:** texto forte, navegação e confiança.
- **Verde movimento `#18A77A`:** ação primária e estados positivos.
- **Menta clara `#DDF7EE`:** superfícies de destaque e seleção.
- **Âmbar `#E89B18`:** prazos e atenção.
- **Coral `#D9544D`:** erro, cancelamento e risco.
- **Neutros azulados:** fundo levemente frio, cartões brancos e bordas discretas.

As cores finais devem ser validadas em contraste WCAG e em estados hover, focus, pressed e disabled.

### Tipografia

- Títulos: **Manrope** ou **Plus Jakarta Sans**, para identidade e excelente leitura numérica.
- Corpo e dados: **Inter**, com números tabulares nas tabelas e horários.
- Escala curta e disciplinada: 12, 14, 16, 20, 24, 32 px.

### Linguagem visual

- rota representada por linha, pontos e direção, e não por vários pares de texto soltos;
- cartões com raio moderado (12–16 px), borda sutil e sombra apenas quando indicar elevação real;
- ícones consistentes, sempre acompanhados de texto em ações menos óbvias;
- status em chips com texto e marcador semântico, nunca apenas cor;
- ilustração limitada a onboarding e estados vazios; dados operacionais permanecem limpos;
- motion de 150–250 ms para feedback e mudança de estado, respeitando `prefers-reduced-motion`.

### Hierarquia e contenção visual

- cada região da página deve ter uma superfície principal; bordas internas e divisores devem organizar dados relacionados antes da criação de novos cartões;
- indicadores relacionados devem formar uma faixa de resumo, não uma grade de cartões concorrentes;
- cor forte é reservada à ação primária, ao estado atual e a exceções que exigem atenção; conteúdo saudável permanece predominantemente neutro;
- ícones devem facilitar reconhecimento ou ação, sem receber contêiner colorido meramente decorativo;
- evitar comunicar a mesma informação simultaneamente com ícone, fundo colorido, borda colorida, chip e texto;
- listas operacionais devem privilegiar linhas escaneáveis; cartões são usados quando o conteúdo precisa de comparação interna ou leitura fora de uma tabela;
- ações secundárias aparecem como links ou botões neutros; por região, apenas uma ação deve competir como primária;
- o espaço em branco deve separar grupos sem ampliar desnecessariamente a altura das telas administrativas.

### Densidade por contexto

- **Aluno:** blocos grandes, uma ação principal, conteúdo progressivo.
- **Condutor:** alto contraste, números e horários maiores, controles acessíveis em movimento.
- **Admin:** densidade confortável, tabelas robustas, filtros e comparação visual.

## 9. Design system recomendado

### Tokens

- cores semânticas, não classes de cor espalhadas;
- tipografia, espaçamento em base 4, raios, sombras e breakpoints;
- largura de conteúdo por contexto;
- duração e curva de animação;
- camadas de elevação e `z-index`.

### Componentes essenciais

- App Shell de aluno, condutor e admin;
- Page Header e Context Header;
- Route Card e Route Timeline;
- Status Chip;
- Date Strip e filtros;
- Bottom Action Bar para fluxos mobile ou formulários longos em que a ação precise permanecer acessível;
- Empty, loading, error e offline states;
- Toast/feedback e dialog de confirmação;
- Data Table com filtros, responsividade e menu de ações;
- Detail Drawer para ações rápidas no admin;
- Form Section, campos com ajuda/erro e Action Footer;
- KPI Summary, Alert Queue e Occupancy Meter.

### Regras de formulário

- usar uma coluna para tarefas simples e agrupamento semântico nas complexas;
- rótulo persistente, exemplo opcional e erro junto ao campo;
- aplicar máscara diretamente a CPF, telefone e placa;
- salvar com `submit`, permitindo teclado e acessibilidade;
- preservar dados diante de erro de rede;
- alertar ao sair com alterações não salvas;
- ações primária e destrutiva visualmente separadas.

## 10. Ajustes de conteúdo

Terminologia recomendada para a interface em inglês:

| Termo interno/atual | Texto recomendado na interface |
|---|---|
| Interest Lists | Available Trips |
| Booking | Booking |
| Trips | My Trips |
| Round Trip | Round Trip |
| Outbound | Outbound Only |
| Inbound | Return Only |
| Processing | Planning |
| Scheduled | Confirmed |

No painel, **Demand** pode representar a antiga lista de interesse, preservando **Booking** para cada aluno. Nomes de enums e termos técnicos não precisam ser exibidos literalmente; a interface deve usar inglês natural e orientado à tarefa.

## 11. Evoluções técnicas necessárias

### Imediatas, para suportar a nova UX

- endpoints com filtros por data, destino, status e usuário;
- resposta agregada para as homes, evitando várias chamadas e estados inconsistentes;
- atualização de reserva (trecho e embarque) e cancelamento pelo próprio aluno;
- regras de prazo retornadas pela API, não reproduzidas apenas na interface;
- transições explícitas e validadas para status de lista e viagem;
- paginação adequada ao mobile ou carregamento incremental;
- mensagens de erro com código estável para tradução amigável.

### Próxima evolução de produto

- modelos de recorrência semanal e geração em lote;
- cálculo de capacidade por trecho e ponto;
- notificações de confirmação, alteração e lembrete;
- check-in/presença e execução da viagem pelo condutor;
- histórico/auditoria de mudanças;
- permissões administrativas mais granulares, caso haja vários organizadores.

### Pontos do código atual que merecem validação

- a busca de reservas por viagem aparenta consultar `interestListId` usando o `tripId`;
- o status da reserva nasce sempre aprovado, embora existam estados pendente e rejeitado;
- o status da viagem é criado como “PROCESSING”, mas não há fluxo visível de transição;
- há navegação para criação de viagem no front sem uma rota equivalente;
- o embarque preferencial existe no domínio, porém não aparece de forma clara no checkout atual;
- home de todos os papéis ainda é placeholder.

Esses itens não impedem o design, mas devem ser resolvidos antes de prometer comportamentos na interface.

## 12. Roadmap de implementação

### Fase 0 — alinhamento e validação (curta)

- entrevistar 3–5 alunos, 2 condutores e 1–3 gestores;
- validar terminologia, frequência real, regras de cancelamento e principais exceções;
- medir o fluxo atual como linha de base;
- fechar escopo do MVP visual e comportamentos suportados pelo backend.

**Saída:** mapa de jornada validado, prioridades e critérios de sucesso.

### Fase 1 — fundação

- centralização de todo o conteúdo de interface em inglês, evitando strings divergentes nos templates;
- preparação da arquitetura de internacionalização para idiomas futuros, sem ativar português nesta fase;
- tokens e tema compartilhado;
- tipografia, ícones, status e feedback;
- novos shells responsivos;
- estados de loading, vazio e erro;
- biblioteca dos componentes fundamentais.

**Saída:** design system mínimo utilizável e navegação nova.

### Fase 2 — experiência do aluno

- home;
- disponibilidade;
- detalhe e checkout;
- próximas viagens/histórico;
- perfil e embarque preferencial;
- cancelamento/edição conforme suporte da API.

**Saída:** jornada ponta a ponta que pode ser testada com alunos.

### Fase 3 — operação administrativa

- visão geral e fila de pendências;
- agenda/demanda;
- detalhe unificado e geração de viagem;
- alocação de veículo/condutor;
- modernização de tabelas, filtros e formulários;
- reorganização dos cadastros auxiliares.

**Saída:** fluxo operacional completo orientado a tarefas.

### Fase 4 — experiência do condutor

- home operacional;
- agenda;
- detalhe da viagem e manifesto agrupado;
- posteriormente, presença e mudança de status.

**Saída:** aplicação útil em campo, mesmo antes da automação avançada.

### Fase 5 — recorrência, comunicação e otimização

- planejamento recorrente;
- notificações;
- auditoria;
- métricas de operação;
- melhorias guiadas por telemetria e testes de usabilidade.

## 13. Priorização sugerida

### P0 — muda a experiência central

- idioma e terminologia;
- home do aluno;
- fluxo de agendamento em página dedicada;
- próxima viagem e status claros;
- dashboard e pendências do gestor;
- agenda/detalhe operacional unificados;
- tokens e componentes essenciais.

### P1 — reduz trabalho e erros

- filtros completos;
- edição/cancelamento de reserva;
- sugestão de alocação e indicador de capacidade;
- recorrência/duplicação;
- home e manifesto do condutor;
- feedback, acessibilidade e estados de rede.

### P2 — aumenta maturidade operacional

- notificações;
- check-in;
- auditoria;
- analytics avançado;
- permissões granulares.

## 14. Métricas de sucesso

### Aluno

- taxa de conclusão do agendamento;
- tempo mediano para agendar;
- abandono por etapa;
- erros e pedidos de suporte sobre horário/embarque;
- percentual de alunos que identifica corretamente o status em teste.

### Gestor

- tempo para transformar demanda em viagem alocada;
- viagens sem condutor/veículo perto do horário;
- número de telas/ações por operação;
- conflitos de capacidade detectados antes do fechamento;
- uso de duplicação/recorrência.

### Condutor

- tempo para encontrar a próxima viagem e o manifesto;
- incidentes causados por informação desatualizada;
- conclusão das etapas operacionais, quando implementadas.

### Qualidade

- contraste AA e navegação completa por teclado no admin;
- Core Web Vitals em aparelhos móveis medianos;
- taxa de erro por endpoint e recuperação de falhas;
- cobertura visual dos componentes e fluxos críticos.

## 15. Decisões que precisam de validação com o negócio

1. A lista representa demanda sem garantia ou toda reserva aberta já garante vaga?
2. Como funciona quando a demanda ultrapassa a capacidade disponível?
3. O aluno pode alterar/cancelar até o horário de fechamento? Existe penalidade?
4. Destino e ponto de embarque podem variar por reserva ou devem seguir o perfil?
5. Quem cria as recorrências e com quanta antecedência?
6. Quais mudanças precisam gerar notificação e por qual canal?
7. O condutor deve apenas consultar ou também registrar presença e avançar estados?
8. Haverá mais de uma organização/cliente no mesmo sistema?

Até essas respostas existirem, a proposta assume reservas confirmadas imediatamente, alteração até o fechamento, uma organização principal e condutor inicialmente em modo de consulta.

## 16. Critério de “revolução” concluída

A reestruturação estará cumprida quando:

- um aluno consegue agendar e compreender sua próxima viagem sem conhecer “listas de interesse”;
- um condutor abre o app e encontra imediatamente sua operação relevante;
- um gestor começa o dia pelas exceções e conclui planejamento/alocação em um fluxo único;
- os três contextos compartilham identidade e componentes, mas não são meras versões responsivas da mesma interface;
- o produto torna a recorrência e a previsibilidade visíveis, que são o verdadeiro valor do UNIMOVE.

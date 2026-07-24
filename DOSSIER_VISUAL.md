# DOSSIER VISUAL — Serpenteando

> Documento técnico de referência para avaliação de fidelidade visual.
> Todo valor não explicitado aqui segue o padrão do Android/Material3.

---

## 1. SISTEMA DE DESIGN TOKENS

### 1.1 Paleta de Cores — Dark Mode (Padrão)

| Token | Hex | Uso |
|---|---|---|
| cascavel_bg | #0B1210 | Background principal |
| cascavel_surface | #141E18 | Superfície base (cards, grupos) |
| cascavel_surface_container | #1A2A1F | Container (opções quiz) |
| cascavel_surface_container_high | #243329 | Container alto (mission cards) |
| cascavel_surface_container_highest | #2E3D33 | Container máximo |
| cascavel_jade | #4CAF50 | Primary — chips ativos, botões |
| cascavel_jade_bright | #00E676 | Neon — progresso, next button, quiz feedback |
| cascavel_jade_dim | #2E7D32 | Primary container |
| cascavel_jade_light | #81C784 | Tonal claro |
| cascavel_gold | #C4A84D | Accent — streak, missões, speaker, estrelas |
| cascavel_gold_bright | #FFD54F | Gold claro |
| cascavel_gold_dim | #8D6E1F | Gold escuro (divisores) |
| cascavel_on_bg | #E8EDE8 | Texto primário |
| cascavel_on_surface_variant | #9EAA9E | Texto secundário / inativo |
| cascavel_tertiary_label | #6B7A6B | Texto terciário |
| cascavel_outline | #2E3A30 | Bordas de cards, chip inactive |
| cascavel_outline_variant | #1E2820 | Borda variante |
| cascavel_correct | #4CAF50 | Acerto |
| cascavel_wrong | #E53935 | Erro |
| cascavel_error | #CF6679 | Erro semântico |
| cascavel_glass_bg | #0A1A0E99 | Vidro (60% opacidade) — toolbar, bottom nav |
| cascavel_glass_stroke | #1A3A1F | Borda vidro |
| cascavel_glass_clear | #0A1A0E4D | Vidro claro (30%) |

#### Cores de Feedback

| Contexto | Fundo | Borda |
|---|---|---|
| Mito | #4A1A1A | #E53935 (0.5dp) |
| Verdade | #1A3A1A | #4CAF50 (0.5dp) |
| Opção correta | #1A00E676 | #00E676 (2dp) |
| Opção errada | #3A1A1A | #E53935 (1dp) |
| Opção incorreta | #1AFF6B6B | #FF6B6B (2dp) |

### 1.2 Paleta de Cores — Light Mode

| Token | Hex |
|---|---|
| cascavel_bg_light | #F5F5F0 |
| cascavel_surface_light | #FFFFFF |
| cascavel_surface_container_light | #EDEDE8 |
| cascavel_surface_container_high_light | #E5E5E0 |
| cascavel_surface_container_highest_light | #DADAD5 |
| cascavel_on_bg_light | #1C1C1E |
| cascavel_on_surface_variant_light | #636366 |
| cascavel_jade_on_light | #2E7D32 |
| cascavel_glass_bg_light | #E8E8E499 |
| cascavel_outline_light | #C6C6C0 |
| cascavel_tertiary_label_light | #8E8E93 |

### 1.3 Tipografia — Inter

| Estilo | Size | Weight | Uso |
|---|---|---|---|
| LargeTitle | 34sp | Bold | Título home, page titles |
| Title1 | 28sp | Bold | Content detail title |
| Title2 | 22sp | Bold | Section headers |
| Headline | 17sp | SemiBold | Quiz question |
| Body | 15sp | Regular | Corpo de texto |
| Footnote | 13sp | Regular | Caption |
| Caption | 13sp | Regular | Section captions |

### 1.4 Espaçamento (Grade 8pt)

space_4, space_6, space_8, space_12, space_16, space_20, space_24, space_32

### 1.5 Cantos

| Token | dp | Onde |
|---|---|---|
| radius_card | 16 | Cards genéricos |
| chip | 16 | Chips ativo/inativo |
| option | 14 | Opções de quiz |
| option feedback | 12 | Correct/incorrect |
| radius_search | 18 | Search bar |
| button pill | 999 | btn_onboarding |
| settings group | 14 | Grupo de ajustes |
| points badge | 20 | Badge de pontuação |

---

## 2. NAVEGAÇÃO INFERIOR

### Itens (4)

| ID | Ícone | Rótulo |
|---|---|---|
| nav_home | ic_cobra | Início |
| nav_quiz | ic_book | Quiz |
| nav_missions | ic_missao | Missões |
| nav_settings | ic_menu | Config |

### Estilo
- Fundo: #CC0A1A0E (80% opacidade), 0dp radius
- Altura: 64dp
- labelVisibilityMode: labeled
- itemIconSize: 22dp
- Cor ativa: cascavel_jade (#4CAF50)
- Cor inativa: cascavel_on_surface_variant (#9EAA9E)

---

## 3. GRAFO DE NAVEGAÇÃO

nav_home (start)
 ├── nav_content (arg: category)
 │    └── nav_content_detail (args: contentId, title)
 ├── nav_myth_truth
nav_quiz
nav_missions
nav_settings

Transições: slide_in_right/fade_out (forward), fade_in/slide_out_right (back)

---

## 4. INVENTÁRIO DE DRAWABLES

### Ícones (todos 24dp salvo indicação, vetoriais)

| Arquivo | Tam | Cor | Descrição |
|---|---|---|---|
| ic_cobra | 24 | white | Círculo com rosto de cobra |
| ic_book | 24 | gold | Livro aberto |
| ic_missao | 24 | white | Pin/badge |
| ic_menu | 24 | white | Hamburger |
| ic_quiz | 24 | white | Círculo com ? |
| ic_3d | 24 | white | Círculo com seta |
| ic_fire | 24 | multi | Chama |
| ic_speaker | 24 | gold | Alto-falante |
| ic_arrow_right | 12 | gold | Seta > |
| ic_mitos | 48 | gold | Casa/telhado |
| ic_anfibio | 48 | gold | Círculo com rosto |
| ic_peconhento | 48 | wrong | Círculo vermelho |
| ic_nao_peconhento | 48 | gold | Círculo com rosto |
| ic_leaf | 24 | green_text | Folha |

### Launcher
- Foreground: Cascavel geométrica #C4A84D (corpo bezier, cabeça triangular, olho #0B1210, chocalho 3 elos)
- Background: #0B1210 sólido

### Cards e Chips
- chip_active: jade (#4CAF50), 16dp radius
- chip_inactive: surface (#141E18) + 0.5dp outline, 16dp radius
- Card myth: #4A1A1A + 0.5dp wrong border, 16dp
- Card truth: #1A3A1A + 0.5dp correct border, 16dp
- Card mission: #243329 + 0.5dp gold border, 16dp
- quiz_card_bg: #141E18, 16dp radius
- settings_group_bg: #141E18 + 0.5dp outline, 14dp radius

### Opções de Quiz
- option_default: #1A2A1F + 0.5dp outline, 14dp
- option_correct: #1A00E676 + 2dp #00E676, 12dp
- option_wrong: #3A1A1A + 1dp #E53935, 14dp
- option_incorrect: #1AFF6B6B + 2dp #FF6B6B, 12dp

### Componentes
- bottom_nav_glass_bg: #CC0A1A0E sólido
- bg_voice_toggle: #1A2B25 + 1dp dark_card_border, 20dp
- bg_splash_cobra: layer-list bg + outer oval 240dp + inner oval 120dp
- hero_gradient_overlay: horizontal #0B1210 -> transparent (endX=0.4)
- snake_hero: 200x70dp cascavel dourada bezier
- divider_gold: 24x3dp, gold, 4dp radius
- btn_onboarding: jade, 999dp (pill), 48dp altura

---

## 5. TEMA MD3

| Attribute | Dark | Light |
|---|---|---|
| colorPrimary | #4CAF50 | #4CAF50 |
| colorSurface | #141E18 | #FFFFFF |
| colorOnSurface | #E8EDE8 | #1C1C1E |
| colorOnSurfaceVariant | #9EAA9E | #636366 |
| windowBackground | #0B1210 | #F5F5F0 |
| statusBarStyle | light (branco) | dark (preto) |

---

## 6. REGRAS COMPORTAMENTAIS

1. Dark mode é o padrão. Light mode usa bg #F5F5F0 com texto #1C1C1E.
2. Bottom nav: 4 itens (Início, Quiz, Missões, Config). Ativo=jade, inativo=gray.
3. Anatomia 3D fora do bottom nav — só no card Explorar do Home com overlay Em breve.
4. Transições entre telas: slide_in_right (entrar), fade_out (sair), fade_in (voltar), slide_out_right (voltar).
5. Toolbar translúcida com glass effect (#0A1A0E99), sem elevation, sem borda.
6. Status bar e nav bar transparentes.
7. Quiz: 3 estrelas de dificuldade. Botão Próxima aparece só após responder.
8. Chip categories: single selection, checked=jade+bg text, unchecked=surface+outline+gray text.
9. Splash: camada com 2 círculos concêntricos (outer 240dp, inner 120dp).
10. Cards usam cantos arredondados consistentes de 16dp, exceto onde explicitado.

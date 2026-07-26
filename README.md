# Serpenteando

App educacional Android sobre serpentes brasileiras. TCC de ensino técnico.

**Público:** Estudantes 16-20 anos  
**Tom:** Premium, educativo, moderno — nunca infantil  
**Inspiração:** Apple Fitness+ encontra National Geographic

## Identidade Visual "Cascavel"

- **Animal símbolo:** Cascavel (*Crotalus durissus*)
- **Essência:** "Jade neon na floresta escura"
- **Paleta:** Background `#0B1210`, Accent jade `#4CAF50`, Gold `#C4A84D`
- **Estilo:** Glassmorphism, gradiente radial, bordas neon, bottom nav flutuante

## Funcionalidades

- Quiz com 3 níveis de dificuldade (30 perguntas em JSON em `assets/content/`)
- Mitos e Verdades sobre serpentes (15 cards)
- Missões gamificadas com anel de progresso
- Anatomia 3D com modelo cascavel.glb (Filament)
- Enciclopédia de espécies (10 serpentes em JSON)
- Dica do Dia (32 curiosidades em JSON)
- Onboarding (3 etapas)

## Status do Desenvolvimento

### ✅ Concluído
- Estrutura de navegação (Navigation Component, 5 abas)
- Glassmorphism completo (cards translúcidos + borda neon)
- Gradiente radial jade glow
- Bottom nav flutuante com estilo floating dock
- Quiz states em vidro (correto/incorreto translúcido)
- 53 drawables vetoriais
- Build: `assembleDebug` — SUCCESS
- **100% de fidelidade estática** com as imagens de referência (avaliado por Gemini)

### 🔄 Em Andamento
- **PRIORIDADE 1:** Favicon/launcher icon temático + imagens das espécies
- **PRIORIDADE 2:** Integrar JSONs de assets/content/ (dicas, perguntas, missões, mitos - atualmente não conectados) + integrar assets (envs/, textures/)
- **PRIORIDADE 3:** Animações (fade+slide entre fragments, press scale nos cards)

## Estrutura de Assets

```
assets/
├── content/
│   ├── dicas.json              # 32 curiosidades
│   ├── missoes.json            # 6 missões gamificadas
│   ├── mitos_verdades.json     # 15 mitos vs verdades
│   ├── perguntas.json          # 30 perguntas de quiz (3 níveis)
│   └── serpentes.json          # 10 espécies
├── models/
│   └── cascavel.glb            # Modelo 3D
├── envs/                       # (vazio)
└── textures/                   # (vazio)
```

## Stack Técnica

- **Linguagem:** Kotlin
- **Arquitetura:** MVP (Contract → Presenter → Fragment)
- **Navegação:** Navigation Component (Single Activity)
- **3D:** Google Filament (cascavel.glb)
- **Banco:** SQLite via SQLiteOpenHelper
- **Design:** XML 100% (53 drawables, glassmorphism, gradiente radial)
- **Tema:** Dark mode nativo (daynight), palela Apple HIG

## Repositório

```bash
git clone https://github.com/Kaio193/Serpenteando.git
```

Último commit: `e652fbb` — refactor(ui): glassmorphism + gradiente radial + quiz states glass

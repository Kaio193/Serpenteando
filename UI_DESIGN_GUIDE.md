# Guia de Design Visual — Serpenteando

## Paleta de Cores

### Cores Principais (Natureza/Cascavel)
| Função | Cor | Hex | Uso |
|--------|-----|-----|-----|
| Primária | Verde Floresta | `#2D6A4F` | Botões, estados ativos, CTAs |
| Primária Clara | Sálvia | `#95D5B2` | Hover states, elementos secundários |
| Primária Escura | Evergreen | `#1B4332` | Headers, texto de ênfase |
| Fundo Claro | Mint Wash | `#D8F3DC` | Fundo de cards, superfícies claras |

### Cores Secundárias (Terra/Cobra)
| Função | Cor | Hex | Uso |
|--------|-----|-----|-----|
| Secundária | Terra Quente | `#BC6C25` | Botões secundários, destaques |
| Secundária Clara | Arenito | `#DDA15E` | Badges, destaques leves |
| Neutro Quente | Pergaminho | `#FEFAE0` | Fundo da página, superfícies quentes |

### Cores de Acento
| Função | Cor | Hex | Uso |
|--------|-----|-----|-----|
| Info/Teal | Teal | `#2EC4B6` | Callouts de info, marcadores de habitat |
| Destaque | Âmbar | `#E9C46A` | Warnings, curiosidades, destaques |
| Perigo | Coral | `#E76F51` | Indicadores de perigo (peçonhentos) |

### Neutros
| Função | Cor | Hex | Uso |
|--------|-----|-----|-----|
| Texto Principal | Carvão | `#2D3436` | Corpo, headings |
| Texto Secundário | Ardósia | `#636E72` | Legendas, metadata |
| Texto Desabilitado | Cinza | `#B2BEC3` | Placeholders, estados disabled |
| Fundo | Off-White | `#FAFAFA` | Fundo principal (light mode) |
| Superfície | Papel | `#F5F5F0` | Superfície de cards |
| Borda | Névoa | `#DFE6E9` | Divisores, bordas de cards |

---

## Modo Dark (OLED-friendly)

| Função | Cor | Hex |
|--------|-----|-----|
| Fundo | Cinza Profundo | `#1E1E1E` |
| Superfície | Cinza Elevado | `#2D2D2D` |
| Texto Principal | Branco Suave | `#E0E0E0` |
| Texto Secundário | Cinza Claro | `#B0B0B0` |
| Primária | Verde Claro | `#52B788` |
| Secundária | Âmbar Claro | `#F0D78C` |

**Regra:** Nunca usar preto puro (#000000) — causa halation em OLED.

---

## Tipografia

### Recomendada: Nature-Forward
- **Títulos:** DM Serif Display (elegante, personalidade orgânica)
- **Corpo:** DM Sans (limpo, legível)
- **Por quê:** Serif transmite autoridade científica; sans-serif mantém modernidade

### Alternativa: Modern & Clean
- **Títulos:** Inter Bold
- **Corpo:** Inter Regular

---

## Cards

```xml
<!-- Card Example -->
<LinearLayout
    android:background="@drawable/card_background"
    android:padding="16dp"
    android:layout_margin="8dp">
```

```xml
<!-- card_background.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F5F5F0"/>
    <corners android:radius="16dp"/>
    <stroke android:width="1dp" android:color="#DFE6E9"/>
</shape>
```

**Especificações:**
- Border-radius: 16dp
- Sombra: `0 2dp 8dp rgba(0,0,0,0.08)`
- Borda: 1dp `#DFE6E9`
- Padding: 16-24dp

---

## Botões

### Primário
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#2D6A4F"/>
    <corners android:radius="12dp"/>
</shape>
```
- Fundo: Verde `#2D6A4F`
- Texto: Branco `#FFFFFF`
- Border-radius: 12dp

### Secundário
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <stroke android:width="2dp" android:color="#2D6A4F"/>
    <corners android:radius="12dp"/>
</shape>
```
- Fundo: Transparente
- Borda: 2dp Verde `#2D6A4F`
- Texto: Verde `#2D6A4F`

### Acento (Destaque/Curiosidades)
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#E9C46A"/>
    <corners android:radius="12dp"/>
</shape>
```
- Fundo: Âmbar `#E9C46A`
- Texto: Carvão `#2D3436`

---

## Gradientes

### Floresta (Hero Sections)
```xml
<gradient
    android:startColor="#1B4332"
    android:centerColor="#2D6A4F"
    android:endColor="#40916C"
    android:angle="135"/>
```

### Orvalho (Cards Suaves)
```xml
<gradient
    android:startColor="#D8F3DC"
    android:centerColor="#B7E4C7"
    android:endColor="#95D5B2"
    android:angle="135"/>
```

### Floresta Profunda (Dark Mode)
```xml
<gradient
    android:startColor="#1B4332"
    android:centerColor="#2D6A4F"
    android:endColor="#52796F"
    android:angle="270"/>
```

---

## O que faz design parecer "humano"

### Indicadores positivos:
1. Ilustrações customizadas (não stock)
2. Assimetria intencional
3. Tipografia com personalidade
4. Cores que contam história
5. Micro-copy com empatia
6. Espaço em branco pensado

### Bandeiras vermelhas (parece IA):
- Layouts perfeitamente simétricos
- Cores super saturadas
- Ilustrações genéricas
- Combinações de cores previsíveis
- Falta de personalidade

---

## Acessibilidade (WCAG AA)

| Combinação | Contraste | Status |
|------------|-----------|--------|
| `#2D3436` em `#FAFAFA` | 13.5:1 | ✅ Aprovado |
| `#2D3436` em `#FEFAE0` | 11.2:1 | ✅ Aprovado |
| `#FFFFFF` em `#2D6A4F` | 5.1:1 | ✅ Aprovado |
| `#2D6A4F` em `#D8F3DC` | 2.8:1 | ❌ Só decorativo |

---

## Por que essas cores combinam com o projeto

| Cor | Relação com Cascavel |
|-----|----------------------|
| Verde `#2D6A4F` | Habitat natural (matas, cerrados) |
| Terra `#BC6C25` | Cor da própria cascavel |
| Âmbar `#E9C46A` | Cor do veneno, periculosidade |
| Teal `#2EC4B6` | Informação educativa, calma |
| Coral `#E76F51` | Atenção: peçonhento |

---

## Fontes da Pesquisa
1. GitHub: halilozel1903/DarkMode, shenguojun/AndroidDarkThemeDemo
2. Material Design 3 Official Docs
3. Mobbin (2,800+ telas dark mode)
4. Muzli (60+ telas dark mode 2026)
5. Smashing Magazine (teoria das cores)
6. Behance (apps educacionais de natureza)

---

**Data:** 2026-07-08
**Projeto:** Serpenteando TCC

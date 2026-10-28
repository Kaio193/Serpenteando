# Site do Serpenteando

Site de apresentação do aplicativo **Serpenteando** (TCC do Curso Técnico em Informática — IFPB).

## Como o site está organizado

```
index.html          página única do site
css/base.css        cores, temas (escuro/claro), fontes, cabeçalho, botões e rodapé
css/home.css        seções (apresentação, carrossel, recursos, vídeo, download, equipe)
css/responsivo.css  ajustes de tablet e celular
js/i18n.js          textos em português, inglês e espanhol
js/main.js          tema claro/escuro, menu, carrossel automático, idioma e vídeo
img/                logo oficial e telas do aplicativo (em moldura de celular)
img/equipe/         fotos da equipe (as mesmas do app: profile_*.jpeg)
font/               fonte Inter (a mesma usada no app)
```

## Tema claro e escuro

O site abre no tema escuro (o mesmo do app) ou no claro, se o sistema do visitante
estiver no claro. O botão de sol/lua no cabeçalho troca e a escolha fica salva no
`localStorage` (`serpenteando-tema`). As cores dos dois temas estão em `css/base.css`,
nos blocos `:root` (escuro) e `html.claro` (claro).

HTML, CSS e JavaScript puro — sem frameworks e sem bibliotecas.

## Como publicar o vídeo (etapa 2)

Abra o `index.html` e procure a linha:

```html
<div class="video-moldura" id="video-moldura" data-youtube="">
```

Coloque o ID do vídeo do YouTube (ou a URL completa) dentro de `data-youtube`:

```html
<div class="video-moldura" id="video-moldura" data-youtube="https://www.youtube.com/watch?v=SEU_VIDEO">
```

Salve o arquivo e publique de novo. O player aparece no lugar do aviso automaticamente — nenhuma outra alteração é necessária.

## Regras que não devem ser quebradas

- **Tela de app sempre inteira:** as capturas usam `object-fit: contain` e a moldura
  tem proporção `144 / 271` (a das capturas). Nada de `cover` — foi o que cortava
  (dava "zoom") a tela do 3D.
- **Sem notch por cima da tela:** a ilha cobria o topo da captura. A moldura desenha
  a barra de status (9:41 + sinal, wi-fi e bateria) porque os prints do app vêm sem ela.
- **O modelo 3D é uma píton-real (Python regius)**, não uma cascavel. Qualquer texto
  novo sobre o 3D precisa dizer píton-real.
- **Toda imagem de app vai dentro de `.iphone`** (moldura de celular), pra manter o
  alinhamento do carrossel e do topo.
- **Carrossel automático:** passa a cada 5s (`INTERVALO_AUTO` em `js/main.js`). Clique em
  seta/pontinho pausa e a passagem volta depois de 9s (`ESPERA_VOLTA`).
- **O texto ao lado do carrossel muda com a tela:** cada slide tem `data-tela`
  (splash, home, ficha, quiz, 3d, mitos, missoes, idiomas, config) e o `js/main.js`
  troca o título e a explicação por `tela_<nome>_titulo` / `tela_<nome>_texto`.
- **Requisito mostrado é Android 10.0** (pedido do Kaio). O APK do app instala desde
  o Android 6.0 (`minSdk 23`) — não "corrigir" sem falar com ele.
- **Não criar seção de galeria** enquanto existir o carrossel: as mesmas telas
  apareceriam duas vezes.
- **Cada seção tem o seu fundo** (`.secao-app`, `.secao-sobre`, `.secao-recursos`,
  `.secao-video`, `.secao-download`, `.secao-equipe`) e uma `.divisa` entre elas.

## Onde trocar informações

| O que | Onde |
|---|---|
| Textos do site (3 idiomas) | `js/i18n.js` |
| Nome e cargo da equipe | `index.html`, seção `#equipe` |
| Fotos da equipe | `img/equipe/` (copiadas do `drawable/profile_*.jpeg` do app) |
| Link do APK | `index.html`, seção `#download` |
| Tamanho e versão do APK | `index.html`, seção `#download` |
| Imagens | pasta `img/` |

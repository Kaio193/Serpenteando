# Site do Serpenteando

Site de apresentação do aplicativo **Serpenteando** (TCC do Curso Técnico em Informática — IFPB).

## Como o site está organizado

```
index.html          página única do site
css/base.css        cores, fontes, cabeçalho, botões e rodapé
css/home.css        seções (apresentação, carrossel, recursos, vídeo, galeria, download, equipe)
css/responsivo.css  ajustes de tablet e celular
js/i18n.js          textos em português, inglês e espanhol
js/main.js          menu do celular, carrossel, idioma e player do vídeo
img/                logo oficial e telas do aplicativo
font/               fonte Inter (a mesma usada no app)
```

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

## Onde trocar informações

| O que | Onde |
|---|---|
| Textos do site (3 idiomas) | `js/i18n.js` |
| Nome, cargo e iniciais da equipe | `index.html`, seção `#equipe` |
| Link do APK | `index.html`, seção `#download` |
| Tamanho e versão do APK | `index.html`, seção `#download` |
| Imagens | pasta `img/` |

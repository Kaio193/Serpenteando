/* =========================================================
   main.js — tema claro/escuro, animações ao rolar, menu,
   carrossel, idioma e vídeo. JavaScript puro, sem bibliotecas.
   ========================================================= */

var raiz = document.documentElement;
var idiomaAtual = "pt";

// ---------- 1. Tema claro / escuro ----------
// O tema inicial já foi aplicado no <head> (script curto) pra não piscar.
// Aqui só ligamos o botão e guardamos a escolha do visitante.

var botaoTema = document.getElementById("tema-botao");
var metaTema = document.querySelector('meta[name="theme-color"]');

function temaClaroAtivo() {
    return raiz.classList.contains("claro");
}

/* Troca o rótulo do botão conforme o tema em que a pessoa está */
function atualizarRotuloTema() {
    if (!botaoTema) {
        return;
    }

    var claro = temaClaroAtivo();
    var textos = (typeof TEXTOS !== "undefined" && TEXTOS[idiomaAtual]) ? TEXTOS[idiomaAtual] : null;
    var rotulo = claro
        ? (textos ? textos.tema_para_escuro : "Ativar modo escuro")
        : (textos ? textos.tema_para_claro : "Ativar modo claro");

    botaoTema.setAttribute("aria-label", rotulo);
    botaoTema.setAttribute("title", rotulo);
    botaoTema.setAttribute("aria-pressed", claro ? "true" : "false");

    if (metaTema) {
        metaTema.setAttribute("content", claro ? "#F2F0E9" : "#0B1210");
    }
}

if (botaoTema) {
    botaoTema.addEventListener("click", function () {
        var claro = raiz.classList.toggle("claro");

        try {
            localStorage.setItem("serpenteando-tema", claro ? "claro" : "escuro");
        } catch (erro) {
            // Navegador sem localStorage: o tema vale só nesta visita
        }

        atualizarRotuloTema();
    });
}

// ---------- 2. Idioma ----------

var seletorIdioma = document.getElementById("seletor-idioma");

aplicarIdioma(idiomaInicial());

if (seletorIdioma) {
    seletorIdioma.addEventListener("change", function () {
        aplicarIdioma(seletorIdioma.value);
    });
}

// ---------- 3. Menu do celular ----------

var menu = document.getElementById("menu");
var menuBotao = document.getElementById("menu-botao");

if (menuBotao && menu) {
    menuBotao.addEventListener("click", function () {
        var aberto = menu.classList.toggle("aberto");
        menuBotao.setAttribute("aria-expanded", aberto ? "true" : "false");
    });

    var linksMenu = menu.querySelectorAll("a");
    for (var i = 0; i < linksMenu.length; i++) {
        linksMenu[i].addEventListener("click", function () {
            menu.classList.remove("aberto");
            menuBotao.setAttribute("aria-expanded", "false");
        });
    }
}

// ---------- 4. Carrossel de telas ----------

var slides = document.querySelectorAll(".slide");
var pontosCaixa = document.getElementById("pontos");
var botaoAnterior = document.getElementById("anterior");
var botaoProximo = document.getElementById("proximo");
var slideAtual = 0;

// Cria um pontinho pra cada tela
if (pontosCaixa) {
    for (var s = 0; s < slides.length; s++) {
        var ponto = document.createElement("button");
        ponto.className = "ponto" + (s === 0 ? " ativo" : "");
        ponto.setAttribute("type", "button");
        ponto.setAttribute("role", "tab");
        ponto.setAttribute("aria-label", "Imagem " + (s + 1));
        ponto.setAttribute("aria-selected", s === 0 ? "true" : "false");
        ponto.setAttribute("data-indice", s);
        pontosCaixa.appendChild(ponto);
    }
}

var pontos = document.querySelectorAll(".ponto");

function irPara(indice) {
    if (indice < 0) {
        indice = slides.length - 1;
    }
    if (indice >= slides.length) {
        indice = 0;
    }

    slides[slideAtual].classList.remove("ativa");
    pontos[slideAtual].classList.remove("ativo");
    pontos[slideAtual].setAttribute("aria-selected", "false");

    slideAtual = indice;

    slides[slideAtual].classList.add("ativa");
    pontos[slideAtual].classList.add("ativo");
    pontos[slideAtual].setAttribute("aria-selected", "true");
}

if (slides.length > 0 && pontos.length > 0) {
    if (botaoAnterior) {
        botaoAnterior.addEventListener("click", function () {
            irPara(slideAtual - 1);
        });
    }

    if (botaoProximo) {
        botaoProximo.addEventListener("click", function () {
            irPara(slideAtual + 1);
        });
    }

    for (var p = 0; p < pontos.length; p++) {
        pontos[p].addEventListener("click", function () {
            irPara(parseInt(this.getAttribute("data-indice"), 10));
        });
    }

    // Setas do teclado também trocam a tela
    document.addEventListener("keydown", function (evento) {
        if (evento.target && /^(INPUT|SELECT|TEXTAREA)$/.test(evento.target.tagName)) {
            return;
        }
        if (evento.key === "ArrowLeft") {
            irPara(slideAtual - 1);
        }
        if (evento.key === "ArrowRight") {
            irPara(slideAtual + 1);
        }
    });
}

// ---------- 5. Animação de entrada ao rolar ----------
// Só esconde os blocos se o navegador tiver IntersectionObserver;
// assim o conteúdo nunca fica invisível por falta de suporte.

var blocosRevelar = document.querySelectorAll(".revelar");

if ("IntersectionObserver" in window && blocosRevelar.length > 0) {
    document.body.classList.add("com-revelacao");

    var observadorRevelar = new IntersectionObserver(function (entradas) {
        for (var e = 0; e < entradas.length; e++) {
            if (entradas[e].isIntersecting) {
                entradas[e].target.classList.add("visivel");
                observadorRevelar.unobserve(entradas[e].target);
            }
        }
    }, { threshold: 0.15, rootMargin: "0px 0px -60px 0px" });

    for (var r = 0; r < blocosRevelar.length; r++) {
        observadorRevelar.observe(blocosRevelar[r]);
    }
}

// ---------- 6. Vídeo do YouTube ----------
// Basta preencher data-youtube no HTML com o ID ou a URL do vídeo.

var molduraVideo = document.getElementById("video-moldura");

if (molduraVideo) {
    var valor = (molduraVideo.getAttribute("data-youtube") || "").trim();

    if (valor !== "") {
        var idVideo = valor;

        if (valor.indexOf("youtu.be/") !== -1) {
            idVideo = valor.split("youtu.be/")[1];
        } else if (valor.indexOf("v=") !== -1) {
            idVideo = valor.split("v=")[1];
        }
        idVideo = idVideo.split("&")[0].split("?")[0];

        var player = document.createElement("iframe");
        player.src = "https://www.youtube-nocookie.com/embed/" + idVideo;
        player.title = "Vídeo de apresentação do Serpenteando";
        player.setAttribute("allow", "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture");
        player.setAttribute("allowfullscreen", "");
        player.setAttribute("loading", "lazy");

        molduraVideo.innerHTML = "";
        molduraVideo.appendChild(player);
    }
}

// ---------- 7. Marca o item do menu da seção visível ----------

var linksNavegacao = document.querySelectorAll(".menu a");
var secoes = [];

for (var n = 0; n < linksNavegacao.length; n++) {
    var destino = linksNavegacao[n].getAttribute("href");
    if (destino && destino.charAt(0) === "#") {
        var secao = document.querySelector(destino);
        if (secao) {
            secoes.push({ link: linksNavegacao[n], elemento: secao });
        }
    }
}

if ("IntersectionObserver" in window && secoes.length > 0) {
    var observador = new IntersectionObserver(function (entradas) {
        for (var e = 0; e < entradas.length; e++) {
            if (entradas[e].isIntersecting) {
                for (var f = 0; f < secoes.length; f++) {
                    secoes[f].link.classList.remove("ativo");
                }
                for (var g = 0; g < secoes.length; g++) {
                    if (secoes[g].elemento === entradas[e].target) {
                        secoes[g].link.classList.add("ativo");
                    }
                }
            }
        }
    }, { rootMargin: "-45% 0px -50% 0px" });

    for (var o = 0; o < secoes.length; o++) {
        observador.observe(secoes[o].elemento);
    }
}

// Rótulo do botão do tema já no idioma salvo
atualizarRotuloTema();

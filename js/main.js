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

// Texto ao lado do carrossel: troca junto com a tela que está aparecendo
var tituloLateral = document.getElementById("app-info-titulo");
var textoLateral = document.getElementById("app-info-texto");

function atualizarLateral() {
    if (!tituloLateral || !textoLateral || !slides[slideAtual]) {
        return;
    }

    var nome = slides[slideAtual].getAttribute("data-tela") || "splash";
    var chaveTitulo = "tela_" + nome + "_titulo";
    var chaveTexto = "tela_" + nome + "_texto";
    var textos = TEXTOS[idiomaAtual] || TEXTOS.pt;


    // Guarda a chave no elemento: quando trocar o idioma, o aplicarIdioma
    // já encontra o texto certo desta tela.
    tituloLateral.setAttribute("data-i18n", chaveTitulo);
    textoLateral.setAttribute("data-i18n", chaveTexto);
    tituloLateral.textContent = textos[chaveTitulo] || "";
    textoLateral.textContent = textos[chaveTexto] || "";
}

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

    atualizarLateral();
}

// ---------- 5. Passagem automática das telas ----------
// Fica rodando sozinha. Se a pessoa clicar (seta, pontinho ou teclado),
// para na hora e só volta depois de alguns segundos sem clique.

var INTERVALO_AUTO = 5000;
var ESPERA_VOLTA = 9000;
var relogioAuto = null;
var relogioVolta = null;

function podePassarSozinho() {
    if (slides.length < 2) {
        return false;
    }
    // Quem pediu menos animação no sistema não leva carrossel automático
    if (window.matchMedia &&
        window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        return false;
    }
    return true;
}

function ligarAuto() {
    if (!podePassarSozinho() || relogioAuto) {
        return;
    }
    relogioAuto = window.setInterval(function () {
        irPara(slideAtual + 1);
    }, INTERVALO_AUTO);
}

function desligarAuto() {
    if (relogioAuto) {
        window.clearInterval(relogioAuto);
        relogioAuto = null;
    }
}

function pausarPorClique() {
    desligarAuto();
    if (relogioVolta) {
        window.clearTimeout(relogioVolta);
    }
    relogioVolta = window.setTimeout(ligarAuto, ESPERA_VOLTA);
}

if (slides.length > 0 && pontos.length > 0) {
    if (botaoAnterior) {
        botaoAnterior.addEventListener("click", function () {
            pausarPorClique();
            irPara(slideAtual - 1);
        });
    }

    if (botaoProximo) {
        botaoProximo.addEventListener("click", function () {
            pausarPorClique();
            irPara(slideAtual + 1);
        });
    }

    for (var p = 0; p < pontos.length; p++) {
        pontos[p].addEventListener("click", function () {
            pausarPorClique();
            irPara(parseInt(this.getAttribute("data-indice"), 10));
        });
    }

    // Setas do teclado também trocam a tela
    document.addEventListener("keydown", function (evento) {
        if (evento.target && /^(INPUT|SELECT|TEXTAREA)$/.test(evento.target.tagName)) {
            return;
        }
        if (evento.key === "ArrowLeft") {
            pausarPorClique();
            irPara(slideAtual - 1);
        }
        if (evento.key === "ArrowRight") {
            pausarPorClique();
            irPara(slideAtual + 1);
        }
    });

    atualizarLateral();
    ligarAuto();
}

// Com a aba escondida não faz sentido continuar trocando de tela
if (document.addEventListener) {
    document.addEventListener("visibilitychange", function () {
        if (document.hidden) {
            desligarAuto();
        } else if (!relogioVolta) {
            ligarAuto();
        }
    });
}

// ---------- 6. Vídeo do YouTube ----------
// Basta preencher data-youtube no HTML com o ID ou a URL do vídeo.
// A capa (thumbnail real) fica no lugar do player e o clique abre o vídeo no
// YouTube, em outra aba. A incorporação saiu de propósito: o player dentro do
// site devolvia "Erro 153 — erro de configuração do player".

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

        var capaVideo = document.getElementById("video-capa");
        var imagemCapa = capaVideo ? capaVideo.querySelector(".video-capa-imagem") : null;

        // Capa e link saem do mesmo ID (data-youtube); a capa nunca some
        if (capaVideo) {
            capaVideo.href = "https://youtu.be/" + idVideo;
        }

        if (imagemCapa) {
            imagemCapa.src = "https://i.ytimg.com/vi/" + idVideo + "/maxresdefault.jpg";
            imagemCapa.addEventListener("error", function () {
                this.src = "https://i.ytimg.com/vi/" + idVideo + "/hqdefault.jpg";
            }, { once: true });
        }
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

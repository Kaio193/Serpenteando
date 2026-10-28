/* =========================================================
   main.js — menu do celular, carrossel, idioma e vídeo
   JavaScript puro, sem bibliotecas.
   ========================================================= */

// ---------- 1. Idioma ----------
// O i18n.js já traz os textos; aqui só ligamos o seletor.
var seletorIdioma = document.getElementById("seletor-idioma");

aplicarIdioma(idiomaInicial());

if (seletorIdioma) {
    seletorIdioma.addEventListener("change", function () {
        aplicarIdioma(seletorIdioma.value);
    });
}

// ---------- 2. Menu do celular ----------
var menu = document.getElementById("menu");
var menuBotao = document.getElementById("menu-botao");

if (menuBotao && menu) {
    menuBotao.addEventListener("click", function () {
        var aberto = menu.classList.toggle("aberto");
        menuBotao.setAttribute("aria-expanded", aberto ? "true" : "false");
    });

    // Fecha a gaveta quando o visitante escolhe uma seção
    var linksMenu = menu.querySelectorAll("a");
    for (var i = 0; i < linksMenu.length; i++) {
        linksMenu[i].addEventListener("click", function () {
            menu.classList.remove("aberto");
            menuBotao.setAttribute("aria-expanded", "false");
        });
    }
}

// ---------- 3. Carrossel de telas ----------
var slides = document.querySelectorAll(".slide");
var pontosCaixa = document.getElementById("pontos");
var botaoAnterior = document.getElementById("anterior");
var botaoProximo = document.getElementById("proximo");
var slideAtual = 0;

// Cria um pontinho para cada imagem
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

// Mostra a imagem pedida e atualiza os pontinhos
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

    // Setas do teclado também trocam a imagem
    document.addEventListener("keydown", function (evento) {
        if (evento.key === "ArrowLeft") {
            irPara(slideAtual - 1);
        }
        if (evento.key === "ArrowRight") {
            irPara(slideAtual + 1);
        }
    });
}

// ---------- 4. Vídeo do YouTube ----------
// Basta preencher data-youtube no HTML com o ID ou a URL do vídeo.
var molduraVideo = document.getElementById("video-moldura");

if (molduraVideo) {
    var valor = (molduraVideo.getAttribute("data-youtube") || "").trim();

    if (valor !== "") {
        var idVideo = valor;

        // Aceita link completo, link curto ou só o ID
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

// ---------- 5. Marca o item do menu da seção visível ----------
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

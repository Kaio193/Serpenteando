/* =========================================================
   i18n.js — textos do site em português, inglês e espanhol
   Os mesmos três idiomas que o aplicativo já oferece.
   Para aplicar um texto, o HTML usa data-i18n="chave".
   ========================================================= */

var TEXTOS = {
    pt: {
        meta_titulo: "Serpenteando — app educativo sobre serpentes brasileiras",
        meta_desc: "Aplicativo educativo sobre serpentes brasileiras, desenvolvido como TCC do IFPB. Fichas de espécies, quiz, missões, mitos e verdades e anatomia 3D.",

        nav_app: "O app",
        nav_sobre: "Sobre",
        nav_recursos: "Recursos",
        nav_video: "Vídeo",
        nav_galeria: "Galeria",
        nav_equipe: "Equipe",
        nav_baixar: "Baixar o app",

        hero_selo: "TCC · Instituto Federal da Paraíba",
        hero_titulo: "Conheça as serpentes brasileiras — e aprenda brincando",
        hero_texto: "O Serpenteando é um aplicativo educativo para estudantes do ensino médio que reúne fichas das espécies, quiz com três níveis, missões, mitos e verdades e um modelo 3D interativo da cascavel — tudo em português, inglês e espanhol.",
        hero_btn1: "Baixar o APK",
        hero_btn2: "Conhecer o projeto",
        hero_info: "Android 6.0 ou superior · versão 0.1.0",

        badge_3d_titulo: "Anatomia 3D",
        badge_3d_texto: "Gire, dê zoom e toque nos pontos",
        badge_idioma_titulo: "3 idiomas",
        badge_idioma_texto: "PT · EN · ES",

        app_rotulo: "O aplicativo",
        app_titulo: "O app por dentro",
        app_texto: "As telas abaixo são do aplicativo em funcionamento. Percorra no carrossel para conhecer cada parte do Serpenteando.",

        legenda_home: "Tela inicial com pontuação, missões e sequência",
        legenda_ficha: "Ficha completa das espécies mais conhecidas",
        legenda_quiz: "Quiz com níveis fácil, médio e difícil",
        legenda_3d: "Modelo 3D interativo da cascavel",
        legenda_mitos: "Mitos e verdades com explicação científica",
        legenda_missoes: "Missões com acompanhamento de progresso",
        legenda_idiomas: "Escolha do idioma: português, inglês ou espanhol",
        legenda_config: "Configurações do app",
        legenda_splash: "Tela de inicialização",

        app_info_titulo: "Um guia de bolso sobre serpentes",
        app_info_texto: "O app reúne conteúdo sobre jararaca, cascavel, coral verdadeira, jiboia, sucuri, urutu, caninana e outras espécies, com a indicação de quais são peçonhentas. Tudo foi escrito para o público do ensino médio.",
        app_item_1: "Fichas com nome científico, habitat, alimentação e curiosidades",
        app_item_2: "Quiz com explicação da resposta certa",
        app_item_3: "Missões que acompanham o seu progresso",
        app_item_4: "Anatomia 3D da cascavel com pontos interativos",
        app_item_5: "Mitos e verdades com justificativa científica",
        app_item_6: "Português, inglês e espanhol",
        app_btn: "Ver todos os recursos",

        sobre_rotulo: "Sobre o projeto",
        sobre_titulo: "O que é o Serpenteando",
        sobre_p1: "O Serpenteando é um aplicativo para celular criado para ajudar alunos a aprender sobre as serpentes brasileiras, tanto as peçonhentas quanto as que não são. Ele foi desenvolvido do zero em Android, com Java, como Trabalho de Conclusão de Curso do Instituto Federal da Paraíba.",
        sobre_p2: "A proposta é tornar o estudo de biologia e a prevenção de acidentes com serpentes mais fácil, dinâmico e organizado: em vez de só ler, o estudante consulta fichas, responde quiz, cumpre missões e explora o modelo 3D da cascavel.",
        sobre_card1_titulo: "O problema",
        sobre_card1_texto: "O conteúdo sobre serpentes costuma aparecer espalhado e distante da rotina do aluno, o que dificulta reconhecer espécies e entender o que fazer em caso de acidente.",
        sobre_card2_titulo: "A proposta",
        sobre_card2_texto: "Juntar conteúdo, prática e recompensa no mesmo lugar, com uma linguagem simples e telas feitas para o celular.",
        sobre_card3_titulo: "Para quem",
        sobre_card3_texto: "Estudantes do ensino médio que querem aprender biologia de forma mais leve e também entender como se proteger de animais peçonhentos.",

        rec_rotulo: "Recursos",
        rec_titulo: "O que já funciona no app",
        rec_texto: "Só está listado aqui o que já está implementado e funcionando no aplicativo.",
        rec1_titulo: "Fichas das espécies",
        rec1_texto: "Nome comum e científico, família, se é peçonhenta, descrição, habitat, alimentação, distribuição, reprodução e curiosidades.",
        rec2_titulo: "Quiz com três níveis",
        rec2_texto: "Perguntas de múltipla escolha em fácil, médio e difícil, com correção na hora e explicação do assunto.",
        rec3_titulo: "Missões com progresso",
        rec3_texto: "Objetivos como responder quizzes ou consultar conteúdos, com itens de acompanhamento e aviso de missão concluída.",
        rec4_titulo: "Anatomia 3D da cascavel",
        rec4_texto: "Modelo interativo para girar e dar zoom, com pontos que explicam cabeça, olhos, língua, escamas e guizo, além de um tour guiado.",
        rec5_titulo: "Mitos e verdades",
        rec5_texto: "Crenças populares classificadas como mito ou verdade, cada uma com a explicação científica do porquê.",
        rec6_titulo: "Ajustes e idiomas",
        rec6_texto: "Tema escuro, escolha do idioma do aplicativo (português, inglês e espanhol), versão do app e reporte de problemas.",

        video_rotulo: "Apresentação",
        video_titulo: "Vídeo do projeto",
        video_texto: "Apresentação em vídeo do Serpenteando, mostrando o aplicativo em funcionamento e as decisões do projeto.",
        video_aviso_titulo: "Vídeo em breve",
        video_aviso_texto: "A apresentação em vídeo será publicada nesta seção.",

        galeria_rotulo: "Galeria",
        galeria_titulo: "Telas do aplicativo",
        galeria_texto: "Imagens do app em uso, registradas no celular durante o desenvolvimento do TCC.",

        down_rotulo: "Download",
        down_titulo: "Instale o Serpenteando",
        down_texto: "O aplicativo é distribuído como arquivo APK, para instalar direto no celular Android. Não é necessário cadastro.",
        down_nota: "Ao instalar, o Android pode pedir para permitir a instalação de aplicativos de fontes desconhecidas — é normal em APKs baixados fora da Play Store.",
        down_versao: "Versão 0.1.0",
        down_tam_label: "Tamanho",
        down_req_label: "Requisitos",
        down_req: "Android 6.0 ou superior",
        down_tipo_label: "Formato",
        down_btn: "Baixar o APK",
        down_repo: "Ver o código no GitHub",

        equipe_rotulo: "Equipe",
        equipe_titulo: "Quem fez o Serpenteando",
        equipe_texto: "Trabalho de Conclusão de Curso do Instituto Federal da Paraíba.",
        cargo_desenvolvedor: "Desenvolvedor",
        cargo_orientador: "Orientador",
        cargo_cliente: "Cliente e avaliador",

        rodape_texto: "App educativo sobre serpentes brasileiras, feito como TCC do Instituto Federal da Paraíba.",
        rodape_credito: "Desenvolvido por Kaio Dantas e Ebert Souza · IFPB · 2026",
        rodape_tcc: "Trabalho de Conclusão de Curso · Curso Técnico em Informática"
    },

    en: {
        meta_titulo: "Serpenteando — an educational app about Brazilian snakes",
        meta_desc: "An educational app about Brazilian snakes, built as an IFPB final-year project. Species pages, quiz, missions, myths and facts, and 3D anatomy.",

        nav_app: "The app",
        nav_sobre: "About",
        nav_recursos: "Features",
        nav_video: "Video",
        nav_galeria: "Gallery",
        nav_equipe: "Team",
        nav_baixar: "Download",

        hero_selo: "Final project · Federal Institute of Paraíba",
        hero_titulo: "Get to know Brazilian snakes — and learn while playing",
        hero_texto: "Serpenteando is an educational app for high school students. It brings together species pages, a three-level quiz, missions, myths and facts, and an interactive 3D model of a rattlesnake — all in Portuguese, English and Spanish.",
        hero_btn1: "Download the APK",
        hero_btn2: "About the project",
        hero_info: "Android 6.0 or newer · version 0.1.0",

        badge_3d_titulo: "3D anatomy",
        badge_3d_texto: "Rotate, zoom and tap the points",
        badge_idioma_titulo: "3 languages",
        badge_idioma_texto: "PT · EN · ES",

        app_rotulo: "The app",
        app_titulo: "Inside the app",
        app_texto: "The screens below come from the working app. Browse the carousel to see each part of Serpenteando.",

        legenda_home: "Home screen with score, missions and streak",
        legenda_ficha: "Full species profile",
        legenda_quiz: "Quiz with easy, medium and hard levels",
        legenda_3d: "Interactive 3D model of the rattlesnake",
        legenda_mitos: "Myths and facts with a scientific explanation",
        legenda_missoes: "Missions with progress tracking",
        legenda_idiomas: "Language choice: Portuguese, English or Spanish",
        legenda_config: "App settings",
        legenda_splash: "Splash screen",

        app_info_titulo: "A pocket guide to snakes",
        app_info_texto: "The app gathers content on the jararaca, rattlesnake, coral snake, boa constrictor, anaconda, urutu, caninana and other species, showing which ones are venomous. Everything was written for a high school audience.",
        app_item_1: "Profiles with scientific name, habitat, diet and fun facts",
        app_item_2: "Quiz that explains the right answer",
        app_item_3: "Missions that track your progress",
        app_item_4: "3D rattlesnake anatomy with interactive points",
        app_item_5: "Myths and facts backed by science",
        app_item_6: "Portuguese, English and Spanish",
        app_btn: "See all features",

        sobre_rotulo: "About the project",
        sobre_titulo: "What Serpenteando is",
        sobre_p1: "Serpenteando is a mobile app created to help students learn about Brazilian snakes, both venomous and non-venomous. It was built from scratch in Android with Java as a final-year project at the Federal Institute of Paraíba.",
        sobre_p2: "The goal is to make studying biology and preventing snake accidents easier, more dynamic and better organised: instead of only reading, students browse species pages, answer quizzes, complete missions and explore the 3D rattlesnake model.",
        sobre_card1_titulo: "The problem",
        sobre_card1_texto: "Content about snakes is usually scattered and far from students' daily life, which makes it hard to recognise species and understand what to do after an accident.",
        sobre_card2_titulo: "The approach",
        sobre_card2_texto: "Bring content, practice and reward into one place, with plain language and screens designed for phones.",
        sobre_card3_titulo: "Who it is for",
        sobre_card3_texto: "High school students who want to learn biology in a lighter way and also understand how to protect themselves from venomous animals.",

        rec_rotulo: "Features",
        rec_titulo: "What already works in the app",
        rec_texto: "Only features that are already implemented and working are listed here.",
        rec1_titulo: "Species profiles",
        rec1_texto: "Common and scientific name, family, whether it is venomous, description, habitat, diet, distribution, reproduction and fun facts.",
        rec2_titulo: "Three-level quiz",
        rec2_texto: "Multiple-choice questions in easy, medium and hard levels, corrected instantly with an explanation.",
        rec3_titulo: "Missions with progress",
        rec3_texto: "Goals such as answering quizzes or reading content, with checklists and a message when a mission is complete.",
        rec4_titulo: "3D rattlesnake anatomy",
        rec4_texto: "An interactive model you can rotate and zoom, with points explaining head, eyes, tongue, scales and rattle, plus a guided tour.",
        rec5_titulo: "Myths and facts",
        rec5_texto: "Popular beliefs labelled as myth or fact, each one with the scientific explanation behind it.",
        rec6_titulo: "Settings and languages",
        rec6_texto: "Dark theme, app language (Portuguese, English and Spanish), app version and problem reporting.",

        video_rotulo: "Presentation",
        video_titulo: "Project video",
        video_texto: "A video presentation of Serpenteando, showing the app running and the decisions behind the project.",
        video_aviso_titulo: "Video coming soon",
        video_aviso_texto: "The video presentation will be published in this section.",

        galeria_rotulo: "Gallery",
        galeria_titulo: "App screens",
        galeria_texto: "Screens captured on a phone during the development of the project.",

        down_rotulo: "Download",
        down_titulo: "Install Serpenteando",
        down_texto: "The app is distributed as an APK file, installed directly on an Android phone. No account is needed.",
        down_nota: "During installation Android may ask you to allow apps from unknown sources — that is normal for APKs downloaded outside the Play Store.",
        down_versao: "Version 0.1.0",
        down_tam_label: "Size",
        down_req_label: "Requirements",
        down_req: "Android 6.0 or newer",
        down_tipo_label: "Format",
        down_btn: "Download the APK",
        down_repo: "See the code on GitHub",

        equipe_rotulo: "Team",
        equipe_titulo: "Who built Serpenteando",
        equipe_texto: "Final-year project at the Federal Institute of Paraíba.",
        cargo_desenvolvedor: "Developer",
        cargo_orientador: "Advisor",
        cargo_cliente: "Client and evaluator",

        rodape_texto: "Educational app about Brazilian snakes, built as a final-year project at the Federal Institute of Paraíba.",
        rodape_credito: "Built by Kaio Dantas and Ebert Souza · IFPB · 2026",
        rodape_tcc: "Final-year project · Technical course in Computing"
    },

    es: {
        meta_titulo: "Serpenteando — app educativo sobre serpientes brasileñas",
        meta_desc: "Aplicación educativa sobre serpientes brasileñas, desarrollada como trabajo de fin de curso del IFPB. Fichas de especies, quiz, misiones, mitos y verdades y anatomía 3D.",

        nav_app: "La app",
        nav_sobre: "Acerca",
        nav_recursos: "Recursos",
        nav_video: "Vídeo",
        nav_galeria: "Galería",
        nav_equipe: "Equipo",
        nav_baixar: "Descargar",

        hero_selo: "Trabajo de fin de curso · Instituto Federal de Paraíba",
        hero_titulo: "Conoce las serpientes brasileñas — y aprende jugando",
        hero_texto: "Serpenteando es una aplicación educativa para estudiantes de secundaria que reúne fichas de las especies, quiz con tres niveles, misiones, mitos y verdades y un modelo 3D interactivo de la cascabel — todo en portugués, inglés y español.",
        hero_btn1: "Descargar el APK",
        hero_btn2: "Conocer el proyecto",
        hero_info: "Android 6.0 o superior · versión 0.1.0",

        badge_3d_titulo: "Anatomía 3D",
        badge_3d_texto: "Gira, hace zoom y toca los puntos",
        badge_idioma_titulo: "3 idiomas",
        badge_idioma_texto: "PT · EN · ES",

        app_rotulo: "La aplicación",
        app_titulo: "La app por dentro",
        app_texto: "Las pantallas de abajo son de la aplicación funcionando. Recorre el carrusel para conocer cada parte de Serpenteando.",

        legenda_home: "Pantalla inicial con puntuación, misiones y racha",
        legenda_ficha: "Ficha completa de las especies más conocidas",
        legenda_quiz: "Quiz con niveles fácil, medio y difícil",
        legenda_3d: "Modelo 3D interactivo de la cascabel",
        legenda_mitos: "Mitos y verdades con explicación científica",
        legenda_missoes: "Misiones con seguimiento del progreso",
        legenda_idiomas: "Elección del idioma: portugués, inglés o español",
        legenda_config: "Ajustes de la app",
        legenda_splash: "Pantalla de inicio",

        app_info_titulo: "Una guía de bolsillo sobre serpientes",
        app_info_texto: "La app reúne contenido sobre yarará, cascabel, coral verdadera, boa, anaconda, urutu, caninana y otras especies, indicando cuáles son venenosas. Todo fue escrito para el público de secundaria.",
        app_item_1: "Fichas con nombre científico, hábitat, alimentación y curiosidades",
        app_item_2: "Quiz con explicación de la respuesta correcta",
        app_item_3: "Misiones que siguen tu progreso",
        app_item_4: "Anatomía 3D de la cascabel con puntos interactivos",
        app_item_5: "Mitos y verdades con justificación científica",
        app_item_6: "Portugués, inglés y español",
        app_btn: "Ver todos los recursos",

        sobre_rotulo: "Sobre el proyecto",
        sobre_titulo: "Qué es Serpenteando",
        sobre_p1: "Serpenteando es una aplicación para celular creada para ayudar a los alumnos a aprender sobre las serpientes brasileñas, tanto las venenosas como las que no lo son. Fue desarrollada desde cero en Android con Java como trabajo de fin de curso del Instituto Federal de Paraíba.",
        sobre_p2: "La propuesta es hacer que el estudio de biología y la prevención de accidentes con serpientes sea más fácil, dinámico y organizado: en lugar de solo leer, el estudiante consulta fichas, responde quiz, cumple misiones y explora el modelo 3D de la cascabel.",
        sobre_card1_titulo: "El problema",
        sobre_card1_texto: "El contenido sobre serpientes suele estar disperso y lejos de la rutina del alumno, lo que dificulta reconocer especies y entender qué hacer en caso de accidente.",
        sobre_card2_titulo: "La propuesta",
        sobre_card2_texto: "Reunir contenido, práctica y recompensa en un mismo lugar, con un lenguaje sencillo y pantallas pensadas para el celular.",
        sobre_card3_titulo: "Para quién",
        sobre_card3_texto: "Estudiantes de secundaria que quieren aprender biología de forma más ligera y también entender cómo protegerse de animales venenosos.",

        rec_rotulo: "Recursos",
        rec_titulo: "Lo que ya funciona en la app",
        rec_texto: "Aquí solo se lista lo que ya está implementado y funcionando en la aplicación.",
        rec1_titulo: "Fichas de las especies",
        rec1_texto: "Nombre común y científico, familia, si es venenosa, descripción, hábitat, alimentación, distribución, reproducción y curiosidades.",
        rec2_titulo: "Quiz con tres niveles",
        rec2_texto: "Preguntas de opción múltiple en fácil, medio y difícil, con corrección inmediata y explicación del tema.",
        rec3_titulo: "Misiones con progreso",
        rec3_texto: "Objetivos como responder quiz o consultar contenidos, con listas de seguimiento y aviso de misión completada.",
        rec4_titulo: "Anatomía 3D de la cascabel",
        rec4_texto: "Modelo interactivo para girar y hacer zoom, con puntos que explican cabeza, ojos, lengua, escamas y cascabel, además de un recorrido guiado.",
        rec5_titulo: "Mitos y verdades",
        rec5_texto: "Creencias populares clasificadas como mito o verdad, cada una con la explicación científica del porqué.",
        rec6_titulo: "Ajustes e idiomas",
        rec6_texto: "Tema oscuro, elección del idioma de la app (portugués, inglés y español), versión y reporte de problemas.",

        video_rotulo: "Presentación",
        video_titulo: "Vídeo del proyecto",
        video_texto: "Presentación en vídeo de Serpenteando, mostrando la aplicación funcionando y las decisiones del proyecto.",
        video_aviso_titulo: "Vídeo próximamente",
        video_aviso_texto: "La presentación en vídeo se publicará en esta sección.",

        galeria_rotulo: "Galería",
        galeria_titulo: "Pantallas de la aplicación",
        galeria_texto: "Imágenes de la app en uso, capturadas en el celular durante el desarrollo del trabajo.",

        down_rotulo: "Descarga",
        down_titulo: "Instala Serpenteando",
        down_texto: "La aplicación se distribuye como archivo APK, para instalar directamente en un celular Android. No hace falta registro.",
        down_nota: "Al instalar, Android puede pedir permiso para instalar aplicaciones de fuentes desconocidas — es normal en APK descargados fuera de Play Store.",
        down_versao: "Versión 0.1.0",
        down_tam_label: "Tamaño",
        down_req_label: "Requisitos",
        down_req: "Android 6.0 o superior",
        down_tipo_label: "Formato",
        down_btn: "Descargar el APK",
        down_repo: "Ver el código en GitHub",

        equipe_rotulo: "Equipo",
        equipe_titulo: "Quién hizo Serpenteando",
        equipe_texto: "Trabajo de fin de curso del Instituto Federal de Paraíba.",
        cargo_desenvolvedor: "Desarrollador",
        cargo_orientador: "Orientador",
        cargo_cliente: "Cliente y evaluador",

        rodape_texto: "App educativo sobre serpientes brasileñas, hecho como trabajo de fin de curso del Instituto Federal de Paraíba.",
        rodape_credito: "Desarrollado por Kaio Dantas y Ebert Souza · IFPB · 2026",
        rodape_tcc: "Trabajo de fin de curso · Curso Técnico en Informática"
    }
};

var idiomasDisponiveis = ["pt", "en", "es"];

/* Descobre o idioma salvo pelo visitante, ou usa o do navegador */
function idiomaInicial() {
    var salvo = null;
    try {
        salvo = localStorage.getItem("serpenteando-idioma");
    } catch (erro) {
        salvo = null;
    }

    if (salvo && idiomasDisponiveis.indexOf(salvo) !== -1) {
        return salvo;
    }

    var navegador = (navigator.language || "pt").slice(0, 2).toLowerCase();
    return idiomasDisponiveis.indexOf(navegador) !== -1 ? navegador : "pt";
}

/* Troca os textos de todos os elementos marcados com data-i18n */
function aplicarIdioma(idioma) {
    if (!TEXTOS[idioma]) {
        idioma = "pt";
    }

    var textos = TEXTOS[idioma];

    // Textos simples
    var elementos = document.querySelectorAll("[data-i18n]");
    for (var i = 0; i < elementos.length; i++) {
        var chave = elementos[i].getAttribute("data-i18n");
        if (textos[chave]) {
            elementos[i].textContent = textos[chave];
        }
    }

    // Atributos (ex.: content da meta description)
    var comAtributo = document.querySelectorAll("[data-i18n-attr]");
    for (var j = 0; j < comAtributo.length; j++) {
        var regra = comAtributo[j].getAttribute("data-i18n-attr").split(":");
        var atributo = regra[0];
        var chaveAttr = regra[1];
        if (textos[chaveAttr]) {
            comAtributo[j].setAttribute(atributo, textos[chaveAttr]);
        }
    }

    document.documentElement.setAttribute("lang", idioma === "pt" ? "pt-BR" : idioma);
    document.title = textos.meta_titulo;

    try {
        localStorage.setItem("serpenteando-idioma", idioma);
    } catch (erro) {
        // Navegador sem localStorage: segue sem salvar a escolha
    }

    var seletor = document.getElementById("seletor-idioma");
    if (seletor) {
        seletor.value = idioma;
    }
}

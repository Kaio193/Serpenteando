/* =========================================================
   i18n.js — textos do site em português, inglês e espanhol
   Os mesmos três idiomas que o aplicativo já oferece.
   Para aplicar um texto, o HTML usa data-i18n="chave".
   ========================================================= */

var TEXTOS = {
    pt: {
        meta_titulo: "Serpenteando — app educativo sobre serpentes brasileiras",
        meta_desc: "Aplicativo educativo sobre serpentes brasileiras, desenvolvido como TCC do IFSertãoPB. Fichas de espécies, quiz, missões, mitos e verdades e anatomia 3D.",

        nav_app: "O app",
        nav_sobre: "Sobre",
        nav_recursos: "Recursos",
        nav_video: "Vídeo",
        nav_equipe: "Equipe",
        nav_baixar: "Baixar o app",
        nav_download: "Download",

        tema_para_claro: "Ativar modo claro",
        tema_para_escuro: "Ativar modo escuro",

        hero_selo: "TCC · Instituto Federal do Sertão Paraibano (IFSertãoPB)",
        hero_titulo: "Conheça as serpentes brasileiras — e aprenda praticando",
        hero_texto: "O Serpenteando é um aplicativo educativo para estudantes do ensino médio que reúne fichas das espécies, quiz com três níveis, missões, mitos e verdades e um modelo 3D interativo da píton-real — tudo em português, inglês e espanhol.",
        hero_btn1: "Baixar o APK",
        hero_btn2: "Conhecer o projeto",
        hero_info: "Android 10.0 ou superior · versão 0.1.0",

        app_rotulo: "O aplicativo",
        app_titulo: "O aplicativo por dentro",
        app_texto: "As telas abaixo são do aplicativo em funcionamento. Percorra no carrossel para conhecer cada parte do Serpenteando.",

        legenda_splash: "Tela de inicialização",
        legenda_home: "Tela inicial com pontuação, missões e sequência",
        legenda_ficha: "Fichas com nome científico, habitat, alimentação e curiosidades",
        legenda_quiz: "Quiz com explicação da resposta certa",
        legenda_3d: "Modelo 3D interativo da píton-real",
        legenda_mitos: "Mitos e verdades com justificativa científica",
        legenda_missoes: "Missões que acompanham o seu progresso",
        legenda_idiomas: "Português, inglês e espanhol",
        legenda_config: "Configurações do app",

        app_btn: "Ver todos os recursos",

        tela_splash_titulo: "Abertura do aplicativo",
        tela_splash_texto: "A tela de inicialização mostra a logo e o nome do app enquanto o conteúdo é carregado.",
        tela_home_titulo: "Tela inicial",
        tela_home_texto: "Mostra a pontuação, a sequência de dias e as missões em andamento, com atalhos para conteúdo, quiz e anatomia 3D.",
        tela_ficha_titulo: "Ficha da espécie",
        tela_ficha_texto: "Nome comum e científico, família, se é peçonhenta, descrição, habitat, alimentação, distribuição e curiosidades.",
        tela_quiz_titulo: "Quiz em três níveis",
        tela_quiz_texto: "A pessoa escolhe fácil, médio ou difícil; cada pergunta vem com correção na hora e a explicação da resposta.",
        tela_3d_titulo: "Anatomia 3D",
        tela_3d_texto: "Modelo 3D interativo da píton-real que permite girar e ampliar a visualização para explorar a serpente.",
        tela_mitos_titulo: "Mitos e verdades",
        tela_mitos_texto: "Cada crença popular aparece classificada como mito ou verdade, com a explicação científica logo abaixo.",
        tela_missoes_titulo: "Missões",
        tela_missoes_texto: "Objetivos como responder quizzes ou consultar conteúdos, com barra de progresso, pontos e aviso de missão concluída.",
        tela_idiomas_titulo: "Três idiomas",
        tela_idiomas_texto: "Português, inglês e espanhol: o app troca os textos na hora, sem precisar reiniciar.",
        tela_config_titulo: "Ajustes",
        tela_config_texto: "Tema escuro, idioma, versão do app, reporte de problemas e informações do projeto.",

        sobre_rotulo: "Sobre o projeto",
        sobre_titulo: "O que é o Serpenteando?",
        sobre_p1: "O Serpenteando é um aplicativo para celular criado para ajudar alunos a aprender sobre as serpentes brasileiras, tanto as peçonhentas quanto as que não são. Ele foi desenvolvido do zero em Android, com Java, como Trabalho de Conclusão de Curso do Instituto Federal do Sertão Paraibano (IFSertãoPB).",
        sobre_p2: "A proposta é tornar o estudo de biologia e a prevenção de acidentes com serpentes mais fácil, dinâmico e organizado: em vez de só ler, o estudante consulta fichas, responde quiz, cumpre missões e explora o modelo 3D da píton-real.",
        sobre_card1_titulo: "O problema",
        sobre_card1_texto: "O conteúdo sobre serpentes costuma aparecer espalhado e distante da rotina do aluno, o que dificulta reconhecer espécies e entender o que fazer em caso de acidente.",
        sobre_card2_titulo: "A proposta",
        sobre_card2_texto: "Juntar conteúdo, prática e recompensa no mesmo lugar, com uma linguagem simples e telas feitas para o celular.",
        sobre_card3_titulo: "Para quem?",
        sobre_card3_texto: "Estudantes do ensino médio que querem aprender biologia de forma mais leve e também entender como se proteger de animais peçonhentos.",

        rec_rotulo: "Recursos",
        rec_titulo: "O que funciona no aplicativo?",
        rec_texto: "Só está listado aqui o que já está implementado e funcionando no aplicativo.",
        rec1_titulo: "Fichas das espécies",
        rec1_texto: "Nome comum e científico, família, se é peçonhenta, descrição, habitat, alimentação, distribuição, reprodução e curiosidades.",
        rec2_titulo: "Quiz com três níveis",
        rec2_texto: "Perguntas de múltipla escolha em fácil, médio e difícil, com correção na hora e explicação do assunto.",
        rec3_titulo: "Missões com progresso",
        rec3_texto: "Objetivos como responder quizzes ou consultar conteúdos, com itens de acompanhamento e aviso de missão concluída.",
        rec4_titulo: "Anatomia 3D da píton-real",
        rec4_texto: "Modelo 3D interativo da píton-real que permite girar e ampliar a visualização para explorar a serpente.",
        rec5_titulo: "Mitos e verdades",
        rec5_texto: "Crenças populares classificadas como mito ou verdade, cada uma com a explicação científica do porquê.",
        rec6_titulo: "Ajustes e idiomas",
        rec6_texto: "Tema escuro, escolha do idioma do aplicativo (português, inglês e espanhol), versão do app e reporte de problemas.",

        rec7_titulo: "Dica do dia",
        rec7_texto: "Conteúdo diário com 85 dicas sobre curiosidades das serpentes e prevenção de acidentes. Apenas uma dica é exibida por dia, mudando automaticamente a cada 24 horas.",
        rec8_titulo: "Notificações",
        rec8_texto: "Receba lembretes para conferir a dica do dia e completar as missões. As notificações podem ser ativadas ou desativadas nas configurações do aplicativo.",

        video_rotulo: "Apresentação",
        video_titulo: "Vídeo do aplicativo",
        video_texto: "Apresentação em vídeo do Serpenteando, mostrando o aplicativo em funcionamento e as decisões do projeto.",
        video_aviso_titulo: "Vídeo em breve",
        video_aviso_texto: "A apresentação em vídeo será publicada nesta seção.",
        video_play: "Assistir ao vídeo no YouTube",
        video_youtube: "Assistir no YouTube",

        down_rotulo: "Download",
        down_titulo: "Instale o Serpenteando",
        down_texto: "O aplicativo é distribuído como arquivo APK, para instalar direto no celular Android. Não é necessário cadastro.",
        down_nota: "Ao instalar, o Android pode pedir para permitir a instalação de aplicativos de fontes desconhecidas — é normal em APKs baixados fora da Play Store.",
        down_versao: "Versão 0.1.0",
        down_tam_label: "Tamanho",
        down_req_label: "Requisitos",
        down_req: "Android 10.0 ou superior",
        down_tipo_label: "Formato",
        down_btn: "Baixar o APK",
        down_repo: "Ver o código no GitHub",

        equipe_rotulo: "Equipe",
        equipe_titulo: "Quem fez o Serpenteando?",
        equipe_texto: "Trabalho de Conclusão de Curso do Instituto Federal do Sertão Paraibano (IFSertãoPB).",
        cargo_desenvolvedor: "Desenvolvedor",
        cargo_orientador: "Orientador",
        cargo_avaliador: "Avaliador",
        cargo_cliente: "Cliente/avaliador",

        rodape_texto: "App educativo sobre serpentes brasileiras, feito como TCC do Instituto Federal do Sertão Paraibano (IFSertãoPB).",
        rodape_credito: "Desenvolvido por Kaio Dantas e Ebert Souza · IFSertãoPB · 2026",
        rodape_tcc: "Trabalho de Conclusão de Curso · Curso Técnico em Informática"
    },

    en: {
        meta_titulo: "Serpenteando — an educational app about Brazilian snakes",
        meta_desc: "An educational app about Brazilian snakes, built as an IFSertãoPB final-year project. Species pages, quiz, missions, myths and facts, and 3D anatomy.",

        nav_app: "The app",
        nav_sobre: "About",
        nav_recursos: "Features",
        nav_video: "Video",
        nav_equipe: "Team",
        nav_baixar: "Download",
        nav_download: "Download",

        tema_para_claro: "Switch to light mode",
        tema_para_escuro: "Switch to dark mode",

        hero_selo: "Final project · Federal Institute of Sertão Paraibano (IFSertãoPB)",
        hero_titulo: "Get to know Brazilian snakes — and learn by practising",
        hero_texto: "Serpenteando is an educational app for high school students. It brings together species pages, a three-level quiz, missions, myths and facts, and an interactive 3D model of a ball python — all in Portuguese, English and Spanish.",
        hero_btn1: "Download the APK",
        hero_btn2: "About the project",
        hero_info: "Android 10.0 or newer · version 0.1.0",

        app_rotulo: "The app",
        app_titulo: "Inside the app",
        app_texto: "The screens below come from the working app. Browse the carousel to see each part of Serpenteando.",

        legenda_splash: "Splash screen",
        legenda_home: "Home screen with score, missions and streak",
        legenda_ficha: "Profiles with scientific name, habitat, diet and fun facts",
        legenda_quiz: "Quiz that explains the right answer",
        legenda_3d: "Interactive 3D model of a ball python",
        legenda_mitos: "Myths and facts with a scientific justification",
        legenda_missoes: "Missions that track your progress",
        legenda_idiomas: "Portuguese, English and Spanish",
        legenda_config: "App settings",

        app_btn: "See all features",

        tela_splash_titulo: "App launch",
        tela_splash_texto: "The splash screen shows the logo and the app name while the content loads.",
        tela_home_titulo: "Home screen",
        tela_home_texto: "Shows score, daily streak and ongoing missions, with shortcuts to content, quiz and 3D anatomy.",
        tela_ficha_titulo: "Species profile",
        tela_ficha_texto: "Common and scientific name, family, whether it is venomous, description, habitat, diet, distribution and fun facts.",
        tela_quiz_titulo: "Three-level quiz",
        tela_quiz_texto: "You pick easy, medium or hard; each question is corrected instantly with an explanation.",
        tela_3d_titulo: "3D anatomy",
        tela_3d_texto: "An interactive 3D model of a ball python that you can rotate and zoom to explore the snake.",
        tela_mitos_titulo: "Myths and facts",
        tela_mitos_texto: "Each popular belief is labelled myth or fact, with the scientific explanation right below.",
        tela_missoes_titulo: "Missions",
        tela_missoes_texto: "Goals such as answering quizzes or reading content, with a progress bar, points and a completion notice.",
        tela_idiomas_titulo: "Three languages",
        tela_idiomas_texto: "Portuguese, English and Spanish: the app switches the text instantly, no restart needed.",
        tela_config_titulo: "Settings",
        tela_config_texto: "Dark theme, language, app version, problem reporting and project information.",

        sobre_rotulo: "About the project",
        sobre_titulo: "What is Serpenteando?",
        sobre_p1: "Serpenteando is a mobile app created to help students learn about Brazilian snakes, both venomous and non-venomous. It was built from scratch in Android with Java as a final-year project at the Federal Institute of Sertão Paraibano (IFSertãoPB).",
        sobre_p2: "The goal is to make studying biology and preventing snake accidents easier, more dynamic and better organised: instead of only reading, students browse species pages, answer quizzes, complete missions and explore the 3D ball python model.",
        sobre_card1_titulo: "The problem",
        sobre_card1_texto: "Content about snakes is usually scattered and far from students' daily life, which makes it hard to recognise species and understand what to do after an accident.",
        sobre_card2_titulo: "The approach",
        sobre_card2_texto: "Bring content, practice and reward into one place, with plain language and screens designed for phones.",
        sobre_card3_titulo: "Who is it for?",
        sobre_card3_texto: "High school students who want to learn biology in a lighter way and also understand how to protect themselves from venomous animals.",

        rec_rotulo: "Features",
        rec_titulo: "What works in the app?",
        rec_texto: "Only features that are already implemented and working are listed here.",
        rec1_titulo: "Species profiles",
        rec1_texto: "Common and scientific name, family, whether it is venomous, description, habitat, diet, distribution, reproduction and fun facts.",
        rec2_titulo: "Three-level quiz",
        rec2_texto: "Multiple-choice questions in easy, medium and hard levels, corrected instantly with an explanation.",
        rec3_titulo: "Missions with progress",
        rec3_texto: "Goals such as answering quizzes or reading content, with checklists and a message when a mission is complete.",
        rec4_titulo: "3D ball python anatomy",
        rec4_texto: "An interactive 3D model of a ball python that you can rotate and zoom to explore the snake.",
        rec5_titulo: "Myths and facts",
        rec5_texto: "Popular beliefs labelled as myth or fact, each one with the scientific explanation behind it.",
        rec6_titulo: "Settings and languages",
        rec6_texto: "Dark theme, app language (Portuguese, English and Spanish), app version and problem reporting.",

        rec7_titulo: "Tip of the day",
        rec7_texto: "A daily tip with 85 entries about snake facts and accident prevention. Only one tip is shown per day, changing automatically every 24 hours.",
        rec8_titulo: "Notifications",
        rec8_texto: "Get reminders to check the tip of the day and complete the missions. Notifications can be turned on or off in the app settings.",

        video_rotulo: "Presentation",
        video_titulo: "App video",
        video_texto: "A video presentation of Serpenteando, showing the app running and the decisions behind the project.",
        video_aviso_titulo: "Video coming soon",
        video_aviso_texto: "The video presentation will be published in this section.",
        video_play: "Watch the video on YouTube",
        video_youtube: "Watch on YouTube",

        down_rotulo: "Download",
        down_titulo: "Install Serpenteando",
        down_texto: "The app is distributed as an APK file, installed directly on an Android phone. No account is needed.",
        down_nota: "During installation Android may ask you to allow apps from unknown sources — that is normal for APKs downloaded outside the Play Store.",
        down_versao: "Version 0.1.0",
        down_tam_label: "Size",
        down_req_label: "Requirements",
        down_req: "Android 10.0 or newer",
        down_tipo_label: "Format",
        down_btn: "Download the APK",
        down_repo: "See the code on GitHub",

        equipe_rotulo: "Team",
        equipe_titulo: "Who built Serpenteando?",
        equipe_texto: "Final-year project at the Federal Institute of Sertão Paraibano (IFSertãoPB).",
        cargo_desenvolvedor: "Developer",
        cargo_orientador: "Advisor",
        cargo_avaliador: "Evaluator",
        cargo_cliente: "Client/evaluator",

        rodape_texto: "Educational app about Brazilian snakes, built as a final-year project at the Federal Institute of Sertão Paraibano (IFSertãoPB).",
        rodape_credito: "Built by Kaio Dantas and Ebert Souza · IFSertãoPB · 2026",
        rodape_tcc: "Final-year project · Technical course in Computing"
    },

    es: {
        meta_titulo: "Serpenteando — app educativo sobre serpientes brasileñas",
        meta_desc: "Aplicación educativa sobre serpientes brasileñas, desarrollada como trabajo de fin de curso del IFSertãoPB. Fichas de especies, quiz, misiones, mitos y verdades y anatomía 3D.",

        nav_app: "La app",
        nav_sobre: "Acerca",
        nav_recursos: "Recursos",
        nav_video: "Vídeo",
        nav_equipe: "Equipo",
        nav_baixar: "Descargar",
        nav_download: "Descargar",

        tema_para_claro: "Activar modo claro",
        tema_para_escuro: "Activar modo oscuro",

        hero_selo: "Trabajo de fin de curso · Instituto Federal del Sertão Paraibano (IFSertãoPB)",
        hero_titulo: "Conoce las serpientes brasileñas — y aprende practicando",
        hero_texto: "Serpenteando es una aplicación educativa para estudiantes de secundaria que reúne fichas de las especies, quiz con tres niveles, misiones, mitos y verdades y un modelo 3D interactivo de la pitón real — todo en portugués, inglés y español.",
        hero_btn1: "Descargar el APK",
        hero_btn2: "Conocer el proyecto",
        hero_info: "Android 10.0 o superior · versión 0.1.0",

        app_rotulo: "La aplicación",
        app_titulo: "La aplicación por dentro",
        app_texto: "Las pantallas de abajo son de la aplicación funcionando. Recorre el carrusel para conocer cada parte de Serpenteando.",

        legenda_splash: "Pantalla de inicio",
        legenda_home: "Pantalla inicial con puntuación, misiones y racha",
        legenda_ficha: "Fichas con nombre científico, hábitat, alimentación y curiosidades",
        legenda_quiz: "Quiz con explicación de la respuesta correcta",
        legenda_3d: "Modelo 3D interactivo de la pitón real",
        legenda_mitos: "Mitos y verdades con justificación científica",
        legenda_missoes: "Misiones que acompañan tu progreso",
        legenda_idiomas: "Portugués, inglés y español",
        legenda_config: "Ajustes de la app",

        app_btn: "Ver todos los recursos",

        tela_splash_titulo: "Apertura de la app",
        tela_splash_texto: "La pantalla de inicio muestra el logo y el nombre de la app mientras se carga el contenido.",
        tela_home_titulo: "Pantalla inicial",
        tela_home_texto: "Muestra la puntuación, la racha de días y las misiones en curso, con accesos a contenido, quiz y anatomía 3D.",
        tela_ficha_titulo: "Ficha de la especie",
        tela_ficha_texto: "Nombre común y científico, familia, si es venenosa, descripción, hábitat, alimentación, distribución y curiosidades.",
        tela_quiz_titulo: "Quiz en tres niveles",
        tela_quiz_texto: "Se elige fácil, medio o difícil; cada pregunta se corrige al instante con la explicación.",
        tela_3d_titulo: "Anatomía 3D",
        tela_3d_texto: "Modelo 3D interactivo de la pitón real que permite girar y ampliar la visualización para explorar la serpiente.",
        tela_mitos_titulo: "Mitos y verdades",
        tela_mitos_texto: "Cada creencia popular aparece como mito o verdad, con la explicación científica debajo.",
        tela_missoes_titulo: "Misiones",
        tela_missoes_texto: "Objetivos como responder quiz o consultar contenidos, con barra de progreso, puntos y aviso de misión completada.",
        tela_idiomas_titulo: "Tres idiomas",
        tela_idiomas_texto: "Portugués, inglés y español: la app cambia los textos al instante, sin reiniciar.",
        tela_config_titulo: "Ajustes",
        tela_config_texto: "Tema oscuro, idioma, versión de la app, reporte de problemas e información del proyecto.",

        sobre_rotulo: "Sobre el proyecto",
        sobre_titulo: "¿Qué es Serpenteando?",
        sobre_p1: "Serpenteando es una aplicación para celular creada para ayudar a los alumnos a aprender sobre las serpientes brasileñas, tanto las venenosas como las que no lo son. Fue desarrollada desde cero en Android con Java como trabajo de fin de curso del Instituto Federal del Sertão Paraibano (IFSertãoPB).",
        sobre_p2: "La propuesta es hacer que el estudio de biología y la prevención de accidentes con serpientes sea más fácil, dinámico y organizado: en lugar de solo leer, el estudiante consulta fichas, responde quiz, cumple misiones y explora el modelo 3D de la pitón real.",
        sobre_card1_titulo: "El problema",
        sobre_card1_texto: "El contenido sobre serpientes suele estar disperso y lejos de la rutina del alumno, lo que dificulta reconocer especies y entender qué hacer en caso de accidente.",
        sobre_card2_titulo: "La propuesta",
        sobre_card2_texto: "Reunir contenido, práctica y recompensa en un mismo lugar, con un lenguaje sencillo y pantallas pensadas para el celular.",
        sobre_card3_titulo: "¿Para quién?",
        sobre_card3_texto: "Estudiantes de secundaria que quieren aprender biología de forma más ligera y también entender cómo protegerse de animales venenosos.",

        rec_rotulo: "Recursos",
        rec_titulo: "¿Qué funciona en la aplicación?",
        rec_texto: "Aquí solo se lista lo que ya está implementado y funcionando en la aplicación.",
        rec1_titulo: "Fichas de las especies",
        rec1_texto: "Nombre común y científico, familia, si es venenosa, descripción, hábitat, alimentación, distribución, reproducción y curiosidades.",
        rec2_titulo: "Quiz con tres niveles",
        rec2_texto: "Preguntas de opción múltiple en fácil, medio y difícil, con corrección inmediata y explicación del tema.",
        rec3_titulo: "Misiones con progreso",
        rec3_texto: "Objetivos como responder quiz o consultar contenidos, con listas de seguimiento y aviso de misión completada.",
        rec4_titulo: "Anatomía 3D de la pitón real",
        rec4_texto: "Modelo 3D interactivo de la pitón real que permite girar y ampliar la visualización para explorar la serpiente.",
        rec5_titulo: "Mitos y verdades",
        rec5_texto: "Creencias populares clasificadas como mito o verdad, cada una con la explicación científica del porqué.",
        rec6_titulo: "Ajustes e idiomas",
        rec6_texto: "Tema oscuro, elección del idioma de la app (portugués, inglés y español), versión y reporte de problemas.",

        rec7_titulo: "Dica del día",
        rec7_texto: "Contenido diario con 85 consejos sobre curiosidades de las serpientes y prevención de accidentes. Solo se muestra un consejo por día, cambiando automáticamente cada 24 horas.",
        rec8_titulo: "Notificaciones",
        rec8_texto: "Recibe recordatorios para consultar el consejo del día y completar las misiones. Las notificaciones se pueden activar o desactivar en los ajustes de la aplicación.",

        video_rotulo: "Presentación",
        video_titulo: "Vídeo de la aplicación",
        video_texto: "Presentación en vídeo de Serpenteando, mostrando la aplicación funcionando y las decisiones del proyecto.",
        video_aviso_titulo: "Vídeo próximamente",
        video_aviso_texto: "La presentación en vídeo se publicará en esta sección.",
        video_play: "Ver el vídeo en YouTube",
        video_youtube: "Ver en YouTube",

        down_rotulo: "Descarga",
        down_titulo: "Instala Serpenteando",
        down_texto: "La aplicación se distribuye como archivo APK, para instalar directamente en un celular Android. No hace falta registro.",
        down_nota: "Al instalar, Android puede pedir permiso para instalar aplicaciones de fuentes desconocidas — es normal en APK descargados fuera de Play Store.",
        down_versao: "Versión 0.1.0",
        down_tam_label: "Tamaño",
        down_req_label: "Requisitos",
        down_req: "Android 10.0 o superior",
        down_tipo_label: "Formato",
        down_btn: "Descargar el APK",
        down_repo: "Ver el código en GitHub",

        equipe_rotulo: "Equipo",
        equipe_titulo: "¿Quién hizo Serpenteando?",
        equipe_texto: "Trabajo de fin de curso del Instituto Federal del Sertão Paraibano (IFSertãoPB).",
        cargo_desenvolvedor: "Desarrollador",
        cargo_orientador: "Orientador",
        cargo_avaliador: "Evaluador",
        cargo_cliente: "Cliente/evaluador",

        rodape_texto: "App educativo sobre serpientes brasileñas, hecho como trabajo de fin de curso del Instituto Federal del Sertão Paraibano (IFSertãoPB).",
        rodape_credito: "Desarrollado por Kaio Dantas y Ebert Souza · IFPB · 2026",
        rodape_tcc: "Trabajo de fin de curso · Curso Técnico en Informática"
    }
};

var idiomasDisponiveis = ["pt", "en", "es"];

/* Capturas de tela do app em cada idioma.
   A chave é o data-imagem que está no HTML; o carrossel e o destaque do topo
   trocam a imagem junto com os textos quando o visitante muda o idioma.
   As versões em inglês e espanhol ficam em img/en/ e img/es/, com o mesmo
   nome de arquivo da versão em português. */
var IMAGENS = {
    pt: {
        splash: "img/tela-inicializacao.jpeg",
        home: "img/tela-inicial.jpeg",
        ficha: "img/ficha-especies.jpeg",
        quiz: "img/quiz-dificuldade.jpeg",
        "3d": "img/modelo-3d.jpeg",
        mitos: "img/mitos-verdades.jpeg",
        missoes: "img/missoes.jpeg",
        idiomas: "img/idiomas.jpeg",
        config: "img/configuracoes.jpeg"
    },
    en: {
        splash: "img/en/tela-inicializacao.jpeg",
        home: "img/en/tela-inicial.jpeg",
        ficha: "img/en/ficha-especies.jpeg",
        quiz: "img/en/quiz-dificuldade.jpeg",
        "3d": "img/en/modelo-3d.jpeg",
        mitos: "img/en/mitos-verdades.jpeg",
        missoes: "img/en/missoes.jpeg",
        idiomas: "img/en/idiomas.jpeg",
        config: "img/en/configuracoes.jpeg"
    },
    es: {
        splash: "img/es/tela-inicializacao.jpeg",
        home: "img/es/tela-inicial.jpeg",
        ficha: "img/es/ficha-especies.jpeg",
        quiz: "img/es/quiz-dificuldade.jpeg",
        "3d": "img/es/modelo-3d.jpeg",
        mitos: "img/es/mitos-verdades.jpeg",
        missoes: "img/es/missoes.jpeg",
        idiomas: "img/es/idiomas.jpeg",
        config: "img/es/configuracoes.jpeg"
    }
};

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

    // Capturas de tela no idioma escolhido (carrossel e destaque do topo)
    var capturas = IMAGENS[idioma] || IMAGENS.pt;
    var imagensApp = document.querySelectorAll("[data-imagem]");
    for (var m = 0; m < imagensApp.length; m++) {
        var tela = imagensApp[m].getAttribute("data-imagem");
        if (capturas[tela]) {
            imagensApp[m].setAttribute("src", capturas[tela]);
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

    idiomaAtual = idioma;
    atualizarRotuloTema();
}

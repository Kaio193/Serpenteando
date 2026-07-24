package com.kaio.cobra3d.view.renderer;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public final class TourOverlayManager {

    private static final long FADE_DURATION_MS = 400;
    private static final long DISPLAY_DURATION_MS = 3000;
    private static final long HIDE_DURATION_MS = 300;

    private final View overlayPanel;
    private final TextView nameView;
    private final TextView functionView;
    private final TextView howItWorksView;
    private final TextView curiosityView;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideRunnable = this::animateOut;

    private boolean visible;

    public static final class StructureInfo {
        public final String name;
        public final String function;
        public final String howItWorks;
        public final String curiosity;

        public StructureInfo(String name, String function, String howItWorks, String curiosity) {
            this.name = name;
            this.function = function;
            this.howItWorks = howItWorks;
            this.curiosity = curiosity;
        }
    }

    public static final StructureInfo[] STRUCTURES = {
        new StructureInfo(
            "Escamas e Pele",
            "Proteção, camuflagem e percepção sensorial",
            "As escamas da Cascavel são placas de queratina que cobrem todo o corpo. Escamas quilhadas (com crista central) dão textura áspera. A pele é trocada periodicamente (ecdise) -- uma Cascavel adulta troca de pele 2-4 vezes por ano.",
            "O chocalho é feito de queratina igual as escamas. Cada vez que a cobra troca de pele, um novo segmento é adicionado ao chocalho -- mas ele quebra com o tempo, então não dá para saber a idade pelos segmentos!"
        ),
        new StructureInfo(
            "Camada Muscular",
            "Locomoção, constrição e ataque",
            "A Cascavel possui ~300 pares de músculos ao longo da coluna vertebral. Músculos epaxiais (dorsais) e hipaxiais (ventrais) trabalham em ondas alternadas para produzir o movimento serpentiforme. Músculos da cabeça controlam a abertura de até 180 graus da mandíbula.",
            "A velocidade de ataque de uma Cascavel pode chegar a 2,5 m/s -- mais rápido que o reflexo humano. O bote completo leva menos de 100 ms."
        ),
        new StructureInfo(
            "Tecido Interno e Conectivo",
            "Suporte estrutural e isolamento",
            "O tecido conjuntivo envolve e protege os órgãos internos. A fáscia muscular conecta a pele aos músculos, permitindo deslizamento suave. O mesentério suspende o intestino na cavidade celomática. A Cascavel não tem diafragma -- depende dos músculos do corpo para respirar.",
            "Répteis têm metabolismo ectotérmico. A Cascavel pode sobreviver até 2 anos sem se alimentar, reduzindo seu metabolismo em até 70%."
        ),
        new StructureInfo(
            "Esqueleto Completo",
            "Estrutura axial, proteção da medula e ancoragem muscular",
            "A Cascavel possui 150-300 vértebras, cada uma com costelas articuladas. O crânio é altamente cinético -- os ossos são conectados por ligamentos elásticos, permitindo engolir presas maiores que a cabeça. Não há externo: as costelas são livres ventralmente.",
            "Diferente de mamíferos, a Cascavel não tem membros. Os vestígios evolutivos da cintura pélvica ainda existem em pitões, mas desapareceram completamente nas cascavéis."
        ),
        new StructureInfo(
            "Crânio",
            "Proteção cerebral, captura de presas e alimentação",
            "O crânio da Cascavel é formado por ossos móveis (maxila, pterigoide, quadrado). A mandíbula inferior divide-se em dois ramos unidos por um ligamento elástico, permitindo deglutição de presas grandes. As fossetas loreais (termossensoriais) ficam entre o olho e a narina.",
            "O crânio das cascavéis tem formato triangular característico, mais largo que o pescoço, abrigando as glândulas de veneno e os músculos adutores mandibulares."
        ),
        new StructureInfo(
            "Presas",
            "Inoculação de veneno por hipoderme",
            "As presas são dentes modificados, ocos e retráteis, localizados na maxila. Em repouso, ficam dobradas contra o céu da boca. Durante o bote, os músculos pterigoideos projetam as presas para frente. O veneno passa pelo canal interno e sai pelo orifício subterminal.",
            "As presas da Cascavel medem de 1 a 3 cm e são substituídas continuamente. Uma Cascavel pode produzir presas de reposição em 6-8 semanas se perder uma presa durante o ataque."
        ),
        new StructureInfo(
            "Glândulas de Veneno",
            "Produção e armazenamento de toxinas",
            "As glândulas de veneno são estruturas alongadas atrás dos olhos, envolvidas por músculo compressor. Quando a cobra morde, o músculo contrai e força o veneno através do duto até as presas. Cada glândula armazena até 1 ml de veneno líquido.",
            "O veneno da Crotalus durissus é proteolítico e neurotóxico. Uma dose pode matar até 20 ratos adultos. A LD50 em camundongos é de ~0,5 mg/kg -- extremamente potente."
        ),
        new StructureInfo(
            "Dentes",
            "Preensão e retenção de presas",
            "A Cascavel possui fileiras de dentes recurvados no pterigoide, palatino e dentário. São acrodontes (fixos ao osso) e substituídos continuamente ao longo da vida. Os dentes posteriores auxiliam a empurrar a presa em direção ao esôfago.",
            "Uma Cascavel pode ter até 200 dentes em diferentes estágios de desenvolvimento. Ao contrário das presas, os dentes não são ocos e não inoculam veneno."
        ),
        new StructureInfo(
            "Língua Bifurcada",
            "Coleta de partículas químicas para o órgão de Jacobson",
            "A língua é longa, fina e bifurcada (dividida em duas pontas). Cada ponta coleta moléculas odoríferas independentemente. Ao retrair a língua, as pontas tocam o órgão de Jacobson no céu da boca, transferindo as partículas para análise química.",
            "A bifurcação permite detecção estereoscópica de odores -- a Cascavel sente a direção do cheiro comparando o sinal entre as duas pontas. A língua não tem papilas gustativas."
        ),
        new StructureInfo(
            "Órgão de Jacobson (Vomeronasal)",
            "Análise química de feromônios e presas",
            "Localizado no céu da boca, o órgão de Jacobson é um par de câmaras sensoriais revestidas por epitélio olfativo. As partículas coletadas pela língua são transferidas para essas câmaras, onde neurônios sensoriais identificam moléculas específicas.",
            "Este órgão é tão sensível que a Cascavel pode detectar a presença de uma presa a até 200 metros, seguindo seu rastro químico no solo."
        ),
        new StructureInfo(
            "Cérebro",
            "Processamento sensorial e coordenação motora",
            "O cérebro da Cascavel é pequeno (proporcional ao corpo) mas especializado. O lobo óptico processa visão noturna. O telencéfalo coordena comportamento de caça e defesa. O cerebelo controla os movimentos serpentiformes precisos. O bulbo olfativo é relativamente grande.",
            "O cérebro reptiliano funciona em temperaturas corporais baixas. A Cascavel pode ficar ativa com temperatura corporal de apenas 15 graus C, graças a adaptações enzimáticas."
        ),
        new StructureInfo(
            "Sistema Nervoso",
            "Controle motor, reflexos e percepção sensorial",
            "O sistema nervoso consiste em cérebro, medula espinhal (protegida pela coluna) e nervos periféricos. A medula espinhal controla os movimentos ondulatórios através de geradores de padrão central (CPGs) -- a cobra pode se mover mesmo sem input cerebral.",
            "Uma Cascavel decapitada ainda pode dar o bote e inocular veneno por até 60 minutos, devido aos reflexos espinhais autônomos."
        ),
        new StructureInfo(
            "Sistema Circulatório",
            "Transporte de oxigênio, nutrientes e hormônios",
            "O coração da Cascavel é tricavitário (dois átrios, um ventrículo com septo parcial). Sangue oxigenado e desoxigenado se misturam minimamente. A circulação é dupla incompleta. O sangue contém hemácias nucleadas ovais, ao contrário dos mamíferos.",
            "A Cascavel pode redirecionar o fluxo sanguíneo ao engolir presas grandes -- o sangue é desviado do trato digestório anterior para o posterior, otimizando a digestão."
        ),
        new StructureInfo(
            "Coração",
            "Bombeamento sanguíneo -- 2 átrios, 1 ventrículo funcional",
            "Localizado a aproximadamente 1/3 do comprimento do corpo a partir da cabeça. O coração reptiliano bate de 30 a 60 bpm à temperatura ambiente. O ventrículo tem cristas musculares que reduzem a mistura sanguínea. O cono arterioso divide-se em três artérias.",
            "O coração da Cascavel pode deslocar-se dentro do corpo quando a cobra engole presas grandes -- ele se move para trás para não ser comprimido pelo bolo alimentar."
        ),
        new StructureInfo(
            "Sistema Respiratório",
            "Troca gasosa -- pulmão único funcional",
            "A Cascavel tem apenas um pulmão funcional (o direito), longo e sacular, estendendo-se por grande parte do corpo. O pulmão esquerdo é vestigial/ausente. A respiração é controlada por músculos intercostais -- não há diafragma. A traqueia também tem função de troca gasosa.",
            "A Cascavel pode prender a respiração por até 30 minutos durante o mergulho. Durante a deglutição de presas grandes, a traqueia se projeta para fora da boca para permitir a respiração."
        ),
        new StructureInfo(
            "Sistema Digestório",
            "Digestão química e mecânica de presas inteiras",
            "O estômago secreta ácido clorídrico e enzimas proteolíticas extremamente potentes. O intestino delgado completa a digestão. O intestino grosso é curto. O fígado e pâncreas secretam bile e enzimas. A digestão de uma presa grande pode levar 3-7 dias.",
            "O pH estomacal da Cascavel atinge 1,0 -- suficiente para dissolver ossos e dentes. Durante a digestão, o metabolismo da cobra aumenta até 10 vezes."
        ),
        new StructureInfo(
            "Fígado",
            "Metabolismo, armazenamento de energia e desintoxicação",
            "O fígado da Cascavel é alongado e multilobado, situado atrás do coração. Armazena glicogênio e lipídios, produz bile para digestão de gorduras, e desintoxica o sangue. É crucial para o jejum prolongado -- armazena energia para meses sem alimentação.",
            "O fígado da Cascavel pode armazenar até 30% do peso corporal em lipídios e glicogênio, permitindo sobrevivência por mais de um ano sem se alimentar."
        ),
        new StructureInfo(
            "Rins",
            "Excreção de resíduos nitrogenados e osmorregulação",
            "Os rins são alongados e lobulados, localizados na porção posterior do corpo. Diferente dos mamíferos, os répteis excretam ácido úrico (não ureia), economizando água. O ácido úrico é eliminado como pasta branca junto com as fezes.",
            "A Cascavel pode concentrar a urina a níveis extremos, perdendo menos de 1% do peso corporal em água por dia -- adaptação essencial para habitats secos e semiáridos."
        ),
        new StructureInfo(
            "Intestinos",
            "Absorção de nutrientes e formação de fezes",
            "O intestino delgado é relativamente curto para um carnívoro (digestão química potente compensa). O intestino grosso (cólon) termina na cloaca. A motilidade intestinal é lenta -- uma refeição pode levar 5-10 dias para percorrer todo o trato digestório.",
            "A Cascavel não defeca com frequência -- após digerir uma presa grande, pode passar semanas ou meses sem evacuar, maximizando a absorção de nutrientes."
        ),
        new StructureInfo(
            "Sistema Reprodutor",
            "Reprodução sexuada com ovoviviparidade",
            "A Cascavel é ovovivípara -- os ovos se desenvolvem dentro do corpo da fêmea e os filhotes nascem vivos. Machos possuem hemipênis (órgão copulador bifurcado) armazenado na base da cauda. A gestação dura 4-6 meses, com ninhadas de 5-20 filhotes.",
            "Filhotes de Cascavel nascem com presas funcionais e veneno ativo -- são independentes desde o nascimento. O veneno dos filhotes é mais potente que o dos adultos (menor quantidade, maior toxicidade)."
        ),
        new StructureInfo(
            "Coluna Vertebral",
            "Eixo esquelético axial, flexível e segmentado",
            "A coluna vertebral da Cascavel tem 150-300 vértebras procélicas (encaixe côncavo-convexo), permitindo flexibilidade extrema em todas as direções. Cada vértebra tem zigapófises adicionais para estabilidade lateral. A coluna termina no pigóstilo (fusão de vértebras caudais) do chocalho.",
            "Uma Cascavel pode curvar o corpo em até 180 graus em menos de 0,5 segundos. As articulações entre vértebras permitem curvatura de 20-30 graus entre cada segmento."
        ),
        new StructureInfo(
            "Costelas",
            "Proteção visceral e suporte à locomoção",
            "Cada vértebra (exceto as caudais) possui um par de costelas articuladas. As costelas são livres (sem externo), conectadas por músculos intercostais. Movimentos das costelas auxiliam a respiração e a locomoção. As costelas mais longas ficam no terço médio do corpo.",
            "Ao engolir presas grandes, as costelas da Cascavel se abrem lateralmente como um acordeão, expandindo o diâmetro corporal em até 3 vezes sem fraturar."
        ),
        new StructureInfo(
            "Cauda e Chocalho",
            "Sinalização acústica de defesa",
            "O chocalho (ou guizo) é composto por segmentos de queratina oca entrelaçados na ponta da cauda. O músculo do chocalho vibra a cauda 50-60 vezes por segundo. O som é produzido pelo atrito entre os segmentos. Cada vibração produz um som distinto -- um zumbido característico.",
            "O chocalho incha antes da troca de pele -- isso cria uma câmara de ressonância maior. Uma Cascavel irritada pode manter o chocalho vibrando por até 30 minutos contínuos."
        )
    };

    public TourOverlayManager(View panel, TextView name, TextView function, TextView howItWorks, TextView curiosity) {
        this.overlayPanel = panel;
        this.nameView = name;
        this.functionView = function;
        this.howItWorksView = howItWorks;
        this.curiosityView = curiosity;
        this.visible = false;
    }

    public void showStructure(int index) {
        if (index < 0 || index >= STRUCTURES.length) return;
        handler.removeCallbacks(hideRunnable);
        StructureInfo s = STRUCTURES[index];
        nameView.setText(s.name);
        functionView.setText(s.function);
        howItWorksView.setText(s.howItWorks);
        curiosityView.setText(s.curiosity);
        animateIn();
        handler.postDelayed(hideRunnable, DISPLAY_DURATION_MS);
    }

    public void hide() {
        handler.removeCallbacks(hideRunnable);
        animateOut();
    }

    private void animateIn() {
        if (visible) return;
        visible = true;
        overlayPanel.setAlpha(0f);
        overlayPanel.setTranslationY(40f);
        overlayPanel.setVisibility(View.VISIBLE);
        overlayPanel.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(FADE_DURATION_MS)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animateOut() {
        if (!visible) return;
        visible = false;
        overlayPanel.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(HIDE_DURATION_MS)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> overlayPanel.setVisibility(View.GONE))
            .start();
    }

    public boolean isVisible() {
        return visible;
    }
}

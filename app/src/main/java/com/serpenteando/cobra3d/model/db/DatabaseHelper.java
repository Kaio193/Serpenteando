package com.serpenteando.cobra3d.model.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.serpenteando.cobra3d.model.db.Contract.*;

/**
 * Gerencia o banco de dados SQLite do app (singleton).
 *
 * [TEI Persistência] Mesmo conceito do slide 06:
 * - SQLiteOpenHelper (cria/atualiza tabelas)
 * - onCreate() cria as tabelas
 * - onUpgrade() migra dados entre versões
 *
 * Diferença do slide: o slide cria 1-2 tabelas simples;
 * o Serpenteando tem ~8 tabelas relacionadas (perguntas, missões,
 * mitos, conteúdo, anatomia, progresso) com chave estrangeira.
 *
 * Padrão singleton: getInstance() garante 1 única instância do banco
 * — melhor prática pra não ter conflito de acesso.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;  // Singleton
    private final Context context;

    // Construtor privado — só pode ser chamado via getInstance()
    private DatabaseHelper(Context context) {
        super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Retorna a instância única do banco.
     * [TEI Persistência] getWritableDatabase() / getReadableDatabase()
     * — mesmo padrão do slide 06, mas encapsulado no singleton.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Versão do CONTEÚDO (os JSONs em assets/content).
     *
     * Suba este número SEMPRE que mexer em algum JSON de conteúdo
     * (missoes.json, dicas.json, serpentes.json, mitos.json, perguntas.json...).
     *
     * Por que isso existe: o banco só é populado na primeira instalação
     * (ou quando o usuário troca de idioma). Sem essa checagem, quem já tem
     * o app instalado continuaria vendo o conteúdo ANTIGO depois de atualizar —
     * foi o que aconteceu com a missão do 3D e com as dicas de primeiros socorros.
     */
    public static final int CONTENT_VERSION = 2;
    private static final String PREF_CONTENT_VERSION = "content_version";

    /**
     * Confere se o conteúdo do banco está na versão atual.
     * Se estiver desatualizado, dropa só as tabelas de conteúdo e repopula
     * a partir dos JSONs (o progresso do usuário é preservado).
     *
     * Chamado no onCreate() do Application, antes de qualquer tela abrir o banco.
     */
    public static void ensureContentVersion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "serpenteando_prefs", Context.MODE_PRIVATE);
        int saved = prefs.getInt(PREF_CONTENT_VERSION, 0);

        if (saved < CONTENT_VERSION) {
            try {
                // Conteúdo mudou: repovoa as tabelas de conteúdo (o progresso fica)
                resetForLanguageChange(context);
                prefs.edit().putInt(PREF_CONTENT_VERSION, CONTENT_VERSION).apply();
            } catch (Exception ignored) {
                // Se falhar tenta de novo no próximo boot — não pode derrubar o app
            }
        }

        // Missão nova entra aqui e só ela: assim quem já tem o app ganha a missão
        // nova sem perder o "concluída" das antigas
        try {
            DatabaseInitializer.ensureNewMissions(context, getInstance(context).getWritableDatabase());
        } catch (Exception ignored) {
        }
    }

    /**
     * Reseta o banco quando o idioma muda.
     * IMPORTANTE: dropa só as tabelas de conteúdo (perguntas, missões, etc.)
     * e NÃO toca em user_progress (pontuação, níveis, streak).
     * Isso garante que o progresso do usuário sobrevive à troca de idioma.
     */
    public static synchronized void resetForLanguageChange(Context context) {
        // Fecha instância anterior
        if (instance != null) {
            instance.close();
            instance = null;
        }
        // Abre DB existente (mantém dados do usuário)
        DatabaseHelper helper = new DatabaseHelper(context.getApplicationContext());
        SQLiteDatabase bancoDados = helper.getWritableDatabase();

        // Só dropa tabelas de conteúdo — user_progress fica intacto
        String[] contentTables = {
                Contract.QuestionsTable.TABLE_NAME,
                Contract.TipsTable.TABLE_NAME,
                Contract.MissionsTable.TABLE_NAME,
                Contract.MissionItemsTable.TABLE_NAME,
                Contract.MythsTruthsTable.TABLE_NAME,
                Contract.ContentTable.TABLE_NAME,
                Contract.AnatomyLayersTable.TABLE_NAME,
                Contract.AnatomyHotspotsTable.TABLE_NAME
        };
        for (String table : contentTables) {
            try { bancoDados.execSQL("DROP TABLE IF EXISTS " + table); } catch (Exception ignored) {}
        }

        // Recria tabelas de conteúdo e popula com JSON do idioma novo
        helper.onCreate(bancoDados);

        // Fecha e limpa singleton pra reabrir limpo
        helper.close();
        instance = null;
    }

    /**
     * Habilita foreign keys — sem isso, ON DELETE CASCADE não funciona.
     * Exemplo: deletar uma missão deleta automaticamente seus itens.
     */
    @Override
    public void onConfigure(SQLiteDatabase bancoDados) {
        super.onConfigure(bancoDados);
        bancoDados.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Fail-safe: se as missões estiverem vazias (migração antiga), resemeia.
     * Isso evita que usuários que migraram de versões antigas fiquem sem missões.
     */
    @Override
    public void onOpen(SQLiteDatabase bancoDados) {
        super.onOpen(bancoDados);
        try {
            int count = 0;
            android.database.Cursor cursor = bancoDados.rawQuery(
                    "SELECT COUNT(*) FROM " + MissionsTable.TABLE_NAME, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) count = cursor.getInt(0);
                cursor.close();
            }
            if (count == 0) {
                DatabaseInitializer.reseedMissions(context, bancoDados);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Cria todas as tabelas do banco na primeira instalação.
     * [TEI Persistência] Mesma lógica do slide 06:
     * CREATE TABLE + colunas + tipos + chave primária.
     *
     * Tabelas:
     * - questions: perguntas do quiz (com dificuldade e explicação)
     * - tips: dicas do dia
     * - missions: missões gamificadas
     * - mission_items: itens dentro de cada missão (com foreign key)
     * - myths_truths: mitos e verdades sobre serpentes
     * - content: artigos sobre espécies
     * - anatomy_layers: camadas da anatomia 3D
     * - anatomy_hotspots: pontos interativos no modelo 3D
     * - user_progress: progresso do usuário (pontos, streak, níveis)
     */
    @Override
    public void onCreate(SQLiteDatabase bancoDados) {
        // [TEI Persistência] CREATE TABLE — mesmo SQL do slide 06
        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + QuestionsTable.TABLE_NAME + " (" +
                QuestionsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COL_QUESTION_TEXT + " TEXT NOT NULL, " +
                QuestionsTable.COL_OPTION_A + " TEXT NOT NULL, " +
                QuestionsTable.COL_OPTION_B + " TEXT NOT NULL, " +
                QuestionsTable.COL_OPTION_C + " TEXT NOT NULL, " +
                QuestionsTable.COL_OPTION_D + " TEXT NOT NULL, " +
                QuestionsTable.COL_CORRECT_ANSWER + " INTEGER NOT NULL, " +
                QuestionsTable.COL_DIFFICULTY + " INTEGER NOT NULL DEFAULT 1, " +
                QuestionsTable.COL_CATEGORY + " TEXT NOT NULL, " +
                QuestionsTable.COL_EXPLANATION + " TEXT, " +
                QuestionsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + TipsTable.TABLE_NAME + " (" +
                TipsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TipsTable.COL_TIP_TEXT + " TEXT NOT NULL, " +
                TipsTable.COL_CATEGORY + " TEXT, " +
                TipsTable.COL_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                TipsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + MissionsTable.TABLE_NAME + " (" +
                MissionsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MissionsTable.COL_TITLE + " TEXT NOT NULL, " +
                MissionsTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                MissionsTable.COL_DIFFICULTY + " INTEGER NOT NULL DEFAULT 1, " +
                MissionsTable.COL_POINTS + " INTEGER NOT NULL DEFAULT 10, " +
                MissionsTable.COL_TOTAL_DAYS + " INTEGER NOT NULL DEFAULT 0, " +
                MissionsTable.COL_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                MissionsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + MissionItemsTable.TABLE_NAME + " (" +
                MissionItemsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MissionItemsTable.COL_MISSION_ID + " INTEGER NOT NULL, " +
                MissionItemsTable.COL_ITEM_TEXT + " TEXT NOT NULL, " +
                MissionItemsTable.COL_TRIGGER + " TEXT NOT NULL DEFAULT '', " +
                MissionItemsTable.COL_TARGET + " INTEGER NOT NULL DEFAULT 1, " +
                MissionItemsTable.COL_IS_CORRECT + " INTEGER NOT NULL DEFAULT 0, " +
                MissionItemsTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + MissionItemsTable.COL_MISSION_ID + ") REFERENCES " +
                MissionsTable.TABLE_NAME + "(" + MissionsTable.COL_ID + ") ON DELETE CASCADE)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + MythsTruthsTable.TABLE_NAME + " (" +
                MythsTruthsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MythsTruthsTable.COL_STATEMENT + " TEXT NOT NULL, " +
                MythsTruthsTable.COL_IS_MYTH + " INTEGER NOT NULL, " +
                MythsTruthsTable.COL_EXPLANATION + " TEXT NOT NULL, " +
                MythsTruthsTable.COL_CATEGORY + " TEXT, " +
                MythsTruthsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + ContentTable.TABLE_NAME + " (" +
                ContentTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContentTable.COL_CATEGORY + " TEXT NOT NULL, " +
                ContentTable.COL_TITLE + " TEXT NOT NULL, " +
                ContentTable.COL_SCIENTIFIC_NAME + " TEXT, " +
                ContentTable.COL_FAMILY + " TEXT, " +
                ContentTable.COL_IS_VENOMOUS + " INTEGER DEFAULT 0, " +
                ContentTable.COL_BODY + " TEXT NOT NULL, " +
                ContentTable.COL_HABITAT + " TEXT, " +
                ContentTable.COL_DISTRIBUTION + " TEXT, " +
                ContentTable.COL_DIET + " TEXT, " +
                ContentTable.COL_REPRODUCTION + " TEXT, " +
                ContentTable.COL_MEDICAL_IMPORTANCE + " TEXT, " +
                ContentTable.COL_CURIOSITY + " TEXT, " +
                ContentTable.COL_IMAGE_RES_NAME + " TEXT, " +
                ContentTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                ContentTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + AnatomyLayersTable.TABLE_NAME + " (" +
                AnatomyLayersTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AnatomyLayersTable.COL_LAYER_NAME + " TEXT NOT NULL, " +
                AnatomyLayersTable.COL_DISPLAY_NAME + " TEXT NOT NULL, " +
                AnatomyLayersTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                AnatomyLayersTable.COL_MODEL_FILE_NAME + " TEXT, " +
                AnatomyLayersTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + AnatomyHotspotsTable.TABLE_NAME + " (" +
                AnatomyHotspotsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AnatomyHotspotsTable.COL_LAYER_ID + " INTEGER NOT NULL, " +
                AnatomyHotspotsTable.COL_HOTSPOT_NAME + " TEXT NOT NULL, " +
                AnatomyHotspotsTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                AnatomyHotspotsTable.COL_POSITION_X + " REAL NOT NULL, " +
                AnatomyHotspotsTable.COL_POSITION_Y + " REAL NOT NULL, " +
                AnatomyHotspotsTable.COL_POSITION_Z + " REAL NOT NULL, " +
                "FOREIGN KEY (" + AnatomyHotspotsTable.COL_LAYER_ID + ") REFERENCES " +
                AnatomyLayersTable.TABLE_NAME + "(" + AnatomyLayersTable.COL_ID + ") ON DELETE CASCADE)");

        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS " + UserProgressTable.TABLE_NAME + " (" +
                UserProgressTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserProgressTable.COL_QUIZ_HIGH_SCORE + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZ_HIGH_FACIL + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZ_HIGH_MEDIO + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZ_HIGH_DIFICIL + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZZES_COMPLETED + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_MISSIONS_COMPLETED + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_TOTAL_POINTS + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_CURRENT_STREAK + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_LAST_QUIZ_DATE + " TEXT, " +
                UserProgressTable.COL_LAST_TIP_DATE + " TEXT, " +
                UserProgressTable.COL_LAST_MISSION_DATE + " TEXT, " +
                UserProgressTable.COL_QUIZ_FACIL_COUNT + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZ_MEDIO_COUNT + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZ_DIFICIL_COUNT + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_ANATOMY_LAYER_COUNT + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_CONTENT_DETAIL_COUNT + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_AWARDED_MISSIONS + " TEXT DEFAULT '', " +
                UserProgressTable.COL_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        DatabaseInitializer.initialize(context, bancoDados);
    }

    /**
     * Migração do banco quando a versão do app muda.
     * [TEI Persistência] onUpgrade() — mesmo conceito do slide 06,
     * só que mais complexo: adiciona colunas novas sem apagar dados.
     *
     * Migração v4→v5:
     * - Adiciona colunas trigger/target nos mission_items
     * - Adiciona colunas de contagem de quiz no user_progress
     * - Recria tabelas de missão com os novos gatilhos
     */
    @Override
    public void onUpgrade(SQLiteDatabase bancoDados, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            try {
                bancoDados.execSQL("ALTER TABLE " + MissionItemsTable.TABLE_NAME +
                        " ADD COLUMN " + MissionItemsTable.COL_TRIGGER +
                        " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {}
            try {
                bancoDados.execSQL("ALTER TABLE " + MissionItemsTable.TABLE_NAME +
                        " ADD COLUMN " + MissionItemsTable.COL_TARGET +
                        " INTEGER NOT NULL DEFAULT 1");
            } catch (Exception ignored) {}
            for (String col : new String[]{
                    UserProgressTable.COL_QUIZ_FACIL_COUNT,
                    UserProgressTable.COL_QUIZ_MEDIO_COUNT,
                    UserProgressTable.COL_QUIZ_DIFICIL_COUNT,
                    UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT,
                    UserProgressTable.COL_ANATOMY_LAYER_COUNT,
                    UserProgressTable.COL_CONTENT_DETAIL_COUNT
            }) {
                try {
                    bancoDados.execSQL("ALTER TABLE " + UserProgressTable.TABLE_NAME +
                            " ADD COLUMN " + col + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
            }
            try {
                bancoDados.execSQL("ALTER TABLE " + UserProgressTable.TABLE_NAME +
                        " ADD COLUMN " + UserProgressTable.COL_AWARDED_MISSIONS +
                        " TEXT DEFAULT ''");
            } catch (Exception ignored) {}

            // Re-semeia as missões com os novos gatilhos (drop + insert na tabela de missões)
            try {
                bancoDados.execSQL("DROP TABLE IF EXISTS " + MissionItemsTable.TABLE_NAME);
                bancoDados.execSQL("DROP TABLE IF EXISTS " + MissionsTable.TABLE_NAME);
                bancoDados.execSQL("CREATE TABLE " + MissionsTable.TABLE_NAME + " (" +
                        MissionsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MissionsTable.COL_TITLE + " TEXT NOT NULL, " +
                        MissionsTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                        MissionsTable.COL_DIFFICULTY + " INTEGER NOT NULL DEFAULT 1, " +
                        MissionsTable.COL_POINTS + " INTEGER NOT NULL DEFAULT 10, " +
                        MissionsTable.COL_TOTAL_DAYS + " INTEGER NOT NULL DEFAULT 0, " +
                        MissionsTable.COL_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                        MissionsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                bancoDados.execSQL("CREATE TABLE " + MissionItemsTable.TABLE_NAME + " (" +
                        MissionItemsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MissionItemsTable.COL_MISSION_ID + " INTEGER NOT NULL, " +
                        MissionItemsTable.COL_ITEM_TEXT + " TEXT NOT NULL, " +
                        MissionItemsTable.COL_TRIGGER + " TEXT NOT NULL DEFAULT '', " +
                        MissionItemsTable.COL_TARGET + " INTEGER NOT NULL DEFAULT 1, " +
                        MissionItemsTable.COL_IS_CORRECT + " INTEGER NOT NULL DEFAULT 0, " +
                        MissionItemsTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                        "FOREIGN KEY (" + MissionItemsTable.COL_MISSION_ID + ") REFERENCES " +
                        MissionsTable.TABLE_NAME + "(" + MissionsTable.COL_ID + ") ON DELETE CASCADE)");
                DatabaseInitializer.reseedMissions(context, bancoDados);
            } catch (Exception ignored) {}
        }
        if (oldVersion < 6) {
            // Migração v5→v6: adiciona total_days (contador "Dia X de Y" do mockup)
            try {
                bancoDados.execSQL("ALTER TABLE " + MissionsTable.TABLE_NAME +
                        " ADD COLUMN " + MissionsTable.COL_TOTAL_DAYS +
                        " INTEGER NOT NULL DEFAULT 0");
                // Missões existentes: "Ofensiva de 7 dias" tem 7 dias, as outras 0
                bancoDados.execSQL("UPDATE " + MissionsTable.TABLE_NAME +
                        " SET " + MissionsTable.COL_TOTAL_DAYS +
                        " = 7 WHERE " + MissionsTable.COL_ID + " = 1");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 7) {
            // v6→v7: a sequência de dias agora conta as MISSÕES concluídas
            // (antes era o quiz). A coluna nasce vazia, então a sequência
            // recomeça na próxima missão que o usuário concluir.
            try {
                bancoDados.execSQL("ALTER TABLE " + UserProgressTable.TABLE_NAME +
                        " ADD COLUMN " + UserProgressTable.COL_LAST_MISSION_DATE + " TEXT");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 8) {
            // v7→v8: pontuação independente por nível. Cada nível (fácil/médio/difícil)
            // nasce em 0 e cresce sozinho; o recorde geral (quiz_high_score) é o maior
            // dos três e nunca desce, então o card da Home não perde o valor antigo.
            for (String col : new String[]{
                    UserProgressTable.COL_QUIZ_HIGH_FACIL,
                    UserProgressTable.COL_QUIZ_HIGH_MEDIO,
                    UserProgressTable.COL_QUIZ_HIGH_DIFICIL
            }) {
                try {
                    bancoDados.execSQL("ALTER TABLE " + UserProgressTable.TABLE_NAME +
                            " ADD COLUMN " + col + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
            }
        }
    }
}

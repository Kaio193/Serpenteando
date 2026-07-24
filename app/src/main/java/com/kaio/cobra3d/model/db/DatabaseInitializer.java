package com.kaio.cobra3d.model.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseInitializer {

    public static void initialize(SQLiteDatabase db) {
        insertQuestions(db);
        insertTips(db);
        insertMissions(db);
        insertMythsTruths(db);
        insertContent(db);
        insertAnatomyLayers(db);
        insertUserProgress(db);
    }

    private static void insertQuestions(SQLiteDatabase db) {
        String[] questions = {
            "Qual órgão é responsável pela respiração da cobra?",
            "Qual a principal característica física das cobras?",
            "As cobras possuem patas visíveis?",
            "Qual tipo de cobra é a jararaca?",
            "Qual é o hábitat mais comum das cobras?",
            "As cobras são répteis ou anfíbios?",
            "Qual é a função das escamas de uma cobra?",
            "Como as cobras se movimentam?",
            "Qual cobra é conhecida como não peçonhenta?",
            "O que as cobras comem?"
        };

        String[][] options = {
            {"Pulmão", "Coração", "Fígado", "Rim"},
            {"Patas", "Corpo alongado sem patas", "Asas", "Barbatanas"},
            {"Sim", "Não", "Apenas em filhotes", "Apenas em machos"},
            {"Peçonhenta", "Não peçonhenta", "Anfíbia", "Aquática"},
            {"Floresta", "Deserto", "Oceano", "Montanha"},
            {"Répteis", "Anfíbios", "Mamíferos", "Aves"},
            {"Proteção e locomoção", "Digestão", "Respiração", "Visão"},
            {"Andando", "Rastejando", "Nadando apenas", "Voando"},
            {"Jiboia", "Cascavel", "Jararaca", "Cobra-coral"},
            {"Insetos", "Plantas", "Pequenos mamíferos", "Frutas"}
        };

        int[] correct = {0, 1, 1, 0, 0, 0, 0, 1, 0, 2};
        int[] difficulty = {1, 1, 1, 2, 1, 1, 1, 1, 1, 1};
        String category = "basico";

        for (int i = 0; i < questions.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(Contract.QuestionsTable.COL_QUESTION_TEXT, questions[i]);
            cv.put(Contract.QuestionsTable.COL_OPTION_A, options[i][0]);
            cv.put(Contract.QuestionsTable.COL_OPTION_B, options[i][1]);
            cv.put(Contract.QuestionsTable.COL_OPTION_C, options[i][2]);
            cv.put(Contract.QuestionsTable.COL_OPTION_D, options[i][3]);
            cv.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, correct[i]);
            cv.put(Contract.QuestionsTable.COL_DIFFICULTY, difficulty[i]);
            cv.put(Contract.QuestionsTable.COL_CATEGORY, category);
            cv.put(Contract.QuestionsTable.COL_EXPLANATION, "Explicação da questão " + (i + 1));
            db.insert(Contract.QuestionsTable.TABLE_NAME, null, cv);
        }
    }

    private static void insertTips(SQLiteDatabase db) {
        String[] tips = {
            "Dica: Cobras dormem com os olhos abertos!",
            "Dica: A cobra perde sua pele várias vezes ao ano.",
            "Dica: Nem todas as cobras são peçonhentas.",
            "Dica: Cobras comem apenas carnívoro.",
            "Dica: A cascavel avisa antes de atacar."
        };

        for (String tip : tips) {
            ContentValues cv = new ContentValues();
            cv.put(Contract.TipsTable.COL_TIP_TEXT, tip);
            cv.put(Contract.TipsTable.COL_CATEGORY, "geral");
            cv.put(Contract.TipsTable.COL_IS_ACTIVE, 1);
            db.insert(Contract.TipsTable.TABLE_NAME, null, cv);
        }
    }

    private static void insertMissions(SQLiteDatabase db) {
        ContentValues m1 = new ContentValues();
        m1.put(Contract.MissionsTable.COL_TITLE, "Aprenda sobre Cobras");
        m1.put(Contract.MissionsTable.COL_DESCRIPTION, "Marque as afirmações corretas sobre cobras");
        m1.put(Contract.MissionsTable.COL_DIFFICULTY, 1);
        m1.put(Contract.MissionsTable.COL_POINTS, 10);
        long m1Id = db.insert(Contract.MissionsTable.TABLE_NAME, null, m1);

        insertMissionItem(db, m1Id, "Cobras são répteis", true, 1);
        insertMissionItem(db, m1Id, "Cobras têm patas", false, 2);
        insertMissionItem(db, m1Id, "Cobras respiram", true, 3);
        insertMissionItem(db, m1Id, "Cobras voa", false, 4);
        insertMissionItem(db, m1Id, "Cobras são carnívoras", true, 5);
    }

    private static void insertMissionItem(SQLiteDatabase db, long missionId,
                                          String text, boolean isCorrect, int order) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.MissionItemsTable.COL_MISSION_ID, missionId);
        cv.put(Contract.MissionItemsTable.COL_ITEM_TEXT, text);
        cv.put(Contract.MissionItemsTable.COL_IS_CORRECT, isCorrect ? 1 : 0);
        cv.put(Contract.MissionItemsTable.COL_DISPLAY_ORDER, order);
        db.insert(Contract.MissionItemsTable.TABLE_NAME, null, cv);
    }

    private static void insertMythsTruths(SQLiteDatabase db) {
        String[][] data = {
            {"Toda cobra é venenosa", "Falso! Existem cobras peçonhentas e não peçonhentas.", "cobras", "1"},
            {"Cobras perseguem pessoas", "Falso! Na maioria das vezes, cobras tentam fugir.", "cobras", "1"},
            {"Cobras são répteis", "Verdade! Cobras pertencem à classe Reptilia.", "cobras", "0"},
            {"Cobras mudam de pele", "Verdade! Cobras mudam de pele várias vezes ao ano.", "anatom", "0"},
            {"Existem cobras peçonhentas e não peçonhentas", "Verdade! Apenas algumas espécies possuem veneno.", "cobras", "0"}
        };

        for (String[] row : data) {
            ContentValues cv = new ContentValues();
            cv.put(Contract.MythsTruthsTable.COL_STATEMENT, row[0]);
            cv.put(Contract.MythsTruthsTable.COL_IS_MYTH, Integer.parseInt(row[3]));
            cv.put(Contract.MythsTruthsTable.COL_EXPLANATION, row[1]);
            cv.put(Contract.MythsTruthsTable.COL_CATEGORY, row[2]);
            db.insert(Contract.MythsTruthsTable.TABLE_NAME, null, cv);
        }
    }

    private static void insertContent(SQLiteDatabase db) {
        insertContentItem(db, "cobras", "Cascavel", "Cobra peçonhenta com guizo na cauda.", 1);
        insertContentItem(db, "cobras", "Jararaca", "Uma das cobras mais peçonhentas do Brasil.", 2);
        insertContentItem(db, "cobras", "Jiboia", "Cobra dócil que mata por constrição.", 3);
        insertContentItem(db, "cobras", "Cobra-coral", "Cobra muito venenosa do Brasil.", 4);
        insertContentItem(db, "cobras", "Papa-ovo", "Cobra que se alimenta apenas de ovos.", 5);
    }

    private static void insertContentItem(SQLiteDatabase db, String category, String title,
                                          String body, int order) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.ContentTable.COL_CATEGORY, category);
        cv.put(Contract.ContentTable.COL_TITLE, title);
        cv.put(Contract.ContentTable.COL_BODY, body);
        cv.put(Contract.ContentTable.COL_DISPLAY_ORDER, order);
        db.insert(Contract.ContentTable.TABLE_NAME, null, cv);
    }

    private static void insertAnatomyLayers(SQLiteDatabase db) {
        ContentValues pele = new ContentValues();
        pele.put(Contract.AnatomyLayersTable.COL_LAYER_NAME, "pele");
        pele.put(Contract.AnatomyLayersTable.COL_DISPLAY_NAME, "Pele e Escamas");
        pele.put(Contract.AnatomyLayersTable.COL_DESCRIPTION, "A pele protege o corpo da cobra. As escamas ajudam na locomoção.");
        pele.put(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER, 1);
        long peleId = db.insert(Contract.AnatomyLayersTable.TABLE_NAME, null, pele);

        ContentValues orgaos = new ContentValues();
        orgaos.put(Contract.AnatomyLayersTable.COL_LAYER_NAME, "orgaos");
        orgaos.put(Contract.AnatomyLayersTable.COL_DISPLAY_NAME, "Órgãos Internos");
        orgaos.put(Contract.AnatomyLayersTable.COL_DESCRIPTION, "Pulmão, coração, fígado, estômago, intestino e rins.");
        orgaos.put(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER, 2);
        long orgaosId = db.insert(Contract.AnatomyLayersTable.TABLE_NAME, null, orgaos);

        insertHotspot(db, peleId, "Escamas", "Escamas que cobrem o corpo", 0, 0, 0);
        insertHotspot(db, orgaosId, "Pulmão", "Órgão respiratório principal", 0.3f, 0.3f, 0f);
        insertHotspot(db, orgaosId, "Coração", "Órgão que bombeia sangue", 0.5f, 0.5f, 0f);
    }

    private static void insertHotspot(SQLiteDatabase db, long layerId, String name,
                                      String description, float x, float y, float z) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.AnatomyHotspotsTable.COL_LAYER_ID, layerId);
        cv.put(Contract.AnatomyHotspotsTable.COL_HOTSPOT_NAME, name);
        cv.put(Contract.AnatomyHotspotsTable.COL_DESCRIPTION, description);
        cv.put(Contract.AnatomyHotspotsTable.COL_POSITION_X, x);
        cv.put(Contract.AnatomyHotspotsTable.COL_POSITION_Y, y);
        cv.put(Contract.AnatomyHotspotsTable.COL_POSITION_Z, z);
        db.insert(Contract.AnatomyHotspotsTable.TABLE_NAME, null, cv);
    }

    private static void insertUserProgress(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        db.insert(Contract.UserProgressTable.TABLE_NAME, null, cv);
    }
}

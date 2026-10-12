package com.serpenteando.cobra3d.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import com.serpenteando.cobra3d.util.LocaleHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Popula o banco de dados com conteúdo inicial (seeds).
 *
 * [TEI Persistência] Lê JSON de assets/content/ e insere no SQLite.
 * Isso é diferente do slide 06 (que cria dados na mão):
 * aqui os dados vêm de arquivos JSON, não de INSERT direto.
 *
 * Cada método insertXxx() escolhe o JSON correto baseado no idioma:
 * - Português: perguntas.json
 * - English: perguntas_en.json
 * - Español: perguntas_es.json
 *
 * Isso é a internacionalização (i18n) — o app mostra conteúdo
 * traduzido sem precisar mudar o código.
 */
public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";

    /**
     * Inicializa todas as tabelas de conteúdo.
     * Chamado pelo DatabaseHelper.onCreate() na primeira instalação.
     */
    public static void initialize(Context context, SQLiteDatabase db) {
        insertQuestions(context, db);   // Perguntas do quiz
        insertTips(context, db);        // Dicas do dia
        insertMissions(context, db);    // Missões gamificadas
        insertMythsTruths(context, db); // Mitos e verdades
        insertContent(context, db);     // Artigos sobre serpentes
        insertAnatomyLayers(context, db); // Camadas da anatomia 3D
        insertUserProgress(db);         // Linha de progresso do usuário
    }

    /**
     * Re-semeia só as missões (usado na migração v4→v5).
     * Não mexe em outros dados — só repopula a tabela de missões.
     */
    public static void reseedMissions(Context context, SQLiteDatabase db) {
        insertMissions(context, db);
    }

    /**
     * Lê um arquivo JSON de assets/content/ e retorna como JSONArray.
     * UTF-8 garante que acentos funcionem (ã, é, ü, etc.).
     */
    private static JSONArray readJson(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open("content/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return new JSONArray(sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "Falha ao ler " + fileName, e);
            return null;
        }
    }

    /**
     * Converte texto de dificuldade do JSON pra número.
     * "fácil"/"facil" → 1, "médio"/"medio" → 2, "difícil"/"dificil" → 3
     */
    private static int difficultyToInt(String difficulty) {
        if (difficulty == null) return 1;
        switch (difficulty) {
            case "medio":
            case "media":
                return 2;
            case "dificil":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * Insere perguntas do quiz no banco.
     * Escolhe o JSON correto baseado no idioma (PT/EN/ES).
     */
    private static void insertQuestions(Context context, SQLiteDatabase db) {
        // [TEI i18n] Escolhe JSON baseado no idioma do dispositivo
        String lang = LocaleHelper.getLanguage(context);
        String fileName = "perguntas.json";
        if ("en".equals(lang)) {
            fileName = "perguntas_en.json";
        } else if ("es".equals(lang)) {
            fileName = "perguntas_es.json";
        }
        JSONArray arr = readJson(context, fileName);
        if (arr == null) return;
        int inserted = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject q = arr.getJSONObject(i);
                JSONArray options = q.getJSONArray("options");
                if (options.length() < 4) continue;
                ContentValues cv = new ContentValues();
                cv.put(Contract.QuestionsTable.COL_QUESTION_TEXT, q.getString("question"));
                cv.put(Contract.QuestionsTable.COL_OPTION_A, options.getString(0));
                cv.put(Contract.QuestionsTable.COL_OPTION_B, options.getString(1));
                cv.put(Contract.QuestionsTable.COL_OPTION_C, options.getString(2));
                cv.put(Contract.QuestionsTable.COL_OPTION_D, options.getString(3));
                cv.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, q.getInt("correctIndex"));
                cv.put(Contract.QuestionsTable.COL_DIFFICULTY, difficultyToInt(q.optString("difficulty")));
                cv.put(Contract.QuestionsTable.COL_CATEGORY, "geral");
                cv.put(Contract.QuestionsTable.COL_EXPLANATION, q.optString("explanation"));
                db.insert(Contract.QuestionsTable.TABLE_NAME, null, cv);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na pergunta " + i, e);
            }
        }
        Log.d(TAG, "Perguntas inseridas: " + inserted);
    }

    private static void insertTips(Context context, SQLiteDatabase db) {
        String lang = LocaleHelper.getLanguage(context);
        String fileName = "dicas.json";
        if ("en".equals(lang)) {
            fileName = "dicas_en.json";
        } else if ("es".equals(lang)) {
            fileName = "dicas_es.json";
        }
        JSONArray arr = readJson(context, fileName);
        if (arr == null) return;
        int inserted = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject t = arr.getJSONObject(i);
                ContentValues cv = new ContentValues();
                cv.put(Contract.TipsTable.COL_TIP_TEXT, t.getString("text"));
                cv.put(Contract.TipsTable.COL_CATEGORY, "geral");
                cv.put(Contract.TipsTable.COL_IS_ACTIVE, 1);
                db.insert(Contract.TipsTable.TABLE_NAME, null, cv);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na dica " + i, e);
            }
        }
        Log.d(TAG, "Dicas inseridas: " + inserted);
    }

    private static void insertMissions(Context context, SQLiteDatabase db) {
        String lang = LocaleHelper.getLanguage(context);
        String fileName = "missoes.json";
        if ("en".equals(lang)) {
            fileName = "missoes_en.json";
        } else if ("es".equals(lang)) {
            fileName = "missoes_es.json";
        }
        JSONArray arr = readJson(context, fileName);
        if (arr == null) return;
        int inserted = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject m = arr.getJSONObject(i);
                ContentValues cv = new ContentValues();
                cv.put(Contract.MissionsTable.COL_TITLE, m.getString("title"));
                cv.put(Contract.MissionsTable.COL_DESCRIPTION, m.getString("description"));
                cv.put(Contract.MissionsTable.COL_DIFFICULTY, 1);
                cv.put(Contract.MissionsTable.COL_POINTS, m.optInt("points", 100));
                cv.put(Contract.MissionsTable.COL_TOTAL_DAYS, m.optInt("totalDays", 0));
                cv.put(Contract.MissionsTable.COL_IS_COMPLETED, 0);
                long missionId = db.insert(Contract.MissionsTable.TABLE_NAME, null, cv);

                JSONArray items = m.optJSONArray("items");
                if (items != null) {
                    for (int j = 0; j < items.length(); j++) {
                        JSONObject item = items.getJSONObject(j);
                        ContentValues icv = new ContentValues();
                        icv.put(Contract.MissionItemsTable.COL_MISSION_ID, missionId);
                        icv.put(Contract.MissionItemsTable.COL_ITEM_TEXT, item.getString("text"));
                        icv.put(Contract.MissionItemsTable.COL_TRIGGER, item.optString("trigger", ""));
                        icv.put(Contract.MissionItemsTable.COL_TARGET, item.optInt("target", 1));
                        icv.put(Contract.MissionItemsTable.COL_IS_CORRECT, 0);
                        icv.put(Contract.MissionItemsTable.COL_DISPLAY_ORDER, j + 1);
                        db.insert(Contract.MissionItemsTable.TABLE_NAME, null, icv);
                    }
                }
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na missao " + i, e);
            }
        }
        Log.d(TAG, "Missoes inseridas: " + inserted);
    }

    private static void insertMythsTruths(Context context, SQLiteDatabase db) {
        String lang = LocaleHelper.getLanguage(context);
        String fileName = "mitos_verdades.json";
        if ("en".equals(lang)) {
            fileName = "mitos_verdades_en.json";
        } else if ("es".equals(lang)) {
            fileName = "mitos_verdades_es.json";
        }
        JSONArray arr = readJson(context, fileName);
        if (arr == null) return;
        int inserted = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject mv = arr.getJSONObject(i);
                ContentValues cv = new ContentValues();
                cv.put(Contract.MythsTruthsTable.COL_STATEMENT, mv.getString("phrase"));
                cv.put(Contract.MythsTruthsTable.COL_IS_MYTH,
                        "MITO".equals(mv.optString("type")) || "MYTH".equals(mv.optString("type")) ? 1 : 0);
                cv.put(Contract.MythsTruthsTable.COL_EXPLANATION, mv.getString("explanation"));
                cv.put(Contract.MythsTruthsTable.COL_CATEGORY, "cobras");
                db.insert(Contract.MythsTruthsTable.TABLE_NAME, null, cv);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro no mito " + i, e);
            }
        }
        Log.d(TAG, "Mitos/verdades inseridos: " + inserted);
    }

    private static void insertContent(Context context, SQLiteDatabase db) {
        String lang = LocaleHelper.getLanguage(context);
        String fileName = "serpentes.json";
        if ("en".equals(lang)) {
            fileName = "serpentes_en.json";
        } else if ("es".equals(lang)) {
            fileName = "serpentes_es.json";
        }
        JSONArray arr = readJson(context, fileName);
        if (arr == null) return;
        int inserted = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject s = arr.getJSONObject(i);
                ContentValues cv = new ContentValues();
                cv.put(Contract.ContentTable.COL_CATEGORY, "serpentes");
                cv.put(Contract.ContentTable.COL_TITLE, s.getString("popularName"));
                cv.put(Contract.ContentTable.COL_SCIENTIFIC_NAME, s.optString("scientificName"));
                cv.put(Contract.ContentTable.COL_FAMILY, s.optString("family"));
                cv.put(Contract.ContentTable.COL_IS_VENOMOUS, s.optBoolean("venomous") ? 1 : 0);
                cv.put(Contract.ContentTable.COL_BODY, s.optString("description"));
                cv.put(Contract.ContentTable.COL_HABITAT, s.optString("habitat"));
                cv.put(Contract.ContentTable.COL_DISTRIBUTION, s.optString("distribution"));
                cv.put(Contract.ContentTable.COL_DIET, s.optString("diet"));
                cv.put(Contract.ContentTable.COL_REPRODUCTION, s.optString("reproduction"));
                cv.put(Contract.ContentTable.COL_MEDICAL_IMPORTANCE, s.optString("medicalImportance"));
                cv.put(Contract.ContentTable.COL_CURIOSITY, s.optString("curiosity"));
                cv.put(Contract.ContentTable.COL_IMAGE_RES_NAME, s.optString("imageName"));
                cv.put(Contract.ContentTable.COL_DISPLAY_ORDER, i + 1);
                db.insert(Contract.ContentTable.TABLE_NAME, null, cv);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na serpente " + i, e);
            }
        }
        Log.d(TAG, "Conteudos inseridos: " + inserted);
    }

    /**
     * Insere as camadas da anatomia 3D no banco.
     * [3D INOVADOR] Essa parte NÃO existe nos slides do prof. Alexandre —
     * é inovação do TCC. O professor vai perguntar como funciona!
     *
     * O modelo 3D tem 2 camadas:
     * 1. Pele e Escamas (proteção externa)
     * 2. Órgãos Internos (pulmão, coração, fígado)
     *
     * Cada camada tem hotspots (pontos interativos) que o usuário
     * pode tocar pra ver informações sobre cada estrutura.
     */
    private static void insertAnatomyLayers(Context context, SQLiteDatabase db) {
        // Escolhe idioma pros nomes das estruturas
        String lang = LocaleHelper.getLanguage(context);

        String peleDisplayName, peleDescription, orgaosDisplayName, orgaosDescription;
        String hsEscamasName, hsEscamasDesc, hsPulmaoName, hsPulmaoDesc, hsCoracaoName, hsCoracaoDesc;

        if ("en".equals(lang)) {
            peleDisplayName = "Skin and Scales";
            peleDescription = "The skin protects the snake's body. Scales aid in locomotion.";
            orgaosDisplayName = "Internal Organs";
            orgaosDescription = "Lungs, heart, liver, stomach, intestines and kidneys.";
            hsEscamasName = "Scales"; hsEscamasDesc = "Scales that cover the body";
            hsPulmaoName = "Lung"; hsPulmaoDesc = "Main respiratory organ";
            hsCoracaoName = "Heart"; hsCoracaoDesc = "Organ that pumps blood";
        } else if ("es".equals(lang)) {
            peleDisplayName = "Piel y Escamas";
            peleDescription = "La piel protege el cuerpo de la serpiente. Las escamas ayudan en la locomoción.";
            orgaosDisplayName = "Órganos Internos";
            orgaosDescription = "Pulmones, corazón, hígado, estómago, intestinos y riñones.";
            hsEscamasName = "Escamas"; hsEscamasDesc = "Escamas que cubren el cuerpo";
            hsPulmaoName = "Pulmón"; hsPulmaoDesc = "Órgano respiratorio principal";
            hsCoracaoName = "Corazón"; hsCoracaoDesc = "Órgano que bombea sangre";
        } else {
            peleDisplayName = "Pele e Escamas";
            peleDescription = "A pele protege o corpo da cobra. As escamas ajudam na locomoção.";
            orgaosDisplayName = "Órgãos Internos";
            orgaosDescription = "Pulmão, coração, fígado, estômago, intestino e rins.";
            hsEscamasName = "Escamas"; hsEscamasDesc = "Escamas que cobrem o corpo";
            hsPulmaoName = "Pulmão"; hsPulmaoDesc = "Órgão respiratório principal";
            hsCoracaoName = "Coração"; hsCoracaoDesc = "Órgão que bombeia sangue";
        }

        ContentValues pele = new ContentValues();
        pele.put(Contract.AnatomyLayersTable.COL_LAYER_NAME, "pele");
        pele.put(Contract.AnatomyLayersTable.COL_DISPLAY_NAME, peleDisplayName);
        pele.put(Contract.AnatomyLayersTable.COL_DESCRIPTION, peleDescription);
        pele.put(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER, 1);
        long peleId = db.insert(Contract.AnatomyLayersTable.TABLE_NAME, null, pele);

        ContentValues orgaos = new ContentValues();
        orgaos.put(Contract.AnatomyLayersTable.COL_LAYER_NAME, "orgaos");
        orgaos.put(Contract.AnatomyLayersTable.COL_DISPLAY_NAME, orgaosDisplayName);
        orgaos.put(Contract.AnatomyLayersTable.COL_DESCRIPTION, orgaosDescription);
        orgaos.put(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER, 2);
        long orgaosId = db.insert(Contract.AnatomyLayersTable.TABLE_NAME, null, orgaos);

        insertHotspot(db, peleId, hsEscamasName, hsEscamasDesc, 0, 0, 0);
        insertHotspot(db, orgaosId, hsPulmaoName, hsPulmaoDesc, 0.3f, 0.3f, 0f);
        insertHotspot(db, orgaosId, hsCoracaoName, hsCoracaoDesc, 0.5f, 0.5f, 0f);
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

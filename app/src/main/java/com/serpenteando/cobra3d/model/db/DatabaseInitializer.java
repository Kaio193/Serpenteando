package com.serpenteando.cobra3d.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
    public static void initialize(Context context, SQLiteDatabase bancoDados) {
        insertQuestions(context, bancoDados);   // Perguntas do quiz
        insertTips(context, bancoDados);        // Dicas do dia
        insertMissions(context, bancoDados);    // Missões gamificadas
        insertMythsTruths(context, bancoDados); // Mitos e verdades
        insertContent(context, bancoDados);     // Artigos sobre serpentes
        insertAnatomyLayers(context, bancoDados); // Camadas da anatomia 3D
        insertUserProgress(bancoDados);         // Linha de progresso do usuário
    }

    /**
     * Re-semeia só as missões (usado na migração v4→v5).
     * Não mexe em outros dados — só repopula a tabela de missões.
     */
    public static void reseedMissions(Context context, SQLiteDatabase bancoDados) {
        insertMissions(context, bancoDados);
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
     * PT/ES: "fácil"/"facil" → 1, "médio"/"medio"/"media" → 2, "difícil"/"dificil" → 3
     * EN: "easy" → 1, "medium" → 2, "hard" → 3
     * Sem os termos em inglês, o perguntas_en.json inteiro virava dificuldade 1
     * e o quiz em inglês não achava perguntas em médio/difícil (bug de 12/10).
     */
    private static int difficultyToInt(String difficulty) {
        if (difficulty == null) return 1;
        switch (difficulty) {
            case "medio":
            case "media":
            case "medium":
                return 2;
            case "dificil":
            case "hard":
                return 3;
            case "easy":
            case "facil":
            default:
                return 1;
        }
    }

    /**
     * Insere perguntas do quiz no banco.
     * Escolhe o JSON correto baseado no idioma (PT/EN/ES).
     */
    private static void insertQuestions(Context context, SQLiteDatabase bancoDados) {
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
                ContentValues valores = new ContentValues();
                valores.put(Contract.QuestionsTable.COL_QUESTION_TEXT, q.getString("question"));
                valores.put(Contract.QuestionsTable.COL_OPTION_A, options.getString(0));
                valores.put(Contract.QuestionsTable.COL_OPTION_B, options.getString(1));
                valores.put(Contract.QuestionsTable.COL_OPTION_C, options.getString(2));
                valores.put(Contract.QuestionsTable.COL_OPTION_D, options.getString(3));
                valores.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, q.getInt("correctIndex"));
                valores.put(Contract.QuestionsTable.COL_DIFFICULTY, difficultyToInt(q.optString("difficulty")));
                valores.put(Contract.QuestionsTable.COL_CATEGORY, "geral");
                valores.put(Contract.QuestionsTable.COL_EXPLANATION, q.optString("explanation"));
                bancoDados.insert(Contract.QuestionsTable.TABLE_NAME, null, valores);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na pergunta " + i, e);
            }
        }
        Log.d(TAG, "Perguntas inseridas: " + inserted);
    }

    private static void insertTips(Context context, SQLiteDatabase bancoDados) {
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
                ContentValues valores = new ContentValues();
                valores.put(Contract.TipsTable.COL_TIP_TEXT, t.getString("text"));
                valores.put(Contract.TipsTable.COL_CATEGORY, "geral");
                valores.put(Contract.TipsTable.COL_IS_ACTIVE, 1);
                bancoDados.insert(Contract.TipsTable.TABLE_NAME, null, valores);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na dica " + i, e);
            }
        }
        Log.d(TAG, "Dicas inseridas: " + inserted);
    }

    // Qual é o arquivo de missões do idioma que está selecionado
    private static String missionsFileName(Context context) {
        String lang = LocaleHelper.getLanguage(context);
        if ("en".equals(lang)) return "missoes_en.json";
        if ("es".equals(lang)) return "missoes_es.json";
        return "missoes.json";
    }

    private static void insertMissions(Context context, SQLiteDatabase bancoDados) {
        JSONArray arr = readJson(context, missionsFileName(context));
        if (arr == null) return;
        int inserted = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                if (insertMission(bancoDados, arr.getJSONObject(i)) > 0) inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro na missao " + i, e);
            }
        }
        Log.d(TAG, "Missoes inseridas: " + inserted);
    }

    // Insere uma missão com os itens dela (devolve o id, ou -1 se der erro)
    private static long insertMission(SQLiteDatabase bancoDados, JSONObject m) {
        try {
            ContentValues valores = new ContentValues();
            valores.put(Contract.MissionsTable.COL_TITLE, m.getString("title"));
            valores.put(Contract.MissionsTable.COL_DESCRIPTION, m.getString("description"));
            valores.put(Contract.MissionsTable.COL_DIFFICULTY, 1);
            valores.put(Contract.MissionsTable.COL_POINTS, m.optInt("points", 100));
            valores.put(Contract.MissionsTable.COL_TOTAL_DAYS, m.optInt("totalDays", 0));
            valores.put(Contract.MissionsTable.COL_IS_COMPLETED, 0);
            long missionId = bancoDados.insert(Contract.MissionsTable.TABLE_NAME, null, valores);

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
                    bancoDados.insert(Contract.MissionItemsTable.TABLE_NAME, null, icv);
                }
            }
            return missionId;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inserir missao", e);
            return -1;
        }
    }

    // Essa missão já está no banco?
    private static boolean missionExists(SQLiteDatabase bancoDados, String title) {
        Cursor cursor = bancoDados.query(Contract.MissionsTable.TABLE_NAME,
                new String[]{Contract.MissionsTable.COL_ID},
                Contract.MissionsTable.COL_TITLE + " = ?",
                new String[]{title}, null, null, null);
        boolean existe = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return existe;
    }

    // Insere só as missões que ainda não existem no banco
    // (assim dá pra lançar missão nova sem apagar o que o usuário já concluiu)
    public static void ensureNewMissions(Context context, SQLiteDatabase bancoDados) {
        JSONArray arr = readJson(context, missionsFileName(context));
        if (arr == null) return;
        int inseridas = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject m = arr.getJSONObject(i);
                String titulo = m.getString("title");
                if (missionExists(bancoDados, titulo)) continue;
                if (insertMission(bancoDados, m) > 0) inseridas++;
            } catch (Exception e) {
                Log.e(TAG, "Erro no ensureNewMissions " + i, e);
            }
        }
        if (inseridas > 0) Log.d(TAG, "Missoes novas inseridas: " + inseridas);
    }

    private static void insertMythsTruths(Context context, SQLiteDatabase bancoDados) {
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
                ContentValues valores = new ContentValues();
                valores.put(Contract.MythsTruthsTable.COL_STATEMENT, mv.getString("phrase"));
                valores.put(Contract.MythsTruthsTable.COL_IS_MYTH,
                        "MITO".equals(mv.optString("type")) || "MYTH".equals(mv.optString("type")) ? 1 : 0);
                valores.put(Contract.MythsTruthsTable.COL_EXPLANATION, mv.getString("explanation"));
                valores.put(Contract.MythsTruthsTable.COL_CATEGORY, "cobras");
                bancoDados.insert(Contract.MythsTruthsTable.TABLE_NAME, null, valores);
                inserted++;
            } catch (Exception e) {
                Log.e(TAG, "Erro no mito " + i, e);
            }
        }
        Log.d(TAG, "Mitos/verdades inseridos: " + inserted);
    }

    private static void insertContent(Context context, SQLiteDatabase bancoDados) {
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
                ContentValues valores = new ContentValues();
                valores.put(Contract.ContentTable.COL_CATEGORY, "serpentes");
                valores.put(Contract.ContentTable.COL_TITLE, s.getString("popularName"));
                valores.put(Contract.ContentTable.COL_SCIENTIFIC_NAME, s.optString("scientificName"));
                valores.put(Contract.ContentTable.COL_FAMILY, s.optString("family"));
                valores.put(Contract.ContentTable.COL_IS_VENOMOUS, s.optBoolean("venomous") ? 1 : 0);
                valores.put(Contract.ContentTable.COL_BODY, s.optString("description"));
                valores.put(Contract.ContentTable.COL_HABITAT, s.optString("habitat"));
                valores.put(Contract.ContentTable.COL_DISTRIBUTION, s.optString("distribution"));
                valores.put(Contract.ContentTable.COL_DIET, s.optString("diet"));
                valores.put(Contract.ContentTable.COL_REPRODUCTION, s.optString("reproduction"));
                valores.put(Contract.ContentTable.COL_MEDICAL_IMPORTANCE, s.optString("medicalImportance"));
                valores.put(Contract.ContentTable.COL_CURIOSITY, s.optString("curiosity"));
                valores.put(Contract.ContentTable.COL_IMAGE_RES_NAME, s.optString("imageName"));
                valores.put(Contract.ContentTable.COL_DISPLAY_ORDER, i + 1);
                bancoDados.insert(Contract.ContentTable.TABLE_NAME, null, valores);
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
    private static void insertAnatomyLayers(Context context, SQLiteDatabase bancoDados) {
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
        long peleId = bancoDados.insert(Contract.AnatomyLayersTable.TABLE_NAME, null, pele);

        ContentValues orgaos = new ContentValues();
        orgaos.put(Contract.AnatomyLayersTable.COL_LAYER_NAME, "orgaos");
        orgaos.put(Contract.AnatomyLayersTable.COL_DISPLAY_NAME, orgaosDisplayName);
        orgaos.put(Contract.AnatomyLayersTable.COL_DESCRIPTION, orgaosDescription);
        orgaos.put(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER, 2);
        long orgaosId = bancoDados.insert(Contract.AnatomyLayersTable.TABLE_NAME, null, orgaos);

        insertHotspot(bancoDados, peleId, hsEscamasName, hsEscamasDesc, 0, 0, 0);
        insertHotspot(bancoDados, orgaosId, hsPulmaoName, hsPulmaoDesc, 0.3f, 0.3f, 0f);
        insertHotspot(bancoDados, orgaosId, hsCoracaoName, hsCoracaoDesc, 0.5f, 0.5f, 0f);
    }

    private static void insertHotspot(SQLiteDatabase bancoDados, long layerId, String name,
                                      String description, float x, float y, float z) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.AnatomyHotspotsTable.COL_LAYER_ID, layerId);
        valores.put(Contract.AnatomyHotspotsTable.COL_HOTSPOT_NAME, name);
        valores.put(Contract.AnatomyHotspotsTable.COL_DESCRIPTION, description);
        valores.put(Contract.AnatomyHotspotsTable.COL_POSITION_X, x);
        valores.put(Contract.AnatomyHotspotsTable.COL_POSITION_Y, y);
        valores.put(Contract.AnatomyHotspotsTable.COL_POSITION_Z, z);
        bancoDados.insert(Contract.AnatomyHotspotsTable.TABLE_NAME, null, valores);
    }

    private static void insertUserProgress(SQLiteDatabase bancoDados) {
        ContentValues valores = new ContentValues();
        bancoDados.insert(Contract.UserProgressTable.TABLE_NAME, null, valores);
    }
}

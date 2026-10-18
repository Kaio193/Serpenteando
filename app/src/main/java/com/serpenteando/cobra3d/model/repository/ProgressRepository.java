package com.serpenteando.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.UserProgress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// [ALÉM DO TEI] Repository Pattern — isola as queries SQL; a lógica de negócio não conhece o banco
public class ProgressRepository {

    private final SQLiteDatabase bancoDados;

    public ProgressRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public UserProgress getProgress() {
        // [TEI Persistência] db.query: lê registro de progresso do SQLite
        Cursor cursor = bancoDados.query(Contract.UserProgressTable.TABLE_NAME, null,
                Contract.UserProgressTable.COL_ID + " = 1", null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            UserProgress progress = cursorToProgress(cursor);
            cursor.close();
            return progress;
        }
        UserProgress defaultProgress = new UserProgress();
        insertProgress(defaultProgress);
        return defaultProgress;
    }

    public void insertProgress(UserProgress progress) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE, progress.getQuizHighScore());
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_FACIL, progress.getQuizHighScoreFacil());
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_MEDIO, progress.getQuizHighScoreMedio());
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_DIFICIL, progress.getQuizHighScoreDificil());
        valores.put(Contract.UserProgressTable.COL_QUIZZES_COMPLETED, progress.getQuizzesCompleted());
        valores.put(Contract.UserProgressTable.COL_MISSIONS_COMPLETED, progress.getMissionsCompleted());
        valores.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints());
        valores.put(Contract.UserProgressTable.COL_CURRENT_STREAK, progress.getCurrentStreak());
        valores.put(Contract.UserProgressTable.COL_LAST_MISSION_DATE, progress.getLastMissionDate());
        valores.put(Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT, progress.getQuizFacilCount());
        valores.put(Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT, progress.getQuizMedioCount());
        valores.put(Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT, progress.getQuizDificilCount());
        valores.put(Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT, progress.getAnatomyHotspotCount());
        valores.put(Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT, progress.getAnatomyLayerCount());
        valores.put(Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT, progress.getContentDetailCount());
        valores.put(Contract.UserProgressTable.COL_AWARDED_MISSIONS,
                progress.getAwardedMissions() != null ? progress.getAwardedMissions() : "");
        valores.put(Contract.UserProgressTable.COL_TOTAL_QUESTIONS_CORRECT,
                progress.getTotalQuestionsCorrect());
        bancoDados.insert(Contract.UserProgressTable.TABLE_NAME, null, valores);
    }

    // Incrementa o contador de uma ação real (quiz por dificuldade, anatomia, conteúdo)
    public void incrementEvent(String trigger) {
        if (trigger == null || trigger.isEmpty()) return;
        String column = null;
        switch (trigger) {
            case "QUIZ_FACIL": column = Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT; break;
            case "QUIZ_MEDIO": column = Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT; break;
            case "QUIZ_DIFICIL": column = Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT; break;
            case "ANATOMY_HOTSPOT": column = Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT; break;
            case "ANATOMY_LAYER": column = Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT; break;
            case "CONTENT_DETAIL": column = Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT; break;
            default: return;
        }
        bancoDados.execSQL("UPDATE " + Contract.UserProgressTable.TABLE_NAME +
                " SET " + column + " = " + column + " + 1" +
                " WHERE " + Contract.UserProgressTable.COL_ID + " = 1");
        // A ação que alimenta uma missão também conta o dia da sequência.
        // Várias ações no mesmo dia continuam valendo 1 dia só.
        registrarAtividadeDoDia();
    }

    /**
     * Marca que houve atividade válida de missão hoje.
     *
     * A sequência é por DIA, não por etapa: a primeira ação do dia começa em 1,
     * a do dia seguinte soma 1 e um dia inteiro sem ação reinicia em 1. Chamado
     * pelos contadores de quiz/conteúdo/anatomia (os mesmos gatilhos das
     * missões) e pela conclusão de missão. Abrir o app sozinho não chama isto.
     */
    private void registrarAtividadeDoDia() {
        try {
            UserProgress progress = getProgress();
            String hoje = diaDeHoje();
            String ultimo = progress.getLastMissionDate();
            int novaSequencia = calcularSequencia(ultimo, progress.getCurrentStreak(), hoje);

            // Já contou hoje e o valor não mudou: não regrava à toa.
            if (ultimo != null && hoje.equals(ultimo.trim())
                    && novaSequencia == progress.getCurrentStreak()) {
                return;
            }

            ContentValues valores = new ContentValues();
            valores.put(Contract.UserProgressTable.COL_CURRENT_STREAK, novaSequencia);
            valores.put(Contract.UserProgressTable.COL_LAST_MISSION_DATE, hoje);
            valores.put(Contract.UserProgressTable.COL_UPDATED_AT,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            bancoDados.update(Contract.UserProgressTable.TABLE_NAME, valores,
                    Contract.UserProgressTable.COL_ID + " = 1", null);
        } catch (Exception ignored) {
        }
    }

    // Quantas vezes o usuário já realizou a ação X (usado pelas missões)
    public int getEventCount(String trigger) {
        if (trigger == null) return 0;
        String column = null;
        switch (trigger) {
            case "QUIZ_FACIL": column = Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT; break;
            case "QUIZ_MEDIO": column = Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT; break;
            case "QUIZ_DIFICIL": column = Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT; break;
            case "ANATOMY_HOTSPOT": column = Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT; break;
            case "ANATOMY_LAYER": column = Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT; break;
            case "CONTENT_DETAIL": column = Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT; break;
            default: return 0;
        }
        Cursor cursor = bancoDados.rawQuery("SELECT " + column +
                " FROM " + Contract.UserProgressTable.TABLE_NAME +
                " WHERE " + Contract.UserProgressTable.COL_ID + " = 1", null);
        int value = 0;
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getInt(0);
            cursor.close();
        }
        return value;
    }

    // Recorde POR NÍVEL (v8): fácil, médio e dificil são independentes — a pontuação
    // de um nível só disputa com a do MESMO nível, então trocar de nível conta sempre.
    // O recorde geral (card da Home) vira o maior entre o valor já guardado e os três
    // níveis: ele só sobe, nunca perde o melhor resultado da história.
    public void updateQuizScore(int score, int dificuldade) {
        UserProgress progress = getProgress();
        ContentValues valores = new ContentValues();
        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int facil = progress.getQuizHighScoreFacil();
        int medio = progress.getQuizHighScoreMedio();
        int dificil = progress.getQuizHighScoreDificil();
        if (dificuldade == 2) {
            if (score > medio) medio = score;
        } else if (dificuldade == 3) {
            if (score > dificil) dificil = score;
        } else if (score > facil) {
            facil = score;
        }
        int geral = Math.max(progress.getQuizHighScore(),
                Math.max(facil, Math.max(medio, dificil)));
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_FACIL, facil);
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_MEDIO, medio);
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_DIFICIL, dificil);
        valores.put(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE, geral);
        valores.put(Contract.UserProgressTable.COL_QUIZZES_COMPLETED, progress.getQuizzesCompleted() + 1);
        // Soma apenas as questoes acertadas neste quiz ao total acumulado de acertos.
        // Cada acerto conta 1, independente da dificuldade ou pontuacao da missao.
        valores.put(Contract.UserProgressTable.COL_TOTAL_QUESTIONS_CORRECT,
                progress.getTotalQuestionsCorrect() + score);

        // A sequência de dias NÃO é mais contada pelo quiz (regra nova, 13/10):
        // o dia só conta quando o usuário conclui uma MISSÃO — ver
        // updateMissionCompleted(). O quiz continua somando pontos normalmente.
        valores.put(Contract.UserProgressTable.COL_LAST_QUIZ_DATE, hoje);
        valores.put(Contract.UserProgressTable.COL_UPDATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        bancoDados.update(Contract.UserProgressTable.TABLE_NAME, valores,
                Contract.UserProgressTable.COL_ID + " = 1", null);
    }

    // Regra da sequência: 1ª missão = 1 · concluir outra no MESMO dia mantém
    // · concluir no dia seguinte soma 1 · pular um dia reinicia em 1.
    private int calcularSequencia(String ultimoDia, int sequenciaAtual, String hoje) {
        if (ultimoDia == null || ultimoDia.trim().isEmpty()) return 1;
        if (hoje.equals(ultimoDia.trim())) return Math.max(sequenciaAtual, 1);
        if (ehDiaAnterior(ultimoDia.trim(), hoje)) return Math.max(sequenciaAtual, 0) + 1;
        return 1;
    }

    private boolean ehDiaAnterior(String dia, String hoje) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            fmt.setLenient(false);
            Date referencia = fmt.parse(hoje);
            if (referencia == null) return false;
            Date ontem = new Date(referencia.getTime() - 24L * 60 * 60 * 1000);
            return fmt.format(ontem).equals(dia);
        } catch (Exception e) {
            return false;
        }
    }

    // O que o card da Home mostra: se a última missão foi hoje ou ontem a
    // sequência vale; se ficou mais de um dia sem concluir, ela já quebrou (0)
    public int getSequenciaEfetiva(UserProgress progress) {
        if (progress == null) return 0;
        String ultimo = progress.getLastMissionDate();
        if (ultimo == null || ultimo.trim().isEmpty()) return 0;
        String hoje = diaDeHoje();
        if (hoje.equals(ultimo.trim())) return Math.max(progress.getCurrentStreak(), 1);
        if (ehDiaAnterior(ultimo.trim(), hoje)) return Math.max(progress.getCurrentStreak(), 1);
        return 0;
    }

    // Recorde do nível 1/2/3 (fácil/médio/difícil) — mostra no dialog de resultado
    public int getRecordeDoNivel(int dificuldade) {
        UserProgress progress = getProgress();
        if (dificuldade == 2) return progress.getQuizHighScoreMedio();
        if (dificuldade == 3) return progress.getQuizHighScoreDificil();
        return progress.getQuizHighScoreFacil();
    }

    private String diaDeHoje() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public void updateMissionCompleted(long missionId, int points) {
        UserProgress progress = getProgress();
        ContentValues valores = new ContentValues();
        String hoje = diaDeHoje();
        valores.put(Contract.UserProgressTable.COL_MISSIONS_COMPLETED, progress.getMissionsCompleted() + 1);
        valores.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints() + points);
        // Só aqui a sequência avança: concluir missão é o que faz o dia contar.
        // Várias no mesmo dia contam uma vez só.
        valores.put(Contract.UserProgressTable.COL_CURRENT_STREAK,
                calcularSequencia(progress.getLastMissionDate(), progress.getCurrentStreak(), hoje));
        valores.put(Contract.UserProgressTable.COL_LAST_MISSION_DATE, hoje);
        // Registra o id da missão premiada (ex.: "1,3") para não premiar a mesma missão 2x
        String current = progress.getAwardedMissions();
        String sep = (current == null || current.trim().isEmpty()) ? "" : ",";
        valores.put(Contract.UserProgressTable.COL_AWARDED_MISSIONS,
                (current == null ? "" : current.trim()) + sep + missionId);
        valores.put(Contract.UserProgressTable.COL_UPDATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        bancoDados.update(Contract.UserProgressTable.TABLE_NAME, valores,
                Contract.UserProgressTable.COL_ID + " = 1", null);
    }

    private UserProgress cursorToProgress(Cursor cursor) {
        UserProgress progress = new UserProgress();
        progress.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_ID)));
        progress.setQuizHighScore(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE)));
        progress.setQuizHighScoreFacil(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_HIGH_FACIL)));
        progress.setQuizHighScoreMedio(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_HIGH_MEDIO)));
        progress.setQuizHighScoreDificil(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_HIGH_DIFICIL)));
        progress.setQuizzesCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZZES_COMPLETED)));
        progress.setMissionsCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_MISSIONS_COMPLETED)));
        progress.setTotalPoints(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_TOTAL_POINTS)));
        progress.setCurrentStreak(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_CURRENT_STREAK)));
        progress.setLastQuizDate(cursor.getString(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_LAST_QUIZ_DATE)));
        progress.setLastTipDate(cursor.getString(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_LAST_TIP_DATE)));
        progress.setLastMissionDate(cursor.getString(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_LAST_MISSION_DATE)));
        progress.setQuizFacilCount(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT)));
        progress.setQuizMedioCount(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT)));
        progress.setQuizDificilCount(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT)));
        progress.setAnatomyHotspotCount(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT)));
        progress.setAnatomyLayerCount(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT)));
        progress.setContentDetailCount(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT)));
        progress.setAwardedMissions(cursor.getString(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_AWARDED_MISSIONS)));
        progress.setTotalQuestionsCorrect(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.UserProgressTable.COL_TOTAL_QUESTIONS_CORRECT)));
        return progress;
    }
}

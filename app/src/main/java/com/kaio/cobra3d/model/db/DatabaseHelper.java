package com.kaio.cobra3d.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kaio.cobra3d.model.db.Contract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + QuestionsTable.TABLE_NAME + " (" +
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

        db.execSQL("CREATE TABLE " + TipsTable.TABLE_NAME + " (" +
                TipsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TipsTable.COL_TIP_TEXT + " TEXT NOT NULL, " +
                TipsTable.COL_CATEGORY + " TEXT, " +
                TipsTable.COL_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                TipsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE " + MissionsTable.TABLE_NAME + " (" +
                MissionsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MissionsTable.COL_TITLE + " TEXT NOT NULL, " +
                MissionsTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                MissionsTable.COL_DIFFICULTY + " INTEGER NOT NULL DEFAULT 1, " +
                MissionsTable.COL_POINTS + " INTEGER NOT NULL DEFAULT 10, " +
                MissionsTable.COL_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                MissionsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE " + MissionItemsTable.TABLE_NAME + " (" +
                MissionItemsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MissionItemsTable.COL_MISSION_ID + " INTEGER NOT NULL, " +
                MissionItemsTable.COL_ITEM_TEXT + " TEXT NOT NULL, " +
                MissionItemsTable.COL_IS_CORRECT + " INTEGER NOT NULL, " +
                MissionItemsTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + MissionItemsTable.COL_MISSION_ID + ") REFERENCES " +
                MissionsTable.TABLE_NAME + "(" + MissionsTable.COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + MythsTruthsTable.TABLE_NAME + " (" +
                MythsTruthsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MythsTruthsTable.COL_STATEMENT + " TEXT NOT NULL, " +
                MythsTruthsTable.COL_IS_MYTH + " INTEGER NOT NULL, " +
                MythsTruthsTable.COL_EXPLANATION + " TEXT NOT NULL, " +
                MythsTruthsTable.COL_CATEGORY + " TEXT, " +
                MythsTruthsTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE " + ContentTable.TABLE_NAME + " (" +
                ContentTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContentTable.COL_CATEGORY + " TEXT NOT NULL, " +
                ContentTable.COL_TITLE + " TEXT NOT NULL, " +
                ContentTable.COL_BODY + " TEXT NOT NULL, " +
                ContentTable.COL_IMAGE_RES_NAME + " TEXT, " +
                ContentTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                ContentTable.COL_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE " + AnatomyLayersTable.TABLE_NAME + " (" +
                AnatomyLayersTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AnatomyLayersTable.COL_LAYER_NAME + " TEXT NOT NULL, " +
                AnatomyLayersTable.COL_DISPLAY_NAME + " TEXT NOT NULL, " +
                AnatomyLayersTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                AnatomyLayersTable.COL_MODEL_FILE_NAME + " TEXT, " +
                AnatomyLayersTable.COL_DISPLAY_ORDER + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + AnatomyHotspotsTable.TABLE_NAME + " (" +
                AnatomyHotspotsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AnatomyHotspotsTable.COL_LAYER_ID + " INTEGER NOT NULL, " +
                AnatomyHotspotsTable.COL_HOTSPOT_NAME + " TEXT NOT NULL, " +
                AnatomyHotspotsTable.COL_DESCRIPTION + " TEXT NOT NULL, " +
                AnatomyHotspotsTable.COL_POSITION_X + " REAL NOT NULL, " +
                AnatomyHotspotsTable.COL_POSITION_Y + " REAL NOT NULL, " +
                AnatomyHotspotsTable.COL_POSITION_Z + " REAL NOT NULL, " +
                "FOREIGN KEY (" + AnatomyHotspotsTable.COL_LAYER_ID + ") REFERENCES " +
                AnatomyLayersTable.TABLE_NAME + "(" + AnatomyLayersTable.COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + UserProgressTable.TABLE_NAME + " (" +
                UserProgressTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserProgressTable.COL_QUIZ_HIGH_SCORE + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_QUIZZES_COMPLETED + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_MISSIONS_COMPLETED + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_TOTAL_POINTS + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_CURRENT_STREAK + " INTEGER DEFAULT 0, " +
                UserProgressTable.COL_LAST_QUIZ_DATE + " TEXT, " +
                UserProgressTable.COL_LAST_TIP_DATE + " TEXT, " +
                UserProgressTable.COL_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        DatabaseInitializer.initialize(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AnatomyHotspotsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AnatomyLayersTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MissionItemsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MissionsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserProgressTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ContentTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MythsTruthsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TipsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(db);
    }
}

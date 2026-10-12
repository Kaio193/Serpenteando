package com.kaio.cobra3d.model.db;

public final class Contract {

    private Contract() {}

    public static final String DATABASE_NAME = "serpenteando.db";
    public static final int DATABASE_VERSION = 6;

    public static final class QuestionsTable {
        public static final String TABLE_NAME = "questions";
        public static final String COL_ID = "id";
        public static final String COL_QUESTION_TEXT = "question_text";
        public static final String COL_OPTION_A = "option_a";
        public static final String COL_OPTION_B = "option_b";
        public static final String COL_OPTION_C = "option_c";
        public static final String COL_OPTION_D = "option_d";
        public static final String COL_CORRECT_ANSWER = "correct_answer";
        public static final String COL_DIFFICULTY = "difficulty";
        public static final String COL_CATEGORY = "category";
        public static final String COL_EXPLANATION = "explanation";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class TipsTable {
        public static final String TABLE_NAME = "tips";
        public static final String COL_ID = "id";
        public static final String COL_TIP_TEXT = "tip_text";
        public static final String COL_CATEGORY = "category";
        public static final String COL_IS_ACTIVE = "is_active";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class MissionsTable {
        public static final String TABLE_NAME = "missions";
        public static final String COL_ID = "id";
        public static final String COL_TITLE = "title";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_DIFFICULTY = "difficulty";
        public static final String COL_POINTS = "points";
        public static final String COL_TOTAL_DAYS = "total_days";
        public static final String COL_IS_COMPLETED = "is_completed";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class MissionItemsTable {
        public static final String TABLE_NAME = "mission_items";
        public static final String COL_ID = "id";
        public static final String COL_MISSION_ID = "mission_id";
        public static final String COL_ITEM_TEXT = "item_text";
        public static final String COL_TRIGGER = "trigger";
        public static final String COL_TARGET = "target";
        public static final String COL_IS_CORRECT = "is_correct";
        public static final String COL_DISPLAY_ORDER = "display_order";
    }

    public static final class MythsTruthsTable {
        public static final String TABLE_NAME = "myths_truths";
        public static final String COL_ID = "id";
        public static final String COL_STATEMENT = "statement";
        public static final String COL_IS_MYTH = "is_myth";
        public static final String COL_EXPLANATION = "explanation";
        public static final String COL_CATEGORY = "category";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class ContentTable {
        public static final String TABLE_NAME = "content";
        public static final String COL_ID = "id";
        public static final String COL_CATEGORY = "category";
        public static final String COL_TITLE = "title";
        public static final String COL_SCIENTIFIC_NAME = "scientific_name";
        public static final String COL_FAMILY = "family";
        public static final String COL_IS_VENOMOUS = "is_venomous";
        public static final String COL_BODY = "body";
        public static final String COL_HABITAT = "habitat";
        public static final String COL_DISTRIBUTION = "distribution";
        public static final String COL_DIET = "diet";
        public static final String COL_REPRODUCTION = "reproduction";
        public static final String COL_MEDICAL_IMPORTANCE = "medical_importance";
        public static final String COL_CURIOSITY = "curiosity";
        public static final String COL_IMAGE_RES_NAME = "image_res_name";
        public static final String COL_DISPLAY_ORDER = "display_order";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class AnatomyLayersTable {
        public static final String TABLE_NAME = "anatomy_layers";
        public static final String COL_ID = "id";
        public static final String COL_LAYER_NAME = "layer_name";
        public static final String COL_DISPLAY_NAME = "display_name";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_MODEL_FILE_NAME = "model_file_name";
        public static final String COL_DISPLAY_ORDER = "display_order";
    }

    public static final class AnatomyHotspotsTable {
        public static final String TABLE_NAME = "anatomy_hotspots";
        public static final String COL_ID = "id";
        public static final String COL_LAYER_ID = "layer_id";
        public static final String COL_HOTSPOT_NAME = "hotspot_name";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_POSITION_X = "position_x";
        public static final String COL_POSITION_Y = "position_y";
        public static final String COL_POSITION_Z = "position_z";
    }

    public static final class UserProgressTable {
        public static final String TABLE_NAME = "user_progress";
        public static final String COL_ID = "id";
        public static final String COL_QUIZ_HIGH_SCORE = "quiz_high_score";
        public static final String COL_QUIZZES_COMPLETED = "quizzes_completed";
        public static final String COL_MISSIONS_COMPLETED = "missions_completed";
        public static final String COL_TOTAL_POINTS = "total_points";
        public static final String COL_CURRENT_STREAK = "current_streak";
        public static final String COL_LAST_QUIZ_DATE = "last_quiz_date";
        public static final String COL_LAST_TIP_DATE = "last_tip_date";
        public static final String COL_QUIZ_FACIL_COUNT = "quiz_facil_count";
        public static final String COL_QUIZ_MEDIO_COUNT = "quiz_medio_count";
        public static final String COL_QUIZ_DIFICIL_COUNT = "quiz_dificil_count";
        public static final String COL_ANATOMY_HOTSPOT_COUNT = "anatomy_hotspot_count";
        public static final String COL_ANATOMY_LAYER_COUNT = "anatomy_layer_count";
        public static final String COL_CONTENT_DETAIL_COUNT = "content_detail_count";
        public static final String COL_AWARDED_MISSIONS = "awarded_missions";
        public static final String COL_UPDATED_AT = "updated_at";
    }
}

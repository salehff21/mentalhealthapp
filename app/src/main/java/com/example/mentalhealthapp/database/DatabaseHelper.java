package com.example.mentalhealthapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "mental_health.db";
    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // users
        db.execSQL(
                "CREATE TABLE users (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "full_name TEXT NOT NULL," +
                        "username TEXT NOT NULL UNIQUE," +
                        "email TEXT NOT NULL UNIQUE," +
                        "password_hash TEXT NOT NULL," +
                        "gender TEXT NOT NULL," +
                        "birth_year INTEGER NOT NULL," +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "last_login_at TEXT," +
                        "is_active INTEGER DEFAULT 1" +
                        ");"
        );

        // questionnaires
        db.execSQL(
                "CREATE TABLE questionnaires (" +
                        "questionnaire_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT NOT NULL," +
                        "description TEXT," +
                        "is_active INTEGER DEFAULT 1," +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                        ");"
        );

        // questions
        db.execSQL(
                "CREATE TABLE questions (" +
                        "question_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "questionnaire_id INTEGER NOT NULL," +
                        "question_text TEXT NOT NULL," +
                        "question_type TEXT NOT NULL," +
                        "order_no INTEGER DEFAULT 1," +
                        "is_required INTEGER DEFAULT 1," +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(questionnaire_id) REFERENCES questionnaires(questionnaire_id)" +
                        ");"
        );

        // options
        db.execSQL(
                "CREATE TABLE options (" +
                        "option_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "question_id INTEGER NOT NULL," +
                        "option_text TEXT NOT NULL," +
                        "option_value INTEGER," +
                        "order_no INTEGER DEFAULT 1," +
                        "FOREIGN KEY(question_id) REFERENCES questions(question_id)" +
                        ");"
        );

        // mood_entries
        db.execSQL(
                "CREATE TABLE mood_entries (" +
                        "entry_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "mood_level TEXT NOT NULL," +
                        "stress_score INTEGER," +
                        "sleep_quality INTEGER," +
                        "energy_level INTEGER," +
                        "notes TEXT," +
                        "recorded_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                        ");"
        );

        // entry_answers
        db.execSQL(
                "CREATE TABLE entry_answers (" +
                        "answer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "entry_id INTEGER NOT NULL," +
                        "question_id INTEGER NOT NULL," +
                        "option_id INTEGER," +
                        "answer_text TEXT," +
                        "score_value INTEGER," +
                        "answered_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(entry_id) REFERENCES mood_entries(entry_id)," +
                        "FOREIGN KEY(question_id) REFERENCES questions(question_id)," +
                        "FOREIGN KEY(option_id) REFERENCES options(option_id)" +
                        ");"
        );

        // ai_analysis
        db.execSQL(
                "CREATE TABLE ai_analysis (" +
                        "analysis_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "entry_id INTEGER NOT NULL," +
                        "user_id INTEGER NOT NULL," +
                        "mood_class TEXT NOT NULL," +
                        "risk_level TEXT NOT NULL," +
                        "recommendation TEXT," +
                        "analyzed_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(entry_id) REFERENCES mood_entries(entry_id)," +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                        ");"
        );

        // privacy_settings (1-to-1)
        db.execSQL(
                "CREATE TABLE privacy_settings (" +
                        "privacy_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL UNIQUE," +
                        "share_data INTEGER DEFAULT 0," +
                        "notifications INTEGER DEFAULT 1," +
                        "language_pref TEXT DEFAULT 'ar'," +
                        "updated_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                        ");"
        );

        // notifications
        db.execSQL(
                "CREATE TABLE notifications (" +
                        "notification_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "title TEXT NOT NULL," +
                        "body TEXT NOT NULL," +
                        "notification_type TEXT NOT NULL," +
                        "is_read INTEGER DEFAULT 0," +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                        ");"
        );

        // =======================
        //   SEED DEFAULT DATA
        // =======================
        seedDefaultQuestionnaire(db);
    }

    /**
     * إضافة الاستبيان + الأسئلة + الخيارات عند إنشاء قاعدة البيانات لأول مرة.
     */
    private void seedDefaultQuestionnaire(SQLiteDatabase db) {

        // 1) الاستبيان
        long questionnaireId = insertQuestionnaire(
                db,
                "الاستبيان اليومي للصحة النفسية",
                "استبيان قصير لمتابعة مزاجك، مستوى التوتر، جودة النوم، الطاقة، العلاقات الاجتماعية، والتركيز."
        );

        // 2) الأسئلة (نفس الأسئلة في MoodFragment)

        // السؤال 1: المزاج العام
        long q1Id = insertQuestion(
                db,
                questionnaireId,
                "كيف تقيم مزاجك العام اليوم؟",
                "single_choice",
                1,
                true
        );
        insertOption(db, q1Id, "ممتاز", 4, 1);
        insertOption(db, q1Id, "جيد",   3, 2);
        insertOption(db, q1Id, "متوسط", 2, 3);
        insertOption(db, q1Id, "سيئ",   1, 4);

        // السؤال 2: مستوى التوتر
        long q2Id = insertQuestion(
                db,
                questionnaireId,
                "ما مستوى التوتر الذي تشعر به الآن؟",
                "single_choice",
                2,
                true
        );
        insertOption(db, q2Id, "منخفض جدًا", 1, 1);
        insertOption(db, q2Id, "منخفض",      2, 2);
        insertOption(db, q2Id, "متوسط",      3, 3);
        insertOption(db, q2Id, "مرتفع",      4, 4);

        // السؤال 3: جودة النوم
        long q3Id = insertQuestion(
                db,
                questionnaireId,
                "كيف تقيم جودة نومك خلال الليلة الماضية؟",
                "single_choice",
                3,
                true
        );
        insertOption(db, q3Id, "جيدة جدًا", 4, 1);
        insertOption(db, q3Id, "جيدة",     3, 2);
        insertOption(db, q3Id, "متوسطة",   2, 3);
        insertOption(db, q3Id, "سيئة",     1, 4);

        // السؤال 4: مستوى الطاقة
        long q4Id = insertQuestion(
                db,
                questionnaireId,
                "ما مستوى طاقتك اليوم؟",
                "single_choice",
                4,
                true
        );
        insertOption(db, q4Id, "عالية جدًا", 4, 1);
        insertOption(db, q4Id, "جيدة",      3, 2);
        insertOption(db, q4Id, "متوسطة",    2, 3);
        insertOption(db, q4Id, "منخفضة",    1, 4);

        // السؤال 5: الرضا عن العلاقات
        long q5Id = insertQuestion(
                db,
                questionnaireId,
                "إلى أي مدى تشعر بالرضا عن علاقاتك الاجتماعية؟",
                "single_choice",
                5,
                true
        );
        insertOption(db, q5Id, "راضٍ جدًا", 4, 1);
        insertOption(db, q5Id, "راضٍ",     3, 2);
        insertOption(db, q5Id, "محايد",    2, 3);
        insertOption(db, q5Id, "غير راضٍ", 1, 4);

        // السؤال 6: التركيز
        long q6Id = insertQuestion(
                db,
                questionnaireId,
                "ما مدى قدرتك على التركيز على مهامك اليوم؟",
                "single_choice",
                6,
                true
        );
        insertOption(db, q6Id, "تركيز ممتاز", 4, 1);
        insertOption(db, q6Id, "جيد",        3, 2);
        insertOption(db, q6Id, "متوسط",      2, 3);
        insertOption(db, q6Id, "ضعيف",       1, 4);
    }

    private long insertQuestionnaire(SQLiteDatabase db, String title, String description) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("is_active", 1);
        return db.insert("questionnaires", null, cv);
    }

    private long insertQuestion(SQLiteDatabase db,
                                long questionnaireId,
                                String text,
                                String type,
                                int orderNo,
                                boolean isRequired) {

        ContentValues cv = new ContentValues();
        cv.put("questionnaire_id", questionnaireId);
        cv.put("question_text", text);
        cv.put("question_type", type);
        cv.put("order_no", orderNo);
        cv.put("is_required", isRequired ? 1 : 0);
        return db.insert("questions", null, cv);
    }

    private long insertOption(SQLiteDatabase db,
                              long questionId,
                              String text,
                              int value,
                              int orderNo) {

        ContentValues cv = new ContentValues();
        cv.put("question_id", questionId);
        cv.put("option_text", text);
        cv.put("option_value", value);
        cv.put("order_no", orderNo);
        return db.insert("options", null, cv);
    }

    // ===========================
    //      AUTH / HELPERS
    // ===========================

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email=? AND password_hash=?",
                new String[]{email, password}
        );

        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    // حفظ إجابة سؤال واحد في entry_answers
    public long insertEntryAnswer(long entryId,
                                  int questionId,
                                  Integer optionId,   // يمكن أن يكون null
                                  String answerText,
                                  Integer scoreValue) { // يمكن أن يكون null

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("entry_id", entryId);
        values.put("question_id", questionId);

        if (optionId != null) {
            values.put("option_id", optionId);
        }

        if (answerText != null) {
            values.put("answer_text", answerText);
        }

        if (scoreValue != null) {
            values.put("score_value", scoreValue);
        }

        return db.insert("entry_answers", null, values);
    }

    /**
     * إدخال mood_entry مع إرجاع رقم السجل (entry_id)
     */
    public long insertMoodEntry(int userId,
                                String mood,
                                Integer stress,
                                Integer sleep,
                                Integer energy,
                                String notes) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("user_id", userId);
        cv.put("mood_level", mood);
        cv.put("stress_score", stress);
        cv.put("sleep_quality", sleep);
        cv.put("energy_level", energy);
        cv.put("notes", notes);

        return db.insert("mood_entries", null, cv);
    }

    /**
     * نسخة قديمة ترجع boolean (لو كنت مستخدمها في كود آخر)
     */
    public boolean insertMood(int userId,
                              String mood,
                              Integer stress,
                              Integer sleep,
                              Integer energy,
                              String notes) {

        long result = insertMoodEntry(userId, mood, stress, sleep, energy, notes);
        return result != -1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notifications");
        db.execSQL("DROP TABLE IF EXISTS privacy_settings");
        db.execSQL("DROP TABLE IF EXISTS ai_analysis");
        db.execSQL("DROP TABLE IF EXISTS entry_answers");
        db.execSQL("DROP TABLE IF EXISTS mood_entries");
        db.execSQL("DROP TABLE IF EXISTS options");
        db.execSQL("DROP TABLE IF EXISTS questions");
        db.execSQL("DROP TABLE IF EXISTS questionnaires");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}

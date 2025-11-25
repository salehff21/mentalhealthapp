package com.example.mentalhealthapp.database;

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
        }
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



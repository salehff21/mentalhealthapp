 # 🧠📱 MentalHealthApp  
*A Mobile Application for Monitoring Mental Health Using On-Device Rule-Based Analysis*

---

## 📌 Overview
**MentalHealthApp** is a mobile application designed to help users **monitor their mental wellbeing on a daily basis** through a simple questionnaire and **on-device intelligent analysis**.  

The app focuses on key psychological indicators such as **mood, stress, sleep quality, and energy level**, and provides users with **instant feedback, visual trends, and general wellbeing recommendations** – all processed locally on the device using rule-based logic.

> ⚠️ *Note:* This application is intended for **self-awareness and wellbeing support only**.  
> It is **not a medical diagnostic or treatment tool**.

---

## ⭐ Main Features

### 🔐 User Authentication
- Local user registration and login  
- Basic credential validation  
- User profile management (name, email, language preference, etc.)

### 📋 Daily Mood Questionnaire
- Short, easy-to-answer daily questionnaire  
- Covers:
  - Overall mood  
  - Stress level  
  - Sleep quality  
  - Energy/fatigue  
- Questions and options are stored in the local database (SQLite)

### 🧠 On-Device Intelligent Analysis
- No external server or cloud AI is required  
- Uses simple **statistical calculations** (e.g., averages) and **rule-based logic** to:
  - Analyze daily scores  
  - Detect patterns such as:
    - Persistently low mood  
    - High stress over several days  
  - Generate textual feedback based on detected patterns  
- All analysis is performed **locally in the app**, in Java

### 📊 Mood Trends & History
- Line charts of mood trend over time (weekly/monthly view)  
- Visualized using a charting library (e.g., MPAndroidChart)  
- Full history of past entries stored in SQLite  
- Ability to review older records and compare changes in mood and stress

### 💬 Wellbeing Recommendations
- General recommendations based on the user’s recent scores  
- Simple rule-based messages for:
  - High stress  
  - Low mood  
  - Relatively stable / positive patterns  
- Focus on lifestyle tips and self-help suggestions

### 🌐 Bilingual User Interface
- Supports **Arabic and English**  
- Language can be changed from settings  
- Uses Android resource files (`values` / `values-ar`) for localization

### 🛡️ Profile & Privacy
- Local profile settings (name, email, language, etc.)  
- Basic privacy options stored in the database  
- All data is stored **locally on the device** using SQLite

---

## 🏗️ System Architecture (High-Level)

The current version of **MentalHealthApp** is a fully on-device solution with two main logical layers:

### 📱 Presentation Layer (Mobile App)
- Android app developed in **Java** using **Android Studio**  
- Activities and Fragments:
  - Authentication screens (Login / Register)  
  - Main screen with bottom navigation  
  - Home, Mood, Analysis, Profile, and Settings screens  
- Responsible for:
  - Displaying questions and collecting responses  
  - Rendering charts and history  
  - Displaying analysis results and recommendations  

### 🗄️ Data & Analysis Layer (On-Device)
- **SQLite** database via a custom `SQLiteOpenHelper` (`DatabaseHelper`)  
- Stores:
  - Users  
  - Questionnaires and questions  
  - Daily mood entries and detailed answers  
  - Analysis logs and basic privacy settings  
- Contains the **rule-based analysis engine** implemented in Java to:
  - Compute average scores  
  - Check threshold conditions  
  - Generate feedback messages

> There is **no separate backend API** and **no remote AI server** in the current implementation.  
> All logic and storage are local to the Android device.

---

## 🧠 Analysis Logic (Current Version)

The current “intelligent” component is implemented as **rule-based analysis** running inside the app:

- Aggregates questionnaire scores (e.g., mood, stress) over a configurable period  
- Computes average scores per metric  
- Applies a set of rules, for example:
  - If average stress is high → show stress-related warning and tips  
  - If mood is consistently low → display low-mood advice and encouragement  
  - If mood is relatively stable and positive → show a positive reinforcement message  
- Stores analysis results and messages in the local database when needed

> Future versions may replace or extend this rule-based approach with trained machine-learning models.

---

## 🗃️ Database Design (Simplified)

All data is stored locally in an **SQLite** database. Typical tables include:

- **users** – authentication & basic profile data  
- **questionnaires** – definitions of questionnaire templates  
- **questions** – each question with its type and category  
- **options** – choices for multiple-choice questions and their scores  
- **mood_entries** – one record per daily submission  
- **entry_answers** – detailed answers linked to each mood entry  
- **ai_analysis** – (optional) stores generated analysis summaries and messages  
- **privacy_settings** – language, basic privacy-related flags  

> The exact schema may evolve, but the overall structure follows this design.

---

## 🛠️ Tech Stack

### Mobile (Current Implementation)
- **Platform:** Android  
- **Language:** Java  
- **UI Layouts:** XML  
- **IDE:** Android Studio  

### Data Storage
- **SQLite** (on-device)  
- Custom `DatabaseHelper` using `SQLiteOpenHelper`

### Visualization
- **MPAndroidChart** (or similar) for line charts and visualizing mood trends

---

## 🔄 Core User Flow

1. User installs the app and opens it for the first time.  
2. User registers a new account or logs in with an existing one.  
3. User navigates to the **Daily Mood Check** screen.  
4. User answers the questionnaire and submits the form.  
5. The app:
   - Saves the data in the local SQLite database  
   - Runs on-device rule-based analysis  
   - Generates feedback/recommendations  
6. User can:
   - View today’s analysis and message  
   - Check charts for weekly/monthly trends  
   - Browse older entries in the history  
   - Edit profile information and language in settings  

---

## 📌 Project Status

- ✅ System analysis & basic design completed  
- ✅ Android application implemented with:
  - Local authentication  
  - Daily mood questionnaire  
  - On-device rule-based analysis  
  - Charts and history visualization  
  - Bilingual UI (Arabic/English)  
- 🔜 Future enhancements planned:
  - Remote backup / sync  
  - More advanced AI models (e.g., Naive Bayes, Decision Tree, or Deep Learning) via a secure backend  
  - Richer notification and reminder system  

---

## ⚠️ Disclaimer

This application is part of an **academic graduation project**.  
It is intended **for educational and self-awareness purposes only** and should **not** be used as a substitute for professional psychological diagnosis, treatment, or therapy.

---

## 👤 Developers / Contributors

*(Saleh Al-Shaebi)*

---


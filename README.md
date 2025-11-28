# 🧠📱 MentalHealthApp  
*A Mobile Application for Monitoring Mental Health Using Artificial Intelligence*

---

## 📌 Overview
**MentalHealthApp** is a mobile application designed to help users monitor their mental health daily using **AI-based analysis** of questionnaire responses.  
The app focuses on key psychological indicators such as **mood, stress, sleep quality, and energy level**, and provides users with **instant feedback, trends, and general recommendations**.

> ⚠️ *Note:* This application is intended for **self-awareness and wellbeing support only**.  
> It is **not a medical diagnostic or treatment tool**.

---

## ⭐ Main Features

### 🔐 User Authentication
- Secure registration and login  
- Password hashing and user activity tracking  

### 📋 Daily Mood Questionnaire
- Simple daily assessment  
- Covers mood, stress, sleep, and energy  
- Dynamic questions powered by database tables  

### 🤖 AI-Based Mood Analysis
- Uses **Naive Bayes** and **Decision Tree** algorithms  
- Classifies mental state (Calm, Stressed, Anxious, etc.)  
- Provides risk level + confidence score  
- Generates general recommendations  

### 📊 Mood Trends & History
- Weekly and monthly charts  
- Full history of past entries  
- Detailed breakdown per day  

### 💬 Personalized Recommendations
- Lifestyle suggestions  
- Self-help practices  
- AI-generated feedback  

### 🔔 Notifications & Reminders
- Optional reminders for daily check-ins  
- Alerts for new analysis and recommendations  

### 🛡️ Profile & Privacy
- Change language, notifications, and personal details  
- Control data-sharing preferences  

---

## 🏗️ System Architecture (High-Level)

### 📱 Mobile App (Client)
- Displays questionnaire  
- Sends responses  
- Receives AI analysis  
- Shows trends, history, and notifications  

### 🌐 Backend API
- Authentication (Login / Register)  
- Stores responses and analysis  
- Fetches questionnaire structure  
- Manages notifications and privacy settings  

### 🤖 AI Module
- Receives questionnaire data  
- Applies Naive Bayes & Decision Tree  
- Returns:  
  - Mood class  
  - Risk level  
  - Confidence  
  - Recommendation text  

### 🗄️ MySQL Database
Stores:
- Users  
- Questionnaires, Questions, Options  
- Mood Entries, Entry Answers  
- AI Analysis Results  
- Privacy Settings  
- Notifications  

---

## 🧠 AI Models Used

### 1) Naive Bayes
- Probabilistic classifier  
- Fast and efficient for questionnaire-type data  
- Works well with categorical or scaled inputs  

### 2) Decision Tree
- Rule-based, interpretable model  
- Shows the reasoning behind classification  
- Supports explainable AI  

**Together, they provide:**
- ⚡ Fast and efficient classification (Naive Bayes)  
- 📘 Clear explanation and reasoning (Decision Tree)

---

## 🗃️ Database Design (Simplified)

### Core Tables
- **users** – authentication & profile  
- **questionnaires** – defines questionnaire templates  
- **questions** – stores each question  
- **options** – multiple-choice options  
- **mood_entries** – daily form submissions  
- **entry_answers** – detailed answers per question  
- **ai_analysis** – classification, risk, recommendation  
- **privacy_settings** – language, notifications, permissions  
- **notifications** – messages/reminders  

> Full ERD documented in system design phase.

---

## 🛠️ Suggested Tech Stack

### Mobile:
- Android Kotlin / Java  

### Backend:
- Node.js (Express)  
- or Django REST / Laravel API  

### AI:
- Python (FastAPI)  
- scikit-learn models (Naive Bayes, Decision Tree)

### Database:
- MySQL / MariaDB  

---

## 🔄 Core User Flow

1. User registers or logs in.  
2. Opens **Daily Mood Check**.  
3. Answers questionnaire and submits.  
4. Backend saves data → Sends to AI module.  
5. AI analyzes responses and returns:  
   - 🧠 mood class  
   - ⚠️ risk level  
   - 📊 confidence score  
   - 💬 recommendation  
6. User can view:  
   - Today’s analysis  
   - Trends & charts  
   - Full history  
   - Notifications  
   - Profile & privacy settings  

---

## 📌 Project Status

- ✅ System analysis & UML design completed  
- ✅ Full database schema created  
- 🚧 Mobile app development in progress  
- 🚧 AI integration in progress  

---

## ⚠️ Disclaimer
This application is part of an **academic graduation project**.  
It is intended **for educational and self-awareness purposes only**.  
It must **not** replace professional psychological diagnosis, treatment, or therapy.

---

## 👤 Developers / Contributors
*(Add your names here)*

---


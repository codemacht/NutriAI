# NutriAI — AI-Powered Meal Planner

NutriAI is an Android mobile application built with Kotlin that helps users make smarter food choices using artificial intelligence.

The app allows users to enter available ingredients and receive AI-generated recipes, personalized meal plans, and nutrition recommendations powered by the Groq API (LLaMA 3.1 model).

---

## Features

### Authentication System
- User registration and login
- Local session management using SharedPreferences
- User data stored with Room Database

### AI Recipe Finder
- Enter ingredients available at home
- AI generates recipe suggestions
- Includes calories, cooking time, and instructions

### Meal Plan Generator
- Generate full 7-day meal plans
- Supports multiple diet types:
  - Balanced
  - Healthy
  - Bulk
  - Cut

### Favorites
- Save AI-generated recipes
- Delete saved recipes anytime

### Calorie Calculator
- Calculate daily calorie needs
- Protein, carbohydrates, and fat breakdown
- Based on weight, height, age, and fitness goals

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM |
| Navigation | Android Navigation Component |
| Database | Room Database |
| API | Groq API (LLaMA 3.1) |
| UI | Material Design 3 |
| Async Operations | Kotlin Coroutines |
| Session Management | SharedPreferences |

---

## Architecture

The project follows MVVM architecture with a clean separation of concerns.

- `data/` → API and database layer
- `ui/` → Fragments and ViewModels
- `repository/` → Handles data operations
- `navigation/` → Single Navigation Graph

---

## AI Integration

NutriAI integrates the Groq API with the LLaMA 3.1 8B Instant model to generate intelligent and personalized meal recommendations.

The AI can:
- Suggest recipes from ingredients
- Create meal plans
- Generate nutrition-focused responses

---

## Screens

- Login / Register
- Home
- Meal Planner
- Favorites
- Calorie Calculator

---

## Installation

1. Clone the repository

```bash
git clone https://github.com/codemacht/NutriAI.git
```

2. Open the project in Android Studio

3. Add your Groq API key

Create or edit `local.properties`:

```properties
GROQ_API_KEY=your_api_key_here
```

4. Run the application

---

## Future Improvements

- Dark mode support
- Cloud synchronization
- Barcode food scanner
- AI nutrition assistant
- Firebase authentication
- Multi-language support

---

## Author

Developed by Akbar Sobirjonov
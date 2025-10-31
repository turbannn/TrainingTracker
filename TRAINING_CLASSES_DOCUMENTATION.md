# Training and Exercise Classes Documentation

## ✅ Створено 2 класи для роботи з тренуваннями

---

## 📋 Class: `Exercise`

### Структура:
```kotlin
data class Exercise(
    val name: String = "",              // Назва вправи
    val duration: Int? = null,          // Тривалість в секундах (nullable)
    val sets: Int? = null,              // Кількість підходів (nullable)
    val repetitions: List<Int>? = null  // Масив повторень для кожного підходу (nullable)
)
```

### Особливості:
- ✅ Всі поля nullable (окрім name) - можна створювати різні типи вправ
- ✅ `repetitions` - це List<Int>, розмір має дорівнювати `sets`
- ✅ Сумісний з Firebase Firestore
- ✅ Додано корисні методи

### Методи:

#### `isValid(): Boolean`
Перевіряє, чи кількість повторень відповідає кількості підходів
```kotlin
val exercise = Exercise(
    name = "Push-ups",
    sets = 3,
    repetitions = listOf(10, 12, 15)
)
exercise.isValid() // true (3 sets = 3 repetitions)
```

#### `getFormattedDetails(): String`
Повертає відформатовану інформацію про вправу
```kotlin
// Приклад 1:
Exercise(name = "Plank", duration = 60).getFormattedDetails()
// Output: "60s"

// Приклад 2:
Exercise(name = "Push-ups", sets = 3, repetitions = listOf(10, 12, 15)).getFormattedDetails()
// Output: "3 sets | reps: 10, 12, 15"
```

---

## 📋 Class: `Training`

### Структура:
```kotlin
data class Training(
    @DocumentId
    val id: String = "",                    // Firestore document ID
    val isComplete: Boolean = false,         // Чи завершене тренування
    val trainingDate: Timestamp = Timestamp.now(),  // Unix timestamp
    val exercises: List<Exercise> = emptyList(),    // Список вправ
    val userId: String = ""                  // ID користувача
)
```

### Особливості:
- ✅ Використовує Firebase `Timestamp` для дати (Unix timestamp)
- ✅ `@DocumentId` автоматично заповнюється Firestore
- ✅ `userId` для зв'язку тренування з користувачем
- ✅ Багато корисних методів для відображення та обробки даних

### Методи:

#### `getFormattedDate(): String`
Повертає дату та час у форматі "DD/MM/YYYY HH:mm"
```kotlin
training.getFormattedDate() // "31/10/2024 14:30"
```

#### `getFormattedDateOnly(): String`
Повертає тільки дату у форматі "DD/MM/YYYY"
```kotlin
training.getFormattedDateOnly() // "31/10/2024"
```

#### `getExerciseCount(): Int`
Повертає кількість вправ у тренуванні
```kotlin
training.getExerciseCount() // 5
```

#### `getStatusText(): String`
Повертає статус тренування
```kotlin
training.getStatusText() // "Completed" або "In Progress"
```

#### `getTotalDuration(): Int?`
Обчислює загальну тривалість всіх вправ (у секундах)
```kotlin
training.getTotalDuration() // 600 (якщо є вправи з duration)
```

#### `getFormattedDuration(): String`
Повертає відформатовану тривалість
```kotlin
training.getFormattedDuration() // "10m 0s" або "N/A"
```

#### `markAsComplete(): Training`
Створює копію тренування зі статусом "завершено"
```kotlin
val completedTraining = training.markAsComplete()
```

#### `markAsIncomplete(): Training`
Створює копію тренування зі статусом "не завершено"
```kotlin
val incompleteTraining = training.markAsIncomplete()
```

---

## 🔥 Приклади використання

### Створення вправи:
```kotlin
// Вправа з підходами та повтореннями
val pushUps = Exercise(
    name = "Push-ups",
    sets = 3,
    repetitions = listOf(10, 12, 15)
)

// Вправа на час
val plank = Exercise(
    name = "Plank",
    duration = 60
)

// Комбінована вправа
val squats = Exercise(
    name = "Squats",
    sets = 4,
    repetitions = listOf(15, 15, 12, 10),
    duration = 120
)
```

### Створення тренування:
```kotlin
val training = Training(
    isComplete = false,
    trainingDate = Timestamp.now(),
    exercises = listOf(pushUps, plank, squats),
    userId = UserSession.getUserId() ?: ""
)
```

### Збереження в Firestore:
```kotlin
fun saveTraining(training: Training) {
    val firestore = FirebaseFirestore.getInstance()
    
    firestore.collection("trainings")
        .add(training)
        .addOnSuccessListener { documentReference ->
            Toast.makeText(context, "Training saved!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
```

### Завантаження тренувань користувача:
```kotlin
fun loadUserTrainings() {
    val firestore = FirebaseFirestore.getInstance()
    val userId = UserSession.getUserId()
    
    firestore.collection("trainings")
        .whereEqualTo("userId", userId)
        .orderBy("trainingDate", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { documents ->
            val trainings = documents.mapNotNull { 
                it.toObject(Training::class.java) 
            }
            // Використовуйте trainings
        }
}
```

### Оновлення статусу:
```kotlin
fun markTrainingAsComplete(trainingId: String) {
    FirebaseFirestore.getInstance()
        .collection("trainings")
        .document(trainingId)
        .update("isComplete", true)
        .addOnSuccessListener {
            Toast.makeText(context, "Training completed!", Toast.LENGTH_SHORT).show()
        }
}
```

---

## 📊 Структура даних в Firestore

### Collection: `trainings`

```json
{
  "id": "auto-generated-id",
  "isComplete": false,
  "trainingDate": {
    "_seconds": 1698764400,
    "_nanoseconds": 0
  },
  "userId": "user123",
  "exercises": [
    {
      "name": "Push-ups",
      "sets": 3,
      "repetitions": [10, 12, 15]
    },
    {
      "name": "Plank",
      "duration": 60
    },
    {
      "name": "Squats",
      "sets": 4,
      "repetitions": [15, 15, 12, 10]
    }
  ]
}
```

---

## 🎯 Варіанти використання

### 1. Вправа тільки на час:
```kotlin
Exercise(name = "Running", duration = 600) // 10 minutes
```

### 2. Вправа тільки з підходами:
```kotlin
Exercise(
    name = "Deadlift", 
    sets = 5, 
    repetitions = listOf(5, 5, 5, 3, 3)
)
```

### 3. Вправа без деталей (для запису):
```kotlin
Exercise(name = "Warm-up")
```

### 4. Комбінована вправа:
```kotlin
Exercise(
    name = "Burpees",
    duration = 300,  // 5 minutes
    sets = 5,
    repetitions = listOf(10, 10, 10, 10, 10)
)
```

---

## 🔍 Валідація даних

### Перевірка вправи:
```kotlin
val exercise = Exercise(
    name = "Pull-ups",
    sets = 3,
    repetitions = listOf(8, 6)  // Помилка: 3 sets, але тільки 2 repetitions
)

if (!exercise.isValid()) {
    println("Error: Repetitions count doesn't match sets!")
}
```

---

## 📱 Інтеграція з UI

### Відображення тренування:
```kotlin
// У RecyclerView або списку
textViewDate.text = training.getFormattedDate()
textViewStatus.text = training.getStatusText()
textViewExercises.text = "${training.getExerciseCount()} exercises"
textViewDuration.text = training.getFormattedDuration()
```

### Відображення вправи:
```kotlin
textViewExerciseName.text = exercise.name
textViewExerciseDetails.text = exercise.getFormattedDetails()
```

---

## ✅ Створені файли:

1. ✅ `Exercise.kt` - клас для вправ
2. ✅ `Training.kt` - клас для тренувань
3. ✅ `TrainingExamples.kt` - приклади використання
4. ✅ `TRAINING_CLASSES_DOCUMENTATION.md` - ця документація

---

## 🎉 Готово до використання!

Обидва класи готові для:
- ✅ Збереження в Firebase Firestore
- ✅ Відображення в UI
- ✅ Фільтрації та сортування
- ✅ Розширення додатковим функціоналом

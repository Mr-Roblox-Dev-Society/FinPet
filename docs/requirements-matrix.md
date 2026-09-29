# Матрица требований

Статусы: ✅ реализовано · 🟡 частично (прототип) · ⏳ документация/процесс

| # | Требование | Статус | Где |
|---|------------|--------|-----|
| 1 | Бесплатное просветительское приложение 7–11 | ✅ | README, RuStore |
| 2 | Только внутриигровая валюта, без платежей/рекламы/ПДн | ✅ | Manifest (нет INTERNET), security.md |
| 3 | Роли ребёнок / родитель + код | ✅ | RoleSelect, ParentLogin, Settings |
| 4 | Онбординг: 3 типа решений | ✅ | OnboardingScreen |
| 5 | Экономика: все суммы ТЗ | ✅ | EconomyConstants, GameEconomy, JSON |
| 6 | 15 работ, 3 сложности | ✅ | works.json, WorkScreen |
| 7 | Еда / лекарство / энергия / радость | ✅ | GameEconomy, Shop |
| 8 | Сезонная одежда + дизайны | ✅ | clothing.json, ClothingScreen |
| 9 | 9 питомцев, 3 стадии | ✅ | PetSpecies, evaluateStage |
| 10 | Период = день, неделя = 7, демо 5 | ✅ | advancePeriod, demoMode |
| 11 | План бюджета 3 направления | ✅ | BudgetScreen |
| 12 | Накопления 3 цели, мин 50, снятие с подтверждением | ✅ | SavingsScreen |
| 13 | ≥6 заданий, 3 темы | ✅ | tasks.json |
| 14 | Урок + тест, награда 25 | ✅ | lessons.json, LessonDetail |
| 15 | Главный экран: питомец, баланс, шкалы, цель, задание | ✅ | HomeScreen |
| 16 | Обратная связь после действия | ✅ | ActionFeedback, FeedbackCard |
| 17 | Раздел взрослого + барьер + сброс | ✅ | AdultGate, ParentScreen |
| 18 | Демо-режим | ✅ | Settings, demo-mode.md |
| 19 | Локальное сохранение Room | ✅ | FinnyDatabase |
| 20 | Android 8+, портрет, 360dp | ✅ | minSdk 26, portrait |
| 21 | Офлайн | ✅ | нет сетевых permission |
| 22 | Контент отделён от UI | ✅ | assets/content |
| 23 | MVVM + Clean | ✅ | architecture.md |
| 24 | UX доступность базово | ✅ | кнопки 52dp, текст 16sp, semantics |
| 25 | Тесты экономики | ✅ | GameEconomyTest |
| 26 | Подписанный APK | 🟡 | Инструкция в build.md (ключ у издателя) |
| 27 | Проверка на физ. устройстве 3ГБ | 🟡 | test-report.md (шаблон прогона) |
| 28 | RuStore карточка | ✅ | rustore.md + assets/store |
| 29 | Документация п.18 | ✅ | docs/* |

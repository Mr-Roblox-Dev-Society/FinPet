# ФинПет

Бесплатное просветительское Android-приложение для детей 7–11 лет.  
В игровой форме формирует базовые навыки управления личными финансами на основе **Единой рамки компетенций в области финансовой грамотности** (раздел 6).

**Валюта только внутриигровая.** Нет реальных денег, платежей, рекламы, подписок и сбора персональных данных.

---

## Быстрый старт

1. Установите **Android Studio Hedgehog (2023.1.1)+** или новее, JDK 17.
2. Откройте папку `FINANCIAL_GAME` как проект.
3. Дождитесь синхронизации Gradle.
4. Запустите конфигурацию **app** на эмуляторе API 26+ или физическом устройстве Android 8.0+.
5. Для демо: **Настройки → Демо-режим** (см. [docs/demo-mode.md](docs/demo-mode.md)).

Подробная сборка APK: [docs/build.md](docs/build.md).  
Сброс профиля: [docs/profile-reset.md](docs/profile-reset.md).

---

## Состав репозитория

| Путь | Назначение |
|------|------------|
| `app/` | Модуль Android-приложения (код, ресурсы, тесты) |
| `docs/` | Вся проектная документация |
| `assets/store/` | Материалы для RuStore (иконка, скриншоты-заготовки) |
| `LICENSE` | Лицензия проекта |
| `CHANGELOG.md` | История версий |
| `README.md` | Этот файл |

---

## Структура проекта (объяснение)

```
FINANCIAL_GAME/
├── app/src/main/java/ru/finny/pet/
│   ├── FinnyApp.kt              # Application, инициализация DI
│   ├── MainActivity.kt          # Точка входа UI
│   ├── di/                      # Простая ручная DI (без Hilt — меньше скрытой магии)
│   ├── data/                    # Слой данных
│   │   ├── local/               # Room: БД, DAO, Entity
│   │   ├── repository/          # Реализации репозиториев
│   │   └── mapper/              # Entity ↔ Domain
│   ├── domain/                  # Бизнес-логика (без Android UI)
│   │   ├── model/               # Модели домена
│   │   ├── economy/             # Правила экономики, формулы
│   │   ├── usecase/             # Сценарии использования
│   │   └── repository/          # Интерфейсы репозиториев
│   ├── content/                 # Загрузка учебного контента из assets
│   ├── ui/                      # Jetpack Compose
│   │   ├── theme/               # Цвета, типографика, тема
│   │   ├── components/          # Переиспользуемые UI-компоненты
│   │   ├── navigation/          # NavHost, маршруты
│   │   └── screens/             # Экраны по разделам
│   └── util/                    # Утилиты (код ребёнка, даты, демо)
├── app/src/main/assets/content/ # JSON: работы, задания, уроки, одежда, цели
├── app/src/test/                # Юнит-тесты domain/economy
└── docs/                        # Документация (см. ниже)
```

### Принципы разделения

- **Экономика** (`domain/economy`) не знает про Compose и Room.
- **Учебный контент** лежит в `assets/content/*.json` и читается через `content/`, UI только отображает.
- **Хранение** — только в `data/` (Room/SQLite).
- **UI-компоненты** в `ui/components` переиспользуются экранами.
- **Тесты** рядом: `app/src/test` (логика), `app/src/androidTest` (инструментальные).

---

## Роли

| Роль | Что делает |
|------|------------|
| **Ребёнок** | Создаёт питомца, играет, планирует бюджет, копит, учится |
| **Родитель** | Вводит код ребёнка (`FIN7K2`-подобный), смотрит прогресс и траты |

Регистрация не нужна. Всё хранится локально.

---

## Документация

1. [Архитектура](docs/architecture.md)
2. [Модель данных](docs/data-model.md)
3. [Экономика](docs/economy.md)
4. [Карта образовательного контента](docs/content-map.md)
5. [Матрица требований](docs/requirements-matrix.md)
6. [Тест-кейсы](docs/test-cases.md)
7. [Отчёт о тестировании](docs/test-report.md)
8. [UX/UI](docs/ux-ui.md)
9. [Доступность](docs/accessibility.md)
10. [Разрешения Android](docs/permissions.md)
11. [Безопасность](docs/security.md)
12. [Ограничения](docs/limitations.md)
13. [План развития](docs/roadmap.md)
14. [Лицензии сторонних материалов](docs/third-party-licenses.md)
15. [Сборка](docs/build.md)
16. [Демо-режим](docs/demo-mode.md)
17. [Сброс профиля](docs/profile-reset.md)
18. [RuStore](docs/rustore.md)
19. [Презентация](docs/presentation.md)
20. [Руководство администратора контента](docs/admin-guide.md)

---

## Технологии

- Kotlin, Jetpack Compose, Navigation Compose
- Room (SQLite), Coroutines / Flow
- MVVM + Clean Architecture (data / domain / ui)
- minSdk 26 (Android 8.0), targetSdk 34
- Портретная ориентация, ширина от 360 dp
- Имя пакета: `ru.finny.pet`

---

## Лицензия

См. [LICENSE](LICENSE). Сторонние библиотеки — [docs/third-party-licenses.md](docs/third-party-licenses.md).

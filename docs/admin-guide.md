# Руководство администратора контента

Цель: менять задания, цены, одежду и уроки **без переписывания** `GameEconomy` / UI.

## Где лежит контент

`app/src/main/assets/content/`

| Файл | Что править |
|------|-------------|
| `works.json` | 15 работ: название, описание, difficulty (`EASY`/`MEDIUM`/`HARD`), isPhysical |
| `shop_and_goals.json` | Товары магазина и цели накоплений |
| `clothing.json` | Сезонные предметы и варианты цветов/дизайнов |
| `tasks.json` | Игровые задания |
| `lessons.json` | Теория и тесты |

После правок — пересобрать приложение (assets упаковываются в APK).

## Добавить задание

1. Скопируйте объект в массиве `tasks`.
2. Уникальный `id` (например `t_budget_3`).
3. `theme`: `BUDGET` | `SAVINGS` | `PAYMENTS`.
4. `type`: `CHOICE` | `DISTRIBUTE` | `PICK_PURCHASE` | `SAVE_AMOUNT`.
5. В `options` ровно один `isCorrect: true` (или логика, которую проверяет текущий UI по выбранному optionId).
6. Заполните `correctExplanation` и `wrongExplanation` простым языком.
7. Добавьте строку в `docs/content-map.md`.

## Изменить цены

- Магазин: поле `price` в `shop_and_goals.json`.
- Одежда база: `basePrice`; варианты: `price` у variant.
- **Системные константы** (вход 100, налог 150, мин. перевод 50, награда теста 25, оплата работ) заданы в коде `EconomyConstants` / `WorkDifficulty` — меняйте согласованно с документацией `economy.md`, иначе разойдутся тесты и ТЗ.

## Новый дизайн / цвет одежды

В нужном item в `clothing.json` добавьте variant:

```json
{"id": "summer_tshirt_6", "title": "Радуга", "colorHex": "#FF00AA", "price": 40, "isBase": false}
```

`isBase: true` только у одного варианта на предмет. UI подхватит список автоматически.

## Обновить урок

В `lessons.json`: `theoryText`, вопросы с `correctIndex` (0-based), `miniActionHint`, `explanation`.  
Награда за идеальный тест остаётся 25 (`EconomyConstants.GOOD_TEST_REWARD`).

## Новая цель накоплений

В `goals` добавьте `{ "id": "goal_4", "title": "...", "targetAmount": 2500 }`.  
Активная цель по умолчанию — `goal_1` в профиле; для смены логики активности потребуется небольшая правка UI/интерактора.

## Чего не делать в JSON

- Не менять имена enum-значений (`MANDATORY`, `SUMMER`, …) без правки Kotlin.
- Не удалять id, на которые уже ссылаются сохранения пользователей (лучше deprecatе + скрыть).
- Не класть персональные данные и внешние URL для ребёнка.

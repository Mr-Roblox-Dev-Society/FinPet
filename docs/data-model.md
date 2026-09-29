# Модель данных

## ChildProfile (домен)

| Поле | Тип | Описание |
|------|-----|----------|
| id | Long | Всегда 1 на устройстве (один профиль) |
| childCode | String | Код вида FIN7K2 |
| petName | String | Имя питомца |
| species | PetSpecies | 9 видов |
| stage | PetStage | BABY / TEEN / ADULT |
| stageReason | String | Объяснение стадии ребёнку |
| balance | Int | Баланс монет |
| savingsBalance | Int | Копилка |
| periodIndex | Int | Номер игрового периода |
| weekPeriod | Int | 1…7 внутри недели |
| stats | PetStats | hunger, energy, joy, isSick, lastFed/Entertainment |
| currentPlan | BudgetPlan? | mandatory / optional / savings |
| lastFact | PeriodFact? | Факт трат периода |
| activeGoalId | String? | Текущая цель |
| wardrobe | List\<OwnedClothingVariant\> | Купленные и надетые вещи |
| completedTaskIds | Set\<String\> | Пройденные задания |
| completedLessonIds | Set\<String\> | Пройденные уроки |
| ownedShopItemIds | Set\<String\> | Купленные игрушки |
| dailyLoginClaimedPeriod | Int | За какой период взят вход |
| demoMode | Boolean | Демо |
| manualSeason | Season? | Ручной сезон в демо |
| soundEnabled / animationsEnabled | Boolean | Доступность |
| createdAtEpochMs | Long | Создание |

## Room

- `profiles` — сериализация профиля (шкалы отдельно, гардероб JSON, CSV id).
- `spend_log` — журнал трат для родителя.
- `app_meta` — служебные ключи.

## Контент (assets/content)

- `works.json` — 15 работ.
- `shop_and_goals.json` — магазин + 3 цели.
- `clothing.json` — сезонная одежда и варианты.
- `tasks.json` — 6 заданий.
- `lessons.json` — 3 урока с тестами.

## Связи

Профиль ссылается на контент по строковым id (`goal_1`, `w01`, `t_budget_1`…). Контент неизменяем в рантайме (меняется файлами, см. admin-guide).

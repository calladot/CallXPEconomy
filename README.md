# CallXPEconomy

Плагін Paper для використання досвіду Minecraft як валюти через [Vault](https://www.spigotmc.org/resources/vault.34315/). Баланс зберігається як ціла кількість XP і синхронізується з досвідом гравця під час входу, зміни XP та виходу із сервера.

## Можливості

- Реалізація Vault Economy API для балансу досвіду гравців.
- Підтримка SQLite (типово) і YAML-сховища.
- Автоматичне створення облікового запису гравця.
- Налаштовувані назви валюти в однині та множині.
- Безпечні операції поповнення і списання без від'ємних балансів.

## Вимоги

- Сервер Paper 1.21 або новіший.
- Java 21.
- [Vault](https://www.spigotmc.org/resources/vault.34315/) для реєстрації економічного провайдера.

Без Vault плагін запускається, але не надає економічний API іншим плагінам.

## Встановлення

1. Завантажте останній `CallXPEconomy-<version>.jar` зі сторінки [Releases](../../releases).
2. Помістіть JAR у каталог `plugins/` сервера Paper.
3. Встановіть Vault у той самий каталог.
4. Перезапустіть сервер.
5. За потреби змініть `plugins/CallXPEconomy/config.yml` і знову перезапустіть сервер.

## Налаштування

```yaml
storage:
  # Допустимі значення: sqlite, yaml
  type: sqlite
  sqlite:
    file: accounts.db
  yaml:
    file: accounts.yml

currency:
  singular: XP
  plural: XP

logging:
  # Консольний аудит поповнень, списань і відхилених Vault-запитів.
  vault:
    # true - журналювати для всіх гравців.
    all-players: false
    # Ніки для журналювання, коли all-players: false. Регістр не враховується.
    players: []
  # Консольний аудит синхронізації досвіду з обліковим записом.
  xp-sync:
    # true - журналювати для всіх гравців.
    all-players: false
    # Ніки для журналювання, коли all-players: false. Регістр не враховується.
    players: []
```

SQLite зберігає дані у файлі `accounts.db`; YAML-варіант - у `accounts.yml`. Обидва файли створюються в каталозі плагіна. Валютні операції приймають лише невід'ємні цілі значення; банківські рахунки Vault не підтримуються.

### Консольне логування

`logging.vault` і `logging.xp-sync` налаштовуються незалежно. Для кожної категорії встановіть `all-players: true`, щоб бачити події всіх гравців, або залиште його `false` і перелічіть потрібні ніки в `players`. Порівняння нікнеймів не враховує регістр. Якщо `all-players` має значення `false`, а `players` порожній, категорію вимкнено. Після зміни конфігурації перезапустіть сервер. Зміна ніка гравця потребує оновлення списку.

`logging.vault` виводить успішні поповнення та списання XP, а також відхилені запити: некоректну суму, недостатній баланс або помилку сховища. Кожний запис містить тип дії, нік, суму, результат і баланс. Наприклад:

```text
[CallXPEconomy] Vault withdraw successful: amount=25, balance=340, player=Calladot
[CallXPEconomy] Vault withdraw rejected: amount=500, reason=insufficient XP, balance=340, player=Calladot
```

`logging.xp-sync` виводить створення або відновлення облікового запису під час входу, збереження зміненого ігрового XP та збереження під час виходу. Записи містять джерело синхронізації, нік і загальну кількість XP. Незмінений баланс не зберігається і не потрапляє до журналу, тому операція Vault не створює зайвий запис синхронізації.

```text
[CallXPEconomy] XP sync join restored: player=Calladot, balance=340
[CallXPEconomy] XP sync XP change persisted: player=Calladot, balance=347
[CallXPEconomy] XP sync quit persisted: player=Calladot, balance=347
```

## Розробка

```bash
./gradlew build
```

Або використовуйте цілі Makefile:

```bash
make build  # зібрати, протестувати та створити JAR
make test   # запустити тести
make jar    # створити лише JAR
```

Версію за замовчуванням задає змінна `VERSION` у [Makefile](Makefile). Її можна перевизначити під час запуску:

```bash
make VERSION=0.1.4 jar
```

Готовий самодостатній JAR створюється в `build/libs/`.

# GigaChat CLI на Kotlin

Простой CLI-чат на Kotlin с подключением к **GigaChat API**.

Проект реализует консольный интерфейс для общения с моделью, хранит историю сообщений в памяти и позволяет во время работы менять **модель**, **temperature** и **system prompt**.

---

## Что реализовано

Ниже — основные возможности, соответствующие заданию:

- подключение к **GigaChat API**
- ввод текста с консоли
- отправка запроса в API
- вывод ответа в консоль
- хранение истории диалога в памяти
- смена модели: **Lite / Pro / Max**
- смена **temperature**
- смена **system prompt**

---

## Стек

Проект собран на **Kotlin/JVM** и использует:

- **Kotlin 1.9.24**
- **JDK 17**
- **OkHttp** для HTTP-запросов
- **Jackson** для JSON

---

## Структура проекта

Ниже — базовая структура:

```text
gigachat-cli/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── lessons_1.md
└── src/
    └── main/
        └── kotlin/
            ├── Main.kt
            ├── CliChat.kt
            ├── GigaChatClient.kt
            └── Models.kt
```

---

## Требования для запуска

Перед запуском должны быть установлены:

- **JDK 17**
- доступ в интернет
- ключ авторизации для GigaChat API

Проверка Java:

```bash
java -version
```

Ожидается **Java 17**.

---

## Переменные окружения

Для запуска используется переменная:

- `GIGACHAT_AUTH_KEY` — Base64 от строки `clientId:clientSecret`

Пример:

```bash
export GIGACHAT_AUTH_KEY='ВАШ_BASE64_КЛЮЧ'
```

### Дополнительно

В проекте поддержан флаг:

- `GIGACHAT_UNSAFE_SSL=true`

Он включает небезопасный SSL-режим для локальной отладки, если JVM не доверяет TLS-цепочке OAuth endpoint.

Пример:

```bash
export GIGACHAT_UNSAFE_SSL=true
```

> Этот режим нужен только для локальной диагностики и разработки.

---

## Сборка проекта

Сборка через Gradle:

```bash
./gradlew clean installDist
```

После успешной сборки исполняемый скрипт будет находиться здесь:

```bash
./build/install/gigachat-cli/bin/gigachat-cli
```

---

## Запуск

Ниже — пример полного запуска:

```bash
export GIGACHAT_AUTH_KEY='ВАШ_BASE64_КЛЮЧ'
export GIGACHAT_UNSAFE_SSL=true
./gradlew clean installDist
./build/install/gigachat-cli/bin/gigachat-cli
```

Если `unsafe SSL` не нужен, можно запускать без него:

```bash
export GIGACHAT_AUTH_KEY='ВАШ_BASE64_КЛЮЧ'
./gradlew clean installDist
./build/install/gigachat-cli/bin/gigachat-cli
```

---

## Использование CLI

После запуска приложение выводит текущее состояние и список команд.

### Пример старта

```text
Starting GigaChat CLI...
Unsafe SSL mode: true
GigaChat CLI Chat
Model: GigaChat, Temp: 0.87, System: "Ты полезный AI-ассистент."
Commands:
/model Lite|Pro|Max
/temp <0..2>
/system <text>
/history
/help
/quit
You:
```

### Обычное сообщение

Любой текст без префикса `/` отправляется в GigaChat:

```text
You: Hello
GigaChat: Hello! How can I assist you today?
```

---

## Команды

Ниже — команды, которые поддерживает CLI:

| **Команда** | **Описание** |
|---|---|
| `/model Lite` | переключить модель на Lite |
| `/model Pro` | переключить модель на Pro |
| `/model Max` | переключить модель на Max |
| `/temp 1.2` | установить temperature |
| `/system You are a strict teacher` | изменить system prompt |
| `/history` | показать историю диалога |
| `/help` | показать список команд |
| `/quit` | выйти из программы |

Ниже — короткий пример реальной сессии:

```text
You: Hello
GigaChat: Hello! How can I assist you today?

You: /model Max
Model switched to GigaChat-Max
Model: GigaChat-Max, Temp: 0.87, System: "Ты полезный AI-ассистент."

You: /temp 1
Temperature set to 1.0
Model: GigaChat-Max, Temp: 1.0, System: "Ты полезный AI-ассистент."

You: /system be a serious teacher
System prompt updated
Model: GigaChat-Max, Temp: 1.0, System: "be a serious teacher"

You: /history
History:
1. user: Hello
2. assistant: Hello! How can I assist you today?
```

---

## Как хранится история

История диалога хранится **в памяти** в виде массива сообщений:

- сообщения пользователя добавляются в `history`
- ответы модели тоже добавляются в `history`
- при каждом новом запросе отправляется:
    - текущий `system prompt`
    - вся накопленная история

Это соответствует требованию задания: **история в памяти (просто массив)**.

---

## Как работает выбор модели

В CLI используются алиасы:

- `Lite`
- `Pro`
- `Max`

Внутри они сопоставляются с реальными именами моделей, которые возвращает GigaChat API, например:

- `Lite` → `GigaChat` или `GigaChat-2`
- `Pro` → `GigaChat-Pro` или `GigaChat-2-Pro`
- `Max` → `GigaChat-Max` или `GigaChat-2-Max`

Это сделано потому, что API может возвращать реальные `model id`, отличающиеся от коротких пользовательских названий.

---

## Кодировка консоли

Для корректной работы ввода/вывода приложение запускается с UTF-8 JVM-параметрами:

```kotlin
applicationDefaultJvmArgs = listOf(
    "-Dfile.encoding=UTF-8",
    "-Dsun.stdout.encoding=UTF-8",
    "-Dsun.stderr.encoding=UTF-8"
)
```

Ввод из консоли читается через `BufferedReader` + `InputStreamReader(System.in, Charsets.UTF_8)`.

Это сделано для предотвращения ошибок кодировки при интерактивном вводе.

---

## Возможные проблемы

Ниже — несколько типичных проблем и причин.

### 1. Не задан `GIGACHAT_AUTH_KEY`

Ошибка:

```text
Environment variable GIGACHAT_AUTH_KEY is not set
```

Решение:

```bash
export GIGACHAT_AUTH_KEY='ВАШ_BASE64_КЛЮЧ'
```

### 2. Ошибка TLS / SSL

Если локальная JVM не доверяет сертификатной цепочке OAuth endpoint, можно временно включить:

```bash
export GIGACHAT_UNSAFE_SSL=true
```

> Это временный `dev-only` workaround.

### 3. Приложение не читает ввод через `gradlew run`

Для интерактивного режима надёжнее использовать собранный launch script:

```bash
./build/install/gigachat-cli/bin/gigachat-cli
```

а не:

```bash
./gradlew run
```

---

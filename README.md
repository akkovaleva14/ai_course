# GigaChat CLI на Kotlin

Простой CLI-чат на Kotlin с подключением к **GigaChat API**.

Проект реализует консольный интерфейс для общения с моделью, хранит историю сообщений в памяти и позволяет во время работы менять **модель**, **temperature** и **system prompt**.  
Дополнительно реализованы отдельные demo-режимы для:

- **structured output** с разбором JSON в Kotlin data class
- **function calling** с нативным вызовом локальной функции через формат **GigaChat API**

---

## Что реализовано

Проект поддерживает:

- подключение к **GigaChat API**
- ввод текста с консоли
- отправку запроса в API
- вывод ответа в консоль
- хранение истории диалога в памяти
- смену модели: **Lite / Pro / Max**
- смену **temperature**
- смену **system prompt**
- demo-сценарий для **structured output**
- demo-сценарий для **function calling**

---

## Стек

Проект собран на **Kotlin/JVM** и использует:

- **Kotlin 1.9.24**
- **JDK 17**
- **Gradle 8.10.2**
- **OkHttp** для HTTP-запросов
- **Jackson** для JSON

---

## Структура проекта

Базовая структура проекта:

```text
gigachat-cli/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── homework-2-task1.md
├── lessons_1.md
└── src/
    └── main/
        └── kotlin/
            ├── Main.kt
            ├── CliChat.kt
            ├── GigaChatClient.kt
            ├── Models.kt
            ├── StructuredOutputDemo.kt
            └── FunctionCallingDemo.kt
```

> Допустимо, если часть demo-кода находится не в отдельных файлах, а в уже существующих Kotlin-файлах. Главное, чтобы режимы запуска работали корректно.

---

## Требования для запуска

Перед запуском должны быть установлены:

- **JDK 17**
- доступ в интернет
- ключ авторизации для GigaChat API

Проверка Java:

```bash
java --version
./gradlew -version
```

Ожидается, что и `java`, и `Gradle JVM` используют **Java 17**.

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

## Режимы запуска

Проект поддерживает три основных режима:

- `chat` — интерактивный CLI-чат
- `structured-output` — demo structured output
- `function-calling` — demo function calling

---

## Запуск интерактивного CLI

Для интерактивного чата рекомендуется использовать **launch script**, собранный через `installDist`:

```bash
export GIGACHAT_AUTH_KEY='ВАШ_BASE64_КЛЮЧ'
export GIGACHAT_UNSAFE_SSL=true
./gradlew clean installDist
./build/install/gigachat-cli/bin/gigachat-cli
```

Если `unsafe SSL` не нужен:

```bash
export GIGACHAT_AUTH_KEY='ВАШ_BASE64_КЛЮЧ'
./gradlew clean installDist
./build/install/gigachat-cli/bin/gigachat-cli
```

Этот режим был проверен и работает корректно.

### Пример вывода при старте

```text
Starting GigaChat app...
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

---

## Альтернативный запуск через Gradle

Можно использовать запуск через Gradle:

```bash
./gradlew run --args="chat"
```

или:

```bash
./gradlew run --args="structured-output"
```

или:

```bash
./gradlew run --args="function-calling"
```

Но есть важное отличие:

- `./gradlew run --args="chat"` в текущем окружении **не поддерживает интерактивный ввод** и завершает программу после старта
- `./gradlew run --args="structured-output"` работает корректно
- `./gradlew run --args="function-calling"` работает корректно

Поэтому для обычного CLI-чата рекомендуется использовать именно:

```bash
./build/install/gigachat-cli/bin/gigachat-cli
```

---

## Structured Output Demo

Для демонстрации structured output реализован отдельный режим запуска:

```bash
./gradlew run --args="structured-output"
```

В этом режиме приложение:

1. отправляет **несколько тестовых пользовательских отзывов** в GigaChat
2. просит модель вернуть ответ в виде **строгого JSON**
3. при необходимости использует **defensive fallback** для извлечения JSON из raw response
4. парсит ответ в Kotlin data class
5. валидирует результат
6. печатает результат в структурированном виде

### Что именно демонстрирует этот режим

Structured output в проекте реализован как полный цикл:

- **system prompt** задаёт жёсткую JSON-схему ответа
- модель возвращает JSON-объект
- приложение извлекает JSON из ответа модели
- JSON десериализуется в Kotlin-структуру
- результат проходит базовую валидацию и выводится в читаемом виде

Это демонстрирует подход **prompt → JSON → объект → читаемый вывод**.

### Схема результата

Для structured output используются enum-поля и Kotlin data class:

- `sentiment`: `positive | neutral | negative`
- `category`: `ui | stability | performance | billing | notifications | other`
- `priority`: `low | medium | high`
- `summary`: краткое резюме отзыва

### Пример ответа модели

```json
{
  "sentiment": "negative",
  "category": "stability",
  "priority": "high",
  "summary": "Зависание и вылеты после обновления"
}
```

### Пример вывода приложения

Ниже — пример реального вывода режима `structured-output`:

```text
Starting GigaChat app...
Unsafe SSL mode: true
Running structured output demo...
Model: GigaChat
Example 1:
Review:
После последнего обновления приложение зависает на экране логина и иногда вылетает.

Raw model response:
{
  "sentiment": "negative",
  "category": "stability",
  "priority": "high",
  "summary": "Зависание и вылеты приложения после обновления"
}

Parsed result:
  Sentiment : negative
  Category  : stability
  Priority  : high
  Summary   : Зависание и вылеты приложения после обновления

--------------------------------------------------

Example 2:
Review:
Уведомления иногда приходят с задержкой, но в целом приложение работает нормально.

Raw model response:
{
  "sentiment": "neutral",
  "category": "notifications",
  "priority": "medium",
  "summary": "Задержка уведомлений"
}

Parsed result:
  Sentiment : neutral
  Category  : notifications
  Priority  : medium
  Summary   : Задержка уведомлений

--------------------------------------------------

Example 3:
Review:
Очень понравился новый интерфейс, всё стало заметно удобнее.

Raw model response:
{
  "sentiment": "positive",
  "category": "ui",
  "priority": "low",
  "summary": "Положительный отзыв о новом удобном интерфейсе"
}

Parsed result:
  Sentiment : positive
  Category  : ui
  Priority  : low
  Summary   : Положительный отзыв о новом удобном интерфейсе

--------------------------------------------------
```

### Важное замечание

Даже при строгом system prompt модель иногда может вернуть JSON с markdown-обёрткой или с лишним текстом.  
Поэтому в проекте используется функция defensive extraction: приложение сначала **просит строгий JSON**, а затем при необходимости **извлекает JSON из raw response**.

Это не заменяет prompt, а делает интеграцию более устойчивой.

---

## Function Calling Demo

Для демонстрации function calling реализован отдельный режим запуска:

```bash
./gradlew run --args="function-calling"
```

В этом режиме приложение показывает **нативный полный цикл function calling через GigaChat API**:

1. пользователь задаёт вопрос
2. приложение отправляет запрос с описанием функции в поле `functions`
3. запрос отправляется с `function_call: "auto"`
4. модель сама решает, нужен ли вызов функции
5. если модель возвращает `finish_reason = "function_call"`, приложение извлекает `message.function_call`
6. приложение выполняет локальную Kotlin-функцию
7. результат функции сериализуется в JSON-строку и отправляется обратно модели как сообщение роли `function`
8. модель формирует финальный ответ пользователю

### Что именно демонстрирует этот режим

Function calling в проекте реализован через формат GigaChat API, а не через имитацию JSON в prompt.

В demo используется локальная функция:

```text
getWeather(city: String)
```

Она возвращает мок-данные о погоде в структурированном виде, например:

```json
{
  "city": "Москва",
  "temperatureC": -2,
  "condition": "snow",
  "precipitation": "snow",
  "windSpeedMs": 5
}
```

### Пример описания функции

В запрос передаётся описание функции в формате GigaChat API:

```json
[
  {
    "name": "getWeather",
    "description": "Возвращает текущую погоду по названию города",
    "parameters": {
      "type": "object",
      "properties": {
        "city": {
          "type": "string",
          "description": "Название города"
        }
      },
      "required": ["city"]
    }
  }
]
```

### Пример ответа модели на первом шаге

Если модель решает вызвать функцию, она возвращает `finish_reason = "function_call"` и структуру вызова функции:

```json
{
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "",
        "function_call": {
          "name": "getWeather",
          "arguments": {
            "city": "Москва"
          }
        }
      },
      "finish_reason": "function_call"
    }
  ]
}
```

### Пример вывода приложения

Ниже — пример реального вывода режима `function-calling`:

```text
Starting GigaChat app...
Unsafe SSL mode: true
Running function calling demo...
Model: GigaChat
User: Какая погода в Москве?

Raw first response:
{
  "choices" : [ {
    "message" : {
      "role" : "assistant",
      "content" : "",
      "function_call" : {
        "name" : "getWeather",
        "arguments" : {
          "city" : "Москва"
        }
      }
    },
    "finish_reason" : "function_call"
  } ]
}

Model requested function call: getWeather(city=Москва)

Function result:
{"city":"Москва","temperatureC":-2,"condition":"snow","precipitation":"snow","windSpeedMs":5}

Final answer:
Сейчас в Москве температура −2°C, идёт снег, ветрено (скорость ветра около 11 м/с).
```

### Важное замечание

В этой реализации function calling используется **реальный API-механизм**:

- функция описывается в `functions`
- запрос отправляется с `function_call: "auto"`
- приложение проверяет `finish_reason == "function_call"`
- аргументы функции берутся из `message.function_call.arguments`
- результат функции возвращается модели как JSON-строка
- финальный ответ генерирует модель

Это соответствует требованию задания: **вопрос → решение модели → вызов функции → возврат результата → финальный ответ**.

---

## Использование CLI

После запуска приложение выводит текущее состояние и список команд.

### Обычное сообщение

Любой текст без префикса `/` отправляется в GigaChat:

```text
You: what is the weather in moscow today?
GigaChat: У меня нет доступа к реальному времени или интернету, чтобы предоставить актуальную информацию о погоде.
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

Ниже — краткий итог по этой таблице: CLI поддерживает смену модели, температуры, system prompt и просмотр истории прямо во время работы.

### Пример сессии

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

Для корректной работы вывода приложение запускается с UTF-8 JVM-параметрами:

```kotlin
applicationDefaultJvmArgs = listOf(
    "-Dfile.encoding=UTF-8",
    "-Dsun.stdout.encoding=UTF-8",
    "-Dsun.stderr.encoding=UTF-8"
)
```

Ввод из консоли читается через `BufferedReader` + `InputStreamReader(System.in, Charsets.UTF_8)`.

Это уменьшает риск проблем с кодировкой в интерактивном режиме.

---

## Возможные проблемы

Ниже — несколько типичных проблем и способов решения.

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

### 3. `./gradlew run --args="chat"` сразу завершает программу

В текущем окружении Gradle не передаёт интерактивный `stdin` так, как ожидает CLI. В этом случае используйте:

```bash
./gradlew clean installDist
./build/install/gigachat-cli/bin/gigachat-cli
```

### 4. Gradle запускается не на той версии Java

Проверьте:

```bash
java --version
./gradlew -version
```

Если Gradle использует не Java 17, настройте `JAVA_HOME` в `.zshrc` или зафиксируйте JDK через `gradle.properties`.

### 5. Structured output не парсится в JSON

Если модель вернула JSON с markdown-обёрткой или с лишним текстом, приложение использует defensive extraction для извлечения JSON из raw response.

Проверьте, что в system prompt явно указано:

- отвечать строго в JSON
- не использовать markdown
- не добавлять пояснения
- не добавлять текст до или после JSON

Также проверьте, что модель вернула поля в ожидаемой схеме:

- `sentiment`
- `category`
- `priority`
- `summary`

### 6. Structured output не проходит парсинг в Kotlin object

Проверьте, что значения enum-полей совпадают с ожидаемыми:

- `sentiment`: `positive`, `neutral`, `negative`
- `category`: `ui`, `stability`, `performance`, `billing`, `notifications`, `other`
- `priority`: `low`, `medium`, `high`

Если модель вернула другие значения, Jackson не сможет корректно десериализовать ответ в `ReviewClassification`.

### 7. Function calling demo не срабатывает

Проверьте:

- что запускается режим `function-calling`
- что запрос отправляется с `functions` и `function_call: "auto"`
- что модель вернула `finish_reason = "function_call"`
- что в `message.function_call` есть `name` и `arguments`
- что результат локальной функции отправляется обратно как JSON-строка
- что второй запрос содержит сообщение роли `function`

Если API возвращает ошибку 422, обычно проблема в формате результата функции или в структуре сообщений второго запроса.

---

## Что сделано по домашнему заданию

В рамках homework 2 реализованы:

1. **CLI chat с GigaChat API**
    - ввод текста с консоли
    - хранение истории в памяти
    - смена модели, temperature и system prompt во время работы

2. **Задание 1: Prompting Techniques**
    - добавлен файл `homework-2-task1.md`
    - выбрана одна задача
    - применены 5 техник промптинга
    - для каждой техники показаны before / after и вывод

3. **Задание 2: Structured Output**
    - добавлен demo-режим `structured-output`
    - модель возвращает JSON по заданной схеме
    - JSON при необходимости извлекается из raw response
    - JSON парсится в Kotlin data class с enum-полями
    - результат валидируется и выводится в читаемом виде

4. **Задание 3: Function Calling**
    - добавлен demo-режим `function-calling`
    - функция описывается через `functions` в формате GigaChat API
    - запрос отправляется с `function_call: "auto"`
    - приложение проверяет `finish_reason == "function_call"`
    - приложение извлекает `message.function_call`
    - вызывается локальная функция `getWeather(city)`
    - результат сериализуется в JSON и отправляется обратно модели
    - модель формирует финальный ответ

---
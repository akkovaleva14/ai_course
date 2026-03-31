import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherArguments(
    val city: String? = null
)

data class WeatherResult(
    val city: String,
    val temperatureC: Int?,
    val condition: String,
    val precipitation: String? = null,
    val windSpeedMs: Int? = null
)

class WeatherService {
    fun getWeather(city: String): WeatherResult {
        return when (city.trim().lowercase()) {
            "москва", "moscow" -> WeatherResult(
                city = "Москва",
                temperatureC = -2,
                condition = "snow",
                precipitation = "snow",
                windSpeedMs = 5
            )

            "санкт-петербург", "saint petersburg", "st. petersburg", "saint-petersburg" -> WeatherResult(
                city = "Санкт-Петербург",
                temperatureC = 0,
                condition = "rain",
                precipitation = "rain",
                windSpeedMs = 7
            )

            "казань", "kazan" -> WeatherResult(
                city = "Казань",
                temperatureC = -5,
                condition = "clear",
                precipitation = "none",
                windSpeedMs = 3
            )

            "новосибирск", "novosibirsk" -> WeatherResult(
                city = "Новосибирск",
                temperatureC = -8,
                condition = "cloudy",
                precipitation = "none",
                windSpeedMs = 4
            )

            else -> WeatherResult(
                city = city,
                temperatureC = null,
                condition = "unknown",
                precipitation = null,
                windSpeedMs = null
            )
        }
    }
}

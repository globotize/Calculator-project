package com.example.model

data class UnitCategory(
    val name: String,
    val units: List<UnitItem>
)

data class UnitItem(
    val name: String,
    val symbol: String,
    val toBaseFactor: Double = 1.0, // multiplier to convert to base unit
    val isTemperature: Boolean = false
)

object UnitConverterData {
    val CATEGORIES: List<UnitCategory> = listOf(
        UnitCategory(
            name = "Length",
            units = listOf(
                UnitItem("Meter (base)", "m", 1.0),
                UnitItem("Kilometer", "km", 1000.0),
                UnitItem("Centimeter", "cm", 0.01),
                UnitItem("Millimeter", "mm", 0.001),
                UnitItem("Mile", "mi", 1609.344),
                UnitItem("Yard", "yd", 0.9144),
                UnitItem("Foot", "ft", 0.3048),
                UnitItem("Inch", "in", 0.0254),
                UnitItem("Nautical Mile", "nmi", 1852.0)
            )
        ),
        UnitCategory(
            name = "Mass",
            units = listOf(
                UnitItem("Kilogram (base)", "kg", 1.0),
                UnitItem("Gram", "g", 0.001),
                UnitItem("Milligram", "mg", 0.000001),
                UnitItem("Metric Ton", "t", 1000.0),
                UnitItem("Pound", "lb", 0.45359237),
                UnitItem("Ounce", "oz", 0.028349523)
            )
        ),
        UnitCategory(
            name = "Temperature",
            units = listOf(
                UnitItem("Celsius", "°C", isTemperature = true),
                UnitItem("Fahrenheit", "°F", isTemperature = true),
                UnitItem("Kelvin", "K", isTemperature = true)
            )
        ),
        UnitCategory(
            name = "Speed",
            units = listOf(
                UnitItem("Meters/sec (base)", "m/s", 1.0),
                UnitItem("Km/hour", "km/h", 0.277777778),
                UnitItem("Miles/hour", "mph", 0.44704),
                UnitItem("Knot", "kn", 0.514444444)
            )
        ),
        UnitCategory(
            name = "Pressure",
            units = listOf(
                UnitItem("Pascal (base)", "Pa", 1.0),
                UnitItem("Kilopascal", "kPa", 1000.0),
                UnitItem("Bar", "bar", 100000.0),
                UnitItem("Atmosphere", "atm", 101325.0),
                UnitItem("PSI", "psi", 6894.757),
                UnitItem("Torr / mmHg", "mmHg", 133.322)
            )
        ),
        UnitCategory(
            name = "Data Storage",
            units = listOf(
                UnitItem("Byte (base)", "B", 1.0),
                UnitItem("Kilobyte", "KB", 1024.0),
                UnitItem("Megabyte", "MB", 1024.0 * 1024.0),
                UnitItem("Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
                UnitItem("Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
            )
        )
    )

    fun convert(value: Double, from: UnitItem, to: UnitItem): Double {
        if (from.isTemperature || to.isTemperature) {
            // Convert 'from' to Celsius
            val celsius = when (from.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            // Convert Celsius to 'to'
            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> (celsius * 9.0 / 5.0) + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        val baseValue = value * from.toBaseFactor
        return baseValue / to.toBaseFactor
    }
}

package com.example.lutautosync.processing

import java.io.File

data class Lut3d(val size: Int, val values: FloatArray)

object LutParser {
    fun parse(file: File): Lut3d {
        val rows = file.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") && it.firstOrNull()?.isDigit() == true }
        val size = file.readLines().firstOrNull { it.trim().startsWith("LUT_3D_SIZE") }?.trim()?.split("\\s+")?.last()?.toIntOrNull() ?: 2
        val values = FloatArray(size * size * size * 3)
        rows.flatMap { it.split("\\s+").take(3).map(String::toFloat) }.take(values.size).forEachIndexed { i, v -> values[i] = v }
        return Lut3d(size, values)
    }
}

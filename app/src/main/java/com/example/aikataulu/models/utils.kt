package com.example.aikataulu.models

import com.example.aikataulu.DeparturesForStopIdQuery
import java.lang.StringBuilder

val newLine = System.lineSeparator()

fun timeToString(timestamp: Int?): String {
    if (timestamp == null) return "??:??"
    val h = timestamp / (60 * 60)
    val m = (timestamp - (h * 60 * 60)) / 60

    return "${toDoubleDigitString(h)}:${toDoubleDigitString(m)}"
}

fun toDoubleDigitString(i: Int?): String {
    if (i == null) return "??"
    val j = i.toInt()
    return if (j >= 10) j.toString() else "0${j}"
}

fun formatDepartures(departures: ArrayList<DeparturesForStopIdQuery.StoptimesWithoutPattern>): String {
    val sb = StringBuilder()
    departures.forEach { sb.append("${Departure(it)}$newLine") }
    sb.setLength(kotlin.math.max(sb.length - 1, 0)) // Trim last newLine
    return sb.toString()
}
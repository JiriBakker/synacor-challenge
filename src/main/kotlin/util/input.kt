package util

import java.io.File

fun readInputLines(day: String, year: Int): List<String> {
    return File("input/$year/$day").readLines()
}

fun readInputLine(day: String, year: Int): String {
    return readInputLines(day, year).single()
}

fun parseCsv(exampleInput: String): List<String> {
    return exampleInput.split(",")
}

fun List<String>.splitByDoubleNewLine(): List<List<String>> {
    val groups = mutableListOf<List<String>>()
    var group = mutableListOf<String>()

    this.forEach { line ->
        if (line.isEmpty()) {
            groups.add(group)
            group = mutableListOf()
        } else {
            group.add(line)
        }
    }

    if (group.isNotEmpty()) {
        groups.add(group)
    }

    return groups
}

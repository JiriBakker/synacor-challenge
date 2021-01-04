package util

fun String.countLetters(): Map<Char, Int> {
    val counts = mutableMapOf<Char, Int>()
    this.forEach { char ->
        counts[char] = counts.getOrDefault(char, 0) + 1
    }
    return counts
}

fun List<String>.countLetters(): Map<Char, Int> {
    val counts = mutableMapOf<Char, Int>()
    this.forEach { line ->
        line.forEach { char ->
            counts[char] = counts.getOrDefault(char, 0) + 1
        }
    }
    return counts
}
package util

class BinarySearchRange(var min: Long, var max: Long) {
    fun median(): Long {
        return ((max - min) / 2) + min
    }
    fun consolidateMin() {
        min = median() + 1
    }
    fun consolidateMax() {
        max = median() - 1
    }
}

fun doBinarySearch(range: BinarySearchRange, target: Long, compute: (Long) -> Long): Long {
    while (range.max >= range.min) {
        val value = compute(range.median())
        when {
            value == target -> return range.median()
            value > target  -> range.consolidateMax()
            else            -> range.consolidateMin()
        }
    }
    return range.median()
}
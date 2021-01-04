package util

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun Long.toDigits(): List<Int> {
    return this.toString().map(Character::getNumericValue)
}

fun List<Int>.joinToLong(): Long {
    return this.joinToString("").toLong()
}

fun Long.firstDigit(): Int {
    return Character.getNumericValue(this.toString().first())
}

fun pythDistance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
    return pythDistance(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
}

fun pythDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    return sqrt((x1 - x2).pow(2.0) + (y1 - y2).pow(2.0))
}

fun manhattanDistance(x1: Long, y1: Long, x2: Long, y2: Long): Long {
    return abs(x2 - x1) + abs(y2 - y1)
}

fun greatestCommonDenominator(a: Long, b: Long): Long = if (b == 0L) a else greatestCommonDenominator(b, a % b)
fun leastCommonMultiple(a: Long, b: Long): Long = a / greatestCommonDenominator(a, b) * b
fun leastCommonMultiple(nrs: Collection<Long>): Long = nrs.reduce(::leastCommonMultiple)

fun Int.normalize(): Int {
    return if (this == 0) 0 else this / abs(this)
}

fun Int.safeMod(mod: Int): Int {
    return this.toLong().safeMod(mod.toLong()).toInt()
}

fun Long.safeMod(mod: Long): Long {
    var result = this
    do {
        result = (result + mod) % mod
    } while (result < 0)
    return result
}

fun min(vararg ints: Int): Int {
    return ints.min()!!
}

fun max(vararg ints: Int): Int {
    return ints.max()!!
}

operator fun BigInteger.rem(m: Long): BigInteger = this.mod(BigInteger.valueOf(m))
operator fun BigInteger.times(other: Long): BigInteger = this.times(BigInteger.valueOf(other))
operator fun BigInteger.times(other: Int): BigInteger = this.times(BigInteger.valueOf(other.toLong()))
operator fun BigInteger.plus(other: Int): BigInteger = this.plus(BigInteger.valueOf(other.toLong()))
operator fun Int.minus(other: BigInteger): BigInteger = BigInteger.valueOf(this.toLong()).minus(other)
fun BigInteger.modPow(e: Long, m: Long): BigInteger = this.modPow(BigInteger.valueOf(e), BigInteger.valueOf(m))
fun BigInteger.modInverse(m: Long): BigInteger = this.modInverse(BigInteger.valueOf(m))
fun Long.modInverse(m: Long): BigInteger = BigInteger.valueOf(this).modInverse(BigInteger.valueOf(m))
fun Int.modInverse(m: Long): BigInteger = BigInteger.valueOf(this.toLong()).modInverse(BigInteger.valueOf(m))
fun Long.pow(e: Long): Long = this.toDouble().pow(e.toDouble()).toLong()
fun Long.sqrt(): Long = sqrt(this.toDouble()).toLong()
fun Int.sqrt(): Int = sqrt(this.toDouble()).toInt()

fun Int.gt(other: Int): Boolean = this > other
fun Int.gte(other: Int): Boolean = this >= other
fun Int.lt(other: Int): Boolean = this < other
fun Int.lte(other: Int): Boolean = this <= other
fun Int.notEquals(other: Int): Boolean = this != other

fun BigInteger.asBinaryString(length: Int = 32): String = this.toString(2).padStart(length, '0')
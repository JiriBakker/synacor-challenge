package synacor

import kotlin.system.measureTimeMillis

private fun time(func: () -> Unit) {
    val millis = measureTimeMillis { func() }
    println(" (took $millis ms)")
}

fun main() {
    val vm = VirtualMachine()
    try {
        time { vm.execute() }
    } catch (e: Exception) {}
}

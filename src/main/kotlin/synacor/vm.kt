package synacor

import java.io.File
import java.math.BigInteger
import java.util.Stack

private class State(
    var cursor: Int,
    val memory: MutableList<Int>,
    val registers: MutableMap<Int, Int>,
    val stack: Stack<Int>,
    val outputBuffer: StringBuilder,
    val inputBuffer: ArrayDeque<Int>
) {

    constructor() : this(0, mutableListOf(), mutableMapOf(), Stack<Int>(), StringBuilder(), ArrayDeque()) {
        (32768..32775).forEach { i -> registers[i] = 0 }

        memory.addAll(readSynacorInput().toList().chunked(2).map { (low, high) -> toInt(low, high) })
        while (memory.size < 32768) { memory.add(0) }
    }

    private fun readSynacorInput(): ByteArray {
        return File("input/synacor/challenge.bin").readBytes()
    }

    private fun toInt(low: Byte, high: Byte): Int {
        val bits = "${high.toUByte().toString(2).padStart(8, '0')}${low.toUByte().toString(2).padStart(8, '0')}"
        return bits.toInt(2)
    }

    fun get(offset: Int): Int {
        return memory[cursor + offset]
    }

    fun step(delta: Int): State {
        cursor += delta
        return this
    }

    fun jump(index: Int): State {
        cursor = index
        return this
    }

    fun clone(): State {
        return State(
            cursor + 0,
            memory.toMutableList(),
            registers.toMutableMap(),
            stack.clone() as Stack<Int>,
            StringBuilder(outputBuffer),
            ArrayDeque(inputBuffer)
        )
    }

    fun getOutput(): String {
        val output = outputBuffer.toString()
        outputBuffer.clear()
        outputBuffer.append(output)
        return output
    }

    fun clearOutput() {
        outputBuffer.clear()
    }
}

class VirtualMachine {

    private enum class Operation(val nr: Int) {
        // halt: 0
        //   stop execution and terminate the program
        HALT(0),
        // set: 1 a b
        //   set register <a> to the value of <b>
        SET(1),
        // push: 2 a
        //   push <a> onto the state.stack
        PUSH(2),
        // pop: 3 a
        //   remove the top element from the state.stack and write it into <a>; empty state.stack = error
        POP(3),
        // eq: 4 a b c
        //   set <a> to 1 if <b> is equal to <c>; set it to 0 otherwise
        EQUALS(4),
        // gt: 5 a b c
        //   set <a> to 1 if <b> is greater than <c>; set it to 0 otherwise
        GREATERTHAN(5),
        // jmp: 6 a
        //   jump to <a>
        JUMP(6),
        // jt: 7 a b
        //   if <a> is nonzero, jump to <b>
        JUMPTRUE(7),
        // jf: 8 a b
        //   if <a> is zero, jump to <b>
        JUMPFALSE(8),
        // add: 9 a b c
        //   assign into <a> the sum of <b> and <c> (modulo 32768)
        ADD(9),
        // mult: 10 a b c
        //   store into <a> the product of <b> and <c> (modulo 32768)
        MULTIPLY(10),
        // mod: 11 a b c
        //   store into <a> the remainder of <b> divided by <c>
        MOD(11),
        // and: 12 a b c
        //   stores into <a> the bitwise and of <b> and <c>
        AND(12),
        // or: 13 a b c
        //   stores into <a> the bitwise or of <b> and <c>
        OR(13),
        // not: 14 a b
        //   stores 15-bit bitwise inverse of <b> in <a>
        NOT(14),
        // rmem: 15 a b
        //   read state.memory at address <b> and write it to <a>
        READMEMORY(15),
        // wmem: 16 a b
        //   write the value from <b> into state.memory at address <a>
        WRITEMEMORY(16),
        // call: 17 a
        //   write the address of the next instruction to the state.stack and jump to <a>
        CALL(17),
        // ret: 18
        //   remove the top element from the state.stack and jump to it; empty state.stack = halt
        RETURN(18),
        // out: 19 a
        //   write the character represented by ascii code <a> to the terminal
        OUT(19),
        // in: 20 a
        //   read a character from the terminal and write its ascii code to <a>; it can be assumed that once input starts, it will continue until a newline is encountered; this means that you can safely read whole lines from the keyboard and trust that they will be fully read
        IN(20),
        // noop: 21
        //   no operation
        NOOP(21);


        companion object {
            fun fromInt(value: Int): Operation {
                return values().firstOrNull { value == it.nr }
                    ?: error("Unknown operation: $value")
            }
        }
    }

    private fun Int.getRegisterOrValue(state: State): Int {
        return state.registers[this] ?: this
    }

    fun execute() {
        var state = State()
        try {
            while (state.cursor in state.memory.indices) {
                state = step(state)
            }
        } finally {
            println("${state.outputBuffer}")
        }
    }

    private val states = mutableListOf<State>()

    private val script = ArrayDeque(File("input/synacor/start-script").readLines())

    fun addToScript(commands: List<String>) {
        script.addAll(commands)
    }

    private fun rewind(delta: Int): State {
        repeat(delta) { states.removeLast() }
        val state = states.removeLast()
        state.inputBuffer.clear()
        return state
    }

    private fun step(state: State): State {
        val operation = Operation.fromInt(state.get(0))
        return when (operation) {
            Operation.HALT -> {
                println(state.getOutput())
                println("ERROR! Program was going to terminate. Rewinding..")
                rewind(0)
            }
            Operation.SET -> {
                state.registers[state.get(1)] = state.get(2).getRegisterOrValue(state)
                state.step(3)
            }
            Operation.PUSH -> {
                state.stack.push(state.get(1).getRegisterOrValue(state))
                state.step(2)
            }
            Operation.POP -> {
                state.registers[state.get(1)] = state.stack.pop()
                state.step(2)
            }
            Operation.EQUALS -> {
                state.registers[state.get(1)] =
                    if (state.get(2).getRegisterOrValue(state) == state.get(3).getRegisterOrValue(state)) 1
                    else 0
                state.step(4)
            }
            Operation.GREATERTHAN -> {
                state.registers[state.get(1)] =
                    if (state.get(2).getRegisterOrValue(state) > state.get(3).getRegisterOrValue(state)) 1
                    else 0
                state.step(4)
            }
            Operation.JUMP -> state.jump(state.get(1).getRegisterOrValue(state))
            Operation.JUMPTRUE -> {
                val value = state.get(1).getRegisterOrValue(state)
                if (value != 0) {
                    state.jump(state.get(2).getRegisterOrValue(state))
                } else {
                    state.step(3)
                }
            }
            Operation.JUMPFALSE -> {
                val value = state.get(1).getRegisterOrValue(state)
                if (value == 0) {
                    state.jump(state.get(2).getRegisterOrValue(state))
                } else {
                    state.step(3)
                }
            }
            Operation.ADD -> {
                state.registers[state.get(1)] = (state.get(2).getRegisterOrValue(state) + state.get(3).getRegisterOrValue(state)) % 32768
                state.step(4)
            }
            Operation.MULTIPLY -> {
                state.registers[state.get(1)] = (state.get(2).getRegisterOrValue(state) * state.get(3).getRegisterOrValue(state)) % 32768
                state.step(4)
            }
            Operation.MOD -> {
                state.registers[state.get(1)] = (state.get(2).getRegisterOrValue(state) % state.get(3).getRegisterOrValue(state)) % 32768
                state.step(4)
            }
            Operation.AND -> {
                state.registers[state.get(1)] = (state.get(2).getRegisterOrValue(state) and state.get(3).getRegisterOrValue(state)) % 32768
                state.step(4)
            }
            Operation.OR -> {
                state.registers[state.get(1)] = (state.get(2).getRegisterOrValue(state) or state.get(3).getRegisterOrValue(state)) % 32768
                state.step(4)
            }
            Operation.NOT -> {
                var inverse = BigInteger.valueOf(state.get(2).getRegisterOrValue(state).toLong())
                (0 until 15).forEach { inverse = inverse.flipBit(it) }
                state.registers[state.get(1)] = inverse.toInt()
                state.step(3)
            }
            Operation.READMEMORY -> {
                state.registers[state.get(1)] = state.memory[state.get(2).getRegisterOrValue(state)]!!
                state.step(3)
            }
            Operation.WRITEMEMORY -> {
                state.memory[state.get(1).getRegisterOrValue(state)] = state.get(2).getRegisterOrValue(state)
                state.step(3)
            }
            Operation.CALL -> {
                state.stack.push(state.cursor + 2)
                state.jump(state.get(1).getRegisterOrValue(state))
            }
            Operation.RETURN -> {
                if (state.stack.isEmpty()) {
                    println(state.getOutput())
                    println("ERROR! Program was going to terminate. Rewinding..")
                    rewind(0)
                } else {
                    state.jump(state.stack.pop())
                }
            }
            Operation.OUT -> {
                val char = state.get(1).getRegisterOrValue(state).toChar()
                state.outputBuffer.append(char)
                state.step(2)
            }
            Operation.IN -> {
                if (state.getOutput().isNotEmpty()) {
                    println(state.getOutput())
                }

                var curState = state
                if (curState.inputBuffer.isEmpty()) {
                    states.add(curState.clone())

                    var input: String
                    if (script.isNotEmpty()) {
                        input = script.removeFirst()
                        println("Manual action -> $input")
                    } else {
                        input = readLine()!!
                        if (input.startsWith("rewind ")) {
                            val delta = input.drop(7).toInt()
                            curState = rewind(delta)
                            input = "look"
                        }
                    }

                    if (input == "halt") {
                        error("Manually halted")
                    }

                    input.forEach { curState.inputBuffer.addLast(it.toInt()) }
                    curState.inputBuffer.addLast('\n'.toInt())
                }

                curState.clearOutput()

                curState.registers[curState.get(1)] = curState.inputBuffer.removeFirst()
                curState.step(2)
            }
            Operation.NOOP -> state.step(1)
        }
    }


}
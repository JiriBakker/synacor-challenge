package synacor

import java.io.File
import java.math.BigInteger
import java.util.Stack

private fun readSynacorInput(): ByteArray {
    return File("input/synacor/challenge.bin").readBytes()
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
        //   push <a> onto the stack
        PUSH(2),
        // pop: 3 a
        //   remove the top element from the stack and write it into <a>; empty stack = error
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
        //   read memory at address <b> and write it to <a>
        READMEMORY(15),
        // wmem: 16 a b
        //   write the value from <b> into memory at address <a>
        WRITEMEMORY(16),
        // call: 17 a
        //   write the address of the next instruction to the stack and jump to <a>
        CALL(17),
        // ret: 18
        //   remove the top element from the stack and jump to it; empty stack = halt
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

    }

    private val memory = mutableListOf<Int>()
    private val registers = mutableMapOf<Int, Int>()
    private val stack = Stack<Int>()

    private val outputBuffer = StringBuilder()
    private val inputBuffer = ArrayDeque<Int>()

    init {
        (32768..32775).forEach { i -> registers[i] = 0 }

        memory.addAll(readSynacorInput().toList().chunked(2).map { (low, high) -> toInt(low, high) })
        while (memory.size < 32768) { memory.add(0) }
    }

    private fun Int.toOperation(): Operation {
        return Operation.values().firstOrNull { this == it.nr } ?: error("Unknown operation: $this")
    }

    private fun Int.getRegisterOrValue(): Int {
        return registers[this] ?: this
    }

    private fun toInt(low: Byte, high: Byte): Int {
        val bits = "${high.toUByte().toString(2).padStart(8, '0')}${low.toUByte().toString(2).padStart(8, '0')}"
        //println("$bits = ${bits.toInt(2)}")
        return bits.toInt(2)
    }


    fun execute() {
        var cursor = 0
        try {
            while (cursor in memory.indices) {
                cursor = step(cursor)
                //println("Cursor moved to $cursor")
            }
        } finally {
            println("$outputBuffer")
        }
    }

    private fun step(cursor: Int): Int {
        val operation = memory[cursor].toOperation()
        //println("Next operation: $operation")
        return when (operation) {
            Operation.HALT -> -1
            Operation.SET -> {
                registers[memory[cursor + 1]] = memory[cursor + 2].getRegisterOrValue()
                cursor + 3
            }
            Operation.PUSH -> {
                stack.push(memory[cursor + 1].getRegisterOrValue())
                cursor + 2
            }
            Operation.POP -> {
                registers[memory[cursor + 1]] = stack.pop()
                cursor + 2
            }
            Operation.EQUALS -> {
                registers[memory[cursor + 1]] =
                    if (memory[cursor + 2].getRegisterOrValue() == memory[cursor + 3].getRegisterOrValue()) 1
                    else 0
                cursor + 4
            }
            Operation.GREATERTHAN -> {
                registers[memory[cursor + 1]] =
                    if (memory[cursor + 2].getRegisterOrValue() > memory[cursor + 3].getRegisterOrValue()) 1
                    else 0
                cursor + 4
            }
            Operation.JUMP -> memory[cursor + 1].getRegisterOrValue()
            Operation.JUMPTRUE -> {
                val value = memory[cursor + 1].getRegisterOrValue()
                if (value != 0) {
                    memory[cursor + 2].getRegisterOrValue()
                } else {
                    cursor + 3
                }
            }
            Operation.JUMPFALSE -> {
                val value = memory[cursor + 1].getRegisterOrValue()
                if (value == 0) {
                    memory[cursor + 2].getRegisterOrValue()
                } else {
                    cursor + 3
                }
            }
            Operation.ADD -> {
                registers[memory[cursor + 1]] = (memory[cursor + 2].getRegisterOrValue() + memory[cursor + 3].getRegisterOrValue()) % 32768
                cursor + 4
            }
            Operation.MULTIPLY -> {
                registers[memory[cursor + 1]] = (memory[cursor + 2].getRegisterOrValue() * memory[cursor + 3].getRegisterOrValue()) % 32768
                cursor + 4
            }
            Operation.MOD -> {
                registers[memory[cursor + 1]] = (memory[cursor + 2].getRegisterOrValue() % memory[cursor + 3].getRegisterOrValue()) % 32768
                cursor + 4
            }
            Operation.AND -> {
                registers[memory[cursor + 1]] = (memory[cursor + 2].getRegisterOrValue() and memory[cursor + 3].getRegisterOrValue()) % 32768
                cursor + 4
            }
            Operation.OR -> {
                registers[memory[cursor + 1]] = (memory[cursor + 2].getRegisterOrValue() or memory[cursor + 3].getRegisterOrValue()) % 32768
                cursor + 4
            }
            Operation.NOT -> {
                var inverse = BigInteger.valueOf(memory[cursor + 2].getRegisterOrValue().toLong())
                (0 until 15).forEach { inverse = inverse.flipBit(it) }
                registers[memory[cursor + 1]] = inverse.toInt()
                cursor + 3
            }
            Operation.READMEMORY -> {
                registers[memory[cursor + 1]] = memory[memory[cursor + 2].getRegisterOrValue()]!!
                cursor + 3
            }
            Operation.WRITEMEMORY -> {
                memory[memory[cursor + 1].getRegisterOrValue()] = memory[cursor + 2].getRegisterOrValue()
                cursor + 3
            }
            Operation.CALL -> {
                stack.push(cursor + 2)
                memory[cursor + 1].getRegisterOrValue()
            }
            Operation.RETURN -> {
                if (stack.isEmpty()) {
                    -1
                } else {
                    stack.pop()
                }
            }
            Operation.OUT -> {
                val char = memory[cursor + 1].getRegisterOrValue().toChar()
                outputBuffer.append(char)
                cursor + 2
            }
            Operation.IN -> {
                if (outputBuffer.isNotEmpty()) {
                    println(outputBuffer.toString())
                    outputBuffer.clear()
                }

                if (inputBuffer.isEmpty()) {
                    val input = readLine()!!
                    input.forEach { inputBuffer.addLast(it.toInt()) }
                    inputBuffer.addLast('\n'.toInt())
                }

                registers[memory[cursor + 1]] = inputBuffer.removeFirst()
                cursor + 2
            }
            Operation.NOOP -> cursor + 1
        }
    }
}
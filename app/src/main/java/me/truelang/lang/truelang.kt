package me.truelang.lang

import me.truelang.AtomItem
import me.truelang.indexOf
import me.truelang.indexOfInverse
import kotlin.math.max
import kotlin.math.min

fun main() {
    val data = addBrackets("1+1*2*(2+4)*3 + (12+14/2)*2")
    println(data)
    println((1 + ((((((1 * 2)) * (2 + 4))) * 3)) + (((12 + ((14 / 2))) * 2))))

    interpretMath(addBrackets(data))
}

fun addBrackets(data: String): String {
    var source = if (data.first() != '(') {
        StringBuilder("($data)")
    } else {
        StringBuilder(data)
    }

    var i = 0
    val highOperationsSet = setOf('*', '/')
    val lowOperationsSet = setOf('+', '-')

    var bracketsCount = 0
    while (i < source.length - 1) {
        if (highOperationsSet.contains(source[i])) {
            bracketsCount = 0
            var leftIndex = i
            while (true) {
                if (bracketsCount == 0
                    && (source[leftIndex - 1] == '('
                            || lowOperationsSet.contains(source[leftIndex - 1])
                            || highOperationsSet.contains(source[leftIndex - 1])
                            )
                ) {
                    source.insert(leftIndex, '(')
                    i++
                    println("left: $source")
                    break
                }
                if (source[leftIndex - 1] == ')') {
                    bracketsCount += 1
                } else if (source[leftIndex - 1] == '(') {
                    bracketsCount -= 1
                }
                leftIndex = leftIndex - 1
            }

            bracketsCount = 0
            var rightIndex = i
            while (true) {
                if (bracketsCount == 0 &&
                    (source[rightIndex + 1] == ')'
                            || lowOperationsSet.contains(source[rightIndex + 1])
                            || highOperationsSet.contains(source[rightIndex + 1])
                            )
                ) {
                    source.insert(rightIndex + 1, ')')
                    println("right: " + source)
                    break
                }
                if (source[rightIndex + 1] == '(') {
                    bracketsCount += 1
                } else if (source[rightIndex + 1] == ')') {
                    bracketsCount -= 1
                }
                rightIndex = rightIndex + 1
            }
            i = rightIndex + 1
        }
        // NOTE: какой-то нужен нам интерпретатор прям таких штук чтобы они показывали результат на лету типа превью в Compose
        i++
    }

    return source.toString()
}

fun String.indexOfLast(c: Char): Int {
    var index = 0
    while (true) {
        val newIndex = this.indexOf(c, startIndex = index)
        if (newIndex == -1) {
            return index
        }
        index = newIndex + 1
    }
    return -1
}

fun interpretMath(data: String): String {
    if (data.isEmpty()) {
        return data
    }

    var source = data
    var result = data

    val startIndex = source.indexOfLast('(')
    val endIndex = source.indexOf(')', startIndex = startIndex)

    result = source.substring(startIndex, endIndex)

    val calc = calc(result)
    val newSource = source.replaceRange(startIndex - 1, endIndex + 1, "$calc")
    println(result)
    println(newSource)

    return if (newSource.indexOf('(') == -1) {
        newSource
    } else {
        interpretMath(newSource)
    }
}

fun calc(source: String): Double {
    println("calc")
    val atoms = mutableListOf<String>()

    var number = ""
    source.replace("L", "").replace("l", "").forEach {
        when (it) {
            '*', '/', '+', '-' -> {
                println(number)
                atoms.add(number.trim())
                number = ""
                atoms.add("$it")
            }

            else -> {
                number = "$number$it"
            }
        }
    }
    atoms.add(number.trim())
    println(atoms)
    var result = 0.0

    if (atoms.size == 1) {
        return atoms[0].toDouble()
    }

    val atom = atoms[1]
    fun action(operation: String, a: Double, b: Double): Double = when {
        operation == "*" -> a * b
        operation == "/" -> a / b
        operation == "+" -> a + b
        operation == "-" -> a - b
        operation.isDigitsOnly('.') -> result
        else -> throw IllegalArgumentException(atom)
    }

    result = action(atom, atoms[0].toDouble(), atoms[2].toDouble())

    for (i in 2 until atoms.size) {
        val atom = atoms[i]
        when (atom) {
            "*", "/", "+", "-" -> {
                println(atoms)
                println("debug: $atom | $result | ${i + 1} ")
                result = action(atom, result, atoms[i + 1].toDouble())
            }
        }

    }

    return result
}

fun interpretCode(transformation: String, result: String): String {
    println("interpretCode:  $transformation -> $result")
    return when (transformation) {
        "print" -> {
            "print: ${
                interpretMath(
                    addBrackets(
                        result.replace("println(", "").replace(")", "")
                    )
                )
            }"
        }

        "add", "multiply" -> {

            interpretMath(addBrackets(result))
        }
        else -> ""
    }
}

var transformationsSource = """
        |:a * $:b = multiply
        
        println(|) = print
        println($:string) = printNext
        
        |:a + $:b = add
        
        |:a + $:b * 12 + $:c = func1
        
        Text(text = $:text) = Text
        Button(content = $:content, onClick = $:onClick) = Button
       
""".trimIndent()

fun fillTransformations(): MutableMap<String, String> {
    var transformationsMap = mutableMapOf<String, String>() // name, body
    var index = transformationsSource.indexOf('=')
    var rightIndex = 0

    while (true) {
        val leftIndex = max(0, transformationsSource.indexOfInverse('\n', startIndex = index))
        rightIndex = min(
            transformationsSource.length,
            transformationsSource.indexOf('\n', startIndex = index)
        )
        val body = transformationsSource.substring(leftIndex, index).trim()
        val name = transformationsSource.substring(index + 1, rightIndex).trim()

        transformationsMap.put(name, body)

        transformationsSource = transformationsSource.replaceRange(leftIndex, rightIndex, "")
        index = transformationsSource.indexOf('=', startIndex = 0)

        if (index < 0) {
            break
        }
    }
    return transformationsMap
}

val transformationsMap = fillTransformations()
fun transformAtomsChain(atomItems: List<AtomItem>): String {
    val endOfChainStr = ""

    var codeBlock = ""
    val dataItems = mutableListOf<String>()
    var nextAtomsGets = 0
    for (i in atomItems.indices) {
        if (nextAtomsGets > 0) {
            nextAtomsGets -= 1
            continue
        }
        val atom = atomItems[i].data.trim()
        if (atom == endOfChainStr) {
            if (dataItems.isNotEmpty()) {
                codeBlock += "${dataItems.last()}\n"
                dataItems.clear()
            }
        }

        if (!transformationsMap.contains(atom)) {
            if (!atom.isEmpty() && !atom.startsWith("//")) {
                dataItems.add(atom)
            }

        } else {
            val template = transformationsMap[atom]
            var newData = "$template"
            var index = newData.indexOf('$')
            val totalCount = newData.count { it == '$' }

            var counter = 1
            while (index != -1) {

                val dataIndex = dataItems.size - 1 - (totalCount - counter)
                if (dataIndex >= 0) {
                    newData = newData.replaceRange(
                        index,
                        index + 1,
                        dataItems[dataIndex].trim()
                    )
                } else {
                    val nextAtom = atomItems[i + 1].data.trim()
                    nextAtomsGets += 1
                    //dataItems.add(nextAtom)
                    newData = newData.replaceRange(
                        index,
                        index + 1,
                        nextAtom.trim()
                    )
                }
                index = newData.indexOf('$', startIndex = index + 1)
                counter += 1
            }

            println("newData: $newData")
            dataItems.add(newData)
        }
    }
    if (dataItems.isNotEmpty()) {
        codeBlock += "${dataItems.last()}\n"
    }

    return codeBlock
}

fun String.isDigitsOnly(withChar: Char): Boolean {
    val len = this.length
    var cp: Int
    var i = 0
    while (i < len) {
        cp = Character.codePointAt(this, i)
        if (!Character.isDigit(cp) && this[i] != withChar) {
            return false
        }
        i += Character.charCount(cp)
    }
    return true
}
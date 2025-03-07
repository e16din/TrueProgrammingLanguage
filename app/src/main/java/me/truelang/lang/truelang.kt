package me.truelang.lang

import me.truelang.LineItem
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

var defaultDharmaTemplates = """
        // primitive types examples
        string constant = string
        1 = int
        1l = long
        1.0 = double
        true = boolean
        1,2,3 = array, list
        1,a; 2,b; 3,c; = dictionary, map
        
        # message
        
        true != false -> printNext it is true 
        
        yes
        |: == yes -> print Success
       
        
        // то что уже есть в цепочке 
        |: = |: 
        
        // то что ожидается на ввод
        $: = $: 
        
        
        
        // :chain: - все линии в цепочке
        :chain: = chainName
        
        5
        `:start: = = = :chain: = \| = =` = unhandledString
        ":start: = = = :chain: = \| = = 2" = unhandledString
        
        |:a * $:b = multiply, *
        |:a + $:b = add, +
        
        println(|) = printIt
        println($:string) = printNext
        
        // | - 1  - дает пару контейнер и выбранное .container, .selected (last, first, get, set)
        (| - 1).selected.last = getNext
        |.count = count, size, length
        | size * getNext = foreach
        | foreach printIt = printAll 

        
        :start: import compose.Text
        Text $:text = Text
        Button $:text $:onClick = Button
        
//        $:lets_start
        
//        1. 10 // $:a a
//        2. + // name
//        3. 12 // $:b b
//        4. printIt // console: 30
//        5. = func1 
//        
//        1. 10 + 12 printIt = func2
        
                            
        // long click on dharma template -> interpretator with selected dharma chain 
       
""".trimIndent()

fun fillTransformations(): MutableMap<String, String> {
    var transformationsMap = mutableMapOf<String, String>() // name, body
    var index = defaultDharmaTemplates.indexOf('=')
    var rightIndex = 0

    while (true) {
        val leftIndex = max(0, defaultDharmaTemplates.indexOfInverse('\n', startIndex = index))
        rightIndex = min(
            defaultDharmaTemplates.length,
            defaultDharmaTemplates.indexOf('\n', startIndex = index)
        )
        val body = defaultDharmaTemplates.substring(leftIndex, index).trim()
        val name = defaultDharmaTemplates.substring(index + 1, rightIndex).trim()

        transformationsMap.put(name, body)

        defaultDharmaTemplates = defaultDharmaTemplates.replaceRange(leftIndex, rightIndex, "")
        index = defaultDharmaTemplates.indexOf('=', startIndex = 0)

        if (index < 0) {
            break
        }
    }
    return transformationsMap
}

val dharmasMap = fillTransformations()
fun transformAtomsChain(templateItems: List<LineItem.Dharma>): String {
    val endOfChainStr = ""

    var codeBlock = ""
    val dataItems = mutableListOf<String>()
    var nextAtomsGets = 0
    for (i in templateItems.indices) {
        if (nextAtomsGets > 0) {
            nextAtomsGets -= 1
            continue
        }
        val atom = templateItems[i].body.trim()
        if (atom == endOfChainStr) {
            if (dataItems.isNotEmpty()) {
                codeBlock += "${dataItems.last()}\n"
                dataItems.clear()
            }
        }

        if (!dharmasMap.contains(atom)) {
            if (!atom.isEmpty() && !atom.startsWith("//")) {
                dataItems.add(atom)
            }

        } else {
            val template = dharmasMap[atom]
            var newData = "$template"
            var index = newData.indexOf('$') // todo: add |:
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
                    val nextAtom = templateItems[i + 1].body.trim()
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
package me.truelang

import kotlin.math.max
import kotlin.math.min

// pseudocode model
//class Column {
//    class Row {
//        class Atom {
//            enum class Type {
//                Comment,
//                Empty,
//
//                True,
//                CodeTransformation, // transform code
//                DataTransformation, // transform data
//            }
//        }
//        class TransformationTemplate {} // class/function
//    }
//}


fun main() {
    /*
    * {
                $1
                "Test$1" println

            } = testPrint
            * */
    var example1 = """
        $ * $ = multiply
        println($) = println
        
        $ + $ = add
        
        // 28 testPrint
        
        1, 2 multiply println
        
        2,2
        multiply 
        println
        
        3 2 multiply
        println

        3,3 multiply
        6 add 
        println
        
        2+2 println
        
        "Text" println
        
        1,24 add
""".trimIndent()

    fun String.indexOf(char: Char, startIndex: Int = 0): Int {
        var index = startIndex
        while (index < this.length) {
            if (this[index] == char) {
                return index
            }
            index += 1
        }

        return -1
    }

    fun String.indexOfInverse(char: Char, startIndex: Int = this.length - 1): Int {
        var index = startIndex
        while (index >= 0) {
            if (this[index] == char) {
                return index
            }
            index -= 1
        }

        return -1
    }

//    10 = pageSize
//    20L = maxPages
//    "Ok" = okText
//    "Cancel" = cancelText
//    [1, 2, 3] = ads
//    true = isVisible


    fun fillTemplates(): MutableMap<String, String> {
        var templatesMap = mutableMapOf<String, String>() // name, body
        var index = example1.indexOf('=')
        var rightIndex = 0

        while (true) {
            val leftIndex = max(0, example1.indexOfInverse('\n', startIndex = index))
            rightIndex = min(example1.length, example1.indexOf('\n', startIndex = index))
            val body = example1.substring(leftIndex, index).trim()
            val name = example1.substring(index + 1, rightIndex).trim()

            templatesMap.put(name, body)

            example1 = example1.replaceRange(leftIndex, rightIndex, "")
            index = example1.indexOf('=', startIndex = 0)

            if (index < 0) {
                break
            }
        }
        return templatesMap
    }

    val templatesMap = fillTemplates()
//    println(templatesMap)

    fun fillAtoms(): MutableList<String> {
        val atoms = mutableListOf<String>()
        val column = example1.split('\n')
        column.forEach {
            val row = it.replace(", ", " ").replace(",", " ").trim()
            if (row.isEmpty() || row.startsWith("//")) {
                atoms.add(row)

            } else {
                val rowAtoms = row.split(" ")
                rowAtoms.forEach { atom ->
                    atoms.add(atom)
                }
            }
        }
        return atoms
    }

    val atoms = fillAtoms()
//    atoms.forEach {
//        println(it)
//    }


    var codeBlock = ""
    val dataItems = mutableListOf<String>()
    atoms.forEach {
        val atom = it.trim()
        if (atom.isEmpty()) {
            if (dataItems.isNotEmpty()) {
                codeBlock += "${dataItems.last()}\n"
                dataItems.clear()
            }
        }

        if (!templatesMap.contains(atom)) {
            if (!atom.isEmpty() && !atom.startsWith("//")) {
                dataItems.add(atom)
            }

        } else {
            val template = templatesMap[atom]
            var newData = "$template"
            var index = newData.indexOf('$')
            val totalCount = newData.count { it == '$' }
//            println("totalCount: $totalCount")
//            println(dataItems)
            var counter = 1
            while (index != -1) {
                newData = newData.replaceRange(
                    index,
                    index + 1,
                    dataItems[dataItems.size - 1 - (totalCount - counter)].trim()
                )
                index = newData.indexOf('$', startIndex = index + 1)
                counter += 1
            }
//            println("newData: $newData")
            dataItems.add(newData)
        }
    }
    codeBlock += "${dataItems.last()}\n"

    println(codeBlock)

    atoms.forEach {
        println("atom: $it")
    }
//
//    var prevIndex = -1
//    var currentIndex = 0
//    var nextIndex = 1
//
//
//    println("\nТранслируем ее в Kotlin:\n")
//    val code1 = """
//        // code1 ======================
//
//        {} = emptyFunction
//
//        {
//            $1 = arg
//
//            <- arg
//        } = funcWithArg
//
//        {
//            $1 = arg1
//            $2 = arg2
//
//            <- arg1 + arg2
//        } = funcWithArgs
//
//        {
//            emptyFunction()
//
//            funcWithArg("Test") = testResult
//            funcWithArg("Test2") = testResult2
//
//            // fun arg() = "Test"
//            // fun arg() {
//            //   <- "Test"
//            // }
//            // fun testResult() {
//
//            //   println(resultsStack.pop())
//            // }
//
//            testResult2()
//            // resultsStack.pop()
//
//            println(testResult())
//            // println(resultsStack.pop())
//
//            funcWithArgs(1, 2)
//        } = main
//
//
//    """.trimIndent()
//    println(translateToKotlin2(code1))
//
//    println()
}

fun createFunction(name: String, body: String): String {
    return "fun $name() {\n" +
            "$body\n" +
            "" +
            "}\n"
}


fun translateToKotlin2(source: String): String {
    var result = ""

    var startBodyIndex = 0
    var endBodyIndex = 0
    while (true) {
        startBodyIndex = source.indexOf("{", startIndex = startBodyIndex)
        if (startBodyIndex == -1) {
            break
        }

        endBodyIndex = source.indexOf("}", startIndex = startBodyIndex)
        val funcBody = source.substring(startBodyIndex + 1, endBodyIndex).trim()

        val startNameIndex = source.indexOf("=", startIndex = endBodyIndex)
        var endNameIndex = source.indexOf("\n", startIndex = startNameIndex)
        if (endNameIndex == -1) { // EOF
            endNameIndex = source.length
        }
        val funcName = source.substring(startNameIndex + 1, endNameIndex).trim()

        result += "fun $funcName() {\n"
        result += "$funcBody\n"
        result += "}\n"

        startBodyIndex = endNameIndex + 1
    }

    return result
}
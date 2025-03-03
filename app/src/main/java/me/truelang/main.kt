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
    println(1 * 2)
    println(2 * 2)
    println(3 * 2)
    println(3 * 3 + 6)
    println(2 + 2)
    println("Text")
    1 + 24

    var example2 = """
        $ = foreach
        
        [1,2,3,4,5,6,7,b,c,"text",true] foreach println
        
        {
           2
           18
           add
           println
           
           {
                { "Hello World" println }
                "Click Me"
                `Button(onClick = {
                    $
                }) {
                    Text($)
                }`
                
           } = calc
           
           1+6+ 9 * 2 
           "Test${'$'}" println

        } = blockFun
        
        blockFun
        
        """.trimIndent()

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

        3,4 multiply
        6 add 
        println
        
        2+3 println
        
        "Text" println
        
        1,24 add
        
        10 add 20
        
        10 multiply 5

""".trimIndent()

    fun fillTransformations(): MutableMap<String, String> {
        var transformationsMap = mutableMapOf<String, String>() // name, body
        var index = example1.indexOf('=')
        var rightIndex = 0

        while (true) {
            val leftIndex = max(0, example1.indexOfInverse('\n', startIndex = index))
            rightIndex = min(example1.length, example1.indexOf('\n', startIndex = index))
            val body = example1.substring(leftIndex, index).trim()
            val name = example1.substring(index + 1, rightIndex).trim()

            transformationsMap.put(name, body)

            example1 = example1.replaceRange(leftIndex, rightIndex, "")
            index = example1.indexOf('=', startIndex = 0)

            if (index < 0) {
                break
            }
        }
        return transformationsMap
    }

    val transformationsMap = fillTransformations()
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

    val endOfChainStr = ""
    fun translatedCodeBlock(): String {
        var codeBlock = ""
        val dataItems = mutableListOf<String>()
        var nextAtomsGets = 0
        for (i in atoms.indices) {
            if (nextAtomsGets > 0) {
                nextAtomsGets -= 1
                continue
            }
            val atom = atoms[i].trim()
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
    //            println("totalCount: $totalCount")
    //            println(dataItems)
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
                        val nextAtom = atoms[i + 1].trim()
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

    var codeBlock = translatedCodeBlock()

    // make an interpretator with an output like this
    // 1, 2 multiply println | println(1 * 2) | 2

    println(codeBlock)

    atoms.forEach {
        println("atom: $it")
    }
}

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
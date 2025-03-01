package me.truelang

// NOTE: Основная идея: сначала что-то создаю, затем называю

fun main() {

    // keyword "$->" is input data provider
    // keyword "$n" is argument data placeholder
    // 'any code' = код на другом языке
    val code = """
        :start: import androidx.compose.material3.Button
        :start: import androidx.compose.material3.Text
        `Button(onClick = {
            |onClick()| : () -> Unit|
        }) {
            Text(|text| : String|)
        }` = Button
            
            
        :start: 
        :start: 
        :start: // вставить строку в начало файла
        :start: // NOTE: сначала что-то создаю, затем называю
        { 
            10 = pageSize
            20L = maxPages
            "Ok" = okText
            "Cancel" = cancelText
            
            false -> main()
            //

            Button(okText) = okButton
            okButton()
            
            pageSize + maxPages + 1 = text
            println(text)
            
            true = isVisible
            [1, 2, 3] = ads
            ads.size > 3 -> show(okButton, isVisible) = funcName
            
        } = main

    """.trimIndent()


    println("Программ на языке True:\n")
    println(code)

    println("\nТранслируем ее в Kotlin:\n")
    println(translateToKotlin(code))
}

// NOTE: Язык высокоуровневый, как фассад над другим языком,
// под капотом может быть любая реализация с любыми подключенными фреймворками
// не имеет смысла строить свою систему когда их и так куча,
// это прежде всего инструмент для написания кода в уже существующей инфраструктуре

fun translateToKotlin(source: String): String {
    var code = source
    if (!source.trim(' ', '\t').startsWith("{")) {
//        code = "{\n$code\n}"
    }

    val lines = code.split('\n')
    var resultCode = ""
    var startLines = ""
    var tabsCounter = 0
    val arguments = mutableListOf<String>()
    var isAnotherCode = false
    var anotherCode = ""
    var functionNames = mutableListOf<String>()
    fun translateFunctionCalls(value: String): String {
        val items = value.split(Regex("[^a-zA-Z0-9\"']+"))
        var normalizedValue = value

        items.forEach { it ->
            if (it.isNotBlank() && (!it[0].isDigit() && it != "true" && it != "false" && it[0] != '"')) {
                normalizedValue = normalizedValue.replace(it, "$it()")
                normalizedValue = normalizedValue.replace("()(", "(")
            }
        }
        return normalizedValue
    }

    lines.forEach {
        val isComment = isComment(it)
        val line = if (isComment) it else it.replace("", "").replace("\t", "")
        val startKeyword = ":start: "
        when {
            !isComment && isAnotherCode -> {
                if (line.contains("`")) {
                    val names = line.split("=")[1]
                    val name = names.split(",")[0].trim()

                    anotherCode += "${getTabs(tabsCounter)}${
                        line.replaceAfter("`", "").dropLast(1)
                    }\n"
                    functionNames.add(name)
                    var args = ""
                    var index = anotherCode.indexOf("|")
                    while (index != -1) {
                        var index2 = anotherCode.indexOf("|", startIndex = index + 1)
                        val argName = anotherCode.substring(index + 1, index2)
                        var index3 = anotherCode.indexOf("|", startIndex = index2 + 1)
                        val argType = anotherCode.substring(index2 + 1, index3)

                        val bracketIndexStart = argName.indexOf("(")
                        var clearedArgName = ""
                        if (bracketIndexStart != -1) {
                            val bracketIndexEnd = argName.indexOf(")")
                            clearedArgName =
                                argName.replaceRange(bracketIndexStart, bracketIndexEnd+1, "")
                        }else {
                            clearedArgName = argName
                        }
                        args += "$clearedArgName$argType, "
                        anotherCode = anotherCode.replaceRange(index2, index3 + 1, "")
                            .replaceRange(index, index + 1, "")

                        index = anotherCode.indexOf("|")
                    }
                    resultCode += "fun $name($args) =\n$anotherCode\n"

                    isAnotherCode = false
                    anotherCode = ""
                    tabsCounter -= 1
                } else {
                    anotherCode += "${getTabs(tabsCounter)}${line}\n"
                }
            }

            !isComment && line.startsWith("`") -> {
                tabsCounter += 1
                isAnotherCode = true
                anotherCode += "${getTabs(tabsCounter)}${line.replace("`", "")}\n"
            }

            !isComment && line.startsWith(startKeyword) -> {
                startLines += "${
                    line.replaceFirst(
                        startKeyword,
                        ""
                    )
                }\n"
            }

            !isComment && line.trim() == "{" -> {
                tabsCounter += 1
                resultCode += "${getTabs(tabsCounter - 1)}\$fun$${tabsCounter} {\n"
            }

            (!isComment && line.contains("=") && !line.startsWith("}")) -> {
                val items = line.split("=")

                val name = items[1].trim()
                functionNames.add(name)
                if (items[0].contains("->")) {
                    val items2 = items[0].split("->")
                    resultCode += "${getTabs(tabsCounter)}fun $name() {\n"
                    resultCode += "${getTabs(tabsCounter + 1)}if (${items2[0].trim()}) {\n${
                        getTabs(
                            tabsCounter + 2
                        )
                    }${items2[1].trim()}\n${
                        getTabs(
                            tabsCounter + 1
                        )
                    }}\n"
                    resultCode += "${getTabs(tabsCounter)}}\n"
                } else {

                    fun unpackList(string: String) = string.replace("[", "").replace("]", "").trim()

                    val isList = items[0].contains('[')
                    val value = items[0].trim()

                    var normalizedValue = translateFunctionCalls(value)

                    resultCode += "${getTabs(tabsCounter)}fun $name() =\n${getTabs(tabsCounter + 1)}${if (isList) "mutableListOf(" else ""}${
                        unpackList(
                            normalizedValue
                        )
                    }${if (isList) ")" else ""}\n${getTabs(tabsCounter)}\n"
                }
            }

            (!isComment && line.contains("->")) -> {
                val items = line.split("->")
                resultCode += "${getTabs(tabsCounter)}if (${items[0].trim()}) {\n${
                    getTabs(
                        tabsCounter + 1
                    )
                }${items[1].trim()}\n${
                    getTabs(tabsCounter)
                }}\n"
            }

            !isComment && line.startsWith("}") -> {
                if (line.length > 1) {
                    val names = line.split("=")[1]
                    val name = names.split(",")[0].trim()
                    resultCode += "${getTabs(tabsCounter - 1)}}\n"

                    var args = ""
                    arguments.forEach {
                        args += "$it, "
                    }
                    arguments.clear()

                    functionNames.add(name)
                    resultCode = resultCode.replace(
                        "\$fun$${tabsCounter} ",
                        "fun $name(${args.trim(' ', ',')}) "
                    )
                }
                tabsCounter -= 1
            }

            isComment -> resultCode += "$line\n"
            line == "" -> resultCode += "\n"
            else -> {
                var normalizedValue = translateFunctionCalls(line)

                resultCode += "$normalizedValue\n"
            }
        }
    }

    return startLines + resultCode
}

fun getTabs(count: Int): String {
    return count multiply "\t"
}

infix fun Int.multiply(string: String): String {
    var result = ""
    repeat(this) {
        result += string
    }
    return result
}

fun isComment(s: String): Boolean = s.trim().startsWith("//")

//  = Функциональный Язык программирования True :)
package me.truelang

// NOTE: Основная идея: сначала что-то создаю, затем называю

// Pseudo Code:

// Primitives: Int, Long, String, Array, Fun, ParallelFun
// {
//    10 = pageSize
//    20L = maxPages
//    "Ok" = okText
//    "Cancel" = cancelText
//
//    {
//      empty = text : String
//    } = Button
//
//    Button(okText) = okButton
//    true = isVisible
//    1, 2, 3 = ads
//
//    {
//      empty = count : Int
//      empty = fun : Fun
//
//      0 = i
//      i < count -> fun()
//    } = repeat
//
//    {
//      empty = items : Array
//      empty = fun : Fun
//
//      0 = i
//      i < count -> fun(items[i])
//    } = foreach
//
//    {
//      empty = button : Button
//      true = visible : Boolean
//      true = result : Boolean
//
//      visible -> Platform.show(button)
//    } = show
//
//    ads.size > 3 -> show(okButton, isVisible) = funcName
//    Backend.getUser() : Parallel = getUserJob
//    getUserJob.success -> repeat(3, println("${getUserJob.data}")) = logUser
//    getUserJob.success == false -> wait(getUserJob.success).than(logUser)
//    {...} = wait : ParallelFun
//
//    true -> foreach(ads, println(.it))
//    "log" -> println(.it)
// } = main, secondName

//  = Язык программирования True :)

fun main() {
    val code = """
        // NOTE: сначала что-то создаю, затем называю
        { 
            10 = pageSize
            20L = maxPages
            "Ok" = okText
            "Cancel" = cancelText
            
            false -> main()
            //
            
            {
              empty = text : String
            } = Button

            Button(okText) = okButton
            true = isVisible
            1, 2, 3 = ads
            ads.size > 3 -> show(okButton, isVisible) = funcName
            
        } = main
    """.trimIndent()


    println("Программ на языке True:\n")
    println(code)

    println("\nТранслируем ее в Kotlin:\n")
    println(translateToKotlin(code))
}

// NOTE: переделать функции в классы чтобы иметь доступ к полям аргументов

fun translateToKotlin(code: String): String {
    val lines = code.split('\n')
    var result = ""
    var counter = 0
    val arguments = mutableListOf<String>()
    lines.forEach {
        val isComment = isComment(it)
        val s = if (isComment) it else it.replace(" ", "").replace("\t", "")
        when {
            !isComment && s == "{" -> {
                counter += 1
                result += "${getTabs(counter - 1)}$${counter} {\n"
            }

            (!isComment && s.contains("=") && !s.startsWith("}") && !s.contains(":")) -> {
                val items = s.split("=")

                if (items[0].contains("->")) {
                    val items2 = items[0].split("->")
                    result += "${getTabs(counter)}fun ${items[1]}() {\n"
                    result += "${getTabs(counter + 1)}if (${items2[0]}) {\n${getTabs(counter + 2)}${items2[1]}\n${
                        getTabs(
                            counter + 1
                        )
                    }}\n"
                    result += "${getTabs(counter)}}\n"
                } else {

                    val isList = items[0].contains(',')
                    result += "${getTabs(counter)}var ${items[1]} = ${if (isList) "mutableListOf(" else ""}${items[0]}${if (isList) ")" else ""}\n"
                }
            }

            (!isComment && s.contains("->")) -> {
                val items = s.split("->")
                result += "${getTabs(counter)}if (${items[0]}) {\n${getTabs(counter + 1)}${items[1]}\n${
                    getTabs(
                        counter
                    )
                }}\n"
            }

            !isComment && s.startsWith("}") -> {
                val names = s.split("=")[1]
                val name = names.split(",")[0]
                result += "${getTabs(counter - 1)}}\n"

                var args = ""
                arguments.forEach {
                    args += "$it, "
                }
                arguments.clear()

                result = result.replace("$${counter} ", "fun $name(${args.trim(' ', ',')}) ")
                counter -= 1
            }

            !isComment && s.contains(":") -> {
                val argItems = s.split(":")
                val argType = argItems[1]

                val items = argItems[0].split("=")
                val isList = items[0].contains(',')
                val isEmpty = items[0] == "empty"
                val arg = if (isEmpty)
                    "${items[1]}: ${argType}"
                else
                    "${items[1]}: ${argType} = ${if (isList) "mutableListOf(" else ""}${items[0]}${if (isList) ")" else ""}"


                arguments.add(arg)
            }

            isComment -> result += "$s\n"
            s == "" -> result += "\n"
        }
    }
    return result
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
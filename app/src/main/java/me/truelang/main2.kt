package me.truelang

//fun main() {
//    val code = """
//        // NOTE: сначала что-то создаю, затем называю
//        // {} - дхарма которая одни данные преобразует в другие
//        // inline: keyword to transform data
//        // actual код на языке трансформации который будет вставлен как есть
//        inline {
//            (list)
//            -> dharma
//
//            actual {
//                list.forEach { it ->
//                    dharma(it)
//                }
//            }
//
//        } foreach
//
//        inline {
//            :value
//            (names)
//
//            foreach(names) -> { value } it
//        } =, name
//
//        inline {
//            (text)
//
//            actual {
//                println(:text)
//            }
//        } log
//
//        inline {
//            :funcContainer
//            (funcArgument)
//
//            actual {
//                imports {
//                    import example
//                }
//
//                work(onDone = { it ->
//                    funcContainer
//                }) {
//                    funcArgument
//                }
//            }
//
//        } async
//
//
//        {
//            { 10 } pageSize
//            { pageSize } size
//            { Button(size) } initButton
//            { true -> initButton } checkAndInit
//            { [1,2,3] } list
//
//            initButton()
//
//            10 = pageSize, size
//            10 name count
//            true -> log(count)
//            20L = maxPages
//            "Ok" = okText
//            "Cancel" = cancelText
//
//            false -> main()
//            //
//
//            inline {
//              (name)
//              -> dharma
//
//              actual {
//                  Button(onClick = {
//                        dharma
//                  }) {
//                        Text(text = name)
//                  }
//              }
//
//            } Button
//
//            Button(okText) = okButton
//            okButton() -> println("Hello World!")
//            true = isVisible
//            [1, 2, 3] = ads
//            ads.size > 3 -> show(okButton, isVisible) = funcName
//            true -> funcName()
//
//            {
//                log("Work Work Work")
//            } requestUserFromBack
//
//            true -> UserView(async(requestUserFromBack(id)))
//
//            UserView(requestUserFromBack(id))
//            // NOTE: функция UserView не отработает пока не отработает requestUserFromBack()
//            // при этом другие функции продолжат исполняться
//
//
//        } main
//    """.trimIndent()
//
//
//    println("Программ на языке True:\n")
//    println(code)
//
//    println("\nТранслируем ее в Kotlin:\n")
//    println(translateToKotlin2(code))
//}
//
//
//// Составить дерево функций
//// Заинлайнить inline функции
//// Преобразовать каждую функцию в код
//// использовать вставку inline actual, если нет использовать базовый преобразователь
//
//data class DharmaData(
//    val left: String, // :arg // то что слева до имени функции
//    val right: String, // (123, 1, 0) // то что справа от имени функции
//    val action: String, // -> func // то что справа от имени функции после знака ->
//    val body: String, //  то что исполняет функция, последняя строка будет возвращаемым результатом функции
//    val name: String
//)
//
//fun translateToKotlin2(code: String): String {
//    var result = ""
//    val rootDharms = getDharms(code)
//
//}
//
//fun getDharms(code: String): MutableList<DharmaData> {
//    val result = mutableListOf<DharmaData>()
//    val flatCode = code.replace(" ", "").replace("\t", "")
//    var cursor = 0
//    while (true) {
//        val inlineDharmaIndex = flatCode.indexOf("inline{", startIndex = cursor)
//        if (inlineDharmaIndex >= 0) {
//            cursor = inlineDharmaIndex
//
//            continue
//        }
//
//        break
//    }
//
//    cursor = 0
//    while (true) {
//        val baseDharmaIndex = flatCode.indexOf("{", startIndex = cursor)
//        if (baseDharmaIndex >= 0) {
//            cursor = baseDharmaIndex
//
//            continue
//        }
//
//        break
//    }
//    val lines = code.split("\n")
//    return result
//}

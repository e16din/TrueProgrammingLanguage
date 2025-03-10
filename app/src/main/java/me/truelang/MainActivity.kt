package me.truelang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.truelang.lang.*
import me.truelang.ui.theme.TrueProgrammingLanguageTheme
import kotlin.text.StringBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrueProgrammingLanguageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreenProvider(innerPadding)
                }
            }
        }
    }
}

@Composable
private fun MainScreenProvider(innerPadding: PaddingValues) {
    // todo
    val testScreen = """
            # Press "Run" to show a screen
            // state for Text
            |:label = textLabel
            
            Column
            Spacer
            
            textLabel Text
            
            Spacer 
            
            `add` Button = AddButton
            
            [...] = MainScreen
            
            `Hello World!` MainScreen

            Spacer
           
           1 = counter
           user[AddButton:onClick] * (counter + 1 = counter `Hello World! |:i` MainScreen)
           
//           ProgressBarScreen
//           PostLogin
//           backend[PostLogin:onResponse] * (it = response - user - name print) 
//           
//           container - element 
//           container - !element

        """.trimIndent()

    val main = """
            # Let's start!
            10
            multiply
            20
            add
            22
            print
            
            # Enter your code:

        """.trimIndent()

    MainScreenView(
        modifier = Modifier.padding(innerPadding),
        initialChains = main.toDharmaChains(),
        name = "Main"
    )
}


data class TemplateItem(var id: Int, val dharma: LineItem.Dharma)

private var idCounterVal = 0
val idCounter: Int
    get() {
        idCounterVal += 1
        return idCounterVal
    }

sealed class LineItem(open val id: Int) {
    data class Message(
        override val id: Int = idCounter,
        var message: String,
    ) : LineItem(id)

    data class Dharma( // Transformation, Presenter, Map, Function, Producer, Action, Operation
        override val id: Int = idCounter,
        var name: String,
        var body: String,
        var color: Color = Color.White,
        var argumentName: String? = null
    ) : LineItem(id)

    data class End(
        override val id: Int = idCounter,
    ) : LineItem(id)
}


// TODO: подсвечивать функции с необходимым кол-вом аргументов, остальные дизэйблить
// TODO: Брать предыдущий атом если | и следующий если $

fun List<LineItem>.toBlank(): String {
    var result = StringBuilder()
    this.forEach {
        when (it) {
            is LineItem.Message -> {
                result.append("${it.message}\n")
            }

            is LineItem.Dharma -> {
                val line = if (it.name.isNotEmpty()) it.name else it.body
                result.append("$line\n")
            }

            is LineItem.End -> {
                // do nothing
            }
        }
    }

    return result.toString()
}

fun String.toDharmaChains(): MutableList<LineItem> {
    var result = mutableListOf<LineItem>()
    val lines = this.split('\n')
    lines.forEach {
        val line = it.trim()
        when {
            line.isNotEmpty() && line.first() == '#' -> {
                result.add(
                    LineItem.Message(message = (line minus 1).other)
                )
                println("id1: ${result.last().id}")
            }

            dharmasMap.keys.contains(line) -> {
                result.add(
                    LineItem.Dharma(name = line, body = dharmasMap[line] ?: "")
                )
                println("id2: ${result.last().id}")
            }

            line.startsWith("//") -> {
                // do nothing
            }

            else -> {
                result.add(
                    LineItem.Dharma(name = "", body = line)
                )
                println("id3: ${result.last().id}")
            }
        }
    }
    return result
}

fun main() {
    val ends = listOf(")", " ", ",", "]", "\n", "}")
    val args = "println(|:value) = print".divideBy(
        start = "|:",
        condition = { it, selected, isLast ->
            println(it)
            it.endsWithAny(ends)
        }
    ).map { it.replaceAll(ends + "|:") }
    println(args)
}


@Composable
fun MainScreenView(
    modifier: Modifier = Modifier,
    initialChains: List<LineItem>,
    name: String
) {
    val dharmaChains = remember {
        initialChains.toMutableStateList()
    }

    fun calcResult(dharmas: MutableList<LineItem.Dharma>): String {
        val instructionAndOther =
            dharmas.divideBy<LineItem.Dharma, LineItem.Dharma>(condition = { it, selected ->
                it.argumentName == null
            })
        val instruction = instructionAndOther.selected.first().body
        val other = instructionAndOther.other

        var result = instruction
        other.forEach {
            result = result
                .replace("|:${it.argumentName}", it.body)
                .replace("$:${it.argumentName}", it.body)
        }

        println("result: $result")
        try {
            return interpretMath(addBrackets(result))
        } catch (e: Exception) {
            println("Interpretation Error:")
            e.printStackTrace()
            return "error"
        }
    }

    fun addDharmaToChain(dharma: LineItem.Dharma) {
        println("addDharmaToChain: ${dharma}")
        val ends = listOf(")", " ", ",", "]", "\n", "}")

        if (!dharma.body.startsWith("\"")
            && !dharma.body.startsWith("`")
            && dharma.body.contains("|")
        ) {
            println("debug: 52")
            val prevArgNames = dharma.body.divideBy(
                start = "|:",
                condition = { it, selected, isLast ->
                    println(it)
                    it.endsWithAny(ends) || isLast
                }
            ).map { it.replaceAll(ends + "|:") }
            println("selectedArgNames: $prevArgNames")

            val selectedDharmasIndices =
                dharmaChains.divideBy<LineItem, LineItem.Dharma>(
                    condition = { it, selected ->
                        it is LineItem.Dharma
                                && selected.size < prevArgNames.size
                    },
                    fromEnd = true
                ).selectedIndices

            println("selectedDharmasIndices: $selectedDharmasIndices")

            var i = 0
            selectedDharmasIndices.forEach { index ->
                println("debug: 6: $index")

                dharmaChains[index] = (dharmaChains[index] as LineItem.Dharma).copy(
                    id = idCounter,
                    color = Color.Green,
                    argumentName = prevArgNames[i]
                )
                i++
            }
        }

        var argPlaceholdersToCommit = listOf<LineItem.Dharma>()
        if (!dharma.body.startsWith("\"")
            && !dharma.body.startsWith("`")
            && dharma.body.contains("$")
        ) {
            println("debug: 552")

            val nextArgNames = dharma.body.divideBy(
                start = "$:",
                condition = { it, selected, isLast ->
                    println(it)
                    it.endsWithAny(ends) || isLast
                }
            ).map { it.replaceAll(ends + "$:") }
            println("nextArgNames: $nextArgNames")

            val selectedDharmasIndices =
                dharmaChains.divideBy<LineItem, LineItem.Dharma>(
                    condition = { it, selected ->
                        it is LineItem.Dharma
                                && selected.size < nextArgNames.size
                    },
                    fromEnd = true
                ).selectedIndices

            println("selectedDharmasIndices: $selectedDharmasIndices")

            var i = 0
            argPlaceholdersToCommit = selectedDharmasIndices.map {
                LineItem.Dharma(
                    id = idCounter,
                    name = "",
                    body = "$",
                    color = Color.Yellow,
                    argumentName = nextArgNames[i]
                ).also {
                    i++
                }
            }
        }

        val argPlaceholdersCommited = (dharmaChains.divideBy<LineItem, LineItem.Dharma>(
            condition = { it, selected ->
                it is LineItem.Dharma
                        && it.body == "$"
            },
            fromEnd = true,
            addToStart = true
        ))
        println("lastDharmaResult: $argPlaceholdersCommited")
        if (argPlaceholdersCommited.selected.isNotEmpty()) { // NOTE: fill placeholders
            println("111")
            val index = argPlaceholdersCommited.selectedIndices.first()
            dharmaChains[index] = dharma.copy(
                id = idCounter,
                color = (dharmaChains[index] as LineItem.Dharma).color,
                argumentName = (dharmaChains[index] as LineItem.Dharma).argumentName
            )
            if (argPlaceholdersCommited.selected.size == 1) { // NOTE: completed
                dharmaChains.add(
                    LineItem.Dharma(
                        name = "",
                        body = calcResult(
                            dharmaChains.divideBy<LineItem, LineItem.Dharma>(
                                condition = { it, selected ->
                                    it is LineItem.Dharma
                                },
                                breakCondition = { it, selected ->
                                    it is LineItem.Dharma
                                            && it.argumentName == "result" // || end of list
                                },
                                fromEnd = true
                            ).selected
                        ),
                        argumentName = "result"
                    )
                )
            }

        } else {
            println("222")
            dharmaChains.add(dharma)
            argPlaceholdersToCommit.forEach { argDharma ->
                dharmaChains.add(argDharma)
            }
        }
    }

    Scaffold(
        topBar = {
            NavigationBar {
                Text(name)
            }
        },
        modifier = Modifier.systemBarsPadding()
    ) { padding ->
        var inputText by remember { mutableStateOf(TextFieldValue("")) }

        val dharmaTemplates = remember {
            dharmasMap.keys.mapIndexed { index, key ->
                TemplateItem(
                    id = index,
                    dharma = LineItem.Dharma(
                        id = idCounter,
                        name = key,
                        body = dharmasMap[key] ?: ""
                    )
                )

            }.toMutableStateList()
        }

        var selectedDharmaItem by remember { mutableStateOf<LineItem.Dharma?>(null) }
        var selectedItemIndex by remember { mutableIntStateOf(0) }

        var dharmaChainToName by remember { mutableStateOf<List<LineItem>?>(null) }
        var dharmaChainName by remember { mutableStateOf("") }

        Surface(
            modifier
                .systemBarsPadding()
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(Modifier.fillMaxSize()) {
                LazyColumn {
                    items(dharmaChains.size, key = { i -> dharmaChains[i].id }) { i ->
                        println("debug: 5")
                        val dharma = dharmaChains[i]
                        when (dharma) {
                            is LineItem.Message -> {
                                Row {
                                    Spacer(Modifier.weight(1f))
                                    Text("${dharma.message}")
                                    Spacer(Modifier.weight(1f))
                                    Button(onClick = {
                                        dharmaChains.removeAt(i)
                                    }) {
                                        Icon(Icons.Default.Close, "Remove")
                                    }
                                }
                            }

                            is LineItem.Dharma -> {
                                println("debug: 51")

                                DharmaItemView(
                                    dharma = dharma,
                                    lineNumber = i,
                                    result = "temp",
                                    onItemSelected = {
                                        selectedDharmaItem = dharma
                                        selectedItemIndex = i
                                    },
                                    onRemoveClick = {
                                        dharmaChains.removeAt(i)
                                    }
                                )
                            }

                            is LineItem.End -> {
                                Row {
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = "End",
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clickable {

                                            })
                                }
                            }

                        }
                    }

                    item {
                        Row {
                            TextField(value = inputText, onValueChange = {
                                inputText = it
                            }, modifier = Modifier.weight(1f))
                            Button(onClick = {
                                val input = inputText.text.trim()
                                if (input.isEmpty()) {
                                    println("debug: 2")
                                    dharmaChains.add(LineItem.End())

                                } else {
                                    val values = input.split(" ")
                                    println("keys: "+dharmasMap.keys)
                                    values.forEach { value ->
                                        if (dharmasMap.keys.contains(value)) {
                                            println("Value1: $value")
                                            val template =
                                                dharmaTemplates.first { it.dharma.name == value }
                                            addDharmaToChain(template.dharma.copy(idCounter))

                                        } else {
                                            println("Value2: $value")
                                            val dharma = LineItem.Dharma(
                                                name = "",
                                                body = value
                                            )
                                            println("debug: 3")
                                            addDharmaToChain(dharma)
                                        }
                                    }
                                    inputText = TextFieldValue("")
                                }

                            }) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Post")
                            }
                        }
                    }

                    item {
                        KeyboardActionsView(onItemClick = {
                            val newText = inputText.text + it
                            inputText =
                                TextFieldValue(newText, TextRange(newText.length, newText.length))
                        })
                    }

                    item {
                        TemplatesView(dharmaTemplates, onTemplateClick = {
                            println("debug: 1")
                            addDharmaToChain(it.dharma)
                        })
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }

                AnimatedVisibility(selectedDharmaItem != null) {
                    BackHandler {
                        selectedDharmaItem = null
                    }
                    TemplateEditorView(selectedDharmaItem, onSaveClick = { name, template ->
                        println("debug: 4")
                        dharmaChains[selectedItemIndex] = selectedDharmaItem!!.copy(
                            id = idCounter,
                            name = name,
                            body = template,
                        )
                        selectedDharmaItem = null
                    })
                }

                AnimatedVisibility(dharmaChainToName != null) {
                    BackHandler {
                        dharmaChainToName = null
                    }

                    dharmasMap[dharmaChainName] =
                        (dharmaChainToName!! minusFromEnd 1).other.toBlank()
                    MainScreenView(
                        modifier,
                        dharmaChainToName!!,
                        dharmaChainName
                    )
                }
            }
        }
    }
}

@Composable
fun DharmaItemView(
    dharma: LineItem.Dharma,
    lineNumber: Int,
    result: String,
    onItemSelected: (LineItem) -> Unit,
    onRemoveClick: (LineItem) -> Unit,
) {
    println("DharmaItemView")
    Box {
        Row(
            Modifier
                .background(dharma.color)
                .clickable {
                    onItemSelected(dharma)
                }) {
            Text(
                "$lineNumber.",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Text(
                text = if (dharma.name.isEmpty()) dharma.body else dharma.name,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "$result",
                maxLines = 1,
                color = Color.Gray,
                modifier = Modifier.width(100.dp)
            )

            Button(onClick = {
                onRemoveClick(dharma)
            }) {
                Icon(Icons.Default.Close, "Remove")
            }
        }

        dharma.argumentName?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .padding(top = 32.dp, start = 24.dp)
                    .clip(CircleShape)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

//@Composable
//private fun InterpretatorView(
//    dharmaChain: SnapshotStateList<LineItem>,
//    onChainNamed: (String) -> Unit
//) {
//    println("InterpretatorView")
//    Column(modifier = Modifier.background(Color.Gray)) {
//        println("debug: 111")
//        var lastChain1 = dharmaChain.divideBy<LineItem, LineItem.Dharma>(
//            condition = { it, selected ->
//                it is LineItem.Dharma
//            },
//            breakCondition = { it, selected ->
//                it is LineItem.End
//            },
//            fromEnd = true,
//        ).selected
//
//        var lastChain = dharmaChain.filter { it is LineItem.Dharma }.map { it as LineItem.Dharma }
//        println("debug: 222")
//        val lastDharma =
//            dharmaChain.last() as LineItem.Dharma// (lastChain minus 1).selected.first()
////        val bodyParts = lastDharma.body.replace(" ", "").split("=")
////        when {
////            bodyParts.size > 1 && bodyParts[0].startsWith(":chain:") -> {
////                onChainNamed(bodyParts[1])
////                return
////            }
////        }
//
//        println("debug: 333")
//        var console = ""
//        try {
//            lastResult = transformAtomsChain(lastChain)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        println("debug: 444")
//        console = interpretCode(
//            result = lastResult,
//            transformation = lastDharma.body
//        )
//        println("debug: 555")
//        Text("Console: $console")
//    }
//}

@Composable
fun TemplateEditorView(
    selectedLineItem: LineItem?,
    onSaveClick: (name: String, template: String) -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        when (selectedLineItem) {
            is LineItem.Dharma -> {
                Column {
                    var templateValue by remember {
                        mutableStateOf(
                            selectedLineItem.body
                        )
                    }
                    Row {
                        TextField(
                            value = templateValue,
                            label = { Text("Body") },
                            onValueChange = {
                                templateValue = it
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    var nameValue by remember {
                        mutableStateOf(
                            selectedLineItem.name
                        )
                    }
                    Row {
                        TextField(
                            value = nameValue,
                            label = { Text("Name") },
                            onValueChange = {
                                nameValue = it
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button({
                        onSaveClick(nameValue, templateValue)
                    }) {
                        Text("Save")
                    }
                }
            }

            else -> {}
        }
    }
}

val keyboardActions = listOf(
    "+",// plus
    "-",// minus
    "/",// divide
    "/?",// divideBy
    "*",// multiply // for
    "*?",// multiplyBy // while // 0 = i, i < n *? (i+1 = i, print)
    " ",
    "|:",// prevArg
    "$:",// nextArg
    "...",// all dharma lines
    "#", // message
    "=" // name

//    ":start:" // insert to start of implementation file
)

@Composable
fun KeyboardActionsView(onItemClick: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        keyboardActions.forEach {
            Button(
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.inversePrimary),
                onClick = {
                    onItemClick(it)
                }
            ) {
                Text(it)
            }
        }
    }
}

@Composable
fun TemplatesView(
    templates: SnapshotStateList<TemplateItem>,
    onTemplateClick: (TemplateItem) -> Unit
) {
    LazyHorizontalStaggeredGrid(
        modifier = Modifier.height(120.dp),
        rows = StaggeredGridCells.Adaptive(32.dp),
        horizontalItemSpacing = 4.dp,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(templates, key = {
                it.id
            }) { it ->
                val containerColor =
                    if (it.dharma.body.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
                Button(
                    colors = ButtonDefaults.buttonColors()
                        .copy(containerColor = containerColor),
                    onClick = {
                        onTemplateClick(it)

                    }) {
                    Text(it.dharma.name)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val main = """
            # Let's start!
            10
            multiply
            20
            add
            22
            print
            
            # Enter your code:

        """.trimIndent()

    TrueProgrammingLanguageTheme {
        MainScreenView(
            Modifier,
            initialChains = main.toDharmaChains(),
            name = "Main"
        )
    }
}


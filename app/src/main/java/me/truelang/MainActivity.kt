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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.truelang.lang.*
import me.truelang.ui.theme.TrueProgrammingLanguageTheme

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
            Column
            Spacer
            
            // state for Text
            Hello World! = text
            Text
            
            Spacer 
            
            Button
            // onClick
            // update text state
            text + 1 = text
            
            Spacer
            
            # Enter your code:

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
    val dharmaChains = remember {
        main.toDharmaChains()
            .toMutableStateList()
    }

    MainScreenView(
        modifier = Modifier.padding(innerPadding),
        dharmaChains = dharmaChains,
        name = "Main"
    )
}


data class TemplateItem(var id: Int, val dharma: LineItem.Dharma)

sealed class LineItem(open val id: Int) {
    data class Message(
        override val id: Int,
        var message: String,
    ) : LineItem(id)

    data class Dharma( // Transformation, Presenter, Map, Function, Producer, Action, Operation
        override val id: Int,
        var name: String,
        var body: String,
        var color: Color = Color.White
    ) : LineItem(id)

    data class End(
        override val id: Int
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
            line.first() == '#' -> {
                result.add(
                    LineItem.Message(id = lines.size, message = (line minus 1).other)
                )
            }

            dharmasMap.keys.contains(line) -> {
                result.add(
                    LineItem.Dharma(id = lines.size, name = line, body = dharmasMap[line] ?: "")
                )
            }

            line.startsWith("//") -> {
                // do nothing
            }

            else -> {
                result.add(
                    LineItem.Dharma(id = lines.size, name = "", body = line)
                )
            }
        }
    }
    return result
}


@Composable
fun MainScreenView(
    modifier: Modifier = Modifier,
    dharmaChains: SnapshotStateList<LineItem>,
    name: String
) {
    Scaffold(topBar = {
        Text(name)
    }) { padding ->
        var inputText by remember { mutableStateOf(TextFieldValue("")) }

        val dharmaTemplates = remember {
            dharmasMap.keys.mapIndexed { index, key ->
                TemplateItem(
                    id = index,
                    dharma = LineItem.Dharma(id = -1, name = key, body = dharmasMap[key] ?: "")
                )

            }.toMutableStateList()
        }

        var selectedDharmaItem by remember { mutableStateOf<LineItem.Dharma?>(null) }
        var selectedItemIndex by remember { mutableIntStateOf(0) }

        var dharmaChainToName by remember { mutableStateOf<List<LineItem>?>(null) }
        var dharmaChainName by remember { mutableStateOf("") }

        Surface(
            modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(padding)
        ) {
            Box(Modifier.fillMaxSize()) {
                LazyColumn {
                    items(dharmaChains.size, key = { i -> dharmaChains[i].id }) { i ->
                        val dharma = dharmaChains[i]
                        when (dharma) {
                            is LineItem.Message -> {
                                Row {
                                    Spacer(Modifier.weight(1f))
                                    Text("# ${dharma.message} #")
                                    Spacer(Modifier.weight(1f))
                                }
                            }

                            is LineItem.Dharma -> {
                                DharmaItemView(
                                    dharma,
                                    i,
                                    dharmaChains,
                                    onItemSelected = {
                                        selectedDharmaItem = dharma
                                        selectedItemIndex = i
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
                        InterpretatorView(dharmaChains, onChainNamed = {
                            dharmaChainToName = dharmaChains.divide<LineItem, LineItem>(
                                selectCondition = {
                                    true
                                },
                                breakCondition = {
                                    it is LineItem.End
                                },
                                fromEnd = true,
                            ).selected
                        })
                    }

                    item {
                        Row {
                            TextField(value = inputText, onValueChange = {
                                inputText = it
                            }, modifier = Modifier.weight(1f))
                            Button(onClick = {
                                if (inputText.text.isEmpty()) {
                                    dharmaChains.add(LineItem.End(dharmaChains.size))

                                } else {
                                    val parts = inputText.text.split("=")
                                    val template = parts[0]
                                    val name = if (parts.size > 1) parts[1] else ""
                                    val dharma = LineItem.Dharma(
                                        id = dharmaChains.size,
                                        name = name,
                                        body = template
                                    )
                                    dharmaChains.add(dharma)

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
                            dharmaChains.add(
                                it.dharma.copy(dharmaChains.size)
                            )
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
                        dharmaChains[selectedItemIndex] = selectedDharmaItem!!.copy(
                            name = name,
                            body = template,
                            id = selectedDharmaItem?.id!! * 10,
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
                        dharmaChainToName!!.toMutableStateList(),
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
    i: Int,
    dharmaChain: SnapshotStateList<LineItem>,
    onItemSelected: (LineItem) -> Unit
) {

    Row(
        Modifier
            .background(dharma.color)
            .clickable {
                onItemSelected(dharma)
            }) {
        Text(
            text = dharma.name + " = " + dharma.body,
            modifier = Modifier.weight(1f)
        )

        if (dharma.body.trim().isNotEmpty()) {
            Button(onClick = {
                dharmaChain.removeAt(i)
            }) {
                Icon(Icons.Default.Close, "Remove")
            }
        }
    }
}

var lastResult = ""

@Composable
private fun InterpretatorView(
    dharmaChain: SnapshotStateList<LineItem>,
    onChainNamed: (String) -> Unit
) {
    Column(modifier = Modifier.background(Color.Gray)) {
        var lastChain = dharmaChain.divide<LineItem, LineItem.Dharma>(
            selectCondition = {
                it is LineItem.Dharma
            },
            breakCondition = {
                it is LineItem.End
            },
            fromEnd = true,
        ).selected
//        try {
//            code = transformAtomsChain(lastChain)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        Text("Kotlin: $code")

        val lastDharma = (lastChain minus 1).selected.first()
        val bodyParts = lastDharma.body.replace(" ", "").split("=")
        when {
            bodyParts.size > 1 && bodyParts[0].startsWith(":chain:") -> {
                onChainNamed(bodyParts[1])
                return
            }
        }

        var console = ""
        try {
            lastResult = transformAtomsChain(lastChain)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        console = interpretCode(
            result = lastResult,
            transformation = lastDharma.body
        )
        Text("Console: $console")
    }
}

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
    "+",
    "-",
    "/",
    "*",
    " ",
    "|",
    "$",
    "...",
    ":",
    "#",
    ":start:"
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
                        println("here! $it")
                        if (it.dharma.body.isNotEmpty()) {
                            // TODO: color
//                                            val argsCount = it.name.count { it == '$' }
//
//                                            repeat(argsCount) {
//                                                dharmaChain[dharmaChain.size - 1 - it - 1].color =
//                                                    Color.Yellow
//                                            }
                        }

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
fun GreetingPreview() {
    TrueProgrammingLanguageTheme {
//        MainScreenView(dharmaChain = dharmaChainToName)
    }
}


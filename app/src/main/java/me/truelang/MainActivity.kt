package me.truelang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.truelang.lang.interpretCode
import me.truelang.lang.transformAtomsChain
import me.truelang.lang.dharmasMap
import me.truelang.ui.theme.TrueProgrammingLanguageTheme
import kotlin.reflect.KClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrueProgrammingLanguageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreenView(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


data class TemplateItem(var id: Int, val dharma: EditorItem.Dharma)

sealed class EditorItem(open val id: Int) {
    data class Dharma( // Transformation, Presenter, Map, Function, Producer, Action, Operation
        override val id: Int,
        var name: String,
        var template: String,
        var color: Color = Color.White
    ) : EditorItem(id)

    data class EndOfChain(
        override val id: Int,
    ) : EditorItem(id)

    data class Message(
        override val id: Int,
        var text: String,
    ) : EditorItem(id)
}


// TODO: подсвечивать функции с необходимым кол-вом аргументов, остальные дизэйблить
// TODO: Брать предыдущий атом если | и следующий если $

@Composable
fun MainScreenView(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    val dharmaChains = remember {
        mutableStateListOf<EditorItem>().apply {
            add(EditorItem.Message(size, "Let's start!"))
            add(EditorItem.Dharma(size, "10", ""))
            add(EditorItem.Dharma(size, "10", ""))
            add(EditorItem.Dharma(size, "20", ""))
            add(EditorItem.Dharma(size, "multiply", ""))
            add(EditorItem.Dharma(size, "print", ""))
            add(EditorItem.EndOfChain(size))
// todo
//            add("Column")
//            add("Spacer")
//            add("Text")
//            add("Button")
//            add("Spacer")
//            add("print")

        }
    }
    val dharmaTemplates = remember {
        dharmasMap.keys.mapIndexed { index, key ->
            TemplateItem(
                id = index,
                dharma = EditorItem.Dharma(id = -1, name = key, template = dharmasMap[key] ?: "")
            )

        }.toMutableStateList()
    }

    var selectedDharmaItem by remember { mutableStateOf<EditorItem.Dharma?>(null) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    Surface(
        modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn {
                items(dharmaChains.size, key = { i -> dharmaChains[i].id }) { i ->
                    val dharma = dharmaChains[i]
                    when (dharma) {
                        is EditorItem.Dharma -> {
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

                        is EditorItem.EndOfChain -> {
                            Row {
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "Name",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            println("Add Name")
                                        })
                            }
                        }

                        is EditorItem.Message -> {
                            Row {
                                Spacer(Modifier.weight(1f))
                                Text("Name")
                            }
                        }
                    }
                }

                item {
                    ConsoleView(dharmaChains)
                }

                item {
                    Row {
                        TextField(value = input, onValueChange = {
                            input = it
                        }, modifier = Modifier.weight(1f))
                        Button(onClick = {
                            if (input.isEmpty()) {
                                dharmaChains.add(EditorItem.EndOfChain(dharmaChains.size))

                            } else {
                                val parts = input.split("=")
                                val template = parts[0]
                                val name = if (parts.size > 1) parts[1] else ""
                                val dharma = EditorItem.Dharma(
                                    id = dharmaChains.size,
                                    name = name,
                                    template = template
                                )
                                dharmaChains.add(dharma)

                                input = ""
                            }

                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Post")
                        }
                    }
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
                        template = template,
                        id = selectedDharmaItem?.id!! * 10,
                    )
                    selectedDharmaItem = null
                })
            }
        }
    }
}

@Composable
fun DharmaItemView(
    dharma: EditorItem.Dharma,
    i: Int,
    dharmaChain: SnapshotStateList<EditorItem>,
    onItemSelected: (EditorItem) -> Unit
) {

    Row(
        Modifier
            .background(dharma.color)
            .clickable {
                onItemSelected(dharma)
            }) {
        Text(
            text = dharma.name + " = " + dharma.template,
            modifier = Modifier.weight(1f)
        )

        if (dharma.template.trim().isNotEmpty()) {
            Button(onClick = {
                dharmaChain.removeAt(i)
            }) {
                Icon(Icons.Default.Close, "Remove")
            }
        }
    }
}

class MathList<OTHER, SELECTED> {
    val other = mutableListOf<OTHER>()
    val selected = mutableListOf<SELECTED>()
}

fun <T> MutableList<T>.minus(count: Int, fromEnd: Boolean = false): MathList<T, T> {
    return MathList<T, T>().apply {
        if (fromEnd) {
            repeat(count) { i ->
                selected.add(this@minus.removeAt(this@minus.size - 1 - i))
            }
        } else {
            repeat(count) { i ->
                selected.add(this@minus.removeAt(i))
            }
        }

        other.addAll(this@minus)
    }
}

inline fun <reified O : Any, S : Any> MutableList<O>.divide(
    type: KClass<S>,
    fromEnd: Boolean = false,
    untilFirstDifferent: Boolean = false
): MathList<O, S> {
    return MathList<O, S>().apply {
        var i = 0
        fun isTypeToSelect(): Boolean = this@divide[i].javaClass == type.java

        fun updateSelected() {
            if (isTypeToSelect()) {
                selected.add(this@divide.removeAt(i) as S)
            }
        }

        if (fromEnd) {
            i = this@divide.size - 1
            while (i >= 0) {
                if (untilFirstDifferent && isTypeToSelect()) {
                    break
                }

                updateSelected()
                i--
            }

        } else {
            i = 0
            while (i < this@divide.size) {
                if (untilFirstDifferent && isTypeToSelect()) {
                    break
                }

                updateSelected()
                i++
            }
        }

        other.addAll(this@divide)
    }
}

@Composable
private fun ConsoleView(dharmaChain: SnapshotStateList<EditorItem>) {
    Column(modifier = Modifier.background(Color.Gray)) {

        var code = ""
        var lastChain = dharmaChain.divide(
            type = EditorItem.Dharma::class,
            fromEnd = true,
            untilFirstDifferent = true
        ).selected
        try {
            code = transformAtomsChain(lastChain)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Text("Kotlin: $code")

        var console = ""
        try {
            console = interpretCode(
                result = transformAtomsChain(lastChain),
                transformation = lastChain[lastChain.size - 1].template
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Text("Console: $console")
    }
}

@Composable
fun TemplateEditorView(
    selectedEditorItem: EditorItem?,
    onSaveClick: (name: String, template: String) -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        when (selectedEditorItem) {
            is EditorItem.Dharma -> {
                Column {
                    var templateValue by remember {
                        mutableStateOf(
                            selectedEditorItem.template
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
                            selectedEditorItem.name
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
                    if (it.dharma.template.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
                Button(
                    colors = ButtonDefaults.buttonColors()
                        .copy(containerColor = containerColor),
                    onClick = {
                        println("here! $it")
                        if (it.dharma.template.isNotEmpty()) {
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
        MainScreenView()
    }
}


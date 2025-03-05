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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.truelang.lang.interpretCode
import me.truelang.lang.transformAtomsChain
import me.truelang.lang.transformationsMap
import me.truelang.ui.theme.TrueProgrammingLanguageTheme
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrueProgrammingLanguageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Main(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class DharmaItem(var id: Int, val dharma: Dharma, var color: Color = Color.White)
data class Dharma(var name: String, var body: String)

val emptyDharma = Dharma("", "")
val emptyDharmaItem = DharmaItem(-1, emptyDharma)

// TODO: подсвечивать функции с необходимым кол-вом аргументов, остальные дизэйблить
// TODO: Брать предыдущий атом если | и следующий если $

@Composable
fun Main(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    val dharmaChain = remember {
        mutableStateListOf<DharmaItem>().apply {
            add(DharmaItem(size, Dharma("10", "")))
            add(DharmaItem(size, Dharma("20", "")))
            add(DharmaItem(size, Dharma("multiply", "")))
            add(DharmaItem(size, Dharma("print", "")))
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
        transformationsMap.keys.mapIndexed { index, key ->
            DharmaItem(id = index, dharma = Dharma(key, transformationsMap[key] ?: ""))
        }.toMutableStateList()
    }

    var selectedDharmaItem by remember { mutableStateOf<DharmaItem?>(null) }
    var selectedDharmaItemIndex by remember { mutableIntStateOf(0) }

    Surface(
        modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f)) {
                    items(dharmaChain.size, key = { i -> dharmaChain[i].id }) { i ->
                        val dharmaItem = dharmaChain[i]
                        Row(
                            Modifier
                                .background(dharmaItem.color)
                                .clickable {
                                    selectedDharmaItem = dharmaItem
                                    selectedDharmaItemIndex = i
                                }) {
                            Text(
                                text = dharmaItem.dharma.name + " = " + dharmaItem.dharma.body,
                                modifier = Modifier.weight(1f)
                            )

                            if (dharmaItem.dharma.body.trim().isNotEmpty()) {
                                Button(onClick = {
                                    dharmaChain.removeAt(i)
                                }) {
                                    Icon(Icons.Default.Close, "Remove")
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.background(Color.Gray)) {
                            val index = max(0, dharmaChain.lastIndexOf(emptyDharmaItem))
                            var code = ""
                            var dharmasLastChain = mutableListOf<DharmaItem>()
                            try {
                                dharmasLastChain = dharmaChain.subList(index, dharmaChain.size)
                                code = transformAtomsChain(dharmasLastChain)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            Text("Kotlin: $code")

                            var console = ""
                            try {
                                console = interpretCode(
                                    result = transformAtomsChain(dharmasLastChain),
                                    transformation = dharmasLastChain[dharmasLastChain.size - 1].dharma.body
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            Text("Console: $console")
                        }
                    }

                    item {
                        Row {
                            TextField(value = input, onValueChange = {
                                input = it
                            }, modifier = Modifier.weight(1f))
                            Button(onClick = {
                                if (input.isEmpty()) {
                                    dharmaChain.add(emptyDharmaItem)
                                } else {
                                    val parts = input.split("=")
                                    val body = parts[0]
                                    val name = if (parts.size > 1) parts[1] else ""
                                    val dharma = Dharma(body, name)
                                    dharmaChain.add(
                                        DharmaItem(
                                            dharmaChain.size,
                                            dharma,
                                            Color.White
                                        )
                                    )
                                    if (!dharmaTemplates.any { it.dharma.name == input }) {
                                        dharmaTemplates.add(
                                            DharmaItem(
                                                id = dharmaTemplates.size,
                                                dharma = Dharma(
                                                    name = "",
                                                    body = input
                                                )
                                            )
                                        )
                                    }
                                    input = ""
                                }

                            }) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Post")
                            }
                        }
                    }

                    item {
                        LazyHorizontalStaggeredGrid(
                            modifier = Modifier.height(120.dp),
                            rows = StaggeredGridCells.Adaptive(32.dp),
                            horizontalItemSpacing = 4.dp,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            content = {
                                items(dharmaTemplates, key = {
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

                                            dharmaChain.add(DharmaItem(dharmaChain.size, it.dharma))

                                        }) {
                                        Text(it.dharma.name)
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            AnimatedVisibility(selectedDharmaItem != null) {
                BackHandler {
                    selectedDharmaItem = null
                }

                Surface(Modifier.fillMaxSize()) {
                    Column {
                        var bodyValue by remember {
                            mutableStateOf(
                                selectedDharmaItem?.dharma?.body ?: ""
                            )
                        }
                        Row {
                            TextField(
                                value = bodyValue,
                                label = { Text("Body") },
                                onValueChange = {
                                    bodyValue = it
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        var nameValue by remember {
                            mutableStateOf(
                                selectedDharmaItem?.dharma?.name ?: ""
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
                            dharmaChain[selectedDharmaItemIndex] = selectedDharmaItem!!.copy(
                                dharma = Dharma(nameValue, bodyValue),
                                id = selectedDharmaItem?.id!! * 10,
                            )
                            selectedDharmaItem = null
                        }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrueProgrammingLanguageTheme {
        Main()
    }
}


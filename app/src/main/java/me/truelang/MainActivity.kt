package me.truelang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.runtime.key
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
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class AtomItem(val data: String, var color: Color = Color.White)
data class TransformationItem(val id: Int, val value: String, val isInited: Boolean)

val emptyAtomItem = AtomItem("       ")

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    val atomItems = remember {
        mutableStateListOf<AtomItem>().apply {
            add(AtomItem("10"))
            add(AtomItem("20"))
            add(AtomItem("multiply"))
            add(AtomItem("print"))
// todo
//            add("Column")
//            add("Spacer")
//            add("Text")
//            add("Button")
//            add("Spacer")
//            add("print")

        }
    }
    val transformationItems = remember {
        transformationsMap.keys.mapIndexed { index, it ->
            TransformationItem(id = index, value = it, isInited = true)
        }.toMutableStateList()
    }

    var inputAtomModeEnabled by remember { mutableStateOf(false) }

    Surface(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        Box {
            LazyColumn {
                items(atomItems.size, key = { i -> atomItems[i].data + atomItems[i].color }) { i ->
                    val atom = atomItems[i]
                    Row(Modifier.background(atom.color)) {
                        Text(text = atom.data, modifier = Modifier.weight(1f))
                        if (atom.data.isNotEmpty()) {
                            Button(onClick = {
                                atomItems.removeAt(i)
                            }) {
                                Icon(Icons.Default.Close, "Remove")
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.background(Color.Gray)) {
                        val index = max(0, atomItems.lastIndexOf(emptyAtomItem))
                        var code = ""
                        var atomsLastChain = mutableListOf<AtomItem>()
                        try {
                            atomsLastChain = atomItems.subList(index, atomItems.size)
                            code = transformAtomsChain(atomsLastChain)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Text("Kotlin: $code")

                        var console = ""
                        try {
                            console = interpretCode(
                                result = transformAtomsChain(atomsLastChain),
                                transformation = atomsLastChain[atomsLastChain.size - 1].data
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
                                atomItems.add(emptyAtomItem)
                            } else {
                                atomItems.add(AtomItem(input, Color.White))
                                if (!transformationItems.any { it.value == input }) {
                                    transformationItems.add(
                                        TransformationItem(
                                            id = transformationItems.size,
                                            value = input,
                                            isInited = false
                                        )
                                    )
                                }
                                input = ""
                            }
                            inputAtomModeEnabled = false

                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Post")
                        }
                    }
                }

                item() {
                    LazyHorizontalStaggeredGrid(
                        modifier = Modifier.height(120.dp),
                        rows = StaggeredGridCells.Adaptive(32.dp),
                        horizontalItemSpacing = 4.dp,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        content = {
                            items(transformationItems, key = {
                                it.id
                            }) { it ->
                                val containerColor =
                                    if (it.isInited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
                                Button(
                                    colors = ButtonDefaults.buttonColors()
                                        .copy(containerColor = containerColor),
                                    onClick = {
                                        println("here! $it")
                                        if (it.isInited) {
                                            val argsCount = it.value.count { it == '$' }

                                            repeat(argsCount) {
                                                atomItems[atomItems.size - 1 - it - 1].color =
                                                    Color.Yellow
                                            }
                                        }

                                        atomItems.add(AtomItem(it.value))

                                    }) {
                                    Text(it.value)
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
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrueProgrammingLanguageTheme {
        Greeting("Android")
    }
}


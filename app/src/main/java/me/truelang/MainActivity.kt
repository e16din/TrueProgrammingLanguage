package me.truelang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.truelang.ui.theme.TrueProgrammingLanguageTheme
import kotlin.math.max
import kotlin.math.min
import kotlin.text.trim

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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    val items = remember {
        mutableStateListOf<String>().apply {
            add("10")
            add("20")
            add("multiply")
            add("println")
        }
    }
    val transformations = transformationsMap.keys

    var inputAtomModeEnabled by remember { mutableStateOf(false) }

    Surface(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        Box {
            LazyColumn {
                items(items.size) { i ->
                    val atom = items[i]
                    Row {
                        Text(text = atom, modifier = Modifier.weight(1f))
                        if (atom.isNotEmpty()) {
                            Button(onClick = {
                                items.removeAt(i)
                            }) {
                                Icon(Icons.Default.Close, "Remove")
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.background(Color.Gray)) {
                        val index = max(0, items.lastIndexOf(""))
                        var code = ""
                        var atomsLastChain = mutableListOf<String>()
                        try {
                            atomsLastChain = items.subList(index, items.size)
                            code = transformToKotlin(atomsLastChain)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Text("Kotlin: $code")

                        var console = ""
                        try {
                            console = interpretCode(
                                data = atomsLastChain[atomsLastChain.size-2],
                                transformation = atomsLastChain[atomsLastChain.size-1]
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
                            items.add(input)
                            inputAtomModeEnabled = false
                            input = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Post")
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        transformations.forEach {
                            Button(onClick = {
                                items.add(it)
                            }) {
                                Text(it)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                }

            }
        }
    }
}

fun interpretateMath(data: String): Double {
    val allAtoms = mutableListOf<String>()
    data.replace("L", "").replace("l", "").forEach {
        var number = ""
        when (it) {
            '*', '/', '+', '-' -> {
                allAtoms.add(number.trim())
                number = ""
                allAtoms.add("$it")
            }

            else -> {
                number += it
            }
        }
    }

    val multedAtoms = mutableListOf<Double>()
    for (i in 0 until allAtoms.size) {
        val current = allAtoms[i]
        when (current) {
            "*" -> multedAtoms.add(allAtoms[i - 1].toDouble() * allAtoms[i + 1].toDouble())
            "/" -> multedAtoms.add(allAtoms[i - 1].toDouble() / allAtoms[i + 1].toDouble())
            "+", "-" -> {}
            else -> multedAtoms.add(current.toDouble())
        }
    }

    var result = 0.0

    for (i in 0 until multedAtoms.size) {
        val current = allAtoms[i]
        when (current) {
            "+" -> multedAtoms.add(allAtoms[i - 1].toDouble() + allAtoms[i + 1].toDouble())
            "-" -> multedAtoms.add(allAtoms[i - 1].toDouble() - allAtoms[i + 1].toDouble())
            else -> throw IllegalArgumentException()
        }
    }

    // todo: handle brackets

    return result
}

fun interpretCode(data: String, transformation: String): String {
    return when (transformation) {
        "println" -> "${interpretateMath(data)}"
        else -> ""
    }
}

var transformationsSource = """
        $ * $ = multiply
        
        println($) = println
        
        $ + $ = add
       
""".trimIndent()

fun fillTransformations(): MutableMap<String, String> {
    var transformationsMap = mutableMapOf<String, String>() // name, body
    var index = transformationsSource.indexOf('=')
    var rightIndex = 0

    while (true) {
        val leftIndex = max(0, transformationsSource.indexOfInverse('\n', startIndex = index))
        rightIndex = min(
            transformationsSource.length,
            transformationsSource.indexOf('\n', startIndex = index)
        )
        val body = transformationsSource.substring(leftIndex, index).trim()
        val name = transformationsSource.substring(index + 1, rightIndex).trim()

        transformationsMap.put(name, body)

        transformationsSource = transformationsSource.replaceRange(leftIndex, rightIndex, "")
        index = transformationsSource.indexOf('=', startIndex = 0)

        if (index < 0) {
            break
        }
    }
    return transformationsMap
}

val transformationsMap = fillTransformations()

fun transformToKotlin(atoms: MutableList<String>): String {
    val endOfChainStr = ""

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrueProgrammingLanguageTheme {
        Greeting("Android")
    }
}
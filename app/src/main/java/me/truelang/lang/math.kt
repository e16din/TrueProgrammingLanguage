package me.truelang.lang

class MathList<OTHER : Any, SELECTED : Any> {
    val other = mutableListOf<OTHER>()
    val selected = mutableListOf<SELECTED>()
    val selectedIndices = mutableListOf<Int>()
}

class MathString {
    var other = ""
    var selected = ""
}

infix fun String.minusFromEnd(count: Int): MathString {
    return minus(count, fromEnd = true)
}

infix fun String.minus(count: Int): MathString {
    return minus(count, fromEnd = false)
}

fun String.minus(count: Int, fromEnd: Boolean = false): MathString {
    val builderOther = StringBuilder(this)
    val builderSelected = StringBuilder("")

    fun updateContainers(index: Int) {
        val char = builderOther[index]
        builderOther.replace(index, index, "")
        builderSelected.insert(0, char)
    }

    if (fromEnd) {
        repeat(count) { i ->
            updateContainers(this@minus.length - 1 - i)
        }
    } else {
        repeat(count) { i ->
            updateContainers(i)
        }
    }

    return MathString().apply {
        selected = builderSelected.toString()
        other = builderOther.toString()
    }
}

infix fun <T : Any> List<T>.minusFromEnd(count: Int): MathList<T, T> {
    return minus(count, fromEnd = true)
}

infix fun <T : Any> List<T>.minus(count: Int): MathList<T, T> {
    return minus(count, fromEnd = false)
}

infix fun <T : Any> List<T>.plus(value: T): List<T> {
    return plus(1, value, false)
}

infix fun <T : Any> List<T>.plusToStart(value: T): List<T> {
    return plus(1, value, true)
}

fun <T : Any> List<T>.plus(count: Int, value: T, toStart: Boolean = false): List<T> {
    val result = this.toMutableList()
    repeat(count) {
        if (toStart) {
            result.add(0, value)
        } else {
            result.add(value)
        }
    }
    return result
}

fun String.plus(count: Int, value: String): String {
    var result = ""
    repeat(count) {
        result += value
    }
    return result
}

fun <T : Any> List<T>.minus(count: Int, fromEnd: Boolean = false): MathList<T, T> {
    return MathList<T, T>().apply {
        val source = this@minus.toMutableList()

        if (fromEnd) {
            repeat(count) { i ->
                val index = source.size - 1 - i
                selected.add(source.removeAt(index))
                selectedIndices.add(index)
            }
        } else {
            repeat(count) { i ->
                selected.add(source.removeAt(i))
                selectedIndices.add(i)
            }
        }

        other.addAll(source)
    }
}

inline fun String.divide(
    itemSize: Int = 1,
    fromIndex:Int = 0,
    toIndex:Int = this.length-1,
    betweenStart:String = "",
    betweenEnd:String = "",
    selectCondition: (it: String) -> Boolean = { true },
    breakCondition: (it: String) -> Boolean = { false },
    fromEnd: Boolean = false
): MathList<String, String> {

}

fun <O : Any, S : Any> MutableList<O>.divide(
    selectCondition: (it: O, selected: List<S>) -> Boolean = { _, _ -> true },
    breakCondition: (it: O, selected: List<S>) -> Boolean = { _, _ -> false },
    fromEnd: Boolean = false
): MathList<O, S> {
    return MathList<O, S>().apply {
        var i = 0

        fun updateSelected() {
            if (selectCondition(this@divide[i], selected)) {
                selected.add(this@divide.removeAt(i) as S)
                selectedIndices.add(i)
            }
        }

        if (fromEnd) {
            i = this@divide.size - 1
            while (i >= 0) {
                if (breakCondition(this@divide[i], selected)) {
                    break
                }

                updateSelected()
                i--
            }

        } else {
            i = 0
            while (i < this@divide.size) {
                if (breakCondition(this@divide[i], selected)) {
                    break
                }

                updateSelected()
                i++
            }
        }

        other.addAll(this@divide)
    }
}
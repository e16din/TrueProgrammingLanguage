package me.truelang.lang

class MathList<OTHER : Any, SELECTED : Any> {
    val other = mutableListOf<OTHER>()
    val selected = mutableListOf<SELECTED>()
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

fun <T : Any> List<T>.plus(count: Int, value: T, toStart:Boolean = false): List<T> {
    val result = this.toMutableList()
    repeat(count) {
        if(toStart) {
            result.add(0, value)
        }else {
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
                selected.add(source.removeAt(source.size - 1 - i))
            }
        } else {
            repeat(count) { i ->
                selected.add(source.removeAt(i))
            }
        }

        other.addAll(source)
    }
}

inline fun <reified O : Any, S : Any> MutableList<O>.divide(
    crossinline selectCondition: (it: O) -> Boolean,
    breakCondition: (it: O) -> Boolean,
    fromEnd: Boolean = false
): MathList<O, S> {
    return MathList<O, S>().apply {
        var i = 0

        fun updateSelected() {
            if (selectCondition(this@divide[i])) {
                selected.add(this@divide.removeAt(i) as S)
            }
        }

        if (fromEnd) {
            i = this@divide.size - 1
            while (i >= 0) {
                if (breakCondition(this@divide[i])) {
                    break
                }

                updateSelected()
                i--
            }

        } else {
            i = 0
            while (i < this@divide.size) {
                if (breakCondition(this@divide[i])) {
                    break
                }

                updateSelected()
                i++
            }
        }

        other.addAll(this@divide)
    }
}
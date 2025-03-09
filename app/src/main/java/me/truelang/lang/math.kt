package me.truelang.lang


class SelectList<OTHER : Any, SELECTED : Any> {
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

    if (this.isNotEmpty()) {
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
    }

    return MathString().apply {
        selected = builderSelected.toString()
        other = builderOther.toString()
    }
}

infix fun <T : Any> List<T>.minusFromEnd(count: Int): SelectList<T, T> {
    return minus(count, fromEnd = true)
}

infix fun <T : Any> List<T>.minus(count: Int): SelectList<T, T> {
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

fun <T : Any> List<T>.minus(itemsCount: Int, fromEnd: Boolean = false): SelectList<T, T> {
    return SelectList<T, T>().apply {
        val source = this@minus.toMutableList()

        if (source.isNotEmpty()) {
            if (fromEnd) {
                repeat(itemsCount) { i ->
                    val index = source.size - 1 - i
                    selected.add(source.removeAt(index))
                    selectedIndices.add(index)
                }
            } else {
                repeat(itemsCount) { i ->
                    selected.add(source.removeAt(i))
                    selectedIndices.add(i)
                }
            }
        }

        other.addAll(source)
    }
}

fun <T : Any> List<T>.divide(partsCount: Int, fromEnd: Boolean = false): List<List<T>> {
    if (this.isEmpty()) {
        return emptyList()
    }

    val source = this@divide.toMutableList()
    val result = mutableListOf<MutableList<T>>()

    repeat(partsCount) {
        result.add(mutableListOf())
    }

    var i = 0
    while (source.isNotEmpty()) {
        result[i].add(
            source.removeAt(
                if (fromEnd) source.size - 1 else 0
            )
        )

        i++
        if (i == partsCount - 1) {
            i = 0
        }
    }
    return result
}

fun String.select(
    start: String,
    selectCondition: (it: String, selected: List<String>) -> Boolean = { _, _ -> true },
    breakCondition: (it: String, selected: List<String>) -> Boolean = { _, _ -> false },
    fromEnd: Boolean = false
): SelectList<String, String> {
    val result = SelectList<String, String>()

    if (this.isEmpty()) {
        return result
    }

    var it = StringBuilder()

    var startIndex = if (fromEnd) this.indexOf(start, last = true) else this.indexOf(start)
    while (startIndex != -1) {
        val ints = if (fromEnd)
            this.length - 1 downTo startIndex
        else
            startIndex until this.length

        for (i in ints) {
            it.append(this[i])
            val current = it.toString()
            if (breakCondition(current, result.selected)) {
                return result
            }

            if (selectCondition(current, result.selected)) {
                result.selected.add(current)
                it.clear()
                break
            }
        }

        startIndex = this.indexOf(start, startIndex + 1)
    }

    return result
}

fun CharSequence.indexOf(
    other: CharSequence,
    startIndex: Int = 0,
    endIndex: Int = this.length - 1,
    ignoreCase: Boolean = false,
    last: Boolean = false
): Int {
    val indices = if (!last)
        startIndex.coerceAtLeast(0)..endIndex.coerceAtMost(length)
    else
        startIndex.coerceAtMost(lastIndex) downTo endIndex.coerceAtLeast(0)

    for (index in indices) {
        if (other.regionMatches(0, this, index, other.length, ignoreCase))
            return index
    }

    return -1
}

fun <O : Any, S : Any> MutableList<O>.select(
    selectCondition: (it: O, selected: List<S>) -> Boolean = { _, _ -> true },
    breakCondition: (it: O, selected: List<S>) -> Boolean = { _, _ -> false },
    fromEnd: Boolean = false
): SelectList<O, S> {
    return SelectList<O, S>().apply {
        if (this@select.isNotEmpty()) {
            var i = 0

            fun updateSelected() {
                if (selectCondition(this@select[i], selected)) {
                    selected.add(this@select.removeAt(i) as S)
                    selectedIndices.add(i)
                }
            }

            if (fromEnd) {
                i = this@select.size - 1
                while (i >= 0) {
                    if (breakCondition(this@select[i], selected)) {
                        break
                    }

                    updateSelected()
                    i--
                }

            } else {
                i = 0
                while (i < this@select.size) {
                    if (breakCondition(this@select[i], selected)) {
                        break
                    }

                    updateSelected()
                    i++
                }
            }
        }

        other.addAll(this@select)
    }
}
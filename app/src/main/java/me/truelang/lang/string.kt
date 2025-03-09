package me.truelang.lang


fun String.replaceAll(
    strings: List<String>,
    newValue: String = "",
    ignoreCase: Boolean = false
): String {
    var result = this

    strings.forEach {
        result = result.replace(it, newValue, ignoreCase)
    }

    return result
}

fun String.endsWithAny(strings: List<String>): Boolean {
    strings.forEach {
        if (this.endsWith(it)) {
            return true
        }
    }

    return false
}

fun String.indexOf(char: Char, startIndex: Int = 0): Int {
    var index = startIndex
    while (index < this.length) {
        if (this[index] == char) {
            return index
        }
        index += 1
    }

    return -1
}


fun String.indexOfInverse(char: Char, startIndex: Int = this.length - 1): Int {
    var index = startIndex
    while (index >= 0) {
        if (this[index] == char) {
            return index
        }
        index -= 1
    }

    return -1
}
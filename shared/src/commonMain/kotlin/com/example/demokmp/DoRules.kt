package com.example.demokmp

// Rule 1: a Daikin DO is exactly 9 digits
fun isValidDo(text: String): Boolean =
    text.length == 9 && text.all { it.isDigit() }

// Rule 2: the camera must see the same number twice in a row before we accept it
// (same idea as ocrdemo confirmAndAdd)
class ScanConfirmer {
    private var pending: String? = null   // number seen in the previous frame

    // Returns the number when it is confirmed, otherwise null
    fun offer(candidate: String?, alreadyHave: List<String>): String? {
        if (candidate == null || !isValidDo(candidate) || candidate in alreadyHave) {
            pending = null                 // nothing valid, start over
            return null
        }
        return if (candidate == pending) {
            pending = null
            candidate                      // second sighting → confirmed
        } else {
            pending = candidate            // first sighting → wait
            null
        }
    }
}
// One line of text found in a camera frame, with its position
// (bigger top = lower on the page)
data class TextLine(
    val text: String,
    val left: Float, val top: Float,
    val right: Float, val bottom: Float
)

// Same logic as ocrdemo findDOText():
// 1. find the "Number/Date" label
// 2. take the closest line BELOW it, in the same column
// 3. return the first all-digit word ("802141782"), skipping "/" and the date
fun findDoNumber(lines: List<TextLine>): String? {
    val label = lines.firstOrNull {
        it.text.replace(" ", "").contains("Number/Date", ignoreCase = true)
    } ?: return null
    val labelCenterY = (label.top + label.bottom) / 2

    val valueLine = lines
        .filter { it !== label }
        .filter { it.top > labelCenterY && it.left <= label.right && it.right >= label.left }
        .minByOrNull { it.top }
        ?: return null

    return valueLine.text.split(" ").firstOrNull { w -> w.isNotEmpty() && w.all { it.isDigit() } }
}
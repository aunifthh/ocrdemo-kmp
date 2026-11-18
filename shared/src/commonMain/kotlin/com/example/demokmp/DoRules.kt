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
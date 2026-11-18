package com.example.demokmp

// ok = true  → short buzz (accepted)
// ok = false → longer buzz (duplicate / invalid)
expect fun vibrate(ok: Boolean)
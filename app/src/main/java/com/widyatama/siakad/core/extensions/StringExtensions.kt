package com.widyatama.siakad.core.extensions

fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return this.matches(emailRegex.toRegex())
}

fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

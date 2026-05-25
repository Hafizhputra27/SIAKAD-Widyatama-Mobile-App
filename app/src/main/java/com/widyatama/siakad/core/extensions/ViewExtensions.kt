package com.widyatama.siakad.core.extensions

import android.view.View

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.toggleVisibility() { visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE }

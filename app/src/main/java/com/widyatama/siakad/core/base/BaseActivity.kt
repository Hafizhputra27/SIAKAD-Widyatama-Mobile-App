package com.widyatama.siakad.core.base

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    protected fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    protected fun showLoading(view: View) { view.visibility = View.VISIBLE }
    protected fun hideLoading(view: View) { view.visibility = View.GONE }
}

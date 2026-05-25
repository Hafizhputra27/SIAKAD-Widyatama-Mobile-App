package com.widyatama.siakad.core.base

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun showLoading(view: View) { view.visibility = View.VISIBLE }
    protected fun hideLoading(view: View) { view.visibility = View.GONE }
}

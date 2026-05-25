package com.widyatama.siakad.ui.profile

<<<<<<< HEAD:app/src/main/java/com/example/myapplication/SettingsFragment.kt
=======
import android.app.AlertDialog
import android.content.Context
>>>>>>> f3aa7b186584d76fe2b7bd4a0fe384862cfc8973:app/src/main/java/com/widyatama/siakad/ui/profile/SettingsFragment.kt
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.widyatama.siakad.databinding.FragmentSettingsBinding
import com.widyatama.siakad.ui.auth.LoginActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }
    private var progressDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         
        binding.ivSettings.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.btnLogout.setOnClickListener {
<<<<<<< HEAD:app/src/main/java/com/example/myapplication/SettingsFragment.kt
            UserSession.isLoggedIn = false
            UserSession.resetToDefault()
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
=======
            logout()
>>>>>>> f3aa7b186584d76fe2b7bd4a0fe384862cfc8973:app/src/main/java/com/widyatama/siakad/ui/profile/SettingsFragment.kt
        }
    }

    private fun logout() {
        prefs.edit().clear().apply()
        
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

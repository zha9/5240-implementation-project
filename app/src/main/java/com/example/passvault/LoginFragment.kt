package com.example.passvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.passvault.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Null binding!"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.apply {
            loginButton.setOnClickListener { handleLogin() }
        }
        return binding.root
    }

    private fun handleLogin() {
        val username = binding.usernameEntry.text.toString()
        val password = binding.passwordEntry.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Username/Password cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        val databaseName = generateDatabaseName(username, password)

        ItemsRepository.get().initializeDatabase(databaseName, password)
        findNavController().navigate(R.id.login_success)
    }

    private fun generateDatabaseName(username: String, password: String): String {
        return "${username}_${password.hashCode()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
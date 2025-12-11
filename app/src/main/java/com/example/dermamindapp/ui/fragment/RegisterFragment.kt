// main/java/com/example/dermamindapp/ui/fragment/RegisterFragment.kt (Revisi)

package com.example.dermamindapp.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.dermamindapp.R
import com.example.dermamindapp.ui.viewmodel.AuthViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel

    // Deklarasi View
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    // >> BARU: Tambahkan konfirmasi password
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoginLink: TextView
    private lateinit var tvUsernameStatus: TextView
    private lateinit var cgUsernameSuggestions: ChipGroup
    private lateinit var tvSuggestionTitle: TextView
    private lateinit var tilPassword: TextInputLayout
    // >> BARU: Tambahkan konfirmasi password TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // 1. Inisialisasi View (findViewById)
        etUsername = view.findViewById(R.id.etUsername)
        etPassword = view.findViewById(R.id.etPassword)
        // >> BARU: Inisialisasi etConfirmPassword & tilConfirmPassword
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        progressBar = view.findViewById(R.id.progressBar)
        tvLoginLink = view.findViewById(R.id.tvLoginLink)
        tvUsernameStatus = view.findViewById(R.id.tvUsernameStatus)
        cgUsernameSuggestions = view.findViewById(R.id.cgUsernameSuggestions)
        tvSuggestionTitle = view.findViewById(R.id.tvSuggestionTitle)
        tilPassword = view.findViewById(R.id.tilPassword)
        // >> BARU: Inisialisasi tilConfirmPassword
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword)

        setupUsernameCheck()
        setupObservers()

        btnRegister.setOnClickListener {
            tilPassword.error = null
            tilConfirmPassword.error = null // Clear error confirm password

            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString() // >> BARU: Ambil Conf Password

            if (password.length < 6) {
                tilPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            // >> BARU: Cek Konfirmasi Password
            if (password != confirmPassword) {
                tilConfirmPassword.error = "Konfirmasi Password tidak cocok!"
                return@setOnClickListener
            }

            authViewModel.register(username, password)
        }

        tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun setupUsernameCheck() {
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvUsernameStatus.visibility = View.GONE
                cgUsernameSuggestions.removeAllViews()
                tvSuggestionTitle.visibility = View.GONE

                searchRunnable?.let { searchHandler.removeCallbacks(it) }
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.length >= 3) {
                    searchRunnable = Runnable {
                        authViewModel.checkUsernameAvailability(input)
                    }
                    searchHandler.postDelayed(searchRunnable!!, 800)
                }
            }
        })
    }

    private fun setupObservers() {
        authViewModel.usernameStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AuthViewModel.UsernameStatus.Checking -> {
                    showStatus("Mengecek...", Color.GRAY)
                }
                is AuthViewModel.UsernameStatus.Available -> {
                    showStatus("✅ Username tersedia!", Color.parseColor("#4CAF50"))
                }
                is AuthViewModel.UsernameStatus.Taken -> {
                    showStatus("❌ Yah, username sudah dipakai.", Color.RED)
                    showSuggestions(status.suggestions)
                }
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            // Tambahkan disable/enable untuk field baru
            etConfirmPassword.isEnabled = !isLoading
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }
        }

        authViewModel.loginSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_profileSetupFragment)
            }
        }
    }

    private fun showStatus(msg: String, color: Int) {
        tvUsernameStatus.text = msg
        tvUsernameStatus.setTextColor(color)
        tvUsernameStatus.visibility = View.VISIBLE
    }

    private fun showSuggestions(suggestions: List<String>) {
        tvSuggestionTitle.visibility = View.VISIBLE
        cgUsernameSuggestions.visibility = View.VISIBLE
        cgUsernameSuggestions.removeAllViews()

        for (suggestion in suggestions) {
            val chip = Chip(context)
            chip.text = suggestion
            chip.isCheckable = false
            chip.setOnClickListener {
                etUsername.setText(suggestion)
                etUsername.setSelection(suggestion.length)
            }
            cgUsernameSuggestions.addView(chip)
        }
    }
}
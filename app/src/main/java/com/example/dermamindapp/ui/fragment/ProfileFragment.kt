package com.example.dermamindapp.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.ui.viewmodel.ProfileViewModel
// Pastikan import R sesuai package Anda

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var progressBar: ProgressBar // Tambahkan ProgressBar di XML biar bagus

    // Launcher untuk memilih gambar dari galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Saat gambar dipilih, langsung upload
            viewModel.uploadProfilePicture(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture) // Pastikan ID ini ada di XML
        tvUserName = view.findViewById(R.id.profile_name)
        // progressBar = view.findViewById(R.id.progressBar) // Opsional

        // Klik foto -> Buka Galeri
        ivProfilePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        setupObservers()

        // Load data awal
        viewModel.loadProfile()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                tvUserName.text = profile.name.ifEmpty { "Pengguna Baru" }

                // Load gambar dengan Glide
                if (profile.photoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(profile.photoUrl)
                        .placeholder(R.drawable.ic_profile_avatar) // Gambar default
                        .circleCrop() // Agar bulat
                        .into(ivProfilePicture)
                }
            }
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Tampilkan/Sembunyikan loading jika ada progress bar
            // progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
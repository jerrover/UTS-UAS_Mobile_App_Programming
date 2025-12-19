package com.example.dermamindapp.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.UserProfile
import com.example.dermamindapp.ui.viewmodel.ProfileViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    // UI Components
    private lateinit var ivProfilePicture: ImageView
    private lateinit var progressBarPhoto: ProgressBar
    private lateinit var tvUserName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvSkinType: TextView
    private lateinit var tvPreferences: TextView
    private lateinit var tvRoutines: TextView

    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfilePicture(uri)
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

        val cardMyShelf = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardMyShelf)

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        progressBarPhoto = view.findViewById(R.id.progressBarPhoto)
        tvUserName = view.findViewById(R.id.profile_name)
        tvAge = view.findViewById(R.id.profile_age)
        tvSkinType = view.findViewById(R.id.tvSkinTypeValue)
        tvPreferences = view.findViewById(R.id.tvPreferencesValue)
        tvRoutines = view.findViewById(R.id.tvRoutinesValue)

        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)

        setupListeners()
        setupObservers()
        viewModel.loadProfile()

        cardMyShelf.setOnClickListener {
            try {
                view.findNavController().navigate(R.id.action_profileFragment_to_myShelfFragment)
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Nav Error: ${e.message}")
            }
        }
    }

    private fun setupListeners() {
        ivProfilePicture.setOnClickListener {
            showPhotoOptionsDialog()
        }

        btnEditProfile.setOnClickListener {
            val currentUser = viewModel.userProfile.value
            if (currentUser != null) {
                showEditProfileDialog(currentUser)
            } else {
                Toast.makeText(context, "Menunggu data profil...", Toast.LENGTH_SHORT).show()
                viewModel.loadProfile() // Coba load lagi jika null
            }
        }

        btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ -> viewModel.logout() }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Akun Permanen")
                .setMessage("Tindakan ini tidak bisa dibatalkan. Yakin?")
                .setPositiveButton("HAPUS") { _, _ -> viewModel.deleteAccount() }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun setupObservers() {
        // A. Data Profil User
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                // Update Teks
                tvUserName.text = profile.name.ifEmpty { "Pengguna" }
                tvAge.text = if (profile.age.isNotEmpty()) "${profile.age} Tahun" else "-"
                tvSkinType.text = profile.skinType.ifEmpty { "-" }
                tvPreferences.text = profile.preferences.ifEmpty { "-" }
                tvRoutines.text = profile.routines.ifEmpty { "-" }

                // Update Gambar dengan TRIK ANTI-CACHE
                if (profile.photoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(profile.photoUrl)
                        // KUNCI PERBAIKAN: Tambahkan signature waktu agar Glide selalu ambil gambar terbaru
                        .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Jangan simpan cache disk
                        .skipMemoryCache(true) // Jangan simpan cache memori
                        .placeholder(R.drawable.ic_profile_avatar)
                        .circleCrop()
                        .into(ivProfilePicture)
                } else {
                    // Jika URL kosong (dihapus), paksa ganti ke gambar default
                    Glide.with(this).clear(ivProfilePicture) // Bersihkan Glide dulu
                    ivProfilePicture.setImageResource(R.drawable.ic_profile_avatar)
                }
            }
        }

        // B. Status Upload (Loading Indicator)
        viewModel.isUploading.observe(viewLifecycleOwner) { isBusy ->
            if (isBusy) {
                progressBarPhoto.visibility = View.VISIBLE
                ivProfilePicture.alpha = 0.5f
                ivProfilePicture.isEnabled = false
            } else {
                progressBarPhoto.visibility = View.GONE
                ivProfilePicture.alpha = 1.0f
                ivProfilePicture.isEnabled = true
            }
        }

        // C. Pesan Toast
        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                if (!it.contains("Mengupload")) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearStatus()
            }
        }

        // D. Navigasi Logout
        viewModel.navigateToLogin.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, true)
                    .build()
                try {
                    requireActivity().findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.loginFragment, null, navOptions)
                } catch (e: Exception) { }
                viewModel.resetNavigate()
            }
        }
    }

    // --- LOGIC BARU UNTUK PILIHAN FOTO ---
    private fun showPhotoOptionsDialog() {
        val currentProfile = viewModel.userProfile.value
        val hasPhoto = !currentProfile?.photoUrl.isNullOrEmpty()

        // Jika sudah ada foto, tampilkan opsi "Hapus". Jika belum, hanya "Ganti".
        val options = if (hasPhoto) {
            arrayOf("Ganti Foto Profil", "Hapus Foto Profil")
        } else {
            arrayOf("Ganti Foto Profil")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Foto Profil")
            .setItems(options) { dialog, which ->
                if (hasPhoto) {
                    // Logic jika ada 2 menu
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*") // Ganti
                        1 -> confirmDeletePhoto() // Hapus
                    }
                } else {
                    // Logic jika cuma ada 1 menu (Ganti)
                    if (which == 0) pickImageLauncher.launch("image/*")
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun confirmDeletePhoto() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Foto?")
            .setMessage("Anda yakin ingin menghapus foto profil ini?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteProfilePicture()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditProfileDialog(user: UserProfile) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditName)
        val etAge = dialogView.findViewById<TextInputEditText>(R.id.etEditAge)
        val cgSkinType = dialogView.findViewById<ChipGroup>(R.id.cgSkinType)
        val cgPreferences = dialogView.findViewById<ChipGroup>(R.id.cgPreferences)
        val cgRoutines = dialogView.findViewById<ChipGroup>(R.id.cgRoutines)

        // Set Data Awal
        etName.setText(user.name)
        etAge.setText(user.age)

        // Helper untuk menyalakan Chip sesuai data database
        fun setChipsFromText(chipGroup: ChipGroup, dataString: String) {
            if (dataString.isEmpty()) return
            // Split string (misal: "Jerawat, Kusam") jadi list
            val items = dataString.split(",").map { it.trim() }

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                if (chip != null && items.contains(chip.text.toString())) {
                    chip.isChecked = true
                }
            }
        }

        setChipsFromText(cgSkinType, user.skinType)

        // --- BAGIAN INI SAYA AKTIFKAN KEMBALI ---
        setChipsFromText(cgPreferences, user.preferences)
        setChipsFromText(cgRoutines, user.routines)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = etName.text.toString().trim()
                val newAge = etAge.text.toString().trim()

                // Helper ambil data Chip yang dipilih
                fun getSelectedChipsText(chipGroup: ChipGroup): String {
                    val selectedValues = mutableListOf<String>()
                    val checkedIds = chipGroup.checkedChipIds
                    for (id in checkedIds) {
                        val chip = chipGroup.findViewById<Chip>(id)
                        selectedValues.add(chip.text.toString())
                    }
                    return selectedValues.joinToString(", ")
                }

                val newSkinType = getSelectedChipsText(cgSkinType)
                val newPreferences = getSelectedChipsText(cgPreferences)
                val newRoutines = getSelectedChipsText(cgRoutines)

                // Update Data Object
                val updatedUser = user.copy(
                    name = newName, // Jangan lupa update nama juga
                    age = newAge,
                    skinType = newSkinType,
                    preferences = newPreferences,
                    routines = newRoutines
                )

                viewModel.updateProfileData(updatedUser)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
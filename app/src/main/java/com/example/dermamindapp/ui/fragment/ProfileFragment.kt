package com.example.dermamindapp.ui.fragment

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.User
import com.example.dermamindapp.ui.viewmodel.ProfileViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var btnEditProfile: Button // Tombol Edit

    // Variabel untuk detail profil
    private lateinit var tvAge: TextView
    private lateinit var tvSkinType: TextView
    private lateinit var tvPreferences: TextView
    private lateinit var tvRoutines: TextView

    // Launcher untuk memilih gambar dari galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
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

        // 1. Inisialisasi View
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvUserName = view.findViewById(R.id.profile_name)
        tvAge = view.findViewById(R.id.profile_age)

        // Pastikan ID ini sesuai dengan yang ada di fragment_profile.xml
        tvSkinType = view.findViewById(R.id.tvSkinTypeValue)
        tvPreferences = view.findViewById(R.id.tvPreferencesValue)
        tvRoutines = view.findViewById(R.id.tvRoutinesValue)

        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        // 2. Setup Tombol Edit
        btnEditProfile.setOnClickListener {
            val currentUser = viewModel.userProfile.value
            if (currentUser != null) {
                showEditProfileDialog(currentUser)
            } else {
                Toast.makeText(context, "Data profil belum dimuat", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Setup Ganti Foto
        ivProfilePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        setupObservers()
        viewModel.loadProfile()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                tvUserName.text = profile.name.ifEmpty { "Pengguna Baru" }
                tvAge.text = if (profile.age.isNotEmpty()) "${profile.age} Tahun" else "Umur belum diatur"
                tvSkinType.text = profile.skinType.ifEmpty { "-" }
                tvPreferences.text = profile.preferences.ifEmpty { "-" }
                tvRoutines.text = profile.routines.ifEmpty { "-" }

                if (profile.photoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(profile.photoUrl)
                        .placeholder(R.drawable.ic_profile_avatar)
                        .circleCrop()
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
    }

    // =========================================================================
    // LOGIKA DIALOG EDIT PROFILE
    // =========================================================================
    private fun showEditProfileDialog(user: User) {
        // Inflate layout dialog_edit_profile.xml
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)

        // Init Views di dalam dialog
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditName)
        val etAge = dialogView.findViewById<TextInputEditText>(R.id.etEditAge)
        val cgSkinType = dialogView.findViewById<ChipGroup>(R.id.cgSkinType)
        val cgPreferences = dialogView.findViewById<ChipGroup>(R.id.cgPreferences)
        val cgRoutines = dialogView.findViewById<ChipGroup>(R.id.cgRoutines)

        // ---------------------------------------------------------------------
        // 1. SET DATA LAMA KE INPUT (Pre-fill)
        // ---------------------------------------------------------------------
        etName.setText(user.name)
        etAge.setText(user.age)

        // Helper Function: Untuk mencentang chip secara otomatis berdasarkan string database
        // Contoh: dataString = "Vegan, Alcohol-Free" -> Chip "Vegan" & "Alcohol-Free" dicentang
        fun setChipsFromText(chipGroup: ChipGroup, dataString: String) {
            if (dataString.isEmpty()) return
            // Pisahkan string dengan koma, lalu hapus spasi berlebih
            val items = dataString.split(",").map { it.trim() }

            for (i in 0 until chipGroup.childCount) {
                val view = chipGroup.getChildAt(i)
                if (view is Chip) {
                    // Jika teks chip ada di daftar items, maka centang
                    if (items.contains(view.text.toString())) {
                        view.isChecked = true
                    }
                }
            }
        }

        // Terapkan ke masing-masing kategori
        setChipsFromText(cgSkinType, user.skinType)
        setChipsFromText(cgPreferences, user.preferences)
        setChipsFromText(cgRoutines, user.routines)

        // ---------------------------------------------------------------------
        // 2. TAMPILKAN DIALOG & SIMPAN DATA
        // ---------------------------------------------------------------------
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // User wajib klik Simpan atau Batal
            .setPositiveButton("Simpan") { _, _ ->
                // Ambil data text
                val newName = etName.text.toString().trim()
                val newAge = etAge.text.toString().trim()

                // Helper Function: Ambil teks dari chip yang DIPILIH (Checked)
                fun getSelectedChipsText(chipGroup: ChipGroup): String {
                    val selectedValues = mutableListOf<String>()
                    val checkedIds = chipGroup.checkedChipIds
                    for (id in checkedIds) {
                        val chip = chipGroup.findViewById<Chip>(id)
                        selectedValues.add(chip.text.toString())
                    }
                    // Gabungkan jadi string, misal: "Pagi, Malam"
                    return selectedValues.joinToString(", ")
                }

                // Ambil data dari ChipGroups
                val newSkinType = getSelectedChipsText(cgSkinType)
                val newPreferences = getSelectedChipsText(cgPreferences)
                val newRoutines = getSelectedChipsText(cgRoutines) // Sekarang support banyak pilihan

                // Buat object User baru
                val updatedUser = user.copy(
                    name = newName,
                    age = newAge,
                    skinType = newSkinType,
                    preferences = newPreferences,
                    routines = newRoutines
                )

                // Kirim ke ViewModel untuk disimpan ke Firebase
                viewModel.updateProfileData(updatedUser)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
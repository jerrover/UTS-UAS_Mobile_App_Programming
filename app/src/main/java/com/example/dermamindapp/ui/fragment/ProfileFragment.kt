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
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
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
    private lateinit var tvAge: TextView
    private lateinit var tvSkinType: TextView
    private lateinit var tvPreferences: TextView
    private lateinit var tvRoutines: TextView
    private lateinit var btnEditProfile: Button

    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Placeholder upload
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

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvUserName = view.findViewById(R.id.profile_name)
        tvAge = view.findViewById(R.id.profile_age)
        tvSkinType = view.findViewById(R.id.tvSkinTypeValue)
        tvPreferences = view.findViewById(R.id.tvPreferencesValue)
        tvRoutines = view.findViewById(R.id.tvRoutinesValue)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)

        btnEditProfile.setOnClickListener {
            val currentUser = viewModel.userProfile.value
            if (currentUser != null) {
                showEditProfileDialog(currentUser)
            } else {
                Toast.makeText(context, "Data profil belum dimuat", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ -> viewModel.logout() }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Akun Permanen")
                .setMessage("Tindakan ini tidak bisa dibatalkan. Yakin?")
                .setPositiveButton("HAPUS") { _, _ -> viewModel.deleteAccount() }
                .setNegativeButton("Batal", null)
                .show()
        }

        ivProfilePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        setupObservers()
        viewModel.loadProfile()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                tvUserName.text = profile.name.ifEmpty { "Pengguna" }
                tvAge.text = if (profile.age.isNotEmpty()) "${profile.age} Tahun" else "-"
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

        // === PERBAIKAN DI SINI (LOGOUT ERROR) ===
        viewModel.navigateToLogin.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                // Kita hapus histori sampai 'mainFragment' saja, BUKAN 'app_nav'
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, true) // Hapus Home dari stack
                    .build()

                try {
                    // Gunakan Root NavController dari Activity agar bisa pindah antar modul (Main -> Login)
                    requireActivity().findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.loginFragment, null, navOptions)
                } catch (e: Exception) {
                    Toast.makeText(context, "Navigasi error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                viewModel.resetNavigate()
            }
        }
    }

    private fun showEditProfileDialog(user: User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditName)
        val etAge = dialogView.findViewById<TextInputEditText>(R.id.etEditAge)
        val cgSkinType = dialogView.findViewById<ChipGroup>(R.id.cgSkinType)
        val cgPreferences = dialogView.findViewById<ChipGroup>(R.id.cgPreferences)
        val cgRoutines = dialogView.findViewById<ChipGroup>(R.id.cgRoutines)

        etName.setText(user.name)
        etAge.setText(user.age)

        fun setChipsFromText(chipGroup: ChipGroup, dataString: String) {
            if (dataString.isEmpty()) return
            val items = dataString.split(",").map { it.trim() }
            for (i in 0 until chipGroup.childCount) {
                val view = chipGroup.getChildAt(i)
                if (view is Chip && items.contains(view.text.toString())) {
                    view.isChecked = true
                }
            }
        }

        setChipsFromText(cgSkinType, user.skinType)
        setChipsFromText(cgPreferences, user.preferences)
        setChipsFromText(cgRoutines, user.routines)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Simpan") { _, _ ->
                val newAge = etAge.text.toString().trim()

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

                val updatedUser = user.copy(
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
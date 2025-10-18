package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.dermamindapp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// Fragment ini mengelola alur onboarding (pengenalan aplikasi) menggunakan ViewPager2.
class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        // Inisialisasi komponen UI.
        viewPager = view.findViewById(R.id.viewPager)
        nextButton = view.findViewById(R.id.nextButton)
        tabLayout = view.findViewById(R.id.tabLayout)

        // Mengatur adapter untuk ViewPager2.
        val pagerAdapter = OnboardingPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Menghubungkan TabLayout dengan ViewPager2 untuk menampilkan indikator halaman.
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Mengubah teks tombol "Next" menjadi "Get Started" di halaman terakhir.
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == pagerAdapter.itemCount - 1) {
                    nextButton.setText(R.string.get_started)
                } else {
                    nextButton.setText(R.string.next)
                }
            }
        })

        // Menangani aksi klik tombol "Next" atau "Get Started".
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                // Jika bukan halaman terakhir, pindah ke halaman berikutnya.
                viewPager.currentItem = currentItem + 1
            } else {
                // Jika di halaman terakhir, navigasi ke halaman setup profil.
                findNavController().navigate(R.id.action_onboardingFragment_to_profileSetupFragment)
            }
        }

        return view
    }
}

// Adapter untuk ViewPager2 yang mengelola fragmen-fragmen halaman onboarding.
class OnboardingPagerAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {
    // Daftar halaman onboarding dengan judul dan subjudul.
    private val pages = listOf(
        Pair(R.string.onboarding_title_1, R.string.onboarding_subtitle_1),
        Pair(R.string.onboarding_title_2, R.string.onboarding_subtitle_2)
    )

    override fun getItemCount(): Int = pages.size

    // Membuat instance OnboardingPageFragment untuk setiap halaman.
    override fun createFragment(position: Int): Fragment {
        val (titleRes, subtitleRes) = pages[position]
        return OnboardingPageFragment.newInstance(getString(titleRes), getString(subtitleRes))
    }

    // Helper untuk mendapatkan string dari resources.
    private fun getString(resId: Int): String {
        return fragment.getString(resId)
    }
}

// Fragment untuk menampilkan konten satu halaman onboarding.
class OnboardingPageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_page, container, false)
        // Mengisi judul dan subjudul dari argumen yang diterima.
        view.findViewById<TextView>(R.id.titleTextView).text = arguments?.getString(ARG_TITLE)
        view.findViewById<TextView>(R.id.subtitleTextView).text = arguments?.getString(ARG_SUBTITLE)
        return view
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"

        // Membuat instance baru dari OnboardingPageFragment dengan data yang diperlukan.
        fun newInstance(title: String, subtitle: String): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
            }
            return fragment
        }
    }
}
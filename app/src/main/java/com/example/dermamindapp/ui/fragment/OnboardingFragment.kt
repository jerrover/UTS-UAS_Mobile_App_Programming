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

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        nextButton = view.findViewById(R.id.nextButton)
        tabLayout = view.findViewById(R.id.tabLayout)

        val pagerAdapter = OnboardingPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == pagerAdapter.itemCount - 1) {
                    nextButton.setText(R.string.get_started) // Pastikan string ini ada di strings.xml
                } else {
                    nextButton.text = "Lanjut" // Hardcode sementara biar aman
                }
            }
        })

        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                viewPager.currentItem = currentItem + 1
            } else {
                // [PERBAIKAN DISINI]
                // Ubah tujuan navigasi ke LoginFragment
                findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
            }
        }

        return view
    }
}

class OnboardingPagerAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {
    // Pastikan ID string ini ada di res/values/strings.xml
    // Jika error, bisa ganti pakai string biasa dulu
    private val pages = listOf(
        Pair("Analisis Kulit AI", "Deteksi masalah kulitmu secara instan dengan teknologi AI canggih."),
        Pair("Rekomendasi Pintar", "Dapatkan saran produk dan perawatan yang paling cocok untukmu.")
    )

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        val (title, subtitle) = pages[position]
        return OnboardingPageFragment.newInstance(title, subtitle)
    }
}

class OnboardingPageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_page, container, false)
        view.findViewById<TextView>(R.id.titleTextView).text = arguments?.getString(ARG_TITLE)
        view.findViewById<TextView>(R.id.subtitleTextView).text = arguments?.getString(ARG_SUBTITLE)
        return view
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"

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
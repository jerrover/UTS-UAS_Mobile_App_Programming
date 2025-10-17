package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Product

class ProductRecommendationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_recommendation, container, false)

        // Data Produk
        val niacinamideSerum = Product(
            id = 1,
            name = "Niacinamide Serum",
            suitability = "Cocok untuk: Bekas Jerawat",
            description = "Niacinamide, atau Vitamin B3, adalah bahan aktif serbaguna yang efektif mengatasi berbagai masalah kulit. Serum ini diformulasikan untuk menyamarkan bekas jerawat, mencerahkan kulit kusam, dan mengontrol produksi minyak berlebih. Dengan penggunaan rutin, kulit akan tampak lebih bersih, cerah, dan sehat.",
            imageUrl = "https://nihonmart.id/pub/media/catalog/product/cache/11ac1d1f1c987b7e84b3f488d403431e/n/i/niacinamide_serum_2-min.png",
            tokopediaUrl = "https://www.tokopedia.com/search?q=azarine%20niacinamide%205%25",
            shopeeUrl = "https://shopee.co.id/search?keyword=azarine%20niacinamide%205%25"
        )

        val moisturizer = Product(
            id = 2,
            name = "5x Ceramide Moisturizer",
            suitability = "Cocok untuk: Garis Halus Awal",
            description = "Pelembap dengan kandungan 5 jenis Ceramide yang dapat membantu memperbaiki skin barrier dan menjaga kelembapan kulit. Diperkaya dengan Hyaluronic Acid, Centella, dan Marine Collagen, pelembap ini mampu menghidrasi kulit secara mendalam, mengurangi tanda-tanda penuaan dini seperti garis halus, dan menjadikan kulit lebih halus serta kenyal.",
            imageUrl = "https://medias.watsons.co.id/publishing/WTCID-28566-front-zoom.jpg?version=1734743310",
            tokopediaUrl = "https://www.tokopedia.com/search?q=skintific%205x%20ceramide%20moisture%20gel",
            shopeeUrl = "https://shopee.co.id/search?keyword=skintific%205x%20ceramide%20moisture%20gel"
        )

        // Inisialisasi ImageView
        val productImage1: ImageView = view.findViewById(R.id.productImage1)
        val productImage2: ImageView = view.findViewById(R.id.productImage2)

        // Memuat gambar dari URL menggunakan Glide
        Glide.with(this)
            .load(niacinamideSerum.imageUrl)
            .into(productImage1)

        Glide.with(this)
            .load(moisturizer.imageUrl)
            .into(productImage2)


        // Set OnClickListener untuk tombol "View Details"
        val viewDetailsButton1: Button = view.findViewById(R.id.viewDetailsButton1)
        viewDetailsButton1.setOnClickListener {
            val action = ProductRecommendationFragmentDirections.actionProductRecommendationFragmentToProductDetailsFragment(niacinamideSerum)
            findNavController().navigate(action)
        }

        val viewDetailsButton2: Button = view.findViewById(R.id.viewDetailsButton2)
        viewDetailsButton2.setOnClickListener {
            val action = ProductRecommendationFragmentDirections.actionProductRecommendationFragmentToProductDetailsFragment(moisturizer)
            findNavController().navigate(action)
        }

        return view
    }
}

// Model data (POKO) untuk entri riwayat analisis. Parcelable agar bisa dikirim antar fragment.
package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkinAnalysis(
    val id: Long,
    val date: Long,
    val imageUri: String,
    val result: String,
    var notes: String?
) : Parcelable
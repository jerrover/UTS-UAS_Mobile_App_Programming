package com.example.dermamindapp.ml

// Daftar label, HARUS urutan yang sama persis dengan di Python
val ALL_CLASSES = listOf(
    "Jerawat_Aktif",
    "Kulit_Berminyak",
    "Kemerahan",
    "Tekstur_Pori_pori",
    "Kulit_Sehat"
)

/**
 * Data class untuk menampung hasil analisis.
 * @param probabilities Peta mentah dari Nama Kelas -> Skor Probabilitas (0.0 - 1.0)
 * @param diagnosis Peta final dari Nama Kelas -> Boolean (true/false setelah threshold)
 */
data class SkinResult(
    val probabilities: Map<String, Float>,
    val diagnosis: Map<String, Boolean>
)
package com.example.wastrain

import android.content.Context

data class MotifInfo(
    val id: String,
    val name: String,
    val origin: String,
    val category: String,
    val imageResourceName: String,
    val visualCharacteristics: String,
    val philosophy: String
)

object MotifDataRepository {

    val motifs: List<MotifInfo> = listOf(
        MotifInfo(
            id = "batik-bali",
            name = "Batik Bali",
            origin = "Denpasar, Bali",
            category = "Pesisir",
            imageResourceName = "katalog_batik_bali",
            visualCharacteristics = "Menggabungkan ornamen khas Bali seperti Bunga Frangipani (Kamboja), Burung Cendrawasih, Pura, dan Barong dengan warna-warna hangat dan berani.",
            philosophy = "Mencerminkan keharmonisan alam, budaya Bali yang kental, serta nilai spiritualisme dan rasa syukur masyarakat Bali kepada Sang Pencipta."
        ),
        MotifInfo(
            id = "batik-betawi",
            name = "Batik Betawi",
            origin = "Jakarta",
            category = "Pesisir",
            imageResourceName = "katalog_batik_betawi",
            visualCharacteristics = "Memiliki ragam hias ikon khas Jakarta seperti Ondel-ondel, Monas, Burung Hong, dan Sungai Ciliwung dengan warna cerah mencolok.",
            philosophy = "Melambangkan kehangatan, kegembiraan, keterbukaan masyarakat Betawi, serta semangat menjaga warisan budaya ibu kota."
        ),
        MotifInfo(
            id = "batik-celup",
            name = "Batik Celup (Shibori / Tritik)",
            origin = "Nusantara",
            category = "Kontemporer",
            imageResourceName = "katalog_batik_celup",
            visualCharacteristics = "Dibuat dengan teknik ikat celup (jumputan), menghasilkan pola gradasi lingkaran, gelombang, atau abstrak berpola simetris yang lembut.",
            philosophy = "Menggambarkan fleksibilitas, kreativitas tanpa batas, serta keindahan alam dalam kesederhanaan proses pembuatan tradisional."
        ),
        MotifInfo(
            id = "batik-cendrawasih",
            name = "Batik Cendrawasih",
            origin = "Papua",
            category = "Flora/Fauna",
            imageResourceName = "katalog_batik_cendrawasih",
            visualCharacteristics = "Didominasi siluet gagah Burung Cendrawasih dengan bulu ekor indah dipadu ukiran khas suku Asmat dan warna alam yang tegas.",
            philosophy = "Simbol keindahan langka, kebanggaan tanah Papua, kemuliaan, serta keanggunan burung surga yang menjadi kebanggaan Indonesia."
        ),
        MotifInfo(
            id = "batik-ceplok",
            name = "Batik Ceplok",
            origin = "Yogyakarta / Surakarta",
            category = "Geometris",
            imageResourceName = "katalog_batik_ceplok",
            visualCharacteristics = "Pola geometris berulang berupa bujur sangkar, lingkaran, bintang, atau segi empat simetris menyerupai kelopak bunga teratur.",
            philosophy = "Melambangkan keteraturan hidup, ketertiban dalam bermasyarakat, serta keselarasan antara alam semesta dan kehidupan manusia."
        ),
        MotifInfo(
            id = "batik-ciamis",
            name = "Batik Ciamis (Ciamisan)",
            origin = "Ciamis, Jawa Barat",
            category = "Priangan",
            imageResourceName = "katalog_batik_ciamis",
            visualCharacteristics = "Memiliki ragam hias naturalis sederhana dengan ruang kosong (tanah) yang luas, menggunakan warna pastel dan cokelat kekuningan.",
            philosophy = "Mencerminkan karakter masyarakat Ciamis yang tenang, humble, bersahaja, serta memiliki kedekatan erat dengan alam sekitar."
        ),
        MotifInfo(
            id = "batik-garutan",
            name = "Batik Garutan",
            origin = "Garut, Jawa Barat",
            category = "Priangan",
            imageResourceName = "katalog_batik_garutan",
            visualCharacteristics = "Bentuk geometris gumerang (lereng) atau flora-fauna dengan warna khas gumading (kuning gading), cokelat, dan krem lembut.",
            philosophy = "Melambangkan ketenangan jiwa, kemurnian niat, kesederhanaan, serta keharmonisan dalam kehidupan bermasyarakat."
        ),
        MotifInfo(
            id = "batik-gentongan",
            name = "Batik Gentongan",
            origin = "Bangkalan, Madura",
            category = "Pesisir",
            imageResourceName = "katalog_batik_gentongan",
            visualCharacteristics = "Warna sangat pekat dan tahan lama hasil perendaman berbulan-bulan di gentong batu, dengan motif flora, fauna, dan sisik ikan yang rumit.",
            philosophy = "Simbol ketekunan luar biasa, kesabaran, kerja keras, dan daya tahan tinggi masyarakat pesisir Madura."
        ),
        MotifInfo(
            id = "batik-kawung",
            name = "Batik Kawung",
            origin = "Yogyakarta / Surakarta",
            category = "Geometris / Keraton",
            imageResourceName = "katalog_batik_kawung",
            visualCharacteristics = "Pola empat elips menyerupai buah kawung (kolang-kaling) yang disusun melingkar mengelilingi satu titik pusat.",
            philosophy = "Melambangkan kesucian hati, keadilan, ketulusan, serta harapan agar pemakainya menjadi manusia yang berguna bagi sesama."
        ),
        MotifInfo(
            id = "batik-keraton",
            name = "Batik Keraton",
            origin = "Surakarta / Yogyakarta",
            category = "Keraton",
            imageResourceName = "katalog_batik_keraton",
            visualCharacteristics = "Ragam hias sarat makna keraton seperti Lar (sayap), Gurda, Parang, dan Sawat dengan warna klasik soga (brown), hitam, dan putih gading.",
            philosophy = "Mencerminkan kearifan lokal, wibawa kepemimpinan, keluhuran budi pekerti, serta tatanan etika kehidupan bangsawan."
        ),
        MotifInfo(
            id = "batik-lasem",
            name = "Batik Lasem",
            origin = "Rembang, Jawa Tengah",
            category = "Akulturasi",
            imageResourceName = "katalog_batik_lasem",
            visualCharacteristics = "Perpaduan akulturasi Tionghoa dan Jawa (Tiga Negeri) dengan ikon Naga, Burung Hong, Bunga Seruni, dipadu warna merah cabe khas Lasem.",
            philosophy = "Simbol persatuan dalam keberagaman, harmoni antar-etnis, keberuntungan, kebahagiaan, serta kedamaian hidup bersama."
        ),
        MotifInfo(
            id = "batik-megamendung",
            name = "Batik Megamendung",
            origin = "Cirebon, Jawa Barat",
            category = "Pesisir",
            imageResourceName = "katalog_batik_megamendung",
            visualCharacteristics = "Bentuk awan bergulung dengan sembilan gradasi warna dari biru muda hingga biru tua berlatar merah atau putih.",
            philosophy = "Melambangkan kesabaran, penyejuk jiwa di saat marah, kepemimpinan yang mengayomi, serta harapan akan hujan pembawa kesuburan."
        ),
        MotifInfo(
            id = "batik-parang",
            name = "Batik Parang",
            origin = "Yogyakarta / Surakarta",
            category = "Geometris / Keraton",
            imageResourceName = "katalog_batik_parang",
            visualCharacteristics = "Garis diagonal miring menyerupai susunan huruf 'S' atau ombak laut yang saling bersambungan tanpa putus.",
            philosophy = "Melambangkan perjuangan tiada henti, ketahanan, semangat membara, serta ikatan keluarga yang tak pernah terputus."
        ),
        MotifInfo(
            id = "batik-pekalongan",
            name = "Batik Pekalongan",
            origin = "Pekalongan, Jawa Tengah",
            category = "Pesisir",
            imageResourceName = "katalog_batik_pekalongan",
            visualCharacteristics = "Sangat kaya warna (polikromatik) dengan rincian flora, buketan bunga, dan kupu-kupu yang sangat dinamis dan realistis.",
            philosophy = "Melambangkan keterbukaan, dinamisme, keindahan estetika budaya pesisir, serta kemampuan beradaptasi dengan zaman."
        ),
        MotifInfo(
            id = "batik-priangan",
            name = "Batik Priangan",
            origin = "Tasikmalaya & Ciamis",
            category = "Priangan",
            imageResourceName = "katalog_batik_priangan",
            visualCharacteristics = "Garis motif halus, anggun, dengan gambar flora khas Tatar Sunda seperti daun talas, bunga anggrek, dan burung.",
            philosophy = "Menggambarkan keanggunan, keramahan, keindahan alam Parahyangan, serta kedamaian hidup dalam harmoni."
        ),
        MotifInfo(
            id = "batik-sekar",
            name = "Batik Sekar Jagad",
            origin = "Yogyakarta / Surakarta",
            category = "Flora",
            imageResourceName = "katalog_batik_sekar",
            visualCharacteristics = "Kumpulan pulau-pulau kecilan berdinding garis lengkung yang berisi aneka ragam motif bunga dan ornamen cantik.",
            philosophy = "Sekar berarti bunga, Jagad berarti dunia. Melambangkan keindahan keberagaman budaya Nusantara dan pesona keindahan dunia."
        ),
        MotifInfo(
            id = "batik-sidoluhur",
            name = "Batik Sidoluhur",
            origin = "Surakarta / Yogyakarta",
            category = "Keraton",
            imageResourceName = "katalog_batik_sidoluhur",
            visualCharacteristics = "Pola geometris berisikan bangunan keraton, garuda, atau pohon hayat dengan warna soga klasik yang tinggi estetika.",
            philosophy = "Sido berarti jadi/menjadi, Luhur berarti terhormat. Berisi doa agar pemakainya mencapai kedudukan luhur dan berbudi pekerti tinggi."
        ),
        MotifInfo(
            id = "batik-sidomukti",
            name = "Batik Sidomukti",
            origin = "Solo / Yogyakarta",
            category = "Keraton",
            imageResourceName = "katalog_batik_sidomukti",
            visualCharacteristics = "Ragam hias Gurda dan Ornamen Bunga berlatar belakang ukiran bidang geometris dengan pewarnaan soga alam berkelas.",
            philosophy = "Sidomukti melambangkan doa dan harapan agar mendapatkan kemakmuran, kebahagiaan, serta kecukupan lahir dan batin."
        ),
        MotifInfo(
            id = "batik-sogan",
            name = "Batik Sogan",
            origin = "Solo / Yogyakarta",
            category = "Klasik",
            imageResourceName = "katalog_batik_sogan",
            visualCharacteristics = "Dominasi warna cokelat muda hingga cokelat tua khas pewarna alami kulit pohon soga jawa dengan isen-isen halus.",
            philosophy = "Menyimbolkan kerendahan hati, kehangatan bumi, kejujuran, serta kearifan tradisi masyarakat Jawa klasik."
        ),
        MotifInfo(
            id = "batik-tambal",
            name = "Batik Tambal",
            origin = "Yogyakarta",
            category = "Klasik",
            imageResourceName = "katalog_batik_tambal",
            visualCharacteristics = "Terdiri dari gabungan berbagai potongan bidang motif batik berbeda yang ditambal menjadi satu kesatuan harmoni.",
            philosophy = "Melambangkan harapan untuk menambal atau memperbaiki hal-hal yang kurang baik menuju kebaikan, kesehatan, dan kesempurnaan."
        )
    )

    fun getById(id: String): MotifInfo? {
        return motifs.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    fun getByName(name: String): MotifInfo? {
        val normalized = name.lowercase().trim().replace(" ", "-")
        return motifs.firstOrNull {
            it.id.equals(normalized, ignoreCase = true) ||
            it.name.equals(name, ignoreCase = true) ||
            it.id.contains(normalized) ||
            normalized.contains(it.id)
        }
    }

    fun getDrawableResId(context: Context, motifIdOrName: String): Int {
        val info = getById(motifIdOrName) ?: getByName(motifIdOrName)
        val resName = info?.imageResourceName ?: ("katalog_" + motifIdOrName.lowercase().trim().replace(" ", "_").replace("-", "_"))
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.wastrain_logo
    }

    fun search(query: String, categoryFilter: String = "Semua"): List<MotifInfo> {
        return motifs.filter { motif ->
            val matchesCategory = (categoryFilter == "Semua" || motif.category.equals(categoryFilter, ignoreCase = true))
            val matchesQuery = query.isEmpty() ||
                    motif.name.contains(query, ignoreCase = true) ||
                    motif.origin.contains(query, ignoreCase = true) ||
                    motif.visualCharacteristics.contains(query, ignoreCase = true) ||
                    motif.philosophy.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    fun getCategories(): List<String> {
        return listOf("Semua") + motifs.map { it.category }.distinct().sorted()
    }
}

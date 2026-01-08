package com.example.sawit.IsiDashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.example.sawit.model.KebunData
import com.example.sawit.model.PanenData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DetailLaporanFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var imgKebun: ImageView
    private lateinit var tvNamaKebun: TextView
    private lateinit var tvLokasiKebun: TextView
    private lateinit var tvLuas: TextView
    private lateinit var tvJenisTanah: TextView
    private lateinit var tvPokok: TextView
    private lateinit var tvTahun: TextView

    // Header title yang akan berubah
    private lateinit var tvHeaderTitle: TextView

    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvTotalBeratTBS: TextView
    private lateinit var tvTotalBeratMatang: TextView
    private lateinit var tvTotalBeratTidakMatang: TextView
    private lateinit var tvTotalBeratKelewatMatang: TextView
    private lateinit var tvTotalJumlahMatang: TextView
    private lateinit var tvTotalJumlahTidakMatang: TextView
    private lateinit var tvTotalJumlahKelewatMatang: TextView

    private lateinit var lineChart: LineChart

    // Filter buttons
    private lateinit var cardFilterHari: CardView
    private lateinit var cardFilterBulan: CardView
    private lateinit var cardFilterTahun: CardView
    private lateinit var tvFilterHari: TextView
    private lateinit var tvFilterBulan: TextView
    private lateinit var tvFilterTahun: TextView

    private var kebunData: KebunData? = null
    private var panenList: List<PanenData> = emptyList()

    // Filter state
    private enum class FilterType {
        HARI, BULAN, TAHUN
    }
    private var currentFilter: FilterType = FilterType.HARI

    companion object {
        private const val TAG = "DetailLaporanFragment"
        private const val ARG_KEBUN_DATA = "kebun_data"
        private const val ARG_PANEN_LIST = "panen_list"

        fun newInstance(kebunData: KebunData, panenList: List<PanenData>): DetailLaporanFragment {
            val fragment = DetailLaporanFragment()
            val args = Bundle()
            args.putParcelable(ARG_KEBUN_DATA, kebunData)
            args.putParcelableArrayList(ARG_PANEN_LIST, ArrayList(panenList))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kebunData = it.getParcelable(ARG_KEBUN_DATA)
            panenList = it.getParcelableArrayList<PanenData>(ARG_PANEN_LIST) ?: emptyList()
        }
        Log.d(TAG, "Fragment created with ${panenList.size} panen data")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_detail_laporan, container, false)

        initViews(rootView)
        setupListeners()
        displayData()
        updateHeaderTitle() // Update header saat pertama kali dibuat
        setupChart()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle) // Tambahkan ID ini di XML
        imgKebun = view.findViewById(R.id.imgKebun)
        tvNamaKebun = view.findViewById(R.id.tvNamaKebun)
        tvLokasiKebun = view.findViewById(R.id.tvLokasiKebun)
        tvLuas = view.findViewById(R.id.tvLuas)
        tvJenisTanah = view.findViewById(R.id.tvJenisTanah)
        tvPokok = view.findViewById(R.id.tvPokok)
        tvTahun = view.findViewById(R.id.tvTahun)

        tvTotalPendapatan = view.findViewById(R.id.tvTotalPendapatan)
        tvTotalBeratTBS = view.findViewById(R.id.tvTotalBeratTBS)
        tvTotalBeratMatang = view.findViewById(R.id.tvTotalBeratMatang)
        tvTotalBeratTidakMatang = view.findViewById(R.id.tvTotalBeratTidakMatang)
        tvTotalBeratKelewatMatang = view.findViewById(R.id.tvTotalBeratKelewatMatang)
        tvTotalJumlahMatang = view.findViewById(R.id.tvTotalJumlahMatang)
        tvTotalJumlahTidakMatang = view.findViewById(R.id.tvTotalJumlahTidakMatang)
        tvTotalJumlahKelewatMatang = view.findViewById(R.id.tvTotalJumlahKelewatMatang)

        lineChart = view.findViewById(R.id.lineChart)

        // Filter buttons
        cardFilterHari = view.findViewById(R.id.cardFilterHari)
        cardFilterBulan = view.findViewById(R.id.cardFilterBulan)
        cardFilterTahun = view.findViewById(R.id.cardFilterTahun)
        tvFilterHari = view.findViewById(R.id.tvFilterHari)
        tvFilterBulan = view.findViewById(R.id.tvFilterBulan)
        tvFilterTahun = view.findViewById(R.id.tvFilterTahun)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // Filter button listeners
        cardFilterHari.setOnClickListener {
            currentFilter = FilterType.HARI
            updateFilterUI()
            updateHeaderTitle() // Update header ketika filter berubah
            setupChart()
        }

        cardFilterBulan.setOnClickListener {
            currentFilter = FilterType.BULAN
            updateFilterUI()
            updateHeaderTitle() // Update header ketika filter berubah
            setupChart()
        }

        cardFilterTahun.setOnClickListener {
            currentFilter = FilterType.TAHUN
            updateFilterUI()
            updateHeaderTitle() // Update header ketika filter berubah
            setupChart()
        }
    }

    /**
     * Update header title berdasarkan filter dan range data
     */
    private fun updateHeaderTitle() {
        if (panenList.isEmpty()) {
            tvHeaderTitle.text = "Detail Laporan"
            return
        }

        val dateFormats = listOf(
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        )

        // Ambil tanggal pertama dan terakhir
        val sortedPanen = panenList.sortedBy { it.timestamp }
        val firstPanen = sortedPanen.first()
        val lastPanen = sortedPanen.last()

        var firstDate: java.util.Date? = null
        var lastDate: java.util.Date? = null

        // Parse tanggal pertama
        for (format in dateFormats) {
            try {
                firstDate = format.parse(firstPanen.tanggalPanen.trim())
                if (firstDate != null) break
            } catch (e: Exception) {
                // Continue
            }
        }
        if (firstDate == null) {
            firstDate = java.util.Date(firstPanen.timestamp)
        }

        // Parse tanggal terakhir
        for (format in dateFormats) {
            try {
                lastDate = format.parse(lastPanen.tanggalPanen.trim())
                if (lastDate != null) break
            } catch (e: Exception) {
                // Continue
            }
        }
        if (lastDate == null) {
            lastDate = java.util.Date(lastPanen.timestamp)
        }

        // Format title berdasarkan filter
        val title = when (currentFilter) {
            FilterType.HARI -> {
                val dayFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                if (firstDate.time == lastDate.time) {
                    "Detail : ${dayFormat.format(firstDate)}"
                } else {
                    "Detail : ${dayFormat.format(firstDate)} - ${dayFormat.format(lastDate)}"
                }
            }
            FilterType.BULAN -> {
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                val firstMonth = monthFormat.format(firstDate)
                val lastMonth = monthFormat.format(lastDate)
                if (firstMonth == lastMonth) {
                    "Detail : $firstMonth"
                } else {
                    val firstMonthOnly = SimpleDateFormat("MMMM", Locale("id", "ID")).format(firstDate)
                    val lastMonthFull = monthFormat.format(lastDate)
                    "Detail : $firstMonthOnly - $lastMonthFull"
                }
            }
            FilterType.TAHUN -> {
                val yearFormat = SimpleDateFormat("yyyy", Locale("id", "ID"))
                val firstYear = yearFormat.format(firstDate)
                val lastYear = yearFormat.format(lastDate)
                if (firstYear == lastYear) {
                    "Detail : Tahun $firstYear"
                } else {
                    "Detail : $firstYear - $lastYear"
                }
            }
        }

        tvHeaderTitle.text = title
        Log.d(TAG, "Header updated: $title")
    }

    private fun updateFilterUI() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.green_primary)
        val inactiveColor = Color.parseColor("#E0E0E0")
        val activeTextColor = Color.WHITE
        val inactiveTextColor = Color.parseColor("#666666")

        // Reset all
        cardFilterHari.setCardBackgroundColor(inactiveColor)
        cardFilterBulan.setCardBackgroundColor(inactiveColor)
        cardFilterTahun.setCardBackgroundColor(inactiveColor)
        tvFilterHari.setTextColor(inactiveTextColor)
        tvFilterBulan.setTextColor(inactiveTextColor)
        tvFilterTahun.setTextColor(inactiveTextColor)

        // Set active
        when (currentFilter) {
            FilterType.HARI -> {
                cardFilterHari.setCardBackgroundColor(activeColor)
                tvFilterHari.setTextColor(activeTextColor)
            }
            FilterType.BULAN -> {
                cardFilterBulan.setCardBackgroundColor(activeColor)
                tvFilterBulan.setTextColor(activeTextColor)
            }
            FilterType.TAHUN -> {
                cardFilterTahun.setCardBackgroundColor(activeColor)
                tvFilterTahun.setTextColor(activeTextColor)
            }
        }
    }

    private fun displayData() {
        // Display Kebun Data
        kebunData?.let { kebun ->
            tvNamaKebun.text = kebun.namaKebun
            tvLokasiKebun.text = kebun.lokasiKebun
            tvLuas.text = "${String.format("%.0f", kebun.luasLahan)} ha"
            tvJenisTanah.text = kebun.jenisTanah
            tvPokok.text = "${kebun.jumlahTanaman} Pokok"
            tvTahun.text = "${kebun.getUsiaTahun()} Tahun"
        }

        // Calculate totals from all panen data
        var totalPendapatan = 0.0
        var totalBeratTBS = 0.0
        var totalBeratMatang = 0.0
        var totalBeratTidakMatang = 0.0
        var totalBeratKelewatMatang = 0.0
        var totalJumlahMatang = 0
        var totalJumlahTidakMatang = 0
        var totalJumlahKelewatMatang = 0

        panenList.forEach { panen ->
            totalPendapatan += panen.getTotalPendapatan()
            totalBeratTBS += panen.getTotalBerat()
            totalBeratMatang += panen.tbsMatang
            totalBeratTidakMatang += panen.tbsTidakMatang
            totalBeratKelewatMatang += panen.tbsKelewatMatang
            totalJumlahMatang += panen.jumlahMatang
            totalJumlahTidakMatang += panen.jumlahTidakMatang
            totalJumlahKelewatMatang += panen.jumlahKelewatMatang
        }

        // Display totals
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvTotalPendapatan.text = currencyFormat.format(totalPendapatan)
        tvTotalBeratTBS.text = "${formatNumber(totalBeratTBS)} Kg"
        tvTotalBeratMatang.text = "${formatNumber(totalBeratMatang)} Kg"
        tvTotalBeratTidakMatang.text = "${formatNumber(totalBeratTidakMatang)} Kg"
        tvTotalBeratKelewatMatang.text = "${formatNumber(totalBeratKelewatMatang)} Kg"
        tvTotalJumlahMatang.text = "$totalJumlahMatang buah"
        tvTotalJumlahTidakMatang.text = "$totalJumlahTidakMatang buah"
        tvTotalJumlahKelewatMatang.text = "$totalJumlahKelewatMatang buah"

        Log.d(TAG, "Total Pendapatan: $totalPendapatan")
        Log.d(TAG, "Total Berat TBS: $totalBeratTBS Kg")
    }

    private fun setupChart() {
        Log.d(TAG, "=== SETUP CHART START (Filter: $currentFilter) ===")
        Log.d(TAG, "Total panen data received: ${panenList.size}")

        // Prepare data based on filter
        val chartData = when (currentFilter) {
            FilterType.HARI -> prepareDailyData()
            FilterType.BULAN -> prepareMonthlyData()
            FilterType.TAHUN -> prepareYearlyData()
        }

        Log.d(TAG, "Chart will show ${chartData.size} data points")

        if (chartData.isEmpty()) {
            Log.w(TAG, "⚠️ No valid data to display after processing")
            showEmptyChart()
            return
        }

        // Prepare chart entries
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        chartData.forEachIndexed { index, (label, weight) ->
            entries.add(Entry(index.toFloat(), weight.toFloat()))
            labels.add(label)
            Log.d(TAG, "Chart Point [$index]: $label = $weight Kg")
        }

        Log.d(TAG, "Creating line chart with ${entries.size} points")

        // Create dataset
        val dataSet = LineDataSet(entries, "Total TBS (Kg)")
        dataSet.color = Color.parseColor("#4CAF50")
        dataSet.setCircleColor(Color.parseColor("#4CAF50"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 7f
        dataSet.setDrawCircleHole(true)
        dataSet.circleHoleColor = Color.WHITE
        dataSet.circleHoleRadius = 4f
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 11f
        dataSet.valueTextColor = Color.parseColor("#000000")
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#4CAF50")
        dataSet.fillAlpha = 50
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.2f

        // Create line data
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Customize X-Axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#666666")
        xAxis.textSize = 9f
        xAxis.labelRotationAngle = if (currentFilter == FilterType.HARI) -45f else 0f
        xAxis.setLabelCount(labels.size, false)

        // Customize Y-Axis (Left)
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.textColor = Color.parseColor("#666666")
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f

        // Disable Right Y-Axis
        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // General chart settings
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.legend.textColor = Color.parseColor("#000000")
        lineChart.legend.textSize = 12f
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)
        lineChart.animateX(1200)
        lineChart.setExtraOffsets(10f, 10f, 10f, 25f)

        // Refresh chart
        lineChart.invalidate()

        Log.d(TAG, "✓ Chart setup completed with ${entries.size} points")
        Log.d(TAG, "=== SETUP CHART END ===")
    }

    private fun prepareDailyData(): List<Pair<String, Double>> {
        Log.d(TAG, "=== PREPARING DAILY DATA ===")

        val dailyList = mutableListOf<Pair<String, Double>>()
        val sortedPanen = panenList.sortedBy { it.timestamp }

        val dateFormats = listOf(
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        )

        val outputFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))

        sortedPanen.forEach { panen ->
            var parsed = false
            for (format in dateFormats) {
                try {
                    val date = format.parse(panen.tanggalPanen.trim())
                    if (date != null) {
                        val label = outputFormat.format(date)
                        val weight = panen.getTotalBerat()
                        dailyList.add(Pair(label, weight))
                        parsed = true
                        break
                    }
                } catch (e: Exception) {
                    // Continue trying other formats
                }
            }

            if (!parsed) {
                try {
                    val date = java.util.Date(panen.timestamp)
                    val label = outputFormat.format(date)
                    val weight = panen.getTotalBerat()
                    dailyList.add(Pair(label, weight))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse date: ${panen.tanggalPanen}", e)
                }
            }
        }

        return dailyList
    }

    private fun prepareMonthlyData(): List<Pair<String, Double>> {
        Log.d(TAG, "=== PREPARING MONTHLY DATA ===")

        val monthlyMap = mutableMapOf<String, Double>()

        val dateFormats = listOf(
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        )

        val outputFormat = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))

        panenList.forEach { panen ->
            var parsed = false
            for (format in dateFormats) {
                try {
                    val date = format.parse(panen.tanggalPanen.trim())
                    if (date != null) {
                        val monthKey = outputFormat.format(date)
                        val weight = panen.getTotalBerat()
                        monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0.0) + weight
                        parsed = true
                        break
                    }
                } catch (e: Exception) {
                    // Continue trying other formats
                }
            }

            if (!parsed) {
                try {
                    val date = java.util.Date(panen.timestamp)
                    val monthKey = outputFormat.format(date)
                    val weight = panen.getTotalBerat()
                    monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0.0) + weight
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse date: ${panen.tanggalPanen}", e)
                }
            }
        }

        val sortFormat = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))
        return monthlyMap.entries
            .sortedBy {
                try {
                    sortFormat.parse(it.key)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
            .map { Pair(it.key, it.value) }
    }

    private fun prepareYearlyData(): List<Pair<String, Double>> {
        Log.d(TAG, "=== PREPARING YEARLY DATA ===")

        val yearlyMap = mutableMapOf<String, Double>()

        val dateFormats = listOf(
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        )

        val outputFormat = SimpleDateFormat("yyyy", Locale("id", "ID"))

        panenList.forEach { panen ->
            var parsed = false
            for (format in dateFormats) {
                try {
                    val date = format.parse(panen.tanggalPanen.trim())
                    if (date != null) {
                        val yearKey = outputFormat.format(date)
                        val weight = panen.getTotalBerat()
                        yearlyMap[yearKey] = (yearlyMap[yearKey] ?: 0.0) + weight
                        parsed = true
                        break
                    }
                } catch (e: Exception) {
                    // Continue trying other formats
                }
            }

            if (!parsed) {
                try {
                    val date = java.util.Date(panen.timestamp)
                    val yearKey = outputFormat.format(date)
                    val weight = panen.getTotalBerat()
                    yearlyMap[yearKey] = (yearlyMap[yearKey] ?: 0.0) + weight
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse date: ${panen.tanggalPanen}", e)
                }
            }
        }

        return yearlyMap.entries
            .sortedBy { it.key }
            .map { Pair(it.key, it.value) }
    }

    private fun showEmptyChart() {
        Log.d(TAG, "Showing empty chart placeholder")

        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 0f))

        val dataSet = LineDataSet(entries, "Belum ada data")
        dataSet.color = Color.parseColor("#CCCCCC")
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.setNoDataText("Belum ada data panen untuk ditampilkan")
        lineChart.setNoDataTextColor(Color.parseColor("#666666"))
        lineChart.invalidate()
    }

    private fun formatNumber(number: Double): String {
        return if (number % 1.0 == 0.0) {
            number.toInt().toString()
        } else {
            String.format("%.1f", number)
        }
    }
}


//package com.example.sawit.IsiDashboard
//
//import android.graphics.Color
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import com.example.sawit.R
//import com.example.sawit.model.KebunData
//import com.example.sawit.model.PanenData
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import java.text.NumberFormat
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class DetailLaporanFragment : Fragment() {
//
//    private lateinit var btnBack: ImageView
//    private lateinit var imgKebun: ImageView
//    private lateinit var tvNamaKebun: TextView
//    private lateinit var tvLokasiKebun: TextView
//    private lateinit var tvLuas: TextView
//    private lateinit var tvJenisTanah: TextView
//    private lateinit var tvPokok: TextView
//    private lateinit var tvTahun: TextView
//
//    private lateinit var tvTotalPendapatan: TextView
//    private lateinit var tvTotalBeratTBS: TextView
//    private lateinit var tvTotalBeratMatang: TextView
//    private lateinit var tvTotalBeratTidakMatang: TextView
//    private lateinit var tvTotalBeratKelewatMatang: TextView
//    private lateinit var tvTotalJumlahMatang: TextView
//    private lateinit var tvTotalJumlahTidakMatang: TextView
//    private lateinit var tvTotalJumlahKelewatMatang: TextView
//
//    private lateinit var lineChart: LineChart
//
//    // Filter buttons
//    private lateinit var cardFilterHari: CardView
//    private lateinit var cardFilterBulan: CardView
//    private lateinit var cardFilterTahun: CardView
//    private lateinit var tvFilterHari: TextView
//    private lateinit var tvFilterBulan: TextView
//    private lateinit var tvFilterTahun: TextView
//
//    private var kebunData: KebunData? = null
//    private var panenList: List<PanenData> = emptyList()
//
//    // Filter state
//    private enum class FilterType {
//        HARI, BULAN, TAHUN
//    }
//    private var currentFilter: FilterType = FilterType.HARI
//
//    companion object {
//        private const val TAG = "DetailLaporanFragment"
//        private const val ARG_KEBUN_DATA = "kebun_data"
//        private const val ARG_PANEN_LIST = "panen_list"
//
//        fun newInstance(kebunData: KebunData, panenList: List<PanenData>): DetailLaporanFragment {
//            val fragment = DetailLaporanFragment()
//            val args = Bundle()
//            args.putParcelable(ARG_KEBUN_DATA, kebunData)
//            args.putParcelableArrayList(ARG_PANEN_LIST, ArrayList(panenList))
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            kebunData = it.getParcelable(ARG_KEBUN_DATA)
//            panenList = it.getParcelableArrayList<PanenData>(ARG_PANEN_LIST) ?: emptyList()
//        }
//        Log.d(TAG, "Fragment created with ${panenList.size} panen data")
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_detail_laporan, container, false)
//
//        initViews(rootView)
//        setupListeners()
//        displayData()
//        setupChart()
//
//        return rootView
//    }
//
//    private fun initViews(view: View) {
//        btnBack = view.findViewById(R.id.btnBack)
//        imgKebun = view.findViewById(R.id.imgKebun)
//        tvNamaKebun = view.findViewById(R.id.tvNamaKebun)
//        tvLokasiKebun = view.findViewById(R.id.tvLokasiKebun)
//        tvLuas = view.findViewById(R.id.tvLuas)
//        tvJenisTanah = view.findViewById(R.id.tvJenisTanah)
//        tvPokok = view.findViewById(R.id.tvPokok)
//        tvTahun = view.findViewById(R.id.tvTahun)
//
//        tvTotalPendapatan = view.findViewById(R.id.tvTotalPendapatan)
//        tvTotalBeratTBS = view.findViewById(R.id.tvTotalBeratTBS)
//        tvTotalBeratMatang = view.findViewById(R.id.tvTotalBeratMatang)
//        tvTotalBeratTidakMatang = view.findViewById(R.id.tvTotalBeratTidakMatang)
//        tvTotalBeratKelewatMatang = view.findViewById(R.id.tvTotalBeratKelewatMatang)
//        tvTotalJumlahMatang = view.findViewById(R.id.tvTotalJumlahMatang)
//        tvTotalJumlahTidakMatang = view.findViewById(R.id.tvTotalJumlahTidakMatang)
//        tvTotalJumlahKelewatMatang = view.findViewById(R.id.tvTotalJumlahKelewatMatang)
//
//        lineChart = view.findViewById(R.id.lineChart)
//
//        // Filter buttons
//        cardFilterHari = view.findViewById(R.id.cardFilterHari)
//        cardFilterBulan = view.findViewById(R.id.cardFilterBulan)
//        cardFilterTahun = view.findViewById(R.id.cardFilterTahun)
//        tvFilterHari = view.findViewById(R.id.tvFilterHari)
//        tvFilterBulan = view.findViewById(R.id.tvFilterBulan)
//        tvFilterTahun = view.findViewById(R.id.tvFilterTahun)
//    }
//
//    private fun setupListeners() {
//        btnBack.setOnClickListener {
//            activity?.supportFragmentManager?.popBackStack()
//        }
//
//        // Filter button listeners
//        cardFilterHari.setOnClickListener {
//            currentFilter = FilterType.HARI
//            updateFilterUI()
//            setupChart()
//        }
//
//        cardFilterBulan.setOnClickListener {
//            currentFilter = FilterType.BULAN
//            updateFilterUI()
//            setupChart()
//        }
//
//        cardFilterTahun.setOnClickListener {
//            currentFilter = FilterType.TAHUN
//            updateFilterUI()
//            setupChart()
//        }
//    }
//
//    private fun updateFilterUI() {
//        val activeColor = ContextCompat.getColor(requireContext(), R.color.green_primary)
//        val inactiveColor = Color.parseColor("#E0E0E0")
//        val activeTextColor = Color.WHITE
//        val inactiveTextColor = Color.parseColor("#666666")
//
//        // Reset all
//        cardFilterHari.setCardBackgroundColor(inactiveColor)
//        cardFilterBulan.setCardBackgroundColor(inactiveColor)
//        cardFilterTahun.setCardBackgroundColor(inactiveColor)
//        tvFilterHari.setTextColor(inactiveTextColor)
//        tvFilterBulan.setTextColor(inactiveTextColor)
//        tvFilterTahun.setTextColor(inactiveTextColor)
//
//        // Set active
//        when (currentFilter) {
//            FilterType.HARI -> {
//                cardFilterHari.setCardBackgroundColor(activeColor)
//                tvFilterHari.setTextColor(activeTextColor)
//            }
//            FilterType.BULAN -> {
//                cardFilterBulan.setCardBackgroundColor(activeColor)
//                tvFilterBulan.setTextColor(activeTextColor)
//            }
//            FilterType.TAHUN -> {
//                cardFilterTahun.setCardBackgroundColor(activeColor)
//                tvFilterTahun.setTextColor(activeTextColor)
//            }
//        }
//    }
//
//    private fun displayData() {
//        // Display Kebun Data
//        kebunData?.let { kebun ->
//            tvNamaKebun.text = kebun.namaKebun
//            tvLokasiKebun.text = kebun.lokasiKebun
//            tvLuas.text = "${String.format("%.0f", kebun.luasLahan)} ha"
//            tvJenisTanah.text = kebun.jenisTanah
//            tvPokok.text = "${kebun.jumlahTanaman} Pokok"
//            tvTahun.text = "${kebun.getUsiaTahun()} Tahun"
//        }
//
//        // Calculate totals from all panen data
//        var totalPendapatan = 0.0
//        var totalBeratTBS = 0.0
//        var totalBeratMatang = 0.0
//        var totalBeratTidakMatang = 0.0
//        var totalBeratKelewatMatang = 0.0
//        var totalJumlahMatang = 0
//        var totalJumlahTidakMatang = 0
//        var totalJumlahKelewatMatang = 0
//
//        panenList.forEach { panen ->
//            totalPendapatan += panen.getTotalPendapatan()
//            totalBeratTBS += panen.getTotalBerat()
//            totalBeratMatang += panen.tbsMatang
//            totalBeratTidakMatang += panen.tbsTidakMatang
//            totalBeratKelewatMatang += panen.tbsKelewatMatang
//            totalJumlahMatang += panen.jumlahMatang
//            totalJumlahTidakMatang += panen.jumlahTidakMatang
//            totalJumlahKelewatMatang += panen.jumlahKelewatMatang
//        }
//
//        // Display totals
//        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
//        tvTotalPendapatan.text = currencyFormat.format(totalPendapatan)
//        tvTotalBeratTBS.text = "${formatNumber(totalBeratTBS)} Kg"
//        tvTotalBeratMatang.text = "${formatNumber(totalBeratMatang)} Kg"
//        tvTotalBeratTidakMatang.text = "${formatNumber(totalBeratTidakMatang)} Kg"
//        tvTotalBeratKelewatMatang.text = "${formatNumber(totalBeratKelewatMatang)} Kg"
//        tvTotalJumlahMatang.text = "$totalJumlahMatang buah"
//        tvTotalJumlahTidakMatang.text = "$totalJumlahTidakMatang buah"
//        tvTotalJumlahKelewatMatang.text = "$totalJumlahKelewatMatang buah"
//
//        Log.d(TAG, "Total Pendapatan: $totalPendapatan")
//        Log.d(TAG, "Total Berat TBS: $totalBeratTBS Kg")
//    }
//
//    private fun setupChart() {
//        Log.d(TAG, "=== SETUP CHART START (Filter: $currentFilter) ===")
//        Log.d(TAG, "Total panen data received: ${panenList.size}")
//
//        // Prepare data based on filter
//        val chartData = when (currentFilter) {
//            FilterType.HARI -> prepareDailyData()
//            FilterType.BULAN -> prepareMonthlyData()
//            FilterType.TAHUN -> prepareYearlyData()
//        }
//
//        Log.d(TAG, "Chart will show ${chartData.size} data points")
//
//        if (chartData.isEmpty()) {
//            Log.w(TAG, "⚠️ No valid data to display after processing")
//            showEmptyChart()
//            return
//        }
//
//        // Prepare chart entries
//        val entries = ArrayList<Entry>()
//        val labels = ArrayList<String>()
//
//        chartData.forEachIndexed { index, (label, weight) ->
//            entries.add(Entry(index.toFloat(), weight.toFloat()))
//            labels.add(label)
//            Log.d(TAG, "Chart Point [$index]: $label = $weight Kg")
//        }
//
//        Log.d(TAG, "Creating line chart with ${entries.size} points")
//
//        // Create dataset
//        val dataSet = LineDataSet(entries, "Total TBS (Kg)")
//        dataSet.color = Color.parseColor("#4CAF50")
//        dataSet.setCircleColor(Color.parseColor("#4CAF50"))
//        dataSet.lineWidth = 3f
//        dataSet.circleRadius = 7f
//        dataSet.setDrawCircleHole(true)
//        dataSet.circleHoleColor = Color.WHITE
//        dataSet.circleHoleRadius = 4f
//        dataSet.setDrawValues(true)
//        dataSet.valueTextSize = 11f
//        dataSet.valueTextColor = Color.parseColor("#000000")
//        dataSet.setDrawFilled(true)
//        dataSet.fillColor = Color.parseColor("#4CAF50")
//        dataSet.fillAlpha = 50
//        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
//        dataSet.cubicIntensity = 0.2f
//
//        // Create line data
//        val lineData = LineData(dataSet)
//        lineChart.data = lineData
//
//        // Customize X-Axis
//        val xAxis = lineChart.xAxis
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//        xAxis.granularity = 1f
//        xAxis.setDrawGridLines(false)
//        xAxis.textColor = Color.parseColor("#666666")
//        xAxis.textSize = 9f
//        xAxis.labelRotationAngle = if (currentFilter == FilterType.HARI) -45f else 0f
//        xAxis.setLabelCount(labels.size, false)
//
//        // Customize Y-Axis (Left)
//        val leftAxis = lineChart.axisLeft
//        leftAxis.setDrawGridLines(true)
//        leftAxis.gridColor = Color.parseColor("#E0E0E0")
//        leftAxis.textColor = Color.parseColor("#666666")
//        leftAxis.textSize = 10f
//        leftAxis.axisMinimum = 0f
//
//        // Disable Right Y-Axis
//        val rightAxis = lineChart.axisRight
//        rightAxis.isEnabled = false
//
//        // General chart settings
//        lineChart.description.isEnabled = false
//        lineChart.legend.isEnabled = true
//        lineChart.legend.textColor = Color.parseColor("#000000")
//        lineChart.legend.textSize = 12f
//        lineChart.setTouchEnabled(true)
//        lineChart.isDragEnabled = true
//        lineChart.setScaleEnabled(true)
//        lineChart.setPinchZoom(true)
//        lineChart.setDrawGridBackground(false)
//        lineChart.animateX(1200)
//        lineChart.setExtraOffsets(10f, 10f, 10f, 25f)
//
//        // Refresh chart
//        lineChart.invalidate()
//
//        Log.d(TAG, "✓ Chart setup completed with ${entries.size} points")
//        Log.d(TAG, "=== SETUP CHART END ===")
//    }
//
//    /**
//     * Prepare data per hari
//     */
//    private fun prepareDailyData(): List<Pair<String, Double>> {
//        Log.d(TAG, "=== PREPARING DAILY DATA ===")
//
//        val dailyList = mutableListOf<Pair<String, Double>>()
//        val sortedPanen = panenList.sortedBy { it.timestamp }
//
//        val dateFormats = listOf(
//            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
//        )
//
//        val outputFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))
//
//        sortedPanen.forEach { panen ->
//            var parsed = false
//            for (format in dateFormats) {
//                try {
//                    val date = format.parse(panen.tanggalPanen.trim())
//                    if (date != null) {
//                        val label = outputFormat.format(date)
//                        val weight = panen.getTotalBerat()
//                        dailyList.add(Pair(label, weight))
//                        parsed = true
//                        break
//                    }
//                } catch (e: Exception) {
//                    // Continue trying other formats
//                }
//            }
//
//            if (!parsed) {
//                try {
//                    val date = java.util.Date(panen.timestamp)
//                    val label = outputFormat.format(date)
//                    val weight = panen.getTotalBerat()
//                    dailyList.add(Pair(label, weight))
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to parse date: ${panen.tanggalPanen}", e)
//                }
//            }
//        }
//
//        return dailyList
//    }
//
//    /**
//     * Prepare data per bulan (aggregate by month)
//     */
//    private fun prepareMonthlyData(): List<Pair<String, Double>> {
//        Log.d(TAG, "=== PREPARING MONTHLY DATA ===")
//
//        val monthlyMap = mutableMapOf<String, Double>()
//
//        val dateFormats = listOf(
//            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
//        )
//
//        val outputFormat = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))
//
//        panenList.forEach { panen ->
//            var parsed = false
//            for (format in dateFormats) {
//                try {
//                    val date = format.parse(panen.tanggalPanen.trim())
//                    if (date != null) {
//                        val monthKey = outputFormat.format(date)
//                        val weight = panen.getTotalBerat()
//                        monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0.0) + weight
//                        parsed = true
//                        break
//                    }
//                } catch (e: Exception) {
//                    // Continue trying other formats
//                }
//            }
//
//            if (!parsed) {
//                try {
//                    val date = java.util.Date(panen.timestamp)
//                    val monthKey = outputFormat.format(date)
//                    val weight = panen.getTotalBerat()
//                    monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0.0) + weight
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to parse date: ${panen.tanggalPanen}", e)
//                }
//            }
//        }
//
//        // Sort by date
//        val sortFormat = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))
//        return monthlyMap.entries
//            .sortedBy {
//                try {
//                    sortFormat.parse(it.key)?.time ?: 0L
//                } catch (e: Exception) {
//                    0L
//                }
//            }
//            .map { Pair(it.key, it.value) }
//    }
//
//    /**
//     * Prepare data per tahun (aggregate by year)
//     */
//    private fun prepareYearlyData(): List<Pair<String, Double>> {
//        Log.d(TAG, "=== PREPARING YEARLY DATA ===")
//
//        val yearlyMap = mutableMapOf<String, Double>()
//
//        val dateFormats = listOf(
//            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
//            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
//        )
//
//        val outputFormat = SimpleDateFormat("yyyy", Locale("id", "ID"))
//
//        panenList.forEach { panen ->
//            var parsed = false
//            for (format in dateFormats) {
//                try {
//                    val date = format.parse(panen.tanggalPanen.trim())
//                    if (date != null) {
//                        val yearKey = outputFormat.format(date)
//                        val weight = panen.getTotalBerat()
//                        yearlyMap[yearKey] = (yearlyMap[yearKey] ?: 0.0) + weight
//                        parsed = true
//                        break
//                    }
//                } catch (e: Exception) {
//                    // Continue trying other formats
//                }
//            }
//
//            if (!parsed) {
//                try {
//                    val date = java.util.Date(panen.timestamp)
//                    val yearKey = outputFormat.format(date)
//                    val weight = panen.getTotalBerat()
//                    yearlyMap[yearKey] = (yearlyMap[yearKey] ?: 0.0) + weight
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to parse date: ${panen.tanggalPanen}", e)
//                }
//            }
//        }
//
//        // Sort by year
//        return yearlyMap.entries
//            .sortedBy { it.key }
//            .map { Pair(it.key, it.value) }
//    }
//
//    private fun showEmptyChart() {
//        Log.d(TAG, "Showing empty chart placeholder")
//
//        val entries = ArrayList<Entry>()
//        entries.add(Entry(0f, 0f))
//
//        val dataSet = LineDataSet(entries, "Belum ada data")
//        dataSet.color = Color.parseColor("#CCCCCC")
//        dataSet.setDrawCircles(false)
//        dataSet.setDrawValues(false)
//
//        val lineData = LineData(dataSet)
//        lineChart.data = lineData
//        lineChart.setNoDataText("Belum ada data panen untuk ditampilkan")
//        lineChart.setNoDataTextColor(Color.parseColor("#666666"))
//        lineChart.invalidate()
//    }
//
//    private fun formatNumber(number: Double): String {
//        return if (number % 1.0 == 0.0) {
//            number.toInt().toString()
//        } else {
//            String.format("%.1f", number)
//        }
//    }
//}
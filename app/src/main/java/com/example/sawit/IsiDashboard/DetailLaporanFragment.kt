package com.example.sawit.IsiDashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvTotalBeratTBS: TextView
    private lateinit var tvTotalBeratMatang: TextView
    private lateinit var tvTotalBeratTidakMatang: TextView
    private lateinit var tvTotalBeratKelewatMatang: TextView
    private lateinit var tvTotalJumlahMatang: TextView
    private lateinit var tvTotalJumlahTidakMatang: TextView
    private lateinit var tvTotalJumlahKelewatMatang: TextView

    private lateinit var lineChart: LineChart

    private var kebunData: KebunData? = null
    private var panenList: List<PanenData> = emptyList()

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
        setupChart()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
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
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
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
        Log.d(TAG, "=== SETUP CHART START (PER DAY) ===")
        Log.d(TAG, "Total panen data received: ${panenList.size}")

        // DEBUGGING: Log semua data mentah
        panenList.forEachIndexed { index, panen ->
            Log.d(TAG, "RAW Data [$index]:")
            Log.d(TAG, "  - ID: ${panen.id}")
            Log.d(TAG, "  - KebunId: ${panen.kebunId}")
            Log.d(TAG, "  - Tanggal: '${panen.tanggalPanen}'")
            Log.d(TAG, "  - Total Berat: ${panen.getTotalBerat()} Kg")
            Log.d(TAG, "  - Timestamp: ${panen.timestamp}")
        }

        // Prepare data for chart
        val dailyData = prepareDailyData()

        Log.d(TAG, "Chart will show ${dailyData.size} data points")

        if (dailyData.isEmpty()) {
            Log.w(TAG, "⚠️ No valid data to display after processing")
            showEmptyChart()
            return
        }

        // Prepare chart entries
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        dailyData.forEachIndexed { index, (label, weight) ->
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
        xAxis.labelRotationAngle = -45f
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

    /**
     * FIXED: Prepare data per hari dengan penanganan error yang lebih baik
     */
    private fun prepareDailyData(): List<Pair<String, Double>> {
        Log.d(TAG, "=== PREPARING DAILY DATA (FIXED) ===")

        val dailyList = mutableListOf<Pair<String, Double>>()

        // PENTING: Sort berdasarkan timestamp terlebih dahulu
        val sortedPanen = panenList.sortedBy { it.timestamp }
        Log.d(TAG, "Sorted ${sortedPanen.size} records by timestamp")

        // Multiple date formats untuk fallback
        val dateFormats = listOf(
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        )

        val outputFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))

        sortedPanen.forEachIndexed { index, panen ->
            var parsed = false
            var label = ""

            // Coba semua format tanggal
            for (format in dateFormats) {
                try {
                    val date = format.parse(panen.tanggalPanen.trim())
                    if (date != null) {
                        label = outputFormat.format(date)
                        val weight = panen.getTotalBerat()

                        dailyList.add(Pair(label, weight))

                        Log.d(TAG, "✓ [$index] Success: '${panen.tanggalPanen}' -> $label | $weight Kg")
                        parsed = true
                        break
                    }
                } catch (e: Exception) {
                    // Continue trying other formats
                }
            }

            if (!parsed) {
                // Fallback: gunakan timestamp
                try {
                    val date = java.util.Date(panen.timestamp)
                    label = outputFormat.format(date)
                    val weight = panen.getTotalBerat()

                    dailyList.add(Pair(label, weight))

                    Log.w(TAG, "⚠ [$index] Fallback using timestamp: '${panen.tanggalPanen}' -> $label | $weight Kg")
                } catch (e: Exception) {
                    Log.e(TAG, "✗ [$index] FAILED to parse: '${panen.tanggalPanen}'", e)
                }
            }
        }

        Log.d(TAG, "Total daily entries created: ${dailyList.size}")
        Log.d(TAG, "=== END PREPARING ===")

        return dailyList
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
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
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
//    private var kebunData: KebunData? = null
//    private var panenList: List<PanenData> = emptyList()
//
//    companion object {
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
//    }
//
//    private fun setupListeners() {
//        btnBack.setOnClickListener {
//            activity?.supportFragmentManager?.popBackStack()
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
//    }
//
//    private fun setupChart() {
//        // Prepare data for chart
//        val entries = ArrayList<Entry>()
//        val labels = ArrayList<String>()
//
//        // Group by month and calculate total pendapatan
//        val monthlyData = panenList.groupBy { it.getBulan() }
//            .mapValues { entry -> entry.value.sumOf { it.getTotalPendapatan() } }
//
//        monthlyData.entries.forEachIndexed { index, entry ->
//            entries.add(Entry(index.toFloat(), entry.value.toFloat() / 1000)) // Convert to thousands
//            labels.add(entry.key)
//        }
//
//        // If no data, show sample data
//        if (entries.isEmpty()) {
//            entries.add(Entry(0f, 10f))
//            entries.add(Entry(1f, 25f))
//            entries.add(Entry(2f, 15f))
//            entries.add(Entry(3f, 35f))
//            labels.addAll(listOf("Bulan 1", "Bulan 2", "Bulan 3", "Bulan 4"))
//        }
//
//        // Create dataset
//        val dataSet = LineDataSet(entries, "Pendapatan (Ribu Rupiah)")
//        dataSet.color = Color.parseColor("#4CAF50")
//        dataSet.setCircleColor(Color.parseColor("#4CAF50"))
//        dataSet.lineWidth = 3f
//        dataSet.circleRadius = 5f
//        dataSet.setDrawCircleHole(true)
//        dataSet.circleHoleColor = Color.WHITE
//        dataSet.setDrawValues(false)
//        dataSet.setDrawFilled(false)
//        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
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
//
//        // Customize Y-Axis
//        val leftAxis = lineChart.axisLeft
//        leftAxis.setDrawGridLines(true)
//        leftAxis.gridColor = Color.parseColor("#E0E0E0")
//        leftAxis.textColor = Color.parseColor("#666666")
//
//        val rightAxis = lineChart.axisRight
//        rightAxis.isEnabled = false
//
//        // General chart settings
//        lineChart.description.isEnabled = false
//        lineChart.legend.isEnabled = false
//        lineChart.setTouchEnabled(true)
//        lineChart.isDragEnabled = true
//        lineChart.setScaleEnabled(false)
//        lineChart.setPinchZoom(false)
//        lineChart.setDrawGridBackground(false)
//        lineChart.animateX(1000)
//
//        // Refresh chart
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
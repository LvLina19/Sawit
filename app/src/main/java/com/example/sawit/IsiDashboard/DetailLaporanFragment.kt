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
        Log.d(TAG, "=== SETUP CHART START ===")
        Log.d(TAG, "Total panen data received: ${panenList.size}")

        // Log all raw data
        panenList.forEachIndexed { index, panen ->
            Log.d(TAG, "Raw Data [$index]: Tanggal='${panen.tanggalPanen}' | TBS Matang=${panen.tbsMatang} | TBS Tidak Matang=${panen.tbsTidakMatang} | TBS Kelewat Matang=${panen.tbsKelewatMatang} | Total=${panen.getTotalBerat()} Kg")
        }

        // Group data by month and sum the total TBS weight
        val monthlyData = groupDataByMonth()

        Log.d(TAG, "After grouping: ${monthlyData.size} unique months")

        // Prepare chart entries
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        monthlyData.entries.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
            Log.d(TAG, "Chart Entry [$index]: Month=${entry.key} | Weight=${entry.value} Kg")
        }

        // If no data, show message in chart
        if (entries.isEmpty()) {
            Log.w(TAG, "⚠️ No data to display in chart - entries is empty")
            showEmptyChart()
            return
        }

        Log.d(TAG, "Creating chart with ${entries.size} data points")

        // Create dataset
        val dataSet = LineDataSet(entries, "Total TBS (Kg)")
        dataSet.color = Color.parseColor("#4CAF50")
        dataSet.setCircleColor(Color.parseColor("#4CAF50"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 6f
        dataSet.setDrawCircleHole(true)
        dataSet.circleHoleColor = Color.WHITE
        dataSet.circleHoleRadius = 3f
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.parseColor("#000000")
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#4CAF50")
        dataSet.fillAlpha = 50
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

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
        xAxis.textSize = 10f
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
        lineChart.animateX(1000)
        lineChart.setExtraOffsets(10f, 10f, 10f, 20f)

        // Refresh chart
        lineChart.invalidate()

        Log.d(TAG, "✓ Chart setup completed successfully with ${entries.size} points")
        Log.d(TAG, "=== SETUP CHART END ===")
    }

    /**
     * Group panen data by month and sum total TBS weight
     * MENGGUNAKAN formattedTanggalPendek untuk parsing yang lebih akurat
     */
    private fun groupDataByMonth(): Map<String, Double> {
        val monthlyMap = mutableMapOf<String, Double>()

        // Format untuk parsing dan output
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        val outputFormat = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))

        Log.d(TAG, "=== GROUPING DATA BY MONTH ===")
        Log.d(TAG, "Total panen records to process: ${panenList.size}")

        panenList.forEachIndexed { index, panen ->
            try {
                // Gunakan formattedTanggalPendek yang sudah ter-convert
                val dateString = panen.getFormattedTanggalPendek()
                Log.d(TAG, "[$index] Processing: Original='${panen.tanggalPanen}' | Formatted='$dateString'")

                val date = inputFormat.parse(dateString)

                if (date != null) {
                    // Get month-year string
                    val monthYear = outputFormat.format(date)

                    // Sum total TBS weight for this month
                    val totalWeight = panen.getTotalBerat()
                    monthlyMap[monthYear] = (monthlyMap[monthYear] ?: 0.0) + totalWeight

                    Log.d(TAG, "[$index] ✓ SUCCESS: '$dateString' -> $monthYear | Weight: $totalWeight Kg | Running Total: ${monthlyMap[monthYear]} Kg")
                } else {
                    Log.e(TAG, "[$index] ✗ FAILED: Date is null for '$dateString'")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[$index] ✗ ERROR parsing: '${panen.tanggalPanen}'", e)
            }
        }

        Log.d(TAG, "=== MONTHLY SUMMARY ===")
        monthlyMap.forEach { (month, weight) ->
            Log.d(TAG, "Month: $month | Total Weight: $weight Kg")
        }

        // Sort by date (chronologically)
        val sortedMap = monthlyMap.toSortedMap(compareBy {
            try {
                outputFormat.parse(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error sorting month: $it", e)
                null
            }
        })

        Log.d(TAG, "Total unique months after sorting: ${sortedMap.size}")
        Log.d(TAG, "=== END GROUPING ===")

        return sortedMap
    }

    private fun showEmptyChart() {
        // Show empty state
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
package com.example.locationapp

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvAddress: TextView
    private lateinit var tvLatLng: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnCamera: Button
    private lateinit var btnPhotoList: Button
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var currentAltitude = 0.0
    private var currentAddress = ""
    private var photoUri: Uri? = null
    private var photoFilePath: String = ""

    private var currentSessionId: String = ""
    private val sessionPhotoPaths = mutableListOf<String>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            sessionPhotoPaths.add(photoFilePath)
            showContinueOrInputDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAddress    = findViewById(R.id.tvAddress)
        tvLatLng     = findViewById(R.id.tvLatLng)
        tvAltitude   = findViewById(R.id.tvAltitude)
        btnRefresh   = findViewById(R.id.btnRefresh)
        btnCamera    = findViewById(R.id.btnCamera)
        btnPhotoList = findViewById(R.id.btnPhotoList)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        btnRefresh.setOnClickListener { checkPermissionAndGetLocation() }
        btnCamera.setOnClickListener { startNewSession() }
        btnPhotoList.setOnClickListener {
            startActivity(Intent(this, PhotoListActivity::class.java))
        }
        checkPermissionAndGetLocation()
    }

    private fun startNewSession() {
        currentSessionId = UUID.randomUUID().toString()
        sessionPhotoPaths.clear()
        checkCameraPermission()
    }

    private fun showContinueOrInputDialog() {
        AlertDialog.Builder(this)
            .setTitle("撮影完了（${sessionPhotoPaths.size}枚）")
            .setMessage("続けて撮影しますか？\nまたはデータ入力に進みますか？")
            .setPositiveButton("続けて撮影") { _, _ -> checkCameraPermission() }
            .setNegativeButton("データ入力へ") { _, _ -> showInputDialog() }
            .setCancelable(false)
            .show()
    }

    private fun showInputDialog() {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)

        // 床面積
        val etFloorArea1     = dialogView.findViewById<EditText>(R.id.etFloorArea1)
        val etFloorArea2     = dialogView.findViewById<EditText>(R.id.etFloorArea2)
        val etFloorArea3     = dialogView.findViewById<EditText>(R.id.etFloorArea3)
        val etFloorAreaTotal = dialogView.findViewById<EditText>(R.id.etFloorAreaTotal)
        // 土地
        val etArea               = dialogView.findViewById<EditText>(R.id.etArea)
        val spinnerLandCategory  = dialogView.findViewById<Spinner>(R.id.spinnerLandCategory)
        val etFrontage           = dialogView.findViewById<EditText>(R.id.etFrontage)
        val etRoadWidth          = dialogView.findViewById<EditText>(R.id.etRoadWidth)
        val spinnerRoadDirection = dialogView.findViewById<Spinner>(R.id.spinnerRoadDirection)
        // 建物
        val spinnerStructure     = dialogView.findViewById<Spinner>(R.id.spinnerStructure)
        val spinnerBuiltYear     = dialogView.findViewById<Spinner>(R.id.spinnerBuiltYear)
        val spinnerFloors        = dialogView.findViewById<Spinner>(R.id.spinnerFloors)
        val spinnerLayout        = dialogView.findViewById<Spinner>(R.id.spinnerLayout)
        val spinnerParking       = dialogView.findViewById<Spinner>(R.id.spinnerParking)
        val spinnerWaterSupply   = dialogView.findViewById<Spinner>(R.id.spinnerWaterSupply)
        val spinnerSewage        = dialogView.findViewById<Spinner>(R.id.spinnerSewage)
        val etMemo               = dialogView.findViewById<EditText>(R.id.etMemo)

        fun setSpinner(spinner: Spinner, arrayResId: Int) {
            ArrayAdapter.createFromResource(this, arrayResId, android.R.layout.simple_spinner_item)
                .also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = it
                }
        }
        setSpinner(spinnerLandCategory,  R.array.land_categories)
        setSpinner(spinnerRoadDirection, R.array.road_directions)
        setSpinner(spinnerStructure,     R.array.structures)
        setSpinner(spinnerBuiltYear,     R.array.built_years)
        setSpinner(spinnerFloors,        R.array.floors_list)
        setSpinner(spinnerLayout,        R.array.layouts)
        setSpinner(spinnerParking,       R.array.parking_list)
        setSpinner(spinnerWaterSupply,   R.array.utility_options)
        setSpinner(spinnerSewage,        R.array.utility_options)

        AlertDialog.Builder(this)
            .setTitle("物件情報入力（${sessionPhotoPaths.size}枚）")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                saveSession(
                    floorArea1    = etFloorArea1.text.toString(),
                    floorArea2    = etFloorArea2.text.toString(),
                    floorArea3    = etFloorArea3.text.toString(),
                    floorAreaTotal= etFloorAreaTotal.text.toString(),
                    area          = etArea.text.toString(),
                    landCategory  = spinnerLandCategory.selectedItem.toString(),
                    frontage      = etFrontage.text.toString(),
                    roadWidth     = etRoadWidth.text.toString(),
                    roadDirection = spinnerRoadDirection.selectedItem.toString(),
                    structure     = spinnerStructure.selectedItem.toString(),
                    builtYear     = spinnerBuiltYear.selectedItem.toString(),
                    floors        = spinnerFloors.selectedItem.toString(),
                    layout        = spinnerLayout.selectedItem.toString(),
                    parking       = spinnerParking.selectedItem.toString(),
                    waterSupply   = spinnerWaterSupply.selectedItem.toString(),
                    sewage        = spinnerSewage.selectedItem.toString(),
                    memo          = etMemo.text.toString()
                )
            }
            .setNegativeButton("スキップ") { _, _ -> saveSession() }
            .setCancelable(false)
            .show()
    }

    private fun saveSession(
        floorArea1: String = "", floorArea2: String = "",
        floorArea3: String = "", floorAreaTotal: String = "",
        area: String = "", landCategory: String = "", frontage: String = "",
        roadWidth: String = "", roadDirection: String = "", structure: String = "",
        builtYear: String = "", floors: String = "", layout: String = "",
        parking: String = "", waterSupply: String = "", sewage: String = "", memo: String = ""
    ) {
        val db  = AppDatabase.getDatabase(this)
        val now = System.currentTimeMillis()
        scope.launch {
            val photos = sessionPhotoPaths.mapIndexed { index, path ->
                Photo(
                    filePath       = path,
                    address        = currentAddress,
                    latitude       = currentLat,
                    longitude      = currentLng,
                    altitude       = currentAltitude,
                    sessionId      = currentSessionId,
                    isMainPhoto    = (index == 0),
                    floorArea1     = floorArea1,
                    floorArea2     = floorArea2,
                    floorArea3     = floorArea3,
                    floorAreaTotal = floorAreaTotal,
                    area           = area,
                    landCategory   = landCategory,
                    frontage       = frontage,
                    roadWidth      = roadWidth,
                    roadDirection  = roadDirection,
                    structure      = structure,
                    builtYear      = builtYear,
                    floors         = floors,
                    layout         = layout,
                    parking        = parking,
                    waterSupply    = waterSupply,
                    sewage         = sewage,
                    memo           = memo,
                    timestamp      = now + index
                )
            }
            withContext(Dispatchers.IO) { db.photoDao().insertAll(photos) }
            Toast.makeText(this@MainActivity, "${photos.size}枚を保存しました", Toast.LENGTH_SHORT).show()
            sessionPhotoPaths.clear()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2001)
        }
    }

    private fun openCamera() {
        val timeStamp  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile  = File.createTempFile("PHOTO_${timeStamp}_", ".jpg", storageDir)
        photoFilePath  = photoFile.absolutePath
        photoUri = FileProvider.getUriForFile(
            this, "com.example.locationapp.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun checkPermissionAndGetLocation() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocation == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE)
        }
    }

    private fun getCurrentLocation() {
        tvAddress.text  = "位置情報を取得中..."
        tvLatLng.text   = ""
        tvAltitude.text = "標高を取得中..."

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLng = location.longitude
                    tvLatLng.text = "緯度: %.6f　経度: %.6f".format(currentLat, currentLng)
                    getAddressFromLocation(currentLat, currentLng)
                    getAltitudeFromGSI(currentLat, currentLng)
                } else {
                    tvAddress.text  = "位置情報を取得できませんでした"
                    tvAltitude.text = ""
                }
            }
    }

    private fun getAltitudeFromGSI(lat: Double, lng: Double) {
        scope.launch {
            try {
                val altitude = withContext(Dispatchers.IO) {
                    val url = "https://cyberjapandata2.gsi.go.jp/general/dem/scripts/getelevation.php" +
                              "?lon=$lng&lat=$lat&outtype=JSON"
                    val response = URL(url).readText()
                    JSONObject(response).getDouble("elevation")
                }
                currentAltitude = altitude
                tvAltitude.text = "標高: %.1f m（国土地理院）".format(altitude)
            } catch (e: Exception) {
                tvAltitude.text = "標高を取得できませんでした"
            }
        }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double) {
        try {
            val geocoder  = Geocoder(this, Locale.JAPANESE)
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val parts = listOf(
                    address.adminArea, address.locality, address.subLocality,
                    address.thoroughfare, address.subThoroughfare
                ).filterNotNull()
                currentAddress = if (parts.isNotEmpty()) parts.joinToString("")
                                 else address.getAddressLine(0) ?: ""
                tvAddress.text = currentAddress
            } else {
                tvAddress.text = "住所が見つかりませんでした"
            }
        } catch (e: Exception) {
            tvAddress.text = "住所の取得に失敗しました"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getCurrentLocation()
                else Toast.makeText(this, "位置情報の許可が必要です", Toast.LENGTH_LONG).show()
            }
            2001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openCamera()
                else Toast.makeText(this, "カメラの許可が必要です", Toast.LENGTH_LONG).show()
            }
        }
    }
}

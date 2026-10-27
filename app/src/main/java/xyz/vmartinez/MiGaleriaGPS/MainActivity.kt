package xyz.vmartinez.MiGaleriaGPS

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import xyz.vmartinez.MiGaleriaGPS.database.DatabaseHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var photoFile: File? = null
    private var photoUri: Uri? = null
    private var lastLocation: Location? = null
    private lateinit var preview: ImageView
    private lateinit var comment: EditText
    private var pendingSave = false

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok && photoFile != null) {
            preview.setImageURI(null)
            preview.setImageURI(photoUri)
            pendingSave = true
            obtenerUbicacionYGuardar()
        } else {
            photoFile?.delete()
            Toast.makeText(this, "No se tomó la fotografía", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            obtenerUbicacionYGuardar()
        } else {
            guardarRegistro(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getSharedPreferences("sesion", MODE_PRIVATE).getLong("usuario_id", -1L) < 1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.ivPreview)
        comment = findViewById(R.id.etComentario)

        findViewById<Button>(R.id.btnTomarFoto).setOnClickListener { abrirCamara() }
        findViewById<Button>(R.id.btnVerFotos).setOnClickListener {
            startActivity(Intent(this, ListaDeFotosActivity::class.java))
        }
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            getSharedPreferences("sesion", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun abrirCamara() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        if (!dir.exists()) dir.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        photoFile = File(dir, "IMG_$stamp.jpg")
        photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile!!)
        try {
            cameraLauncher.launch(photoUri!!)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerUbicacionYGuardar() {
        if (!pendingSave) return
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            val providers = lm.getProviders(true)
            lastLocation = providers.mapNotNull { p ->
                try {
                    lm.getLastKnownLocation(p)
                } catch (_: SecurityException) {
                    null
                }
            }.maxByOrNull { it.time }
        } catch (_: Exception) {
            lastLocation = null
        }
        guardarRegistro(lastLocation)
    }

    private fun guardarRegistro(location: Location?) {
        if (!pendingSave) return
        val uid = getSharedPreferences("sesion", MODE_PRIVATE).getLong("usuario_id", -1L)
        val f = photoFile ?: return
        try {
            DatabaseHelper(this).guardarFoto(uid, f.absolutePath, comment.text.toString().trim(), location?.latitude, location?.longitude)
            Toast.makeText(this, if (location != null) "Foto guardada con ubicación GPS" else "Foto guardada; no se obtuvo ubicación GPS", Toast.LENGTH_LONG).show()
            pendingSave = false
            comment.text.clear()
        } catch (e: Exception) {
            Toast.makeText(this, "Error guardando el registro: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

package xyz.vmartinez.MiGaleriaGPS

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import xyz.vmartinez.MiGaleriaGPS.database.DatabaseHelper

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val nombre = findViewById<EditText>(R.id.etNombre)
        val apellidos = findViewById<EditText>(R.id.etApellidos)
        val correo = findViewById<EditText>(R.id.etCorreo)
        val pass = findViewById<EditText>(R.id.etPassword)
        val btnRegistrarme = findViewById<Button>(R.id.btnRegistrarme)

        btnRegistrarme.setOnClickListener {
            val n = nombre.text.toString().trim()
            val a = apellidos.text.toString().trim()
            val c = correo.text.toString().trim()
            val p = pass.text.toString()

            if (n.isEmpty() || a.isEmpty() || c.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val ok = DatabaseHelper(this).registrarUsuario(n, a, c, Security.hash(p))
            if (ok) {
                Toast.makeText(this, "Usuario registrado. Ya puedes iniciar sesión", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "No se pudo registrar. Revisa si el correo ya existe", Toast.LENGTH_LONG).show()
            }
        }
    }
}

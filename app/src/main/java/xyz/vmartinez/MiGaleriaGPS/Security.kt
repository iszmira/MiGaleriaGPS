package xyz.vmartinez.MiGaleriaGPS

import java.security.MessageDigest

// Hash didáctico con SHA-256 y salt aleatorio. En producción se recomienda Argon2id o PBKDF2.
object Security {
    fun hash(password: String): String {
        val salt = "MiGaleriaGPS-curso-2026:" // Para el ejercicio. Sustituir por salt aleatorio por usuario en una versión de producción.
        return MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}

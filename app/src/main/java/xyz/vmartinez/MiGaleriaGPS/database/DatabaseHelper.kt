package xyz.vmartinez.MiGaleriaGPS.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "mi_galeria.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, apellidos TEXT NOT NULL, correo TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL)")
        db.execSQL("CREATE TABLE fotos (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_id INTEGER NOT NULL, ruta TEXT NOT NULL, comentario TEXT NOT NULL, latitud REAL, longitud REAL, fecha TEXT NOT NULL, FOREIGN KEY(usuario_id) REFERENCES usuarios(id))")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }

    fun registrarUsuario(nombre: String, apellidos: String, correo: String, hash: String): Boolean {
        val values = ContentValues().apply { put("nombre", nombre); put("apellidos", apellidos); put("correo", correo.lowercase()); put("password_hash", hash) }
        return writableDatabase.insert("usuarios", null, values) != -1L
    }
    fun validarUsuario(correo: String, hash: String): Long {
        readableDatabase.rawQuery("SELECT id FROM usuarios WHERE correo=? AND password_hash=?", arrayOf(correo.lowercase(), hash)).use { c -> return if (c.moveToFirst()) c.getLong(0) else -1L }
    }
    fun guardarFoto(usuarioId: Long, ruta: String, comentario: String, lat: Double?, lon: Double?) {
        val v = ContentValues().apply { put("usuario_id", usuarioId); put("ruta", ruta); put("comentario", comentario); if(lat != null) put("latitud",lat); if(lon != null) put("longitud",lon); put("fecha", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())) }
        writableDatabase.insert("fotos", null, v)
    }
    fun eliminarFoto(fotoId: Long): Boolean {
        return writableDatabase.delete("fotos", "id=?", arrayOf(fotoId.toString())) > 0
    }
    data class Foto(val id: Long, val ruta: String, val comentario: String, val lat: Double?, val lon: Double?, val fecha: String)
    fun fotosDe(usuarioId: Long): List<Foto> {
        val result = mutableListOf<Foto>()
        readableDatabase.rawQuery("SELECT id,ruta,comentario,latitud,longitud,fecha FROM fotos WHERE usuario_id=? ORDER BY id DESC", arrayOf(usuarioId.toString())).use { c ->
            while(c.moveToNext()) result.add(Foto(c.getLong(0), c.getString(1), c.getString(2), if(c.isNull(3)) null else c.getDouble(3), if(c.isNull(4)) null else c.getDouble(4), c.getString(5)))
        }; return result
    }
}

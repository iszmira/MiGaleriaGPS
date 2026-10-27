package xyz.vmartinez.MiGaleriaGPS

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.vmartinez.MiGaleriaGPS.database.DatabaseHelper
import java.io.File

class ListaDeFotosActivity : AppCompatActivity() {
    private lateinit var rvFotos: RecyclerView
    private lateinit var tvSinFotos: TextView
    private lateinit var adapter: FotosAdapter
    private var listaFotos = mutableListOf<DatabaseHelper.Foto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_de_fotos)

        rvFotos = findViewById(R.id.rvFotos)
        tvSinFotos = findViewById(R.id.tvSinFotos)

        rvFotos.layoutManager = LinearLayoutManager(this)
        adapter = FotosAdapter(listaFotos) { foto -> mostrarDetalle(foto) }
        rvFotos.adapter = adapter

        // Configurar gesto de deslizar horizontalmente (swipe) para revelar el icono de eliminar
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val deleteIcon = ContextCompat.getDrawable(this@ListaDeFotosActivity, android.R.drawable.ic_menu_delete)
            private val background = ColorDrawable(Color.parseColor("#D32F2F"))

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position in listaFotos.indices) {
                    val foto = listaFotos[position]
                    confirmarEliminar(foto, position)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2

                if (dX > 0) { // Deslizando a la derecha
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    deleteIcon?.let {
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + it.intrinsicHeight
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }
                } else if (dX < 0) { // Deslizando a la izquierda
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon?.let {
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + it.intrinsicHeight
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }
                } else {
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)
                deleteIcon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(rvFotos)

        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }

        cargarFotos()
    }

    override fun onResume() {
        super.onResume()
        cargarFotos()
    }

    private fun cargarFotos() {
        val uid = getSharedPreferences("sesion", MODE_PRIVATE).getLong("usuario_id", -1L)
        listaFotos.clear()
        listaFotos.addAll(DatabaseHelper(this).fotosDe(uid))

        if (listaFotos.isEmpty()) {
            tvSinFotos.visibility = View.VISIBLE
            rvFotos.visibility = View.GONE
        } else {
            tvSinFotos.visibility = View.GONE
            rvFotos.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }

    private fun confirmarEliminar(foto: DatabaseHelper.Foto, position: Int) {
        val nombreArchivo = File(foto.ruta).name
        AlertDialog.Builder(this)
            .setTitle("Eliminar fotografía")
            .setMessage("¿Deseas eliminar la foto \"$nombreArchivo\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                val f = File(foto.ruta)
                if (f.exists()) f.delete()
                DatabaseHelper(this).eliminarFoto(foto.id)
                Toast.makeText(this, "Fotografía eliminada", Toast.LENGTH_SHORT).show()
                cargarFotos()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun mostrarDetalle(foto: DatabaseHelper.Foto) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
        }
        val image = ImageView(this).apply {
            adjustViewBounds = true
            maxHeight = 600
            setImageBitmap(BitmapFactory.decodeFile(foto.ruta))
            contentDescription = "Fotografía seleccionada"
        }
        content.addView(image, LinearLayout.LayoutParams(-1, -2))
        content.addView(TextView(this).apply {
            text = "Comentario: ${foto.comentario.ifBlank { "Sin comentario" }}\nFecha: ${foto.fecha}\nUbicación GPS: ${if (foto.lat != null && foto.lon != null) "${foto.lat}, ${foto.lon}" else "No disponible"}"
            textSize = 16f
        })
        AlertDialog.Builder(this)
            .setTitle("Detalle de fotografía")
            .setView(content)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    class FotosAdapter(
        private val fotos: List<DatabaseHelper.Foto>,
        private val onItemClick: (DatabaseHelper.Foto) -> Unit
    ) : RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {

        class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombreFoto)
            val tvFecha: TextView = view.findViewById(R.id.tvFechaFoto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto, parent, false)
            return FotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
            val foto = fotos[position]
            holder.tvNombre.text = File(foto.ruta).name
            holder.tvFecha.text = foto.fecha

            holder.itemView.setOnClickListener { onItemClick(foto) }
        }

        override fun getItemCount(): Int = fotos.size
    }
}

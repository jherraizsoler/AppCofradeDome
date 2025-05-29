package com.example.CofradeDome.Adaptadores

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.CofradeDome.Models.DatosTarjetasCofrades
import com.example.CofradeDome.R

class AdaptadorTarjetasCofrades(
    private val contexto: Context,
    private val lista: ArrayList<DatosTarjetasCofrades>,
    private val escuchador: View.OnClickListener?, //Fuente Datos
    private var fuenteDatos: Typeface?
) :
    RecyclerView.Adapter<AdaptadorTarjetasCofrades.MiContenedor>(),
    View.OnClickListener {
    private val posicionEdicion = 0

    // Dentro de la clase AdaptadorTarjetasCofrades
    val elementosSeleccionados: ArrayList<Map<String, String?>> = ArrayList()

    fun toggleSeleccion(position: Int, isChecked: Boolean) {
        val dato = lista[position]
        val itemSeleccionado: MutableMap<String, String?> = HashMap()
        itemSeleccionado["idCofrade"] = dato.id.toString()
        itemSeleccionado["nombreCompleto"] = dato.nombrecompleto

        if (isChecked) {
            if (!elementosSeleccionados.contains(itemSeleccionado)) {
                elementosSeleccionados.add(itemSeleccionado)
            }
        } else {
            // Necesitamos una forma de identificar el elemento a eliminar.
            // Podemos iterar y comparar los IDs.
            elementosSeleccionados.removeIf { item: Map<String, String?> -> item["idCofrade"] == dato.id.toString() }
        }
    }

    fun setFuenteDatos(fuenteDatos: Typeface?) {
        this.fuenteDatos = fuenteDatos
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class MiContenedor(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var tvID: TextView =
            itemview.findViewById(R.id.tvIdCofrade_Tarjeta_Dato)
        var tvNombreCompleto: TextView =
            itemview.findViewById(R.id.tvNombreCompleto_Tarjeta_Dato)
        var cbSeleccion: CheckBox =
            itemview.findViewById(R.id.cb_Seleccion_Tarjeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiContenedor {
        val inflater = contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val vItem = inflater.inflate(R.layout.itemtarjeta_layout, parent, false)

        vItem.setOnClickListener(this)

        return MiContenedor(vItem)
    }

    override fun onClick(view: View) {
        escuchador?.onClick(view)
    }

    override fun onBindViewHolder(holder: MiContenedor, position: Int) {
        // Obtener la posición de forma dinámica

        val dato = lista[position]
        // Configurar los datos en las vistas del ítem
        Log.d(
            "OnBindViewHolderAdaptadorTarjetasCofrades ",
            "El nombre de la fuente Datos es: " + fuenteDatos.toString()
        )

        holder.tvID.typeface = fuenteDatos
        holder.tvID.text = dato.id.toString()

        holder.tvNombreCompleto.typeface = fuenteDatos
        holder.tvNombreCompleto.text = dato.nombrecompleto

        holder.cbSeleccion.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            toggleSeleccion(holder.adapterPosition, isChecked)
        }

        // Aquí podrías restaurar el estado de selección si es necesario
        var isCurrentlySelected = false
        for (seleccionado in elementosSeleccionados) {
            if (seleccionado["idCofrade"] == dato.id.toString()) {
                isCurrentlySelected = true
                break
            }
        }
        holder.cbSeleccion.isChecked = isCurrentlySelected
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}
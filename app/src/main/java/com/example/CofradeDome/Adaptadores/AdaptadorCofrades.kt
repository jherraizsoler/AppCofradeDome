package com.example.CofradeDome.Adaptadores

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.CofradeDome.Models.DatosCofrade
import com.example.CofradeDome.R

class AdaptadorCofrades(
    private val contexto: Context?,
    private val lista: ArrayList<DatosCofrade>,
    private val escuchador: View.OnClickListener?, //Fuente Datos
    private var fuenteDatos: Typeface?
) :
    RecyclerView.Adapter<AdaptadorCofrades.MiContenedor>(),
    View.OnClickListener {

    fun setFuenteDatos(fuenteDatos: Typeface?) {
        this.fuenteDatos = fuenteDatos
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class MiContenedor(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var tvID: TextView =
            itemview.findViewById(R.id.tvIdCofrade_Cofrade_Dato)
        var tvNombre: TextView =
            itemview.findViewById(R.id.tvNombreCofrade_Cofrade_Dato)
        var tvPrimerApellido: TextView =
            itemview.findViewById(R.id.tvPrimerApellido_Cofrade_Dato)
        var tvSegundoApellido: TextView =
            itemview.findViewById(R.id.tvSegundoApellido_Cofrade_Dato)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiContenedor {
        val inflater =
            contexto!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val vItem = inflater.inflate(R.layout.item_layout, parent, false)

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
            "OnBindViewHolderAdaptadorCofrades ",
            "El nombre de la fuente Datos es: " + fuenteDatos.toString()
        )

        holder.tvID.typeface = fuenteDatos
        holder.tvID.text = dato.id.toString()

        holder.tvNombre.typeface = fuenteDatos
        holder.tvNombre.text = dato.nombre

        holder.tvPrimerApellido.typeface = fuenteDatos
        holder.tvPrimerApellido.text = dato.primerApellido

        holder.tvSegundoApellido.typeface = fuenteDatos
        holder.tvSegundoApellido.text = dato.segundoApellido
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return lista.size
    }
}
package com.example.CofradeDome.Adaptadores

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.CofradeDome.Models.DatosPermisoCofrade
import com.example.CofradeDome.R


class AdaptadorPermisos(
    private val contexto: Context?,
    private val lista: ArrayList<DatosPermisoCofrade>,
    private val escuchador: View.OnClickListener?,
    private var fuenteDatos: Typeface?
) :
    RecyclerView.Adapter<AdaptadorPermisos.MiContenedor>(),
    View.OnClickListener {
    private val posicionEdicion = 0

    fun setFuenteDatos(fuenteDatos: Typeface?) {
        this.fuenteDatos = fuenteDatos
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class MiContenedor(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var tvID: TextView =
            itemview.findViewById(R.id.tvIDPermiso_Dato)
        var tvIdCofrade: TextView =
            itemview.findViewById(R.id.tvIdCofrade_Dato)
        var tvNivelPermiso: TextView =
            itemview.findViewById(R.id.tvNivelPermiso_Dato)
        var tvFecha: TextView =
            itemview.findViewById(R.id.tvFecha_Dato)
        var tvCorreoElectronico: TextView =
            itemview.findViewById(R.id.tvCorreo)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiContenedor {
        val inflater =
            contexto!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val vItem = inflater.inflate(R.layout.itempermiso_layout, parent, false)
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
        holder.tvID.typeface = fuenteDatos
        holder.tvID.text = dato.idCofradePermiso.toString() + ""
        holder.tvIdCofrade.typeface = fuenteDatos
        holder.tvIdCofrade.text = dato.idCofrade.toString() + ""
        holder.tvNivelPermiso.typeface = fuenteDatos
        holder.tvNivelPermiso.text = dato.nivelPermiso.toString() + ""
        holder.tvFecha.typeface = fuenteDatos
        holder.tvFecha.text = dato.fechaOtorgamiento.toString()
        holder.tvCorreoElectronico.typeface = fuenteDatos

        holder.tvCorreoElectronico.text =  getAjustarEmail(dato.correoElectronico.toString())
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return lista.size
    }

    fun getAjustarEmail(email: String): String {
        val regex = "^(.*)@(.*)$".toRegex()
        val matchResult = regex.matchEntire(email)

        return if (matchResult != null) {
            val username = matchResult.groupValues[1]
            val domain = matchResult.groupValues[2]
            "$username\n@$domain"
        } else {
            "Invalid email format"
        }
    }

}
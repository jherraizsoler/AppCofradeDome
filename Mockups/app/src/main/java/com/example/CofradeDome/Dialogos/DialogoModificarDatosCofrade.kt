package com.example.CofradeDome.Dialogos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.CofradeDome.Activities.MainActivity
import com.example.CofradeDome.R

class DialogoModificarDatosCofrade : DialogFragment() {
    private var tvID_Encabezado: TextView? = null
    private var tvNombre_Encabezado: TextView? = null
    private var tvPrimerApellido_Encabezado: TextView? = null
    private var tvSegundoApellido_Encabezado: TextView? = null
    private var etID: EditText? = null
    private var etNombre: EditText? = null
    private var etPrimerApellido: EditText? = null
    private var etSegundoApellido: EditText? = null
    private var listener: DatosCofrade? = null

    // Variables temporales para guardar los datos iniciales
    private var idInicial = -9
    private var nombreInicial: String? = null
    private var primerApellidoInicial: String? = null
    private var segundoApellidoInicial: String? = null
    private var activity: MainActivity? = null

    private var btn_Guardar: Button? = null
    private var btn_Cancelar: Button? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder

        if (activity != null) {
            val activity = activity;
            // Obtener el ContextThemeWrapper con el estilo del diálogo
            builder = AlertDialog.Builder(context)
            val inflador = activity!!.layoutInflater
            val vistaVentana = inflador.inflate(R.layout.dialogo_modificardatoscofrade, null)

            //Encabezados
            tvID_Encabezado = vistaVentana.findViewById(R.id.tvID_dialogo)
            tvNombre_Encabezado = vistaVentana.findViewById(R.id.tvNombre_dialogo)
            tvPrimerApellido_Encabezado = vistaVentana.findViewById(R.id.tvPrimerApellido_dialogo)
            tvSegundoApellido_Encabezado = vistaVentana.findViewById(R.id.tvSegundoApellido_dialogo)

            etID = vistaVentana.findViewById(R.id.etID_dialogo)
            etNombre = vistaVentana.findViewById(R.id.etpassword_dialogo)
            etPrimerApellido = vistaVentana.findViewById(R.id.etPrimerApellido_dialogo)
            etSegundoApellido = vistaVentana.findViewById(R.id.etSegundoApellido_dialogo)


            // Obtener la fuente Encabezados
            val fuenteEncabezados = activity.fuenteEncabezados

            tvID_Encabezado?.setTypeface(fuenteEncabezados)
            tvNombre_Encabezado?.setTypeface(fuenteEncabezados)
            tvPrimerApellido_Encabezado?.setTypeface(fuenteEncabezados)
            tvSegundoApellido_Encabezado?.setTypeface(fuenteEncabezados)

            // Obtener la fuente preferida
            val fuenteDatos = activity.fuenteDatos
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity.colorPrincipal!!))

            // Aplicar la fuente a los EditTexts
            etID?.setTypeface(fuenteDatos)
            etID?.setBackgroundTintList(colorStateList)
            etNombre?.setTypeface(fuenteDatos)
            etNombre?.setBackgroundTintList(colorStateList)
            etPrimerApellido?.setTypeface(fuenteDatos)
            etPrimerApellido?.setBackgroundTintList(colorStateList)
            etSegundoApellido?.setTypeface(fuenteDatos)
            etSegundoApellido?.setBackgroundTintList(colorStateList)

            // Establecer los valores iniciales si existen
            if (idInicial != -9) {
                etID?.setText(idInicial.toString())
                etNombre?.setText(nombreInicial)
                etPrimerApellido?.setText(primerApellidoInicial.toString())
                etSegundoApellido?.setText(segundoApellidoInicial.toString())
            }

            btn_Guardar = vistaVentana.findViewById(R.id.btn_ModificarCofrade_Guardar)
            btn_Guardar?.setOnClickListener(View.OnClickListener {
                val idCofrade = etID?.getText().toString().toInt()
                val nombreCofrade = etNombre?.getText().toString()
                val primerApellidoCofrade = etPrimerApellido?.getText().toString()
                val segundoApellidoCofrade = etSegundoApellido?.getText().toString()

                if (listener != null) {
                    listener!!.DatosC(
                        idCofrade,
                        nombreCofrade,
                        primerApellidoCofrade,
                        segundoApellidoCofrade
                    )
                }
                Toast.makeText(getActivity(), "Guardado", Toast.LENGTH_SHORT).show()
                dismiss() // Cierra el diálogo después de guardar
            })

            //botones
            // -- Boton Alta
            btn_Guardar?.setBackgroundTintList(colorStateList)
            btn_Guardar?.setTypeface(activity.fuenteBotones)


            btn_Cancelar = vistaVentana.findViewById(R.id.btn_ModificarCofrade_Cancelar)
            btn_Cancelar?.setOnClickListener(View.OnClickListener {
                Toast.makeText(getActivity(), "Cancelado", Toast.LENGTH_SHORT).show()
                dismiss() // Cierra el diálogo después de guardar
            })

            btn_Cancelar?.setBackgroundTintList(colorStateList)
            btn_Cancelar?.setTypeface(activity.fuenteBotones)


            builder.setTitle("Modificar datos Cofrade")
            builder.setView(vistaVentana)

            return builder.create()
        } else {
            // Inicializar builder con un contexto por defecto si activity es null
            builder = AlertDialog.Builder(getActivity())
        }
        return builder.create()
    }

    // Método modificado para guardar los datos temporalmente
    fun setDatos(
        id: Int,
        nombre: String?,
        primerApellidoInicial: String?,
        segundoApellidoInicial: String?
    ) {
        this.idInicial = id
        this.nombreInicial = nombre
        this.primerApellidoInicial = primerApellidoInicial
        this.segundoApellidoInicial = segundoApellidoInicial
    }

    // Método setter para asignar el listener
    fun setDialogoPersonalizadoListener(listener: DatosCofrade?) {
        this.listener = listener
    }


    // Interfaz para comunicar datos a la actividad principal
    interface DatosCofrade {
        fun DatosC(id: Int, nombre: String, primerApellido: String, segundoApellido: String)
    }

    fun setActivity(activity: MainActivity?) {
        this.activity = activity
    }

    private fun obtenerColorPrincipalDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(context, R.color.Color_Principal_Azul)
            "Verde" -> ContextCompat.getColor(context, R.color.Color_Principal_Verde)
            "Rojo" -> ContextCompat.getColor(context, R.color.Color_Principal_Rojo)
            "Naranja" -> ContextCompat.getColor(context, R.color.Color_Principal_Naranja)
            "Morado" -> ContextCompat.getColor(context, R.color.Color_Principal_Morado)
            else -> ContextCompat.getColor(context, R.color.Color_Principal_Morado)
        }
    }
}

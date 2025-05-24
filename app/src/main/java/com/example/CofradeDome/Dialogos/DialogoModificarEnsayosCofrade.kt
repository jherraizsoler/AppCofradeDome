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

class DialogoModificarEnsayosCofrade : DialogFragment() {
    private var tvEnsayosNormalesMinimos_Encabezado: TextView? = null
    private var tvEnsayosNormalesMaximos_Encabezado: TextView? = null
    private var etEnsayoNormalesMinimos_dato: EditText? = null
    private var etEnsayosNormalesMaximos_dato: EditText? = null
    private var listener: EnsayosCofrades? = null

    // Variables temporales para guardar los datos iniciales
    private var ensayosMinimosInicial = -9
    private var ensayosMaximosInicial = 0
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
            val vistaVentana = inflador.inflate(R.layout.dialogo_modificarensayoscofrade, null)

            //Encabezados
            tvEnsayosNormalesMinimos_Encabezado =
                vistaVentana.findViewById(R.id.tvEnsayosNormalesMinimos_dialogo)
            tvEnsayosNormalesMaximos_Encabezado =
                vistaVentana.findViewById(R.id.tvEnsayosNormalesMaximos_dialogo)


            etEnsayoNormalesMinimos_dato =
                vistaVentana.findViewById(R.id.etEnsayoNormalesMinimos_dialogo)
            etEnsayosNormalesMaximos_dato =
                vistaVentana.findViewById(R.id.etEnsayosNormalesMaximos_dialogo)


            // Obtener la fuente Encabezados
            val fuenteEncabezados = activity.fuenteEncabezados

            tvEnsayosNormalesMinimos_Encabezado?.setTypeface(fuenteEncabezados)
            tvEnsayosNormalesMaximos_Encabezado?.setTypeface(fuenteEncabezados)

            // Obtener la fuente preferida
            val fuenteDatos = activity.fuenteDatos
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity.colorPrincipal!!))

            // Aplicar la fuente a los EditTexts
            etEnsayoNormalesMinimos_dato?.setTypeface(fuenteDatos)
            etEnsayoNormalesMinimos_dato?.setBackgroundTintList(colorStateList)
            etEnsayosNormalesMaximos_dato?.setTypeface(fuenteDatos)
            etEnsayosNormalesMaximos_dato?.setBackgroundTintList(colorStateList)

            // Establecer los valores iniciales si existen
            if (ensayosMinimosInicial != -9) {
                etEnsayoNormalesMinimos_dato?.setText(ensayosMinimosInicial.toString())
                etEnsayosNormalesMaximos_dato?.setText(ensayosMaximosInicial.toString())
            }

            btn_Guardar = vistaVentana.findViewById(R.id.btn_Ensayos_Guardar)
            btn_Guardar?.setOnClickListener(View.OnClickListener {
                val numEnsayosNormalesMinimos =
                    etEnsayoNormalesMinimos_dato?.getText().toString().toInt()
                val numEnsayosNormalesMaximos =
                    etEnsayosNormalesMaximos_dato?.getText().toString().toInt()

                if (listener != null) {
                    listener!!.EnsayosC(numEnsayosNormalesMinimos, numEnsayosNormalesMaximos)
                }
                Toast.makeText(getActivity(), "Guardado", Toast.LENGTH_SHORT).show()
                dismiss() // Cierra el diálogo después de guardar
            })

            //botones
            // -- Boton Alta
            btn_Guardar?.setBackgroundTintList(colorStateList)
            btn_Guardar?.setTypeface(activity.fuenteBotones)


            btn_Cancelar = vistaVentana.findViewById(R.id.btn_Ensayos_Cancelar)
            btn_Cancelar?.setOnClickListener(View.OnClickListener {
                Toast.makeText(getActivity(), "Cancelado", Toast.LENGTH_SHORT).show()
                dismiss() // Cierra el diálogo después de guardar
            })

            btn_Cancelar?.setBackgroundTintList(colorStateList)
            btn_Cancelar?.setTypeface(activity.fuenteBotones)


            builder.setTitle("Modificar ensayos Cofrade")
            builder.setView(vistaVentana)

            return builder.create()
        } else {
            // Inicializar builder con un contexto por defecto si activity es null
            builder = AlertDialog.Builder(getActivity())
        }
        return builder.create()
    }

    // Método modificado para guardar los datos temporalmente
    fun setDatos(numEnsayosNormalesMinimos: Int, numEnsayosNormalesMaximos: Int) {
        this.ensayosMinimosInicial = numEnsayosNormalesMinimos
        this.ensayosMaximosInicial = numEnsayosNormalesMaximos
    }

    // Método setter para asignar el listener
    fun setDialogoPersonalizadoListener(listener: EnsayosCofrades?) {
        this.listener = listener
    }


    // Interfaz para comunicar datos a la actividad principal
    interface EnsayosCofrades {
        fun EnsayosC(numEnsayosMinimos: Int, numEnsayosMaximos: Int)
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

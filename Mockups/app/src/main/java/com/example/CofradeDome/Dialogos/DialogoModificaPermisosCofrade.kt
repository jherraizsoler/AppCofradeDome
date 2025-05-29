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

class DialogoModificaPermisosCofrade : DialogFragment() {
    private var etIDcofrade: EditText? = null
    private var etNivelPermiso: EditText? = null
    private var etCorreoElectronico: EditText? = null
    private var tv_dialogoMofificarPermisosCofrades: TextView? = null
    private var listener: PermisosCofrades? = null

    // Variables temporales para guardar los datos iniciales
    private var idInicial = -9

    private var idCofradeInicial = 0

    private var nivelPermisoInicial = 0

    private var correoElectronicoInicial: String? = null
    private var activity: MainActivity? = null

    private var btn_Guardar: Button? = null
    private var btn_Cancelar: Button? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder

        if (activity != null) {
            // Obtener el ContextThemeWrapper con el estilo del diálogo
            builder = AlertDialog.Builder(context)
            val inflador = requireActivity().layoutInflater
            val vistaVentana = inflador.inflate(R.layout.dialogo_modificarpermisoscofrade, null)

            val activity = activity;
            // Obtener la fuente preferida
            val fuenteDatos = activity!!.fuenteDatos
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity.colorPrincipal!!))

            etIDcofrade = vistaVentana.findViewById(R.id.etIDcofrade_dialogo_permiso)
            etNivelPermiso = vistaVentana.findViewById(R.id.etNivelPermiso_dialogo_permiso)
            etCorreoElectronico = vistaVentana.findViewById(R.id.etCorreo_dialogo_permiso)

            etIDcofrade?.setTypeface(fuenteDatos)
            etNivelPermiso?.setTypeface(fuenteDatos)
            etCorreoElectronico?.setTypeface(fuenteDatos)

            etIDcofrade?.setBackgroundTintList(colorStateList)
            etNivelPermiso?.setBackgroundTintList(colorStateList)
            etCorreoElectronico!!.setBackgroundTintList(colorStateList)

            tv_dialogoMofificarPermisosCofrades =
                vistaVentana.findViewById(R.id.tv_dialogoModificarPermisosCofrades)
            // Establecer los valores iniciales si existen
            if (idInicial != -9) {
                etIDcofrade?.setText(idCofradeInicial.toString() + "")
                etIDcofrade?.isEnabled = false;
                etNivelPermiso?.setText(nivelPermisoInicial.toString() + "")
                etCorreoElectronico?.setText(correoElectronicoInicial)
            }

            btn_Guardar = vistaVentana.findViewById(R.id.btn_ModificarCofradePermiso_Guardar)
            btn_Guardar!!.setOnClickListener(View.OnClickListener {
                try {
                    val idCofrade = etIDcofrade?.getText().toString().toInt()
                    val nivelPermiso = etNivelPermiso?.getText().toString().toInt()
                    val correoElectronicoCofrade = etCorreoElectronico?.getText().toString()

                    if (listener != null) {
                        listener!!.permisosC(
                            idInicial,
                            idCofrade,
                            nivelPermiso,
                            correoElectronicoCofrade
                        )
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        getActivity(),
                        "Error: Por favor, ingrese valores numéricos válidos para ID y Nivel.",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace() // Log the exception for debugging
                }
                dismiss() // Cierra el diálogo después de guardar
            })
            //botones
            // -- Boton Alta
            btn_Guardar?.setBackgroundTintList(colorStateList)
            btn_Guardar?.setTypeface(activity.fuenteBotones)

            btn_Cancelar = vistaVentana.findViewById(R.id.btn_ModificarCofradePermiso_Cancelar)
            btn_Cancelar?.setOnClickListener(View.OnClickListener {
                Toast.makeText(getActivity(), "Cancelado", Toast.LENGTH_SHORT).show()
                dismiss() // Cierra el diálogo después de guardar
            })

            btn_Cancelar?.setBackgroundTintList(colorStateList)
            btn_Cancelar?.setTypeface(activity.fuenteBotones)

            builder.setTitle("Modificar permisos Cofrade")
            builder.setView(vistaVentana)
        } else {
            // Inicializar builder con un contexto por defecto si activity es null
            builder = AlertDialog.Builder(getActivity())
        }
        return builder.create()
    }

    // Método modificado para guardar los datos temporalmente
    fun setDatos(
        idCofradePermiso: Int,
        idCofrade: Int,
        nivelPermiso: Int,
        correoElectronico: String?
    ) {
        this.idInicial = idCofradePermiso
        this.idCofradeInicial = idCofrade
        this.nivelPermisoInicial = nivelPermiso
        this.correoElectronicoInicial = correoElectronico
    }

    // Método setter para asignar el listener
    fun setDialogoModificarPermisosCofradeListener(listener: PermisosCofrades?) {
        this.listener = listener
    }


    // Interfaz para comunicar datos a la actividad principal
    interface PermisosCofrades {
        fun permisosC(
            idCofradePermiso: Int,
            idCofrade: Int,
            nivelPermiso: Int,
            correoElectronico: String
        )
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

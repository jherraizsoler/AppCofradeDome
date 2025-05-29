package com.example.CofradeDome.Activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.CofradeDome.R
import java.util.Locale

class AltaCofradesActivity : AppCompatActivity() {
    private var etNombre: EditText? = null
    private var etPrimerApellido: EditText? = null
    private var etSegundoApellido: EditText? = null
    private var btnAlta: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_altacofrades)

        etNombre = findViewById(R.id.etIdCofrade_AltaPermisosCofrades)
        etPrimerApellido = findViewById(R.id.etNivelPermiso)
        etSegundoApellido = findViewById(R.id.etCorreoElectronico)
        btnAlta = findViewById(R.id.btnAlta)

        btnAlta?.isEnabled = true

        btnAlta?.setOnClickListener(View.OnClickListener { v: View? ->
            val nombre = etNombre?.text?.toString() ?: ""
            val pApellido = etPrimerApellido?.text?.toString() ?: ""
            val sApellido = etSegundoApellido?.text?.toString() ?: ""
            btnAlta?.isEnabled = false
            // Devuelve los datos a la actividad principal
            val resultado = Intent()
            resultado.putExtra("NOMBRE", nombre)
            resultado.putExtra("PRIMER_APELLIDO", pApellido)
            resultado.putExtra("SEGUNDO_APELLIDO", sApellido)
            setResult(RESULT_OK, resultado)
            finish()
        })
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Aplicar color principal
        val colorPrincipalPref = prefs.getString("colorPrincipal", "Morado")!!
        val rootView =
            findViewById<View>(android.R.id.content) // Obtiene la vista raíz de la Activity
        if (colorPrincipalPref.length > 0 && rootView != null) {
            // Obtener el color real (puedes usar un mapa o switch)
            val colorPrincipal = obtenerColorPrincipalDesdeNombre(colorPrincipalPref)

            // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)

            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))

            etNombre!!.backgroundTintList = colorStateList
            etPrimerApellido!!.backgroundTintList = colorStateList
            etSegundoApellido!!.backgroundTintList = colorStateList

            //Boton Login
            btnAlta!!.setBackgroundColor(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
        } else {
            Toast.makeText(
                this,
                "Error: Fallo al cargar las preferencias en los colores.",
                Toast.LENGTH_SHORT
            ).show()
        }


        // Aplicar fuente de los Encabezados
        val fuenteEncabezadosPref = prefs.getString("fuenteEncabezados", "sans-serif")!!
        if (fuenteEncabezadosPref.length > 0) {
            val fuenteEncabezados = obtenerFuenteDesdeNombre(fuenteEncabezadosPref)
            // Aplicar la fuente a los textview
            etNombre!!.typeface = fuenteEncabezados
            etPrimerApellido!!.typeface = fuenteEncabezados
            etSegundoApellido!!.typeface = fuenteEncabezados
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente encabezados de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Botonoes
        val fuenteBotonesPref = prefs.getString("fuenteBotones", "sans-serif")!!
        if (fuenteBotonesPref.length > 0) {
            val fuenteBotones = obtenerFuenteDesdeNombre(fuenteBotonesPref)
            // Aplicar la fuente al boton
            btnAlta!!.typeface = fuenteBotones
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente del boton de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun obtenerColorPrincipalDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(this, R.color.Color_Principal_Azul)
            "Verde" -> ContextCompat.getColor(this, R.color.Color_Principal_Verde)
            "Rojo" -> ContextCompat.getColor(this, R.color.Color_Principal_Rojo)
            "Naranja" -> ContextCompat.getColor(this, R.color.Color_Principal_Naranja)
            "Morado" -> ContextCompat.getColor(this, R.color.Color_Principal_Morado)
            else -> ContextCompat.getColor(this, R.color.Color_Principal_Morado)
        }
    }

    private fun obtenerColorFondoDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(this, R.color.Color_fondo_Azul)
            "Verde" -> ContextCompat.getColor(this, R.color.Color_fondo_Verde)
            "Rojo" -> ContextCompat.getColor(this, R.color.Color_fondo_Rojo)
            "Naranja" -> ContextCompat.getColor(this, R.color.Color_fondo_Naranja)
            "Morado" -> ContextCompat.getColor(this, R.color.Color_fondo_Morado)
            else -> ContextCompat.getColor(this, R.color.Color_fondo_Morado)
        }
    }

    private fun obtenerFuenteDesdeNombre(nombreFuente: String?): Typeface? {
        if (nombreFuente == null || nombreFuente.equals("sans-serif", ignoreCase = true)) {
            return Typeface.DEFAULT
        } else if (nombreFuente.equals("serif", ignoreCase = true)) {
            return Typeface.SERIF
        } else if (nombreFuente.equals("monospace", ignoreCase = true)) {
            return Typeface.MONOSPACE
        } else if (nombreFuente.lowercase(Locale.getDefault()).endsWith(".ttf")) {
            try {
                return Typeface.createFromAsset(assets, "fonts/$nombreFuente")
            } catch (e: Exception) {
                Log.e(
                    "PreferenciasActivity",
                    "Fuente .ttf no encontrada: $nombreFuente"
                )
                return Typeface.DEFAULT
            }
        } else if (nombreFuente.lowercase(Locale.getDefault()).endsWith(".xml")) {
            try {
                val fontResourceId = resources.getIdentifier(
                    nombreFuente.substring(0, nombreFuente.lastIndexOf(".")), "font",
                    packageName
                )
                if (fontResourceId != 0) {
                    return ResourcesCompat.getFont(this, fontResourceId)
                } else {
                    Log.e(
                        "PreferenciasActivity",
                        "Recurso de fuente .xml no encontrado: $nombreFuente"
                    )
                    return Typeface.DEFAULT
                }
            } catch (e: Exception) {
                Log.e(
                    "PreferenciasActivity",
                    "Error al cargar familia de fuentes .xml: $nombreFuente", e
                )
                return Typeface.DEFAULT
            }
        } else {
            Log.w(
                "PreferenciasActivity",
                "Formato de archivo de fuente desconocido: $nombreFuente"
            )
            return Typeface.DEFAULT
        }
    }
}
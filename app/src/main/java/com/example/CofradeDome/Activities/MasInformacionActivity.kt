package com.example.CofradeDome.Activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.CofradeDome.Dialogos.crearDialogoConfirmacion
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.R

class MasInformacionActivity : AppCompatActivity() {

    private var btn_Contacto:Button? = null;
    private var btn_LeerMas:Button? = null;
    private var btn_Cancelar:Button? = null;
    private var logo: ImageView? = null
    private lateinit var dialogoHelper: crearDialogoConfirmacion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.masinformacion_layout)
        dialogoHelper = crearDialogoConfirmacion()

        logo = findViewById(R.id.logo_MasInformacion)


        btn_Contacto = findViewById(R.id.btn_Contacto_MasInformacion)
        btn_Contacto?.setOnClickListener {
            dialogoHelper.mostrarDialogoOk(
                this@MasInformacionActivity,
                "Información Contacto",
                "Formas de contacto",
                "Correo electronico: \n estudiantejorgeherraiz@gmail.com", // El parámetro 'Mensaje' en tu función
                "Salir",
                onConfirmar = {

                }
            )
        }
        btn_LeerMas = findViewById(R.id.btn_LeerMas_MasInformacion)

        btn_LeerMas?.setOnClickListener {
            val ventana = Intent(
                this@MasInformacionActivity,
                LeerMas_MasInformacionActivity::class.java
            )
            startActivity(ventana)
        }
        btn_Cancelar = findViewById(R.id.btn_Cancelar_MasInformacion)
        btn_Cancelar?.setOnClickListener{
            this.finish()
        }


    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Aplicar color principal
        val colorPrincipalPref = prefs.getString("colorPrincipal", "Morado")!!
        val rootView =
            findViewById<View>(android.R.id.content) // Obtiene la vista raíz de la Activity
        if (colorPrincipalPref.length > 0 && rootView != null) {
            val colorPrincipal = colorPrincipalPref
            obtenerImagenLogoDesdeNombre(colorPrincipalPref)

           // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)

            btn_Contacto!!.setBackgroundColor(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
            btn_LeerMas!!.setBackgroundColor(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))

        } else {
            Toast.makeText(
                this,
                "Fallo al cargar las preferencias en los colores.",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Aplicar fuente de los Encabezados
        val fuenteEncabezadosPref = prefs.getString("fuenteEncabezados", "sans-serif")!!
        if (fuenteEncabezadosPref.length > 0) {
            val fuenteEncabezados = obtenerFuenteDesdeNombre(fuenteEncabezadosPref)
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente encabezados de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Feedback
        val fuenteFeedbackPref = prefs.getString("fuenteFeedback", "sans-serif")!!
        if (fuenteFeedbackPref.length > 0) {
            val fuenteFeedback = obtenerFuenteDesdeNombre(fuenteFeedbackPref)
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente del comentario de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Botonoes
        val fuenteBotonesPref = prefs.getString("fuenteBotones", "sans-serif")!!
        if (fuenteBotonesPref.length > 0) {
            val fuenteBotones = obtenerFuenteDesdeNombre(fuenteBotonesPref)
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

    private fun obtenerImagenLogoDesdeNombre(nombreColor: String) {
        when (nombreColor) {
            "Azul" -> logo!!.setImageResource(R.mipmap.ic_logo_azul)
            "Verde" -> logo!!.setImageResource(R.mipmap.ic_logo_verde)
            "Rojo" -> logo!!.setImageResource(R.mipmap.ic_logo_rojo)
            "Naranja" -> logo!!.setImageResource(R.mipmap.ic_logo_naranja)
            "Morado" -> logo!!.setImageResource(R.mipmap.ic_logo_morado)
            else -> logo!!.setImageResource(R.mipmap.ic_logo_morado)
        }
    }

    private fun obtenerFuenteDesdeNombre(nombreFuente: String?): Typeface {
        if (nombreFuente == null || nombreFuente.equals("sans-serif", ignoreCase = true)) {
            return Typeface.DEFAULT
        } else if (nombreFuente.equals("serif", ignoreCase = true)) {
            return Typeface.SERIF
        } else if (nombreFuente.equals("monospace", ignoreCase = true)) {
            return Typeface.MONOSPACE
        } else {
            try {
                val fontResourceId = resources.getIdentifier(
                    nombreFuente, "font",
                    packageName
                )
                if (fontResourceId != 0) {
                    val typeface = ResourcesCompat.getFont(this, fontResourceId)
                    if (typeface != null) {
                        return typeface
                    } else {
                        Log.e(
                            "LoginActivity",
                            "Error al cargar fuente (ResourcesCompat): $nombreFuente"
                        )
                        return Typeface.DEFAULT
                    }
                } else {
                    Log.e(
                        "LoginActivity",
                        "Recurso de fuente no encontrado: $nombreFuente"
                    )
                    return Typeface.DEFAULT
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error al cargar fuente: $nombreFuente", e)
                return Typeface.DEFAULT
            }
        }
    }
}
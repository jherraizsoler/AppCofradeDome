package com.example.CofradeDome.Activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.CofradeDome.R

class LeerMas_MasInformacionActivity : AppCompatActivity() {

    private var btn_Cancelar: Button? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leer_mas_mas_informacion)

        btn_Cancelar = findViewById(R.id.btn_Cancelar_ActivityLeerMas)
        btn_Cancelar?.setOnClickListener{
            this.finish()
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

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Aplicar color principal
        val colorPrincipalPref = prefs.getString("colorPrincipal", "Morado")!!
        val rootView =
            findViewById<View>(android.R.id.content) // Obtiene la vista raíz de la Activity
        if (colorPrincipalPref.length > 0 && rootView != null) {

            // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)


        } else {
            Toast.makeText(
                this,
                "Error: Fallo al cargar las preferencias en los colores.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
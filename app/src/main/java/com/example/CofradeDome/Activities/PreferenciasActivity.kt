package com.example.CofradeDome.Activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.CofradeDome.R

class PreferenciasActivity : AppCompatActivity() {
    private var spColoresPrincipal: Spinner? = null
    private var spFuentesEncabezados: Spinner? = null
    private var spFuentesDatos: Spinner? = null
    private var spFuentesFeedback: Spinner? = null
    private var spFuentesBotones: Spinner? = null
    private var btn_Guardar: Button? = null
    private var btn_Cancelar: Button? = null
    private var miImageView: ImageView? = null

    //Textviews Titulos
    private var tv_Colores: TextView? = null
    private var tv_Fuentes: TextView? = null

    //TextViews encabezados
    private var tv_ColorPrincipal: TextView? = null
    private var tv_FuentesEncabezados: TextView? = null
    private var tv_FuentesDatos: TextView? = null
    private var tv_FuentesFeedback: TextView? = null
    private var tv_FuentesBotones: TextView? = null


    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var ultimaPestaña = 0
    private var permisosUsuario = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        editor = sharedPreferences?.edit()

        // Inicializar Spinners de Colores
        spColoresPrincipal = findViewById(R.id.sp_colores_principal)

        // Inicializar Spinners de Fuentes
        spFuentesEncabezados = findViewById(R.id.sp_fuentes_encabezados)
        spFuentesDatos = findViewById(R.id.sp_fuentes_datos)
        spFuentesFeedback = findViewById(R.id.sp_fuentes_feedback)
        spFuentesBotones = findViewById(R.id.sp_fuentes_botones)

        // Recuperar el índice de la última pestaña visitada
        val intent = intent
        ultimaPestaña = intent.getIntExtra(
            "ultima_pestaña",
            0
        ) // 0 es el valor predeterminado si no se pasa el extra
        permisosUsuario = intent.getIntExtra(
            "permisos",
            -1
        ) // -1 es el valor predeterminado si no se pasa el extra


        // Inicializar imagen logo
        miImageView = findViewById(R.id.imagenLogo)

        // Inicializar textview titulos apartados
        tv_Colores = findViewById(R.id.tv_colores_titulo)
        tv_Fuentes = findViewById(R.id.tv_fuentes_titulo)

        //Inicializar textViews Encabezados
        tv_ColorPrincipal = findViewById(R.id.tv_colores_principal)
        tv_FuentesEncabezados = findViewById(R.id.tv_fuente_encabezados)
        tv_FuentesDatos = findViewById(R.id.tv_fuente_datos)
        tv_FuentesFeedback = findViewById(R.id.tv_fuente_feedback)
        tv_FuentesBotones = findViewById(R.id.tv_fuente_botones)

        // Configurar Adaptadores para los Spinners de Colores
        val colorAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, obtenerListaColores())
        spColoresPrincipal?.setAdapter(colorAdapter)

        // Configurar Adaptadores para los Spinners de Fuentes
        val fuenteAdapterEncabezados = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            obtenerListaFuentesEncabezados()
        )
        spFuentesEncabezados?.setAdapter(fuenteAdapterEncabezados)
        val fuenteAdapterDatos = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            obtenerListaFuentesDatos()
        )
        spFuentesDatos?.setAdapter(fuenteAdapterDatos)
        val fuenteAdapterFeedback = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            obtenerListaFuentesFeedback()
        )
        spFuentesFeedback?.setAdapter(fuenteAdapterFeedback)
        val fuenteAdapterBotones = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            obtenerListaFuentesBotones()
        )
        spFuentesBotones?.setAdapter(fuenteAdapterBotones)

        // Cargar las preferencias guardadas (si existen)
        cargarPreferencias()

        // Listeners para los Spinners (guardar la selección temporalmente)
        spColoresPrincipal?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                editor?.putString("colorPrincipal", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {} // No hacer nada
        })

        /** */
        spFuentesEncabezados?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                editor?.putString("fuenteEncabezados", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {} // No hacer nada
        })

        spFuentesDatos?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                editor?.putString("fuenteDatos", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {} // No hacer nada
        })

        spFuentesFeedback?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                editor?.putString("fuenteFeedback", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {} // No hacer nada
        })

        spFuentesBotones?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                editor?.putString("fuenteBotones", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {} // No hacer nada
        })
        /** Morado */
        // Listener para el botón Guardar Morado
        btn_Guardar = findViewById(R.id.btn_preferencias_Morado_Guardar)
        btn_Guardar?.setOnClickListener(View.OnClickListener { v: View? ->
            funcionamientoBotonGuardar()
        })
        // Listener para el botón Cancelar Morado
        btn_Cancelar = findViewById(R.id.btn_preferencias_Morado_cancelar)
        btn_Cancelar?.setOnClickListener(View.OnClickListener { v: View? ->
            funcionamientoBotonCancelar()
        })
    }

    // Método para obtener la lista de colores
    private fun obtenerListaColores(): List<String> {
        val colores: MutableList<String> = ArrayList()
        colores.add("Morado") //Morado
        colores.add("Azul")
        colores.add("Verde")
        colores.add("Rojo")
        colores.add("Naranja")
        // Añade más colores según necesites
        return colores
    }

    // Método para obtener la lista de fuentes para los encabezados
    private fun obtenerListaFuentesEncabezados(): List<String> {
        val fuentes: MutableList<String> = ArrayList()
        fuentes.add("allerta_stencil")
        fuentes.add("oxygen_mono")
        fuentes.add("sans-serif") // Predeterminada
        fuentes.add("abril_fatface")
        fuentes.add("aclonica")
        fuentes.add("adlam_display")
        fuentes.add("agbalumo")

        return fuentes
    }

    // Método para obtener la lista de fuentes para los Datos
    private fun obtenerListaFuentesDatos(): List<String> {
        val fuentes: MutableList<String> = ArrayList()

        fuentes.add("akaya_telivigala")
        fuentes.add("allerta_stencil")
        fuentes.add("oxygen_mono")
        fuentes.add("sans-serif")
        fuentes.add("serif")
        fuentes.add("monospace")

        return fuentes
    }

    // Método para obtener la lista de fuentes para los Feedback
    private fun obtenerListaFuentesFeedback(): List<String> {
        val fuentes: MutableList<String> = ArrayList()
        fuentes.add("sans-serif")
        fuentes.add("akaya_telivigala")
        fuentes.add("allerta_stencil")
        fuentes.add("oxygen_mono")
        fuentes.add("serif")
        fuentes.add("monospace")

        return fuentes
    }

    // Método para obtener la lista de fuentes para los Botones
    private fun obtenerListaFuentesBotones(): List<String> {
        val fuentes: MutableList<String> = ArrayList()

        fuentes.add("allerta_stencil")
        fuentes.add("sans-serif")
        fuentes.add("oxygen_mono")
        fuentes.add("adlam_display")
        fuentes.add("aclonica")
        fuentes.add("fraunces_black_italic")

        return fuentes
    }

    // Método para cargar las preferencias guardadas y seleccionar los Spinners
    private fun cargarPreferencias() {
        // Colores
        val colorPrincipalGuardado =
            sharedPreferences!!.getString("colorPrincipal", "Morado")!!

        seleccionarSpinner(spColoresPrincipal!!, colorPrincipalGuardado)

        // Fuentes
        val fuenteEncabezadosGuardado =
            sharedPreferences!!.getString("fuenteEncabezados", "sans-serif")!!
        val fuenteDatosGuardada = sharedPreferences!!.getString("fuenteDatos", "monospace")!!
        val fuenteFeedbackGuardada =
            sharedPreferences!!.getString("fuenteFeedback", "sans-serif")!!
        val fuenteBotonesGuardada = sharedPreferences!!.getString("fuenteBotones", "sans-serif")!!

        seleccionarSpinner(spFuentesEncabezados!!, fuenteEncabezadosGuardado)
        seleccionarSpinner(spFuentesDatos!!, fuenteDatosGuardada)
        seleccionarSpinner(spFuentesFeedback!!, fuenteFeedbackGuardada)
        seleccionarSpinner(spFuentesBotones!!, fuenteBotonesGuardada)
    }

    // Método auxiliar para seleccionar un valor en un Spinner
    private fun seleccionarSpinner(spinner: Spinner, valor: String) {
        val adapter = spinner.adapter as ArrayAdapter<String>
        if (adapter != null) {
            val position = adapter.getPosition(valor)
            if (position != -1) {
                spinner.setSelection(position)
            }
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
            // Obtener el color real (puedes usar un mapa o switch)
            val colorPrincipal = obtenerColorPrincipalDesdeNombre(colorPrincipalPref)

            // Para cambiar la imagen a otra de mipmap (usando el ID del recurso):
            obtenerImagenLogoDesdeNombre(colorPrincipalPref)

            // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)

            //Cambiar color backgroung toolbar
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
            tv_Colores!!.backgroundTintList = colorStateList
            tv_Fuentes!!.backgroundTintList = colorStateList

            btn_Guardar!!.backgroundTintList = colorStateList
            btn_Cancelar!!.backgroundTintList = colorStateList

            //Cambiar fuentes a los encabezados
            val fuenteEncabezadosPref = prefs.getString("fuenteEncabezados", "sans-serif")!!
            if (fuenteEncabezadosPref.length > 0) {
                val fuenteEncabezados = obtenerFuenteDesdeNombre(fuenteEncabezadosPref)
                // Aplicar la fuente a los textview
                tv_ColorPrincipal!!.typeface = fuenteEncabezados
                tv_FuentesEncabezados!!.typeface = fuenteEncabezados
                tv_FuentesDatos!!.typeface = fuenteEncabezados
                tv_FuentesFeedback!!.typeface = fuenteEncabezados
                tv_FuentesBotones!!.typeface = fuenteEncabezados
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
                // Aplicar la fuente a los botones Guardar
                btn_Guardar!!.typeface = fuenteBotones
                // Aplicar la fuente a los botones  Cancelar
                btn_Cancelar!!.typeface = fuenteBotones
            } else {
                Toast.makeText(
                    this,
                    "Error al cargar la fuente del boton de las preferencias",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Fallo al cargar las preferencias en los colores.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun funcionamientoBotonGuardar() {
        // Aplicar las preferencias guardadas

        editor!!.apply()
        Toast.makeText(this, "Preferencias actualizadas con éxito", Toast.LENGTH_SHORT).show()
        val result = Intent()
        setResult(RESULT_OK, result)

        // Opcional: Recargar la actividad principal
        val mainIntent = Intent(
            this,
            MainActivity::class.java
        )
        // Añade la bandera FLAG_ACTIVITY_CLEAR_TOP para cerrar cualquier instancia existente de MainActivity
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        mainIntent.putExtra("ultimaPestaña", ultimaPestaña)
        mainIntent.putExtra("permisos", permisosUsuario)
        startActivity(mainIntent)
        finish() // Finaliza la PreferenciasActivity
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

    private fun funcionamientoBotonCancelar() {
        Toast.makeText(this, "Cancelar", Toast.LENGTH_SHORT).show()
        finish()
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

    private fun obtenerColorFondoBotonesDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(this, R.color.Botones_Background_Azul)
            "Verde" -> ContextCompat.getColor(this, R.color.Botones_Background_Verde)
            "Rojo" -> ContextCompat.getColor(this, R.color.Botones_Background_Rojo)
            "Naranja" -> ContextCompat.getColor(
                this,
                R.color.Botones_Background_Naranja
            )

            "Morado" -> ContextCompat.getColor(
                this,
                R.color.Botones_Background_Morado
            )

            else -> ContextCompat.getColor(this, R.color.Botones_Background_Morado)
        }
    }

    private fun obtenerImagenLogoDesdeNombre(nombreColor: String) {
        when (nombreColor) {
            "Azul" -> miImageView!!.setImageResource(R.mipmap.ic_logo_azul)
            "Verde" -> miImageView!!.setImageResource(R.mipmap.ic_logo_verde)
            "Rojo" -> miImageView!!.setImageResource(R.mipmap.ic_logo_rojo)
            "Naranja" -> miImageView!!.setImageResource(R.mipmap.ic_logo_naranja)
            "Morado" -> miImageView!!.setImageResource(R.mipmap.ic_logo_morado)
            else -> miImageView!!.setImageResource(R.mipmap.ic_logo_morado)
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
}
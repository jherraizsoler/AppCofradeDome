package com.example.CofradeDome.Activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.R
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.Executor

class CrearPasswordActivity : AppCompatActivity() {

    private lateinit var clienteSSL: ClienteSSL
    private var mainExecutor: Executor? = null
    private val CambiarPasswordExecutor: MiExecutorTask? = null
    private var idCofrade: Int = 0
    private var etContraseña: EditText? = null
    private var etContraseñaRepetir: EditText? = null
    private var tvContraseña: TextView? = null

    private var permisoUsuario = 0
    private var correoElectronico: String? = ""

    private var btn_Guardar: Button? = null
    private var contraseñaDefault: String = "Cofrade25.$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_password)
        mainExecutor = ContextCompat.getMainExecutor(this@CrearPasswordActivity)
        clienteSSL = ClienteSSL()

        idCofrade = intent.getIntExtra("IDCOFRADEPASALISTA", 0)
        correoElectronico = intent.getStringExtra("EMAIL").toString()
        permisoUsuario = intent.getIntExtra("PERMISO", 0)

        etContraseña = findViewById(R.id.editTextTextPassword2)
        etContraseñaRepetir = findViewById(R.id.editTextTextPassword3)
        tvContraseña = findViewById(R.id.tvContraseña)

        correoElectronico = intent.getStringExtra("EMAIL")
        permisoUsuario = intent.getIntExtra("PERMISO", 0)

        btn_Guardar = findViewById(R.id.btn_GuardarCrearContraseña)
        btn_Guardar?.setOnClickListener(View.OnClickListener { v: View? ->
            val contraseñaNueva = etContraseña?.getText().toString()
            val contraseñaNuevaRepetir = etContraseñaRepetir?.getText().toString()

            val resultado = verificarContraseña(contraseñaNueva)
            if (!resultado.isEmpty()) {
                tvContraseña?.setText(" Contraseña Errónea:\n$resultado")
                Log.i("DialogoPCambiarContraseña", " Contraseña Errónea:\n$resultado")
            } else if (contraseñaNueva != contraseñaNuevaRepetir) {
                tvContraseña?.setText("Las contraseñas no coinciden.")
            } else {
                val contraseñaNuevaCifrada = hashPassword(contraseñaNueva)

                Log.d(
                    "CambiarPasswordActivity",
                    "Contraseña cifrada: $contraseñaNuevaCifrada"
                )
                val updateCmd =
                    "UPDATE CofradesPermisos SET contraseña = '$contraseñaNuevaCifrada' WHERE correoElectronico  = '$correoElectronico' $permisoUsuario"

                val updateTask = MiExecutorTask(clienteSSL, object : MiExecutorTask.MiExecutorTaskCallback {
                    override fun onRespuestaRecibida(respuesta: String) {
                        Log.d(
                            "CambiarPasswordActivity",
                            "Respuesta UPDATE: $respuesta"
                        )
                        tvContraseña?.let { textView ->
                            textView.text = respuesta
                        }
                        if (respuesta.startsWith("Registro actualizado correctamente")) {
                            Toast.makeText(
                                this@CrearPasswordActivity, // Usa this@ para disambiguar si es necesario
                                "Contraseña actualizada con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            val result = Intent()
                            setResult(RESULT_OK, result)
                        } else {
                            Toast.makeText(
                                this@CrearPasswordActivity,
                                "Error al actualizar la contraseña",
                                Toast.LENGTH_SHORT
                            ).show()
                            setResult(RESULT_CANCELED) // Indica que la operación falló
                        }
                        finish() // Finaliza la actividad después de procesar la respuesta
                    }
                }, mainExecutor)

                updateTask.ejecutar(updateCmd)
            }
        })
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    private fun verificarContraseña(contraseña: String): String {
        var resultado = ""

        if (contraseña.length < 8) {
            resultado += "X - Debe tener al menos 8 caracteres. \n"
        }
        if (!contraseña.matches(".*[a-z].*".toRegex())) {
            resultado += "X - Debe contener al menos una letra minúscula. \n"
        }
        if (!contraseña.matches(".*[A-Z].*".toRegex())) {
            resultado += "X - Debe contener al menos una letra mayúscula. \n"
        }
        if (!contraseña.matches(".*\\d.*".toRegex())) {
            resultado += "X - Debe contener al menos un número. \n"
        }
        if (!contraseña.matches(".*[@#$%^&+=!].*".toRegex())) {
            resultado += "X - Debe contener al menos un carácter especial (@#$%^&+=!).\n"
        }
        if (contraseña.equals(contraseñaDefault)) {
            resultado += "X - La contraseña que has escrito es la misma que \n tenias por defecto, cambiala.\n"
        }

        return resultado
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
            etContraseña!!.backgroundTintList = colorStateList
            etContraseñaRepetir!!.backgroundTintList = colorStateList

            //Boton Login
            btn_Guardar!!.setBackgroundColor(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))

        } else {
            Toast.makeText(
                this,
                "Fallo al cargar las preferencias en los colores.",
                Toast.LENGTH_SHORT
            ).show()
        }


        // Aplicar fuente de los Datos
        val fuenteDatosPref = prefs.getString("fuenteDatos", "monospace")!!
        if (fuenteDatosPref.length > 0) {
            val fuenteDatos = obtenerFuenteDesdeNombre(fuenteDatosPref)
            etContraseña!!.typeface = fuenteDatos
            etContraseñaRepetir!!.typeface = fuenteDatos
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente del comentario de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Feedback
        val fuenteFeedbackPref = prefs.getString("fuenteFeedback", "sans-serif")!!
        if (fuenteFeedbackPref.length > 0) {
            val fuenteFeedback = obtenerFuenteDesdeNombre(fuenteFeedbackPref)
            // Aplicar la fuente al feedback
            tvContraseña!!.typeface = fuenteFeedback
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
            // Aplicar la fuente al boton
            btn_Guardar!!.typeface = fuenteBotones
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
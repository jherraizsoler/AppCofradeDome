package com.example.CofradeDome.Activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.Models.EmailEncrypter
import com.example.CofradeDome.R
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.Executor
import com.example.CofradeDome.Dialogos.crearDialogoConfirmacion;

class LoginActivity : AppCompatActivity() {
    private lateinit var clienteSSL: ClienteSSL
    private var LoginExecutor: Executor? = null

    private var miImageView: ImageView? = null

    private var btn_login: Button? = null
    private var btn_Huella: Button? = null
    private var et_correoElectronico: EditText? = null
    private var et_contraseña: EditText? = null
    private var tv_feedback: TextView? = null
    private lateinit var dialogoHelper: crearDialogoConfirmacion

    private var contraseñaDefault: String = "Cofrade25.$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        LoginExecutor = ContextCompat.getMainExecutor(this)
        clienteSSL = ClienteSSL()
        dialogoHelper = crearDialogoConfirmacion()

        // Generar la clave al inicio si no existe
        try {
            if (!EmailEncrypter.isKeyGenerated(this)) {
                EmailEncrypter.generateKey(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar la clave: " + e.message)
            Toast.makeText(this, "Error al inicializar la seguridad.", Toast.LENGTH_SHORT).show()
        }

        et_correoElectronico = findViewById(R.id.editTextTextEmailAddress)
        et_contraseña = findViewById(R.id.editTextTextPassword)
        tv_feedback = findViewById(R.id.tv)
        btn_login = findViewById(R.id.btn_Login)
        btn_Huella = findViewById(R.id.btn_Huella)
        miImageView = findViewById(R.id.imageView)

        btn_login?.setOnClickListener(View.OnClickListener {
            val email = et_correoElectronico?.getText().toString()
            val password = et_contraseña?.getText().toString()
            val hashedPassword = hashPassword(password)

            val selectCmd =
                "SELECT idCofrade, nivelPermiso, contraseña FROM CofradesPermisos WHERE correoElectronico = '$email';"

            val selectTask = MiExecutorTask(
                clienteSSL,
                object : MiExecutorTask.MiExecutorTaskCallback {
                    override fun onRespuestaRecibida(respuesta: String) {
                        try {
                            if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se ha podido realizar el login contacta con el administrador")) {
                                val registros =
                                    respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                if (registros.size == 1) {
                                    val campos =
                                        registros[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                            .toTypedArray()
                                    val idCofrade = campos[0].toInt()
                                    val permiso = campos[1].toInt()
                                    val contraseñaBD = campos[2].toString()

                                    if (BCrypt.checkpw(password, contraseñaBD)) {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Verificado correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tv_feedback?.setText("Verificado correctamente")

                                        if(password.equals(contraseñaDefault)){
                                            val crearPasswordActivity = Intent(
                                                this@LoginActivity,
                                                CrearPasswordActivity::class.java
                                            )
                                            crearPasswordActivity.putExtra("IDCOFRADEPASALISTA", idCofrade)
                                            crearPasswordActivity.putExtra("PERMISO", permiso)
                                            crearPasswordActivity.putExtra("EMAIL", email)
                                            startActivity(crearPasswordActivity)
                                        }

                                        // **Verificar si el email no es null antes de cifrar**
                                        if (email != null) {
                                            Log.d(
                                                "LoginActivity",
                                                "El email no es null --> " + email.length + " contenido de email " + email.toString()
                                            )

                                            val encryptedEmail =
                                                getPreferences(MODE_PRIVATE).getString(PREF_ENCRYPTED_EMAIL, null)
                                            val email = et_correoElectronico?.getText().toString()
                                            if (encryptedEmail != null) {
                                                try {
                                                    val decryptedEmail =
                                                        EmailEncrypter.decryptEmail(this@LoginActivity, encryptedEmail)
                                                    if(!email.equals(decryptedEmail)){

                                                        dialogoHelper.mostrarDialogoConfirmarCancelar(
                                                            this@LoginActivity,
                                                            "Solicitud inico rapido - encriptar email - tecnologias biometricas",
                                                            "¿ Quieres activar el inicio rapido mediante tecnologias biometricas(huella dacticar o detección facial)" +
                                                                    " y guardar tu email en un archivo cifrado interno dentro de la aplicación? ",
                                                            "Necesitamos que nos des permiso para poder encriptar su email en un" +
                                                                    "archivo cifrado internamente en la app para habilitar las " +
                                                                    "tecnologias biometricas(huella dactilar, detección facial interno del movil)",
                                                            "Dar permiso",
                                                            "No doy permiso",{
                                                                saveEncryptedEmail(email)
                                                                updateBiometricButtonVisibility()

                                                                val main = Intent(
                                                                    this@LoginActivity,
                                                                    MainActivity::class.java
                                                                )
                                                                main.putExtra("IDCOFRADEPASALISTA", idCofrade)
                                                                main.putExtra("PERMISO", permiso)
                                                                main.putExtra("EMAIL", email)
                                                                startActivity(main)
                                                            },{
                                                                Toast.makeText(
                                                                    this@LoginActivity,
                                                                    "Se a denegado exitosamente los permisos de encriptar email internamente y " +
                                                                            "habilitar la tecnologia biometrica(huella dactilar, detección facial) ",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                val main = Intent(
                                                                    this@LoginActivity,
                                                                    MainActivity::class.java
                                                                )
                                                                main.putExtra("IDCOFRADEPASALISTA", idCofrade)
                                                                main.putExtra("PERMISO", permiso)
                                                                main.putExtra("EMAIL", email)
                                                                startActivity(main)
                                                            })
                                                    }else{
                                                        val main = Intent(
                                                            this@LoginActivity,
                                                            MainActivity::class.java
                                                        )
                                                        main.putExtra("IDCOFRADEPASALISTA", idCofrade)
                                                        main.putExtra("PERMISO", permiso)
                                                        main.putExtra("EMAIL", email)
                                                        startActivity(main)

                                                    }

                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error al descifrar el correo electrónico: " + e.message)
                                                    Toast.makeText(
                                                        this@LoginActivity,
                                                        "Error al descifrar la información.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                dialogoHelper.mostrarDialogoConfirmarCancelar(
                                                    this@LoginActivity,
                                                    "Solicitud inico rapido - encriptar email - tecnologias biometricas",
                                                    "¿ Quieres activar el inicio rapido mediante tecnologias biometricas(huella dacticar o detección facial) ? ",
                                                    "Necesitamos que nos des permiso para poder encriptar su email en un" +
                                                            "archivo cifrado internamente en la app para habilitar las " +
                                                            "tecnologias biometricas(huella dactilar, detección facial interno del movil)",
                                                    "Dar permiso",
                                                    "No doy permiso",{
                                                        saveEncryptedEmail(email)
                                                        updateBiometricButtonVisibility()

                                                        val main = Intent(
                                                            this@LoginActivity,
                                                            MainActivity::class.java
                                                        )
                                                        main.putExtra("IDCOFRADEPASALISTA", idCofrade)
                                                        main.putExtra("PERMISO", permiso)
                                                        main.putExtra("EMAIL", email)
                                                        startActivity(main)
                                                    },{
                                                        Toast.makeText(
                                                            this@LoginActivity,
                                                            "Se a denegado exitosamente los permisos de encriptar email internamente y " +
                                                                    "habilitar la tecnologia biometrica(huella dactilar, detección facial) ",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        val main = Intent(
                                                            this@LoginActivity,
                                                            MainActivity::class.java
                                                        )
                                                        main.putExtra("IDCOFRADEPASALISTA", idCofrade)
                                                        main.putExtra("PERMISO", permiso)
                                                        main.putExtra("EMAIL", email)
                                                        startActivity(main)
                                                    })
                                            }
                                        } else {
                                            Log.e(
                                                "LoginActivity",
                                                "Error: El correo electrónico obtenido del EditText es null."
                                            )
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "Error de inicio de sesión.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Contraseña incorrecta",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tv_feedback?.setText("Contraseña incorrecta")
                                    }
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Email incorrecto",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    tv_feedback?.setText("Email incorrecto")
                                }
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "No esta registrado este email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error al procesar la respuesta: " + e.message)
                            Toast.makeText(
                                this@LoginActivity,
                                "No esta registrado este email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                LoginExecutor
            )
            selectTask.ejecutar(selectCmd)
        })

        btn_Huella?.setOnClickListener(View.OnClickListener { showBiometricPrompt() })

        updateBiometricButtonVisibility() // Actualizar la visibilidad del botón al iniciar la actividad
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
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
            // Aplicar el color a los elementos correspondientes (ejemplo: Toolbar)
            if (supportActionBar != null) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(colorPrincipal))
            }
            // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)

            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
            et_correoElectronico!!.backgroundTintList = colorStateList
            et_contraseña!!.backgroundTintList = colorStateList

            //Boton Login
            btn_login!!.setBackgroundColor(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
            btn_Huella!!.setBackgroundColor(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
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
            // Aplicar la fuente a los textview
            et_correoElectronico!!.typeface = fuenteEncabezados
            et_contraseña!!.typeface = fuenteEncabezados
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
            // Aplicar la fuente al feedback
            tv_feedback!!.typeface = fuenteFeedback
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
            btn_login!!.typeface = fuenteBotones
            btn_Huella!!.typeface = fuenteBotones
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
            "Azul" -> miImageView!!.setImageResource(R.mipmap.ic_logo_azul)
            "Verde" -> miImageView!!.setImageResource(R.mipmap.ic_logo_verde)
            "Rojo" -> miImageView!!.setImageResource(R.mipmap.ic_logo_rojo)
            "Naranja" -> miImageView!!.setImageResource(R.mipmap.ic_logo_naranja)
            "Morado" -> miImageView!!.setImageResource(R.mipmap.ic_logo_morado)
            else -> miImageView!!.setImageResource(R.mipmap.ic_logo_morado)
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

    /**Sección huella dactilar */
    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast("Autenticación error: Tienes el reconocimiento facial o detector de huella digital desactivadas")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i(TAG, "Autenticación biométrica exitosa.")
                    Toast.makeText(
                        this@LoginActivity,
                        "Autenticación biométrica exitosa.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Descifrar el correo electrónico después de la autenticación
                    val encryptedEmail =
                        getPreferences(MODE_PRIVATE).getString(PREF_ENCRYPTED_EMAIL, null)
                    val email =  et_correoElectronico!!.getText().toString().replace("\\s+".toRegex(), "")

                    if (encryptedEmail != null) {
                        try {
                            val decryptedEmail =
                                EmailEncrypter.decryptEmail(this@LoginActivity, encryptedEmail)

                            if(email.length > 10 && !email.equals(decryptedEmail)){
                                dialogoHelper.mostrarDialogoOk(
                                    this@LoginActivity,
                                    "Solicitud inicie sesion normal, actualizar email archivo cifrado ",
                                    "Hemos detectado que existe un email en archivo cifrado que no corresponde al que has iniciado sesion",
                                    "SOLUCION: " +
                                            "Vuelva a iniciar sesion para actualizar el email en el archivo cifrado internamente " +
                                            "en la app para habilitar las tecnologias biometricas(huella dactilar, detección facial interno del movil)",
                                    "Vale",{}
                                )
                            }else{

                                fetchUserPermission(decryptedEmail)
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "Error al descifrar el correo electrónico: " + e.message)
                            Toast.makeText(
                                this@LoginActivity,
                                "Error al descifrar la información.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "No se encontró información cifrada.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("Autenticación fallida: ")
                }
            })
        val promptInfo = PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Usa tu huella digital para continuar")
            .setNegativeButtonText("Cancelar")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun showToast(menssage: String?) {
        Toast.makeText(this, menssage, Toast.LENGTH_LONG).show()
    }

    /** Sección Archivo cifrado Email */
    private fun saveEncryptedEmail(email: String) {
        if (getPreferences(MODE_PRIVATE).getString(PREF_ENCRYPTED_EMAIL, null) == null ||
            getPreferences(MODE_PRIVATE).getString(PREF_ENCRYPTED_EMAIL, null)!!.isNotEmpty()
        ) {
            try {
                if (!EmailEncrypter.isKeyGenerated(this@LoginActivity)) {
                    EmailEncrypter.generateKey(this@LoginActivity)
                }
                Log.d(
                    "LoginActivity",
                    "Dentro de saveEncryptedEmail el  email no es null --> " + email.length + " contenido de email " + email
                )
                val encryptedEmail = EmailEncrypter.encryptEmail(this@LoginActivity, email)
                Log.d(
                    "LoginActivity",
                    "Dentro de saveEncryptedEmail el  encryptedEmail no es null --> " + encryptedEmail!!.length + " contenido de encryptedEmail " + encryptedEmail
                )
                getPreferences(MODE_PRIVATE).edit().putString(PREF_ENCRYPTED_EMAIL, encryptedEmail)
                    .apply()
                Toast.makeText(this, "Correo electrónico cifrado y guardado.", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error al cifrar el correo electrónico: " + e.message)
                Toast.makeText(this, "Error al cifrar la información.", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Error al cifrar el correo electrónico: $e")
                e.printStackTrace()
            }
        }
    }

    private fun updateBiometricButtonVisibility() {
        if (getPreferences(MODE_PRIVATE).getString(PREF_ENCRYPTED_EMAIL, null) != null) {
            btn_Huella!!.visibility = Button.VISIBLE
        } else {
            btn_Huella!!.visibility = Button.GONE
        }
    }

    private fun fetchUserPermission(email: String?) {
        Log.d(
            TAG,
            "Consultando permiso para el correo electrónico: $email"
        )

        val selectCmd =
            "SELECT idCofrade, nivelPermiso FROM CofradesPermisos WHERE correoElectronico = '$email';"

        val selectTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se ha podido realizar el login contacta con el administrador")) {
                        val registros =
                            respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (registros.size == 1) {
                            val campos =
                                registros[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            val idCofrade = campos[0].toInt()
                            val permiso = campos[1].toInt()

                            // **Verificar si el email no es null antes de cifrar**
                            if (email != null) {
                                Log.d(
                                    "LoginActivity",
                                    "El email no es null --> " + email.length + " contenido de email " + email.toString()
                                )

                                updateBiometricButtonVisibility()
                                navigateToMainApp(idCofrade, permiso, email)
                            } else {
                                Log.e(
                                    "LoginActivity",
                                    "Error: El correo electrónico obtenido del EditText es null."
                                )
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error de inicio de sesión.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Email incorrecto", Toast.LENGTH_SHORT)
                                .show()
                            tv_feedback?.text = "Email incorrecto"
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "No esta registrado este email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            LoginExecutor
        )
        selectTask.ejecutar(selectCmd)
    }

    private fun navigateToMainApp(idCofrade: Int, permiso: Int, email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("IDCOFRADEPASALISTA", idCofrade)
        intent.putExtra("PERMISO", permiso)
        intent.putExtra("EMAIL", email)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val PREF_ENCRYPTED_EMAIL = "encrypted_email"
    }
}
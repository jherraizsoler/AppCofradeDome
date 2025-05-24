package com.example.CofradeDome.Activities

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.CofradeDome.Adaptadores.AdaptadorTarjetasCofrades
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.MiExecutorTask.MiExecutorTaskCallback
import com.example.CofradeDome.Models.GeneradorTarjetasDinamico
import com.example.CofradeDome.Models.DatosTarjetasCofrades
import com.example.CofradeDome.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executor

class DAOCofradeTarjetas : AppCompatActivity(), View.OnClickListener,
    MiExecutorTaskCallback {
    private var vistaRecycler: RecyclerView? = null
    private var adaptador: AdaptadorTarjetasCofrades? = null
    private var tvSeleccionado_Cabecera: TextView? = null
    private var tvID_Cabecera: TextView? = null
    private var tvNombreCompleto_Cabecera: TextView? = null
    private var et_Buscador_Tarjeta: EditText? = null
    private var tvFeedback: TextView? = null
    private lateinit var clienteSSL: ClienteSSL
    private var mainExecutor: Executor? = null
    var permisoUsuario: Int = 0
    private var colorPrincipal: String? = null
    private var botonGenerarTarjetasSeleccionadas: Button? = null
    private var botonGenerarTodasTarjetas: Button? = null
    private var botonCancelar: Button? = null
    private var fuenteEncabezados: Typeface? = null
    private var fuenteDatos: Typeface? = null
    private var fuenteFeedback: Typeface? = null
    private var fuenteBotones: Typeface? = null
    private val listaOriginal = ArrayList<DatosTarjetasCofrades>()
    private val listaFiltrada = ArrayList<DatosTarjetasCofrades>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestor_tarjetas)

        clienteSSL = ClienteSSL()

        mainExecutor = ContextCompat.getMainExecutor(this)

        permisoUsuario = intent.getIntExtra("PERMISO", -9)

        tvFeedback = findViewById(R.id.tvFeedBack_Tarjeta)

        et_Buscador_Tarjeta = findViewById(R.id.et_Buscador_Tarjeta)

        et_Buscador_Tarjeta?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No necesitamos hacer nada antes de que cambie el texto
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Este método se llama cada vez que el texto cambia
                filtrarLista(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
                // No necesitamos hacer nada después de que cambie el texto
            }
        })

        //TextView CabecerasTabla
        tvSeleccionado_Cabecera = findViewById(R.id.tvEncabezadoSeleccionado_Tarjeta)
        tvID_Cabecera = findViewById(R.id.tvEncabezadoIDCofrade_Tarjeta)
        tvNombreCompleto_Cabecera = findViewById(R.id.tvEncabezadoNombreCompleto_Tarjeta)

        //Botones CRUD
        botonGenerarTarjetasSeleccionadas = findViewById(R.id.btn_Seleccionadas_Tarjeta)

        // -- Boton Alta
        // En DAOCofradeTarjetas.kt
        botonGenerarTarjetasSeleccionadas?.setOnClickListener {
            val datosSeleccionados = adaptador?.elementosSeleccionados ?: emptyList()
            if (!datosSeleccionados.isEmpty()) {
                val listaParaPdf = mutableListOf<Map<String, String>>() // Usa mutableListOf para crear la lista mutable
                for (seleccionado in datosSeleccionados) {
                    val tarjeta = mutableMapOf<String, String>() // Usa mutableMapOf para crear un mapa mutable
                    tarjeta["nombreCofradia"] = "Cofradía"
                    tarjeta["nombreCofrade"] = seleccionado["nombreCompleto"] ?: "" // Usa el operador Elvis para evitar null
                    tarjeta["qrData"] = seleccionado["idCofrade"] ?: ""
                    tarjeta["nombreColor"] = colorPrincipal ?: "Morado" // Asegúrate de que colorPrincipal esté inicializado
                    listaParaPdf.add(tarjeta)
                }
                guardarPdfEnDescargasMediaStore(this, listaParaPdf)
            } else {
                Toast.makeText(this, "No se ha seleccionado ninguna tarjeta.", Toast.LENGTH_SHORT).show()
            }
        }

        botonGenerarTodasTarjetas = findViewById(R.id.btn_Todas_Tarjeta)
        botonGenerarTodasTarjetas?.setOnClickListener {
            clickGenerarPDF(it)
        }

        botonCancelar = findViewById(R.id.btn_Cancelar_Tarjeta)
        botonCancelar?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this@DAOCofradeTarjetas,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        })

        //Cabeceras
        tvSeleccionado_Cabecera?.setTypeface(fuenteEncabezados)
        tvID_Cabecera?.setTypeface(fuenteEncabezados)
        tvNombreCompleto_Cabecera?.setTypeface(fuenteEncabezados)

        vistaRecycler = findViewById(R.id.recyclerview_Tarjeta)
        adaptador = AdaptadorTarjetasCofrades(this, listaOriginal, this, fuenteDatos)
        vistaRecycler?.setLayoutManager(LinearLayoutManager(this))
        vistaRecycler?.setAdapter(adaptador)

        registerForContextMenu(vistaRecycler)

        refrescarListaDatosBD()
    }

    override fun onClick(v: View) {}

    override fun onRespuestaRecibida(respuesta: String) {}

    fun clickGenerarPDF(view: View?) {
        Log.d("PermisosPDF", "clickGenerarPDF() llamado")
        algunaFuncionDondeNecesitasGenerarElPDF()
    }

    private fun inicializarListaTodosDatosTarjetas(callback: ListaDatosCallback) {
        val lista: MutableList<Map<String, String>> = ArrayList()

        val selectCmd =
            "SELECT idCofrade, concat(nombre, ' ' ,  primerApellido, ' ' , segundoApellido) as nombreCompleto from Cofrades;$permisoUsuario"
        val selectTask = MiExecutorTask(clienteSSL, object : MiExecutorTaskCallback {
            override fun onRespuestaRecibida(respuesta: String) {
                Log.d(
                    "GestionCofradeTarjetasActivity",
                    "Respuesta SELECT: $respuesta"
                )
                try {
                    if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se encontraron resultados")) {
                        val registros = respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (registros.size > 0) {
                            lista.clear()
                            for (i in registros.indices) {
                                val registro = registros[i]
                                val campos = registro.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                                if (campos.size == 2) {
                                    val idCofradeSelect = campos[0].toInt()
                                    val nombreCompletoSelect = campos[1]

                                    val tarjeta = mutableMapOf<String, String>()
                                    tarjeta["nombreCofradia"] = "Cofradía"
                                    tarjeta["nombreCofrade"] = nombreCompletoSelect
                                    tarjeta["qrData"] = idCofradeSelect.toString()
                                    tarjeta["nombreColor"] = colorPrincipal ?: "Morado"

                                    lista.add(tarjeta)
                                } else {
                                    Log.e(
                                        "GestionCofradeTarjetasActivity",
                                        "Formato de registro incorrecto: $registro"
                                    )
                                }
                            }
                            // Llamar al callback con la lista llena en el hilo principal
                            mainExecutor?.execute { callback.onListaLista(lista) }
                        } else {
                            Log.e(
                                "GestionCofradeTarjetasActivity",
                                "Error en la consulta o no se encontraron resultados: $respuesta"
                            )
                            mainExecutor?.execute { callback.onListaLista(ArrayList()) } // Llamar con lista vacía en error
                        }
                    } else {
                        Log.e(
                            "GestionCofradeTarjetasActivity",
                            "Error SQL o sin resultados: $respuesta"
                        )
                        mainExecutor?.execute { callback.onListaLista(ArrayList()) } // Llamar con lista vacía en error
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                    Log.e("GestionFragment", "Error al procesar registro: " + e.message)
                    mainExecutor?.execute { callback.onListaLista(ArrayList()) }
                } catch (e: NumberFormatException) {
                    Log.e("GestionFragment", "Error al convertir a entero: " + e.message)
                    mainExecutor?.execute { callback.onListaLista(ArrayList()) }
                } catch (e: Exception) {
                    Log.e(
                        "GestionFragment",
                        "Error al procesar la respuesta: " + e.message
                    )
                    mainExecutor?.execute { callback.onListaLista(ArrayList()) }
                }
            }
        }, mainExecutor)
        selectTask.ejecutar(selectCmd)
    }

    fun algunaFuncionDondeNecesitasGenerarElPDF() {
        inicializarListaTodosDatosTarjetas(object : ListaDatosCallback {
            override fun onListaLista(listaDeDatos: MutableList<Map<String, String>>) {
                if (!listaDeDatos.isEmpty()) {
                    guardarPdfEnDescargasMediaStore(
                        this@DAOCofradeTarjetas,
                        listaDeDatos
                    ) // Llama a tu método de generación de PDF aquí
                } else {
                    Toast.makeText(
                        this@DAOCofradeTarjetas,
                        "No se encontraron datos para generar el PDF.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
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

    fun refrescarListaDatosBD() {
        val selectCmd =
            "SELECT idCofrade, concat(nombre, ' ' ,  primerApellido, ' ' , segundoApellido) as nombreCompleto from Cofrades ;$permisoUsuario"
        val selectTask = MiExecutorTask(clienteSSL, object : MiExecutorTaskCallback {
            override fun onRespuestaRecibida(respuesta: String) {
                Log.d(
                    "GestionCofradeTarjetasActivity",
                    "Respuesta SELECT: $respuesta"
                )
                try {
                    if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se encontraron resultados")) {
                        val registros =
                            respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray() // Separar por ;
                        if (registros.size > 0) { // Asegurarse de que haya al menos un registro
                            listaOriginal.clear() // Limpia la lista original
                            // Limpiar la lista antes de agregar nuevos datos
                            for (i in registros.indices) {
                                val registro =
                                    registros[i] // Tomar el primer registro
                                val campos =
                                    registro.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray() // Separar por ,

                                if (campos.size == 2) {
                                    val idCofradeSelect =
                                        campos[0].toInt() // ID como int
                                    val nombreCompletoSelect = campos[1]
                                    // Actualizar la lista
                                    listaOriginal.add(
                                        DatosTarjetasCofrades(
                                            idCofradeSelect,
                                            nombreCompletoSelect
                                        )
                                    )
                                } else {
                                    Log.e(
                                        "GestionCofradeTarjetasActivity",
                                        "Formato de registro incorrecto: $registro"
                                    )
                                }
                            }
                            listaFiltrada.clear()
                            listaFiltrada.addAll(listaOriginal) // Inicialmente la lista filtrada es igual a la original
                            adaptador = AdaptadorTarjetasCofrades(
                                this@DAOCofradeTarjetas, // Usa this@ para referirte a la Activity
                                listaFiltrada,
                                this@DAOCofradeTarjetas, // Usa this@ para referirte al OnClickListener de la Activity si es el caso
                                fuenteDatos
                            ) // Inicializa el adaptador con la lista filtrada
                            vistaRecycler?.adapter = adaptador
                        } else {
                            Log.e(
                                "GestionCofradeTarjetasActivity",
                                "Error en la consulta o no se encontraron resultados: $respuesta"
                            )
                        }
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                    Log.e("GestionFragment", "Error al procesar registro: " + e.message)
                } catch (e: NumberFormatException) {
                    Log.e("GestionFragment", "Error al convertir a entero: " + e.message)
                } catch (e: Exception) {
                    Log.e(
                        "GestionFragment",
                        "Error al procesar la respuesta: " + e.message
                    )
                }
            }
        }, mainExecutor)
        selectTask.ejecutar(selectCmd)
    }

    private fun filtrarLista(texto: String) {
        var texto = texto
        listaFiltrada.clear()
        if (TextUtils.isEmpty(texto)) {
            listaFiltrada.addAll(listaOriginal) // Si el texto está vacío, muestra la lista original
        } else {
            texto = texto.lowercase(Locale.getDefault())
            for (dato in listaOriginal) {
                if (dato.id.toString().lowercase(Locale.getDefault()).contains(texto) ||
                    dato.nombrecompleto.lowercase(Locale.getDefault()).contains(texto)
                ) {
                    listaFiltrada.add(dato)
                }
            }
        }
        adaptador!!.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Aplicar color principal
        val colorPrincipalPref = prefs.getString("colorPrincipal", "Morado")!!
        val rootView =
            findViewById<View>(android.R.id.content) // Obtiene la vista raíz de la Activity
        if (colorPrincipalPref.length > 0 && rootView != null) {
            colorPrincipal = colorPrincipalPref
            // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)

            val colorStateList = ColorStateList.valueOf(
                obtenerColorPrincipalDesdeNombre(
                    colorPrincipal!!
                )
            )
            // -- Boton Alta
            botonGenerarTarjetasSeleccionadas!!.backgroundTintList = colorStateList
            // -- Boton Actualizar
            botonGenerarTodasTarjetas!!.backgroundTintList = colorStateList
            // -- Boton Eliminar
            botonCancelar!!.backgroundTintList = colorStateList
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
            fuenteEncabezados = obtenerFuenteDesdeNombre(fuenteEncabezadosPref)
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente encabezados de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Datos
        val fuenteDatosPref = prefs.getString("fuenteDatos", "monospace")!!
        if (fuenteDatosPref.length > 0) {
            fuenteDatos = obtenerFuenteDesdeNombre(fuenteDatosPref)
            adaptador!!.setFuenteDatos(fuenteDatos)
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
            fuenteFeedback = obtenerFuenteDesdeNombre(fuenteFeedbackPref)
            tvFeedback!!.typeface = fuenteFeedback
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
            fuenteBotones = obtenerFuenteDesdeNombre(fuenteBotonesPref)
            botonGenerarTarjetasSeleccionadas!!.typeface = fuenteBotones
            botonGenerarTodasTarjetas!!.typeface = fuenteBotones
            botonCancelar!!.typeface = fuenteBotones
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente del boton de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun guardarPdfEnDescargasMediaStore(
        context: Context,
        listaDeDatos: MutableList<Map<String, String>>
    ) {
        val nombreArchivo = "tarjetas_cofrades.pdf"
        var uri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (API 29) y superior
            val values = ContentValues()
            values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        } else { // Android 9 (API 28) y anterior
            // Aquí puedes usar la forma anterior con File y FileProvider si necesitas compartir la Uri
            val directorioDescargas =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val archivo = File(directorioDescargas, nombreArchivo)
            try {
                FileOutputStream(archivo).use { outputStream ->
                    val generador = GeneradorTarjetasDinamico(context)
                    GeneradorTarjetasDinamico.Companion.generarPdfTarjetas(
                        context,
                        outputStream,
                        listaDeDatos
                    )
                    Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()
                    uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        archivo
                    )
                    abrirPdfUri(context, uri)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al generar y guardar el PDF", Toast.LENGTH_SHORT)
                    .show()
            }
            return  // Salir ya que no usamos MediaStore.insert en versiones antiguas aquí
        }

        // El resto del código para abrir el Uri (para Android 10+)
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri!!).use { outputStream ->
                    if (outputStream != null) {
                        val generador = GeneradorTarjetasDinamico(context)
                        GeneradorTarjetasDinamico.Companion.generarPdfTarjetas(
                            context,
                            outputStream,
                            listaDeDatos
                        )
                        Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT)
                            .show()
                        abrirPdfUri(context, uri)
                    } else {
                        Toast.makeText(
                            context,
                            "Error al abrir el OutputStream",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al generar y guardar el PDF", Toast.LENGTH_SHORT)
                    .show()
                if (uri != null) {
                    context.contentResolver.delete(uri!!, null, null)
                }
            }
        } else {
            Toast.makeText(context, "Error al crear la entrada en MediaStore", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun abrirPdfUri(context: Context, pdfUri: Uri?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No se encontró una aplicación para abrir archivos PDF.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    internal interface ListaDatosCallback {
        fun onListaLista(lista: MutableList<Map<String, String>>)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE_PDF = 456 // Otro código de solicitud de permiso
    }
}
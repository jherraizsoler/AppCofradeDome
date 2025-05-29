package com.example.CofradeDome.fragmentos

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.CofradeDome.Activities.MainActivity
import com.example.CofradeDome.Adaptadores.AdaptadorCofradesEnsayos
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.Dialogos.DialogoModificarEnsayosCofrade
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.MiExecutorTask.MiExecutorTaskCallback
import com.example.CofradeDome.Models.DatosEnsayosCofrades
import com.example.CofradeDome.R
import java.util.Locale
import java.util.concurrent.Executor

class DAOCofradesEnsayosFragment : Fragment(), View.OnClickListener,
    MiExecutorTaskCallback {
    private var vistaRecycler: RecyclerView? = null
    private val lista = ArrayList<DatosEnsayosCofrades>()
    private var adaptador: AdaptadorCofradesEnsayos? = null

    //TextView CabecerasTabla
    private var tvID_Cabecera: TextView? = null
    private var tvNombre_Cabecera: TextView? = null
    private var tvPrimerApellido_Cabecera: TextView? = null
    private var tvSegundoApellido_Cabecera: TextView? = null
    private var tvNumEnsayos_Cabecera: TextView? = null
    private var tvFeedback: TextView? = null
    private var tvNumeroEnsayosNormalesMinimos: TextView? = null
    private var tvNumeroEnsayosNormalesMaximos: TextView? = null
    private lateinit var clienteSSL: ClienteSSL
    private var mainExecutor: Executor? = null
    private val gestionExecutor: MiExecutorTask? = null
    var posicionEdicion: Int = 0
    var permisoUsuario: Int = 0
    private var activity: MainActivity? = null
    private var fuenteDatos: Typeface? = null
    private var botonCambiarNumEnsayos: Button? = null
    private var numEnsayosMinimos = -9
    private var numEnsayosMaximos = -9
    private var botonFondoEncabezados: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ensayo, container, false)
        clienteSSL = ClienteSSL()
        mainExecutor = ContextCompat.getMainExecutor(this@DAOCofradesEnsayosFragment.context)

        //Boton background Encabezados
        botonFondoEncabezados = view.findViewById(R.id.btn_BackgroundEncabezadosEnsayo)

        //TextView CabecerasTabla
        tvID_Cabecera = view.findViewById(R.id.tvEncabezadoIDCofrade_Ensayo)
        tvNombre_Cabecera = view.findViewById(R.id.tvEncabezadoNombre_Ensayo)
        tvPrimerApellido_Cabecera = view.findViewById(R.id.tvEncabezadoPrimerApellido_Ensayo)
        tvSegundoApellido_Cabecera = view.findViewById(R.id.tvEncabezadoSegundoApellido_Ensayo)
        tvNumEnsayos_Cabecera = view.findViewById(R.id.tvEncabezadoNumeroEnsayos_Ensayo)

        tvFeedback = view.findViewById(R.id.tvFeedback_Ensayo)

        tvNumeroEnsayosNormalesMinimos = view.findViewById(R.id.tvNumeroEnsayosNormalesMinimos)
        tvNumeroEnsayosNormalesMaximos = view.findViewById(R.id.tvNumeroEnsayosNormalesMaximos)


        //Botones CRUD
        botonCambiarNumEnsayos = view.findViewById(R.id.btn_Ensayo_CambiarNumeroEnsayosNormales)

        // -- Boton Alta
        activity = getActivity() as MainActivity?
        if (activity != null) {
            val activity = activity;
                permisoUsuario = activity!!.permisoUsuario

                actualizarEnsayosMinimosMaximos(permisoUsuario)
                val colorEncabezados = ColorStateList.valueOf(obtenerColorFondoEncabezadosDesdeNombre(activity.colorPrincipal!!))
                botonFondoEncabezados?.setBackgroundTintList(colorEncabezados)

                botonCambiarNumEnsayos?.setOnClickListener(View.OnClickListener { v ->
                    clickActualizar(
                        v
                    )
                })
                //Cabeceras
                tvID_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvNombre_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvPrimerApellido_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvSegundoApellido_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvNumEnsayos_Cabecera?.setTypeface(activity.fuenteEncabezados)
                //botones
                val colorStateList =
                    ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity.colorPrincipal!!))

                // -- Boton Alta
                botonCambiarNumEnsayos?.setBackgroundTintList(colorStateList)
                botonCambiarNumEnsayos?.setTypeface(activity.fuenteBotones)

                // Obtener la fuente preferida al crear la vista
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val nombreFuentePreferida = prefs.getString("fuenteDatos", "monospace")!!
                fuenteDatos = obtenerFuenteDesdeNombre(nombreFuentePreferida)

                vistaRecycler = view.findViewById(R.id.recyclerView_Ensayos)
                adaptador = AdaptadorCofradesEnsayos(
                    this@DAOCofradesEnsayosFragment.context, lista,
                    this@DAOCofradesEnsayosFragment, fuenteDatos
                )
                vistaRecycler!!.setLayoutManager(LinearLayoutManager(this@DAOCofradesEnsayosFragment.context))
                vistaRecycler!!.setAdapter(adaptador)

                registerForContextMenu(vistaRecycler!!)

                refrescarListaDatosBD()
            }
        return view
    }

    fun contienePalabrasSQL(texto: String?): Boolean {
        var texto = texto
        if (texto == null || texto.isEmpty()) return false

        // Lista de palabras clave SQL a detectar
        val palabrasSQL = arrayOf(
            "SELECT", "INSERT", "DELETE", "UPDATE", "DROP", "ALTER",
            "EXEC", "UNION", "OR", "AND", "--", "#", "/*", "*/"
        )

        // Convertir a mayúsculas para hacer la comparación sin importar el caso
        texto = texto.uppercase(Locale.getDefault())

        // Comprobar si alguna palabra clave está contenida en la cadena
        for (palabra in palabrasSQL) {
            if (texto.startsWith(palabra)) {
                return true // Se encontró una palabra sospechosa
            } else if (texto.contains("$palabra ")) {
                return true
            }
        }
        return false // No se encontraron palabras peligrosas
    }

    fun datosEnsayos() {
        val selectCmd =
            "SELECT ensayosMinimos, ensayosMaximos FROM bd_Cofradia.Ensayos where nombreTipoEnsayos like 'EnsayoNormal' ;$permisoUsuario"
        val selectTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d("EnsayoFragment", "Respuesta SELECT: " + respuesta.length)
                    try {
                        if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se encontraron resultados")) {
                            val registros =
                                respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray() // Separar por ;
                            if (registros.size > 0) { // Asegurarse de que haya al menos un registro
                                // Limpiar la lista antes de agregar nuevos datos
                                // lista.clear() // Comentado porque no parece que estés usando una lista aquí
                                for (i in registros.indices) {
                                    val registro =
                                        registros[i] // Tomar el primer registro
                                    val campos =
                                        registro.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                            .toTypedArray() // Separar por ,

                                    if (campos.size == 2) {
                                        numEnsayosMinimos = campos[0].toInt()
                                        numEnsayosMaximos = campos[1].toInt()
                                        Log.d(
                                            "EnsayoFragment",
                                            "NumEnsayosMinimos --> $numEnsayosMinimos NumEnsayosMaximo --> $numEnsayosMaximos"
                                        )
                                    } else {
                                        Log.e(
                                            "EnsayonFragment",
                                            "Formato de registro incorrecto: $registro"
                                        )
                                    }
                                }
                            } else {
                                Log.e(
                                    "EnsayoFragment",
                                    "Error en la consulta o no se encontraron resultados: $respuesta"
                                )
                            }
                        }
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        Log.e("EnsayoFragment", "Error al procesar registro: " + e.message)
                    } catch (e: NumberFormatException) {
                        Log.e("EnsayoFragment", "Error al convertir a entero: " + e.message)
                    } catch (e: Exception) {
                        Log.e("EnsayoFragment", "Error al procesar la respuesta: " + e.message)
                    }
                }
            },
            mainExecutor
        )
        selectTask.ejecutar(selectCmd)
    }

    fun refrescarListaDatosBD() {
        // Consulta SELECT
        activity = getActivity() as MainActivity?
        if (activity != null) {
            val activity = activity;
            permisoUsuario = activity!!.permisoUsuario
            Log.i("EnsayoFragment", "Permiso: $permisoUsuario")
        }
        val selectCmd =
            "SELECT  c.*, COALESCE(subconsulta.numEnsayosAsistidos, 0) AS numEnsayosAsistidos " +
                    " FROM bd_Cofradia.Cofrades c " +
                    " LEFT JOIN ( " +
                    " SELECT a.idCofradeAsistente, COUNT(*) AS numEnsayosAsistidos " +
                    "    FROM bd_Cofradia.Asistencias a GROUP BY a.idCofradeAsistente " +
                    " ) AS subconsulta ON c.idCofrade = subconsulta.idCofradeAsistente " +
                    " JOIN " +
                    "    bd_Cofradia.Ensayos e ON e.nombreTipoEnsayos = 'EnsayoNormal' " +
                    " WHERE " +
                    "    COALESCE(subconsulta.numEnsayosAsistidos, 0) < e.ensayosMinimos ;" + permisoUsuario
        val selectTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d("EnsayoFragment", "Respuesta SELECT: " + respuesta.length)
                    try {
                        if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se encontraron resultados")) {
                            val registros =
                                respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray() // Separar por ;
                            if (registros.size > 0) { // Asegurarse de que haya al menos un registro
                                // Limpiar la lista antes de agregar nuevos datos
                                lista.clear()
                                for (i in registros.indices) {
                                    val registro =
                                        registros[i] // Tomar el primer registro
                                    val campos =
                                        registro.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                            .toTypedArray() // Separar por ,

                                    if (campos.size == 5) {
                                        val idCofradeSelect =
                                            campos[0].toInt() // ID como int
                                        val nombreSelect = campos[1]
                                        val primerApellidoSelect = campos[2]
                                        val segundoApellidoSelect = campos[3]
                                        val numEnsayosSelect = campos[4].toInt()

                                        // Actualizar la lista
                                        lista.add(
                                            DatosEnsayosCofrades(
                                                idCofradeSelect,
                                                nombreSelect,
                                                primerApellidoSelect,
                                                segundoApellidoSelect,
                                                numEnsayosSelect
                                            )
                                        )
                                    } else {
                                        Log.e(
                                            "EnsayoFragment",
                                            "Formato de registro incorrecto: $registro"
                                        )
                                    }
                                }
                                // Notificar al adaptador que los datos han cambiado
                                adaptador!!.notifyDataSetChanged()
                            } else {
                                Log.e(
                                    "EnsayoFragment",
                                    "Error en la consulta o no se encontraron resultados: $respuesta"
                                )
                            }
                        }
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        Log.e("EnsayoFragment", "Error al procesar registro: " + e.message)
                    } catch (e: NumberFormatException) {
                        Log.e("EnsayoFragment", "Error al convertir a entero: " + e.message)
                    } catch (e: Exception) {
                        Log.e("EnsayoFragment", "Error al procesar la respuesta: " + e.message)
                    }
                }
            },
            mainExecutor
        )
        selectTask.ejecutar(selectCmd)
    }

    fun clickActualizar(view: View?) {
        val dialog1 = DialogoModificarEnsayosCofrade()
        datosEnsayos()
        if (numEnsayosMinimos != -9 && numEnsayosMaximos != -9) {
            dialog1.setActivity(activity)
            dialog1.setDatos(
                numEnsayosMinimos,
                numEnsayosMaximos
            ) // Hacer consulta para saber minimo y maximo y cargar los datos
            dialog1.setDialogoPersonalizadoListener(object : DialogoModificarEnsayosCofrade.EnsayosCofrades {
                override fun EnsayosC(numEnsayosMinimosCallback: Int, numEnsayosMaximosCallback: Int) {
                    if (numEnsayosMinimosCallback < numEnsayosMaximosCallback) {
                        // Actualizacion
                        val updateCmd =
                            "UPDATE Ensayos SET ensayosMinimos = $numEnsayosMinimosCallback, ensayosMaximos = $numEnsayosMaximosCallback " +
                                    "WHERE nombreTipoEnsayos = 'EnsayoNormal' $permisoUsuario"
                        val updateTask = MiExecutorTask(
                            clienteSSL,
                            object : MiExecutorTask.MiExecutorTaskCallback {
                                override fun onRespuestaRecibida(respuesta: String) {
                                    Log.d(
                                        "EnsayoFragment",
                                        "Respuesta UPDATE: $respuesta"
                                    )
                                    tvFeedback!!.text = respuesta
                                    if (respuesta.startsWith("Registro actualizado correctamente")) {
                                        Toast.makeText(
                                            context,
                                            "Numero de ensayos actualizado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tvNumeroEnsayosNormalesMinimos!!.text = numEnsayosMinimosCallback.toString()
                                        tvNumeroEnsayosNormalesMaximos!!.text = numEnsayosMaximosCallback.toString()
                                        refrescarListaDatosBD()
                                    }
                                }
                            },
                            mainExecutor
                        )
                        updateTask.ejecutar(updateCmd)
                    } else {
                        Toast.makeText(
                            activity,
                            "Error: El número de ensayos mínimos es mayor que el número de ensayos máximos.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            dialog1.show(parentFragmentManager, "dialogo_modificar3")
            dialog1.isCancelable = false
        }
    }
    fun actualizarEnsayosMinimosMaximos(permisoUsuario: Int) {
        val selectCmd =
            "SELECT ensayosMinimos, ensayosMaximos FROM bd_Cofradia.Ensayos where nombreTipoEnsayos like 'EnsayoNormal' $permisoUsuario"
        val selectTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d(
                        "EnsayoFragment",
                        "Respuesta SELECT: $respuesta"
                    )
                    //tvFeedback.setText(respuesta);
                    try {
                        if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se encontraron resultados")) {
                            val registros =
                                respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray() // Separar por ;
                            if (registros.size > 0) { // Asegurarse de que haya al menos un registro
                                val registro = registros[0] // Tomar el primer registro
                                val campos =
                                    registro.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray() // Separar por ,

                                if (campos.size == 2) {
                                    numEnsayosMinimos = campos[0].toIntOrNull() ?: -9 // Usar toIntOrNull con valor por defecto
                                    numEnsayosMaximos = campos[1].toIntOrNull() ?: -9 // Usar toIntOrNull con valor por defecto

                                    tvNumeroEnsayosNormalesMinimos?.text = numEnsayosMinimos.toString()
                                    tvNumeroEnsayosNormalesMaximos?.text = numEnsayosMaximos.toString()
                                } else {
                                    Log.e(
                                        "EnsayoFragment",
                                        "Formato de registro incorrecto: $registro"
                                    )
                                }
                            }
                        } else {
                            Log.e(
                                "EnsayoFragment",
                                "Error en la consulta o no se encontraron resultados: $respuesta"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("EnsayoFragment", "Error al procesar la respuesta: " + e.message)
                    }
                }
            },
            mainExecutor
        )
        selectTask.ejecutar(selectCmd)
    }

    override fun onClick(view: View) {
        val id = lista[vistaRecycler!!.getChildAdapterPosition(view)].id
        val nombre = lista[vistaRecycler!!.getChildAdapterPosition(view)].nombre
        val primerApellido = lista[vistaRecycler!!.getChildAdapterPosition(view)].primerApellido
        val segundoApellido = lista[vistaRecycler!!.getChildAdapterPosition(view)].segundoApellido
        val numeroEnsayosAsistido =
            lista[vistaRecycler!!.getChildAdapterPosition(view)].numEnsayosAsistidos.toString() + " "
        val texto = String.format(
            """
           
            ID: %d   Nombre: %s
            Primer Apellido: %s
            Segundo Apellido: %s
            Numero Ensayos Asistido:  %s
            
            """.trimIndent(), id, nombre, primerApellido, segundoApellido, numeroEnsayosAsistido
        )

        tvFeedback!!.text = texto

        posicionEdicion = vistaRecycler!!.getChildAdapterPosition(view)
    }

    override fun onRespuestaRecibida(respuesta: String) {
        if (respuesta.length > 1) {
            Toast.makeText(
                this.context,
                "La respuesta tiene contenido: $respuesta", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun obtenerColorPrincipalDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Azul)
            "Verde" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Verde)
            "Rojo" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Rojo)
            "Naranja" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Naranja)
            "Morado" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Morado)
            else -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Morado)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        // Aplicar fuente de los Botonoes
        val fuenteDatosPref = prefs.getString("fuenteDatos", "monospace")!!
        Log.d(
            "OnResumeEnsayosFragment ",
            "El nombre de la fuente Datos es: $fuenteDatosPref"
        )
        fuenteDatos = obtenerFuenteDesdeNombre(fuenteDatosPref)
        if (fuenteDatos != null) {
            Log.d(
                "OnResumeEnsayosFragment ",
                "La fuente TypeFace Datos no es nula: " + fuenteDatos.toString()
            )
        } else {
            Log.d("OnResumeEnsayosFragment ", "La fuente TypeFace Datos  es nula: ")
        }
        // Actualizar la fuente en el adaptador si el adaptador ya existe
        adaptador = AdaptadorCofradesEnsayos(
            this@DAOCofradesEnsayosFragment.context, lista,
            this@DAOCofradesEnsayosFragment, fuenteDatos
        )
        adaptador!!.setFuenteDatos(fuenteDatos)
        vistaRecycler!!.adapter = adaptador
        registerForContextMenu(vistaRecycler!!)
        adaptador!!.notifyDataSetChanged() // Forzar redibujo de los items
        refrescarListaDatosBD()
        if (adaptador == null) {
            Log.d("OnResumeGestionFragment ", "El adaptador es nulo ")
        } else {
            Log.d("OnResumeGestionFragment ", "El adaptador no es nulo ")
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
                val fontResourceId =
                    resources.getIdentifier(nombreFuente, "font", context?.packageName)
                if (fontResourceId != 0) {
                    val typeface = ResourcesCompat.getFont(context, fontResourceId)
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
    private fun obtenerColorFondoEncabezadosDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(getActivity(), R.color.Encabezado_Background_Azul)
            "Verde" -> ContextCompat.getColor(getActivity(), R.color.Encabezado_Background_Verde)
            "Rojo" -> ContextCompat.getColor(getActivity(), R.color.Encabezado_Background_Rojo)
            "Naranja" -> ContextCompat.getColor(getActivity(), R.color.Encabezado_Background_Naranja)
            "Morado" -> ContextCompat.getColor(getActivity(), R.color.Encabezado_Background_Morado)
            else -> ContextCompat.getColor(getActivity(), R.color.Encabezado_Background_Morado)
        }
    }
}

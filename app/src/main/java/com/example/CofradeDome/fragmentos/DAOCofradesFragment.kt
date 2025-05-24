package com.example.CofradeDome.fragmentos

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.CofradeDome.Activities.AltaCofradesActivity
import com.example.CofradeDome.Activities.DAOCofradeTarjetas
import com.example.CofradeDome.Activities.MainActivity
import com.example.CofradeDome.Adaptadores.AdaptadorCofrades
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.Dialogos.DialogoModificarDatosCofrade
import com.example.CofradeDome.Dialogos.DialogoModificarDatosCofrade.DatosCofrade
import com.example.CofradeDome.Dialogos.crearDialogoConfirmacion
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.MiExecutorTask.MiExecutorTaskCallback
import com.example.CofradeDome.R
import java.util.Locale
import java.util.concurrent.Executor

class DAOCofradesFragment : Fragment(), View.OnClickListener,
    DatosCofrade, MiExecutorTaskCallback {
    private var vistaRecycler: RecyclerView? = null
    private val lista = ArrayList<com.example.CofradeDome.Models.DatosCofrade>()
    private var adaptador: AdaptadorCofrades? = null
    private lateinit var dialogoHelper: crearDialogoConfirmacion

    //TextView CabecerasTabla
    private var tvID_Cabecera: TextView? = null
    private var tvNombre_Cabecera: TextView? = null
    private var tvPrimerApellido_Cabecera: TextView? = null
    private var tvSegundoApellido_Cabecera: TextView? = null

    private var tvFeedback: TextView? = null
    private var lanzadorAlta: ActivityResultLauncher<Intent>? = null

    private lateinit var clienteSSL: ClienteSSL
    private var mainExecutor: Executor? = null
    private var gestionExecutor: MiExecutorTask? = null
    var posicionEdicion: Int = 0
    var permisoUsuario: Int = 0
    private var activity: MainActivity? = null
    private var fuenteDatos: Typeface? = null
    private var botonAlta: Button? = null
    private var botonActualizar: Button? = null
    private var botonEliminar: Button? = null
    private var botonConsultar: Button? = null
    private var botonGenerarPDF: Button? = null
    private var botonFondoEncabezados: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gestion, container, false)
        dialogoHelper = crearDialogoConfirmacion()
        clienteSSL = ClienteSSL()
        mainExecutor = ContextCompat.getMainExecutor(this@DAOCofradesFragment.context)
        tvFeedback = view.findViewById(R.id.tvFeedBack)

        //TextView CabecerasTabla
        tvID_Cabecera = view.findViewById(R.id.tvEncabezadoIDCofrade_Cofrade)
        tvNombre_Cabecera = view.findViewById(R.id.tvEncabezadoNombre_Cofrade)
        tvPrimerApellido_Cabecera = view.findViewById(R.id.tvEncabezadoPrimerApellido_Cofrade)
        tvSegundoApellido_Cabecera = view.findViewById(R.id.tvEncabezadoSegundoApellido_Cofrade)

        //Boton background Encabezados
        botonFondoEncabezados = view.findViewById(R.id.btn_BackgroundEncabezadosGestion)

        //Botones CRUD
        botonAlta = view.findViewById(R.id.btn_gestion_AltaFormulario)

        // -- Boton Alta
        botonAlta?.setOnClickListener(View.OnClickListener { clickAlta(view) })

        botonActualizar = view.findViewById(R.id.btn_gestion_Actualizar)
        botonActualizar?.setOnClickListener(View.OnClickListener { clickActualizar(view) })

        botonConsultar = view.findViewById(R.id.btn_gestion_Consultar)
        botonConsultar?.setOnClickListener(View.OnClickListener { clickConsulta(view) })

        botonEliminar = view.findViewById(R.id.btn_gestion_Eliminar)
        botonEliminar?.setOnClickListener(View.OnClickListener { clickEliminar(view) })

        botonGenerarPDF = view.findViewById(R.id.btn_gestion_GenerarTarjetas)
        botonGenerarPDF?.setOnClickListener(View.OnClickListener { clickGenerarPDF(view) })

        activity = getActivity() as MainActivity?
        if (activity != null) {
            val activity = activity;
            permisoUsuario = activity!!.permisoUsuario

            //Cabeceras
            tvID_Cabecera?.setTypeface(activity.fuenteEncabezados)
            tvNombre_Cabecera?.setTypeface(activity.fuenteEncabezados)
            tvPrimerApellido_Cabecera?.setTypeface(activity.fuenteEncabezados)
            tvSegundoApellido_Cabecera?.setTypeface(activity.fuenteEncabezados)

            val colorEncabezados = ColorStateList.valueOf(obtenerColorFondoEncabezadosDesdeNombre(activity.colorPrincipal!!))
            botonFondoEncabezados?.setBackgroundTintList(colorEncabezados)

            //botones
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity.colorPrincipal!!))

            // -- Boton Alta
            botonAlta?.setBackgroundTintList(colorStateList)
            botonAlta?.setTypeface(activity.fuenteBotones)

            // -- Boton Actualizar
            botonActualizar?.setBackgroundTintList(colorStateList)
            botonActualizar?.setTypeface(activity.fuenteBotones)

            // -- Boton Eliminar
            botonEliminar?.setBackgroundTintList(colorStateList)
            botonEliminar?.setTypeface(activity.fuenteBotones)

            // -- Boton Consultar
            botonConsultar?.setBackgroundTintList(colorStateList)
            botonConsultar?.setTypeface(activity.fuenteBotones)

            // -- Boton Generar Tarjetas PDF
            botonGenerarPDF?.setBackgroundTintList(colorStateList)
            botonGenerarPDF?.setTypeface(activity.fuenteBotones)

            tvFeedback?.setTypeface(activity.fuenteFeedback)



            // Obtener la fuente preferida al crear la vista
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val nombreFuentePreferida = prefs.getString("fuenteDatos", "monospace")!!
            fuenteDatos = obtenerFuenteDesdeNombre(nombreFuentePreferida)

            vistaRecycler = view.findViewById(R.id.recyclerView_gestion)
            adaptador = AdaptadorCofrades(
                this@DAOCofradesFragment.context, lista,
                this@DAOCofradesFragment, fuenteDatos
            )
            vistaRecycler!!.setLayoutManager(LinearLayoutManager(this@DAOCofradesFragment.context))
            vistaRecycler!!.setAdapter(adaptador)

            registerForContextMenu(vistaRecycler!!)

            lanzadorAlta = registerForActivityResult<Intent, ActivityResult>(
                ActivityResultContracts.StartActivityForResult(),
                object : ActivityResultCallback<ActivityResult?> {
                    override fun onActivityResult(resultado: ActivityResult?) {
                        if (resultado!!.resultCode == Activity.RESULT_OK) {
                            val nombre = resultado.data!!.getStringExtra("NOMBRE")
                            val primerApellido = resultado.data!!.getStringExtra("PRIMER_APELLIDO")
                            val segundoApellido =
                                resultado.data!!.getStringExtra("SEGUNDO_APELLIDO")

                            if (nombre!!.trim().length < 3 || primerApellido!!.trim().length < 3 || segundoApellido!!.trim().length < 3) {
                                Toast.makeText(
                                    context,
                                    "Error Alta: Revisa que algun campo no este vacio o con menos de 3 caracteres",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (contienePalabrasSQL(nombre) || contienePalabrasSQL(
                                        primerApellido
                                    ) || contienePalabrasSQL(segundoApellido)
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Error: Entrada inválida detectada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                } else {
                                    // Inserción
                                    val insertCmd =
                                        "INSERT INTO Cofrades (nombre,primerApellido, segundoApellido) VALUES ('$nombre','$primerApellido','$segundoApellido')$permisoUsuario"
                                    val insertTask = MiExecutorTask(
                                        clienteSSL,
                                        object : MiExecutorTask.MiExecutorTaskCallback {
                                            override fun onRespuestaRecibida(respuesta: String) {
                                                Log.d(
                                                    "GestionFragment",
                                                    "Respuesta INSERT: $respuesta"
                                                )
                                                tvFeedback?.text = respuesta
                                                if (respuesta.startsWith("Registro insertado correctamente")) {
                                                    // Consulta SELECT
                                                    val selectCmd =
                                                        "SELECT idCofrade, nombre, primerApellido, segundoApellido from Cofrades where nombre like '$nombre' and primerApellido like '$primerApellido' and segundoApellido like '$segundoApellido' ORDER BY idCofrade DESC LIMIT 1 $permisoUsuario"
                                                    val selectTaskSelect = MiExecutorTask(
                                                        clienteSSL,
                                                        object : MiExecutorTask.MiExecutorTaskCallback {
                                                            override fun onRespuestaRecibida(respuestaSelect: String) {
                                                                Log.d(
                                                                    "GestionFragment",
                                                                    "Respuesta SELECT: $respuestaSelect"
                                                                )
                                                                tvFeedback?.text = respuestaSelect
                                                                try {
                                                                    if (!respuestaSelect.startsWith("Error SQL") && !respuestaSelect.startsWith(
                                                                            "No se encontraron resultados"
                                                                        )
                                                                    ) {
                                                                        val registros =
                                                                            respuestaSelect.split(";".toRegex())
                                                                                .dropLastWhile { it.isEmpty() }
                                                                                .toTypedArray()
                                                                        if (registros.size > 0) {
                                                                            val registro = registros[0]
                                                                            val campos =
                                                                                registro.split(",".toRegex())
                                                                                    .dropLastWhile { it.isEmpty() }
                                                                                    .toTypedArray()

                                                                            if (campos.size == 4) {
                                                                                val idCofradeSelect =
                                                                                    campos[0].toIntOrNull() ?: -1 // Use toIntOrNull with default
                                                                                val nombreSelect = campos[1]
                                                                                val primerApellidoSelect =
                                                                                    campos[2]
                                                                                val segundoApellidoSelect =
                                                                                    campos[3]

                                                                                lista.add(
                                                                                    com.example.CofradeDome.Models.DatosCofrade(
                                                                                        idCofradeSelect,
                                                                                        nombreSelect,
                                                                                        primerApellidoSelect,
                                                                                        segundoApellidoSelect
                                                                                    )
                                                                                )
                                                                                adaptador!!.notifyItemInserted(
                                                                                    lista.size - 1
                                                                                )

                                                                                Toast.makeText(
                                                                                    context,
                                                                                    "Cofrade añadido",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            } else {
                                                                                Log.e(
                                                                                    "GestionFragment",
                                                                                    "Formato de registro incorrecto: $registro"
                                                                                )
                                                                            }
                                                                        }
                                                                    } else {
                                                                        Log.e(
                                                                            "GestionFragment",
                                                                            "Error en la consulta o no se encontraron resultados: $respuestaSelect"
                                                                        )
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Log.e(
                                                                        "GestionFragment",
                                                                        "Error al procesar la respuesta: " + e.message
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        mainExecutor
                                                    )
                                                    selectTaskSelect.ejecutar(selectCmd)
                                                }
                                            }
                                        },
                                        mainExecutor
                                    )
                                    insertTask.ejecutar(insertCmd)
                                }
                            }
                        }
                    }
                })

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

    fun refrescarListaDatosBD() {
        // Consulta SELECT
        activity = getActivity() as MainActivity?
        if (activity != null) {
            val activity = activity;
            permisoUsuario = activity!!.permisoUsuario
            Log.i("GestionFragment", "Permiso: $permisoUsuario")
        }
        val selectCmd =
            "SELECT idCofrade, nombre, primerApellido, segundoApellido from Cofrades ;$permisoUsuario"
        val selectTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d("GestionFragment", "Respuesta SELECT: " + respuesta.length)
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

                                    if (campos.size == 4) {
                                        val idCofradeSelect =
                                            campos[0].toIntOrNull() ?: -1 // Use toIntOrNull with default
                                        val nombreSelect = campos[1]
                                        val primerApellidoSelect = campos[2]
                                        val segundoApellidoSelect = campos[3]

                                        // Actualizar la lista
                                        lista.add(
                                            com.example.CofradeDome.Models.DatosCofrade(
                                                idCofradeSelect,
                                                nombreSelect,
                                                primerApellidoSelect,
                                                segundoApellidoSelect
                                            )
                                        )
                                    } else {
                                        Log.e(
                                            "GestionFragment",
                                            "Formato de registro incorrecto: $registro"
                                        )
                                    }
                                }
                                // Notificar al adaptador que los datos han cambiado
                                adaptador!!.notifyDataSetChanged()
                            } else {
                                Log.e(
                                    "GestionFragment",
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
            },
            mainExecutor
        )
        selectTask.ejecutar(selectCmd)
    }

    fun clickActualizar(view: View?) {
        if (lista.size == 0) {
            Toast.makeText(
                this@DAOCofradesFragment.context,
                "Aviso Actualizar: No hay Cofrades registrados",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (posicionEdicion >= 0 && posicionEdicion < lista.size) {
                val dialog1 = DialogoModificarDatosCofrade()
                dialog1.setActivity(activity)
                val datos = lista[posicionEdicion]
                dialog1.setDatos(
                    datos.id,
                    datos.nombre,
                    datos.primerApellido,
                    datos.segundoApellido
                )
                dialog1.setDialogoPersonalizadoListener(object : DialogoModificarDatosCofrade.DatosCofrade {
                    override fun DatosC(
                        id: Int,
                        nombre: String,
                        primerApellido: String,
                        segundoApellido: String
                    ) {

                        val nuevoDato = com.example.CofradeDome.Models.DatosCofrade(id, nombre, primerApellido, segundoApellido)
                        try {
                            if (contienePalabrasSQL(nombre) || contienePalabrasSQL(primerApellido) || contienePalabrasSQL(
                                    segundoApellido
                                )
                            ) {
                                Toast.makeText(
                                    this@DAOCofradesFragment.context,
                                    "Error: Entrada inválida detectada",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            } else {
                                // Actualizacion
                                val actualizarRegistroCmd =
                                    "UPDATE Cofrades SET nombre = '${nuevoDato.nombre}', primerApellido = '${nuevoDato.primerApellido}', segundoApellido = '${nuevoDato.segundoApellido}' where idCofrade = ${nuevoDato.id} $permisoUsuario"
                                gestionExecutor = MiExecutorTask(
                                    clienteSSL,
                                    object : MiExecutorTask.MiExecutorTaskCallback {
                                        override fun onRespuestaRecibida(respuesta: String) {
                                            Log.d(
                                                "GestionFragment",
                                                "Respuesta UPDATE: $respuesta"
                                            )
                                            tvFeedback!!.text = respuesta
                                            if (respuesta.startsWith("Registro actualizado correctamente")) {
                                                lista[posicionEdicion] = nuevoDato
                                                adaptador!!.notifyItemChanged(posicionEdicion)
                                                Toast.makeText(
                                                    context,
                                                    "Registro actualizado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    mainExecutor
                                )
                                gestionExecutor!!.ejecutar(actualizarRegistroCmd)
                            }
                        } catch (e: NumberFormatException) {
                            Toast.makeText(
                                this@DAOCofradesFragment.context,
                                "El ID del cofrade no es correcto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                })
                dialog1.show(parentFragmentManager, "dialogo_modificar2")
                dialog1.isCancelable = false
            } else {
                // Manejar el caso en que posicionEdicion no es válido
                Toast.makeText(
                    this.context,
                    "Error: No se ha seleccionado ningún elemento para actualizar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun clickEliminar(view: View?) {
        if (lista.size == 0) {
            Toast.makeText(
                this.context,
                "Aviso Eliminar: No hay Cofrades registrados",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (posicionEdicion >= 0 && posicionEdicion < lista.size) {
                val datos = lista[posicionEdicion]
                val id = datos.id
                val nombre = datos.nombre
                val primerApellido = datos.primerApellido
                val segundoApellido = datos.segundoApellido

                val textoDatos = String.format(
                    """           Seleccionado Cofrade
            ID: %d   Nombre: %s
            Primer Apellido: %s
            Segundo Apellido: %s
            """.trimIndent(), id, nombre, primerApellido, segundoApellido
                )

                dialogoHelper.mostrarDialogoConfirmarCancelar(
                    this@DAOCofradesFragment.requireActivity(),
                    "Confirmación baja cofrade",
                    "¿ Esta segur@ de querer dar de baja al cofrade? ",
                    textoDatos,
                    "Confirmar dar de baja",
                    "No confirmar la baja",{
                       //Dar permiso

                        // Consulta SELECT
                        val selectCmd =
                            "SELECT a.idCofrade FROM CofradesPermisos AS a where idCofrade = " + datos.id + ";" + permisoUsuario
                        val selectTask = MiExecutorTask(
                            clienteSSL,
                            object : MiExecutorTask.MiExecutorTaskCallback {
                                override fun onRespuestaRecibida(respuesta: String) {
                                    try {
                                        if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("Consulta correcta, ningun registro") && respuesta.isNotEmpty()) {
                                            val idCofradeSelect = Integer.parseInt(respuesta)
                                            Toast.makeText(this@DAOCofradesFragment.context,
                                                      "Error: Elimina el permiso del cofrade antes de eliminar al cofrade con id: $idCofradeSelect",
                                                       Toast.LENGTH_SHORT
                                             ).show()
                                             tvFeedback!!.text = "Error: Elimina el permiso del cofrade antes de eliminar al cofrade con id: $idCofradeSelect"
                                        } else {
                                            // Actualización
                                            val borrarAsistenciasCmd =
                                                "DELETE FROM Asistencias WHERE idCofradeAsistente = " + datos.id + ";" + permisoUsuario
                                            val deleteAsistenciasTask = MiExecutorTask(
                                                clienteSSL,
                                                object : MiExecutorTask.MiExecutorTaskCallback {
                                                    override fun onRespuestaRecibida(respuesta: String) {
                                                        Log.d(
                                                            "MainActivity",
                                                            "Respuesta DELETE: $respuesta"
                                                        )
                                                        tvFeedback!!.text = respuesta
                                                        if (respuesta.startsWith("Registro eliminado correctamente")) {

                                                            // Actualización
                                                            val borrarCmd =
                                                                "DELETE from Cofrades  where idCofrade = " + datos.id + ";" + permisoUsuario
                                                            val deleteTask = MiExecutorTask(
                                                                clienteSSL,
                                                                object : MiExecutorTask.MiExecutorTaskCallback {
                                                                    override fun onRespuestaRecibida(respuesta: String) {
                                                                        Log.d(
                                                                            "MainActivity",
                                                                            "Respuesta DELETE: $respuesta"
                                                                        )
                                                                        tvFeedback!!.text = respuesta
                                                                        if (respuesta.startsWith("Registro eliminado correctamente")) {
                                                                            Toast.makeText(
                                                                                this@DAOCofradesFragment.context, // Asegúrate de usar el contexto correcto del Fragment
                                                                                "Registro y asistencias  eliminados correctamente",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            lista.removeAt(posicionEdicion)
                                                                            adaptador!!.notifyItemRemoved(posicionEdicion)
                                                                        }
                                                                    }
                                                                },
                                                                mainExecutor
                                                            )
                                                            deleteTask.ejecutar(borrarCmd)
                                                        }else if(respuesta.startsWith("Error SQL al eliminar: 1451 (23000):Cannot delete") && respuesta.contains("CofradesPermisos")){

                                                        }
                                                    }
                                                },
                                                mainExecutor
                                            )
                                            deleteAsistenciasTask.ejecutar(borrarAsistenciasCmd)

                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "GestionFragment",
                                            "Error al procesar la respuesta: " + e.message
                                        )
                                    }
                                }
                            },
                            mainExecutor
                        )
                        selectTask.ejecutar(selectCmd)
                    },{
                        // No dar permiso
                        Toast.makeText(this@DAOCofradesFragment.context,"Baja cancelada exitosamente",Toast.LENGTH_LONG).show()
                    })
            } else {
                // Manejar el caso en que posicionEdicion no es válido
                Toast.makeText(
                    this.context,
                    "Error: No se ha seleccionado ningún elemento para eliminar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun clickAlta(view: View?) {
        val intent = Intent(
            this.context,
            AltaCofradesActivity::class.java
        )
        lanzadorAlta!!.launch(intent)
    }

    fun clickGenerarPDF(view: View?) {
        val i = Intent(getActivity(), DAOCofradeTarjetas::class.java)
        i.putExtra("PERMISO", permisoUsuario)
        startActivity(i)
    }

    fun clickConsulta(view: View?) {
        if (lista.size == 0) {
            Toast.makeText(
                this.context,
                "Aviso Consultar: No hay Cofrades registrados",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val datos = lista[posicionEdicion]
            // Consulta SELECT
            val selectCmd =
                "SELECT idCofrade, nombre, primerApellido, segundoApellido from Cofrades where  idCofrade = " + datos.id + ";" + permisoUsuario
            val selectTask = MiExecutorTask(
                clienteSSL,
                object : MiExecutorTask.MiExecutorTaskCallback {
                    override fun onRespuestaRecibida(respuesta: String) {
                        Log.d(
                            "GestionFragment",
                            "Respuesta SELECT: $respuesta"
                        )
                        tvFeedback!!.text = respuesta
                        try {
                            if (!respuesta.startsWith("Error SQL") && !respuesta.startsWith("No se encontraron resultados")) {
                                val registros =
                                    respuesta.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray() // Separar por ;
                                if (registros.size > 0) { // Asegurarse de que haya al menos un registro
                                    val registro =
                                        registros[0] // Tomar el primer registro
                                    val campos =
                                        registro.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                            .toTypedArray() // Separar por ,

                                    if (campos.size == 4) {
                                        val idCofradeSelect =
                                            campos[0].toIntOrNull() ?: -1 // ID como int con manejo de error
                                        val nombreSelect = campos[1]
                                        val primerApellidoSelect = campos[2]
                                        val segundoApellidoSelect = campos[3]
                                        Toast.makeText(
                                            this@DAOCofradesFragment.context,
                                            "Consulta Cofrade:  id $idCofradeSelect nombre $nombreSelect primer apellido $primerApellidoSelect segundo apellido $segundoApellidoSelect",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Log.e(
                                            "GestionFragment",
                                            "Formato de registro incorrecto: $registro"
                                        )
                                    }
                                }
                            } else {
                                Log.e(
                                    "GestionFragment",
                                    "Error en la consulta o no se encontraron resultados: $respuesta"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "GestionFragment",
                                "Error al procesar la respuesta: " + e.message
                            )
                        }
                    }
                },
                mainExecutor
            )
            selectTask.ejecutar(selectCmd)
        }
    }

    override fun onClick(view: View) {
        val id = lista[vistaRecycler!!.getChildAdapterPosition(view)].id
        val nombre = lista[vistaRecycler!!.getChildAdapterPosition(view)].nombre
        val primerApellido = lista[vistaRecycler!!.getChildAdapterPosition(view)].primerApellido
        val segundoApellido = lista[vistaRecycler!!.getChildAdapterPosition(view)].segundoApellido

        val texto = String.format(
            """           Seleccionado Cofrade
            ID: %d   Nombre: %s
            Primer Apellido: %s
            Segundo Apellido: %s
            """.trimIndent(), id, nombre, primerApellido, segundoApellido
        )

        tvFeedback!!.text = texto
        posicionEdicion = vistaRecycler!!.getChildAdapterPosition(view)
    }

    override fun DatosC(id: Int, nombre: String, primerApellido: String, segundoApellido: String) {
        val updateCmd =
            "UPDATE Cofrades SET nombre = '" + nombre + "', primerApellido = '" + primerApellido +
                    "', segundoApellido = '" + segundoApellido + "' WHERE idCofrade = " + id + permisoUsuario
        val updateTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d(
                        "GestionFragment",
                        "Respuesta UPDATE: $respuesta"
                    )
                    tvFeedback!!.text = respuesta
                    if (respuesta.startsWith("Registro actualizado correctamente")) {
                        lista[posicionEdicion] = com.example.CofradeDome.Models.DatosCofrade(id, nombre, primerApellido, segundoApellido)
                        adaptador!!.notifyItemChanged(posicionEdicion)
                        Toast.makeText(
                            this@DAOCofradesFragment.context,
                            "Registro actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            mainExecutor
        )
        updateTask.ejecutar(updateCmd)
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
            "OnResumeGestionFragment ",
            "El nombre de la fuente Datos es: $fuenteDatosPref"
        )
        fuenteDatos = obtenerFuenteDesdeNombre(fuenteDatosPref)
        if (fuenteDatos != null) {
            Log.d(
                "OnResumeGestionFragment ",
                "La fuente TypeFace Datos no es nula: " + fuenteDatos.toString()
            )
        } else {
            Log.d("OnResumeGestionFragment ", "La fuente TypeFace Datos  es nula: ")
        }
        // Actualizar la fuente en el adaptador si el adaptador ya existe
        adaptador = AdaptadorCofrades(
            this@DAOCofradesFragment.context, lista,
            this@DAOCofradesFragment, fuenteDatos
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

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
import com.example.CofradeDome.Activities.AltaPermisosCofradesActivity
import com.example.CofradeDome.Activities.MainActivity
import com.example.CofradeDome.Adaptadores.AdaptadorPermisos
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.Dialogos.DialogoModificaPermisosCofrade
import com.example.CofradeDome.Dialogos.crearDialogoConfirmacion
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.MiExecutorTask.MiExecutorTaskCallback
import com.example.CofradeDome.Models.DatosPermisoCofrade
import com.example.CofradeDome.R
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor
import java.util.regex.Pattern

class DAOPermisosFragment : Fragment(), View.OnClickListener,
    DialogoModificaPermisosCofrade.PermisosCofrades,
    MiExecutorTaskCallback {
    private var vistaRecycler: RecyclerView? = null
    private val lista = ArrayList<DatosPermisoCofrade>()
    private var adaptador: AdaptadorPermisos? = null
    private lateinit var dialogoHelper: crearDialogoConfirmacion

    //TextView CabecerasTabla
    private var tvID_Cabecera: TextView? = null
    private var tvIDCofrade_Cabecera: TextView? = null
    private var tvNivelPermiso_Cabecera: TextView? = null
    private var tvFechaRegistro_Cabecera: TextView? = null
    private var tvCorreoElectronico_Cabecera: TextView? = null
    private var tvFeedback: TextView? = null
    private var lanzadorAlta: ActivityResultLauncher<Intent>? = null

    private lateinit var clienteSSL: ClienteSSL
    private var mainExecutor: Executor? = null
    private var gestionExecutor: MiExecutorTask? = null
    private var posicionEdicion: Int = 0
    private var permisoUsuario: Int = 0
    private var activity: MainActivity? = null
    private var botonAlta: Button? = null
    private var botonActualizar: Button? = null
    private var botonEliminar: Button? = null
    private var botonConsultar: Button? = null
    private var botonFondoEncabezados: Button? = null
    private var botonRestablecerPassword: Button? = null

    private var fuenteDatos: Typeface? = null

    private var contraseñaDefault: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)
        dialogoHelper = crearDialogoConfirmacion()
        clienteSSL = ClienteSSL()
        mainExecutor = ContextCompat.getMainExecutor(this@DAOPermisosFragment.context)

        //Boton background Encabezados
        botonFondoEncabezados = view.findViewById(R.id.btn_BackgroundEncabezadosAdmin)
        tvFeedback = view.findViewById(R.id.tvFeedBack)

        //Botones CRUD
        botonAlta = view.findViewById(R.id.btn_Admin_AltaFormulario)
        botonAlta?.setOnClickListener(View.OnClickListener { clickAlta(view) })

        botonActualizar = view.findViewById(R.id.btn_Admin_Actualizar)
        botonActualizar?.setOnClickListener(View.OnClickListener { clickActualizar(view) })

        botonConsultar = view.findViewById(R.id.btn_Admin_Consultar)
        botonConsultar?.setOnClickListener(View.OnClickListener { clickConsulta(view) })

        botonEliminar = view.findViewById(R.id.btn_Admin_Eliminar)
        botonEliminar?.setOnClickListener(View.OnClickListener { clickEliminar(view) })



        botonRestablecerPassword = view.findViewById(R.id.btn_Admin_RestablecerPassword)
        botonRestablecerPassword?.setOnClickListener(View.OnClickListener {clickDialogoRestablecerContraseña(view)  })

        tvID_Cabecera = view.findViewById(R.id.tvEncabezadoIDCofradePermiso_CofradePermisos)
        tvIDCofrade_Cabecera = view.findViewById(R.id.tvEncabezadoIDCofrade_CofradePermisos)

        tvNivelPermiso_Cabecera = view.findViewById(R.id.tvEncabezadoNivelPermiso_CofradePermisos)
        tvFechaRegistro_Cabecera = view.findViewById(R.id.tvEncabezadoFechaRegistro_CofradePermisos)
        tvCorreoElectronico_Cabecera =
            view.findViewById(R.id.tvEncabezadoCorreoElectronico_CofradePermisos)
        activity = requireActivity() as MainActivity?
        if (activity != null) {
                val activity = activity;
                permisoUsuario = activity!!.permisoUsuario
                val colorEncabezados = ColorStateList.valueOf(obtenerColorFondoEncabezadosDesdeNombre(activity.colorPrincipal!!))
                botonFondoEncabezados?.setBackgroundTintList(colorEncabezados)

                //Cabeceras
                tvID_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvIDCofrade_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvNivelPermiso_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvFechaRegistro_Cabecera?.setTypeface(activity.fuenteEncabezados)
                tvCorreoElectronico_Cabecera?.setTypeface(activity.fuenteEncabezados)
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

                // -- Boton Restablecer Contraseña
                botonRestablecerPassword?.setBackgroundTintList(colorStateList)
                botonRestablecerPassword?.setTypeface(activity.fuenteBotones)

                tvFeedback?.setTypeface(activity.fuenteFeedback)


                // Obtener la fuente preferida al crear la vista
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val nombreFuentePreferida = prefs.getString("fuenteDatos", "monospace")!!
                fuenteDatos = obtenerFuenteDesdeNombre(nombreFuentePreferida)

                vistaRecycler = view.findViewById(R.id.recyclerView_admin)
                adaptador = AdaptadorPermisos(
                    this@DAOPermisosFragment.context, lista,
                    this@DAOPermisosFragment, fuenteDatos
                )
                vistaRecycler!!.setLayoutManager(LinearLayoutManager(this@DAOPermisosFragment.context))
                vistaRecycler!!.setAdapter(adaptador)

                registerForContextMenu(vistaRecycler!!)

                lanzadorAlta = registerForActivityResult<Intent, ActivityResult>(
                    ActivityResultContracts.StartActivityForResult(),
                    object : ActivityResultCallback<ActivityResult?> {
                        override fun onActivityResult(resultado: ActivityResult?) {
                            if (resultado?.resultCode == Activity.RESULT_OK) {
                                val idCofrade = resultado.data!!.getIntExtra("IDCOFRADE", 0)
                                val nivelpermiso = resultado.data!!.getIntExtra("NIVELPERMISO", 0)
                                val correoElectronico =
                                    resultado.data!!.getStringExtra("CORREOELECTRONICO")
                                val fechaOtorgamiento = obtenerFechaActualCalendar()
                                if (idCofrade <= 0 || nivelpermiso < 1 || nivelpermiso > 3 || validarCorreo(
                                        correoElectronico!!
                                    ) == false
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Error Alta Permiso: Revisa que el idCofrade sea válido (1 | 50), nivel permiso sea válido (1 | 2 | 3) y correo electrónico sea válido (usuario@dominio.com)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val contraseñaDefaultString = "Cofrade25.$"
                                    val contraseñaDefaultCifrada = hashPassword(contraseñaDefaultString)
                                    val resultContraseña = verificarContraseña(contraseñaDefaultString)
                                    if (contienePalabrasSQL(correoElectronico)) {
                                        Toast.makeText(
                                            context,
                                            "Error: Entrada inválida detectada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return
                                    } else if (resultContraseña.length > 1) {
                                        Toast.makeText(
                                            context,
                                            "Error Contraseña: $resultContraseña", Toast.LENGTH_SHORT
                                        ).show()
                                        return
                                    } else {
                                        // Inserción
                                        val insertCmd =
                                            "INSERT INTO CofradesPermisos (idCofrade, nivelPermiso, fechaOtorgamiento, correoElectronico, contraseña) VALUES ($idCofrade,$nivelpermiso,'$fechaOtorgamiento','$correoElectronico','$contraseñaDefaultCifrada')$permisoUsuario"
                                        val insertTask = MiExecutorTask(
                                            clienteSSL,
                                            object : MiExecutorTask.MiExecutorTaskCallback {
                                                override fun onRespuestaRecibida(respuesta: String) {
                                                    Log.d(
                                                        "AdminnFragment",
                                                        "Respuesta INSERT: $respuesta"
                                                    )

                                                    if (respuesta.startsWith("Registro insertado correctamente")) {
                                                        // Consulta SELECT
                                                        val selectCmd =
                                                            "SELECT idCofradePermiso, idCofrade, nivelPermiso, fechaOtorgamiento, correoElectronico from CofradesPermisos where  idCofrade  = $idCofrade and  nivelPermiso = $nivelpermiso and fechaOtorgamiento like '$fechaOtorgamiento' and  correoElectronico like '$correoElectronico' ORDER BY idCofrade DESC LIMIT 1 $permisoUsuario"
                                                        val selectTaskSelect = MiExecutorTask(
                                                            clienteSSL,
                                                            object : MiExecutorTask.MiExecutorTaskCallback {
                                                                override fun onRespuestaRecibida(respuestaSelect: String) {
                                                                    Log.d(
                                                                        "AdminFragment",
                                                                        "Respuesta SELECT: $respuestaSelect"
                                                                    )

                                                                    try {
                                                                        if (!respuestaSelect.startsWith("Error SQL") && !respuestaSelect.startsWith(
                                                                                "No se encontraron resultados"
                                                                            )
                                                                        ) {
                                                                            val registros =
                                                                                respuestaSelect.split(";".toRegex())
                                                                                    .dropLastWhile { it.isEmpty() }
                                                                                    .toTypedArray() // Separar por ;
                                                                            if (registros.size > 0) { // Asegurarse de que haya al menos un registro
                                                                                val registro =
                                                                                    registros[0] // Tomar el primer registro
                                                                                val campos =
                                                                                    registro.split(",".toRegex())
                                                                                        .dropLastWhile { it.isEmpty() }
                                                                                        .toTypedArray() // Separar por ,

                                                                                if (campos.size == 5) {
                                                                                    val idCofradePermisoSelect =
                                                                                        campos[0].toInt()
                                                                                    val idCofradeSelect =
                                                                                        campos[1].toInt() // IDCofradeSelect como int
                                                                                    val nivelPermisoSelect =
                                                                                        campos[2].toInt() // nivelPermisoSelect como int
                                                                                    val fechaOtorgamientoSelect =
                                                                                        campos[3]
                                                                                    val correoElectronicoSelect =
                                                                                        campos[4]



                                                                                    Toast.makeText(
                                                                                        this@DAOPermisosFragment.context,
                                                                                        "Consulta Cofrade Permiso:  Id_Cofrade: $idCofradeSelect Nivel_Permiso: $nivelPermisoSelect Fecha_Otorgamiento:  $fechaOtorgamientoSelect Correo_Electronico: $correoElectronicoSelect",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                } else {
                                                                                    Log.e(
                                                                                        "AdminFragment",
                                                                                        "Formato de registro incorrecto: $registro"
                                                                                    )
                                                                                }
                                                                            }
                                                                        } else {
                                                                            Log.e(
                                                                                "AdminFragment",
                                                                                "Error en la consulta o no se encontraron resultados: $respuesta"
                                                                            )
                                                                            tvFeedback?.text = "Aviso: No se han encontrado resultados"
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Log.e(
                                                                            "AdminFragment",
                                                                            "Error al procesar la respuesta: " + e.message
                                                                        )
                                                                    }
                                                                }
                                                            },
                                                            mainExecutor
                                                        )
                                                        selectTaskSelect.ejecutar(selectCmd)
                                                    }else if(respuesta.startsWith("Error SQL al insertar: 1062 (23000): Duplicate entry")){
                                                        Toast.makeText(this@DAOPermisosFragment.context,"Error ya existe un permiso con este correo",Toast.LENGTH_LONG).show()
                                                        tvFeedback?.text = "Error: Ya existe un permiso con este correo, modifica el permiso"
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
            Log.i("DAOPermisosFragment", "Permiso: $permisoUsuario")
        }
        val selectCmd =
            "SELECT idCofradePermiso, idCofrade, nivelPermiso, fechaOtorgamiento, correoElectronico from CofradesPermisos $permisoUsuario"
        val selectTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d("AdminFragment", "Respuesta SELECT: " + respuesta.length)
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
                                        val idCofradePermisoSelect =
                                            campos[0].toInt() // ID como int
                                        val idCofrade = campos[1].toInt()
                                        val nivelPermiso = campos[2].toInt()
                                        val fechaOtorgamiento = campos[3]+""
                                        val correoElectronico = campos[4]+""
                                        Log.i("DAOPermisosFragment", "" +
                                                "IdCofradePermisosSelect --> " + idCofradePermisoSelect + " \n " +
                                                "Id Cofrade -->  " + idCofrade + " \n" +
                                                "Nivel permiso --> " + nivelPermiso + " \n" +
                                                "FechaOtorgamiento --> " + fechaOtorgamiento + " \n" +
                                                "Correo electronico --> " + correoElectronico)
                                        // Actualizar la lista
                                        lista.add(
                                            DatosPermisoCofrade(
                                                idCofradePermisoSelect,
                                                idCofrade,
                                                nivelPermiso,
                                                fechaOtorgamiento,
                                                correoElectronico
                                            )
                                        )
                                    } else {
                                        Log.e(
                                            "AdminFragment",
                                            "Formato de registro incorrecto: " + campos.size + " |-| " + registro
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
                this@DAOPermisosFragment.context,
                "Aviso Actualizar: No hay Permisos a Cofrades registrados",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (posicionEdicion >= 0 && posicionEdicion < lista.size) {
                val dialog1 = DialogoModificaPermisosCofrade()
                dialog1.setActivity(activity)
                val datos = lista[posicionEdicion]
                dialog1.setDatos(
                    datos.idCofradePermiso,
                    datos.idCofrade,
                    datos.nivelPermiso,
                    datos.correoElectronico
                )

                dialog1.setDialogoModificarPermisosCofradeListener(this)

                dialog1.show(parentFragmentManager, "dialogo_modificarPermisos")
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
                "Aviso Eliminar Permiso: No hay Permisos a Cofrades registrados",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (posicionEdicion >= 0 && posicionEdicion < lista.size) {
                val datos = lista[posicionEdicion]

                val idCofradePermiso: Int = datos.idCofradePermiso
                val idCofrade: Int = datos.idCofrade
                val nivelPermiso : Int= datos.nivelPermiso
                val fechaOtorgamiento: String = datos.fechaOtorgamiento
                val correoElectronico: String = datos.correoElectronico

                val textoDatos = String.format(
                    """                  Seleccionado Permiso 
            ID_Cofrade: %d  Nivel_Permiso: %d
            Fecha_Otorgamiento: %s
            Correo_Electronico: 
            %s
            """.trimIndent(), idCofrade, nivelPermiso, fechaOtorgamiento, correoElectronico
                )

                dialogoHelper.mostrarDialogoConfirmarCancelar(
                    this@DAOPermisosFragment.requireActivity(),
                    "Confirmación baja permiso cofrade",
                    "¿ Esta segur@ de quitar el permiso "+ nivelPermiso + " al siguiente cofrade? ",
                    textoDatos,
                    "Confirmar quitar permiso",
                    "No quitar permiso",{
                        //Positivo

                        val borrarCmd =
                            "DELETE from CofradesPermisos where idCofradePermiso = " + datos.idCofradePermiso + "" + permisoUsuario
                        val deleteTask = MiExecutorTask(
                            clienteSSL,
                            object : MiExecutorTask.MiExecutorTaskCallback {
                                override fun onRespuestaRecibida(respuesta: String) {
                                    Log.d(
                                        "AdminFragment",
                                        "Respuesta DELETE: $respuesta"
                                    )
                                    tvFeedback!!.text = respuesta
                                    if (respuesta.startsWith("Registro eliminado correctamente")) {
                                        Toast.makeText(
                                            this@DAOPermisosFragment.context,
                                            "Registro eliminado correctamente",
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

                    },{
                        // Negativo
                        Toast.makeText(this@DAOPermisosFragment.context,"Cancelada la baja del permiso cofrade exitosamente",Toast.LENGTH_LONG).show()
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
            AltaPermisosCofradesActivity::class.java
        )
        lanzadorAlta!!.launch(intent)
    }

    fun clickConsulta(view: View?) {
        if (lista.size == 0) {
            Toast.makeText(
                this.context,
                "Aviso Consultar: No hay Permisos Cofrades registrados",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val datos = lista[posicionEdicion]
            // Consulta SELECT
            val selectCmd =
                "SELECT  idCofrade, nivelPermiso, fechaOtorgamiento, correoElectronico from CofradesPermisos where  idCofradePermiso = " + datos.idCofradePermiso + permisoUsuario
            val selectTask = MiExecutorTask(
                clienteSSL,
                object : MiExecutorTask.MiExecutorTaskCallback {
                    override fun onRespuestaRecibida(respuesta: String) {
                        Log.d(
                            "AdminFragment",
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
                                            campos[0].toInt() // IDCofradeSelect como int
                                        val nivelPermisoSelect =
                                            campos[1].toInt() // nivelPermisoSelect como int
                                        val fechaOtorgamientoSelect = campos[2]
                                        val correoElectronicoSelect = campos[3]
                                        Toast.makeText(
                                            this@DAOPermisosFragment.context,
                                            "Consulta Cofrade Permiso:  Id_Cofrade: $idCofradeSelect Nivel_Permiso: $nivelPermisoSelect Fecha_Otorgamiento:  $fechaOtorgamientoSelect Correo_Electronico: $correoElectronicoSelect",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Log.e(
                                            "AdminFragment",
                                            "Formato de registro incorrecto: $registro"
                                        )
                                    }
                                }
                            } else {
                                Log.e(
                                    "AdminFragment",
                                    "Error en la consulta o no se encontraron resultados: $respuesta"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "AdminFragment",
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

    fun clickDialogoRestablecerContraseña(view: View?){
        contraseñaDefault = "$2a$10$9kpYsYVgWnhZvdZZH.eMae7NJ8I1bGuSidh75aVC.sTraftijwYIO"

        dialogoHelper.mostrarDialogoConInput(
                    this@DAOPermisosFragment.requireActivity(),
                    "Restablecer Contraseña Cofrade Permiso",
                    "Por favor, introduzca el ID_Permiso (un número entero):",
                    "Número",
                    "Aceptar",
                    "Cancelar",
                    onNumeroIngresado = { idCofradePermiso ->
                        Toast.makeText(this@DAOPermisosFragment.requireActivity(), "Número ingresado: $idCofradePermiso", Toast.LENGTH_SHORT).show()
                        val updateCmd =
                            "UPDATE CofradesPermisos SET contraseña = '" + contraseñaDefault + "' WHERE idCofradePermiso = " + idCofradePermiso + permisoUsuario
                        val updateTask = MiExecutorTask(
                            clienteSSL,
                            object : MiExecutorTask.MiExecutorTaskCallback {
                                override fun onRespuestaRecibida(respuesta: String) {
                                    Log.d(
                                        "DAOPermisosFragment",
                                        "Respuesta UPDATE: $respuesta"
                                    )
                                    tvFeedback!!.text = respuesta
                                    if (respuesta.startsWith("Registro actualizado correctamente")) {
                                        Toast.makeText(
                                            this@DAOPermisosFragment.context,
                                            "Contraseña actualizada correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            mainExecutor
                        )
                        updateTask.ejecutar(updateCmd)
                    },
                    onCancelar = {
                        Log.d("MiActivity", "El usuario canceló la entrada de número")
                        Toast.makeText(this@DAOPermisosFragment.requireActivity(), "Entrada cancelada", Toast.LENGTH_SHORT).show()
                    }
                )
    }

    override fun onClick(view: View) {
        val objeto = lista[vistaRecycler!!.getChildAdapterPosition(view)]

        val idCofradePermiso: Int = objeto.idCofradePermiso
        val idCofrade: Int = objeto.idCofrade
        val nivelPermiso : Int= objeto.nivelPermiso
        val fechaOtorgamiento: String = objeto.fechaOtorgamiento
        val correoElectronico: String = objeto.correoElectronico

        val texto = String.format(
            """                      Seleccionado Permiso
            ID_Cofrade_Permiso: %d   
            ID_Cofrade: %d  Nivel_Permiso: %d
            Fecha_Otorgamiento: %s
            Correo_Electronico: 
            %s
            """.trimIndent(), idCofradePermiso, idCofrade, nivelPermiso, fechaOtorgamiento, correoElectronico
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


    fun obtenerFechaActualCalendar(): String {
        val calendario = Calendar.getInstance()
        val formato =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Puedes cambiar el formato aquí
        return formato.format(calendario.time)
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

        return resultado
    }

    override fun permisosC(
        idCofradePermiso: Int,
        idCofrade: Int,
        nivelPermiso: Int,
        correoElectronico: String
    ) {
        val fechaOtorgamiento = obtenerFechaActualCalendar()
        val updateCmd =
            "UPDATE CofradesPermisos SET idCofrade = " + idCofrade + ", nivelPermiso = " + nivelPermiso +
                    ", fechaOtorgamiento = '" + fechaOtorgamiento + "', correoElectronico = '" + correoElectronico + "' WHERE idCofradePermiso = " + idCofradePermiso + permisoUsuario
        val updateTask = MiExecutorTask(
            clienteSSL,
            object : MiExecutorTask.MiExecutorTaskCallback {
                override fun onRespuestaRecibida(respuesta: String) {
                    Log.d(
                        "AdminFragment",
                        "Respuesta UPDATE: $respuesta"
                    )
                    tvFeedback!!.text = respuesta
                    if (respuesta.startsWith("Registro actualizado correctamente")) {
                        lista[posicionEdicion] = DatosPermisoCofrade(
                            idCofradePermiso,
                            idCofrade,
                            nivelPermiso,
                            fechaOtorgamiento,
                            correoElectronico
                        )
                        adaptador!!.notifyItemChanged(posicionEdicion)
                        Toast.makeText(
                            this@DAOPermisosFragment.context,
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

    private fun obtenerColorPrincipalDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Azul)
            "Verde" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Verde)
            "Rojo" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Rojo)
            "Naranja" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Naranja)
            "Morado" -> ContextCompat.getColor(getActivity(), R.color.Color_Principal_Morado)
            else -> ContextCompat.getColor(requireActivity(), R.color.Color_Principal_Morado)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        // Aplicar fuente de los Botonoes
        val fuenteDatosPref = prefs?.getString("fuenteDatos", "monospace")!!
        Log.d(
            "OnResumeAdminFragment ",
            "El nombre de la fuente Datos es: $fuenteDatosPref"
        )
        fuenteDatos = obtenerFuenteDesdeNombre(fuenteDatosPref)
        if (fuenteDatos != null) {
            Log.d(
                "OnResumeAdminFragment ",
                "La fuente TypeFace Datos no es nula: " + fuenteDatos.toString()
            )
        } else {
            Log.d("OnResumeAdminFragment ", "La fuente TypeFace Datos  es nula: ")
        }
        // Actualizar la fuente en el adaptador si el adaptador ya existe
        adaptador = AdaptadorPermisos(
            this@DAOPermisosFragment.context, lista,
            this@DAOPermisosFragment, fuenteDatos
        )
        adaptador!!.setFuenteDatos(fuenteDatos)
        vistaRecycler!!.adapter = adaptador
        registerForContextMenu(vistaRecycler!!)
        adaptador!!.notifyDataSetChanged() // Forzar redibujo de los items
        refrescarListaDatosBD()
        if (adaptador == null) {
            Log.d("OnResumeAdminFragment ", "El adaptador es nulo ")
        } else {
            Log.d("OnResumeAdminFragment ", "El adaptador no es nulo ")
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

    companion object {
        fun validarCorreo(correo: String): Boolean {
            val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(correo)
            return matcher.matches()
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

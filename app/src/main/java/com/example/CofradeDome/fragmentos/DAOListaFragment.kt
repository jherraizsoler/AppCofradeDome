package com.example.CofradeDome.fragmentos

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.CofradeDome.Activities.MainActivity
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.MiExecutorTask
import com.example.CofradeDome.MiExecutorTask.MiExecutorTaskCallback
import com.example.CofradeDome.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor

class DAOListaFragment : Fragment(), View.OnClickListener,
    MiExecutorTaskCallback {
    private var qrContainer: FrameLayout? = null
    private var barcodeView: DecoratedBarcodeView? = null
    private var btnPasarLista: Button? = null
    private var tvFeedback: TextView? = null
    private var scannerActivo = false

    private lateinit var clienteSSL: ClienteSSL
    private var mainExecutor: Executor? = null
    private var idCofradePasaLista = 0
    private var permisoUsuario = 0
    private var activity: MainActivity? = null

    private val view: View? = null

    private var iv_camara: ImageView? = null
    private var iv_feedback_correcto: ImageView? = null
    private var iv_feedback_incorrecto: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listaqr, container, false)

        clienteSSL = ClienteSSL()
        qrContainer = view.findViewById(R.id.qrScannerContainer)
        btnPasarLista = view.findViewById(R.id.btn_PasarLista)
        tvFeedback = view.findViewById(R.id.tvFeedBack)

        iv_camara = view.findViewById(R.id.iv_camara)
        iv_feedback_correcto = view.findViewById(R.id.iv_feedback_correcto)
        iv_feedback_correcto?.setVisibility(View.INVISIBLE)

        iv_feedback_incorrecto = view.findViewById(R.id.iv_feedback_incorrecto)
        iv_feedback_incorrecto?.setVisibility(View.INVISIBLE)



        mainExecutor = ContextCompat.getMainExecutor(this@DAOListaFragment.context)
        activity = getActivity() as MainActivity?
        if (activity != null) {
            val activity = activity;

            idCofradePasaLista = activity!!.idCofrade
            permisoUsuario = activity!!.permisoUsuario
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity.colorPrincipal!!))
            qrContainer?.setBackgroundTintList(colorStateList)
            btnPasarLista?.setBackgroundTintList(colorStateList)
            tvFeedback?.setTypeface(activity.fuenteFeedback)
            btnPasarLista?.setTypeface(activity.fuenteBotones)

            Log.i(
                "ListaFragment",
                "ID Cofrade PasaLista --> $idCofradePasaLista  Permiso: $permisoUsuario"
            )
        }


        btnPasarLista?.setOnClickListener(View.OnClickListener { v: View? ->
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                if (!scannerActivo) {
                    iniciarEscaneo()
                    val colorStateList = ColorStateList.valueOf(Color.GRAY)
                    iv_camara?.setBackgroundTintList(colorStateList)
                }
            } else {
                // Solicitar el permiso de la cámara si no está concedido
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    123
                )
            }
        })


        return view
    }

    private fun iniciarEscaneo() {
        barcodeView = DecoratedBarcodeView(requireContext())
        barcodeView!!.barcodeView.decoderFactory =
            DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        barcodeView!!.initializeFromIntent(requireActivity().intent)
        barcodeView!!.decodeContinuous(callback)

        qrContainer!!.removeAllViews()
        qrContainer!!.addView(barcodeView)
        barcodeView!!.resume()

        val activity = activity;

        val colorStateList =
            ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity?.colorPrincipal!!))
        val colorStateList2 = ColorStateList.valueOf(Color.WHITE)
        btnPasarLista!!.backgroundTintList = colorStateList
        btnPasarLista!!.setTextColor(colorStateList2)


        scannerActivo = true
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result != null && scannerActivo) {
                barcodeView!!.pause()
                tvFeedback!!.text = "Comprobación: " + result.text

                val colorStateList = ColorStateList.valueOf(Color.BLACK)
                btnPasarLista!!.backgroundTintList = colorStateList
                btnPasarLista!!.setTextColor(Color.WHITE)
                scannerActivo = false
                var idCofrade = -1

                try {
                    idCofrade = result.text.toInt()

                    val selectCmd =
                        "SELECT idCofrade from Cofrades where  idCofrade = $idCofrade;$permisoUsuario"
                    val selectTask = MiExecutorTask(
                        clienteSSL,
                        object : MiExecutorTask.MiExecutorTaskCallback {
                            override fun onRespuestaRecibida(respuesta: String) {
                                Log.d(
                                    "ListaFragment",
                                    "Respuesta SELECT: $respuesta"
                                )
                                val textoFeedBack = tvFeedback!!.text.toString()
                                tvFeedback!!.text = "$textoFeedBack\nRespuesta Select : $respuesta"
                                try {
                                    if (!respuesta.startsWith("Error SQL")) {
                                        if (!respuesta.startsWith("Consulta correcta, ningun registro")) {
                                            val registros = respuesta.split(";".toRegex())
                                                .dropLastWhile { it.isEmpty() }
                                                .toTypedArray() // Separar por ;
                                            if (registros.isNotEmpty()) { // Asegurarse de que haya al menos un registro
                                                val registro = registros[0] // Tomar el primer registro
                                                val campos = registro.split(",".toRegex())
                                                    .dropLastWhile { it.isEmpty() }
                                                    .toTypedArray() // Separar por ,

                                                if (campos.size == 1) {
                                                    val idCofradeSelect = campos[0].toIntOrNull() ?: -1 // ID como int con manejo de error
                                                    Toast.makeText(
                                                        this@DAOListaFragment.context,
                                                        "Consulta Cofrade:  id $idCofradeSelect",
                                                        Toast.LENGTH_SHORT
                                                    ).show()


                                                    val selectAsistenciaCmd =
                                                        "SELECT count(idCofradeAsistente) FROM Asistencias  WHERE idCofradeAsistente = $idCofradeSelect  AND fecha = CURDATE() $permisoUsuario"
                                                    val selectAsistenciaTask = MiExecutorTask(
                                                        clienteSSL,
                                                        object : MiExecutorTask.MiExecutorTaskCallback {
                                                            override fun onRespuestaRecibida(respuestaSelectAsistencia: String) {
                                                                Log.d(
                                                                    "ListaFragment",
                                                                    "Respuesta SELECT: $respuestaSelectAsistencia"
                                                                )
                                                                val textoFeedBackAsistencia =
                                                                    tvFeedback!!.text.toString()
                                                                tvFeedback!!.text =
                                                                    "$textoFeedBackAsistencia\nRespuesta Select : $respuestaSelectAsistencia"
                                                                try {
                                                                    if (!respuestaSelectAsistencia.startsWith(
                                                                            "Error SQL"
                                                                        ) && !respuestaSelectAsistencia.startsWith(
                                                                            "No se encontraron resultados"
                                                                        )
                                                                    ) {
                                                                        val registrosAsistencia =
                                                                            respuestaSelectAsistencia.split(
                                                                                ";".toRegex()
                                                                            )
                                                                                .dropLastWhile { it.isEmpty() }
                                                                                .toTypedArray() // Separar por ;
                                                                        if (registrosAsistencia.isNotEmpty()) { // Asegurarse de que haya al menos un registro
                                                                            val registroAsistencia =
                                                                                registrosAsistencia[0] // Tomar el primer registro
                                                                            val camposAsistencia =
                                                                                registroAsistencia.split(
                                                                                    ",".toRegex()
                                                                                )
                                                                                    .dropLastWhile { it.isEmpty() }
                                                                                    .toTypedArray() // Separar por ,

                                                                            if (camposAsistencia.size == 1) {
                                                                                val numeroVecesSelectIDAsistencia =
                                                                                    camposAsistencia[0].toIntOrNull()
                                                                                        ?: 0 // ID como int con manejo de error

                                                                                if (numeroVecesSelectIDAsistencia == 0) {
                                                                                    // Registrar la asistencia Insert tabla Asistencias

                                                                                    // Inserción

                                                                                    val insertCmd =
                                                                                        "INSERT INTO Asistencias (idCofradeAsistente, idCofradePasalista, fecha) VALUES ($idCofradeSelect, $idCofradePasaLista, '${obtenerFechaActualCalendar()}') $permisoUsuario"
                                                                                    val insertTask =
                                                                                        MiExecutorTask(
                                                                                            clienteSSL,
                                                                                            object :
                                                                                                MiExecutorTask.MiExecutorTaskCallback {
                                                                                                override fun onRespuestaRecibida(
                                                                                                    respuesta2: String
                                                                                                ) {
                                                                                                    Log.d(
                                                                                                        "ListaFragment",
                                                                                                        "Respuesta INSERT: $respuesta2"
                                                                                                    )
                                                                                                    tvFeedback!!.text =
                                                                                                        respuesta2
                                                                                                    if (respuesta2.startsWith(
                                                                                                            "Registro insertado correctamente"
                                                                                                        )
                                                                                                    ) {
                                                                                                        val colorStateList10 =
                                                                                                            ColorStateList.valueOf(
                                                                                                                Color.GREEN
                                                                                                            )
                                                                                                        iv_camara!!.backgroundTintList =
                                                                                                            colorStateList10
                                                                                                        iv_camara !!.visibility = View.VISIBLE

                                                                                                        iv_feedback_correcto!!.visibility =
                                                                                                            View.VISIBLE

                                                                                                        iv_feedback_incorrecto!!.visibility =
                                                                                                            View.INVISIBLE
                                                                                                    } else {
                                                                                                        val colorStateList10 =
                                                                                                            ColorStateList.valueOf(
                                                                                                                Color.RED
                                                                                                            )
                                                                                                        iv_camara!!.backgroundTintList =
                                                                                                            colorStateList10
                                                                                                        iv_camara !!.visibility = View.VISIBLE
                                                                                                        iv_feedback_correcto!!.visibility =
                                                                                                            View.INVISIBLE
                                                                                                        iv_feedback_incorrecto!!.visibility =
                                                                                                            View.VISIBLE
                                                                                                    }
                                                                                                }
                                                                                            },
                                                                                            mainExecutor
                                                                                        )
                                                                                    insertTask.ejecutar(
                                                                                        insertCmd
                                                                                    )

                                                                                    /** */
                                                                                } else if (numeroVecesSelectIDAsistencia > 0) {
                                                                                    Toast.makeText(
                                                                                        this@DAOListaFragment.context,
                                                                                        "Este cofrade ya se le ha registrado la asistencia el dia de hoy tiene el id:  $idCofradeSelect",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                    Log.e(
                                                                                        "ListaFragment",
                                                                                        "Este cofrade ya se le ha registrado la asistencia el dia de hoy tiene el id:  $idCofradeSelect"
                                                                                    )
                                                                                    tvFeedback!!.text =
                                                                                        "Este cofrade ya se le ha registrado la asistencia el dia de hoy tiene el id:  $idCofradeSelect"

                                                                                    val colorStateList10 =
                                                                                        ColorStateList.valueOf(
                                                                                            Color.RED
                                                                                        )
                                                                                    iv_camara!!.backgroundTintList =
                                                                                        colorStateList10
                                                                                    iv_camara !!.visibility = View.VISIBLE
                                                                                    iv_feedback_correcto!!.visibility =
                                                                                        View.INVISIBLE
                                                                                    iv_feedback_incorrecto!!.visibility =
                                                                                        View.VISIBLE
                                                                                }
                                                                            } else {
                                                                                Log.e(
                                                                                    "ListaFragment",
                                                                                    "Formato de registro incorrecto: $registroAsistencia"
                                                                                )
                                                                                val colorStateList10 =
                                                                                    ColorStateList.valueOf(
                                                                                        Color.RED
                                                                                    )
                                                                                iv_camara!!.backgroundTintList =
                                                                                    colorStateList10
                                                                                iv_camara !!.visibility = View.VISIBLE
                                                                                iv_feedback_correcto!!.visibility =
                                                                                    View.INVISIBLE
                                                                                iv_feedback_incorrecto!!.visibility =
                                                                                    View.VISIBLE
                                                                            }
                                                                        }
                                                                    }
                                                                }catch (e: Exception) {
                                                                        Log.e(
                                                                            "ListaFragment",
                                                                            "Error al procesar la respuesta Asistencia: " + e.message
                                                                        )
                                                                        val colorStateList10 =
                                                                            ColorStateList.valueOf(Color.RED)
                                                                        iv_camara!!.backgroundTintList =
                                                                            colorStateList10
                                                                        iv_camara !!.visibility = View.VISIBLE
                                                                        iv_feedback_correcto!!.visibility =
                                                                            View.INVISIBLE
                                                                        iv_feedback_incorrecto!!.visibility =
                                                                            View.VISIBLE
                                                                    }
                                                                }
                                                            },mainExecutor)
                                                            selectAsistenciaTask.ejecutar(selectAsistenciaCmd)
                                                        } else {
                                                        Log.e(
                                                            "ListaFragment",
                                                            "Error: Caracteres no validos en el QR:  $registro"
                                                        )
                                                        tvFeedback!!.text =
                                                            "Error: Caracteres no validos en el QR"
                                                        Toast.makeText(
                                                            activity,
                                                            "Error: Caracteres no validos en el QR",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        val colorStateList10 = ColorStateList.valueOf(Color.RED)
                                                        iv_camara!!.backgroundTintList = colorStateList10
                                                        iv_camara !!.visibility = View.VISIBLE
                                                    }
                                                }
                                            } else {
                                                Log.e(
                                                    "ListaFragment",
                                                    "Error no se encontraron resultados al id Cofrade, no existe: $respuesta"
                                                )
                                                tvFeedback!!.text =
                                                    "Error no se encontraron resultados al id Cofrade, no existe"
                                                Toast.makeText(
                                                    activity,
                                                    "Error no se encontraron resultados al id Cofrade, no existe",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val colorStateList10 = ColorStateList.valueOf(Color.RED)
                                                iv_camara!!.backgroundTintList = colorStateList10
                                                iv_camara !!.visibility = View.VISIBLE
                                            }
                                        } else {
                                            Log.e(
                                                "ListaFragment",
                                                "Error en la consulta o no se encontraron resultados al id Cofrade, no existe: $respuesta"
                                            )
                                            tvFeedback!!.text = "Error en la consulta"
                                            Toast.makeText(
                                                activity,
                                                "Error Al procesasr la respuesta, contacta con el admin",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val colorStateList10 = ColorStateList.valueOf(Color.RED)
                                            iv_camara!!.backgroundTintList = colorStateList10
                                            iv_camara !!.visibility = View.VISIBLE
                                        }

                                    } catch (e: Exception) {
                                        Log.e("ListaFragment", "Error al procesar la respuesta: " + e.message)
                                        tvFeedback!!.text =
                                            "Error: A existido un error al procesar la respuesta contacta con el admin"
                                        Toast.makeText(
                                            activity,
                                            "Error Al procesasr la respuesta, contacta con el admin",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val colorStateList10 = ColorStateList.valueOf(Color.RED)
                                        iv_camara!!.backgroundTintList = colorStateList10
                                        iv_camara !!.visibility = View.VISIBLE
                                    }
                                }
                            },
                            mainExecutor
                            )
                            selectTask.ejecutar(selectCmd)
                } catch (e: NumberFormatException) {
                    Toast.makeText(activity, "Error: Dato del QR no valido", Toast.LENGTH_SHORT)
                        .show()
                    val colorStateList10 = ColorStateList.valueOf(Color.RED)
                    iv_camara!!.backgroundTintList = colorStateList10
                    iv_camara !!.visibility = View.VISIBLE
                }
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
        }
    }

    fun obtenerFechaActualCalendar(): String {
        val calendario = Calendar.getInstance()
        val formato =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Puedes cambiar el formato aquí
        return formato.format(calendario.time)
    }


    override fun onPause() {
        super.onPause()
        val activity = activity;
        val colorStateList =
            ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(activity?.colorPrincipal!!))
        val colorStateList2 = ColorStateList.valueOf(Color.WHITE)
        btnPasarLista!!.backgroundTintList = colorStateList
        btnPasarLista!!.setTextColor(colorStateList2)
    }

    override fun onClick(v: View) {
    }

    override fun onRespuestaRecibida(respuesta: String) {
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
}


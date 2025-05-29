package com.example.CofradeDome

import android.os.Handler
import android.os.Looper
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MiExecutorTask(
    private val cliente: ClienteSSL,
    private val callback: MiExecutorTaskCallback,
    private val mainExecutor: Executor?
) :
    Executor {
    private val backgroundExecutor: Executor =
        Executors.newSingleThreadExecutor() // Executor para tareas en segundo plano

    fun ejecutar(comando: String) {
        backgroundExecutor.execute {
            val respuesta = cliente.enviarComandoSync(comando) // Ejecuta la tarea en segundo plano
            mainExecutor?.execute { callback.onRespuestaRecibida(respuesta!!) }
        }
    }

    // Interfaz para el callback
    interface MiExecutorTaskCallback {
        fun onRespuestaRecibida(respuesta: String)
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun execute(r: Runnable) {
        handler.post(r)
    }
}



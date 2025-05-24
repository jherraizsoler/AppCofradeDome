package com.example.CofradeDome.ConexionCliente // Asegúrate de que este sea el paquete correcto

import android.util.Log

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ClienteSSL() {
    var respuestaServidor: String = "Nada"

    private var sslSocketFactory: SSLSocketFactory? = null

    init {
        try {
            // =================================================================
            // ADVERTENCIA DE SEGURIDAD: ESTO DESHABILITA LA VALIDACIÓN DEL CERTIFICADO
            // NO USAR EN PRODUCCIÓN
            // =================================================================

            // Crear un TrustManager que confía en TODOS los certificados (ignora la validación)
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // No hace nada: confía en todos los clientes (no aplicable para autenticación de servidor)
                }
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // No hace nada: confía en todos los servidores
                }
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf() // No proporciona ninguna CA aceptada, lo que implica que no hay validación
                }
            })

            // Inicializar el SSLContext con este TrustManager "no-op"
            val sslContext: SSLContext = SSLContext.getInstance("TLS").apply {
                // init(KeyManagers, TrustManagers, SecureRandom)
                // KeyManagers: null porque no autenticamos al cliente
                // TrustManagers: nuestro array que confía en todo
                // SecureRandom: para generar números aleatorios seguros para criptografía
                init(null, trustAllCerts, SecureRandom())
            }

            // Obtener la SSLSocketFactory desde el contexto SSL configurado
            sslSocketFactory = sslContext.socketFactory

            Log.d("ClienteSSL", "SSLContext inicializado: Validación de certificado DESHABILITADA.")

        } catch (e: Exception) {
            Log.e("ClienteSSL", "Error al inicializar SSL con TrustAllCerts: " + e.message, e)
        }
    }

    fun enviarComandoSync(comando: String): String {
        var sslSocket: SSLSocket? = null
        var entrada: BufferedReader? = null
        var salida: PrintWriter? = null
        var respuesta = ""

        if (sslSocketFactory == null) {
            Log.e("ClienteSSL", "SSLSocketFactory no inicializado. No se puede establecer conexión segura.")
            return "Error de configuración de seguridad"
        }

        try {
            // Usa la SSLSocketFactory creada para establecer la conexión segura
            // IP_SERVIDOR debe ser la IP ACTUAL de tu servidor Ubuntu
            sslSocket = sslSocketFactory?.createSocket(IP_SERVIDOR, PUERTO) as SSLSocket?

            // Aunque el TrustManager confía en todo, el handshake sigue ocurriendo
            // y es necesario para establecer la clave de sesión y el cifrado.
            sslSocket?.startHandshake()
            Log.i("ClienteSSL", "Handshake SSL completado (validación de certificado deshabilitada).")


            entrada = BufferedReader(InputStreamReader(sslSocket?.getInputStream()))
            salida = PrintWriter(sslSocket?.getOutputStream(), true)

            salida.println(comando)
            Log.i("ClienteSSL", "Comando enviado: $comando")
            respuesta = entrada.readLine()
        } catch (e: IOException) {
            Log.e("ClienteSSL", "Error en la comunicación SSL (red o conexión rechazada): " + e.message, e)
            respuesta = "Error en la comunicación segura o conexión rechazada."
        } catch (e: Exception) {
            Log.e("ClienteSSL", "Error SSL desconocido o general: " + e.message, e)
            respuesta = "Error de seguridad inesperado."
        } finally {
            try {
                entrada?.close()
                salida?.close()
                sslSocket?.close()
            } catch (e: IOException) {
                Log.e("ClienteSSL", "Error al cerrar socket o streams: " + e.message)
            }
        }
        return respuesta
    }

    interface ClienteTCPCallback {
        fun onRespuestaRecibida(respuesta: String?)
    }

    companion object {
        private const val IP_SERVIDOR = "192.168.1.45"
        private const val PUERTO = 5000
    }
}


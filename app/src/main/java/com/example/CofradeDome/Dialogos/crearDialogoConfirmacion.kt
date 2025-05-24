package com.example.CofradeDome.Dialogos
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.example.CofradeDome.R

class crearDialogoConfirmacion {

    fun mostrarDialogoOk(
        context: Context,
        tituloDialogo: String,
        tituloMensaje: String,
        Mensaje: String,
        textoBotonPositivo: String,
        onConfirmar: () -> Unit,
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(tituloDialogo) // Puedes personalizar el título
        builder.setMessage(tituloMensaje + "\n" + Mensaje)

        // Botón "Confirmar / Dar acceso"
        builder.setPositiveButton(textoBotonPositivo) { dialog: DialogInterface, which: Int ->
            onConfirmar() // Llama a la función lambda cuando se hace clic en "Confirmar"
            dialog.dismiss() // Cierra el diálogo
        }
        builder.setCancelable(false) // Evita que el diálogo se cierre al tocar fuera de él o al presionar el botón de retroceso (opcional)

        val dialog = builder.create()
        dialog.show()
    }

    fun mostrarDialogoConfirmarCancelar(
        context: Context,
        tituloDialogo: String,
        pregunta: String,
        textoDatos: String,
        textoBotonPositivo: String,
        textoBotonNegativo: String,
        onConfirmar: () -> Unit,
        onCancelar: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(tituloDialogo) // Puedes personalizar el título
        builder.setMessage(pregunta + "\n" + textoDatos)

        // Botón "Confirmar / Dar acceso"
        builder.setPositiveButton(textoBotonPositivo) { dialog: DialogInterface, which: Int ->
            onConfirmar() // Llama a la función lambda cuando se hace clic en "Confirmar"
            dialog.dismiss() // Cierra el diálogo
        }

        // Botón "Cancelar / No dar permiso"
        builder.setNegativeButton(textoBotonNegativo) { dialog: DialogInterface, which: Int ->
            onCancelar() // Llama a la función lambda cuando se hace clic en "Cancelar"
            dialog.dismiss() // Cierra el diálogo
        }

        builder.setCancelable(false) // Evita que el diálogo se cierre al tocar fuera de él o al presionar el botón de retroceso (opcional)

        val dialog = builder.create()
        dialog.show()
    }

    fun mostrarDialogoConInput(
        context: Context,
        tituloDialogo: String,
        mensaje: String,
        hintTexto: String,
        textoBotonPositivo: String,
        textoBotonNegativo: String,
        onNumeroIngresado: (Int) -> Unit, // Callback para recibir el número ingresado (cambiado a Int)
        onCancelar: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(tituloDialogo)
        builder.setMessage(mensaje)

        val viewInflated = LayoutInflater.from(context).inflate(R.layout.dialogo_con_input, null)
        val input = viewInflated.findViewById<EditText>(R.id.editTextDialogInput)
        input.hint = hintTexto
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER // También puedes establecerlo aquí por código

        builder.setView(viewInflated)

        builder.setPositiveButton(textoBotonPositivo) { dialog: DialogInterface, which: Int ->
            val textoIngresado = input.text.toString()
            // Intenta convertir el texto a un entero
            textoIngresado.toIntOrNull()?.let { numeroIngresado ->
                onNumeroIngresado(numeroIngresado) // Enviar el número ingresado a través del callback
                dialog.dismiss()
            } ?: run {
                Toast.makeText(context, "Por favor, ingrese un número válido", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(textoBotonNegativo) { dialog: DialogInterface, which: Int ->
            onCancelar()
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.show()
    }
}
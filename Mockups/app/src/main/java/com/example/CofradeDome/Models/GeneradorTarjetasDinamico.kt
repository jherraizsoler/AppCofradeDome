package com.example.CofradeDome.Models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.CofradeDome.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream


class GeneradorTarjetasDinamico(private val context: Context?) {
    private fun generateQrCode(data: String, width: Int, height: Int): Bitmap? {
        try {
            val writer = QRCodeWriter()
            val hints: MutableMap<EncodeHintType, Any?> = HashMap()
            hints[EncodeHintType.ERROR_CORRECTION] =
                ErrorCorrectionLevel.L
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            return bitmap
        } catch (e: WriterException) {
            Log.e(TAG, "Error generating QR code: " + e.message)
            return null
        }
    }

    private fun cambiarLogoTarjetaDesdeNombre(nombreColor: String): Drawable? {
        var logoDrawable: Drawable? = null
        if (context != null) {
            logoDrawable = when (nombreColor) {
                "Azul" -> ContextCompat.getDrawable(context, R.mipmap.ic_logo_azul)
                "Verde" -> ContextCompat.getDrawable(context, R.mipmap.ic_logo_verde)
                "Rojo" -> ContextCompat.getDrawable(context, R.mipmap.ic_logo_rojo)
                "Naranja" -> ContextCompat.getDrawable(context, R.mipmap.ic_logo_naranja)
                "Morado" -> ContextCompat.getDrawable(context, R.mipmap.ic_logo_morado)
                else -> ContextCompat.getDrawable(context, R.mipmap.ic_logo_morado)
            }
        }
        return logoDrawable
    }


    companion object {
        private const val TAG = "GeneradorTarjetasDinamico"

        @Throws(IOException::class)
        private fun bitmapToImage(pdfDocument: PdfDocument, bitmap: Bitmap): Image {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageData = stream.toByteArray()
            val iTextImageData = ImageDataFactory.create(imageData)
            val image = Image(iTextImageData) // Crear Image directamente desde ImageData
            stream.close()
            return image
        }

        @Throws(IOException::class)
        fun generarPdfTarjetas(
            context: Context,
            outputStream: OutputStream?,
            listaDeDatos: MutableList<Map<String, String>>
        ) {
            //File pdfFile = new File(outputStream);
            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)

            val generador = GeneradorTarjetasDinamico(context.applicationContext)

            val tarjetaAncho = 250f
            val tarjetaAlto = 150f
            val margenPagina = 30f
            val espacioX = 20f
            val espacioY = 20f

            val xInicial = margenPagina
            val yInicial = pdfDocument.defaultPageSize.height - margenPagina - tarjetaAlto
            var xActual = xInicial
            var yActual = yInicial

            var canvas: PdfCanvas? = null

            for (datosTarjeta in listaDeDatos) {
                // Verificar si la siguiente tarjeta sobrepasaría el margen inferior
                if (yActual < margenPagina) {
                    Log.d("GenerarTarjetasPDF", "Creando nueva página por límite inferior.")
                    document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
                    yActual = pdfDocument.defaultPageSize.height - margenPagina - tarjetaAlto
                    xActual = xInicial
                    canvas = PdfCanvas(pdfDocument.lastPage)
                } else if (canvas == null) {
                    val firstPage = pdfDocument.addNewPage()
                    canvas = PdfCanvas(firstPage)
                }

                canvas.setStrokeColor(ColorConstants.BLACK)
                canvas.rectangle(
                    xActual.toDouble(),
                    yActual.toDouble(),
                    tarjetaAncho.toDouble(),
                    tarjetaAlto.toDouble()
                )
                canvas.stroke()

                val tarjetaParagraph = Paragraph()
                var imagenLogo: Image? = null
                var qrImage: Image? = null

                if (datosTarjeta.containsKey("imagenPath")) {
                    try {
                        val imageData = ImageDataFactory.create(datosTarjeta["imagenPath"])
                        val img = Image(imageData)
                        imagenLogo = img
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error al cargar la imagen: " + datosTarjeta["imagenPath"] + " - " + e.message
                        )
                    }
                } else if (datosTarjeta.containsKey("nombreColor")) {
                    try {
                        val drawable = generador.cambiarLogoTarjetaDesdeNombre(
                            datosTarjeta["nombreColor"]!!
                        )
                        val bitmap = Bitmap.createBitmap(
                            drawable!!.intrinsicWidth,
                            drawable.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val androidCanvas = Canvas(bitmap)
                        androidCanvas.setBitmap(bitmap)
                        drawable.setBounds(0, 0, androidCanvas.width, androidCanvas.height)
                        drawable.draw(androidCanvas)
                        imagenLogo = bitmapToImage(pdfDocument, bitmap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al cargar la imagen desde drawable: " + e.message)
                    }
                }

                if (datosTarjeta.containsKey("qrData")) {
                    val qrBitmap = generador.generateQrCode(datosTarjeta["qrData"]!!, 90, 90)
                    if (qrBitmap != null) {
                        qrImage = bitmapToImage(pdfDocument, qrBitmap)
                    }
                }

                if (imagenLogo != null) {
                    imagenLogo.scaleToFit(50f, 50f)
                    imagenLogo.setHorizontalAlignment(HorizontalAlignment.CENTER)

                    val tablaContenido = Table(
                        UnitValue.createPercentArray(
                            floatArrayOf(45f, 55f)
                        )
                    )

                    val celdaImagen =
                        Cell().add(imagenLogo).setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setBorder(
                                Border.NO_BORDER
                            )
                    tablaContenido.addCell(celdaImagen)

                    val tablaTextoQr = Table(floatArrayOf(1f))
                    tablaTextoQr.setBorder(Border.NO_BORDER)

                    if (datosTarjeta.containsKey("nombreCofradia")) {
                        tablaTextoQr.addCell(
                            Cell().add(
                                Paragraph(
                                    datosTarjeta["nombreCofradia"]
                                ).setFontSize(12f)
                            ).setBorder(Border.NO_BORDER)
                        )
                    }
                    if (datosTarjeta.containsKey("nombreCofrade")) {
                        tablaTextoQr.addCell(
                            Cell().add(
                                Paragraph(
                                    datosTarjeta["nombreCofrade"]
                                ).setFontSize(10f)
                            ).setBorder(Border.NO_BORDER)
                        )
                    }
                    if (qrImage != null) {
                        val celdaQr =
                            Cell().add(qrImage).setVerticalAlignment(VerticalAlignment.BOTTOM)
                                .setHorizontalAlignment(
                                    HorizontalAlignment.RIGHT
                                ).setBorder(Border.NO_BORDER)
                        tablaTextoQr.addCell(celdaQr)
                    }

                    tablaContenido.addCell(Cell().add(tablaTextoQr).setBorder(Border.NO_BORDER))
                    tarjetaParagraph.add(tablaContenido)
                } else {
                    if (datosTarjeta.containsKey("nombreCofradia")) {
                        tarjetaParagraph.add(
                            Paragraph(datosTarjeta["nombreCofradia"]).setFontSize(
                                12f
                            )
                        )
                    }
                    if (datosTarjeta.containsKey("nombreCofrade")) {
                        tarjetaParagraph.add(
                            Paragraph(datosTarjeta["nombreCofrade"]).setFontSize(
                                10f
                            )
                        )
                    }
                    if (qrImage != null) {
                        qrImage.scaleToFit(40f, 40f)
                        qrImage.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                        tarjetaParagraph.add(qrImage)
                    }
                }

                tarjetaParagraph.setFixedPosition(xActual, yActual, tarjetaAncho)
                document.add(tarjetaParagraph)

                xActual += tarjetaAncho + espacioX
                // Lógica para pasar a la siguiente fila
                if (xActual + tarjetaAncho > pdfDocument.defaultPageSize.width - margenPagina) {
                    xActual = xInicial
                    yActual -= tarjetaAlto + espacioY
                }
            }
            document.close()
        }
    }
}
package com.agon.app.data

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun renderQrBitmap(content: String, sizePx: Int = 1024, dark: Int = 0xFF0F2A44.toInt(), light: Int = 0xFFFFFFFF.toInt()): Bitmap =
    withContext(Dispatchers.Default) {
        val hints = mapOf(EncodeHintType.MARGIN to 1, EncodeHintType.CHARACTER_SET to "UTF-8")
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) for (y in 0 until sizePx) {
            bmp.setPixel(x, y, if (matrix.get(x, y)) dark else light)
        }
        bmp
    }

@Composable
fun rememberQrImage(content: String, sizePx: Int = 768): ImageBitmap? {
    val bmp = produceState<Bitmap?>(initialValue = null, content) {
        value = try { renderQrBitmap(content, sizePx) } catch (_: Exception) { null }
    }
    return bmp.value?.asImageBitmap()
}

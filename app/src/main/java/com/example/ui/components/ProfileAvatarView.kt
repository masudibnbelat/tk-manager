package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import java.io.InputStream
import kotlin.math.min

object ProfileAvatarHelper {

    val PRESET_EMOJIS = listOf(
        "💼", "💰", "💳", "🏦", "💎", "🚀", "🌟", "🎯",
        "👤", "🪙", "📊", "👑", "🦁", "🦊", "🐼", "😎", "🔥", "✨"
    )

    fun isCustomImage(avatar: String): Boolean {
        return avatar.startsWith("/") || avatar.startsWith("file:") || avatar.startsWith("content:")
    }

    /**
     * Crops any input bitmap to a perfect 1:1 aspect ratio centered crop
     * and scales to target size to ensure zero distortion.
     */
    fun cropToCenterSquare(source: Bitmap, targetSize: Int = 512): Bitmap {
        val width = source.width
        val height = source.height
        val minDim = min(width, height)
        val xOffset = (width - minDim) / 2
        val yOffset = (height - minDim) / 2
        val squareBitmap = Bitmap.createBitmap(source, xOffset, yOffset, minDim, minDim)
        return if (minDim != targetSize) {
            Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
        } else {
            squareBitmap
        }
    }

    /**
     * Reads the chosen image URI, crops it centered to 1:1 square,
     * writes it to the app's internal private storage, and returns the file path.
     */
    fun saveCroppedAvatarImage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            val croppedSquare = cropToCenterSquare(originalBitmap, 512)
            val avatarFile = File(context.filesDir, "avatar_${System.currentTimeMillis()}.png")
            avatarFile.outputStream().use { out ->
                croppedSquare.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            avatarFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun ProfileAvatarView(
    avatar: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isCircle: Boolean = true,
    fontSize: TextUnit = 24.sp
) {
    val shape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)

    if (ProfileAvatarHelper.isCustomImage(avatar)) {
        val file = File(avatar.removePrefix("file://"))
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = file,
                contentDescription = "Profile Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(shape)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatar.ifEmpty { "💼" },
                fontSize = fontSize
            )
        }
    }
}

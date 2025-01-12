package cypherdesigns.gamestudio.crewz.data.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun getBitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    tintColor: Color
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        ?: return BitmapDescriptorFactory.defaultMarker() // Fallback to default marker if resource fails

    // Set bounds for the drawable
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    // Create a bitmap to draw the vector onto
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    // Apply tint
    val paint = Paint()
    paint.colorFilter = PorterDuffColorFilter(tintColor.toArgb(), PorterDuff.Mode.SRC_IN)

    // Draw the tinted drawable
    canvas.save()
    canvas.translate(0f, 0f)
    vectorDrawable.draw(canvas)
    canvas.restore()

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

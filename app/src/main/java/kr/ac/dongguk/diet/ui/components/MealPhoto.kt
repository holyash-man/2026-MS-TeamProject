package kr.ac.dongguk.diet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/** 식사 사진. 사진이 없거나 불러오지 못하면 식기 아이콘을 보여준다. */
@Composable
fun MealPhoto(
    uri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val clipped = modifier.clip(shape)
    if (uri == null) {
        PhotoPlaceholder(clipped)
    } else {
        val fallback = rememberVectorPainter(Icons.Outlined.Restaurant)
        AsyncImage(
            model = uri,
            contentDescription = contentDescription,
            modifier = clipped.background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = fallback,
        )
    }
}

@Composable
private fun PhotoPlaceholder(modifier: Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Outlined.Restaurant,
            contentDescription = null, // 장식용
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


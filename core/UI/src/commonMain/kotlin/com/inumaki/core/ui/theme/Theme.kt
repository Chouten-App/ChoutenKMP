package com.inumaki.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.components.SharedElementController
import com.inumaki.core.ui.components.SharedElementOverlay

// Define color scheme
data class AppColors(
    val background: Color,
    val container: Color,
    val overlay: Color,
    val border: Color,
    val fg: Color,
    val accent: Color
)

val LightAppColors = AppColors(
    background = Color(0xFFF5F5F5),
    container = Color(0xFFFFFFFF),
    overlay = Color(0xFFEBEBEB),
    border = Color(0xFFE0E0E0),
    fg = Color(0xFF757575),
    accent = Color(0xFF6458ED)
)

val DarkAppColors = AppColors(
    background = Color(0xFF0C0C0C),
    container = Color(0xFF171717),
    overlay = Color(0xFF272727),
    border = Color(0xFF3B3B3B),
    fg = Color(0xFFD4D4D4),
    accent = Color(0xFF5E5CE6)
)

// Define typography
data class AppTypography(
    val largeTitle: TextStyle,
    val title1: TextStyle,
    val title2: TextStyle,
    val title3: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val callout: TextStyle,
    val subheadline: TextStyle,
    val footnote: TextStyle,
    val caption1: TextStyle,
    val caption2: TextStyle
)

val DefaultTypography = AppTypography(
    largeTitle = TextStyle(
        fontSize = 34.sp,
        lineHeight = 41.sp,
        fontWeight = FontWeight.Normal
    ),
    title1 = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Normal
    ),
    title2 = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Normal
    ),
    title3 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.Normal
    ),
    headline = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    body = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    callout = TextStyle(
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal
    ),
    subheadline = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    footnote = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal
    ),
    caption1 = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    caption2 = TextStyle(
        fontSize = 11.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.Normal
    )
)


// Define shapes
data class AppShapes(
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape
)

val DefaultShapes = AppShapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

data class AppLayout(
    val contentPadding: PaddingValues,
    val screenEdgePadding: PaddingValues,
    val posterSize: DpSize,
    val iconSize: DpSize,
    val forceHideLabels: Boolean,
    val bottomBarItemSize: DpSize,
    val bottomBarLocation: Alignment
)

private fun layoutForWidth(width: Dp): AppLayout {
    return when {
        width < 360.dp -> compactLayout()
        width < 600.dp -> phoneLayout()
        else -> tabletLayout()
    }
}

private fun compactLayout(): AppLayout {
    return AppLayout(
        contentPadding = PaddingValues(horizontal = 20.dp),
        screenEdgePadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 38.dp, top = 44.dp),
        posterSize = DpSize(90.dp, 128.dp),
        iconSize = DpSize(32.dp, 32.dp),
        forceHideLabels = true,
        bottomBarItemSize = DpSize(54.dp, 46.dp),
        bottomBarLocation = Alignment.BottomCenter
    )
}


private fun phoneLayout(): AppLayout {
    return AppLayout(
        contentPadding = PaddingValues(horizontal = 24.dp),
        screenEdgePadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 38.dp, top = 20.dp),
        posterSize = DpSize(90.dp, 128.dp),
        iconSize = DpSize(44.dp, 44.dp),
        forceHideLabels = false,
        bottomBarItemSize = DpSize(68.dp, 48.dp),
        bottomBarLocation = Alignment.BottomCenter
    )
}


private fun tabletLayout(): AppLayout {
    return AppLayout(
        contentPadding = PaddingValues(horizontal = 80.dp),
        screenEdgePadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 38.dp, top = 44.dp),
        posterSize = DpSize(140.dp, 196.dp),
        iconSize = DpSize(52.dp, 52.dp),
        forceHideLabels = false,
        bottomBarItemSize = DpSize(68.dp, 48.dp),
        bottomBarLocation = Alignment.CenterStart
    )
}

// Define CompositionLocals
val LocalChoutenColors = staticCompositionLocalOf { LightAppColors }
val LocalChoutenTypography = staticCompositionLocalOf { DefaultTypography }
val LocalChoutenShapes = staticCompositionLocalOf { DefaultShapes }
val LocalChoutenLayout = staticCompositionLocalOf { compactLayout() }
val LocalSharedController = staticCompositionLocalOf { SharedElementController() }

// ChoutenTheme provider function
@Composable
fun AppTheme(
    colors: AppColors = if (isSystemInDarkTheme()) DarkAppColors else LightAppColors,
    typography: AppTypography = DefaultTypography,
    shapes: AppShapes = DefaultShapes,
    controller: SharedElementController = SharedElementController(),
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val layout = remember(maxWidth) { layoutForWidth(maxWidth) }

        CompositionLocalProvider(
            LocalChoutenColors provides colors,
            LocalChoutenTypography provides typography,
            LocalChoutenShapes provides shapes,
            LocalChoutenLayout provides layout,
            LocalSharedController provides controller
        ) {
            content()
        }
    }
}

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalChoutenColors.current

    val typography: AppTypography
        @Composable get() = LocalChoutenTypography.current

    val shapes: AppShapes
        @Composable get() = LocalChoutenShapes.current

    val layout: AppLayout
        @Composable get() = LocalChoutenLayout.current


    val controller: SharedElementController
        @Composable get() = LocalSharedController.current
}
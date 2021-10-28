package com.thewire.nested_scroll.ui.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.abs

@Composable
fun RefreshContainer(
    modifier: Modifier = Modifier,
    iconImage: ImageVector = Icons.Filled.Refresh,
    iconBackgroundShape: Shape = CircleShape,
    iconColor: Color = Color.Cyan,
    iconBackgroundColor: Color = Color.DarkGray,
    threshold: Dp = 150.dp,
    iconSize: Dp = 25.dp,
    initialIconPosY: Dp = -iconSize,
    iconLoadPos: Dp = 15.dp,
    iconZIndex: Float = 1f,
    refreshCallback: (() -> Unit) -> Unit,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {

    //is refreshing currently happening
    val refreshState = remember {
        mutableStateOf(false)
    }

    //icon position set by scrolling
    val iconPos = remember {
        mutableStateOf(initialIconPosY)
    }

    //state of icon being dragged
    val iconState = remember {
        mutableStateOf(false)
    }


    //icon position set by either scroll or animation
    val iconPosY = animateDpAsState(
        animationSpec = tween(
            durationMillis = 250,
            easing = LinearEasing
        ),
        targetValue = when {
            //when icon is being dragged
            iconState.value -> {
//                Log.d("SCROLL", "iconpos")
                iconPos.value
            }
            //when threshold is met
            refreshState.value -> {
//                Log.d("SCROLL", "iconloadpos")
                iconLoadPos
            }
            //when released either by drag or completed refreshing
            else -> {
//                Log.d("SCROLL", "initial")
                initialIconPosY
            }
        }
    )

    //icon rotation animation when at loading pos during refresh
    val infiniteTransition = rememberInfiniteTransition()
    val iconRotate = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    //box with constraints so we know width
    BoxWithConstraints(
        modifier = modifier
            .nestedScroll(
                refreshAction(
                    threshold = with(LocalDensity.current) { threshold.toPx() },
                    iconPos = iconPos,
                    iconState = iconState,
                    refreshCallback = { callback ->
                        //set local refresh state to true
                        refreshState.value = true
                        //then call higher callback
                        //when that callback is completed set local
                        //refresh state to false
                        refreshCallback {
                            refreshState.value = false
                        }

                        callback()
                    },
                )
            )
    ) {
        //refresh indication icon
        Icon(
            imageVector= iconImage,
            contentDescription = "refresh",
            modifier = Modifier
                .offset(
                    x = (maxWidth - iconSize) / 2,
                    y = iconPosY.value
                )
                .background(iconBackgroundColor, iconBackgroundShape)
                .height(iconSize)
                .width(iconSize)
                .zIndex(iconZIndex)
                .rotate(
                    //if refreshing set loading rotation animation
                    degrees = if (refreshState.value) {
                        iconRotate.value
                    } else { //if not refreshing position is based on scrolling
                        abs(iconPosY.value / threshold) * 720f

                    }
                ),
            tint = iconColor
        )

        content()
    }
}
package com.thewire.nested_scroll.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun RefreshContainer(
    modifier: Modifier = Modifier,
    refreshState: MutableState<Boolean>,
    iconImage: ImageVector = Icons.Filled.Refresh,
    iconBackgroundShape: Shape = CircleShape,
    iconColor: Color = Color.Cyan,
    iconBackgroundColor: Color = Color.DarkGray,
    threshold: Float = 150f,
    iconSize: Dp = 25.dp,
    initialIconPosY: Dp = -iconSize,
    iconLoadPos: Dp = 15.dp,
    refreshCallback: (MutableState<Boolean>) -> Unit,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {

    //icon position set by scrolling
    val iconPos = remember {
        mutableStateOf(initialIconPosY)
    }


    //icon position set by either scroll or animation
    val iconPosY = animateDpAsState(
        animationSpec = tween(
            durationMillis = 250,
            easing = LinearEasing
        ),
        //if there is no fresh i.e. threshold not met then
        //set icon position to scroll position
        targetValue = if(!refreshState.value) {
            iconPos.value
        } else { //if threshold met set icon to loading pos
            iconLoadPos
        },
        finishedListener = {
            //after refresh
            //when done animating loading pos send icon back to its initial position
            if(refreshState.value) {
                iconPos.value = initialIconPosY
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
                    threshold = threshold,
                    iconPos = iconPos,
                    refreshState = refreshState,
                    refreshCallback = refreshCallback,
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
                .rotate(
                    //if refreshing set loading rotation animation
                    degrees = if (refreshState.value) {
                        iconRotate.value
                    } else { //if not refreshing position is based on scrolling
                        abs(iconPosY.value / threshold.dp) * 720f

                    }
                ),
            tint = iconColor
        )

        content()
    }
}
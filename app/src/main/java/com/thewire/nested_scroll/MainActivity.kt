package com.thewire.nested_scroll

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.thewire.nested_scroll.ui.theme.Nested_ScrollTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Nested_ScrollTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ListWithRefresh()
                }
            }
        }
    }
}

//simulated refresh/reload
fun doSimulatedRefresh(refreshState: MutableState<Boolean>) {
    CoroutineScope(Dispatchers.Main).launch {
        refreshState.value = true
        delay(2000)
        refreshState.value = false
    }
}

//test composable with lazy column list
@Composable
fun ListWithRefresh() {

    val refreshState = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = if(refreshState.value) {
                    5.dp
                } else {
                    0.dp
                }, color = Color.Red
            )
    ) {

        RefreshContainer(
            modifier = Modifier.fillMaxSize(),
            refreshState = refreshState,
            refreshCallback = ::doSimulatedRefresh
        ) {
            //example list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp
                )
            ) {
                items(100) { index ->
                    Text("I'm item $index")
                }
            }
        }
    }
}


//handles nested scrolling
//sets refreshState and calls refreshCallback when threshold scroll met
@Composable
fun refreshAction(
    threshold: Float,
    iconPos: MutableState<Dp>,
    refreshState: MutableState<Boolean>,
    refreshCallback: (MutableState<Boolean>) -> Unit,
) : NestedScrollConnection {

    val iOffset = remember { mutableStateOf(0f)}
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//                Log.d("SCROLL", "pre scroll ${available.y}")
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
//                Log.d("SCROLL", "pre fling ${available.y}")
                return super.onPreFling(available)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
//                Log.d("SCROLL", "post scroll consumed: ${consumed.y} available: ${available.y}")
                //if scrolling up and there is unconsumed scroll available i.e. we are scroll as
                //much as possible
                if (available.y > 0) {

                    iOffset.value += available.y
                    //prevent icon moving bellow threshold point
                    if (iOffset.value < threshold) {
                        //set icon position to available scroll
                        iconPos.value = available.y.dp
                    }

                }
                return super.onPostScroll(consumed, available, source)
            }

            //refresh only happens on fling i.e. when scroll is released
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
//                Log.d("SCROLL", "post fling consumed: ${consumed.y} available: ${available.y}")

                //if scrolling threshold met do refresh
                if(iOffset.value >= threshold) {
                    refreshState.value = true
                    refreshCallback(refreshState)
                }
                //reset offset whether threshold was met or not
                iOffset.value = 0f
                return super.onPostFling(consumed, available)
            }

        }
    }
    return nestedScrollConnection
}



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
package com.thewire.nested_scroll

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

fun refreshAction(r: MutableState<Boolean>) {
    CoroutineScope(Dispatchers.Main).launch {
        r.value = true;
        delay(2000)
        r.value = false
    }
}


@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun Greeting(name: String) {

    val iconSize = 25.dp
    val iconInitial = -iconSize
    val threshold = 150f
    val iOffset = remember { mutableStateOf(0f)}
    val iconPos = remember {
        mutableStateOf(iconInitial)
    }

    val iconLoad = 5.dp

    val iconState = remember { mutableStateOf(false) }

    val trans = updateTransition(targetState = iconState)
    val transition = updateTransition(targetState = iconState, label = "derp")
    //can actually do animate offset need to change stuff
    val iconPosY = animateDpAsState(
        animationSpec = tween(
            durationMillis = 250,
            easing = LinearEasing
        ),
        targetValue = if(!iconState.value) {
            iconPos.value
        } else {
            iconLoad
        },
        finishedListener = {
            if(iconState.value) {
                iconPos.value = iconInitial
//                iconState.value = false
            }
        }
    )


    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                Log.d("SCROLL", "pre scroll")
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
//                Log.d("SCROLL", "pre fling")
                return super.onPreFling(available)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
//                Log.d("SCROLL", "post scroll")
                if (available.y > 0) {
                    iOffset.value += available.y
                    if (iOffset.value < threshold) {
                        iconPos.value = available.y.dp
                    }

                }
                return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                Log.d("SCROLL", "post fling")
                if(iOffset.value >= threshold) {
                    refreshAction(iconState)
                }
                iOffset.value = 0f
                return super.onPostFling(consumed, available)
            }

        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .border(
                width = if (iconState.value) {
                    5.dp
                } else {
                    0.dp
                }, color = Color.Red
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Icon(
                imageVector= Icons.Filled.Refresh,
                contentDescription = "refresh",
                modifier = Modifier
                    .offset(
                        x = (maxWidth - iconSize) / 2,
                        y = iconPosY.value
                    )
                    .height(iconSize)
                    .width(iconSize)
                    .rotate(abs(threshold.dp / iconPosY.value) * 360),
                tint = Color.Blue

            )
//            Spacer(
//                modifier = Modifier
//                    .offset(
//                        x = (maxWidth - 50.dp) / 2,
//                        y = iconPosY.value
//                    )
//                    .background(color = Color.Red, shape = CircleShape)
//                    .height(50.dp)
//                    .width(50.dp)
//
//
//            )
        }
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Nested_ScrollTheme {
        Greeting("Android")
    }
}
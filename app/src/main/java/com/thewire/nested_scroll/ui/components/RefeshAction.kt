package com.thewire.nested_scroll.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

//handles nested scrolling
//sets refreshState and calls refreshCallback when threshold scroll met
@Composable
fun refreshAction(
    threshold: Float,
    iconPos: MutableState<Dp>,
    iconState: MutableState<Boolean>,
    refreshCallback: (() -> Unit) -> Unit,
) : NestedScrollConnection {

    val refreshState = remember {
        mutableStateOf(false)
    }

    val iOffset = remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//                Log.d("SCROLL", "pre scroll ${available.y}")
                //when scrolling is happening
                iconState.value = true
                return Offset.Zero
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

            override suspend fun onPreFling(available: Velocity): Velocity {
//                Log.d("SCROLL", "pre fling ${available.y}")
                return super.onPreFling(available)
            }

            //refresh only happens on fling i.e. when scroll is released
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
//                Log.d("SCROLL", "post fling consumed: ${consumed.y} available: ${available.y}")

                //if scrolling threshold met do refresh
                if(iOffset.value >= threshold) {
                    refreshState.value = true
                    refreshCallback {
                        refreshState.value = false
                    }
                }
                //scrolling of icon finished
                iconState.value = false
                //reset offset whether threshold was met or not
                iOffset.value = 0f
                return super.onPostFling(consumed, available)
            }

        }
    }
    return nestedScrollConnection
}
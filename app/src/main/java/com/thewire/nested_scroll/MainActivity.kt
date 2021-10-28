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
import com.thewire.nested_scroll.ui.components.RefreshContainer
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
fun doSimulatedRefresh(refreshState: MutableState<Boolean>, callback: () -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        refreshState.value = true
        delay(2000)
        refreshState.value = false
        //callback when done refreshing so drag to refresh now to reset
        callback()
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
            refreshCallback = { callback ->
                doSimulatedRefresh(refreshState, callback)
            }
        ) {
            //example list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp)
            ) {
                items(100) { index ->
                    Text("I'm item $index")
                }
            }
        }
    }
}
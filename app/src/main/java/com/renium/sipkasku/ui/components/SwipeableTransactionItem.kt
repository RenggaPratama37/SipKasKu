package com.renium.sipkasku.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.local.TransactionEntity
import kotlin.math.roundToInt
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width

@Composable
fun SwipeableTransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    pocketName: String? = null
) {

    var offsetX by remember {
        mutableFloatStateOf(0f)
    }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio =
                Spring.DampingRatioMediumBouncy,

            stiffness =
                Spring.StiffnessLow
        ),
        label = ""
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // BACKGROUND DELETE
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(90.dp)
                .align(Alignment.CenterEnd)
                .clip(MaterialTheme.shapes.medium)
                .background(Color.Red)
                .padding(end=2.dp),

            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = {
                    onDelete()
                },
                modifier = Modifier
                    .padding(end = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }

        // DRAGGABLE CARD
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        animatedOffsetX.roundToInt(),
                        0
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = {
                                _,
                                dragAmount ->
                            offsetX += dragAmount
                            if (offsetX > 0f) {
                                offsetX = 0f
                            }
                            if (offsetX < -200f) {
                                offsetX = -200f
                            }
                        },
                        onDragEnd = {
                            offsetX =
                                if (offsetX < -85f) {
                                    -150f
                                } else {
                                    0f
                                }
                        }
                    )
                }
        ) {
                TransactionItem(
                    transaction = transaction,
                    pocketName = pocketName
                )
        }
    }
}
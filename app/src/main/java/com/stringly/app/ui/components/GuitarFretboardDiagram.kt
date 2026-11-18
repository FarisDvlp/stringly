package com.stringly.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Draws a horizontal left-to-right guitar fretboard mirroring a player's POV while playing:
 * - Nut on the left, frets 1..5 progressing left to right
 * - Strings stacked top-to-bottom: E (low, closest to player) -> A -> D -> G -> B -> e (high)
 * [frets] is in standard E-A-D-G-B-e order: -1 = muted (X), 0 = open (O), 1..5 = fretted dot.
 */
@Composable
fun GuitarFretboardDiagram(
    frets: List<Int>,
    modifier: Modifier = Modifier,
    showStringLabels: Boolean = true
) {
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val nutColor = MaterialTheme.colorScheme.onSurface
    val dotColor = MaterialTheme.colorScheme.primary
    val innerDotColor = MaterialTheme.colorScheme.onPrimary

    val fretCount = maxOf(4, frets.maxOrNull() ?: 4).coerceAtMost(5)
    // Display order top-to-bottom: E (index 0) -> A (1) -> D (2) -> G (3) -> B (4) -> e (5)
    val displayStrings = listOf("E", "A", "D", "G", "B", "e")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showStringLabels) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    displayStrings.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val nutX = 22.dp.toPx()
                val rightPadding = 8.dp.toPx()
                val topPadding = 10.dp.toPx()
                val bottomPadding = 10.dp.toPx()

                val boardWidth = size.width - nutX - rightPadding
                val boardHeight = size.height - topPadding - bottomPadding
                val stringSpacing = boardHeight / 5f
                val fretSpacing = boardWidth / fretCount

                // Thick nut on the left
                drawLine(
                    color = nutColor,
                    start = Offset(nutX, topPadding),
                    end = Offset(nutX, topPadding + boardHeight),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Vertical fret bars progressing left to right
                for (f in 1..fretCount) {
                    val x = nutX + f * fretSpacing
                    drawLine(
                        color = lineColor,
                        start = Offset(x, topPadding),
                        end = Offset(x, topPadding + boardHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // 6 horizontal string lines (thickest 'E' at top, thinnest 'e' at bottom)
                for (row in 0 until 6) {
                    val y = topPadding + row * stringSpacing
                    val strokeW = (2.3f - row * 0.24f).dp.toPx()
                    drawLine(
                        color = lineColor,
                        start = Offset(nutX, y),
                        end = Offset(nutX + boardWidth, y),
                        strokeWidth = strokeW
                    )
                }

                // Draw open (O), muted (X), and fretted finger dots
                // Map frets[0..5] (E, A, D, G, B, e) directly to row = s (E at top row 0, e at bottom row 5)
                frets.take(6).forEachIndexed { s, fret ->
                    val row = s
                    val y = topPadding + row * stringSpacing
                    when {
                        fret < 0 -> {
                            val markerX = nutX - 11.dp.toPx()
                            val r = 3.5.dp.toPx()
                            drawLine(
                                color = nutColor.copy(alpha = 0.75f),
                                start = Offset(markerX - r, y - r),
                                end = Offset(markerX + r, y + r),
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = nutColor.copy(alpha = 0.75f),
                                start = Offset(markerX + r, y - r),
                                end = Offset(markerX - r, y + r),
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        fret == 0 -> {
                            val markerX = nutX - 11.dp.toPx()
                            drawCircle(
                                color = nutColor.copy(alpha = 0.75f),
                                radius = 4.dp.toPx(),
                                center = Offset(markerX, y),
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                        }
                        fret in 1..fretCount -> {
                            val dotX = nutX + (fret - 0.5f) * fretSpacing
                            drawCircle(
                                color = dotColor,
                                radius = 8.dp.toPx(),
                                center = Offset(dotX, y)
                            )
                            drawCircle(
                                color = innerDotColor,
                                radius = 2.5.dp.toPx(),
                                center = Offset(dotX, y)
                            )
                        }
                    }
                }
            }
        }

        // Fret numbers along the bottom (left to right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (showStringLabels) 38.dp else 22.dp, end = 8.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (f in 1..fretCount) {
                Text(
                    text = "$f",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

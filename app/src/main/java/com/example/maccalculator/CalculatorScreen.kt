package com.example.maccalculator

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    calculatorViewModel: CalculatorViewModel = viewModel(),
) {
    val uiState by calculatorViewModel.state.collectAsState()



    Column(modifier = modifier
        .width(340.dp)
        .background(Color(0xff1e1e1e), shape = RoundedCornerShape(16.dp))
        .padding(8.dp, 16.dp)) {
        // Your existing display and traffic lights
        MacOSTrafficLightsWithHover()
        NumberDisplay(
            display = uiState.expression.joinToString(""),
            lastEvaluation = uiState.expressionAfterCompute,
            modifier = Modifier.padding(horizontal = 8.dp))
        ButtonGrid(
            Modifier.padding(horizontal = 16.dp),
            uiState = uiState,
            onAction = calculatorViewModel::onAction,
        )
        }

        // You would continue adding more Rows for the other buttons (4, 5, 6, etc.)
    }



@Composable
fun NumberButton(display: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CalculatorButton(
        onClick = onClick,
        color = Color(0xFF464647),
        modifier = modifier,
        content = {
            Text(
                text = display,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
        }
    )
}

@Composable
fun FunctionButton(
    @DrawableRes drawableRes: Int, // Use @DrawableRes for the local resource ID
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CalculatorButton(
        onClick = onClick,
        color = Color(0xFF727272),
        modifier = modifier,
        content = {
            // Coil's AsyncImage can load a drawable resource ID directly
            Icon(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(36.dp), // Adjust size as needed
                tint = Color.White
            )
        }
    )
}



@Composable
fun ButtonGrid(
    modifier: Modifier = Modifier,
    uiState: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
) {
    val rowPadding = 2.dp
    Column(modifier = modifier) {
        // each row contains four buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = rowPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FunctionButton(R.drawable.delete_icon, "Delete", {
                onAction(CalculatorAction.Delete)
            })
            val clearIcon = if (uiState.ifShowAllClear) R.drawable.ac else R.drawable.c
            FunctionButton(clearIcon, "Clear", {
                onAction(CalculatorAction.Clear)
            })
            FunctionButton(R.drawable.percentage, "%", {
                onAction(CalculatorAction.Percentage)
            })
            OperatorButton("÷", { onAction(CalculatorAction.Operation(
                CalculatorOperation.Divide))})
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = rowPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberButton("7", onClick =
                {onAction(CalculatorAction.Number(7))})
            NumberButton("8", onClick = {onAction(CalculatorAction.Number(8))})
            NumberButton("9", onClick = {onAction(CalculatorAction.Number(9))})
            OperatorButton("×", { onAction(CalculatorAction.Operation(
                CalculatorOperation.Multiply))})
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = rowPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberButton("4", onClick = {onAction(CalculatorAction.Number(4))})
            NumberButton("5", onClick = {onAction(CalculatorAction.Number(5))})
            NumberButton("6", onClick = {onAction(CalculatorAction.Number(6))})
            OperatorButton("-", { onAction(CalculatorAction.Operation(
                CalculatorOperation.Subtract))})
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = rowPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumberButton("1", onClick = {onAction(CalculatorAction.Number(1))})
            NumberButton("2", onClick = {onAction(CalculatorAction.Number(2))})
            NumberButton("3", onClick = {onAction(CalculatorAction.Number(3))})
            OperatorButton("+", { onAction(CalculatorAction.Operation(
                CalculatorOperation.Add))})
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = rowPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FunctionButton(R.drawable.toggle_sign, "±", onClick = { onAction(CalculatorAction.ToggleSign)})
            NumberButton("0", onClick = {onAction(CalculatorAction.Number(0))})
            OperatorButton(".", { onAction(CalculatorAction.Decimal)})
            OperatorButton("=", { onAction(CalculatorAction.Calculate)})
        }
    }
}



@Composable
fun OperatorButton(
    display: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CalculatorButton(
        onClick = onClick,
        color = Color(0xFFff9200),
        modifier = modifier,
        content = {
            // Coil's AsyncImage can load a drawable resource ID directly
            Text(
                text = display,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
                )
        }
    )
}

@Composable
fun CalculatorButton(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    // 1. 修改 content lambda，使其接受一个 Modifier 参数
    content: @Composable () -> Unit
) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.size(70.dp)

        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f),// 示例尺寸,
                colors = ButtonDefaults.buttonColors(containerColor = color)

            ) {  }
            content()
        }
}

@Composable
fun NumberDisplay(display: String,
                  lastEvaluation: String,
                  modifier: Modifier = Modifier) {
    // This Text will display the current calculator value.
    Column(
        modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = lastEvaluation,
            color = Color.Gray,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.End,
            modifier = modifier
                .fillMaxWidth()
        )

        Text(
            text = display,
            color = Color.White,
            modifier = modifier
                .fillMaxWidth(), // Make it fill the width
            style = MaterialTheme.typography.displayLarge, // Use a large font
            textAlign = TextAlign.End // Right-align the text
        )
    }
}

@Composable
fun MacOSTrafficLightsWithHover() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Red (Close) - Icon: X
        TrafficLightButtonWithHover(
            color = Color(0xFFFC605C),
            iconType = MacIconType.Close
        )
        Spacer(Modifier.size(8.dp))

        // Yellow (Minimize) - Icon: Minus
        TrafficLightButtonWithHover(
            color = Color(0xFFFDBC40),
            iconType = MacIconType.Minimize
        )
        Spacer(Modifier.size(8.dp))

        // Green (Maximize/Full Screen) - Icon: Plus
        TrafficLightButtonWithHover(
            color = Color(0xFF474b4d),
            iconType = MacIconType.Maximize
        )
    }
}

// --- Enum for Icon Type ---

enum class MacIconType {
    Close, Minimize, Maximize
}

// --- Traffic Light Button Composable ---

@Composable
fun TrafficLightButtonWithHover(color: Color, iconType: MacIconType) {
    val size = 12.dp
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .hoverable(interactionSource = interactionSource), // Key step: Detects hover events
        contentAlignment = Alignment.Center
    ) {
        if (isHovered) {
            // Only show the icon when the button is hovered
            MacButtonIcon(iconType = iconType)
        }
    }
}

// --- Icon Drawing Composable ---

@Composable
fun MacButtonIcon(iconType: MacIconType) {
    val iconColor = Color(0xAA000000) // Semi-transparent black for the icon
    val strokeWidth = 1.5.dp

    Canvas(modifier = Modifier.size(7.dp)) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        when (iconType) {
            MacIconType.Close -> {
                // Draw an 'X'
                drawLine(
                    color = iconColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = iconColor,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
            MacIconType.Minimize -> {
                // Draw a '-'
                drawLine(
                    color = iconColor,
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
            MacIconType.Maximize -> {
                // Draw a '+'
                drawLine(
                    color = iconColor,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = iconColor,
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFffffff)
@Composable()
fun PanelPreview(modifier: Modifier = Modifier.width(340.dp)) {
//    Panel(Modifier)
    CalculatorScreen(modifier.padding(0.dp))
}

@Preview(showBackground = true)
@Composable
fun ButtonGridPreview(modifier: Modifier = Modifier) {
    ButtonGrid(
        modifier = modifier,
        uiState = CalculatorState(),
        onAction = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFffffff)
@Composable
fun NumberDisplayPreview(modifier: Modifier = Modifier) {
    NumberDisplay("1 + 1", lastEvaluation = "2")
}
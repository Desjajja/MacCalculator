package com.example.maccalculator

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.IllegalArgumentException

// Sealed class to represent all possible calculator actions.
sealed class CalculatorAction {
    data class Number(val value: Int) : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    object ToggleSign: CalculatorAction()
    object Percentage: CalculatorAction()
}

// Sealed class for the different types of operations.
sealed class CalculatorOperation(val symbol: String) {
    object Add : CalculatorOperation("+")
    object Subtract : CalculatorOperation("-")
    object Multiply : CalculatorOperation("×")
    object Divide : CalculatorOperation("÷")
}

data class CalculatorState(
    val expression: List<String> = listOf("0"),
    val expressionAfterCompute: String = "",
    val ifShowAllClear: Boolean = true,
    val ifShowHistory: Boolean = false,
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    private fun List<String>.isLastElementAnOperator(): Boolean {
        val last = this.lastOrNull() ?: return false
        // You can get these symbols from your CalculatorOperation sealed class for better safety
        return last in listOf("+", "-", "×", "÷")
    }

    fun clearHistory() {
        _historyItems.value = emptyList()
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.value.toString())
            is CalculatorAction.Operation -> enterOperation(action.operation.symbol)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Delete -> performDelete()
            is CalculatorAction.Clear -> performClear()
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.ToggleSign -> performToggleSign()
            is CalculatorAction.Percentage -> performPercentage()
        }
    }

    fun toggleHistory() {
        _uiState.value = _uiState.value.copy(ifShowHistory = !_uiState.value.ifShowHistory)
        Log.d("desjajja", "toggleHistory: ${_uiState.value.ifShowHistory}")
    }

    private fun enterNumber(number: String) {
        if (_uiState.value.expressionAfterCompute.isNotEmpty()) {
            _uiState.value = CalculatorState()
        }

        val currentList = _uiState.value.expression.toMutableList()
        if (currentList.size == 1 && currentList[0] == "0") {
            currentList[0] = number
            _uiState.value = _uiState.value.copy(expression = currentList, ifShowAllClear = false)
            return
        }

        // Use our new helper function for clarity
        if (!currentList.isLastElementAnOperator()) {
            val lastElement = currentList.last()
            val newNumber = lastElement + number
            currentList[currentList.size - 1] = newNumber
            _uiState.value = _uiState.value.copy(expression = currentList, ifShowAllClear = false)
        } else {
            currentList.add(number)
            _uiState.value = _uiState.value.copy(expression = currentList, ifShowAllClear = false)
        }
    }

    private fun enterOperation(symbol: String) {
        if (_uiState.value.expressionAfterCompute.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(expressionAfterCompute = "")
        }
        val currentList = _uiState.value.expression.toMutableList()
        if (currentList.isEmpty() || (currentList.size == 1 && currentList[0] == "0")) {
            currentList.clear()
            currentList.add("0")
            currentList.add(symbol)
            _uiState.value = _uiState.value.copy(expression = currentList) // No lastInputNumber
            return
        }

        // Use our helper function again
        if (currentList.isLastElementAnOperator()) {
            currentList[currentList.size - 1] = symbol
        } else {
            currentList.add(symbol)
        }
        // No need to set lastInputNumber to "SYMBOL"
        _uiState.value = _uiState.value.copy(expression = currentList)
    }

    private fun enterDecimal() {
        // Case 1: Start a new expression after a calculation (=) was pressed.
        if (_uiState.value.expressionAfterCompute.isNotEmpty()) {
            _uiState.value = CalculatorState(expression = listOf("0."))
            return
        }

        val currentList = _uiState.value.expression.toMutableList()

        // We can use the helper function here too for consistency.
        // `!isLastElementAnOperator` covers the case where the last element is a number.
        if (!currentList.isLastElementAnOperator()) {
            val lastNumber = currentList.last()
            // Only add a decimal if one doesn't already exist.
            if (!lastNumber.contains(".")) {
                currentList[currentList.size - 1] = "$lastNumber."
                _uiState.value = _uiState.value.copy(expression = currentList)
            }
        } else {
            // Case 2: The last input was an operator, so start a new number.
            currentList.add("0.")
            _uiState.value = _uiState.value.copy(expression = currentList)
        }
    }

    private fun performDelete() {
        val currentList = _uiState.value.expression.toMutableList()
        if (currentList.size == 1 && currentList[0] == "0") return

        val lastItem = currentList.last()
        if (lastItem.length > 1) {
            currentList[currentList.size - 1] = lastItem.dropLast(1)
        } else {
            currentList.removeAt(currentList.size - 1)
        }

        if (currentList.isEmpty()) {
            _uiState.value = CalculatorState()
        } else {
            _uiState.value = _uiState.value.copy(expression = currentList)
        }
    }

    private fun performClear() {
        // 1. If the button is already in "All Clear" mode, reset everything and stop.
        if (_uiState.value.ifShowAllClear) {
            _uiState.value = CalculatorState()
            return // IMPORTANT: Exit the function here.
        }

        // 2. This is the "Clear Entry" part (the first press).
        val currentExpression = _uiState.value.expression
        var newExpression = currentExpression

        // Only remove the last element if it's a number.
        if (!currentExpression.isLastElementAnOperator()) {
            // dropLast(1) creates the new list.
            // ifEmpty ensures we always show "0" instead of a blank screen.
            newExpression = currentExpression.dropLast(1).ifEmpty { listOf("0") }
        }

        // 3. Update the state: use the new expression and set the button to "All Clear" for the next press.
        _uiState.value = _uiState.value.copy(
            expression = newExpression,
            ifShowAllClear = true
        )
    }

    private fun performCalculation() {
        val expressionString = _uiState.value.expression.joinToString("")
        if (expressionString == "Error") return
        try {
            val result = evaluateExpression(expressionString)
            val resultString = if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }
            _uiState.value = _uiState.value.copy(
                expression = listOf(resultString),
                expressionAfterCompute = expressionString,
                ifShowAllClear = true
            )
            _historyItems.update { currentList ->
                listOf(HistoryItem(expressionString, resultString)) + currentList
            }
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(expression = listOf("Error"))
            _historyItems.update { currentList ->
                listOf(HistoryItem(expressionString, "Error")) + currentList
            }
        }
    }

    private fun evaluateExpression(expression: String): Double {
        val expr = expression.replace('×', '*').replace('÷', '/')

        val numbers = Regex("-?\\d+(\\.\\d+)?%?")
            .findAll(expr)
            .mapNotNull { match ->
                val text = match.value
                when {
                    text.endsWith('%') -> {
                        text.removeSuffix("%").toDoubleOrNull()?.div(100)
                    }
                    else -> text.toDoubleOrNull()
                }
            }
            .toMutableList()


        val operators = expr.filter { it in "+-*/" }.toMutableList()

        if (expr.startsWith('-')) {
            if (numbers.isNotEmpty()) {
                operators.removeAt(0)
            }
        }

        if (numbers.isEmpty()) return 0.0
        var result = numbers.first()
        numbers.removeAt(0)

        while (numbers.isNotEmpty() && operators.isNotEmpty()) {
            val operator = operators.first()
            val nextNumber = numbers.first()
            operators.removeAt(0)
            numbers.removeAt(0)

            result = when (operator) {
                '+' -> result + nextNumber
                '-' -> result - nextNumber
                '*' -> result * nextNumber
                '/' -> {
                    if (nextNumber == 0.0) throw ArithmeticException("Division by zero")
                    result / nextNumber
                }
                else -> throw IllegalArgumentException("Unknown operator")
            }
        }
        return result
    }

    private fun performToggleSign() {
        val currentList = _uiState.value.expression.toMutableList()
        val lastElement = currentList.lastOrNull()
        val elementBeforeLastElement = currentList.getOrNull(currentList.size - 2)

        if (lastElement != null && lastElement !in listOf("+", "-", "×", "÷")) {
            val number = lastElement.toDoubleOrNull()
            if (number != 0.0) {
                val newNumber = if (lastElement.startsWith('-')) {
                    lastElement.substring(1)
                } else if (lastElement.startsWith("(-")) {
                    lastElement.substring(2, lastElement.length - 1)
                } else if (elementBeforeLastElement in listOf("+", "-", "×", "÷")) {
                    "(-$lastElement)"
                } else {
                    "-$lastElement"
                }
                currentList[currentList.size - 1] = newNumber
                _uiState.value = _uiState.value.copy(expression = currentList)
            }
        }
    }

    private fun performPercentage() {
        val currentList = _uiState.value.expression.toMutableList()
        val lastElement = currentList.lastOrNull()

        if (lastElement != null && lastElement !in listOf("+", "-", "×", "÷")) {
            val resultString = "$lastElement%"
            currentList[currentList.size - 1] = resultString
            _uiState.value = _uiState.value.copy(expression = currentList)
        }
    }
}
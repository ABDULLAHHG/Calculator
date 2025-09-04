package com.aboud.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.aboud.calculator.databinding.ActivityMainBinding
import com.aboud.calculator.utils.tokenize

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.clear.setOnClickListener { onClearInput() }
        binding.plus.setOnClickListener { addOperation(" + ") }
        binding.minus.setOnClickListener { addOperation(" - ") }
        binding.devide.setOnClickListener { addOperation(" ÷ ") }
        binding.reminder.setOnClickListener { addOperation(" % ") }
        binding.times.setOnClickListener { addOperation(" × ") }
        binding.equal.setOnClickListener { calculateResult() }
        binding.minusPlus.setOnClickListener { toggleSign() }
        binding.delete.setOnClickListener { onDelete() }
    }

    private fun onClearInput() {
        binding.inputDigit.text = "0"
        binding.previousEquation.text = ""
    }

    private fun calculateResult() {
        try {
            binding.previousEquation.text = binding.inputDigit.text
            val result = calculate(binding.inputDigit.text.toString())
            binding.inputDigit.text = formatNumber(result)

        } catch (e: ArithmeticException) {
            binding.inputDigit.text = "Error Division by zero"
        } catch (e: Exception) {
            binding.inputDigit.text = "Error"
        }
    }

    private fun calculate(expression: String): Double {
        if (expression.isEmpty() || expression == "0") return 0.0

        val normalized = expression.replace("×", "*").replace("÷", "/")
        val tokens = tokenize(normalized)

        return evaluateExpression(tokens)
    }

    private fun evaluateExpression(tokens: List<String>): Double {
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<String>()

        tokens.forEach { token ->
            if (token.matches(Regex("[+\\-*/%]"))) {
                operators.add(token)
            } else {
                numbers.add(token.toDouble())
            }
        }

        processOperators(numbers, operators, listOf("*", "/", "%"))
        processOperators(numbers, operators, listOf("+", "-"))
        return numbers.firstOrNull() ?: 0.0
    }

    private fun toggleSign() {
        val currentText = binding.inputDigit.text.toString()
        if (currentText.isEmpty() || currentText == "0" || currentText == "Error") {
            return
        }

        val lastOperatorIndex = currentText.indexOfLast { it.isOperator() }

        if (lastOperatorIndex == -1) {
            val number = currentText.toDoubleOrNull()
            if (number != null) {
                binding.inputDigit.text = formatNumber(-number)
            }
        } else {
            val beforeOperator = currentText.substring(0, lastOperatorIndex + 1)
            val currentNumber = currentText.substring(lastOperatorIndex + 1)

            if (currentNumber.isNotEmpty()) {
                val number = currentNumber.toDoubleOrNull()
                if (number != null) {
                    val toggledNumber = formatNumber(-number)
                    val result = if (toggledNumber.startsWith("-")) {
                        toggledNumber.replace("-", "")
                    } else {
                        beforeOperator + toggledNumber
                    }
                    binding.inputDigit.text = result
                }
            }
        }
    }

    private fun onDelete() {
        val oldDigit = binding.inputDigit.text.toString()
        binding.inputDigit.text = if (oldDigit.isNotEmpty()) {
            val newDigit = oldDigit.dropLast(n = 1)
            newDigit.ifEmpty { "0" }
        } else {
            "0"
        }
    }


    private fun processOperators(
        numbers: MutableList<Double>,
        operators: MutableList<String>,
        ops: List<String>
    ) {
        var i = 0
        while (i < operators.size) {
            if (operators[i] in ops) {
                val result = when (operators[i]) {
                    "*" -> numbers[i] * numbers[i + 1]
                    "/" -> {
                        if (numbers[i + 1] == 0.0) throw ArithmeticException("Division by zero")
                        numbers[i] / numbers[i + 1]
                    }

                    "%" -> numbers[i] * (numbers[i + 1] / 100)
                    "+" -> numbers[i] + numbers[i + 1]
                    "-" -> numbers[i] - numbers[i + 1]
                    else -> throw IllegalArgumentException("Unknown operator")
                }

                numbers[i] = result
                numbers.removeAt(i + 1)
                operators.removeAt(i)
            } else {
                i++
            }
        }
    }

    private fun formatNumber(result: Double): String {
        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            result.toString().trimEnd('0').trimEnd('.')
        }
    }


    private fun currentNumberHasDecimal(text: String): Boolean {
        val lastOperatorIndex = text.indexOfLast { it.isOperator() }
        val currentNumber = text.substring(lastOperatorIndex + 1)
        return currentNumber.contains(".")
    }

    fun onClickNumber(v: View) {
        val newDigit = (v as Button).text.toString()
        val currentText = binding.inputDigit.text.toString()

        binding.inputDigit.text = when {
            currentText == "0" && newDigit != "." -> newDigit
            currentText == "0" && newDigit == "." -> "0."
            newDigit == "." && currentNumberHasDecimal(currentText) -> currentText
            else -> currentText + newDigit
        }
    }

    private fun addOperation(symbol: String) {
        val currentText = binding.inputDigit.text.toString()
        if (currentText.isNotEmpty() && !currentText.last().isOperator()) {
            binding.inputDigit.text = currentText + symbol
        }
    }

    private fun Char.isOperator() = this in "+-×÷%"

}


package com.aboud.calculator.utils

fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentToken = ""

        for (i in expression.indices) {
            val char = expression[i]

            when {
                char.isDigit() || char == '.' -> {
                    currentToken += char
                }

                char in "+-*/%" -> {
                    if (char == '-' && (i == 0 || expression[i - 1] in "+-*/%")) {
                        currentToken += char
                    } else {
                        if (currentToken.isNotEmpty()) {
                            tokens.add(currentToken)
                            currentToken = ""
                        }
                        tokens.add(char.toString())
                    }
                }
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken)
        }

        return tokens.filter { it.isNotEmpty() }
    }
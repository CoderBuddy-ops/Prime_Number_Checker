package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PrimeCheck
import com.example.data.repository.PrimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import kotlin.system.measureTimeMillis

sealed interface PrimeCheckResult {
    object Idle : PrimeCheckResult
    object Checking : PrimeCheckResult
    data class Success(
        val number: String,
        val isPrime: Boolean,
        val resultType: String, // "PRIME", "COMPOSITE", "NEITHER"
        val explanation: String,
        val smallestFactor: String?,
        val nextPrimes: List<String>,
        val digitCount: Int,
        val isEven: Boolean,
        val durationMs: Long
    ) : PrimeCheckResult
    data class Error(val message: String) : PrimeCheckResult
}

class PrimeViewModel(private val repository: PrimeRepository) : ViewModel() {

    private val _numberInput = MutableStateFlow("")
    val numberInput: StateFlow<String> = _numberInput.asStateFlow()

    private val _checkResult = MutableStateFlow<PrimeCheckResult>(PrimeCheckResult.Idle)
    val checkResult: StateFlow<PrimeCheckResult> = _checkResult.asStateFlow()

    private val _showResultDialog = MutableStateFlow(false)
    val showResultDialog: StateFlow<Boolean> = _showResultDialog.asStateFlow()

    private val _animSpeedMultiplier = MutableStateFlow(1.0f)
    val animSpeedMultiplier: StateFlow<Float> = _animSpeedMultiplier.asStateFlow()

    val history: StateFlow<List<PrimeCheck>> = repository.allChecks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onInputChange(input: String) {
        // Only allow digits
        val filtered = input.filter { it.isDigit() }
        _numberInput.value = filtered
    }

    fun dismissResultDialog() {
        _showResultDialog.value = false
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCheckById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun runPrimeCheck(inputStr: String) {
        val trimmed = inputStr.trim()
        if (trimmed.isEmpty()) {
            _checkResult.value = PrimeCheckResult.Error("Please enter a positive integer.")
            return
        }

        viewModelScope.launch {
            _checkResult.value = PrimeCheckResult.Checking
            _animSpeedMultiplier.value = 5.0f // Speed up animation during check!
            
            // Artificial delay to make checking animation feel satisfyingly detailed
            delay(800)

            val result = withContext(Dispatchers.Default) {
                computePrimeDetails(trimmed)
            }

            _checkResult.value = result
            _animSpeedMultiplier.value = 1.0f
            _showResultDialog.value = true

            // If check was successful, save to history database
            if (result is PrimeCheckResult.Success) {
                repository.insertCheck(
                    PrimeCheck(
                        numberString = result.number,
                        isPrime = result.isPrime,
                        resultType = result.resultType,
                        explanation = result.explanation,
                        checkDurationMs = result.durationMs
                    )
                )
            }
        }
    }

    private fun computePrimeDetails(trimmed: String): PrimeCheckResult {
        var result: PrimeCheckResult = PrimeCheckResult.Idle
        val duration = measureTimeMillis {
            try {
                val n = BigInteger(trimmed)

                if (n <= BigInteger.ONE) {
                    result = PrimeCheckResult.Success(
                        number = trimmed,
                        isPrime = false,
                        resultType = "NEITHER",
                        explanation = "$trimmed is neither prime nor composite. By definition, prime numbers must be integers greater than 1.",
                        smallestFactor = null,
                        nextPrimes = emptyList(),
                        digitCount = trimmed.length,
                        isEven = n == BigInteger.ZERO || n.mod(BigInteger.valueOf(2)) == BigInteger.ZERO,
                        durationMs = 0
                    )
                    return@measureTimeMillis
                }

                val isEven = n.mod(BigInteger.valueOf(2)) == BigInteger.ZERO

                // Checking prime status
                val certainty = 40 // ~99.99999999% accurate probabilistic check for large numbers
                val isProbablyPrime = n.isProbablePrime(certainty)

                if (isProbablyPrime) {
                    // Try to generate next 2 prime numbers after this one
                    val nextPrimes = mutableListOf<String>()
                    var temp = n
                    for (i in 1..2) {
                        temp = temp.nextProbablePrime()
                        nextPrimes.add(temp.toString())
                    }

                    result = PrimeCheckResult.Success(
                        number = trimmed,
                        isPrime = true,
                        resultType = "PRIME",
                        explanation = "$trimmed is indeed a prime number! It has exactly two distinct positive divisors: 1 and itself.",
                        smallestFactor = null,
                        nextPrimes = nextPrimes,
                        digitCount = trimmed.length,
                        isEven = isEven,
                        durationMs = 0
                    )
                } else {
                    // It's composite. Find its smallest factor up to 2,000,000
                    var smallestFactor: String? = null
                    if (isEven) {
                        smallestFactor = "2"
                    } else {
                        val maxTrial = BigInteger.valueOf(1000000)
                        val sqrtN = n.sqrt()
                        val limit = if (sqrtN < maxTrial) sqrtN else maxTrial
                        
                        var i = BigInteger.valueOf(3)
                        while (i <= limit) {
                            if (n.mod(i) == BigInteger.ZERO) {
                                smallestFactor = i.toString()
                                break
                            }
                            i = i.add(BigInteger.valueOf(2))
                        }
                    }

                    val expl = if (smallestFactor != null) {
                        "$trimmed is a composite number because it is divisible by $smallestFactor."
                    } else {
                        "$trimmed is a composite number (its smallest divisor is larger than 1,000,000)."
                    }

                    result = PrimeCheckResult.Success(
                        number = trimmed,
                        isPrime = false,
                        resultType = "COMPOSITE",
                        explanation = expl,
                        smallestFactor = smallestFactor,
                        nextPrimes = emptyList(),
                        digitCount = trimmed.length,
                        isEven = isEven,
                        durationMs = 0
                    )
                }
            } catch (e: Exception) {
                result = PrimeCheckResult.Error("Calculation error: ${e.localizedMessage}")
            }
        }

        return if (result is PrimeCheckResult.Success) {
            (result as PrimeCheckResult.Success).copy(durationMs = duration)
        } else {
            result
        }
    }
}

class ViewModelFactory(private val repository: PrimeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

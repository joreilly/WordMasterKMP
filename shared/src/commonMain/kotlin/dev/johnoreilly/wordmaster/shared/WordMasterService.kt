package dev.johnoreilly.wordmaster.shared

import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.MutableStateFlow
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import dev.johnoreilly.wordmaster.shared.LetterStatus.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.StateFlow
import okio.SYSTEM


enum class LetterStatus {
    UNGUESSED, CORRECT_POSITION, INCORRECT_POSITION, NOT_IN_WORD
}


class WordMasterService(wordsFilePath: String) {
    @NativeCoroutineScope
    val coroutineScope: CoroutineScope = MainScope()

    private val validWords = mutableListOf<String>()

    var answer = ""
    var currentGuessAttempt = 0

    @NativeCoroutines
    val boardGuesses: MutableStateFlow<ArrayList<ArrayList<String>>> = MutableStateFlow<ArrayList<ArrayList<String>>>(arrayListOf())

    @NativeCoroutines
    val boardStatus: MutableStateFlow<ArrayList<ArrayList<LetterStatus>>> = MutableStateFlow<ArrayList<ArrayList<LetterStatus>>>(arrayListOf())

    @NativeCoroutines
    val revealedAnswer: MutableStateFlow<String?> = MutableStateFlow(null)

    @NativeCoroutines
    val lastGuessCorrect: MutableStateFlow<Boolean> = MutableStateFlow(false)


    init {
        println("wordsFilePath = $wordsFilePath")
        readWords(wordsFilePath.toPath())
        resetGame()
    }

    fun resetGame() {
        currentGuessAttempt = 0
        answer = validWords.random().uppercase()
        println("answer! = $answer")
        revealedAnswer.value = null
        lastGuessCorrect.value = false

        // set default values for guesses/letter status info
        val newBoardStatus = arrayListOf<ArrayList<LetterStatus>>()
        val newBoardGuesses = arrayListOf<ArrayList<String>>()

        for (guessAttempt in 0 until MAX_NUMBER_OF_GUESSES) {
            val statusList = arrayListOf<LetterStatus>()
            val guesses = arrayListOf<String>()
            for (character in 0 until NUMBER_LETTERS) {
                statusList.add(UNGUESSED)
                guesses.add("")
            }
            newBoardStatus.add(statusList)
            newBoardGuesses.add(guesses)
        }
        boardStatus.value = newBoardStatus
        boardGuesses.value = newBoardGuesses
    }

    fun setGuess(guessAttempt: Int, character: Int, guess: String) {
        // need to make deep copy to trigger MutableStateFlow to emit update
        val currentBoardGuesses = boardGuesses.value

        val newBoardGuesses = arrayListOf<ArrayList<String>>()
        for (guessAttempt in 0 until MAX_NUMBER_OF_GUESSES) {
            val guesses = ArrayList(currentBoardGuesses[guessAttempt])
            newBoardGuesses.add(guesses)
        }

        newBoardGuesses[guessAttempt][character] = guess
        boardGuesses.value = newBoardGuesses
    }

    fun checkGuess() {
        val currentGuess = boardGuesses.value[currentGuessAttempt].joinToString("")
        if (currentGuess.length == NUMBER_LETTERS) {
            val status = checkWord(currentGuess)

            val currentStatusCopy = ArrayList(boardStatus.value)
            currentStatusCopy[currentGuessAttempt] = status
            boardStatus.value = currentStatusCopy

            val isCorrect = status.all { it == CORRECT_POSITION }
            if ( isCorrect ) {
                lastGuessCorrect.value = true
            }
            currentGuessAttempt++

            // Reveal the answer if all guesses are completed and the word wasn't guessed
            if (!isCorrect && currentGuessAttempt >= MAX_NUMBER_OF_GUESSES) {
                revealedAnswer.value = answer
            }
        }
    }

    private fun checkWord(guess: String): ArrayList<LetterStatus> {
        val letterStatusList = arrayListOf(NOT_IN_WORD, NOT_IN_WORD, NOT_IN_WORD, NOT_IN_WORD, NOT_IN_WORD)

        val unusedAnswerLetters = answer.toMutableList()

        // check correct positions
        for (index in 0 until NUMBER_LETTERS) {
            val letter = guess[index]
            if (letter == answer[index]) {
                letterStatusList[index] = CORRECT_POSITION
                unusedAnswerLetters.remove(letter)
            }
        }

        // check letters in incorrect position
        for (index in 0 until NUMBER_LETTERS) {
            if (letterStatusList[index] == CORRECT_POSITION) continue

            val letter = guess[index]
            if (letter in unusedAnswerLetters) {
                letterStatusList[index] = INCORRECT_POSITION
                unusedAnswerLetters.remove(letter)
            }
        }

        return letterStatusList
    }


    private fun readWords(path: Path) {
        // an error will be shown in IDE for now until https://github.com/square/okio/pull/980
        // is resolved....but will build/run ok
        FileSystem.SYSTEM.read(path) {
            while (true) {
                val word = this.readUtf8Line() ?: break
                validWords.add(word)
            }
        }
    }

    companion object {
        const val NUMBER_LETTERS = 5
        const val MAX_NUMBER_OF_GUESSES = 6
    }
}
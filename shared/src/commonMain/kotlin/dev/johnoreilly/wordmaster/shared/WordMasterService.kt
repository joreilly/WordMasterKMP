package dev.johnoreilly.wordmaster.shared

import kotlinx.coroutines.flow.MutableStateFlow
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import dev.johnoreilly.wordmaster.shared.LetterStatus.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okio.SYSTEM


enum class LetterStatus {
    UNGUESSED, CORRECT_POSITION, INCORRECT_POSITION, NOT_IN_WORD
}


class WordMasterService(wordsFilePath: String) {
    val coroutineScope: CoroutineScope = MainScope()

    private val validWords = mutableListOf<String>()
    private val validWordsSet = mutableSetOf<String>()

    var answer = ""
    var currentGuessAttempt = 0

    val boardGuesses: MutableStateFlow<List<List<String>>> = MutableStateFlow(emptyList())

    val boardStatus: MutableStateFlow<List<List<LetterStatus>>> = MutableStateFlow(emptyList())

    val revealedAnswer: MutableStateFlow<String?> = MutableStateFlow(null)

    val lastGuessCorrect: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // Best-known status for each letter typed so far, used to colour the on-screen keyboard.
    val keyStatus: MutableStateFlow<Map<String, LetterStatus>> = MutableStateFlow(emptyMap())

    // Transient message shown to the user when a guess is rejected (e.g. too short / not a word).
    val guessError: MutableStateFlow<String?> = MutableStateFlow(null)


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
        keyStatus.value = emptyMap()
        guessError.value = null

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

    private fun isGameFinished(): Boolean =
        lastGuessCorrect.value || currentGuessAttempt >= MAX_NUMBER_OF_GUESSES

    fun isValidWord(word: String): Boolean = validWordsSet.contains(word.uppercase())

    // Append a letter to the next empty cell of the current guess row.
    fun addLetter(letter: String) {
        if (isGameFinished()) return
        val character = letter.uppercase().take(1)
        if (character.isEmpty()) return

        val row = boardGuesses.value[currentGuessAttempt]
        val column = row.indexOfFirst { it.isEmpty() }
        if (column == -1) return

        setGuess(currentGuessAttempt, column, character)
    }

    // Clear the last filled cell of the current guess row.
    fun removeLetter() {
        if (isGameFinished()) return

        val row = boardGuesses.value[currentGuessAttempt]
        val column = row.indexOfLast { it.isNotEmpty() }
        if (column == -1) return

        setGuess(currentGuessAttempt, column, "")
    }

    fun clearGuessError() {
        guessError.value = null
    }

    // Validate the current row and, if it's a legal word, evaluate it.
    fun submitGuess() {
        if (isGameFinished()) return

        val currentGuess = boardGuesses.value[currentGuessAttempt].joinToString("")
        if (currentGuess.length < NUMBER_LETTERS) {
            guessError.value = "Not enough letters"
            return
        }
        if (!isValidWord(currentGuess)) {
            guessError.value = "Not in word list"
            return
        }
        guessError.value = null
        checkGuess()
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

            updateKeyStatus(currentGuess, status)

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

    // Merge the latest guess result into the per-letter keyboard status, only ever upgrading
    // (NOT_IN_WORD -> INCORRECT_POSITION -> CORRECT_POSITION).
    private fun updateKeyStatus(guess: String, status: ArrayList<LetterStatus>) {
        val newKeyStatus = keyStatus.value.toMutableMap()
        guess.forEachIndexed { index, char ->
            val letter = char.toString()
            val newStatus = status[index]
            if (statusRank(newStatus) > statusRank(newKeyStatus[letter])) {
                newKeyStatus[letter] = newStatus
            }
        }
        keyStatus.value = newKeyStatus
    }

    private fun statusRank(status: LetterStatus?): Int = when (status) {
        CORRECT_POSITION -> 3
        INCORRECT_POSITION -> 2
        NOT_IN_WORD -> 1
        else -> 0
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
                validWordsSet.add(word.uppercase())
            }
        }
    }

    companion object {
        const val NUMBER_LETTERS = 5
        const val MAX_NUMBER_OF_GUESSES = 6
    }
}

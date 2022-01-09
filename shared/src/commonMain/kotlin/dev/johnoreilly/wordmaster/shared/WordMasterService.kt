package dev.johnoreilly.wordmaster.shared

import kotlinx.coroutines.flow.MutableStateFlow
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use


enum class LetterStatus {
    UNGUESSED, CORRECT_POSITION, WRONG_POSITION, NOT_IN_WORD
}


class WordMasterService(wordsFilePath: String) {
    private val validWords = mutableListOf<String>()

    var answer = ""
    var currentGuessAttempt = 0
    val boardGuesses = MutableStateFlow<ArrayList<ArrayList<String>>>(arrayListOf())
    val boardStatus = MutableStateFlow<ArrayList<ArrayList<LetterStatus>>>(arrayListOf())


    init {
        println("wordsFilePath = $wordsFilePath")
        readWords(wordsFilePath.toPath())
        resetGame()
    }

    fun resetGame() {
        currentGuessAttempt = 0
        answer = validWords.random().uppercase()
        println("answer! = $answer")

        // set default values for guesses/letter status info
        val newBoardStatus = arrayListOf<ArrayList<LetterStatus>>()
        val newBoardGuesses = arrayListOf<ArrayList<String>>()

        for (guessAttempt in 0 until MAX_NUMBER_OF_GUESSES) {
            val statusList = arrayListOf<LetterStatus>()
            val guesses = arrayListOf<String>()
            for (character in 0 until NUMBER_LETTERS) {
                statusList.add(LetterStatus.UNGUESSED)
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

    fun getGuess(guessAttempt: Int, character: Int): String {
        return boardGuesses.value[guessAttempt][character]
    }

    fun getLetterStatus(guessAttempt: Int, character: Int): LetterStatus {
        return boardStatus.value[guessAttempt][character]
    }

    fun checkGuess() {
        val currentGuess = boardGuesses.value[currentGuessAttempt].joinToString("")
        if (currentGuess.length == NUMBER_LETTERS) {
            val status = checkWord(currentGuess)

            val currentStatusCopy = ArrayList(boardStatus.value)
            currentStatusCopy[currentGuessAttempt] = status
            boardStatus.value = currentStatusCopy

            currentGuessAttempt++
        }
    }

    private fun checkWord(word: String): ArrayList<LetterStatus> {
        val letterStatusList = arrayListOf<LetterStatus>()

        word.forEachIndexed { index, char ->
            val status = if (answer[index] == char) {
                LetterStatus.CORRECT_POSITION
            } else if (answer.contains(char)) {
                LetterStatus.WRONG_POSITION
            } else {
                LetterStatus.NOT_IN_WORD
            }
            letterStatusList.add(status)
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
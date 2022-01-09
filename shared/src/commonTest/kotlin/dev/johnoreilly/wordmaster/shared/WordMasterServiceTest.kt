package dev.johnoreilly.wordmaster.shared

import kotlin.test.Test
import kotlin.test.assertTrue

import dev.johnoreilly.wordmaster.shared.LetterStatus.*
import kotlin.test.assertEquals

class WordMasterServiceTest {
    private val wordMasterService = WordMasterService("../words.txt")
    private val guessAttempt = 0

    @Test
    fun testWordMasterService() {
        checkGuess(guess = "PRIME", answer = "PRIME", arrayListOf(CORRECT_POSITION, CORRECT_POSITION,
            CORRECT_POSITION, CORRECT_POSITION, CORRECT_POSITION))

        checkGuess(guess = "PERMS", answer = "PRIME", arrayListOf(CORRECT_POSITION, INCORRECT_POSITION,
            INCORRECT_POSITION, CORRECT_POSITION, NOT_IN_WORD))

        checkGuess(guess = "PERMS", answer = "PRIME", arrayListOf(CORRECT_POSITION, INCORRECT_POSITION,
            INCORRECT_POSITION, CORRECT_POSITION, NOT_IN_WORD))

        checkGuess(guess = "PEEPS", answer = "PRIME", arrayListOf(CORRECT_POSITION, INCORRECT_POSITION,
            NOT_IN_WORD, NOT_IN_WORD, NOT_IN_WORD))
    }

    private fun checkGuess(guess: String, answer: String, expectedResult: ArrayList<LetterStatus>) {
        wordMasterService.currentGuessAttempt = guessAttempt
        wordMasterService.answer = answer
        submitGuess(guess)

        val letterStatusList = wordMasterService.boardStatus.value[guessAttempt]
        assertEquals(expectedResult, letterStatusList)
    }


    private fun submitGuess(guess: String) {
        guess.forEachIndexed { index, char ->
            wordMasterService.setGuess(guessAttempt, index, char.toString())
        }
        wordMasterService.checkGuess()
    }
}
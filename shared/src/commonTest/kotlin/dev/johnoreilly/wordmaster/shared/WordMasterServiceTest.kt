package dev.johnoreilly.wordmaster.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class WordMasterServiceTest {
    // TODO provide hooks for test to provide data source for list of valid words
    private val wordMasterService = WordMasterService("../words.txt")

    @Test
    fun testGuessCheck() {
        val guessAttempt = 0

        // check initial values of board status values
        var letterStatusList = wordMasterService.boardStatus.value[guessAttempt]
        assertTrue(letterStatusList.all { it == LetterStatus.UNGUESSED })


        // initial test for guessing fully correct answer
        val guess = wordMasterService.answer
        guess.forEachIndexed { index, char ->
            wordMasterService.setGuess(guessAttempt, index, char.toString())
        }
        wordMasterService.checkGuess()

        letterStatusList = wordMasterService.boardStatus.value[guessAttempt]
        assertTrue(letterStatusList.all { it == LetterStatus.CORRECT_POSITION })
    }
}
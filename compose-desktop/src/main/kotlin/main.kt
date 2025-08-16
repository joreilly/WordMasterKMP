import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import dev.johnoreilly.wordmaster.shared.WordMasterService
import dev.johnoreilly.wordmaster.shared.LetterStatus

fun main() = singleWindowApplication(
    title = "WordMaster KMP",
    state = WindowState(size = DpSize(460.dp, 700.dp))
) {
    WordMasterView()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WordMasterView() {
    val wordMasterService = remember { WordMasterService("words.txt") }

    val boardGuesses by wordMasterService.boardGuesses.collectAsState()
    val boardStatus by wordMasterService.boardStatus.collectAsState()
    val revealedAnswer by wordMasterService.revealedAnswer.collectAsState()
    val lastGuessCorrect by wordMasterService.lastGuessCorrect.collectAsState()

    val focusManager = LocalFocusManager.current
    // FocusRequesters for every cell to precisely control focus navigation within rows
    val cellRequesters = remember { List(WordMasterService.MAX_NUMBER_OF_GUESSES) { List(WordMasterService.NUMBER_LETTERS) { FocusRequester() } } }

    // Ensure focus shifts to the first cell of the current row after a guess submission/recomposition
    val currentAttempt = wordMasterService.currentGuessAttempt
    LaunchedEffect(currentAttempt) {
        if (currentAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
            cellRequesters[currentAttempt][0].requestFocus()
        }
    }

    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.onKeyEvent {
            if (it.key == Key.Enter) {
                // Submit only if current row is fully filled
                val current = wordMasterService.currentGuessAttempt
                var filled = true
                for (c in 0 until WordMasterService.NUMBER_LETTERS) {
                    if (boardGuesses[current][c].isEmpty()) { filled = false; break }
                }
                if (filled) {
                    wordMasterService.checkGuess()
                    // Move focus explicitly to next row’s first cell
                    val nextRow = current + 1
                    if (nextRow < WordMasterService.MAX_NUMBER_OF_GUESSES) {
                        cellRequesters[nextRow][0].requestFocus()
                    }
                    true
                } else false
            } else  {
                false
            }
        }) {
            for (guessAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {

                    for (character in 0 until WordMasterService.NUMBER_LETTERS) {
                        Column(
                            Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            var modifier = Modifier
                                .padding(2.dp)
                                .width(64.dp)
                                .height(64.dp)
                            modifier = modifier.focusRequester(cellRequesters[guessAttempt][character])

                            TextField(
                                enabled = guessAttempt == wordMasterService.currentGuessAttempt,
                                value = boardGuesses[guessAttempt][character],
                                onValueChange = {
                                    if (guessAttempt == wordMasterService.currentGuessAttempt) {
                                        val capped = it.take(1).uppercase()
                                        val current = boardGuesses[guessAttempt][character]
                                        if (capped != current) {
                                            wordMasterService.setGuess(
                                                guessAttempt,
                                                character,
                                                capped
                                            )
                                            if (capped.isNotEmpty() && character < WordMasterService.NUMBER_LETTERS - 1) {
                                                // Advance within the row only
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        }
                                    }
                                },
                                modifier = modifier.border(1.dp, Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp)).onKeyEvent {
                                                                    if (it.key == Key.Backspace && guessAttempt == wordMasterService.currentGuessAttempt) {
                                                                        val currentVal = boardGuesses[guessAttempt][character]
                                                                        if (currentVal.isEmpty() && character > 0) {
                                                                            cellRequesters[guessAttempt][character - 1].requestFocus()
                                                                            true
                                                                        } else false
                                                                    } else false
                                                                },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    backgroundColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
                                    disabledTextColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    unfocusedIndicatorColor = Transparent,
                                    focusedIndicatorColor = Transparent,
                                    disabledIndicatorColor = Transparent,
                                )
                            )

                            if (guessAttempt == 0 && character == 0) {
                                DisposableEffect(Unit) {
                                    cellRequesters[0][0].requestFocus()
                                    onDispose { }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (revealedAnswer != null) {
                Text("Answer: ${'$'}revealedAnswer", style = TextStyle(fontSize = 18.sp))
                Spacer(Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    // Only submit if current row is filled
                    val current = wordMasterService.currentGuessAttempt
                    var filled = true
                    for (c in 0 until WordMasterService.NUMBER_LETTERS) {
                        if (boardGuesses[current][c].isEmpty()) { filled = false; break }
                    }
                    if (filled) {
                        wordMasterService.checkGuess()
                        // Move focus explicitly to next row’s first cell
                        val nextRow = current + 1
                        if (nextRow < WordMasterService.MAX_NUMBER_OF_GUESSES) {
                            cellRequesters[nextRow][0].requestFocus()
                        }
                    }
                }) {
                    Text("Guess")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = {
                    wordMasterService.resetGame()
                    cellRequesters[0][0].requestFocus()
                }) {
                    Text("New Game")
                }
            }

            if (lastGuessCorrect) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("You win!") },
                    text = { Text("Great job guessing the word.") },
                    confirmButton = {
                        Button(onClick = {
                            wordMasterService.resetGame()
                            cellRequesters[0][0].requestFocus()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

}

fun mapLetterStatusToBackgroundColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> White
        LetterStatus.CORRECT_POSITION -> Color(0xFF2E7D32)
        LetterStatus.INCORRECT_POSITION -> Color(0xFF9B870C)
        LetterStatus.NOT_IN_WORD -> Gray
    }
}

fun mapLetterStatusToTextColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> Black
        LetterStatus.CORRECT_POSITION -> White
        LetterStatus.INCORRECT_POSITION -> Black
        LetterStatus.NOT_IN_WORD -> White
    }
}

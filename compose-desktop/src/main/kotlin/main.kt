import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
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
    state = WindowState(size = DpSize(320.dp, 500.dp))
) {
    WordMasterView()
}

@Composable
fun WordMasterView() {
    val wordMasterService = remember { WordMasterService("words.txt") }

    val boardGuesses by wordMasterService.boardGuesses.collectAsState()
    val boardStatus by wordMasterService.boardStatus.collectAsState()

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Row(Modifier.fillMaxSize().padding(16.dp)) {

        Column {
            for (guessAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {

                    for (character in 0 until WordMasterService.NUMBER_LETTERS) {
                        Column(
                            Modifier.padding(4.dp).background(White).border(1.dp, Black),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            val modifier = if (guessAttempt == 0 && character == 0) {
                                Modifier.width(50.dp).focusRequester(focusRequester)
                            } else {
                                Modifier.width(50.dp)
                            }

                            TextField(
                                value = boardGuesses[guessAttempt][character],
                                onValueChange = {
                                    if (it.length <= 1 && guessAttempt == wordMasterService.currentGuessAttempt) {
                                        wordMasterService.setGuess(
                                            guessAttempt,
                                            character,
                                            it.uppercase()
                                        )
                                        focusManager.moveFocus(FocusDirection.Next)
                                    }
                                },
                                modifier = modifier,
                                textStyle = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    backgroundColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
                                    unfocusedIndicatorColor = Transparent,
                                    focusedIndicatorColor = Transparent
                                )
                            )

                            DisposableEffect(Unit) {
                                focusRequester.requestFocus()
                                onDispose { }
                            }
                        }
                    }
                }
            }

            Row {
                Button(onClick = {
                    wordMasterService.checkGuess()
                }) {
                    Text("Guess")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = {
                    wordMasterService.resetGame()
                }) {
                    Text("New Game")
                }
            }
        }
    }

}

fun mapLetterStatusToBackgroundColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> White
        LetterStatus.CORRECT_POSITION -> Color(0xFF008000)
        LetterStatus.WRONG_POSITION -> Color(0xFF09B870C)
        LetterStatus.NOT_IN_WORD -> Gray
    }
}

fun mapLetterStatusToTextColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> Black
        LetterStatus.CORRECT_POSITION -> White
        LetterStatus.WRONG_POSITION -> White
        LetterStatus.NOT_IN_WORD -> White
    }
}

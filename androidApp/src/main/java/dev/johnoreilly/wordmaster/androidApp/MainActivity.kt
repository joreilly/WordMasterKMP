package dev.johnoreilly.wordmaster.androidApp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import dev.johnoreilly.wordmaster.shared.LetterStatus
import dev.johnoreilly.wordmaster.shared.WordMasterService
import dev.johnoreilly.wordmaster.androidApp.theme.WordMasterTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            WordMasterTheme {
                MainLayout()
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainLayout() {
    Scaffold(
        topBar = { WordMasterTopAppBar("WordMaster KMP") }
    ) { innerPadding ->
        WordMasterView(Modifier.padding(innerPadding).imePadding())
    }
}


@Composable
fun WordMasterView(padding: Modifier) {
    val context = LocalContext.current

    val wordMasterService = remember {
        val wordsPath = "${context.filesDir.absolutePath}/words.txt"
        WordMasterService(wordsPath)
    }

    val boardGuesses by wordMasterService.boardGuesses.collectAsState()
    val boardStatus by wordMasterService.boardStatus.collectAsState()
    val revealedAnswer by wordMasterService.revealedAnswer.collectAsState()
    val lastGuessCorrect by wordMasterService.lastGuessCorrect.collectAsState()

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Row(padding.fillMaxSize().padding(16.dp), horizontalArrangement = Center, verticalAlignment = Alignment.CenterVertically) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (guessAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
                Row(horizontalArrangement = Arrangement.Center) {
                    for (character in 0 until WordMasterService.NUMBER_LETTERS) {
                        Column(
                            Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            var modifier = Modifier.width(55.dp).height(55.dp)
                            if (guessAttempt == 0 && character == 0) {
                                modifier = modifier.focusRequester(focusRequester)
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
                                        if (it.isNotEmpty() && character < WordMasterService.NUMBER_LETTERS - 1) {
                                            // Only move within the same row; don't advance to the next row until the guess is submitted
                                            focusManager.moveFocus(FocusDirection.Next)
                                        }
                                    }
                                },
                                modifier = modifier.onKeyEvent {
                                    if (it.type == KeyEventType.KeyUp && (it.key == Key.Enter || it.key == Key.NumPadEnter)) {
                                        if (guessAttempt == wordMasterService.currentGuessAttempt) {
                                            var filled = true
                                            for (c in 0 until WordMasterService.NUMBER_LETTERS) {
                                                if (boardGuesses[guessAttempt][c].isEmpty()) { filled = false; break }
                                            }
                                            if (filled) {
                                                wordMasterService.checkGuess()
                                                // After submitting a guess, move focus to the next row's first cell
                                                focusManager.moveFocus(FocusDirection.Next)
                                                return@onKeyEvent true
                                            }
                                        }
                                    }
                                    false
                                }
                                .border(1.dp, Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (guessAttempt == wordMasterService.currentGuessAttempt) {
                                            var filled = true
                                            for (c in 0 until WordMasterService.NUMBER_LETTERS) {
                                                if (boardGuesses[guessAttempt][c].isEmpty()) { filled = false; break }
                                            }
                                            if (filled) {
                                                wordMasterService.checkGuess()
                                                // After submitting a guess, move focus to the next row's first cell
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        }
                                    }
                                ),
                                textStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    unfocusedTextColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    disabledTextColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    cursorColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    focusedContainerColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
                                    unfocusedContainerColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
                                    disabledContainerColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                ),
                            )

                            DisposableEffect(Unit) {
                                focusRequester.requestFocus()
                                onDispose { }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (revealedAnswer != null) {
                Text(
                    text = "Answer: $revealedAnswer",
                    style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                )
                Spacer(Modifier.height(12.dp))
            }

            Row(horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    // Only submit and advance focus if the current row is filled
                    val current = wordMasterService.currentGuessAttempt
                    var filled = true
                    for (c in 0 until WordMasterService.NUMBER_LETTERS) {
                        if (boardGuesses[current][c].isEmpty()) { filled = false; break }
                    }
                    if (filled) {
                        wordMasterService.checkGuess()
                        // Move focus to next row's first cell
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                }) {
                    Text("Guess")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = {
                    wordMasterService.resetGame()
                    focusRequester.requestFocus()
                }) {
                    Text("New Game")
                }
            }

            if (lastGuessCorrect) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { /* keep dialog until OK pressed */ },
                    title = { Text("You win!") },
                    text = { Text("Great job guessing the word.") },
                    confirmButton = {
                        Button(onClick = {
                            wordMasterService.resetGame()
                            // Re-focus first cell after reset
                            focusRequester.requestFocus()
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
        LetterStatus.UNGUESSED -> Color.White
        LetterStatus.CORRECT_POSITION -> Color(0xFF008000)
        LetterStatus.INCORRECT_POSITION -> Color(0xFF9B870C)
        LetterStatus.NOT_IN_WORD -> Color.Gray
    }
}

fun mapLetterStatusToTextColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> Color.Black
        LetterStatus.CORRECT_POSITION -> Color.White
        LetterStatus.INCORRECT_POSITION -> Color.White
        LetterStatus.NOT_IN_WORD -> Color.White
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordMasterTopAppBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
    )
}

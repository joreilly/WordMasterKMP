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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        WordMasterView(Modifier.padding(innerPadding))
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

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Row(padding.fillMaxSize().padding(16.dp), horizontalArrangement = Center) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (guessAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    for (character in 0 until WordMasterService.NUMBER_LETTERS) {
                        Column(
                            Modifier.padding(4.dp).background(Color.White).border(1.dp, Color.Black),
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
                                        focusManager.moveFocus(FocusDirection.Next)
                                    }
                                },
                                modifier = modifier,
                                textStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
                                colors = TextFieldDefaults.colors(
                                    unfocusedTextColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    unfocusedContainerColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
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
            Row(horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    wordMasterService.checkGuess()
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

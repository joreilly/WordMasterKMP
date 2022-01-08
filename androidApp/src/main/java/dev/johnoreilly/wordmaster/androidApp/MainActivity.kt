package dev.johnoreilly.wordmaster.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import dev.johnoreilly.wordmaster.shared.LetterStatus
import dev.johnoreilly.wordmaster.shared.WordMasterService
import dev.johnoreilly.wordmaster.androidApp.theme.WordMasterTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(window, false)

        setContent {
            WordMasterTheme {
                ProvideWindowInsets {
                    MainLayout()
                }
            }
        }
    }
}

@Composable
fun MainLayout() {
    Scaffold(
        topBar = { WordMasterTopAppBar("WordMaster KMP") }
    ) {
        WordMasterView()
    }
}


@Composable
fun WordMasterView() {
    val context = LocalContext.current

    val wordMasterService = remember {
        val wordsPath = "${context.filesDir.absolutePath}/words.txt"
        WordMasterService(wordsPath)
    }

    val boardGuesses by wordMasterService.boardGuesses.collectAsState()
    val boardStatus by wordMasterService.boardStatus.collectAsState()

    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Center) {

        Column {
            for (guessAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    for (character in 0 until WordMasterService.NUMBER_LETTERS) {
                        Column(
                            Modifier.padding(4.dp).background(Color.White).border(1.dp, Color.Black),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextField(
                                value = boardGuesses[guessAttempt][character],
                                onValueChange = {
                                    if (it.length <= 1) {
                                        wordMasterService.setGuess(
                                            guessAttempt,
                                            character,
                                            it.uppercase()
                                        )
                                    }
                                },
                                modifier = Modifier.width(50.dp),
                                textStyle = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = mapLetterStatusToTextColor(boardStatus[guessAttempt][character]),
                                    backgroundColor = mapLetterStatusToBackgroundColor(boardStatus[guessAttempt][character]),
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                )
                            )
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
        LetterStatus.UNGUESSED -> Color.White
        LetterStatus.CORRECT_POSITION -> Color(0xFF008000)
        LetterStatus.WRONG_POSITION -> Color(0xFF9B870C)
        LetterStatus.NOT_IN_WORD -> Color.Gray
    }
}

fun mapLetterStatusToTextColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> Color.Black
        LetterStatus.CORRECT_POSITION -> Color.White
        LetterStatus.WRONG_POSITION -> Color.White
        LetterStatus.NOT_IN_WORD -> Color.White
    }
}

@Composable
private fun WordMasterTopAppBar(title: String) {
    Surface(color = MaterialTheme.colors.primary) {
        TopAppBar(
            title = { Text(title) },
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            modifier = Modifier.statusBarsPadding()
        )
    }
}

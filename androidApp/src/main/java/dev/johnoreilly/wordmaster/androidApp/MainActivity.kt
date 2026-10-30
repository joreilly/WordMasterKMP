package dev.johnoreilly.wordmaster.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.wordmaster.androidApp.theme.WordMasterTheme
import dev.johnoreilly.wordmaster.shared.LetterStatus
import dev.johnoreilly.wordmaster.shared.WordMasterService


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

@Composable
fun MainLayout() {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { WordMasterTopAppBar("WordMaster KMP") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        WordMasterView(Modifier.padding(innerPadding).imePadding(), snackbarHostState)
    }
}


@Composable
fun WordMasterView(padding: Modifier, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current

    val wordMasterService = remember {
        val wordsPath = "${context.filesDir.absolutePath}/words.txt"
        WordMasterService(wordsPath)
    }

    val boardGuesses by wordMasterService.boardGuesses.collectAsStateWithLifecycle()
    val boardStatus by wordMasterService.boardStatus.collectAsStateWithLifecycle()
    val keyStatus by wordMasterService.keyStatus.collectAsStateWithLifecycle()
    val revealedAnswer by wordMasterService.revealedAnswer.collectAsStateWithLifecycle()
    val lastGuessCorrect by wordMasterService.lastGuessCorrect.collectAsStateWithLifecycle()
    val guessError by wordMasterService.guessError.collectAsStateWithLifecycle()

    // Horizontal shake offset applied to the active row when a guess is rejected.
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(guessError) {
        val error = guessError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        val shift = 16f
        for (step in listOf(-shift, shift, -shift, shift, -shift / 2, shift / 2, 0f)) {
            shakeOffset.animateTo(step)
        }
        wordMasterService.clearGuessError()
    }

    Column(
        padding.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (guessAttempt in 0 until WordMasterService.MAX_NUMBER_OF_GUESSES) {
            val rowModifier = if (guessAttempt == wordMasterService.currentGuessAttempt) {
                Modifier.offset { androidx.compose.ui.unit.IntOffset(shakeOffset.value.toInt(), 0) }
            } else {
                Modifier
            }
            Row(rowModifier, horizontalArrangement = Arrangement.Center) {
                for (character in 0 until WordMasterService.NUMBER_LETTERS) {
                    LetterTile(
                        letter = boardGuesses[guessAttempt][character],
                        status = boardStatus[guessAttempt][character],
                        testTag = "Board.Tile.$guessAttempt.$character"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (revealedAnswer != null) {
            Text(
                text = "Answer: $revealedAnswer",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
        }

        Keyboard(
            keyStatus = keyStatus,
            onLetter = { wordMasterService.addLetter(it) },
            onEnter = { wordMasterService.submitGuess() },
            onDelete = { wordMasterService.removeLetter() }
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { wordMasterService.resetGame() },
            modifier = Modifier.testTag("Game.NewGame.Button")
        ) {
            Text("New Game")
        }

        if (lastGuessCorrect) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { },
                title = { Text("You win!") },
                text = { Text("Great job guessing the word.") },
                confirmButton = {
                    Button(onClick = { wordMasterService.resetGame() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun LetterTile(letter: String, status: LetterStatus, testTag: String) {
    Box(
        Modifier
            .padding(3.dp)
            .size(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(mapLetterStatusToBackgroundColor(status))
            .border(1.5.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = mapLetterStatusToTextColor(status),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(testTag)
        )
    }
}

private val KEYBOARD_ROWS = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")

@Composable
private fun Keyboard(
    keyStatus: Map<String, LetterStatus>,
    onLetter: (String) -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        KEYBOARD_ROWS.forEachIndexed { index, row ->
            Row(horizontalArrangement = Arrangement.Center) {
                if (index == KEYBOARD_ROWS.size - 1) {
                    KeyButton("ENTER", testTag = "Keyboard.Key.Enter", onClick = onEnter, flexWidth = true)
                }
                row.forEach { char ->
                    val letter = char.toString()
                    KeyButton(
                        label = letter,
                        testTag = "Keyboard.Key.$letter",
                        onClick = { onLetter(letter) },
                        status = keyStatus[letter] ?: LetterStatus.UNGUESSED
                    )
                }
                if (index == KEYBOARD_ROWS.size - 1) {
                    KeyButton("DEL", testTag = "Keyboard.Key.Delete", onClick = onDelete, flexWidth = true)
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    status: LetterStatus = LetterStatus.UNGUESSED,
    flexWidth: Boolean = false
) {
    val background = if (status == LetterStatus.UNGUESSED) {
        Color(0xFFD3D6DA)
    } else {
        mapLetterStatusToBackgroundColor(status)
    }
    val textColor = if (status == LetterStatus.UNGUESSED) Color.Black else mapLetterStatusToTextColor(status)

    Box(
        Modifier
            .padding(2.dp)
            .height(48.dp)
            .then(if (flexWidth) Modifier.width(52.dp) else Modifier.width(32.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (flexWidth) 11.sp else 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun mapLetterStatusToBackgroundColor(letterStatus: LetterStatus): Color {
    return when (letterStatus) {
        LetterStatus.UNGUESSED -> Color.White
        LetterStatus.CORRECT_POSITION -> Color(0xFF2E7D32)
        LetterStatus.INCORRECT_POSITION -> Color(0xFF9B870C)
        LetterStatus.NOT_IN_WORD -> Color(0xFF787C7E)
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

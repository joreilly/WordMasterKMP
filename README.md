# WordMasterKMP

![kotlin-version](https://img.shields.io/badge/kotlin-2.4.10-blue?logo=kotlin)

Kotlin Multiplatform sample heavily inspired by [Wordle](https://www.powerlanguage.co.uk/wordle/) game and also [Word Master](https://github.com/octokatherine/word-master) and [wordle-solver](https://github.com/dlew/wordle-solver) samples.  The main game logic/state is included in shared KMP code with basic UI then in following clients
- iOS (SwiftUI)
- Android (Jetpack Compose)
- Desktop (Compose for Desktop)


### Screenshots

<img width="792" height="498" alt="Screenshot 2025-08-16 at 15 21 14" src="https://github.com/user-attachments/assets/7788d21b-9f4c-4e71-8ed9-3ee83666c722" />


### Shared KMP game logic/state

The shared `WordMasterService` class exposes the game state as `StateFlow`s:

```
val boardGuesses = StateFlow<ArrayList<ArrayList<String>>>()       // letters entered on each row
val boardStatus  = StateFlow<ArrayList<ArrayList<LetterStatus>>>() // per-letter result
val keyStatus    = StateFlow<Map<String, LetterStatus>>()          // best status per key, colours the keyboard
val guessError   = StateFlow<String?>()                            // validation feedback
```

Each client renders a board of read-only tiles plus an on-screen keyboard. Key presses call
`WordMasterService.addLetter()` / `removeLetter()`, and `submitGuess()` validates the row against the
word list (surfacing "Not enough letters" / "Not in word list" via `guessError`) before evaluating it
and updating the flows above.  The Compose clients observe state as following (with any updates to those
`StateFlow`'s triggering recomposition)

```
val boardStatus by wordMasterService.boardStatus.collectAsState()
val keyStatus by wordMasterService.keyStatus.collectAsState()
```


On iOS we're using Kotlin's [Swift export](https://kotlinlang.org/docs/native-swift-export.html), which maps `StateFlow` to a typed Swift `AsyncSequence`.  So, for example, our Swift view model includes

```
@Published public var boardStatus: [[LetterStatus]] = []
@Published public var boardGuesses: [[String]] = []
```

which are then updated using for example

```
for try await data in wordMasterService.boardStatus.asAsyncSequence() {
    self.boardStatus = data
}

for try await data in wordMasterService.boardGuesses.asAsyncSequence() {
    self.boardGuesses = data
}
```

Any updates to `boardStatus` or `boardGuesses` will trigger our SwiftUI UI to be recomposed again.



### UI tests

There's a [Maestro](https://maestro.mobile.dev/) UI test flow shared by the Android and iOS clients, run by CI on
an emulator and a simulator (e.g. `maestro test -e APP_ID=dev.johnoreilly.wordmaster.androidApp maestro/WordMaster.flow`).

## Full set of Kotlin Multiplatform/Compose/SwiftUI samples

*  PeopleInSpace (https://github.com/joreilly/PeopleInSpace)
*  GalwayBus (https://github.com/joreilly/GalwayBus)
*  Confetti (https://github.com/joreilly/Confetti)
*  BikeShare (https://github.com/joreilly/BikeShare)
*  FantasyPremierLeague (https://github.com/joreilly/FantasyPremierLeague)
*  ClimateTrace (https://github.com/joreilly/ClimateTraceKMP)
*  GeminiKMP (https://github.com/joreilly/GeminiKMP)
*  MortyComposeKMM (https://github.com/joreilly/MortyComposeKMM)
*  StarWars (https://github.com/joreilly/StarWars)
*  WordMasterKMP (https://github.com/joreilly/WordMasterKMP)
*  Chip-8 (https://github.com/joreilly/chip-8)
*  FirebaseAILogicKMPSample (https://github.com/joreilly/FirebaseAILogicKMPSample)

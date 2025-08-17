# WordMasterKMP

![kotlin-version](https://img.shields.io/badge/kotlin-2.2.0-blue?logo=kotlin)

Kotlin Multiplatform sample heavily inspired by [Wordle](https://www.powerlanguage.co.uk/wordle/) game and also [Word Master](https://github.com/octokatherine/word-master) and [wordle-solver](https://github.com/dlew/wordle-solver) samples.  The main game logic/state is included in shared KMP code with basic UI then in following clients
- iOS (SwiftUI)
- Android (Jetpack Compose)
- Desktop (Compose for Desktop)


### Screenshots

<img width="792" height="498" alt="Screenshot 2025-08-16 at 15 21 14" src="https://github.com/user-attachments/assets/7788d21b-9f4c-4e71-8ed9-3ee83666c722" />


### Shared KMP game logic/state

The shared `WordMasterService` class includes following `StateFlow`s representing the current set of guesses and updated status info for each letter.

```
val boardGuesses = StateFlow<ArrayList<ArrayList<String>>>()
val boardStatus = StateFlow<ArrayList<ArrayList<LetterStatus>>>()
```

The various clients call `WordService.setGuess()` when a user enters a letter and then `WordService.checkGuess()` after row of letters
are entered...UI then reflects any resulting updates to above `StateFlow`'s.  The Compose clients for example do that using following (with any updates to those `StateFlow's` triggering recomposition)

```
val boardGuesses by wordMasterService.boardGuesses.collectAsState()
val boardStatus by wordMasterService.boardStatus.collectAsState()
```
<br/>
On iOS we're using the [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) library to map the `StateFlow` to Swift `AsyncSequence`.  So, for example, our Swift view model includes

```
@Published public var boardStatus: [[LetterStatus]] = []
@Published public var boardGuesses: [[String]] = []
```

which are then updated using for example

```
let stream = asyncSequence(for: wordMasterService.boardStatusNative)
for try await data in stream {
  self.boardStatus = data as! [[LetterStatus]]
}

let stream = asyncSequence(for: wordMasterService.boardGuessesNative)
for try await data in stream {
    self.boardGuesses = data as! [[String]]
}

```

Any updates to `boardStatus` or `boardGuesses` will trigger our SwiftUI UI to be recomposed again.



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

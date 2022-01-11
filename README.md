# WordMasterKMP

Kotlin Multiplatform sample heavily inspired by [Wordle](https://www.powerlanguage.co.uk/wordle/) game and also [Word Master](https://github.com/octokatherine/word-master) and [wordle-solver](https://github.com/dlew/wordle-solver) samples.  The main game logic/state is included in shared KMP code with basic UI then in following clients
- iOS (SwiftUI)
- Android (Jetpack Compose)
- Desktop (Compose for Desktop)

### Shared KMP game logic/state

The shared `WordMasterService` class includes following `StateFlow`s representing the current set of guesses and updated status info for each letter.

```
val boardGuesses = MutableStateFlow<ArrayList<ArrayList<String>>>(arrayListOf())
val boardStatus = MutableStateFlow<ArrayList<ArrayList<LetterStatus>>>(arrayListOf())
```

The various clients call `WordService.setGuess()` when a user enters a letter and then `WordService.checkGuess()` after row of letters
are entered...UI then reflects any resulting updates to above `StateFlow`'s.  The Compose clients for example do that using

```
val boardGuesses by wordMasterService.boardGuesses.collectAsState()
val boardStatus by wordMasterService.boardStatus.collectAsState()
```

On iOS we're using [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) library to map the `StateFlow`s to Swift `AsyncStream`s.  So, for example, our Swift view model includes

```
@Published public var boardStatus: [[LetterStatus]] = []
@Published public var boardGuesses: [[String]] = []
```

which are then updated using for example

```
let stream = asyncStream(for: wordMasterService.boardStatusNative)
for try await data in stream {
  self.boardStatus = data as! [[LetterStatus]]
}

let stream = asyncStream(for: wordMasterService.boardGuessesNative)
for try await data in stream {
    self.boardGuesses = data as! [[String]]
}

```

Any updates to `boardStatus` or `boardGuesses` will trigger our SwiftUI UI to be recomposed again.


### Remaining work includes

- check if overall word is valid and show indication in UI if not (ideally with animations!)
- better keyboard navigation
- share Compose code between Android and Desktop
- indicator in UI that correct guess entered (other than all letters being green)


### Screenshots
<img width="462" alt="Screenshot 2022-01-08 at 22 40 36" src="https://user-images.githubusercontent.com/6302/148663058-a725d403-b956-4c84-8635-fbb388fa63a8.png">

![Simulator Screen Shot - iPhone 13 Pro - 2022-01-08 at 22 38 11](https://user-images.githubusercontent.com/6302/148663064-3ed57b1f-c1a3-4e39-b2c2-2ddb3fb09ed9.png)

![Screenshot_1641682073](https://user-images.githubusercontent.com/6302/148663060-c1047266-425c-4b14-bdaf-b7177a1fa332.png)

import SwiftUI
import shared
import KMPNativeCoroutinesAsync

struct ContentView: View {
    @StateObject private var viewModel = ViewModel()
        
    var body: some View {
        NavigationView {
            VStack {
                ForEach(0 ..< viewModel.getMaxNumberGuesses()) { guessNumber in
                    HStack {
                        ForEach(0 ..< viewModel.getMaxNumberLetters()) { character in
                                                        
                            let guessBinding = Binding<String>(
                                get: { viewModel.getGuess(guessAttempt: guessNumber, character: character) },
                                set: {
                                    if ($0.count <= 1) {
                                        viewModel.setGuess(guessAttempt: guessNumber, character: character, guess: $0)
                                    }
                                }
                            )

                            
                            TextField("", text: guessBinding, onCommit: {
                                        
                            })
                            .frame(maxWidth: 40, alignment: .center)
                            .padding([.trailing, .leading], 10)
                            .padding([.vertical], 15)
                            .lineLimit(1)
                            .multilineTextAlignment(.center)
                            .border(.black)
                            .background(viewModel.getLetterStatusBackgroundColor(guessAttempt: guessNumber, character: character))
                        }
                    }
                    
                }
                
                HStack {
                    Button(action: {
                        viewModel.checkGuess()
                    }) {
                        Text("Guess")
                    }

                    Button(action: {
                        viewModel.newGame()
                    }) {
                        Text("New Game")
                    }

                }
            }
            .navigationBarTitle(Text("WordMaster KMP"))
        }
    }
}

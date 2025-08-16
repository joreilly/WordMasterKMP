import SwiftUI
import shared
import KMPNativeCoroutinesAsync

struct ContentView: View {
    @StateObject private var viewModel = ViewModel()

    // Focus handling for per-cell focus movement
    private struct FocusPos: Hashable { let row: Int; let col: Int }
    @FocusState private var focusedPos: FocusPos?
    @State private var showWinAlert: Bool = false
        
    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                ForEach(0 ..< viewModel.getMaxNumberGuesses(), id: \.self) { guessNumber in
                    HStack(spacing: 8) {
                        ForEach(0 ..< viewModel.getMaxNumberLetters(), id: \.self) { character in
                            let guessBinding = Binding<String>(
                                get: { viewModel.getGuess(guessAttempt: guessNumber, character: character) },
                                set: { newValue in
                                    // Force uppercase and limit to first character
                                    let upper = newValue.uppercased()
                                    let capped = String(upper.prefix(1))

                                    if capped != viewModel.getGuess(guessAttempt: guessNumber, character: character) {
                                        viewModel.setGuess(guessAttempt: guessNumber, character: character, guess: capped)

                                        // Move focus to the next cell when a single character is entered
                                        if !capped.isEmpty {
                                            let nextCol = character + 1
                                            if nextCol < viewModel.getMaxNumberLetters() {
                                                // Advance to next column in same row
                                                DispatchQueue.main.async {
                                                    focusedPos = FocusPos(row: guessNumber, col: nextCol)
                                                }
                                            } else {
                                                // Optionally keep focus or move to next row's first cell; we'll keep it here
                                            }
                                        }
                                    }
                                }
                            )

                            TextField("", text: guessBinding)
                                .textInputAutocapitalization(.characters)
                                .disableAutocorrection(true)
                                .font(.system(size: 20, weight: .semibold, design: .monospaced))
                                .multilineTextAlignment(.center)
                                .frame(width: 56, height: 56)
                                .background(
                                    RoundedRectangle(cornerRadius: 10)
                                        .fill(viewModel.getLetterStatusBackgroundColor(guessAttempt: guessNumber, character: character))
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.black.opacity(0.6), lineWidth: 1)
                                )
                                .focused($focusedPos, equals: FocusPos(row: guessNumber, col: character))
                                .submitLabel(.done)
                                .onSubmit {
                                    // If current row is fully filled, trigger Guess
                                    if guessNumber == viewModel.getCurrentGuessAttempt() {
                                        let maxLetters = viewModel.getMaxNumberLetters()
                                        var filled = true
                                        for col in 0..<maxLetters {
                                            if viewModel.getGuess(guessAttempt: guessNumber, character: col).isEmpty {
                                                filled = false
                                                break
                                            }
                                        }
                                        if filled {
                                            viewModel.checkGuess()
                                            // After submitting a guess, move focus to the next row's first cell
                                            let nextRow = guessNumber + 1
                                            if nextRow < viewModel.getMaxNumberGuesses() {
                                                DispatchQueue.main.async {
                                                    focusedPos = FocusPos(row: nextRow, col: 0)
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
                
                if let answer = viewModel.revealedAnswer {
                    Text("Answer: \(answer)")
                        .font(.system(size: 18, weight: .semibold, design: .default))
                        .foregroundColor(.primary)
                }
                
                HStack(spacing: 16) {
                    Button(action: {
                        // Only submit and advance focus if the current row is filled
                        let current = viewModel.getCurrentGuessAttempt()
                        let maxLetters = viewModel.getMaxNumberLetters()
                        var filled = true
                        for col in 0..<maxLetters {
                            if viewModel.getGuess(guessAttempt: current, character: col).isEmpty {
                                filled = false
                                break
                            }
                        }
                        if filled {
                            viewModel.checkGuess()
                            let nextRow = current + 1
                            if nextRow < viewModel.getMaxNumberGuesses() {
                                focusedPos = FocusPos(row: nextRow, col: 0)
                            }
                        }
                    }) {
                        Text("Guess")
                    }

                    Button(action: {
                        viewModel.newGame()
                        // Reset focus to the first cell
                        focusedPos = FocusPos(row: 0, col: 0)
                    }) {
                        Text("New Game")
                    }

                }
            }
            .padding(20)
            .navigationBarTitle(Text("WordMaster KMP"))
            .onAppear {
                // Set initial focus to first cell
                focusedPos = FocusPos(row: 0, col: 0)
            }
            .onChange(of: viewModel.lastGuessCorrect) { newValue in
                if newValue {
                    showWinAlert = true
                }
            }
            .alert("You win!", isPresented: $showWinAlert) {
                Button("OK") {
                    viewModel.newGame()
                    focusedPos = FocusPos(row: 0, col: 0)
                    showWinAlert = false
                }
            } message: {
                Text("Great job guessing the word.")
            }
        }
    }
}

import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = ViewModel()

    @State private var showWinAlert: Bool = false
    @State private var shakeTrigger: CGFloat = 0

    private let keyboardRows = ["QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"]

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                // Board
                VStack(spacing: 6) {
                    ForEach(0 ..< viewModel.getMaxNumberGuesses(), id: \.self) { guessNumber in
                        HStack(spacing: 6) {
                            ForEach(0 ..< viewModel.getMaxNumberLetters(), id: \.self) { character in
                                let status = viewModel.getLetterStatus(guessAttempt: guessNumber, character: character)
                                Text(viewModel.getGuess(guessAttempt: guessNumber, character: character))
                                    .font(.system(size: 24, weight: .bold, design: .default))
                                    .foregroundColor(viewModel.textColor(for: status))
                                    .frame(width: 52, height: 52)
                                    .background(
                                        RoundedRectangle(cornerRadius: 8)
                                            .fill(viewModel.backgroundColor(for: status))
                                    )
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(Color.black.opacity(0.3), lineWidth: 1.5)
                                    )
                            }
                        }
                        .modifier(ShakeEffect(animatableData: guessNumber == viewModel.getCurrentGuessAttempt() ? shakeTrigger : 0))
                    }
                }

                if let error = viewModel.guessError {
                    Text(error)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(red: 0.69, green: 0.0, blue: 0.13))
                }

                if let answer = viewModel.revealedAnswer {
                    Text("Answer: \(answer)")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.primary)
                }

                Spacer()

                // On-screen keyboard
                VStack(spacing: 6) {
                    ForEach(0 ..< keyboardRows.count, id: \.self) { rowIndex in
                        HStack(spacing: 4) {
                            if rowIndex == keyboardRows.count - 1 {
                                KeyButton(label: "ENTER", width: 56, background: Color(red: 0.83, green: 0.84, blue: 0.85), foreground: .black) {
                                    viewModel.submitGuess()
                                }
                            }
                            ForEach(Array(keyboardRows[rowIndex]), id: \.self) { char in
                                let letter = String(char)
                                KeyButton(label: letter, width: 32,
                                          background: viewModel.keyBackgroundColor(letter),
                                          foreground: viewModel.keyTextColor(letter)) {
                                    viewModel.addLetter(letter)
                                }
                            }
                            if rowIndex == keyboardRows.count - 1 {
                                KeyButton(label: "DEL", width: 56, background: Color(red: 0.83, green: 0.84, blue: 0.85), foreground: .black) {
                                    viewModel.removeLetter()
                                }
                            }
                        }
                    }
                }

                Button(action: {
                    viewModel.newGame()
                }) {
                    Text("New Game")
                }
                .padding(.top, 8)
            }
            .padding(20)
            .navigationBarTitle(Text("WordMaster KMP"))
            .onChange(of: viewModel.lastGuessCorrect) { newValue in
                if newValue {
                    showWinAlert = true
                }
            }
            .onChange(of: viewModel.guessError) { newValue in
                if newValue != nil {
                    withAnimation(.default) {
                        shakeTrigger += 1
                    }
                    // Clear after the shake so the message doesn't linger.
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                        viewModel.clearGuessError()
                    }
                }
            }
            .alert("You win!", isPresented: $showWinAlert) {
                Button("OK") {
                    viewModel.newGame()
                    showWinAlert = false
                }
            } message: {
                Text("Great job guessing the word.")
            }
        }
    }
}

private struct KeyButton: View {
    let label: String
    let width: CGFloat
    let background: Color
    let foreground: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: label.count > 1 ? 12 : 16, weight: .bold))
                .foregroundColor(foreground)
                .frame(width: width, height: 48)
                .background(
                    RoundedRectangle(cornerRadius: 6).fill(background)
                )
        }
        .buttonStyle(.plain)
    }
}

// Horizontal shake applied to the active row when a guess is rejected.
private struct ShakeEffect: GeometryEffect {
    var travelDistance: CGFloat = 8
    var shakesPerUnit = 3
    var animatableData: CGFloat

    func effectValue(size: CGSize) -> ProjectionTransform {
        let translation = travelDistance * sin(animatableData * .pi * CGFloat(shakesPerUnit))
        return ProjectionTransform(CGAffineTransform(translationX: translation, y: 0))
    }
}

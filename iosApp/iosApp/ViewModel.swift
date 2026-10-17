import Foundation
import SwiftUI
import shared


@MainActor
class ViewModel: ObservableObject {
    private let wordMasterService: WordMasterService
    @Published public var boardStatus: [[LetterStatus]] = []
    @Published public var boardGuesses: [[String]] = []
    @Published public var keyStatus: [String: LetterStatus] = [:]
    @Published public var revealedAnswer: String? = nil
    @Published public var lastGuessCorrect: Bool = false
    @Published public var guessError: String? = nil

    init() {
        let wordsPath = Bundle.main.path(forResource: "words", ofType: "txt") ?? ""
        wordMasterService = WordMasterService(wordsFilePath: wordsPath)

        Task {
            do {
                let stream = wordMasterService.boardStatus.asAsyncSequence()
                for try await data in stream {
                    self.boardStatus = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = wordMasterService.boardGuesses.asAsyncSequence()
                for try await data in stream {
                    self.boardGuesses = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = wordMasterService.keyStatus.asAsyncSequence()
                for try await data in stream {
                    self.keyStatus = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = wordMasterService.revealedAnswer.asAsyncSequence()
                for try await data in stream {
                    self.revealedAnswer = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = wordMasterService.lastGuessCorrect.asAsyncSequence()
                for try await data in stream {
                    self.lastGuessCorrect = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = wordMasterService.guessError.asAsyncSequence()
                for try await data in stream {
                    self.guessError = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }

    func getMaxNumberGuesses() -> Int {
        return Int(WordMasterService.Companion.shared.MAX_NUMBER_OF_GUESSES)
    }

    func getMaxNumberLetters() -> Int {
        return Int(WordMasterService.Companion.shared.NUMBER_LETTERS)
    }

    func getCurrentGuessAttempt() -> Int {
        return Int(wordMasterService.currentGuessAttempt)
    }

    func getGuess(guessAttempt: Int, character: Int) -> String {
        if (!boardGuesses.isEmpty) {
            return boardGuesses[guessAttempt][character]
        } else {
            return ""
        }
    }

    func getLetterStatus(guessAttempt: Int, character: Int) -> LetterStatus {
        if (boardStatus.count > 0) {
            return boardStatus[guessAttempt][character]
        } else {
            return .UNGUESSED
        }
    }

    func backgroundColor(for status: LetterStatus) -> Color {
        switch status {
            case .CORRECT_POSITION: return Color(red: 0.18, green: 0.49, blue: 0.20)
            case .INCORRECT_POSITION: return Color(red: 0.61, green: 0.53, blue: 0.05)
            case .NOT_IN_WORD: return Color(red: 0.47, green: 0.49, blue: 0.49)
            default: return .white
        }
    }

    func textColor(for status: LetterStatus) -> Color {
        switch status {
            case .UNGUESSED: return .black
            default: return .white
        }
    }

    // Colour for an on-screen keyboard key (unguessed keys use a light grey).
    func keyBackgroundColor(_ letter: String) -> Color {
        let status = keyStatus[letter] ?? .UNGUESSED
        if status == .UNGUESSED {
            return Color(red: 0.83, green: 0.84, blue: 0.85)
        }
        return backgroundColor(for: status)
    }

    func keyTextColor(_ letter: String) -> Color {
        let status = keyStatus[letter] ?? .UNGUESSED
        return status == .UNGUESSED ? .black : .white
    }

    func addLetter(_ letter: String) {
        wordMasterService.addLetter(letter: letter)
    }

    func removeLetter() {
        wordMasterService.removeLetter()
    }

    func submitGuess() {
        wordMasterService.submitGuess()
    }

    func clearGuessError() {
        wordMasterService.clearGuessError()
    }

    func newGame() {
        wordMasterService.resetGame()
    }
}

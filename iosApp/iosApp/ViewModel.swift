import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync


@MainActor
class ViewModel: ObservableObject {
    private let wordMasterService: WordMasterService
    @Published public var boardStatus: [[LetterStatus]] = []
    @Published public var boardGuesses: [[String]] = []
    @Published public var revealedAnswer: String? = nil
    @Published public var lastGuessCorrect: Bool = false
    
    init() {
        let wordsPath = Bundle.main.path(forResource: "words", ofType: "txt") ?? ""
        wordMasterService = WordMasterService(wordsFilePath: wordsPath)
        
        Task {
            do {
                let stream = asyncSequence(for: wordMasterService.boardStatus)
                for try await data in stream {
                    self.boardStatus = data as! [[LetterStatus]]
                    print(boardStatus)
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = asyncSequence(for: wordMasterService.boardGuesses)
                for try await data in stream {
                    self.boardGuesses = data as! [[String]]
                    print(boardGuesses)
                }
            } catch {
                print("Failed with error: \(error)")
            }

        }
        Task {
            do {
                let stream = asyncSequence(for: wordMasterService.revealedAnswer)
                for try await data in stream {
                    self.revealedAnswer = data as? String
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
        Task {
            do {
                let stream = asyncSequence(for: wordMasterService.lastGuessCorrect)
                for try await data in stream {
                    self.lastGuessCorrect = (data as? Bool) ?? false
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }

    func getMaxNumberGuesses() -> Int {
        return Int(WordMasterService.companion.MAX_NUMBER_OF_GUESSES)
    }

    func getMaxNumberLetters() -> Int {
        return Int(WordMasterService.companion.NUMBER_LETTERS)
    }

    func getCurrentGuessAttempt() -> Int {
        return Int(wordMasterService.currentGuessAttempt)
    }
    
    func setGuess(guessAttempt: Int, character: Int, guess: String) {
        wordMasterService.setGuess(guessAttempt: Int32(guessAttempt), character: Int32(character), guess: guess)
    }

    func getGuess(guessAttempt: Int, character: Int) -> String {
        if (!boardGuesses.isEmpty) {
            return boardGuesses[guessAttempt][character]
        } else {
            return ""
        }
    }

    func getLetterStatusBackgroundColor(guessAttempt: Int, character: Int) -> Color {
        if (boardStatus.count > 0) {
            let status = boardStatus[guessAttempt][character]
            
            var color: Color = Color.white
            switch status {
                case .unguessed:
                    color = .white
                case .correctPosition:
                    color = .green
                case .notInWord:
                    color = .gray
                case .incorrectPosition:
                    color = .yellow
                default:
                    color = .white
            }
            return color
        } else {
            return .white
        }
    }

    func checkGuess() {
        wordMasterService.checkGuess()
    }
    
    func newGame() {
        wordMasterService.resetGame()
    }
}


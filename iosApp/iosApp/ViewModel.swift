import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync


@MainActor
class ViewModel: ObservableObject {
    private let wordMasterService: WordMasterService
    @Published public var boardStatus: [[LetterStatus]] = []
    @Published public var boardGuesses: [[String]] = []
    
    init() {
        let wordsPath = Bundle.main.path(forResource: "words", ofType: "txt") ?? ""
        wordMasterService = WordMasterService(wordsFilePath: wordsPath)
        
        Task {
            do {
                let stream = asyncStream(for: wordMasterService.boardStatusNative)
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
                let stream = asyncStream(for: wordMasterService.boardGuessesNative)
                for try await data in stream {
                    self.boardGuesses = data as! [[String]]
                    print(boardGuesses)
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

    
    func setGuess(guessAttempt: Int, character: Int, guess: String) {
        wordMasterService.setGuess(guessAttempt: Int32(guessAttempt), character: Int32(character), guess: guess)
    }

    func getGuess(guessAttempt: Int, character: Int) -> String {
        //return wordMasterService.getGuess(guessAttempt: Int32(guessAttempt), character: Int32(character))
        
        if (!boardGuesses.isEmpty) {
            return boardGuesses[guessAttempt][character]
        } else {
            return ""
        }
    }

    func getLetterStatus(guessAttempt: Int, character: Int) -> LetterStatus {
        return wordMasterService.getLetterStatus(guessAttempt: Int32(guessAttempt), character: Int32(character))
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


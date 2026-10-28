package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.exception.InvalidGameStateException;
import ru.yandex.exception.InvalidWordFormatException;
import ru.yandex.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WordleGameTest {

    private PrintWriter silentLog() {
        return new PrintWriter(new StringWriter());
    }

    // Небольшой словарь для тестов
    private WordleDictionary testDictionary() {
        return new WordleDictionary(List.of(
                "герой", "гонец", "около", "ооооо", "ааббв", "ежики",
                "берег", "ветер", "город", "завод", "закон", "земля",
                "книга", "конец", "лампа", "маска", "место", "мосты",
                "наука", "океан", "полей", "радио", "рынок", "света",
                "слово", "сосна", "танец", "точка", "финал"
        ));
    }

    @Test
    void analyzeForExampleFromTaskReturnsExpectedHint() {
        assertEquals("+^-^-", WordleGame.analyzeFor("герой", "гонец"));
    }

    @Test
    void analyzeForExactMatchReturnsAllPlus() {
        assertEquals("+++++", WordleGame.analyzeFor("герой", "герой"));
    }

    @Test
    void analyzeForDuplicateLettersCountsEachOccurrenceOnce() {
        assertEquals("+-+-+", WordleGame.analyzeFor("около", "ооооо"));
    }

    @Test
    void analyzeForExcessDuplicatesMarksOnlyAvailableAsPresent() {
        assertEquals("++---", WordleGame.analyzeFor("ааббв", "ааааа"));
    }

    @Test
    void analyzeForForeignLettersMarksAbsentExceptExisting() {
        assertEquals("---^-", WordleGame.analyzeFor("герой", "абвгд"));
    }

    @Test
    void makeMoveWrongLengthThrowsInvalidWordFormatException() {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        assertThrows(InvalidWordFormatException.class, () -> game.makeMove("кот"));
    }

    @Test
    void makeMoveWordNotInDictionaryThrowsWordNotFoundInDictionaryException() {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeMove("ааааа"));
    }

    @Test
    void makeMoveCorrectAnswerFinishesGameAsWin() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String answer = game.getAnswer();

        MoveResult result = game.makeMove(answer);

        assertTrue(result.isWin());
        assertTrue(result.isGameOver());
        assertTrue(game.isFinished());
        assertTrue(game.isWon());
    }

    @Test
    void getHintRepeatedCallsReturnDifferentClues() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String clue1 = game.getHint();
        String clue2 = game.getHint();
        assertNotEquals(clue1, clue2);
    }

    @Test
    void getHintAnswerAlwaysAmongCandidatesDoesNotThrow() {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        assertDoesNotThrow(() -> game.getHint());
    }

    @Test
    void makeMoveMaxWrongMovesFinishesGameAsLoss() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String answer = game.getAnswer();
        List<String> wrongWords = testDictionary().getWords().stream()
                .filter(w -> !w.equals(answer))
                .toList();

        for (int i = 0; i < WordleGame.MAX_STEPS; i++) {
            String w = wrongWords.get(i % wrongWords.size());
            MoveResult r = game.makeMove(w);
            if (i < WordleGame.MAX_STEPS - 1) {
                assertFalse(r.isGameOver(), "игра не должна закончиться на ходу " + (i + 1));
            } else {
                assertTrue(r.isGameOver());
                assertFalse(r.isWin());
            }
        }
        assertTrue(game.isFinished());
        assertFalse(game.isWon());
    }

    @Test
    void makeMoveWrongWordDecreasesStepsAndContinues() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String answer = game.getAnswer();
        String wrong = testDictionary().getWords().stream()
                .filter(w -> !w.equals(answer)).findFirst().orElseThrow();

        MoveResult r = game.makeMove(wrong);
        assertFalse(r.isWin());
        assertFalse(r.isGameOver());
        assertEquals(WordleGame.MAX_STEPS - 1, r.getStepsLeft());
        assertFalse(game.isFinished());
    }

    @Test
    void makeMoveAfterGameFinishedThrowsInvalidGameStateException() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        game.makeMove(game.getAnswer()); // победа
        assertThrows(InvalidGameStateException.class,
                () -> game.makeMove("гонец"));
    }

    @Test
    void getHintAfterMoveReturnsClueCompatibleWithHistory() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String answer = game.getAnswer();
        String wrong = testDictionary().getWords().stream().filter(w -> !w.equals(answer)).findFirst().orElseThrow();
        game.makeMove(wrong);

        String clue = game.getHint();
        String hintForWrong = game.getHints().get(0);
        assertEquals(hintForWrong, WordleGame.analyzeFor(clue, wrong),
                "подсказка должна быть совместима с историей");
    }

    @Test
    void getHintAfterMovesDoesNotRepeatGuessedOrGivenWords() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String answer = game.getAnswer();

        List<String> words = testDictionary().getWords();
        int added = 0;
        for (String w : words) {
            if (!w.equals(answer) && added < 2) {
                game.makeMove(w);
                added++;
            }
        }

        String clue1 = game.getHint();
        String clue2 = game.getHint();

        assertFalse(game.getGuessedWords().contains(clue1));
        assertFalse(game.getGuessedWords().contains(clue2));
        assertNotEquals(clue1, clue2);
    }

    @Test
    void getHintNeverReturnsAnswer() throws Exception {
        WordleGame game = new WordleGame(testDictionary(), silentLog(), new Random(42));
        String answer = game.getAnswer();
        for (int i = 0; i < 10; i++) {
            if (game.isFinished()) break;
            String clue = game.getHint();
            assertNotEquals(answer, clue, "подсказка не должна быть ответом");
        }
    }
}

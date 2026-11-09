package ru.yandex.practicum;

import ru.yandex.exception.InvalidGameStateException;
import ru.yandex.exception.InvalidWordFormatException;
import ru.yandex.exception.NoSuitableClueException;
import ru.yandex.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.util.*;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {

    public static final int MAX_STEPS = 6;
    public static final int WORD_LENGTH = 5;
    private final PrintWriter log;
    private final String answer;

    private int stepsLeft;

    private final WordleDictionary dictionary;
    private final Random random;
    private final List<String> guessedWords = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();
    private final Set<String> givenClues = new HashSet<>();

    private boolean finished;
    private boolean won;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this(dictionary, log, new Random());
    }

    public WordleGame(WordleDictionary dictionary, PrintWriter log, Random random) {
        if (dictionary == null || dictionary.isEmpty()) {
            throw new IllegalArgumentException("Словарь не может быть пустым");
        }
        if (log == null || random == null) {
            throw new IllegalArgumentException("Лог и Random не могут быть null");
        }
        this.dictionary = dictionary;
        this.log = log;
        this.random = random;

        List<String> words = dictionary.getWords();
        this.answer = words.get(random.nextInt(words.size()));
        this.stepsLeft = MAX_STEPS;

        log.println("Игра создана. Ответ загадан (длина " + answer.length() + ")");
        log.println("Осталось попыток: " + stepsLeft);
    }

    public MoveResult makeMove(String rawWord) throws InvalidWordFormatException, WordNotFoundInDictionaryException {
        ensureGameNotFinished();

        String word = WordleDictionary.normalize(rawWord);
        if (!WordleDictionary.hasValidFormat(word)) {
            log.println("Неккоректный формат ввода: " + word);
            throw new InvalidWordFormatException("Слово должно состоять из 5 русских букв");
        }

        if (!dictionary.contains(word)) {
            log.println("Слова " + word + "нет в словаре");
            throw new WordNotFoundInDictionaryException("Слова " + word + "нет в словаре");
        }

        String hint = analyze(word);
        guessedWords.add(word);
        hints.add(hint);

        boolean isWin = word.equals(answer);
        if (isWin) {
            won = true;
            finished = true;
        } else {
            stepsLeft--;
            if (stepsLeft <= 0) {
                finished = true;
            }
        }

        log.println("[GAME] Ход: " + word + " -> " + hint + " | осталось: " + stepsLeft + " | "
                + (isWin ? "ПОБЕДА" : (finished ? "ПОРАЖЕНИЕ" : "продолжаем")));
        return new MoveResult(word, hint, stepsLeft, isWin, finished);
    }

    public String getHint() throws NoSuitableClueException {
        ensureGameNotFinished();

        List<String> candidates = findCandidates();

        candidates.removeAll(givenClues);
        candidates.removeAll(guessedWords);

        if (candidates.isEmpty()) {
            log.println("[ERROR] Нет подъодящих подсказок");
            throw new NoSuitableClueException("Нет подходящих подсказок");
        }

        if (!candidates.contains(answer)) {
            log.println("Ответ не прошел фильтрацыию подсказок - ошибка в логике игры");
            throw new InvalidGameStateException("Ответ не прошел фильтрацыию подсказок - ошибка в логике игры");
        }

        String clue = candidates.get(random.nextInt(candidates.size()));
        givenClues.add(clue);
        log.println("Подсказка: " + clue);
        return clue;
    }


    private List<String> findCandidates() {
        List<String> result = new ArrayList<>();
        for (String candidate : dictionary.getWords()) {
            if (isCompatible(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private boolean isCompatible(String candidate) {
        for (int i = 0; i < guessedWords.size(); i++) {
            String expected = analyzeFor(candidate, guessedWords.get(i));
            if (!expected.equals(hints.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Сравнение с текущим ответом игры.
     */
    public String analyze(String word) {
        return analyzeFor(answer, word);
    }

    /**
     * Анализ слова по алгоритму Wordle: +, ^, -.
     */
    public static String analyzeFor(String answer, String word) {
        if (answer == null || word == null
                || answer.length() != WORD_LENGTH || word.length() != WORD_LENGTH) {
            throw new IllegalArgumentException("Слова должны быть длины " + WORD_LENGTH);
        }

        char[] result = new char[WORD_LENGTH];
        Map<Character, Integer> abailable = new HashMap<>();

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (word.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
            } else {
                abailable.merge(answer.charAt(i), 1, Integer::sum);
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (result[i] == '+') {
                continue;
            }
            char c = word.charAt(i);
            Integer count = abailable.getOrDefault(c, 0);
            if (count > 0) {
                result[i] = '^';
                abailable.put(c, count - 1);
            } else {
                result[i] = '-';
            }
        }
        return new String(result);
    }

    private void ensureGameNotFinished() {
        if (finished) {
            throw new InvalidGameStateException("Игра уже завершена");
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isWon() {
        return won;
    }

    public int getStepsLeft() {
        return stepsLeft;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getGuessedWords() {
        return new ArrayList<>(guessedWords);
    }

    public List<String> getHints() {
        return new ArrayList<>(hints);
    }
}

package ru.yandex.practicum;

import ru.yandex.exception.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        try (PrintWriter log = new PrintWriter(new FileWriter(LOG_FILE, StandardCharsets.UTF_8, true))) {
            log.println("=== Игра началась ===");
            runGame(log);
            log.println("=== Игра закончилась ===");
        } catch (IOException e) {
            System.err.println("[FATAL] Не удалось создать лог-файл: " + e.getMessage());
            e.printStackTrace();
        } catch (DictionaryLoadingException e) {
            System.err.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
        } catch (EmptyDictionaryException | InvalidGameStateException e) {
            System.err.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[FATAL] Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Основной сценарий игры
     */
    private static void runGame(PrintWriter log) throws DictionaryLoadingException {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary = loader.load(DICTIONARY_FILE);
        log.println("Словарь звгружен. Колличество слов: " + dictionary.size());

        WordleGame game = new WordleGame(dictionary, log);

        System.out.println("Добро пожаловать в игру Wordle!");
        System.out.println("Угадайье слово из 5 русских букв. Попыток: " + WordleGame.MAX_STEPS);
        System.out.println("Подсказки: '+' — буква на своём месте, '^' — буква есть, но не там, '-' — буквы нет.");
        System.out.println("Чтобы получить подсказку нажмите Enter на пустой строке");
        System.out.println();

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        while (!game.isFinished()) {
            System.out.println("Осталось попыток: " + game.getStepsLeft());
            System.out.print("> ");
            System.out.flush();

            if (!scanner.hasNextLine()) {
                log.println("Ввод завершен пользоветлем");
                System.out.println();
                System.out.println("Ввод завершен. Игра остановлена");
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                try {
                    String clue = game.getHint();
                    System.out.println("Подсказка: " + clue);
                } catch (NoSuitableClueException e) {
                    log.println("[WARN]: Нет подходящих подсказок: " + e.getMessage());
                    System.out.println("Подсказок не осталось");
                }
                System.out.println();
                continue;
            }
            try {
                MoveResult result = game.makeMove(input);

                System.out.println(result.getWord());
                System.out.println(result.getHint());

                if (result.isWin()) {
                    System.out.println();
                    System.out.println("Ура! Вы угадали слово: " + game.getAnswer());
                } else if (result.isGameOver()) {
                    System.out.println();
                    System.out.println("Увы. Попытки закончились. Игра окончена.");
                }
            } catch (InvalidWordFormatException e) {
                System.out.println(e.getMessage() + ". Попробуйте снова.");
            } catch (WordNotFoundInDictionaryException e) {
                System.out.println(e.getMessage() + ". Попробуйте снова.");
            }
            System.out.println();
        }
        if (!game.isWon()) {
            System.out.println("Загаданное слово: " + game.getAnswer());
        }
        log.println("Итог: " + (game.isWon() ? "ПОБЕДА" : "ПОРАЖЕНИЕ")
                + ", ответ: " + game.getAnswer()
                + ", ходов сделано: " + game.getGuessedWords().size());
    }
}

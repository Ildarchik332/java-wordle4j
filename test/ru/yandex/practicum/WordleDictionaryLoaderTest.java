package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.exception.DictionaryLoadingException;
import ru.yandex.exception.EmptyDictionaryException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryLoaderTest {

    private PrintWriter silentLog() {
        return new PrintWriter(new StringWriter());
    }

    @Test
    void loadRealDictionaryReturnsNonEmptyValidWords() throws Exception {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(silentLog());
        WordleDictionary dict = loader.load("words_ru.txt");

        assertFalse(dict.isEmpty());
        assertTrue(dict.size() > 100, "словарь подозрительно мал: " + dict.size());

        for (String w : dict.getWords()) {
            assertTrue(WordleDictionary.hasValidFormat(w),
                    "слово не прошло формат: " + w);
            assertEquals(w, WordleDictionary.normalize(w),
                    "слово не нормализовано: " + w);
            assertFalse(w.contains("ё"), "в словаре осталась ё: " + w);
        }
    }

    @Test
    void loadMissingFileThrowsDictionaryLoadingException() {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(silentLog());
        assertThrows(DictionaryLoadingException.class,
                () -> loader.load("no_such_file.txt"));
    }

    @Test
    void loadNoValidWordsThrowsEmptyDictionaryException() throws Exception {
        Path file = Files.createTempFile("wordle-test", ".txt");
        try {
            Files.writeString(file, "кот\nёж\nhello\n\n", StandardCharsets.UTF_8);
            WordleDictionaryLoader loader = new WordleDictionaryLoader(silentLog());
            assertThrows(EmptyDictionaryException.class,
                    () -> loader.load(file.toString()));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadMixedInputFiltersAndDeduplicates() throws Exception {
        Path file = Files.createTempFile("wordle-test", ".txt");
        try {
            Files.writeString(file, String.join("\n",
                    "герой",
                    "ГЕРОЙ",
                    "ёжики",
                    "ежики",
                    "кот",
                    "hello",
                    ""
            ), StandardCharsets.UTF_8);
            WordleDictionaryLoader loader = new WordleDictionaryLoader(silentLog());
            WordleDictionary dict = loader.load(file.toString());

            assertEquals(2, dict.size());
            assertTrue(dict.contains("герой"));
            assertTrue(dict.contains("ежики"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadUtf8FileReadsCyrillicCorrectly() throws Exception {
        Path file = Files.createTempFile("wordle-test", ".txt");
        try {
            Files.writeString(file, "герой\nгонец\n", StandardCharsets.UTF_8);
            WordleDictionaryLoader loader = new WordleDictionaryLoader(silentLog());
            WordleDictionary dict = loader.load(file.toString());

            assertEquals(2, dict.size());
            assertTrue(dict.contains("герой"), "кириллица прочитана неверно — проверь UTF-8 в FileReader");
            assertTrue(dict.contains("гонец"));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}

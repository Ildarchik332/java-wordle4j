package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {

    @Test
    void normalizeReplacesYo() {
        assertEquals("ежик", WordleDictionary.normalize("ёжик"));
        assertEquals("ежик", WordleDictionary.normalize("ЁЖИК"));
    }

    @Test
    void normalizeTrimsAndLowercases() {
        assertEquals("герой", WordleDictionary.normalize("  ГЕРОЙ  "));
    }

    @Test
    void normalizeEmptyReturnsEmpty() {
        assertEquals("", WordleDictionary.normalize(""));
        assertEquals("", WordleDictionary.normalize("   "));
    }

    @Test
    void hasValidFormatTrue() {
        assertTrue(WordleDictionary.hasValidFormat("герой"));
        assertTrue(WordleDictionary.hasValidFormat("ежики"));
    }

    @Test
    void hasValidFormatFalseLength() {
        assertFalse(WordleDictionary.hasValidFormat("кот"));
        assertFalse(WordleDictionary.hasValidFormat("привет"));
    }

    @Test
    void hasValidFormatFalseNonRussian() {
        assertFalse(WordleDictionary.hasValidFormat("hello"));
        assertFalse(WordleDictionary.hasValidFormat("гeрой"));
        assertFalse(WordleDictionary.hasValidFormat("12345"));
    }

    @Test
    void hasValidFormatNullSafe() {
        assertFalse(WordleDictionary.hasValidFormat(null));
    }

    @Test
    void constructorDefensiveCopy() {
        List<String> source = new ArrayList<>(List.of("герой"));
        WordleDictionary dict = new WordleDictionary(source);
        source.add("гонец");
        assertEquals(1, dict.size());
    }

    @Test
    void getWordsDefensiveCopy() {
        WordleDictionary dict = new WordleDictionary(List.of("герой", "гонец"));
        List<String> external = dict.getWords();
        external.clear();
        assertEquals(2, dict.size());
    }

    @Test
    void containsAndSizeAndIsEmpty() {
        WordleDictionary dict = new WordleDictionary(List.of("герой", "гонец"));
        assertTrue(dict.contains("герой"));
        assertFalse(dict.contains("ааааа"));
        assertEquals(2, dict.size());
        assertFalse(dict.isEmpty());

        WordleDictionary empty = new WordleDictionary(List.of());
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
    }

    @Test
    void normalizeIdempotent() {
        String once = WordleDictionary.normalize("ЁЖИК");
        String twice = WordleDictionary.normalize(once);
        assertEquals(once, twice);
    }

    @Test
    void hasValidFormatWithYo() {
        assertTrue(WordleDictionary.hasValidFormat(WordleDictionary.normalize("ёжики")));
        assertFalse(WordleDictionary.hasValidFormat("ёжики"));
    }
}

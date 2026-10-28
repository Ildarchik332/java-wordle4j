package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private final List<String> words;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    public int size() {
        return words.size();
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public static String normalize(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Слово не должно быть null");
        }
        return word.toLowerCase().replace('ё', 'е').trim();
    }

    /**
     * ожидает нормализованное слово (нижний регистр + ё -> е)
     */
    public static boolean hasValidFormat(String word) {
        if (word == null || word.length() != 5) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c < 'а' || c > 'я') {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }
}

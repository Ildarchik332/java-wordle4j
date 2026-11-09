package ru.yandex.practicum;

import ru.yandex.exception.DictionaryLoadingException;
import ru.yandex.exception.EmptyDictionaryException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        if (log == null) {
            throw new IllegalArgumentException("Лог не может быть null");
        }
        this.log = log;
    }

    public WordleDictionary load(String fileName) throws DictionaryLoadingException {
        log.println("[INFO] Загузка словаря: " + fileName);

        Set<String> uniqueWords = new LinkedHashSet<>();
        int totalLines = 0;
        int skippedLines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                String normalized = WordleDictionary.normalize(line);
                if (normalized.isEmpty() || !WordleDictionary.hasValidFormat(normalized)) {
                    skippedLines++;
                    continue;
                }
                uniqueWords.add(normalized);
            }
        } catch (IOException e) {
            log.println("[ERROR] Не удалось прочитать файл: " + e.getMessage());
            throw new DictionaryLoadingException("Не удалось загрузить словарь из файла: " + fileName, e);
        }
        log.println("[INFO] Строк прочитано: " + totalLines + ", строк пропущено : " + skippedLines);
        log.println("[INFO] Уникальных слов загружено: " + uniqueWords.size());

        if (uniqueWords.isEmpty()) {
            throw new EmptyDictionaryException("Словарь пуст после фильтрации. Нет слов из 5 русских букв");
        }
        List<String> words = new ArrayList<>(uniqueWords);
        log.println("[INFO] Словарь успешно загружен");
        return new WordleDictionary(words);
    }
}

package ru.yandex.exception;

public class WordNotFoundInDictionaryException extends Exception {

    public WordNotFoundInDictionaryException(String message) {
        super(message);
    }
}

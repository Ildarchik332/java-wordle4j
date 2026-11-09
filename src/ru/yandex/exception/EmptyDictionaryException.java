package ru.yandex.exception;

public class EmptyDictionaryException extends RuntimeException {

    public EmptyDictionaryException(String message) {
        super(message);
    }
}

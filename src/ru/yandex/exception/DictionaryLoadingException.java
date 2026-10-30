package ru.yandex.exception;

public class DictionaryLoadingException extends Exception {
    public DictionaryLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

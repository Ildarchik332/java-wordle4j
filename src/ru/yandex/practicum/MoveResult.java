package ru.yandex.practicum;

public class MoveResult {
    private final String word;
    private final String hint;
    private final int stepsLeft;
    private final boolean win;
    private final boolean gameOver;

    public MoveResult(String word, String hint, int stepsLeft, boolean win, boolean gameOver) {
        this.word = word;
        this.hint = hint;
        this.stepsLeft = stepsLeft;
        this.win = win;
        this.gameOver = gameOver;
    }

    public String getWord() {
        return word;
    }

    public String getHint() {
        return hint;
    }

    public int getStepsLeft() {
        return stepsLeft;
    }

    public boolean isWin() {
        return win;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}

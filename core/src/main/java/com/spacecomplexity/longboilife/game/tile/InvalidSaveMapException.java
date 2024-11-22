package com.spacecomplexity.longboilife.game.tile;

/**
 * Exception to indicate an issue when parsing the save map.
 */
public class InvalidSaveMapException extends RuntimeException {
    public InvalidSaveMapException(String message) {
        super(message);
    }
}

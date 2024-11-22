package com.spacecomplexity.longboilife.game.utils;

/**
 * Class representing a 2d Vector where each element is represented by an integer.
 */
public class Vector2Int {
    public int x;
    public int y;

    /**
     * Initialise a blank Vector, (0, 0).
     */
    public Vector2Int() {
        x = 0;
        y = 0;
    }

    /**
     * Initialise the vector with values.
     *
     * @param x the initial x value.
     * @param y the intel y value.
     */
    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        // Check for the same reference
        if (this == obj) return true;
        // Check class type
        if (obj == null || getClass() != obj.getClass()) return false;

        // Check if fields are equal
        Vector2Int that = (Vector2Int) obj;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        return result;
    }

    /**
     * Returns the addition of another vector on this vector.
     *
     * @param other the vector to add.
     * @return a new Vector2Int representing the result of the addition.
     */
    public Vector2Int add(Vector2Int other) {
        return new Vector2Int(this.x + other.x, this.y + other.y);
    }

    /**
     * Returns the subtraction of another vector from this vector.
     *
     * @param other the vector to subtract.
     * @return a new Vector2Int representing the result of the subtraction.
     */
    public Vector2Int subtract(Vector2Int other) {
        return new Vector2Int(this.x - other.x, this.y - other.y);
    }

    /**
     * Returns the magnitude squared of the vector.
     *
     * @return the squared magnitude of the vector.
     */
    public int mag2() {
        return x * x + y * y;
    }

    /**
     * Returns the magnitude of the vector.
     *
     * @return vectors magnitude.
     */
    public float mag() {
        return (float) Math.sqrt(mag2());
    }
}

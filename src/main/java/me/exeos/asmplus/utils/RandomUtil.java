package me.exeos.asmplus.utils;

public class RandomUtil {

    public static int getInt(int min, int max) {
        if (min == max) {
            return min;
        }

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        return (int) ((Math.random() * (max - min)) + min);
    }

    public static boolean chance(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 - 100");
        }
        return percentage <= getInt(0, 100);
    }
}

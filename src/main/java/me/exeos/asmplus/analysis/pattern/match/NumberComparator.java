package me.exeos.asmplus.analysis.pattern.match;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class NumberComparator {

    public static int compareNumbers(Number a, Number b) {
        Objects.requireNonNull(a, "Param a must not be null");
        Objects.requireNonNull(b, "Param b must not be null");

        if (a.equals(b)) return 0;

        // If both are integral, compare as BigInteger for exactness
        if (isIntegralNumber(a) && isIntegralNumber(b)) {
            BigInteger aInt = toBigInteger(a);
            BigInteger bInt = toBigInteger(b);
            return aInt.compareTo(bInt);
        }

        // If either is float/double, check NaN/infinity cases first
        if (isFloatingPoint(a) || isFloatingPoint(b)) {
            double da = a.doubleValue();
            double db = b.doubleValue();
            if (Double.isNaN(da) || Double.isNaN(db)) {
                throw new IllegalArgumentException("Cannot compare NaN");
            }

            if (Double.isInfinite(da) || Double.isInfinite(db)) {
                return Double.compare(da, db);
            }
        }

        // Fallback to BigDecimal comparison for mixed or fractional numbers
        BigDecimal bdA = toBigDecimal(a);
        BigDecimal bdB = toBigDecimal(b);
        return bdA.compareTo(bdB);
    }

    public static int compareInt(int a, int b) {
        return BigInteger.valueOf(a).compareTo(BigInteger.valueOf(b));
    }

    private static boolean isIntegralNumber(Number n) {
        return (n instanceof Byte) ||
                (n instanceof Short) ||
                (n instanceof Integer) ||
                (n instanceof Long) ||
                (n instanceof BigInteger) ||
                (n instanceof BigDecimal && ((BigDecimal) n).scale() == 0);
    }

    private static boolean isFloatingPoint(Number n) {
        return (n instanceof Float) || (n instanceof Double);
    }

    private static BigInteger toBigInteger(Number n) {
        if (n instanceof BigInteger) return (BigInteger) n;
        if (n instanceof BigDecimal) return ((BigDecimal) n).toBigIntegerExact();
        return BigInteger.valueOf(n.longValue());
    }

    private static BigDecimal toBigDecimal(Number n) {
        if (n instanceof BigDecimal) return (BigDecimal) n;
        if (n instanceof BigInteger) return new BigDecimal((BigInteger) n);
        if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) {
            return BigDecimal.valueOf(n.longValue());
        }
        if (n instanceof Double || n instanceof Float) {
            double d = n.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException("Cannot convert NaN/Infinite to BigDecimal");
            }

            return BigDecimal.valueOf(d);
        }

        return new BigDecimal(n.toString());
    }
}

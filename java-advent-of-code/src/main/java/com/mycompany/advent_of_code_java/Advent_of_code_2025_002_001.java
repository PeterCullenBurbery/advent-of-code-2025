package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_002_001 {

    public static void main(String[] args) {
        String inputPath = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-002\\input.txt";

        try {
            BigInteger ans = solve(Path.of(inputPath));
            System.out.println(ans);
        } catch (IOException e) {
            System.err.println("Failed to read input: " + inputPath);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static BigInteger solve(Path inputFile) throws IOException {
        String line = Files.readString(inputFile).trim();
        if (line.isEmpty()) return BigInteger.ZERO;

        String[] parts = line.split(",");
        long[][] ranges = new long[parts.length][2];

        long globalMax = 0;
        for (int i = 0; i < parts.length; i++) {
            String token = parts[i].trim();
            if (token.isEmpty()) continue;

            int dash = token.indexOf('-');
            if (dash < 0) {
                throw new IllegalArgumentException("Bad range token (missing '-'): " + token);
            }

            long a = Long.parseLong(token.substring(0, dash).trim());
            long b = Long.parseLong(token.substring(dash + 1).trim());
            if (a > b) {
                long t = a; a = b; b = t;
            }

            ranges[i][0] = a;
            ranges[i][1] = b;
            if (b > globalMax) globalMax = b;
        }

        int maxDigits = digits(globalMax);
        int maxK = maxDigits / 2;

        long[] pow10 = new long[Math.max(2, maxK + 1)];
        pow10[0] = 1;
        for (int i = 1; i < pow10.length; i++) {
            pow10[i] = pow10[i - 1] * 10L;
        }

        BigInteger total = BigInteger.ZERO;

        for (long[] r : ranges) {
            long A = r[0], B = r[1];
            if (A == 0 && B == 0) continue;

            for (int k = 1; k <= maxK; k++) {
                long m = pow10[k] + 1L;          // N = X * m
                long xMinDigits = pow10[k - 1];  // smallest k-digit X
                long xMaxDigits = pow10[k] - 1;  // largest k-digit X

                long lo = ceilDiv(A, m);
                long hi = B / m;

                if (lo < xMinDigits) lo = xMinDigits;
                if (hi > xMaxDigits) hi = xMaxDigits;

                if (lo <= hi) {
                    total = total.add(sumRepeated(lo, hi, m));
                }
            }
        }

        return total;
    }

    // Sum over X in [lo..hi] of N = X*m, where m = 10^k + 1
    private static BigInteger sumRepeated(long lo, long hi, long m) {
        BigInteger count = BigInteger.valueOf(hi - lo + 1L);
        BigInteger sumX = BigInteger.valueOf(lo).add(BigInteger.valueOf(hi)).multiply(count).divide(BigInteger.valueOf(2L));
        return BigInteger.valueOf(m).multiply(sumX);
    }

    // For positive integers only
    private static long ceilDiv(long a, long b) {
        return (a + b - 1L) / b;
    }

    private static int digits(long x) {
        if (x == 0) return 1;
        int d = 0;
        long t = x;
        while (t > 0) {
            d++;
            t /= 10;
        }
        return d;
    }
}
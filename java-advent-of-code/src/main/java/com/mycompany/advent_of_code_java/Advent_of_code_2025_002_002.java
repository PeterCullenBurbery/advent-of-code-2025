package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_002_002 {

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
            if (dash < 0) throw new IllegalArgumentException("Bad range token: " + token);

            long a = Long.parseLong(token.substring(0, dash).trim());
            long b = Long.parseLong(token.substring(dash + 1).trim());
            if (a > b) { long t = a; a = b; b = t; }

            ranges[i][0] = a;
            ranges[i][1] = b;
            if (b > globalMax) globalMax = b;
        }

        // Generate all invalid IDs <= globalMax (unique)
        long[] invalid = generateAllRepeatedNumbersUpTo(globalMax);

        // Prefix sums for O(log N) range sums
        BigInteger[] pref = new BigInteger[invalid.length + 1];
        pref[0] = BigInteger.ZERO;
        for (int i = 0; i < invalid.length; i++) {
            pref[i + 1] = pref[i].add(BigInteger.valueOf(invalid[i]));
        }

        BigInteger total = BigInteger.ZERO;
        for (long[] r : ranges) {
            long A = r[0], B = r[1];
            int left = lowerBound(invalid, A);
            int right = upperBound(invalid, B);
            if (left < right) {
                total = total.add(pref[right].subtract(pref[left]));
            }
        }

        return total;
    }

    /**
     * Generates all numbers <= max that can be written as some digit block repeated >=2 times,
     * with no leading zeros (so the block must be a k-digit number with first digit != 0).
     *
     * Returns sorted unique array.
     */
    private static long[] generateAllRepeatedNumbersUpTo(long max) {
        if (max < 11) { // smallest repetition with no leading zeros is 11
            return new long[0];
        }

        int maxDigits = digits(max);

        // Precompute powers of 10 up to maxDigits
        long[] pow10 = new long[maxDigits + 1];
        pow10[0] = 1L;
        for (int i = 1; i <= maxDigits; i++) {
            pow10[i] = pow10[i - 1] * 10L;
        }

        LongList list = new LongList(120_000);

        // Total length n, block length k, repeat count t = n/k >= 2
        for (int n = 2; n <= maxDigits; n++) {
            for (int k = 1; k < n; k++) {
                if (n % k != 0) continue;
                int t = n / k;
                if (t < 2) continue;

                long blockPow = pow10[k];
                long startX = pow10[k - 1];     // smallest k-digit (no leading zero)
                long endX = pow10[k] - 1;       // largest k-digit

                for (long x = startX; x <= endX; x++) {
                    long v = 0L;

                    // build v = repeat x, t times: v = ((x)*10^k + x)*10^k + x ... t times
                    for (int rep = 0; rep < t; rep++) {
                        v = v * blockPow + x;
                        // v cannot overflow for our digit sizes (<= 10 digits here), but keep it simple.
                    }

                    if (v > max) {
                        // For fixed (n,k,t), v increases with x, so we can break early.
                        break;
                    }

                    list.add(v);
                }
            }
        }

        long[] arr = list.toArray();
        Arrays.sort(arr);

        // Deduplicate (e.g., 1111 can arise from "1 repeated 4" and "11 repeated 2")
        int m = 0;
        for (int i = 0; i < arr.length; i++) {
            if (i == 0 || arr[i] != arr[i - 1]) {
                arr[m++] = arr[i];
            }
        }
        return Arrays.copyOf(arr, m);
    }

    private static int digits(long x) {
        int d = 0;
        long t = x;
        while (t > 0) {
            d++;
            t /= 10;
        }
        return Math.max(d, 1);
    }

    // Standard lower_bound: first index i with a[i] >= key
    private static int lowerBound(long[] a, long key) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid] < key) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    // Standard upper_bound: first index i with a[i] > key
    private static int upperBound(long[] a, long key) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid] <= key) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    // Simple dynamic long list (avoids boxing)
    private static final class LongList {
        private long[] data;
        private int size;

        LongList(int initialCapacity) {
            data = new long[Math.max(16, initialCapacity)];
            size = 0;
        }

        void add(long v) {
            if (size == data.length) {
                data = Arrays.copyOf(data, data.length * 2);
            }
            data[size++] = v;
        }

        long[] toArray() {
            return Arrays.copyOf(data, size);
        }
    }
}
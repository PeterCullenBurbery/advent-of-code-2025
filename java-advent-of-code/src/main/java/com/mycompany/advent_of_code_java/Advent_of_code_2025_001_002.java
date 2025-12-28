package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_001_002 {
    private static final int MOD = 100;

    public static void main(String[] args) {
        String inputPath = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-001\\input.txt";

        try {
            long password = solve(Path.of(inputPath));
            System.out.println(password);
        } catch (IOException e) {
            System.err.println("Failed to read input: " + inputPath);
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input format: " + e.getMessage());
            System.exit(2);
        }
    }

    public static long solve(Path inputFile) throws IOException {
        int pos = 50;
        long zeroClicks = 0;

        try (BufferedReader br = Files.newBufferedReader(inputFile)) {
            String line;
            long lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;

                char dir = line.charAt(0);
                if (dir != 'L' && dir != 'R') {
                    throw new IllegalArgumentException("Line " + lineNo + ": must start with L or R, got: " + line);
                }

                String distStr = line.substring(1).trim();
                if (distStr.isEmpty()) {
                    throw new IllegalArgumentException("Line " + lineNo + ": missing distance: " + line);
                }

                long dist;
                try {
                    dist = Long.parseLong(distStr);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Line " + lineNo + ": bad distance: " + line);
                }
                if (dist < 0) {
                    throw new IllegalArgumentException("Line " + lineNo + ": distance must be non-negative: " + line);
                }
                if (dist == 0) continue; // no clicks, no movement

                // Count how many clicks during this rotation land on 0
                zeroClicks += countHitsZeroDuringRotation(pos, dir, dist);

                // Update final position after full rotation
                long distMod = dist % MOD;
                if (dir == 'R') {
                    pos = (int) ((pos + distMod) % MOD);
                } else { // 'L'
                    pos = (int) ((pos - distMod) % MOD);
                    if (pos < 0) pos += MOD;
                }
            }
        }

        return zeroClicks;
    }

    /**
     * Counts the number of click-steps k in [1..dist] such that the dial points at 0
     * immediately after that click, starting from 'pos' before the rotation.
     */
    private static long countHitsZeroDuringRotation(int pos, char dir, long dist) {
        // Find the first k (1..100) that hits 0, then every 100 after that.
        int kFirst;

        if (dir == 'R') {
            // pos + k ≡ 0 (mod 100)  =>  k ≡ -pos ≡ (100 - pos) (mod 100)
            int k0 = (MOD - (pos % MOD)) % MOD; // in [0..99]
            kFirst = (k0 == 0) ? MOD : k0;      // if 0, next hit is at 100
        } else {
            // pos - k ≡ 0 (mod 100)  =>  k ≡ pos (mod 100)
            int k0 = pos % MOD;                // in [0..99]
            kFirst = (k0 == 0) ? MOD : k0;      // if 0, next hit is at 100
        }

        if (kFirst > dist) return 0;

        // hits at kFirst, kFirst+100, kFirst+200, ... <= dist
        return 1 + (dist - kFirst) / MOD;
    }
}

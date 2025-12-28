package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_007_002 {

    static char[][] g;
    static int H, W;

    // memo[r][c] = number of timelines from state (r,c)
    static BigInteger[][] memo;
    // 0 = uncomputed, 1 = computing (for cycle detection), 2 = done
    static byte[][] state;

    static BigInteger ways(int r, int c) {
        // Off-grid horizontally => timeline ends
        if (c < 0 || c >= W) return BigInteger.ONE;
        // Off-grid vertically => timeline ends (exits manifold)
        if (r < 0 || r >= H) return BigInteger.ONE;

        if (state[r][c] == 2) return memo[r][c];
        if (state[r][c] == 1) {
            // Would indicate a same-row cycle (e.g., adjacent splitters bouncing).
            // Puzzle inputs should avoid this; fail fast if it happens.
            throw new IllegalStateException("Cycle detected at r=" + r + ", c=" + c);
        }

        state[r][c] = 1;

        BigInteger ans;
        if (g[r][c] == '^') {
            // Split: choose left OR right (many-worlds => add counts)
            ans = ways(r, c - 1).add(ways(r, c + 1));
        } else {
            // Fall straight down
            ans = ways(r + 1, c);
        }

        memo[r][c] = ans;
        state[r][c] = 2;
        return ans;
    }

    public static void main(String[] args) throws Exception {
        String inputPath = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-007\\input.txt";

        List<String> lines = Files.readAllLines(Path.of(inputPath));
        if (lines.isEmpty()) {
            System.out.println(0);
            return;
        }

        H = lines.size();
        W = 0;
        for (String s : lines) W = Math.max(W, s.length());

        g = new char[H][W];
        for (int r = 0; r < H; r++) {
            String s = lines.get(r);
            for (int c = 0; c < W; c++) {
                g[r][c] = (c < s.length()) ? s.charAt(c) : '.';
            }
        }

        // Locate S
        int sRow = -1, sCol = -1;
        outer:
        for (int r = 0; r < H; r++) {
            for (int c = 0; c < W; c++) {
                if (g[r][c] == 'S') {
                    sRow = r;
                    sCol = c;
                    break outer;
                }
            }
        }
        if (sRow < 0) throw new IllegalArgumentException("No S found in input.");

        memo = new BigInteger[H][W];
        state = new byte[H][W];

        // Particle enters at S and moves downward, so first cell “visited” is row below S.
        BigInteger result = ways(sRow + 1, sCol);

        System.out.println(result.toString());
    }
}
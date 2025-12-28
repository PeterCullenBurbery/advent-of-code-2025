package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_007_001 {

    public static void main(String[] args) throws Exception {
        String inputPath = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-007\\input.txt";

        List<String> lines = Files.readAllLines(Path.of(inputPath));
        if (lines.isEmpty()) {
            System.out.println(0);
            return;
        }

        int H = lines.size();
        int W = 0;
        for (String s : lines) W = Math.max(W, s.length());

        // Pad for safe indexing.
        char[][] g = new char[H][W];
        for (int r = 0; r < H; r++) {
            String s = lines.get(r);
            for (int c = 0; c < W; c++) {
                g[r][c] = (c < s.length()) ? s.charAt(c) : '.';
            }
        }

        // Find S (assumed exactly one).
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

        // Active beam columns on the current row.
        // Use BitSet for speed and to automatically handle overlaps (set semantics).
        BitSet active = new BitSet(W);
        active.set(sCol);

        long splitCount = 0;

        // Beam moves downward starting from the row below S.
        for (int r = sRow + 1; r < H; r++) {
            BitSet next = new BitSet(W);

            // For each active column at this row:
            for (int c = active.nextSetBit(0); c >= 0; c = active.nextSetBit(c + 1)) {
                if (g[r][c] == '^') {
                    // This splitter is hit => one split event.
                    splitCount++;

                    // Emit left and right beams from immediate neighbors, if in bounds.
                    if (c - 1 >= 0) next.set(c - 1);
                    if (c + 1 < W) next.set(c + 1);
                } else {
                    // Pass through unchanged.
                    next.set(c);
                }
            }

            active = next;

            // Early exit: if no beams remain (can happen if all split off-grid), stop.
            if (active.isEmpty()) break;
        }

        System.out.println(splitCount);
    }
}
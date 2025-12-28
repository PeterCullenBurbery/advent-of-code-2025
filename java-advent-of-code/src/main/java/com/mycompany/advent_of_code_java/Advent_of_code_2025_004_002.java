package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_004_002 {

    public static void main(String[] args) {
        String Input_path = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-004\\input.txt";

        try {
            long Total_removed = Solve(Input_path);
            System.out.println(Total_removed);
        } catch (IOException e) {
            System.err.println("Failed to read input: " + Input_path);
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
            System.exit(2);
        }
    }

    public static long Solve(String Input_path) throws IOException {
        List<String> Lines = Files.readAllLines(Path.of(Input_path));
        if (Lines.isEmpty()) return 0;

        int Rows = Lines.size();
        int Cols = Lines.get(0).length();

        for (int r = 0; r < Rows; r++) {
            if (Lines.get(r).length() != Cols) {
                throw new IllegalArgumentException("Grid is not rectangular (line " + (r + 1) + ").");
            }
        }

        char[][] Grid = new char[Rows][Cols];
        int[][] Adjacent_rolls = new int[Rows][Cols];

        long Total_rolls = 0;

        for (int r = 0; r < Rows; r++) {
            String Line = Lines.get(r);
            for (int c = 0; c < Cols; c++) {
                char ch = Line.charAt(c);
                if (ch != '@' && ch != '.') {
                    throw new IllegalArgumentException(
                            "Unexpected character '" + ch + "' at row " + (r + 1) + ", col " + (c + 1) + "."
                    );
                }
                Grid[r][c] = ch;
                if (ch == '@') Total_rolls++;
            }
        }

        int[] Dr = {-1,-1,-1, 0, 0, 1, 1, 1};
        int[] Dc = {-1, 0, 1,-1, 1,-1, 0, 1};

        // Compute initial adjacent-roll counts.
        for (int r = 0; r < Rows; r++) {
            for (int c = 0; c < Cols; c++) {
                if (Grid[r][c] != '@') continue;

                int Count = 0;
                for (int k = 0; k < 8; k++) {
                    int rr = r + Dr[k];
                    int cc = c + Dc[k];
                    if (rr < 0 || rr >= Rows || cc < 0 || cc >= Cols) continue;
                    if (Grid[rr][cc] == '@') Count++;
                }
                Adjacent_rolls[r][c] = Count;
            }
        }

        ArrayDeque<Integer> Queue = new ArrayDeque<>();
        boolean[][] Enqueued = new boolean[Rows][Cols];

        // Seed queue with initially removable rolls.
        for (int r = 0; r < Rows; r++) {
            for (int c = 0; c < Cols; c++) {
                if (Grid[r][c] == '@' && Adjacent_rolls[r][c] < 4) {
                    Queue.addLast(Encode(r, c, Cols));
                    Enqueued[r][c] = true;
                }
            }
        }

        long Removed = 0;

        while (!Queue.isEmpty()) {
            int Code = Queue.removeFirst();
            int r = Code / Cols;
            int c = Code % Cols;

            // It might have been removed already or become non-roll; skip if so.
            if (Grid[r][c] != '@') continue;

            // Remove it.
            Grid[r][c] = '.';
            Removed++;

            // Update neighbors: each neighboring roll loses one adjacent roll.
            for (int k = 0; k < 8; k++) {
                int rr = r + Dr[k];
                int cc = c + Dc[k];
                if (rr < 0 || rr >= Rows || cc < 0 || cc >= Cols) continue;

                if (Grid[rr][cc] == '@') {
                    Adjacent_rolls[rr][cc]--;

                    // If it just became removable, enqueue it.
                    if (Adjacent_rolls[rr][cc] < 4 && !Enqueued[rr][cc]) {
                        Queue.addLast(Encode(rr, cc, Cols));
                        Enqueued[rr][cc] = true;
                    }
                }
            }
        }

        // Removed is the maximum removable by repeatedly removing accessible rolls.
        // (Order does not reduce removability here; any eligible roll can be removed.)
        return Removed;
    }

    private static int Encode(int r, int c, int Cols) {
        return r * Cols + c;
    }
}
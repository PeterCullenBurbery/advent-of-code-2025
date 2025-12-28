package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_004_001 {

    public static void main(String[] args) {
        String Input_path = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-004\\input.txt";

        try {
            long Accessible_rolls = Solve(Input_path);
            System.out.println(Accessible_rolls);
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
            }
        }

        int[] Dr = {-1,-1,-1, 0, 0, 1, 1, 1};
        int[] Dc = {-1, 0, 1,-1, 1,-1, 0, 1};

        long Count = 0;

        for (int r = 0; r < Rows; r++) {
            for (int c = 0; c < Cols; c++) {
                if (Grid[r][c] != '@') continue;

                int Adjacent_rolls = 0;

                for (int k = 0; k < 8; k++) {
                    int rr = r + Dr[k];
                    int cc = c + Dc[k];
                    if (rr < 0 || rr >= Rows || cc < 0 || cc >= Cols) continue;
                    if (Grid[rr][cc] == '@') Adjacent_rolls++;
                }

                if (Adjacent_rolls < 4) {
                    Count++;
                }
            }
        }

        return Count;
    }
}
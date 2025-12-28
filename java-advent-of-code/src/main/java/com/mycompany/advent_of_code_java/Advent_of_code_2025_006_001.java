package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_006_001 {

    public static void main(String[] args) {
        String Input_path = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-006\\input.txt";

        try {
            BigInteger Grand_total = Solve(Input_path);
            System.out.println(Grand_total);
        } catch (IOException e) {
            System.err.println("Failed to read input: " + Input_path);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static BigInteger Solve(String Input_path) throws IOException {
        List<String> Raw_lines = Files.readAllLines(Path.of(Input_path));
        if (Raw_lines.isEmpty()) return BigInteger.ZERO;

        // Expect N number rows + 1 operator row (the last row).
        int Row_count = Raw_lines.size();
        int Operator_row_index = Row_count - 1;

        int Width = 0;
        for (String s : Raw_lines) {
            if (s.length() > Width) Width = s.length();
        }

        // Pad all rows to the same width so column scanning is safe.
        String[] Lines = new String[Row_count];
        for (int r = 0; r < Row_count; r++) {
            String s = Raw_lines.get(r);
            if (s.length() < Width) {
                Lines[r] = s + " ".repeat(Width - s.length());
            } else {
                Lines[r] = s;
            }
        }

        BigInteger Total = BigInteger.ZERO;

        int Col = 0;
        while (Col < Width) {
            // Skip separator columns (full column of spaces across all rows).
            while (Col < Width && Is_full_space_column(Lines, Col)) {
                Col++;
            }
            if (Col >= Width) break;

            // Start of a problem segment.
            int Start = Col;
            while (Col < Width && !Is_full_space_column(Lines, Col)) {
                Col++;
            }
            int End = Col; // [Start, End)

            // Parse one problem from this segment.
            BigInteger Problem_value = Evaluate_problem_segment(Lines, Start, End, Operator_row_index);
            Total = Total.add(Problem_value);
        }

        return Total;
    }

    private static boolean Is_full_space_column(String[] Lines, int Col) {
        for (String Line : Lines) {
            if (Col < Line.length() && Line.charAt(Col) != ' ') {
                return false;
            }
        }
        return true;
    }

    private static BigInteger Evaluate_problem_segment(
            String[] Lines,
            int Start,
            int End,
            int Operator_row_index
    ) {
        // Operator is taken from the operator row inside the segment.
        String Op_text = Lines[Operator_row_index].substring(Start, End).trim();
        if (Op_text.isEmpty()) {
            throw new IllegalArgumentException("Missing operator in segment: [" + Start + "," + End + ")");
        }

        char Op = Op_text.charAt(0);
        if (Op != '+' && Op != '*') {
            throw new IllegalArgumentException("Invalid operator '" + Op_text + "' in segment: [" + Start + "," + End + ")");
        }

        BigInteger Acc = (Op == '*') ? BigInteger.ONE : BigInteger.ZERO;

        for (int r = 0; r < Operator_row_index; r++) {
            String Cell = Lines[r].substring(Start, End).trim();
            if (Cell.isEmpty()) {
                // If your actual puzzle guarantees a number in every row, treat empty as an error.
                // If not, you could instead "continue;" here.
                throw new IllegalArgumentException("Missing number on row " + r + " in segment: [" + Start + "," + End + ")");
            }

            BigInteger Value = new BigInteger(Cell);
            if (Op == '*') {
                Acc = Acc.multiply(Value);
            } else {
                Acc = Acc.add(Value);
            }
        }

        return Acc;
    }
}
package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_006_002 {

    public static void main(String[] Args) {
        String Input_path = (Args.length >= 1)
                ? Args[0]
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

        int Row_count = Raw_lines.size();
        int Operator_row_index = Row_count - 1;

        int Width = 0;
        for (String Line : Raw_lines) {
            Width = Math.max(Width, Line.length());
        }

        // Pad all lines to equal width for safe column indexing.
        String[] Lines = new String[Row_count];
        for (int r = 0; r < Row_count; r++) {
            String Line = Raw_lines.get(r);
            Lines[r] = (Line.length() < Width) ? (Line + " ".repeat(Width - Line.length())) : Line;
        }

        BigInteger Total = BigInteger.ZERO;

        int Col = 0;
        while (Col < Width) {
            // Skip separator columns (a full column of spaces across ALL rows).
            while (Col < Width && Is_full_space_column(Lines, Col)) {
                Col++;
            }
            if (Col >= Width) break;

            int Start = Col;
            while (Col < Width && !Is_full_space_column(Lines, Col)) {
                Col++;
            }
            int End = Col; // segment is [Start, End)

            Total = Total.add(Evaluate_problem_segment_right_to_left_columns(Lines, Start, End, Operator_row_index));
        }

        return Total;
    }

    private static boolean Is_full_space_column(String[] Lines, int Col) {
        for (String Line : Lines) {
            if (Line.charAt(Col) != ' ') return false;
        }
        return true;
    }

    private static BigInteger Evaluate_problem_segment_right_to_left_columns(
            String[] Lines,
            int Start,
            int End,
            int Operator_row_index
    ) {
        char Op = Find_operator_in_segment(Lines[Operator_row_index], Start, End);

        // Parse numbers as columns, read right-to-left (End-1 down to Start).
        BigInteger Acc = (Op == '*') ? BigInteger.ONE : BigInteger.ZERO;
        boolean Saw_any_number = false;

        for (int c = End - 1; c >= Start; c--) {
            String Number_text = Read_column_number(Lines, c, Operator_row_index);
            if (Number_text.isEmpty()) continue;

            Saw_any_number = true;
            BigInteger Value = new BigInteger(Number_text);

            if (Op == '*') {
                Acc = Acc.multiply(Value);
            } else {
                Acc = Acc.add(Value);
            }
        }

        if (!Saw_any_number) {
            throw new IllegalArgumentException("No numbers found in segment: [" + Start + "," + End + ")");
        }

        return Acc;
    }

    private static char Find_operator_in_segment(String Operator_line, int Start, int End) {
        for (int c = Start; c < End; c++) {
            char ch = Operator_line.charAt(c);
            if (ch == '+' || ch == '*') return ch;
        }
        throw new IllegalArgumentException("Missing operator in segment: [" + Start + "," + End + ")");
    }

    private static String Read_column_number(String[] Lines, int Col, int Operator_row_index) {
        StringBuilder Digits = new StringBuilder();

        for (int r = 0; r < Operator_row_index; r++) {
            char ch = Lines[r].charAt(Col);
            if (ch >= '0' && ch <= '9') {
                Digits.append(ch);
            } else if (ch == ' ') {
                // ignore
            } else {
                // If your input is guaranteed to be digits/spaces only, this catches unexpected characters.
                throw new IllegalArgumentException(
                        "Unexpected character '" + ch + "' at row " + r + ", col " + Col
                );
            }
        }

        return Digits.toString();
    }
}
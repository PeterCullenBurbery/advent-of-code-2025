package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_003_002 {

    public static void main(String[] args) {
        String Input_path = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-003\\input.txt";

        try {
            BigInteger Total_output_joltage = Solve(Input_path, 12);
            System.out.println(Total_output_joltage);
        } catch (IOException e) {
            System.err.println("Failed to read input: " + Input_path);
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
            System.exit(2);
        }
    }

    public static BigInteger Solve(String Input_path, int Digits_to_pick) throws IOException {
        BigInteger Sum = BigInteger.ZERO;

        try (BufferedReader Buffered_reader = Files.newBufferedReader(Path.of(Input_path))) {
            String Line;
            long Line_number = 0;

            while ((Line = Buffered_reader.readLine()) != null) {
                Line_number++;
                Line = Line.trim();
                if (Line.isEmpty()) {
                    continue;
                }

                if (!All_digits(Line)) {
                    throw new IllegalArgumentException("Line " + Line_number + " contains non-digit characters.");
                }

                if (Line.length() < Digits_to_pick) {
                    throw new IllegalArgumentException(
                            "Line " + Line_number + " length " + Line.length() +
                            " is less than required " + Digits_to_pick + " digits."
                    );
                }

                String Max_joltage_string = Max_subsequence_of_length(Line, Digits_to_pick);
                Sum = Sum.add(new BigInteger(Max_joltage_string));
            }
        }

        return Sum;
    }

    private static boolean All_digits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the lexicographically largest subsequence of length K (digits only).
     * This is equivalent to the numerically largest K-digit number when all outputs have length K.
     */
    private static String Max_subsequence_of_length(String Digits, int K) {
        int N = Digits.length();
        int Deletes_allowed = N - K;

        char[] Stack = new char[N];
        int Stack_size = 0;

        for (int i = 0; i < N; i++) {
            char Current = Digits.charAt(i);

            while (Deletes_allowed > 0 && Stack_size > 0 && Stack[Stack_size - 1] < Current) {
                Stack_size--;
                Deletes_allowed--;
            }

            Stack[Stack_size++] = Current;
        }

        // If we still can delete, delete from the end (least harmful).
        Stack_size -= Deletes_allowed;

        // Take the first K digits.
        return new String(Stack, 0, K);
    }
}
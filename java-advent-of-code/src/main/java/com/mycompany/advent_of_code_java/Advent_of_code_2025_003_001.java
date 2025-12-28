package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_003_001 {

    public static void main(String[] args) {
        String Input_path = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-003\\input.txt";

        try {
            BigInteger Total_output_joltage = Solve(Input_path);
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

    public static BigInteger Solve(String Input_path) throws IOException {
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

                if (Line.length() < 2) {
                    throw new IllegalArgumentException("Line " + Line_number + " has fewer than 2 digits.");
                }

                int Max_joltage_for_bank = Max_two_digit_from_line(Line);
                Sum = Sum.add(BigInteger.valueOf(Max_joltage_for_bank));
            }
        }

        return Sum;
    }

    private static int Max_two_digit_from_line(String Bank_digits) {
        int Length = Bank_digits.length();

        int[] Digits = new int[Length];
        for (int i = 0; i < Length; i++) {
            char c = Bank_digits.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("Non-digit character '" + c + "' in line: " + Bank_digits);
            }
            Digits[i] = c - '0';
        }

        int[] Suffix_max = new int[Length];
        Suffix_max[Length - 1] = Digits[Length - 1];
        for (int i = Length - 2; i >= 0; i--) {
            Suffix_max[i] = Math.max(Digits[i], Suffix_max[i + 1]);
        }

        int Best = -1;
        for (int i = 0; i < Length - 1; i++) {
            int Tens_digit = Digits[i];
            int Ones_digit = Suffix_max[i + 1];
            int Value = 10 * Tens_digit + Ones_digit;

            if (Value > Best) {
                Best = Value;
            }

            if (Best == 99) {
                break;
            }
        }

        return Best;
    }
}
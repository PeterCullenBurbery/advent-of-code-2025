package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_001_001 {
    private static final int MOD = 100;

    public static void main(String[] args) {
        // Default to your shown input path if no argument is provided.
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
        long countAtZero = 0;

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

                int dist;
                try {
                    dist = Integer.parseInt(distStr);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Line " + lineNo + ": bad distance: " + line);
                }

                dist = ((dist % MOD) + MOD) % MOD; // normalize, just in case

                if (dir == 'R') {
                    pos = (pos + dist) % MOD;
                } else { // 'L'
                    pos = (pos - dist) % MOD;
                    if (pos < 0) pos += MOD;
                }

                if (pos == 0) countAtZero++;
            }
        }

        return countAtZero;
    }
}
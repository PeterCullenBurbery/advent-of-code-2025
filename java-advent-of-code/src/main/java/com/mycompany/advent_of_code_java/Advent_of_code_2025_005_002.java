package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_005_002 {

    private static final class Range {
        long Start;
        long End;

        Range(long Start, long End) {
            this.Start = Start;
            this.End = End;
        }
    }

    public static void main(String[] args) {
        String Input_path = (args.length >= 1)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-005\\input.txt";

        try {
            long Fresh_total = Solve(Input_path);
            System.out.println(Fresh_total);
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

        List<Range> Ranges = new ArrayList<>();
        for (String Raw_line : Lines) {
            String Line = Raw_line.trim();
            if (Line.isEmpty()) break; // stop at blank line; part two ignores the rest
            Ranges.add(Parse_range(Line));
        }

        List<Range> Merged = Merge_ranges(Ranges);

        long Total = 0;
        for (Range r : Merged) {
            // Inclusive range length.
            Total += (r.End - r.Start + 1);
        }
        return Total;
    }

    private static Range Parse_range(String Line) {
        int Dash = Line.indexOf('-');
        if (Dash < 0) {
            throw new IllegalArgumentException("Range missing '-': " + Line);
        }
        String A = Line.substring(0, Dash).trim();
        String B = Line.substring(Dash + 1).trim();

        long Start = Parse_long_strict(A);
        long End = Parse_long_strict(B);

        if (End < Start) {
            throw new IllegalArgumentException("Range end < start: " + Line);
        }
        return new Range(Start, End);
    }

    private static long Parse_long_strict(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid integer: " + s);
        }
    }

    private static List<Range> Merge_ranges(List<Range> Ranges) {
        if (Ranges.isEmpty()) return List.of();

        Ranges.sort((r1, r2) -> {
            int c = Long.compare(r1.Start, r2.Start);
            if (c != 0) return c;
            return Long.compare(r1.End, r2.End);
        });

        List<Range> Out = new ArrayList<>();
        Range Current = new Range(Ranges.get(0).Start, Ranges.get(0).End);

        for (int idx = 1; idx < Ranges.size(); idx++) {
            Range Next = Ranges.get(idx);

            // Merge overlapping OR adjacent ranges (adjacent because inclusive).
            if (Next.Start <= Current.End + 1) {
                if (Next.End > Current.End) Current.End = Next.End;
            } else {
                Out.add(Current);
                Current = new Range(Next.Start, Next.End);
            }
        }
        Out.add(Current);
        return Out;
    }
}
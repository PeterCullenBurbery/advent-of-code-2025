package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_005_001 {

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
            long Fresh_count = Solve(Input_path);
            System.out.println(Fresh_count);
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
        int i = 0;

        // Read ranges until blank line.
        for (; i < Lines.size(); i++) {
            String Line = Lines.get(i).trim();
            if (Line.isEmpty()) {
                i++; // move past blank line
                break;
            }
            Ranges.add(Parse_range(Line));
        }

        // Merge ranges.
        List<Range> Merged = Merge_ranges(Ranges);

        long Count = 0;
        // Remaining lines are available IDs.
        for (; i < Lines.size(); i++) {
            String Line = Lines.get(i).trim();
            if (Line.isEmpty()) continue;
            long Id = Parse_long_strict(Line);
            if (Is_in_ranges(Merged, Id)) Count++;
        }

        return Count;
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
        // No underscores/spaces/etc; rely on Long.parseLong.
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

            // Overlapping OR adjacent ranges merge (inclusive ranges).
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

    private static boolean Is_in_ranges(List<Range> Merged, long Id) {
        // Binary search over disjoint, sorted ranges.
        int lo = 0;
        int hi = Merged.size() - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            Range r = Merged.get(mid);

            if (Id < r.Start) {
                hi = mid - 1;
            } else if (Id > r.End) {
                lo = mid + 1;
            } else {
                return true;
            }
        }
        return false;
    }
}
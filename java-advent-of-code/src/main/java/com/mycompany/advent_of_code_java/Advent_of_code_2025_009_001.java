package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_009_001 {

    static final class Point_record {
        final int X;
        final int Y;

        Point_record(int x, int y) {
            X = x;
            Y = y;
        }
    }

    static List<String> Read_non_empty_lines(String path) throws IOException {
        List<String> Output = new ArrayList<>();
        for (String Line : Files.readAllLines(Path.of(path))) {
            if (!Line.trim().isEmpty()) {
                Output.add(Line);
            }
        }
        return Output;
    }

    public static void main(String[] args) throws Exception {

        String Input_path = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-009\\input.txt";

        List<String> Lines = Read_non_empty_lines(Input_path);

        List<Point_record> Points = new ArrayList<>(Lines.size());
        for (String Line : Lines) {
            String[] Parts = Line.split(",");
            int X = Integer.parseInt(Parts[0].trim());
            int Y = Integer.parseInt(Parts[1].trim());
            Points.add(new Point_record(X, Y));
        }

        long Best_area = -1L;

        int Count = Points.size();
        for (int i = 0; i < Count; i++) {
            Point_record A = Points.get(i);
            for (int j = i + 1; j < Count; j++) {
                Point_record B = Points.get(j);

                long Width_in_tiles = Math.abs((long) A.X - (long) B.X) + 1L;
                long Height_in_tiles = Math.abs((long) A.Y - (long) B.Y) + 1L;
                long Area_in_tiles = Width_in_tiles * Height_in_tiles;

                if (Area_in_tiles > Best_area) {
                    Best_area = Area_in_tiles;
                }
            }
        }

        System.out.println(Best_area);
    }
}
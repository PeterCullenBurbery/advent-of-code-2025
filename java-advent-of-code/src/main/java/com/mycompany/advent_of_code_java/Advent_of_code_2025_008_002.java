package com.mycompany.advent_of_code_java;

import module java.base;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Advent_of_code_2025_008_002 {

    static final class Edge_record {
        final int Left_index;
        final int Right_index;
        final long Squared_distance;

        Edge_record(int left_index, int right_index, long squared_distance) {
            Left_index = left_index;
            Right_index = right_index;
            Squared_distance = squared_distance;
        }
    }

    static final class Disjoint_set_union {
        final int[] Parent;
        final int[] Size;
        int Component_count;

        Disjoint_set_union(int count) {
            Parent = new int[count];
            Size = new int[count];
            Component_count = count;
            for (int i = 0; i < count; i++) {
                Parent[i] = i;
                Size[i] = 1;
            }
        }

        int Find(int value) {
            while (Parent[value] != value) {
                Parent[value] = Parent[Parent[value]];
                value = Parent[value];
            }
            return value;
        }

        boolean Union(int a, int b) {
            int Root_a = Find(a);
            int Root_b = Find(b);
            if (Root_a == Root_b) return false;

            if (Size[Root_a] < Size[Root_b]) {
                int Temp = Root_a;
                Root_a = Root_b;
                Root_b = Temp;
            }
            Parent[Root_b] = Root_a;
            Size[Root_a] += Size[Root_b];
            Component_count--;
            return true;
        }
    }

    static long Compute_squared_distance(int[] X, int[] Y, int[] Z, int i, int j) {
        long Dx = (long) X[i] - X[j];
        long Dy = (long) Y[i] - Y[j];
        long Dz = (long) Z[i] - Z[j];
        return Dx * Dx + Dy * Dy + Dz * Dz;
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
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-008\\input.txt";

        List<String> Lines = Read_non_empty_lines(Input_path);
        int Count = Lines.size();

        int[] X = new int[Count];
        int[] Y = new int[Count];
        int[] Z = new int[Count];

        for (int i = 0; i < Count; i++) {
            String[] Parts = Lines.get(i).split(",");
            X[i] = Integer.parseInt(Parts[0].trim());
            Y[i] = Integer.parseInt(Parts[1].trim());
            Z[i] = Integer.parseInt(Parts[2].trim());
        }

        long Edge_count_long = ((long) Count * (Count - 1)) / 2;
        if (Edge_count_long > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Too many edges for in-memory sort: " + Edge_count_long +
                    " (Count=" + Count + ")."
            );
        }

        int Edge_count = (int) Edge_count_long;
        Edge_record[] All_edges = new Edge_record[Edge_count];

        int Write_index = 0;
        for (int i = 0; i < Count; i++) {
            for (int j = i + 1; j < Count; j++) {
                long Distance_squared = Compute_squared_distance(X, Y, Z, i, j);
                All_edges[Write_index++] = new Edge_record(i, j, Distance_squared);
            }
        }

        Arrays.sort(All_edges, (a, b) -> {
            int Cmp = Long.compare(a.Squared_distance, b.Squared_distance);
            if (Cmp != 0) return Cmp;
            Cmp = Integer.compare(a.Left_index, b.Left_index);
            if (Cmp != 0) return Cmp;
            return Integer.compare(a.Right_index, b.Right_index);
        });

        Disjoint_set_union DSU = new Disjoint_set_union(Count);

        int Last_left_index = -1;
        int Last_right_index = -1;

        for (int i = 0; i < All_edges.length; i++) {
            Edge_record Edge = All_edges[i];

            if (DSU.Union(Edge.Left_index, Edge.Right_index)) {
                Last_left_index = Edge.Left_index;
                Last_right_index = Edge.Right_index;

                if (DSU.Component_count == 1) {
                    break;
                }
            }
        }

        if (DSU.Component_count != 1) {
            throw new IllegalStateException("Did not reach a single circuit. Input may be empty or invalid.");
        }

        BigInteger Product =
                BigInteger.valueOf(X[Last_left_index])
                        .multiply(BigInteger.valueOf(X[Last_right_index]));

        System.out.println(Product);
    }
}
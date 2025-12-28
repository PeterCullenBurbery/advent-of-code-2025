package com.mycompany.advent_of_code_java;

import module java.base;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Day 8: Playground
 * Connect the 1000 closest pairs of junction boxes and
 * multiply the sizes of the three largest resulting circuits.
 */
public class Advent_of_code_2025_008_001 {

    static final int Max_connections = 1000;

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

    static final Comparator<Edge_record> Edge_ascending = (a, b) -> {
        int cmp = Long.compare(a.Squared_distance, b.Squared_distance);
        if (cmp != 0) return cmp;
        cmp = Integer.compare(a.Left_index, b.Left_index);
        if (cmp != 0) return cmp;
        return Integer.compare(a.Right_index, b.Right_index);
    };

    static final Comparator<Edge_record> Edge_descending =
            (a, b) -> Edge_ascending.compare(b, a);

    static final class Disjoint_set_union {
        final int[] Parent;
        final int[] Size;

        Disjoint_set_union(int count) {
            Parent = new int[count];
            Size = new int[count];
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

        void Union(int a, int b) {
            int root_a = Find(a);
            int root_b = Find(b);
            if (root_a == root_b) return;

            if (Size[root_a] < Size[root_b]) {
                int tmp = root_a;
                root_a = root_b;
                root_b = tmp;
            }
            Parent[root_b] = root_a;
            Size[root_a] += Size[root_b];
        }
    }

    static long Compute_squared_distance(
            int[] X, int[] Y, int[] Z, int i, int j) {

        long dx = (long) X[i] - X[j];
        long dy = (long) Y[i] - Y[j];
        long dz = (long) Z[i] - Z[j];
        return dx * dx + dy * dy + dz * dz;
    }

    static boolean Is_better_edge(
            Edge_record candidate, Edge_record current_worst) {

        return Edge_ascending.compare(candidate, current_worst) < 0;
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

        PriorityQueue<Edge_record> Best_edges =
                new PriorityQueue<>(Max_connections + 1, Edge_descending);

        for (int i = 0; i < Count; i++) {
            for (int j = i + 1; j < Count; j++) {
                long Distance_squared =
                        Compute_squared_distance(X, Y, Z, i, j);

                Edge_record Edge =
                        new Edge_record(i, j, Distance_squared);

                if (Best_edges.size() < Max_connections) {
                    Best_edges.add(Edge);
                } else if (Is_better_edge(Edge, Best_edges.peek())) {
                    Best_edges.poll();
                    Best_edges.add(Edge);
                }
            }
        }

        List<Edge_record> Selected_edges =
                new ArrayList<>(Best_edges);
        Selected_edges.sort(Edge_ascending);

        Disjoint_set_union DSU =
                new Disjoint_set_union(Count);

        for (int i = 0; i < Selected_edges.size(); i++) {
            Edge_record Edge = Selected_edges.get(i);
            DSU.Union(Edge.Left_index, Edge.Right_index);
        }

        Map<Integer, Integer> Circuit_sizes =
                new HashMap<>();

        for (int i = 0; i < Count; i++) {
            int Root = DSU.Find(i);
            Circuit_sizes.put(
                    Root,
                    Circuit_sizes.getOrDefault(Root, 0) + 1
            );
        }

        int[] Top_three = new int[] {0, 0, 0};
        for (int Size : Circuit_sizes.values()) {
            if (Size > Top_three[0]) {
                Top_three[2] = Top_three[1];
                Top_three[1] = Top_three[0];
                Top_three[0] = Size;
            } else if (Size > Top_three[1]) {
                Top_three[2] = Top_three[1];
                Top_three[1] = Size;
            } else if (Size > Top_three[2]) {
                Top_three[2] = Size;
            }
        }

        BigInteger Result =
                BigInteger.valueOf(Top_three[0])
                        .multiply(BigInteger.valueOf(Top_three[1]))
                        .multiply(BigInteger.valueOf(Top_three[2]));

        System.out.println(Result);
    }

    static List<String> Read_non_empty_lines(String path)
            throws IOException {

        List<String> Output = new ArrayList<>();
        for (String Line : Files.readAllLines(Path.of(path))) {
            if (!Line.trim().isEmpty()) {
                Output.add(Line);
            }
        }
        return Output;
    }
}
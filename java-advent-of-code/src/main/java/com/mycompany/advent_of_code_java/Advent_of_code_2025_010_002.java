package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_010_002 {

    static final class Machine_record {
        final int Counter_count;
        final int Button_count;
        final int[] Target;
        final int[][] Button_to_counters; // each button -> list of counters it affects

        Machine_record(int counter_count, int button_count, int[] target, int[][] button_to_counters) {
            Counter_count = counter_count;
            Button_count = button_count;
            Target = target;
            Button_to_counters = button_to_counters;
        }
    }

    static List<String> Read_non_empty_lines(String path) throws IOException {
        List<String> Output = new ArrayList<>();
        for (String Line : Files.readAllLines(Path.of(path))) {
            if (!Line.trim().isEmpty()) Output.add(Line.trim());
        }
        return Output;
    }

    static Machine_record Parse_machine(String line) {
        int Curly_start = line.indexOf('{');
        int Curly_end = line.indexOf('}');
        if (Curly_start < 0 || Curly_end < 0 || Curly_end <= Curly_start) {
            throw new IllegalArgumentException("Missing {joltage} in line: " + line);
        }

        String Curly_inside = line.substring(Curly_start + 1, Curly_end).trim();
        String[] Joltage_parts = Curly_inside.isEmpty() ? new String[0] : Curly_inside.split(",");
        int Counter_count = Joltage_parts.length;

        int[] Target = new int[Counter_count];
        for (int i = 0; i < Counter_count; i++) {
            Target[i] = Integer.parseInt(Joltage_parts[i].trim());
            if (Target[i] < 0) throw new IllegalArgumentException("Negative joltage: " + Target[i]);
        }

        String Before_curly = line.substring(0, Curly_start);

        Pattern Paren_pattern = Pattern.compile("\\(([^)]*)\\)");
        Matcher Matcher_obj = Paren_pattern.matcher(Before_curly);

        List<int[]> Buttons = new ArrayList<>();
        while (Matcher_obj.find()) {
            String Inside = Matcher_obj.group(1).trim();
            if (Inside.isEmpty()) continue;

            String[] Parts = Inside.split(",");
            IntArrayList Counter_list = new IntArrayList();
            for (String Part : Parts) {
                String S = Part.trim();
                if (S.isEmpty()) continue;
                int Index = Integer.parseInt(S);
                if (Index < 0 || Index >= Counter_count) {
                    throw new IllegalArgumentException("Counter index out of range: " + Index + " for K=" + Counter_count);
                }
                Counter_list.Add(Index);
            }
            Buttons.add(Counter_list.To_array());
        }

        int Button_count = Buttons.size();
        int[][] Button_to_counters = new int[Button_count][];
        for (int i = 0; i < Button_count; i++) Button_to_counters[i] = Buttons.get(i);

        return new Machine_record(Counter_count, Button_count, Target, Button_to_counters);
    }

    // Minimal int-list helper (avoids extra deps)
    static final class IntArrayList {
        private int[] Data = new int[8];
        private int Size = 0;

        void Add(int v) {
            if (Size == Data.length) Data = Arrays.copyOf(Data, Data.length * 2);
            Data[Size++] = v;
        }

        int[] To_array() {
            return Arrays.copyOf(Data, Size);
        }
    }

    static final class Solver_state {
        final int K;
        final int M;

        // For each subset s, Precomputed_add[s][i] = (Ay)_i as an integer count (0..M)
        final int[][] Precomputed_add;
        final int[] Subset_cost;

        final HashMap<Vector_key, Integer> Memo = new HashMap<>();

        Solver_state(Machine_record machine) {
            K = machine.Counter_count;
            M = machine.Button_count;

            int Subset_count = 1 << M;
            Precomputed_add = new int[Subset_count][K];
            Subset_cost = new int[Subset_count];

            // Precompute contribution vector for each subset
            for (int s = 0; s < Subset_count; s++) {
                Subset_cost[s] = Integer.bitCount(s);
            }

            for (int button_index = 0; button_index < M; button_index++) {
                int Bit = 1 << button_index;
                int[] Counters = machine.Button_to_counters[button_index];
                for (int s = 0; s < Subset_count; s++) {
                    if ((s & Bit) == 0) continue;
                    int Prev = s ^ Bit;
                    // build Precomputed_add[s] from Precomputed_add[Prev] + this button
                    if (button_index == 0) {
                        // ensure prev already okay; but safest is just do generic copy each time
                    }
                }
            }

            // Build iteratively using DP: Precomputed_add[s] = Precomputed_add[s without lsb] + button(lsb)
            Precomputed_add[0] = new int[K];
            for (int s = 1; s < Subset_count; s++) {
                int Lsb = s & -s;
                int Button_index = Integer.numberOfTrailingZeros(Lsb);
                int Prev = s ^ Lsb;

                int[] Prev_vec = Precomputed_add[Prev];
                int[] Vec = Arrays.copyOf(Prev_vec, K);
                for (int counter : machine.Button_to_counters[Button_index]) {
                    Vec[counter] += 1;
                }
                Precomputed_add[s] = Vec;
            }
        }

        int Solve_min_presses(int[] Target_vector) {
            return Solve_recursive(Target_vector);
        }

        private int Solve_recursive(int[] b) {
            Vector_key Key = new Vector_key(b);
            Integer Cached = Memo.get(Key);
            if (Cached != null) return Cached;

            boolean All_zero = true;
            for (int v : b) {
                if (v != 0) { All_zero = false; break; }
            }
            if (All_zero) {
                Memo.put(Key, 0);
                return 0;
            }

            int Best = Integer.MAX_VALUE / 4;

            int Subset_count = Precomputed_add.length;
            for (int s = 0; s < Subset_count; s++) {
                int[] Add = Precomputed_add[s];

                boolean Ok = true;
                int[] Next = new int[K];
                for (int i = 0; i < K; i++) {
                    int bi = b[i];
                    int ai = Add[i];

                    if (ai > bi) { Ok = false; break; }
                    int Diff = bi - ai;
                    if ((Diff & 1) != 0) { Ok = false; break; } // must be even
                    Next[i] = Diff >>> 1;
                }
                if (!Ok) continue;

                int Sub_cost = Subset_cost[s];
                if (Sub_cost >= Best) continue; // quick prune

                int Tail = Solve_recursive(Next);
                if (Tail >= Integer.MAX_VALUE / 8) continue;

                int Candidate = Sub_cost + (2 * Tail);
                if (Candidate < Best) Best = Candidate;
            }

            Memo.put(Key, Best);
            return Best;
        }
    }

    // Hash key for int[] (immutable copy)
    static final class Vector_key {
        final int[] Data;
        final int Hash;

        Vector_key(int[] data) {
            Data = Arrays.copyOf(data, data.length);
            Hash = Arrays.hashCode(Data);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vector_key other)) return false;
            return Arrays.equals(Data, other.Data);
        }

        @Override public int hashCode() {
            return Hash;
        }
    }

    public static void main(String[] args) throws Exception {
        String Input_path = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-010\\input.txt";

        List<String> Lines = Read_non_empty_lines(Input_path);

        long Total = 0;
        for (String Line : Lines) {
            Machine_record Machine = Parse_machine(Line);
            Solver_state Solver = new Solver_state(Machine);
            int Min_presses = Solver.Solve_min_presses(Machine.Target);
            Total += Min_presses;
        }

        System.out.println(Total);
    }
}
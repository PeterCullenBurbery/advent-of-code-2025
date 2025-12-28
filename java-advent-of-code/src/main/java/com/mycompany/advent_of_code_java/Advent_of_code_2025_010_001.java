package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_010_001 {

    static final class Machine_record {
        final int Light_count;
        final long[] Button_masks; // bit i means toggles light i
        final long Target_mask;    // bit i means target light i is ON

        Machine_record(int light_count, long[] button_masks, long target_mask) {
            Light_count = light_count;
            Button_masks = button_masks;
            Target_mask = target_mask;
        }
    }

    static List<String> Read_non_empty_lines(String path) throws IOException {
        List<String> Output = new ArrayList<>();
        for (String Line : Files.readAllLines(Path.of(path))) {
            if (!Line.trim().isEmpty()) Output.add(Line.trim());
        }
        return Output;
    }

    static Machine_record Parse_machine(String Line) {
        // Example:
        // [.##.#..##.] (3,6) (0,1,2) ... {52,67,...}
        // We ignore {...} entirely.

        int Bracket_start = Line.indexOf('[');
        int Bracket_end = Line.indexOf(']');
        if (Bracket_start < 0 || Bracket_end < 0 || Bracket_end <= Bracket_start) {
            throw new IllegalArgumentException("Missing [diagram]: " + Line);
        }
        String Diagram = Line.substring(Bracket_start + 1, Bracket_end).trim();
        int Light_count = Diagram.length();

        long Target_mask = 0L;
        for (int i = 0; i < Light_count; i++) {
            char c = Diagram.charAt(i);
            if (c == '#') Target_mask |= (1L << i);
            else if (c == '.') { /* off */ }
            else throw new IllegalArgumentException("Bad diagram char: " + c);
        }

        // Extract all (...) groups before the first { ... }
        int Curly_start = Line.indexOf('{');
        String Before_curly = (Curly_start >= 0) ? Line.substring(0, Curly_start) : Line;

        Pattern Paren_pattern = Pattern.compile("\\(([^)]*)\\)");
        Matcher M = Paren_pattern.matcher(Before_curly);

        List<Long> Button_masks_list = new ArrayList<>();
        while (M.find()) {
            String Inside = M.group(1).trim();
            if (Inside.isEmpty()) continue;

            long Mask = 0L;
            String[] Parts = Inside.split(",");
            for (String Part : Parts) {
                String S = Part.trim();
                if (S.isEmpty()) continue;
                int Index = Integer.parseInt(S);
                if (Index < 0 || Index >= Light_count) {
                    throw new IllegalArgumentException("Light index out of range: " + Index + " for " + Diagram);
                }
                Mask ^= (1L << Index); // XOR safe even if duplicates appear
            }
            Button_masks_list.add(Mask);
        }

        if (Button_masks_list.isEmpty()) {
            // No buttons: only solvable if target is all-off
            return new Machine_record(Light_count, new long[0], Target_mask);
        }

        long[] Button_masks = new long[Button_masks_list.size()];
        for (int i = 0; i < Button_masks.length; i++) Button_masks[i] = Button_masks_list.get(i);

        return new Machine_record(Light_count, Button_masks, Target_mask);
    }

    /**
     * Solve A x = b over GF(2) with minimum Hamming weight of x.
     * A is represented as row masks over variables (buttons).
     * We will:
     *  - perform elimination to RREF-ish, tracking pivot columns,
     *  - derive one particular solution x0 and a basis for the nullspace,
     *  - enumerate nullspace combinations to minimize weight.
     *
     * Constraints: variable count <= 60 (we use long for masks).
     */
    static int Min_presses(Machine_record Machine) {
        int m = Machine.Light_count;
        int n = Machine.Button_masks.length;

        if (n == 0) {
            return Machine.Target_mask == 0L ? 0 : Integer.MAX_VALUE / 4;
        }
        if (n > 60) {
            throw new IllegalArgumentException("Too many buttons for 64-bit mask approach: " + n);
        }

        // Build augmented matrix rows: [row_mask | rhs_bit]
        // row_mask has n bits, rhs is separate boolean.
        long[] Row = new long[m];
        boolean[] Rhs = new boolean[m];

        for (int i = 0; i < m; i++) {
            long Mask = 0L;
            for (int j = 0; j < n; j++) {
                if (((Machine.Button_masks[j] >>> i) & 1L) != 0L) {
                    Mask |= (1L << j);
                }
            }
            Row[i] = Mask;
            Rhs[i] = ((Machine.Target_mask >>> i) & 1L) != 0L;
        }

        // Gaussian elimination over GF(2)
        int[] Pivot_row_for_col = new int[n];
        Arrays.fill(Pivot_row_for_col, -1);

        int Row_i = 0;
        for (int Col = 0; Col < n && Row_i < m; Col++) {
            int Sel = -1;
            for (int r = Row_i; r < m; r++) {
                if (((Row[r] >>> Col) & 1L) != 0L) { Sel = r; break; }
            }
            if (Sel == -1) continue;

            // swap Sel with Row_i
            long Tmp_row = Row[Row_i]; Row[Row_i] = Row[Sel]; Row[Sel] = Tmp_row;
            boolean Tmp_rhs = Rhs[Row_i]; Rhs[Row_i] = Rhs[Sel]; Rhs[Sel] = Tmp_rhs;

            Pivot_row_for_col[Col] = Row_i;

            // eliminate this column from all other rows
            for (int r = 0; r < m; r++) {
                if (r == Row_i) continue;
                if (((Row[r] >>> Col) & 1L) != 0L) {
                    Row[r] ^= Row[Row_i];
                    Rhs[r] ^= Rhs[Row_i];
                }
            }

            Row_i++;
        }

        // Check consistency: any 0 = 1 row
        for (int r = 0; r < m; r++) {
            if (Row[r] == 0L && Rhs[r]) {
                return Integer.MAX_VALUE / 4; // impossible (shouldn't happen in puzzle input)
            }
        }

        // Determine pivot columns and free columns
        boolean[] Is_pivot_col = new boolean[n];
        int Pivot_count = 0;
        for (int c = 0; c < n; c++) {
            if (Pivot_row_for_col[c] != -1) {
                Is_pivot_col[c] = true;
                Pivot_count++;
            }
        }
        int Free_count = n - Pivot_count;

        int[] Free_cols = new int[Free_count];
        int idx = 0;
        for (int c = 0; c < n; c++) if (!Is_pivot_col[c]) Free_cols[idx++] = c;

        // Build one particular solution x0 by setting all free vars = 0, solving pivots from rows
        long X0 = 0L;
        for (int c = 0; c < n; c++) {
            if (Is_pivot_col[c]) {
                int pr = Pivot_row_for_col[c];
                // In this eliminated form, pivot variable x_c equals rhs XOR sum(other vars in row),
                // but since free vars are 0 in x0, we just take rhs.
                if (Rhs[pr]) X0 |= (1L << c);
            }
        }

        // Build nullspace basis vectors: one per free variable
        long[] Basis = new long[Free_count];
        for (int k = 0; k < Free_count; k++) {
            int Free_col = Free_cols[k];

            long Vec = 0L;
            // set this free var = 1
            Vec |= (1L << Free_col);

            // pivot vars depend on this free var via the row equation
            // For each pivot column c with pivot row pr: x_c = sum(row bits * x) + rhs
            // In homogeneous system (rhs=0), x_c = sum(other bits * x).
            // So if row has bit at Free_col, then pivot gets toggled.
            for (int c = 0; c < n; c++) {
                if (!Is_pivot_col[c]) continue;
                int pr = Pivot_row_for_col[c];
                if (((Row[pr] >>> Free_col) & 1L) != 0L) {
                    Vec ^= (1L << c);
                }
            }

            Basis[k] = Vec;
        }

        // Enumerate all combinations of basis vectors to minimize weight of X0 XOR combination
        // If Free_count is large, use meet-in-the-middle.
        if (Free_count <= 24) {
            int Best = Integer.bitCount((int)(X0)) + Integer.bitCount((int)(X0 >>> 32));
            long Limit = 1L << Free_count;
            for (long mask = 1; mask < Limit; mask++) {
                long Combo = 0L;
                long t = mask;
                int b = 0;
                while (t != 0) {
                    long lsb = t & -t;
                    int bit = Long.numberOfTrailingZeros(lsb);
                    Combo ^= Basis[bit];
                    t ^= lsb;
                    b++;
                }
                long X = X0 ^ Combo;
                int Weight = Long.bitCount(X);
                if (Weight < Best) Best = Weight;
            }
            return Best;
        } else {
            // Meet-in-the-middle
            int Left = Free_count / 2;
            int Right = Free_count - Left;

            long[] Left_basis = Arrays.copyOfRange(Basis, 0, Left);
            long[] Right_basis = Arrays.copyOfRange(Basis, Left, Free_count);

            Map<Long, Integer> Best_weight_for_vec = new HashMap<>();

            long Left_limit = 1L << Left;
            for (long mask = 0; mask < Left_limit; mask++) {
                long Vec = 0L;
                long t = mask;
                while (t != 0) {
                    long lsb = t & -t;
                    int bit = Long.numberOfTrailingZeros(lsb);
                    Vec ^= Left_basis[bit];
                    t ^= lsb;
                }
                int w = Long.bitCount(Vec);
                Integer Prev = Best_weight_for_vec.get(Vec);
                if (Prev == null || w < Prev) Best_weight_for_vec.put(Vec, w);
            }

            int Best = Integer.MAX_VALUE / 4;

            long Right_limit = 1L << Right;
            for (long mask = 0; mask < Right_limit; mask++) {
                long Vec_r = 0L;
                long t = mask;
                while (t != 0) {
                    long lsb = t & -t;
                    int bit = Long.numberOfTrailingZeros(lsb);
                    Vec_r ^= Right_basis[bit];
                    t ^= lsb;
                }

                // We need minimize weight(X0 ^ (Vec_l ^ Vec_r)).
                // This isn't separable by XOR directly with only weights, so we brute Vec_r and scan all Vec_l? too slow.
                // However, in practice Free_count here should be small; still, we can fall back to direct enumeration if needed.
                // To keep it correct without overengineering, do direct enumeration if Free_count is large but still manageable (<= 30-ish).
                // If puzzle has bigger, we'd need a different technique (e.g., Gray-code enumeration + pruning).
                // Given your input, Free_count should be modest.
                //
                // Practical fallback: enumerate all left vecs from the map keys.
                for (Map.Entry<Long, Integer> E : Best_weight_for_vec.entrySet()) {
                    long Combo = E.getKey() ^ Vec_r;
                    long X = X0 ^ Combo;
                    int Weight = Long.bitCount(X);
                    if (Weight < Best) Best = Weight;
                }
            }
            return Best;
        }
    }

    public static void main(String[] args) throws Exception {
        String Input_path = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-010\\input.txt";

        List<String> Lines = Read_non_empty_lines(Input_path);

        long Total_min_presses = 0L;

        for (String Line : Lines) {
            Machine_record Machine = Parse_machine(Line);
            int Min = Min_presses(Machine);
            if (Min >= Integer.MAX_VALUE / 8) {
                throw new IllegalStateException("Machine unsatisfiable: " + Line);
            }
            Total_min_presses += Min;
        }

        System.out.println(Total_min_presses);
    }
}
package com.mycompany.advent_of_code_java;

import module java.base;

/**
 * Day 9 - Part Two:
 * Largest axis-aligned rectangle (tile area inclusive) whose opposite corners are RED tiles,
 * and every tile in the rectangle is RED or GREEN (i.e., inside or on the boundary of the polygon).
 *
 * The polygon is defined by the ordered list of red vertices; consecutive vertices share row/col;
 * boundary tiles between vertices are green; interior is green.
 */
public class Advent_of_code_2025_009_002 {

    static final class Point_record {
        final int X;
        final int Y;
        Point_record(int x, int y) { X = x; Y = y; }
    }

    static List<String> Read_non_empty_lines(String path) throws IOException {
        List<String> Output = new ArrayList<>();
        for (String Line : Files.readAllLines(Path.of(path))) {
            if (!Line.trim().isEmpty()) Output.add(Line.trim());
        }
        return Output;
    }

    static int Lower_bound(List<Integer> Sorted, int Value) {
        int Lo = 0, Hi = Sorted.size();
        while (Lo < Hi) {
            int Mid = (Lo + Hi) >>> 1;
            if (Sorted.get(Mid) < Value) Lo = Mid + 1;
            else Hi = Mid;
        }
        return Lo;
    }

    /**
     * Coordinate compression for tile coordinates.
     *
     * Critical trick:
     * Include each vertex coordinate AND +/-1, so that scanline filling at tile centers
     * is representable without losing adjacency when edges jump.
     */
    static List<Integer> Build_compressed_axis(List<Point_record> Points, boolean Use_x) {
        TreeSet<Integer> Values = new TreeSet<>();
        for (Point_record P : Points) {
            int V = Use_x ? P.X : P.Y;
            Values.add(V);
            Values.add(V - 1);
            Values.add(V + 1);
        }
        // Also include endpoints +/-1 for each segment to preserve gaps
        int N = Points.size();
        for (int i = 0; i < N; i++) {
            Point_record A = Points.get(i);
            Point_record B = Points.get((i + 1) % N);
            int A_v = Use_x ? A.X : A.Y;
            int B_v = Use_x ? B.X : B.Y;
            int Min = Math.min(A_v, B_v);
            int Max = Math.max(A_v, B_v);
            Values.add(Min);
            Values.add(Max);
            Values.add(Min - 1);
            Values.add(Max + 1);
        }
        return new ArrayList<>(Values);
    }

    /**
     * Build inside-or-boundary boolean grid over compressed coordinates,
     * but only for coordinates that correspond to actual integer tile centers in the compressed list.
     *
     * We treat each compressed coordinate as a possible tile X or tile Y.
     * A tile exists at each integer coordinate, and our compressed set contains the needed integers.
     *
     * Fill method:
     * For each y (tile row), do ray casting against the polygon edges (axis-aligned),
     * at the horizontal line y + 0.5 (i.e., through tile centers), and mark x inside.
     */
    static boolean[][] Build_inside_grid(
            List<Point_record> Poly,
            List<Integer> Xs,
            List<Integer> Ys) {

        int W = Xs.size();
        int H = Ys.size();
        boolean[][] Inside = new boolean[H][W];

        // Pre-extract polygon edges
        int N = Poly.size();
        Point_record[] P = Poly.toArray(new Point_record[0]);

        // For each compressed Y that is an integer tile coordinate, compute inside using scanline at y+0.5
        for (int Yi = 0; Yi < H; Yi++) {
            int Y_tile = Ys.get(Yi);
            double Y_scan = Y_tile + 0.5;

            // Collect X-intersections of polygon with this scanline.
            // Only vertical edges contribute intersections in standard ray casting.
            List<Double> X_hits = new ArrayList<>();

            for (int i = 0; i < N; i++) {
                Point_record A = P[i];
                Point_record B = P[(i + 1) % N];

                if (A.X == B.X) {
                    // Vertical edge at x = A.X spanning y in [min, max]
                    int Y0 = A.Y;
                    int Y1 = B.Y;
                    int MinY = Math.min(Y0, Y1);
                    int MaxY = Math.max(Y0, Y1);

                    // Standard rule: count intersection if scanline crosses edge with half-open interval [MinY, MaxY)
                    // to avoid double-counting vertices.
                    if (Y_scan >= MinY && Y_scan < MaxY) {
                        X_hits.add((double) A.X);
                    }
                }
            }

            Collections.sort(X_hits);

            // Now fill between pairs of intersections
            // If polygon is well-formed, intersections come in pairs.
            for (int k = 0; k + 1 < X_hits.size(); k += 2) {
                double X_left = X_hits.get(k);
                double X_right = X_hits.get(k + 1);

                // Tiles whose centers are within (X_left, X_right) are inside.
                // But we want inside-or-boundary; boundary tiles will be added later explicitly.
                for (int Xi = 0; Xi < W; Xi++) {
                    int X_tile = Xs.get(Xi);
                    double X_center = X_tile + 0.5;
                    if (X_center > X_left && X_center < X_right) {
                        Inside[Yi][Xi] = true;
                    }
                }
            }
        }

        // Mark boundary tiles as inside too (inside-or-boundary)
        // Expand each segment into tile coordinates along its row/col, but do it in compressed space.
        // This is safe because we included endpoints and +/-1 in compression.
        Map<Integer, Integer> X_index = new HashMap<>(Xs.size() * 2);
        for (int i = 0; i < Xs.size(); i++) X_index.put(Xs.get(i), i);

        Map<Integer, Integer> Y_index = new HashMap<>(Ys.size() * 2);
        for (int i = 0; i < Ys.size(); i++) Y_index.put(Ys.get(i), i);

        for (int i = 0; i < N; i++) {
            Point_record A = P[i];
            Point_record B = P[(i + 1) % N];

            if (A.Y == B.Y) {
                int Y = A.Y;
                int Yi = Y_index.get(Y);
                int X0 = A.X, X1 = B.X;
                int Min = Math.min(X0, X1);
                int Max = Math.max(X0, X1);
                for (int X = Min; X <= Max; X++) {
                    Integer Xi = X_index.get(X);
                    if (Xi != null) Inside[Yi][Xi] = true;
                }
            } else if (A.X == B.X) {
                int X = A.X;
                int Xi = X_index.get(X);
                int Y0 = A.Y, Y1 = B.Y;
                int Min = Math.min(Y0, Y1);
                int Max = Math.max(Y0, Y1);
                for (int Y = Min; Y <= Max; Y++) {
                    Integer Yi = Y_index.get(Y);
                    if (Yi != null) Inside[Yi][Xi] = true;
                }
            } else {
                throw new IllegalStateException("Non-axis-aligned edge found: (" + A.X + "," + A.Y + ") -> (" + B.X + "," + B.Y + ")");
            }
        }

        return Inside;
    }

    static long[][] Build_prefix_sum(boolean[][] Grid) {
        int H = Grid.length;
        int W = Grid[0].length;
        long[][] Ps = new long[H + 1][W + 1];

        for (int y = 0; y < H; y++) {
            long Row_sum = 0;
            for (int x = 0; x < W; x++) {
                Row_sum += Grid[y][x] ? 1 : 0;
                Ps[y + 1][x + 1] = Ps[y][x + 1] + Row_sum;
            }
        }
        return Ps;
    }

    static long Rect_sum(long[][] Ps, int x0, int y0, int x1, int y1) {
        // inclusive corners on Grid indices -> convert to prefix rectangle [y0,y1], [x0,x1]
        int X0 = x0;
        int Y0 = y0;
        int X1 = x1 + 1;
        int Y1 = y1 + 1;
        return Ps[Y1][X1] - Ps[Y0][X1] - Ps[Y1][X0] + Ps[Y0][X0];
    }

    public static void main(String[] args) throws Exception {

        String Input_path = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-009\\input.txt";

        List<String> Lines = Read_non_empty_lines(Input_path);

        List<Point_record> Red_vertices = new ArrayList<>(Lines.size());
        for (String Line : Lines) {
            String[] Parts = Line.split(",");
            int X = Integer.parseInt(Parts[0].trim());
            int Y = Integer.parseInt(Parts[1].trim());
            Red_vertices.add(new Point_record(X, Y));
        }

        // Coordinate compression axis sets (tile coordinates)
        List<Integer> Xs = Build_compressed_axis(Red_vertices, true);
        List<Integer> Ys = Build_compressed_axis(Red_vertices, false);

        // Map tile coordinate -> compressed index
        Map<Integer, Integer> X_index = new HashMap<>(Xs.size() * 2);
        for (int i = 0; i < Xs.size(); i++) X_index.put(Xs.get(i), i);

        Map<Integer, Integer> Y_index = new HashMap<>(Ys.size() * 2);
        for (int i = 0; i < Ys.size(); i++) Y_index.put(Ys.get(i), i);

        // Inside-or-boundary tiles (green+red)
        boolean[][] Inside = Build_inside_grid(Red_vertices, Xs, Ys);

        // Prefix sum for O(1) rectangle validity checks
        long[][] Inside_ps = Build_prefix_sum(Inside);

        // Store red tiles in compressed indices for fast membership and iteration
        boolean[][] Is_red = new boolean[Ys.size()][Xs.size()];
        for (Point_record P : Red_vertices) {
            Integer Xi = X_index.get(P.X);
            Integer Yi = Y_index.get(P.Y);
            if (Xi != null && Yi != null) Is_red[Yi][Xi] = true;
        }

        // Collect red points as compressed coords
        List<int[]> Red_points = new ArrayList<>(Red_vertices.size());
        for (Point_record P : Red_vertices) {
            Red_points.add(new int[]{ X_index.get(P.X), Y_index.get(P.Y) });
        }

        long Best_area = 0;

        // Brute over red-corner pairs; validate rectangle is fully inside-or-boundary using prefix sums
        // Complexity O(R^2). If R is a few thousand, this is OK in Java (few million pairs).
        int R = Red_points.size();
        for (int i = 0; i < R; i++) {
            int[] A = Red_points.get(i);
            int Ax = A[0], Ay = A[1];
            for (int j = i + 1; j < R; j++) {
                int[] B = Red_points.get(j);
                int Bx = B[0], By = B[1];

                // Must form opposite corners => the other two corners don't need be red, only inside.
                // But both corners A and B are red by construction.
                int x0 = Math.min(Ax, Bx);
                int x1 = Math.max(Ax, Bx);
                int y0 = Math.min(Ay, By);
                int y1 = Math.max(Ay, By);

                // Quick reject: rectangle must have red corners; already true.
                // Ensure all tiles in rectangle are inside-or-boundary.
                long Tile_count = (long)(x1 - x0 + 1) * (long)(y1 - y0 + 1);
                long Inside_count = Rect_sum(Inside_ps, x0, y0, x1, y1);
                if (Inside_count != Tile_count) continue;

                // Compute true area in tile units using original coordinates:
                // Each compressed coordinate is an actual tile coordinate integer, so width/height in tiles
                // is abs(X2-X1)+1, abs(Y2-Y1)+1 on original.
                int X_left = Xs.get(x0);
                int X_right = Xs.get(x1);
                int Y_low = Ys.get(y0);
                int Y_high = Ys.get(y1);

                long Width_tiles = Math.abs((long)X_right - (long)X_left) + 1L;
                long Height_tiles = Math.abs((long)Y_high - (long)Y_low) + 1L;
                long Area = Width_tiles * Height_tiles;

                if (Area > Best_area) Best_area = Area;
            }
        }

        System.out.println(Best_area);
    }
}
package com.mycompany.advent_of_code_java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Advent_of_code_2025_012_001 {

    public static void main(String[] args) throws Exception {
        String inputPath = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-012\\input.txt";

        List<String> lines = Files.readAllLines(Path.of(inputPath));

        ParsedInput parsed = parse(lines);

        int L = parsed.maxBoundingSide; // max over shapes of max(width,height)
        if (L <= 0) throw new IllegalStateException("Failed to determine shape bounding size.");

        long feasibleRegions = 0;
        for (Region r : parsed.regions) {
            long needed = r.totalPresents;
            long capacity = (r.width / L) * (r.height / L);
            if (needed <= capacity) feasibleRegions++;
        }

        System.out.println(feasibleRegions);
    }

    private static final class Region {
        final long width;
        final long height;
        final long totalPresents;

        Region(long width, long height, long totalPresents) {
            this.width = width;
            this.height = height;
            this.totalPresents = totalPresents;
        }
    }

    private static final class ParsedInput {
        final int maxBoundingSide;
        final int shapeCount;
        final List<Region> regions;

        ParsedInput(int maxBoundingSide, int shapeCount, List<Region> regions) {
            this.maxBoundingSide = maxBoundingSide;
            this.shapeCount = shapeCount;
            this.regions = regions;
        }
    }

    private static ParsedInput parse(List<String> lines) {
        Pattern shapeHeader = Pattern.compile("^(\\d+):\\s*$");
        Pattern regionLine = Pattern.compile("^(\\d+)x(\\d+):\\s*(.*)\\s*$");

        int i = 0;

        int maxBoundingSide = 0;
        int maxShapeIndexSeen = -1;

        // Parse shapes: blocks of:
        // N:
        // <grid line>
        // <grid line>
        // ...
        // [blank line]
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                i++;
                continue;
            }

            Matcher sh = shapeHeader.matcher(line);
            if (!sh.matches()) break; // shapes ended

            int idx = Integer.parseInt(sh.group(1));
            maxShapeIndexSeen = Math.max(maxShapeIndexSeen, idx);
            i++;

            List<String> grid = new ArrayList<>();
            while (i < lines.size()) {
                String g = lines.get(i);
                if (g.trim().isEmpty()) break;
                grid.add(g);
                i++;
            }

            int h = grid.size();
            int w = 0;
            for (String g : grid) w = Math.max(w, g.length());

            maxBoundingSide = Math.max(maxBoundingSide, Math.max(w, h));

            // skip blank separator
            while (i < lines.size() && lines.get(i).trim().isEmpty()) i++;
        }

        int shapeCount = maxShapeIndexSeen + 1;
        if (shapeCount <= 0) throw new IllegalArgumentException("No shapes found.");

        // Parse regions
        List<Region> regions = new ArrayList<>();
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            i++;
            if (line.isEmpty()) continue;

            Matcher m = regionLine.matcher(line);
            if (!m.matches()) {
                throw new IllegalArgumentException("Bad region line: " + line);
            }

            long w = Long.parseLong(m.group(1));
            long h = Long.parseLong(m.group(2));
            String rest = m.group(3).trim();

            String[] parts = rest.isEmpty() ? new String[0] : rest.split("\\s+");
            if (parts.length != shapeCount) {
                throw new IllegalArgumentException(
                        "Region quantities count mismatch. Expected " + shapeCount + " but got " + parts.length
                                + " in line: " + line
                );
            }

            long total = 0;
            for (String p : parts) total += Long.parseLong(p);

            regions.add(new Region(w, h, total));
        }

        return new ParsedInput(maxBoundingSide, shapeCount, regions);
    }
}
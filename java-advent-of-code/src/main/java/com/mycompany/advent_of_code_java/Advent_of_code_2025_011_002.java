package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_011_002 {

    // Paths from "svr" to "out" that visit BOTH "dac" and "fft" (in any order).
    // Count can be huge -> BigInteger.

    public static void main(String[] args) throws Exception {
        String Input_path = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-011\\input.txt";

        List<String> Lines = Files.readAllLines(Path.of(Input_path));
        Map<String, List<String>> Graph = Parse_graph(Lines);

        String Start = "svr";
        String End = "out";
        String NeedA = "dac";
        String NeedB = "fft";

        BigInteger Answer = Count_paths_svr_to_out_with_dac_and_fft(Graph, Start, End, NeedA, NeedB);
        System.out.println(Answer);
    }

    private static Map<String, List<String>> Parse_graph(List<String> Lines) {
        Map<String, List<String>> Adjacency = new HashMap<>();

        for (String Raw_line : Lines) {
            String Line = Raw_line.trim();
            if (Line.isEmpty()) continue;

            int Colon_index = Line.indexOf(':');
            if (Colon_index < 0) {
                throw new IllegalArgumentException("Bad line (missing ':'): " + Line);
            }

            String From = Line.substring(0, Colon_index).trim();
            String Right = Line.substring(Colon_index + 1).trim();

            List<String> To_list = new ArrayList<>();
            if (!Right.isEmpty()) {
                for (String Token : Right.split("\\s+")) {
                    String To = Token.trim();
                    if (!To.isEmpty()) To_list.add(To);
                }
            }

            Adjacency.put(From, To_list);
        }

        // Add nodes that only appear on RHS with empty adjacency (no mutation during iteration).
        Set<String> Missing = new HashSet<>();
        for (List<String> To_list : Adjacency.values()) {
            for (String To : To_list) {
                if (!Adjacency.containsKey(To)) Missing.add(To);
            }
        }
        for (String Node : Missing) {
            Adjacency.put(Node, List.of());
        }

        return Adjacency;
    }

    private static BigInteger Count_paths_svr_to_out_with_dac_and_fft(
            Map<String, List<String>> Graph,
            String Start,
            String End,
            String NeedA, // "dac"
            String NeedB  // "fft"
    ) {
        if (!Graph.containsKey(Start)) throw new IllegalArgumentException("Missing start node: " + Start);
        if (!Graph.containsKey(End)) return BigInteger.ZERO;

        // Quick fail if required nodes don't exist at all.
        if (!Graph.containsKey(NeedA) || !Graph.containsKey(NeedB)) return BigInteger.ZERO;

        // Prune to nodes that are on some Start->End path.
        Set<String> Reachable_from_start = Compute_reachable(Graph, Start);
        if (!Reachable_from_start.contains(End)) return BigInteger.ZERO;

        Set<String> Can_reach_end = Compute_can_reach_target(Graph, End);
        Set<String> Relevant = new HashSet<>(Reachable_from_start);
        Relevant.retainAll(Can_reach_end);
        if (!Relevant.contains(Start) || !Relevant.contains(End)) return BigInteger.ZERO;

        // Further pruning: from any node, can it still reach NeedA / NeedB?
        Set<String> Can_reach_needA = Compute_can_reach_target(Graph, NeedA);
        Set<String> Can_reach_needB = Compute_can_reach_target(Graph, NeedB);

        // If Start cannot reach either required node (in forward sense), answer is 0.
        // Using reverse reachability sets: Start must be in "can reach NeedX".
        if (!Can_reach_needA.contains(Start) || !Can_reach_needB.contains(Start)) return BigInteger.ZERO;

        // DP over (node, mask), where:
        // bit0 -> visited NeedA, bit1 -> visited NeedB
        int startMask = 0;
        if (Start.equals(NeedA)) startMask |= 1;
        if (Start.equals(NeedB)) startMask |= 2;

        Map<State, BigInteger> Memo = new HashMap<>();
        Set<State> Visiting = new HashSet<>();

        return Dfs_count_with_mask(
                Start, startMask,
                End, NeedA, NeedB,
                Graph, Relevant,
                Can_reach_needA, Can_reach_needB,
                Memo, Visiting
        );
    }

    private static Set<String> Compute_reachable(Map<String, List<String>> Graph, String Start) {
        Set<String> Seen = new HashSet<>();
        ArrayDeque<String> Q = new ArrayDeque<>();
        Seen.add(Start);
        Q.addLast(Start);

        while (!Q.isEmpty()) {
            String Cur = Q.removeFirst();
            for (String Next : Graph.getOrDefault(Cur, List.of())) {
                if (Seen.add(Next)) Q.addLast(Next);
            }
        }
        return Seen;
    }

    private static Set<String> Compute_can_reach_target(Map<String, List<String>> Graph, String Target) {
        // Reverse BFS from Target.
        Map<String, List<String>> Reverse = new HashMap<>();
        for (String Node : Graph.keySet()) {
            Reverse.put(Node, new ArrayList<>());
        }
        for (Map.Entry<String, List<String>> e : Graph.entrySet()) {
            String From = e.getKey();
            for (String To : e.getValue()) {
                Reverse.computeIfAbsent(To, _k -> new ArrayList<>()).add(From);
            }
        }

        Set<String> Seen = new HashSet<>();
        ArrayDeque<String> Q = new ArrayDeque<>();
        Seen.add(Target);
        Q.addLast(Target);

        while (!Q.isEmpty()) {
            String Cur = Q.removeFirst();
            for (String Prev : Reverse.getOrDefault(Cur, List.of())) {
                if (Seen.add(Prev)) Q.addLast(Prev);
            }
        }
        return Seen;
    }

    private static BigInteger Dfs_count_with_mask(
            String Node,
            int Mask,
            String End,
            String NeedA,
            String NeedB,
            Map<String, List<String>> Graph,
            Set<String> Relevant,
            Set<String> Can_reach_needA,
            Set<String> Can_reach_needB,
            Map<State, BigInteger> Memo,
            Set<State> Visiting
    ) {
        // If we have already failed to be on any Start->End path, stop.
        if (!Relevant.contains(Node)) return BigInteger.ZERO;

        // If we still need dac but cannot reach it from here, stop.
        if ((Mask & 1) == 0 && !Can_reach_needA.contains(Node)) return BigInteger.ZERO;

        // If we still need fft but cannot reach it from here, stop.
        if ((Mask & 2) == 0 && !Can_reach_needB.contains(Node)) return BigInteger.ZERO;

        if (Node.equals(End)) {
            return (Mask == 3) ? BigInteger.ONE : BigInteger.ZERO;
        }

        State Key = new State(Node, Mask);
        BigInteger Cached = Memo.get(Key);
        if (Cached != null) return Cached;

        if (Visiting.contains(Key)) {
            // Cycle in the "state graph" that can still potentially reach End and satisfy requirements.
            // That implies infinitely many paths under the usual definition.
            throw new IllegalStateException("Infinite paths detected (cycle) at state: " + Key);
        }

        Visiting.add(Key);

        BigInteger Total = BigInteger.ZERO;
        for (String Next : Graph.getOrDefault(Node, List.of())) {
            if (!Relevant.contains(Next)) continue;

            int NextMask = Mask;
            if (Next.equals(NeedA)) NextMask |= 1;
            if (Next.equals(NeedB)) NextMask |= 2;

            Total = Total.add(Dfs_count_with_mask(
                    Next, NextMask,
                    End, NeedA, NeedB,
                    Graph, Relevant,
                    Can_reach_needA, Can_reach_needB,
                    Memo, Visiting
            ));
        }

        Visiting.remove(Key);
        Memo.put(Key, Total);
        return Total;
    }

    private static final class State {
        final String node;
        final int mask;

        State(String node, int mask) {
            this.node = node;
            this.mask = mask;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State other)) return false;
            return mask == other.mask && node.equals(other.node);
        }

        @Override
        public int hashCode() {
            return 31 * node.hashCode() + mask;
        }

        @Override
        public String toString() {
            return node + ":" + mask;
        }
    }
}

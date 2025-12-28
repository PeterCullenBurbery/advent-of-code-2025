package com.mycompany.advent_of_code_java;

import module java.base;

public class Advent_of_code_2025_011_001 {

    public static void main(String[] args) throws Exception {
        String Input_path = (args.length > 0)
                ? args[0]
                : "E:\\documents-container\\advent-of-code\\java-advent-of-code\\inputs\\2025-011\\input.txt";

        List<String> Lines = Files.readAllLines(Path.of(Input_path));

        Map<String, List<String>> Adjacency_map = Parse_graph(Lines);

        String Start_node = "you";
        String End_node = "out";

        BigInteger Answer = Count_paths_you_to_out(Adjacency_map, Start_node, End_node);

        System.out.println(Answer);
    }

    private static Map<String, List<String>> Parse_graph(List<String> Lines) {
        Map<String, List<String>> Adjacency_map = new HashMap<>();

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
                    if (!To.isEmpty()) {
                        To_list.add(To);
                    }
                }
            }

            Adjacency_map.put(From, To_list);
        }

        // Add nodes that only appear as targets (RHS) with empty adjacency, without mutating during iteration.
        Set<String> Missing_nodes = new HashSet<>();
        for (List<String> To_list : Adjacency_map.values()) {
            for (String To : To_list) {
                if (!Adjacency_map.containsKey(To)) {
                    Missing_nodes.add(To);
                }
            }
        }
        for (String Node : Missing_nodes) {
            Adjacency_map.put(Node, List.of());
        }

        return Adjacency_map;
    }

    private static BigInteger Count_paths_you_to_out(
            Map<String, List<String>> Adjacency_map,
            String Start_node,
            String End_node
    ) {
        if (!Adjacency_map.containsKey(Start_node)) {
            throw new IllegalArgumentException("Missing start node: " + Start_node);
        }

        // If "out" never appears anywhere, there are 0 paths.
        if (!Adjacency_map.containsKey(End_node)) {
            return BigInteger.ZERO;
        }

        Set<String> Reachable_from_start = Compute_reachable(Adjacency_map, Start_node);
        if (!Reachable_from_start.contains(End_node)) {
            return BigInteger.ZERO;
        }

        Set<String> Can_reach_end = Compute_can_reach_end(Adjacency_map, End_node);

        // Nodes that can lie on a Start->End path
        Set<String> Relevant_nodes = new HashSet<>(Reachable_from_start);
        Relevant_nodes.retainAll(Can_reach_end);

        if (!Relevant_nodes.contains(Start_node) || !Relevant_nodes.contains(End_node)) {
            return BigInteger.ZERO;
        }

        Map<String, BigInteger> Memo = new HashMap<>();
        Set<String> Visiting = new HashSet<>();

        return Dfs_count(Start_node, End_node, Adjacency_map, Relevant_nodes, Memo, Visiting);
    }

    private static Set<String> Compute_reachable(Map<String, List<String>> Adjacency_map, String Start_node) {
        Set<String> Seen = new HashSet<>();
        ArrayDeque<String> Queue = new ArrayDeque<>();

        Seen.add(Start_node);
        Queue.addLast(Start_node);

        while (!Queue.isEmpty()) {
            String Cur = Queue.removeFirst();
            for (String Next : Adjacency_map.getOrDefault(Cur, List.of())) {
                if (Seen.add(Next)) {
                    Queue.addLast(Next);
                }
            }
        }

        return Seen;
    }

    private static Set<String> Compute_can_reach_end(Map<String, List<String>> Adjacency_map, String End_node) {
        // Reverse adjacency: To -> [From...]
        Map<String, List<String>> Reverse_map = new HashMap<>();
        for (String Node : Adjacency_map.keySet()) {
            Reverse_map.put(Node, new ArrayList<>());
        }
        for (Map.Entry<String, List<String>> Entry : Adjacency_map.entrySet()) {
            String From = Entry.getKey();
            for (String To : Entry.getValue()) {
                Reverse_map.computeIfAbsent(To, _k -> new ArrayList<>()).add(From);
            }
        }

        Set<String> Seen = new HashSet<>();
        ArrayDeque<String> Queue = new ArrayDeque<>();

        Seen.add(End_node);
        Queue.addLast(End_node);

        while (!Queue.isEmpty()) {
            String Cur = Queue.removeFirst();
            for (String Prev : Reverse_map.getOrDefault(Cur, List.of())) {
                if (Seen.add(Prev)) {
                    Queue.addLast(Prev);
                }
            }
        }

        return Seen;
    }

    private static BigInteger Dfs_count(
            String Node,
            String End_node,
            Map<String, List<String>> Adjacency_map,
            Set<String> Relevant_nodes,
            Map<String, BigInteger> Memo,
            Set<String> Visiting
    ) {
        if (Node.equals(End_node)) return BigInteger.ONE;

        BigInteger Cached = Memo.get(Node);
        if (Cached != null) return Cached;

        if (Visiting.contains(Node)) {
            // If a directed cycle exists inside the relevant subgraph, the number of paths is infinite.
            // AoC inputs are typically acyclic for this kind of question; if this triggers, your input
            // implies "infinite paths" under the usual definition of paths.
            throw new IllegalStateException("Infinite paths: cycle detected on a you->out-relevant path at node: " + Node);
        }

        Visiting.add(Node);

        BigInteger Total = BigInteger.ZERO;
        for (String Next : Adjacency_map.getOrDefault(Node, List.of())) {
            if (!Relevant_nodes.contains(Next)) continue; // prune dead ends
            Total = Total.add(Dfs_count(Next, End_node, Adjacency_map, Relevant_nodes, Memo, Visiting));
        }

        Visiting.remove(Node);
        Memo.put(Node, Total);
        return Total;
    }
}
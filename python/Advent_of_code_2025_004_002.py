import sys
from pathlib import Path
from collections import deque

def solve(input_path_str: str) -> int:
    input_path = Path(input_path_str)
    
    if not input_path.exists():
        raise FileNotFoundError(f"File not found: {input_path}")

    # Read lines and initialize grid
    lines = [line.strip() for line in input_path.read_text(encoding='utf-8').splitlines() if line.strip()]
    if not lines:
        return 0

    rows = len(lines)
    cols = len(lines[0])
    
    grid = []
    adjacent_counts = [[0] * cols for _ in range(rows)]
    
    # 1. Build grid and validate
    for r, line in enumerate(lines):
        if len(line) != cols:
            raise ValueError(f"Grid is not rectangular (line {r + 1}).")
        grid.append(list(line))

    # 8-direction offsets
    dr = [-1, -1, -1,  0, 0,  1, 1, 1]
    dc = [-1,  0,  1, -1, 1, -1, 0, 1]

    # 2. Precompute initial adjacency counts
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != '@':
                continue
            
            count = 0
            for k in range(8):
                rr, cc = r + dr[k], c + dc[k]
                if 0 <= rr < rows and 0 <= cc < cols:
                    if grid[rr][cc] == '@':
                        count += 1
            adjacent_counts[r][c] = count

    # 3. Seed the queue with rolls that are initially removable
    queue = deque()
    enqueued = [[False] * cols for _ in range(rows)]

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == '@' and adjacent_counts[r][c] < 4:
                queue.append((r, c))
                enqueued[r][c] = True

    removed_count = 0

    # 4. Simulation loop (BFS)
    while queue:
        r, c = queue.popleft()

        # Skip if already removed (though 'enqueued' guard usually prevents this)
        if grid[r][c] != '@':
            continue

        # Remove the roll
        grid[r][c] = '.'
        removed_count += 1

        # Notify neighbors
        for k in range(8):
            rr, cc = r + dr[k], c + dc[k]
            if 0 <= rr < rows and 0 <= cc < cols:
                if grid[rr][cc] == '@':
                    adjacent_counts[rr][cc] -= 1

                    # If neighbor becomes removable, add to queue
                    if adjacent_counts[rr][cc] < 4 and not enqueued[rr][cc]:
                        queue.append((rr, cc))
                        enqueued[rr][cc] = True

    return removed_count

def main():
    default_path = r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-004\input.txt"
    input_path_str = sys.argv[1] if len(sys.argv) >= 2 else default_path
    
    try:
        result = solve(input_path_str)
        print(result)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
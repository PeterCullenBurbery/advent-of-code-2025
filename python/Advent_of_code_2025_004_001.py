import sys
from pathlib import Path

def solve(input_path_str: str) -> int:
    input_path = Path(input_path_str)
    
    if not input_path.exists():
        raise FileNotFoundError(f"File not found: {input_path}")

    # Read all lines and remove trailing whitespace
    lines = [line.strip() for line in input_path.read_text(encoding='utf-8').splitlines() if line.strip()]
    if not lines:
        return 0

    rows = len(lines)
    cols = len(lines[0])

    # Grid validation
    grid = []
    for r, line in enumerate(lines):
        if len(line) != cols:
            raise ValueError(f"Grid is not rectangular (line {r + 1}).")
        
        row_chars = []
        for c, ch in enumerate(line):
            if ch not in ('@', '.'):
                raise ValueError(f"Unexpected character '{ch}' at row {r + 1}, col {c + 1}.")
            row_chars.append(ch)
        grid.append(row_chars)

    # 8-direction offsets (Dr = row delta, Dc = column delta)
    dr = [-1, -1, -1,  0, 0,  1, 1, 1]
    dc = [-1,  0,  1, -1, 1, -1, 0, 1]

    accessible_count = 0

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != '@':
                continue

            adjacent_rolls = 0
            # Check all 8 neighbors
            for k in range(8):
                rr, cc = r + dr[k], c + dc[k]
                
                # Boundary check
                if 0 <= rr < rows and 0 <= cc < cols:
                    if grid[rr][cc] == '@':
                        adjacent_rolls += 1
            
            # According to the logic, it's accessible if fewer than 4 neighbors are rolls
            if adjacent_rolls < 4:
                accessible_count += 1

    return accessible_count

def main():
    # Default path relative to your project structure
    default_path = r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-004\input.txt"
    
    input_path_str = sys.argv[1] if len(sys.argv) >= 2 else default_path
    
    try:
        result = solve(input_path_str)
        print(result)
    except FileNotFoundError:
        print(f"Failed to read input: {input_path_str}", file=sys.stderr)
        sys.exit(1)
    except ValueError as e:
        print(f"Invalid input: {e}", file=sys.stderr)
        sys.exit(2)

if __name__ == "__main__":
    main()
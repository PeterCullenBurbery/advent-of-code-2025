import sys
from pathlib import Path

def solve(input_file_path: Path) -> int:
    MOD = 100
    pos = 50
    count_at_zero = 0
    
    # Using 'with' for automatic file closing (equivalent to try-with-resources)
    with open(input_file_path, 'r', encoding='utf-8') as f:
        for line_no, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
                
            dir_char = line[0]
            if dir_char not in ('L', 'R'):
                raise ValueError(f"Line {line_no}: must start with L or R, got: {line}")
            
            dist_str = line[1:].strip()
            if not dist_str:
                raise ValueError(f"Line {line_no}: missing distance: {line}")
            
            try:
                dist = int(dist_str)
            except ValueError:
                raise ValueError(f"Line {line_no}: bad distance: {line}")
            
            # Normalize distance (Java: ((dist % MOD) + MOD) % MOD)
            # Python's % operator handles negatives naturally, but we'll stay explicit
            dist = dist % MOD
            
            if dir_char == 'R':
                pos = (pos + dist) % MOD
            else:  # 'L'
                pos = (pos - dist) % MOD
                
            if pos == 0:
                count_at_zero += 1
                
    return count_at_zero

def main():
    # Default path based on your Java code
    default_path = r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-001\input.txt"
    
    # Use command line arg if provided, otherwise use default
    input_path_str = sys.argv[1] if len(sys.argv) >= 2 else default_path
    input_path = Path(input_path_str)
    
    try:
        password = solve(input_path)
        print(password)
    except FileNotFoundError:
        print(f"Failed to read input: {input_path}", file=sys.stderr)
        sys.exit(1)
    except ValueError as e:
        print(f"Invalid input format: {e}", file=sys.stderr)
        sys.exit(2)
    except Exception as e:
        print(f"An unexpected error occurred: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
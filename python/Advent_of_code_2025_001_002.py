import sys
from pathlib import Path

def count_hits_zero_during_rotation(pos: int, direction: str, dist: int) -> int:
    """
    Counts the number of click-steps k in [1..dist] such that the dial points at 0
    immediately after that click, starting from 'pos' before the rotation.
    """
    MOD = 100
    
    if direction == 'R':
        # pos + k ≡ 0 (mod 100)  =>  k ≡ -pos ≡ (100 - pos) (mod 100)
        k0 = (MOD - (pos % MOD)) % MOD
        k_first = MOD if k0 == 0 else k0
    else: # 'L'
        # pos - k ≡ 0 (mod 100)  =>  k ≡ pos (mod 100)
        k0 = pos % MOD
        k_first = MOD if k0 == 0 else k0

    if k_first > dist:
        return 0

    # hits at k_first, k_first+100, k_first+200, ... <= dist
    return 1 + (dist - k_first) // MOD

def solve(input_file_path: Path) -> int:
    MOD = 100
    pos = 50
    zero_clicks = 0
    
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
                
            if dist < 0:
                raise ValueError(f"Line {line_no}: distance must be non-negative: {line}")
            if dist == 0:
                continue

            # Count how many clicks during this rotation land on 0
            zero_clicks += count_hits_zero_during_rotation(pos, dir_char, dist)

            # Update final position after full rotation
            dist_mod = dist % MOD
            if dir_char == 'R':
                pos = (pos + dist_mod) % MOD
            else: # 'L'
                pos = (pos - dist_mod) % MOD
                # Python's % handles negative numbers, but we'll be explicit to match Java
                if pos < 0:
                    pos += MOD
                    
    return zero_clicks

def main():
    # Default path based on your environment
    default_path = r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-001\input.txt"
    
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

if __name__ == "__main__":
    main()
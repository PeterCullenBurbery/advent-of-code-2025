import sys
from pathlib import Path

def digits_count(x: int) -> int:
    if x == 0:
        return 1
    return len(str(x))

def ceil_div(a: int, b: int) -> int:
    return (a + b - 1) // b

def sum_repeated(lo: int, hi: int, m: int) -> int:
    """Sum over X in [lo..hi] of N = X*m, where m = 10^k + 1"""
    count = hi - lo + 1
    sum_x = (lo + hi) * count // 2
    return m * sum_x

def solve(input_file_path: Path) -> int:
    if not input_file_path.exists():
        raise FileNotFoundError(f"File not found: {input_file_path}")
        
    content = input_file_path.read_text(encoding='utf-8').strip()
    if not content:
        return 0

    parts = content.split(",")
    ranges = []
    global_max = 0

    for token in parts:
        token = token.strip()
        if not token:
            continue
        
        if '-' not in token:
            raise ValueError(f"Bad range token (missing '-'): {token}")
        
        try:
            a_str, b_str = token.split('-', 1)
            a, b = int(a_str.strip()), int(b_str.strip())
        except ValueError:
            raise ValueError(f"Invalid numbers in token: {token}")

        if a > b:
            a, b = b, a
        
        ranges.append((a, b))
        if b > global_max:
            global_max = b

    max_digits = digits_count(global_max)
    max_k = max_digits // 2

    # Precompute powers of 10
    pow10 = [10**i for i in range(max(2, max_k + 1))]

    total = 0

    for a, b in ranges:
        if a == 0 and b == 0:
            continue

        for k in range(1, max_k + 1):
            m = pow10[k] + 1            # N = X * m
            x_min_digits = pow10[k - 1] # smallest k-digit X
            x_max_digits = pow10[k] - 1 # largest k-digit X

            lo = ceil_div(a, m)
            hi = b // m

            # Constrain to exactly k-digits
            actual_lo = max(lo, x_min_digits)
            actual_hi = min(hi, x_max_digits)

            if actual_lo <= actual_hi:
                total += sum_repeated(actual_lo, actual_hi, m)

    return total

def main():
    # Default path relative to your project structure
    default_path = Path(r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-002\input.txt")
    
    input_path = Path(sys.argv[1]) if len(sys.argv) >= 2 else default_path
    
    try:
        ans = solve(input_path)
        print(ans)
    except FileNotFoundError:
        print(f"Failed to read input: {input_path}", file=sys.stderr)
        sys.exit(1)
    except ValueError as e:
        print(f"Invalid input format: {e}", file=sys.stderr)
        sys.exit(2)

if __name__ == "__main__":
    main()
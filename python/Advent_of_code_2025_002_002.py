import sys
from pathlib import Path
import bisect

def get_digits_count(x: int) -> int:
    if x == 0:
        return 1
    return len(str(x))

def generate_all_repeated_numbers_up_to(max_val: int) -> list[int]:
    """
    Generates all numbers <= max that are formed by repeating a block of digits.
    Example: 1212 (block 12 repeated twice), 111 (block 1 repeated thrice).
    """
    if max_val < 11:
        return []

    max_digits = get_digits_count(max_val)
    # Precompute powers of 10
    pow10 = [10**i for i in range(max_digits + 1)]
    
    results = set() # Use a set to handle deduplication automatically (e.g., 1111)

    # n: total length, k: block length, t: repetition count
    for n in range(2, max_digits + 1):
        for k in range(1, n):
            if n % k != 0:
                continue
            
            t = n // k
            if t < 2:
                continue

            start_x = pow10[k - 1]  # smallest k-digit number
            end_x = pow10[k] - 1    # largest k-digit number

            for x in range(start_x, end_x + 1):
                # Build the repeated number string-wise or math-wise
                # Math-wise to match Java logic:
                v = 0
                block_pow = pow10[k]
                for _ in range(t):
                    v = v * block_pow + x
                
                if v > max_val:
                    # For a fixed n, k, t, v increases with x
                    break
                
                results.add(v)

    return sorted(list(results))

def solve(input_file_path: Path) -> int:
    if not input_file_path.exists():
        raise FileNotFoundError(f"File not found: {input_file_path}")
        
    content = input_file_path.read_text(encoding='utf-8').strip()
    if not content:
        return 0

    # Parse ranges
    parts = content.split(",")
    ranges = []
    global_max = 0

    for token in parts:
        token = token.strip()
        if not token or '-' not in token:
            continue
        
        a_str, b_str = token.split('-', 1)
        a, b = int(a_str.strip()), int(b_str.strip())
        if a > b:
            a, b = b, a
        
        ranges.append((a, b))
        if b > global_max:
            global_max = b

    # Generate invalid IDs and create prefix sums
    invalid_ids = generate_all_repeated_numbers_up_to(global_max)
    
    # pref[i] is the sum of the first i elements
    pref = [0] * (len(invalid_ids) + 1)
    for i in range(len(invalid_ids)):
        pref[i + 1] = pref[i] + invalid_ids[i]

    total = 0
    for a, b in ranges:
        # lower_bound: first index i where invalid_ids[i] >= a
        left = bisect.bisect_left(invalid_ids, a)
        # upper_bound: first index i where invalid_ids[i] > b
        right = bisect.bisect_right(invalid_ids, b)
        
        if left < right:
            total += (pref[right] - pref[left])

    return total

def main():
    default_path = Path(r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-002\input.txt")
    input_path = Path(sys.argv[1]) if len(sys.argv) >= 2 else default_path
    
    try:
        ans = solve(input_path)
        print(ans)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
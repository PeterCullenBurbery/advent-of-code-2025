import sys
from pathlib import Path

def max_subsequence_of_length(digits: str, k: int) -> str:
    """
    Returns the lexicographically largest subsequence of length K using
    a monotonic stack approach.
    """
    n = len(digits)
    deletes_allowed = n - k
    stack = []

    for char in digits:
        # While we have deletes left and the current digit is bigger than 
        # the top of our stack, pop the stack to make room for a bigger number.
        while deletes_allowed > 0 and stack and stack[-1] < char:
            stack.pop()
            deletes_allowed -= 1
        stack.append(char)

    # If we haven't used all our deletes (e.g., input was '9876'),
    # slice the stack to the required length K.
    return "".join(stack[:k])

def solve(input_path_str: str, digits_to_pick: int = 12) -> int:
    total_sum = 0
    input_path = Path(input_path_str)
    
    if not input_path.exists():
        raise FileNotFoundError(f"File not found: {input_path}")

    with open(input_path, 'r', encoding='utf-8') as f:
        for line_no, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
                
            if not line.isdigit():
                raise ValueError(f"Line {line_no} contains non-digit characters.")
                
            if len(line) < digits_to_pick:
                raise ValueError(
                    f"Line {line_no} length {len(line)} is less than "
                    f"required {digits_to_pick} digits."
                )
                
            max_val_str = max_subsequence_of_length(line, digits_to_pick)
            total_sum += int(max_val_str)
            
    return total_sum

def main():
    # Default path based on your environment
    default_path = r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-003\input.txt"
    
    input_path = sys.argv[1] if len(sys.argv) >= 2 else default_path
    
    try:
        # Solving for 12 digits as specified in your Java source
        ans = solve(input_path, 12)
        print(ans)
    except FileNotFoundError:
        print(f"Failed to read input: {input_path}", file=sys.stderr)
        sys.exit(1)
    except ValueError as e:
        print(f"Invalid input: {e}", file=sys.stderr)
        sys.exit(2)

if __name__ == "__main__":
    main()
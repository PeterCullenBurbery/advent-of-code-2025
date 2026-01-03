import sys
from pathlib import Path

def max_two_digit_from_line(bank_digits: str) -> int:
    """
    Finds the max 10*digits[i] + digits[j] where i < j.
    Uses a suffix max array to find the best 'ones' digit for any 'tens' digit.
    """
    length = len(bank_digits)
    
    # Convert string to list of integers and validate
    try:
        digits = [int(d) for d in bank_digits]
    except ValueError:
        raise ValueError(f"Non-digit character in line: {bank_digits}")

    # Build Suffix Max array: Suffix_max[i] is the max digit from index i to the end
    suffix_max = [0] * length
    suffix_max[-1] = digits[-1]
    for i in range(length - 2, -1, -1):
        suffix_max[i] = max(digits[i], suffix_max[i+1])

    best = -1
    for i in range(length - 1):
        tens_digit = digits[i]
        ones_digit = suffix_max[i + 1]
        value = 10 * tens_digit + ones_digit
        
        if value > best:
            best = value
            
        if best == 99: # Early exit optimization
            break
            
    return best

def solve(input_path_str: str) -> int:
    total_sum = 0
    input_path = Path(input_path_str)
    
    if not input_path.exists():
        raise FileNotFoundError(f"File not found: {input_path}")

    with open(input_path, 'r', encoding='utf-8') as f:
        for line_no, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
                
            if len(line) < 2:
                raise ValueError(f"Line {line_no} has fewer than 2 digits.")
                
            max_joltage = max_two_digit_from_line(line)
            total_sum += max_joltage
            
    return total_sum

def main():
    # Default path based on your environment
    default_path = r"E:\documents-container\advent-of-code\java-advent-of-code\inputs\2025-003\input.txt"
    
    input_path = sys.argv[1] if len(sys.argv) >= 2 else default_path
    
    try:
        ans = solve(input_path)
        print(ans)
    except FileNotFoundError:
        print(f"Failed to read input: {input_path}", file=sys.stderr)
        sys.exit(1)
    except ValueError as e:
        print(f"Invalid input: {e}", file=sys.stderr)
        sys.exit(2)

if __name__ == "__main__":
    main()
## Summary

#### 
PrimeSieve is a Java program that calculates the prime numbers from 1 to 10^8, and creates 8 threads to help perform calculations.

## How to Run
#### 
1. Clone this git repo
2. In the directory, run command
'javac PrimeSieve.java'
3. Run command 'java PrimeSieve'
4. Look at primes.txt for output

## Evaluation
The core of the algorithm is the use of a modified Sieve of Eratosthenes. Starting at 3, the algorithm marks off all multiples of a prime. To make use of multithreading, for each prime the remaining array from prime p to n=10^8 is divided into 8 blocks and each spawned thread performs an incremental sieve on each block. When it finishes the sieve operation, it iterates up until the next unmarked number and repeats.

### Optimizations Sieve until sqrt(n)
The sieving halts once reaching sqrt(n) because if a number less than or equal to sqrt(n) has been sieved up, it is guaranteed to be sieved greater than sqrt(n).

### Optimization: Sieve Table 
The Java BitSet structure was used to store whether an index was a prime hit. It was chosen because despite slightly slower lookup times, it only takes n/8 bytes of memory. Additionally, because no even number is prime, the table only needs n/2 bits. In total, the sieve table needs roughly 6.25 MB. This is a major space improvement considering the immensity of n.

### Possible improvements
There are situations where a thread is waiting idle during a sieve because its block has no primes within the range. There may be some way to occupy this thread during this time to prevent idleness. Such a time improvement would be minimal because this situation only arises for large prime numbers that are fast to sieve.

### Tests

Tests were run on an Intel i5-8250U CPU
| Test      | Run time (seconds) |
| ----------- | ----------- |
| 1      | 0.924160900        |
| 2   | 0.846270900         |
| 3   | 0.898138200         |
| 4   | 0.970100300         |
| 5   | 0.905682300       |

Average: 0.908870520 sec

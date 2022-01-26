import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.math.BigInteger;

public class PrimeSieve{

    int n = 100_000_000;
    int sqrtN = 10_000;
    int threadCount = 8;
    int numTopPrimes = 10;
    
    String fileName = "primes.txt";

    Thread threads[];
    BlockingQueue<Job> jobs;
    Queue<Integer> primesQueue;

    // bits default 0, which represents a prime number
    BitSet sieveTable;
    Semaphore semaphore;

    BigInteger primeSum;
    int primeCount = 0;
    long totalRuntime;

    public static void main(String[] args){
        
        PrimeSieve sieve = new PrimeSieve();
    }

    public PrimeSieve(){
        
        long startTime = System.nanoTime();
        primesQueue = new PriorityQueue<Integer>();
        sieveTable = new BitSet((n+1)/2);
        jobs = new ArrayBlockingQueue<Job>(threadCount);
        semaphore = new Semaphore(0);
        threads = new Thread[threadCount];
        primeSum = new BigInteger("0");

        for (int i = 0; i < threadCount; i++){
            threads[i] = new Thread(new Worker());
            threads[i].setDaemon(true);
            threads[i].start();
        }

        addPrime(2);

        // only begin sieve from primes discovered before sqrt(n)
        int i;
        for (i = 0; i <= intToIndex(2 * (sqrtN / 2) - 1); i++) {

            if (sieveTable.get(i) == false) {
                
                addPrime(indexToInt(i));
                // when it finds next prime, dispatch threads 
                sieveOnePrime(indexToInt(i));
            }
        }

        for (; i <= intToIndex(2 * (n / 2) - 1); i++){
            if (sieveTable.get(i) == false){
                addPrime(indexToInt(i));
            }
        }
        long endTime = System.nanoTime();
        totalRuntime = (endTime - startTime);
        
        writeResults();
    }

    // update data with found prime
    private void addPrime(int p){
        if (primesQueue.size() == numTopPrimes){
            primesQueue.remove();
        }
        primesQueue.add(p);
        primeSum = primeSum.add(BigInteger.valueOf(p));
        primeCount++;
    }
    private int indexToInt(int i){
        return 2*(i+1)+1;
    }
    private int intToIndex(int j){
        return (j-1)/2 - 1;
    }

    // Partition array and dispatch threads for sieving
    private void sieveOnePrime(int p){

        int a = n - p;
        int b = a / threadCount;
        int r = a % b;

        for (int i = 0, rb = p, lb; i < threadCount; i++){
 
            lb = rb + 1;
            rb = lb + b - 1;
            if (r > 0){
                rb++;
                r--;
            }
            int jobStart = nextPrimeMult(p,lb,rb);
            // only start if prime is in range
            if (jobStart != -1){
                try {
                    jobs.put(new Job(jobStart, rb, p));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            semaphore.acquire(threadCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void writeResults(){
        File file = new File (fileName);

        try {
            FileWriter fileWriter = new FileWriter(file);

            String bar = "==============================";
            String outputString = "Execution time\n"+bar+"\n" + totalRuntime + " nanosec\n\n"
            + "Number of primes found\n" + bar+ "\n"  + primeCount + "\n\n"
            + "Sum of all primes found\n" + bar + "\n" + primeSum.toString() + "\n\n"
            + "Top 10 primes\n" + bar + getTopPrimesString(10);
            fileWriter.write(outputString);
            fileWriter.close();

        } catch (IOException e){
            System.out.println("File error");
            e.printStackTrace();
        }
        
    }

    private String getTopPrimesString(int numToGet){
        StringBuilder sb = new StringBuilder();

        ArrayList<Integer> list = new ArrayList<>();
        while(!primesQueue.isEmpty()){
            list.add(primesQueue.remove());

        }        
        Collections.sort(list);
        for (Integer i: list){
            sb.append("\n" + i);
        }
        return sb.toString();

    }

    // get lowest multiple of prime within threshold, -1 if exceeds threshold
    int nextPrimeMult(int p, int leftBound, int rightBound){
        if (leftBound % p == 0)
            return leftBound;            

        int next = ((leftBound / p) * p) + p;
        if (next > rightBound)
            return -1;
        else return next;
    }

    private class Job {
        public int rb;
        public int inc;
        public int start;
    
        public Job(int start, int rb, int inc) {
            this.rb = rb;
            this.inc = inc;
            this.start = start;
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                Job job;
                try {
                    job = jobs.take();
                    if (job == null)
                        return;
                    
                    for (int i = job.start; i <= job.rb; i += job.inc) {
                        if (i%2 != 0)
                            sieveTable.set(intToIndex(i),true);
                    }
                    // Notify master thread that a job was done.
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }   
}
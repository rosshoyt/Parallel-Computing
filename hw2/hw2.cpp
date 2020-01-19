/**
 * @file hw2.cpp - implement Ladner-Fischer (inclusive) parallel prefix sum algorithm with C++ threads
 * @author Ross Hoyt
 * @see "Seattle University, CPSC5600, Winter 2020"
 *
 * Design:
 * 1.
 * 2...
 */
#include <iostream>
#include <vector>
#include <chrono>
#include <future>
#include <cmath>  // for log2, floor
using namespace std;

typedef vector<int> Data;

/**
 * @class Heaper - Base class for an array-based complete binary heap from a provided array of leaf nodes
 */
class Heaper {
public:
    /**
     * Heaper constructor which allocates memory for interior heap nodes.
     * Allocates N-1 interior heap nodes in the from a supplied const vector<int> [the leaf nodes]
     * Interior nodes are set to 0
     * @param data pointer to array from which prefix sum will be calculated
     */
    Heaper(const Data *data) : n(data->size()), data(data) {
        interior = new Data(n - 1, 0);
    }

    /**
     * Destructor which deletes allocated memory
     */
    virtual ~Heaper() {
        delete interior;
    }

protected:
    /**
     * Size of data (n-1 is size of interior)
     */
    uint32_t n;

    /**
     * Original std::vector<int> passed in in constructor, which will be used as the leaf nodes in the heap
     */
    const Data *data;

    /**
     * Allocated interior nodes
     */
    Data *interior;

    /**
     * Gets the value at provided tree index, from the interior or leaf node arrays
     *
     * @param i overall tree index to get value from
     * @returns value at given tree index
     */
    virtual int value(uint32_t i) {
        if(isLeaf(i))
            return data->at(i - (n-1));
        else
            return interior->at(i);
    }

    /**
     * Calculates and returns the total size of Heap including interior and leaf nodes
     *
     * @returns number of heap nodes
     */
    virtual uint32_t size() {
        return (n-1) + n;
    }

    /**
     *
     * @return
     */
//    virtual int height() {
//        return (int)(log2(n));
//    }

    /**
     *
     * @param i  tree index to calculate the level for
     * @return   level in the complete binary tree that index i occupies
     */
    virtual uint32_t level(uint32_t i) {
        return i == 0? 1 : (uint32_t)(log2(i) + 1);
    }

    /**
     *
     * @param i
     * @return
     */
    virtual uint32_t parent(uint32_t i) {
        return (i-1) / 2;
    }

    /**
     *
     * @param i
     * @return
     */
    virtual uint32_t left(uint32_t i) {
        return i * 2 + 1;
    }

    /**
     *
     * @param i
     * @return
     */
    virtual uint32_t right(uint32_t i) {
        return i * 2 + 2;
    }

    /**
     *
     * @param i
     * @return
     */
    virtual bool isLeaf(uint32_t i) { return i  >= n; }

};

/**
 * @class SumHeap - Class derived from Heaper which adds methods for correct summation into interior nodes, done
 * recursively with he first four levels of recursion forking off threads (total of eight threads). Computes the
 * sum correctly into the interior nodes
 *
 * @returns total size of Heap
 */
class SumHeap : public Heaper {
public:

    /**
     * Heaper constructor which initializes parent class, allocates memory for
     * interior nodes, and calculates pair-wise sums to fill in the interior nodes
     *
     * @param data pointer to array from which prefix sum will be calculated
     */
    SumHeap(const Data *data) : Heaper(data) {
        calcSum(0);
    }

    int sum(uint32_t node=0) {
        return value(node);
    }

    /**
     *
     * @param prefix vector<int> to store prefix sum
     */
    void prefixSums(Data *prefix) {
        prefixSums(prefix, 0, 0);
    }

private:
    static const int MAX_THREADS = 8;
    //atomic<int> numThreads;
    //int maxThreadCreationLevel = (int)log2(MAX_THREADS);

    /**
     * Recursive method which performs pair-wise summation of provided array, filling interior nodes with
     * calculated values
     * @param i the tree index whose child nodes to sum, if it is not a leaf node
     */
    void calcSum(uint32_t i) {
        // Sequential algorithm
        if(isLeaf(i))
            return;
        calcSum(left(i));
        calcSum(right(i));
        interior->at(i) = value(left(i)) + value(right(i));

        // Parallel algorithm
//        if(MAX_THREADS > numThreads && !isLeaf(i)) {
//            auto handle = async(launch::async, &SumHeap::calcSum, this, right(i));//, level(i) + 1);
//            ++numThreads; //?
//        }
    }

    /**
     * Recursive method which calculates the prefix Sums and stores them in the provided prefix array
     * @param prefix vector<int> where the prefix sums results are stored
     * @param i      the current index
     * @param parVal value from parent node
     */
    void prefixSums(Data *prefix, uint32_t i, int parVal) {
        // Sequential algorithm
        if(isLeaf(i)) {
            prefix->at(i+1-n) = value(i) + parVal;
            return;
        }
        prefixSums(prefix, left(i), parVal);
        prefixSums(prefix, right(i), parVal + value(left(i)));

    }
};

const int N = 1<<26; // FIXME must be power of 2 for now
/**
 * Provided main method which tests and times the Tree Prefix Sum solution
 */
int main() {
    Data data(N, 1); // put a 1 in each element of the data array
    Data prefix(N, 1);

    // start timer
    auto start = chrono::steady_clock::now();

    SumHeap heap(&data);
    heap.prefixSums(&prefix);

    // stop timer
    auto end = chrono::steady_clock::now();
    auto elapsed = chrono::duration<double,milli>(end-start).count();
    int check = 1;
    for (int elem: prefix)
        if (elem != check++) {
            cout << "FAILED RESULT at " << check-1; break;
        }
    cout << "in " << elapsed << "ms" << endl; return 0;
}
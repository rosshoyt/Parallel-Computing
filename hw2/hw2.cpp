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
 * @class Heaper - Base class for an array based, complete binary heap from a provided array
 *
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
    int n;

    /**
     * Original std::vector<int> passed in in constructor, which will be used as the leaf nodes in the heap
     */
    const Data *data;

    /**
     * Allocated interior nodes
     */
    Data *interior;

    /**
     * Calculates and returns the total size of Heap including interior and leaf nodes
     *
     * @returns number of heap nodes
     */
    virtual int size() {
        return (n-1) + n;
    }
    /**
     *
     * @return
     */
    virtual int height() {
        return (int)(log2(n));

    }
    /**
     *
     * @param i  tree index to calculate the level for
     * @return   level in the complete binary tree that index i occupies
     */
    virtual int level(int i){
        return i == 0 ? 0 : (int)(log2(i-1));
    }
    /**
     * Gets the value at provided tree index, from the interior or leaf node arrays
     *
     * @param i overall tree index to get value from
     * @returns value at given tree index
     */
    virtual int value(int i) {
        //if (i < n-1) return interior->at(i);
        //else return data->at(i - (n-1));
        if(isLeaf(i))
            return data->at(i - (n-1));
        else
            return interior->at(i);
    }

    virtual int parent(int i) {
        if(i > 0) return interior->at((i-1) / 2);
        // TODO Handle 0 or Negative i value?
    }

    /**
     * TODO Handling invalid index values in these util methods
     * @param i
     * @return
     */
    virtual int left(int i) {
        int leftChildIndex = i * 2 + 1;
        return isLeaf(i) ? data->at(n-leftChildIndex) : interior->at(leftChildIndex);

    }
    virtual int right(int i) {
        int rightChildIndex = i * 2 + 1;
        return isLeaf(i) ? data->at(n-rightChildIndex) : interior->at(rightChildIndex);
    }

    virtual bool isLeaf(int i) { return i  >= n - 1; }

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
     * Heaper constructor which calls parent class, allocating memory for
     * interior nodes
     *
     * @param data pointer to array from which prefix sum will be calculated
     */
    SumHeap(const Data *data) : Heaper(data) {
        calcSum(0);
    }
    int sum(int node=0) {
        return value(node);
    }
    void prefixSums(Data *prefix) {
        prefixSums(0, 0);

    }

private:
    static const int MAX_THREADS = 8;
    const int maxThreadCreationLevel = floor(log2(MAX_THREADS)); // TODO fix
    void calcSum(int i) {

        if(!isLeaf(i) | level(i) > maxThreadCreationLevel)
            auto handle = async(launch::async, &SumHeap::calcSum, this, left(i), level(i)+1);

//        if (isLeaf(i))
//            return;
//        calcSum(left(i));
//        calcSum(right(i));
//        interior->at(i) = value(left(i)) + value(right(i));
    }

    /**
     * TODO fix
     */

    void prefixSums(int i, int) {
        if(!isLeaf(i)) {
            auto handle = async(launch::async, &SumHeap::prefixSums, this, left(i), level + 1);
        }
        // search left tree

    }



};
/*
 * QUESTIONS For K:
 * 1. What is the purpose of passing in the vector into prefix Sums. Should this vector be used during the calculation,
 * or should the prefix sum be copied into this vector
 * 2. What is the purpose of computing the grand total at the root by pair-wise sum first?
 */

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
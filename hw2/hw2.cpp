/**
 * @file hw2.cpp - implement Ladner-Fischer parallel prefix sum algorithm with C++ threads
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
using namespace std;

typedef vector<int> Data;

/**
 * @class Heaper - Base class for an array tree implementation for the interior nodes of a summation.
 * Allocates memory for N-1 interior nodes in the tree.
 */
class Heaper {
public:
    /**
     * Heaper constructor which allocates memory for interior nodes
     *
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
     * @returns total size of Heap
     */
    virtual int size() {
        return (n-1) + n;
    }

    /**
     * Returns the value at provided tree index, from the interior or leaf node vectors
     *
     * @param i overall tree index to get value from
     * @returns value at given tree index
     */
    virtual int value(int i) {
        if (i < n-1)
            return interior->at(i);
        else
            return data->at(i - (n-1));
    }
    virtual int parent(int i) { /*TODO*/ }
    virtual int left(int i) { /*TODO*/ }
    virtual int right(int i) { /*TODO*/ }
    virtual bool isLeaf(int i) { /*TODO*/ }
};

/**
 * @class SumHeap - Class derived from Heaper which adds methods for correct summation into interior nodes, done
 * recursively witht he first four levels of recursion forking off threads (total of eight threads). Computes the
 * sum correctly into the interior nodes
 *
 * @returns total size of Heap
 */
class SumHeap : public Heaper {
public:
    /**
    * Heaper constructor which allocates memory for interior nodes
    *
    * @param data pointer to array from which prefix sum will be calculated
    */
    SumHeap(const Data *data) : Heaper(data) {
        calcSum(0); // TODO change?
    }
    int sum(int node=0) {
        return value(node);
    }
    void prefixSums(/*TODO - type of prefix used in main? */) {
        // TODO
    }
private:
    void calcSum(int i) {
        if (isLeaf(i))
            return;
        calcSum(left(i));
        calcSum(right(i));
        interior->at(i) = value(left(i)) + value(right(i));
    }

};


const int N = 1<<26; // FIXME must be power of 2 for now
/**
 * Provided main method which tests and times the Tree Prefix Sum solution
 */
int main() {
    Data data(N, 1); // put a 1 in each element of the data array Data prefix(N, 1);
    // start timer
    auto start = chrono::steady_clock::now();
    SumHeap heap(&data); heap.prefixSums(&prefix);
    // stop timer
    auto end = chrono::steady_clock::now();
    auto elpased = chrono::duration<double,milli>(end-start).count();
    int check = 1;
    for (int elem: prefix)
        if (elem != check++) {
            cout << "FAILED RESULT at " << check-1; break;
        }
    cout << "in " << elpased << "ms" << endl; return 0;
}
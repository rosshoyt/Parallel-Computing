/**
 * @file hw2.cpp - implement Ladner-Fischer (inclusive) parallel prefix sum algorithm with C++ threads
 * @author Ross Hoyt
 * @see "Seattle University, CPSC5600, Winter 2020"
 *
 * Design:
 * 1. Heaper allocates vector of size N-1 for interior nodes
 * 2. SumHeap executes pair-wise summation, filling allocated interior nodes
 * 3...
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
     * Constructor which allocates memory for interior heap nodes.
     * Allocates N-1 interior heap nodes in the from a supplied const vector<int> [the leaf nodes]
     * Interior nodes are initially set to 0
     * @param data pointer to array from which prefix sum will be calculated
     */
    Heaper(const Data *data) : numLeafNodes((uint32_t)data->size()), numInteriorNodes(numLeafNodes-1), data(data) {
        interior = new Data(numInteriorNodes, 0);
    }

    /**
     * Destructor which deletes dynamically allocated memory
     */
    virtual ~Heaper() {
        delete interior;
    }
protected:
    /**
     * Size of passed in leaf nodes, and the allocated interior nodes
     */
    const uint32_t numLeafNodes;

    /**
     * Size of allocated interior nodes (
     * Value = numLeafNodes - 1
     */
    const uint32_t numInteriorNodes;

    /**
     * Original std::vector<int> passed in in the constructor, used as the leaf nodes in the heap
     */
    const Data *data;

    /**
     * Dynamically allocated interior nodes
     */
    Data *interior;

    /**
     * Method that gets the value at provided 'global' tree index, from either the interior
     * or leaf node vectors
     *
     * @param i global tree index to get value from
     * @returns value at given tree index
     */
    virtual int value(uint32_t i) {
        if(isLeaf(i))
            return data->at(i - numInteriorNodes);
        else
            return interior->at(i);
    }

    /**
     * Sets the value of an interior node of the tree.
     * If a leaf index is passed in, nothing happens because they are const.
     * TODO could remove, only called once
     *
     * @param i index of an interior node
     * @param value the value to set the node equal to
     */
    virtual void setValue(uint32_t i, int value){
        if(!isLeaf(i)) interior->at(i) = value;
        else return;
    }


    /**
     * Method that calculates the total size of Heap, including interior and leaf nodes
     *
     * @returns total number of heap nodes
     */
    virtual uint32_t size() {
        return numLeafNodes + numInteriorNodes;
    }

    /**
     * Method that calculates the level of a provided 'global' tree index
     * A root node has level = 1, its child node(s) has level = 2, etc.
     *
     * @param i  global tree index to calculate the level of
     * @return   level in the complete binary tree of index i
     */
    virtual uint32_t level(uint32_t i) {
        return 1 + (i == 0 ? 0 : (uint32_t)log2(i));
    }

    /**
     * Method that calculates and returns the index of the provided index's parent node.
     * If provided index is 0, returns 0
     *
     * @param i global tree index to calculate the parent index of
     * @return  index of parent node
     */
    virtual uint32_t parent(uint32_t i) {
        return (i-1) / 2;
    }

    /**
     * Method that calculates and returns the global index of the left child node of the
     * provided global tree index
     *
     * @param i  the global tree index to calculate the left child index of
     * @return   the left child index
     */
    virtual uint32_t left(uint32_t i) {
        return i * 2 + 1;
    }

    /**
     * Method that calculates and returns the global index of the right child node of the
     * provided global tree index
     *
     * @param i  the global tree index to calculate the right child index of
     * @return   the left child index
     */
    virtual uint32_t right(uint32_t i) {
        return i * 2 + 2;
    }

    /**
     * Checks if a global tree index is a leaf or interior index.
     * @param i the index to check
     * @return  true if the index refers to a leaf node
     */
    virtual bool isLeaf(uint32_t i) { return i  >= numInteriorNodes; }

};

/**
 * @class SumHeap - Class derived from @class Heaper and adds methods for computing the pair-wise summation into
 * interior nodes, recursively with the first four levels of recursion forking off threads (total of eight threads).
 * Also, adds parallel Prefix Sum functionality which uses the computed pair-wise summation values to
 * calculate the pair-wise sum into a user-provided vector.
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
        calcSum(0, 1);
    }

    /**
     * Method which returns the total sum of the tree, calculated during the pair-wise summation
     *
     * @param node the tree index
     * @return
     */
    int sum(uint32_t node=0) {
        return value(node);
    }

    /**
     * Method which calculates the prefix sum of the underlying Data array, and fills provided vector with results
     *
     * @param prefix vector<int> of size N-1 to store prefix sum results
     */
    void prefixSums(Data *prefix) {
        prefixSum(prefix, 0, 0, 1);
    }

private:
    /**
     * Maximum number of threads including the main thread
     */
    static const int MAX_THREADS = 8;

    /**
     * Method that checks if the provided tree level should fork worker threads in pair-wise or prefix-sum calculations
     *
     * @param level  the tree level to check
     * @returns true if the level should fork
     */
    bool shouldFork(const uint32_t level){
        return MAX_THREADS>>level > 0;
    }
    /**
     * Recursive method, called from constructor, which performs a parallel pair-wise summation of provided vector,
     * filling interior nodes with calculated values.
     * Creates up to 8 threads
     *
     * @param i the tree index whose child nodes to sum
     */
    void calcSum(uint32_t i, uint32_t level) {
        if(isLeaf(i))
            return;

        calcSum(left(i), level+1);
        // Fork off threads for first 4 levels of recursion
        if(shouldFork(level)) {
            cout << "Creating calcSum thread for level = " << level << "\n";
            auto handle = async(launch::async, &SumHeap::calcSum, this, right(i), level+1);
            //handle.wait(); // TODO need this line?
        } else
            calcSum(right(i), level+1);

        interior->at(i) = value(left(i)) + value(right(i));

    }

    /**
     * Recursive method which calculates the prefix Sums in parallel and stores them in the provided prefix array
     *
     * @param prefix vector<int> where the prefix sums results are stored
     * @param i      the current index
     * @param parVal value from parent node
     */
    void prefixSum(Data *prefix, uint32_t i, int parVal, uint32_t level) {
        if(isLeaf(i)) {
            prefix->at(i+1-numLeafNodes) = value(i) + parVal;
            return;
        }
        prefixSum(prefix, left(i), parVal, level+1);
        // Fork off threads for first 4 levels of recursion
        if(shouldFork(level)) {
            cout << "Creating PrefixSum thread for level = " << level << "\n";
            auto handle = async(launch::async, &SumHeap::prefixSum, this, prefix, right(i), parVal + value(left(i)), level+1);
            return handle.wait(); // TODO need this line?
        } else
            prefixSum(prefix, right(i), parVal + value(left(i)), level+1);
    }

    /**
     * Recursive method which performs sequential pair-wise summation of provided vector,
     * filling interior nodes with calculated values.
     *
     * @param i the tree index whose child nodes to sum
     */
    void calcSumSequential(uint32_t i) {
        if(isLeaf(i))
            return;
        calcSumSequential(left(i));
        calcSumSequential(right(i));
        interior->at(i) = value(left(i)) + value(right(i));
    }


    /**
     * Recursive method which calculates the prefix Sums sequentially and stores them in the provided prefix array
     *
     * @param prefix vector<int> where the prefix sums results are stored
     * @param i      the current index
     * @param parVal value from parent node
     */
    void prefixSumSequential(Data *prefix, uint32_t i, int parVal) {
        if(isLeaf(i)) {
            prefix->at(i+1-numLeafNodes) = value(i) + parVal;
            return;
        }
        prefixSumSequential(prefix, left(i), parVal);
        prefixSumSequential(prefix, right(i), parVal + value(left(i)));
    }



};

const int N = 1<<26; // FIXME must be power of 2 for now

/**
 * Provided main method which tests and times the Tree Prefix Sum solution
 *
 * @returns 0 (success)
 */
int main() {
    Data data(N, 1);
    Data prefix(N, 1);

    // Debug msg
    cout << "Welcome to HW2 Solution.\nCreating SumHeap with N size = " << N << endl;

    // start timer
    auto start = chrono::steady_clock::now();
    SumHeap heap(&data);
    heap.prefixSums(&prefix);
    // stop timer
    auto end = chrono::steady_clock::now();
    auto elapsed = chrono::duration<double,milli>(end-start).count();

    // Debug msg
    cout<< "DEBUG: Heap Pair-wise sum = " << heap.sum() << endl;

    int check = 1;
    for (int elem: prefix)
        if (elem != check++) {
            cout << "FAILED RESULT at " << check-1; break;
        }
    cout << "in " << elapsed << "ms" << endl; return 0;
}

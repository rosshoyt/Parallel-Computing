/**
 * @file hw1.cpp
 * @author Ross Hoyt
 * CPSC-5600 Parallel Computing
 * HW1 Solution
 */
#include <iostream>
#include <vector>
#include "ThreadGroup.h"
using namespace std;

int encode(int v) {
	// do something time-consuming (and arbitrary)
	for (int i = 0; i < 500; i++)
		v = ((v * v) + v) % 10;
	return v;
}

int decode(int v) {
	// do something time-consuming (and arbitrary)
	return encode(v);
}

/**
* @struct Stores data and attributes needed for each thread to complete its
* portion of the prefix summation.
*/
struct SharedData {
public:
	static const int NUM_THREADS = 2;
	const int length;
	int *data;

	SharedData(int *data, const int length) : length(length),  data(data) {}

};

struct EncodeThread {
	/**
	* Thread routine functor
	* @param id 			    thread id
	* @param sharedData 	pointer to SharedData structure
	*/
	void operator()(int id, void *sharedData) {
		SharedData *shData = (SharedData*)sharedData;
		int piece = shData->length / SharedData::NUM_THREADS;
		int start = id * piece, end = start + piece;
		for(int i = start; i < end; i++)
			shData->data[i] = encode(shData->data[i]);
	}
};

struct DecodeThread {
	void operator()(int id, void *sharedData) {
		SharedData *shData = (SharedData*)sharedData;
		int piece = shData->length / SharedData::NUM_THREADS;
		int start = id * piece, end = start + piece;
		for(int i = start; i < end; i++)
			shData->data[i] = decode(shData->data[i]);
	}
};
/**
*
*/
void prefixSums(int *data, int length) {
	// encode data in parallel
	ThreadGroup<EncodeThread> encoders;
	SharedData ourData(data, length);
	encoders.createThread(0, &ourData);
	encoders.createThread(1, &ourData);
	encoders.waitForAll();

	// accumulate sums
	int encodedSum = 0;
	for(int i = 0; i < length; i++){
		encodedSum += data[i];
		ourData.data[i] = encodedSum;
	}
	// decode data in parallel
	ThreadGroup<DecodeThread> decoders;
	decoders.createThread(0, &ourData);
	decoders.createThread(1, &ourData);
	decoders.waitForAll();

}

int main() {
	int length = 1000 * 1000;

	// make array
	int *data = new int[length];
	for (int i = 1; i < length; i++)
		data[i] = 1;
	data[0] = 6;

	// transform array into converted/deconverted prefix sum of original
	prefixSums(data, length);

	// printed out result is 6, 6, and 2 when data[0] is 6 to start and the rest 1
	cout << "[0]: " << data[0] << endl
			<< "[" << length/2 << "]: " << data[length/2] << endl
			<< "[end]: " << data[length-1] << endl;

    delete[] data;
	return 0;
}

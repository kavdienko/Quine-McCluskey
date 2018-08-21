import java.util.ArrayList;
import java.util.List;


/**
 * The BitVector class is a data structure that represents an ordered sequence of 
 * bits (0 or 1) of arbitrary-length. We define some useful functionality for you
 * to use to implement the Quine-McCluskey algorithm, but you may freely add your
 * own operations if you wish. We will compile and test with your own BitVector class.
 */
public class BitVector {
	private List<Long> bitVectorList;
	private int mySize;
	

	/* Create BitVector of a given size (rounds up by 64-bit increments) */
	public BitVector(int size) {
		mySize = size;
		int adjustedSize = ((size-1) / 64) + 1;
		bitVectorList = new ArrayList<Long>();
		for(int i = 0; i < adjustedSize; i++) {
			long newLong = 0;
			bitVectorList.add(newLong);
		}
	}


	public BitVector(int size, List<Long> list) {
		mySize = size;
		bitVectorList = list;
	}


	/* Checks for size (automatically pads with 0s if too small) */
	public void verifySize(int index) {
		if((index + 1) > mySize) {
			int oldAdjustedSize = ((mySize-1) / 64) + 1;
			int newAdjustedSize = ((index) / 64) + 1;
			for(int i = oldAdjustedSize; i < newAdjustedSize; i++) {
				long newLong = 0;
				bitVectorList.add(newLong);
			}
			mySize = index + 1;
		}
	}


	/* Set the bit at the index (to 1) */
	public void setBit(int index) {
		verifySize(index);
		int adjustedSize = (index / 64);
		long bitVectorChunk = bitVectorList.get(adjustedSize);
		long one = 1;
		bitVectorChunk = bitVectorChunk | (one << (index % 64));
		bitVectorList.set(adjustedSize, bitVectorChunk);
	}
	

	/* Clear the bit at the index (set to 0) */
	public void clearBit(int index) {
		verifySize(index);
		int adjustedSize = (index / 64);
		long bitVectorChunk = bitVectorList.get(adjustedSize);
		long one = 1;
		long mask = bitVectorChunk & (one << (index % 64));
		bitVectorChunk = bitVectorChunk & (~mask);
		bitVectorList.set(adjustedSize, bitVectorChunk);
	}


	/* Returns 1 (int) if bit at index is 1, else return 0 (int) */
	public int getBit(int index) {
		verifySize(index);
		int adjustedSize = (index / 64);
		long bitVectorChunk = bitVectorList.get(adjustedSize);
		long one = 1;
		bitVectorChunk = bitVectorChunk & (one << (index % 64));
		return (bitVectorChunk != 0)? 1 : 0;
	}
	

	/* Check for 0 across all longs */
	public boolean isZero() {
		long myLong;
		for(int i = 0; i < bitVectorList.size()-1; ++i) {
			myLong = bitVectorList.get(i);
			if(myLong != 0) 
				return false;
		}
		myLong = bitVectorList.get(bitVectorList.size()-1);
		long mask = (-1) << (mySize % 64);
		return (myLong & (~mask)) == 0;
	}

	/* Perform a bitwise-not operation on the BitVector */
	public void invert() {
		for(int i = 0; i < bitVectorList.size(); i++)
			bitVectorList.set(i, ~bitVectorList.get(i));
	}


	/* Set all bit in the interval spanned by indices (to 1; inclusive) */
	public void setRange(int idx1, int idx2) {
		for(int i = idx1; i <= idx2; i++)
			setBit(i);
	}


	/* Set all bit in the interval spanned by indices (to 1; inclusive) */
	public void clearRange(int idx1, int idx2) {
		for(int i = idx1; i <= idx2; i++)
			clearBit(i);
	}
	

	/* Get BitVector size */
	public int getSize() {
		return mySize;
	}
	

	/* Get internal list of longs */
	public List<Long> getBitVectorList() {
		return bitVectorList;
	}


	/* Get index of the first non-zero bit (across all of the longs) */
	public int getFirstBitIdx() {
		int k = 0;
		while(bitVectorList.get(k) == 0)
			k++;
		return 64*k + Long.numberOfTrailingZeros(bitVectorList.get(k));
	}
	

	/* Returns the number of set bits in the BitVector */
	public int getCardinality() {
		int cardinality = 0;
		for(int i = 0; i < bitVectorList.size(); i++) {
			cardinality += Long.bitCount(bitVectorList.get(i));
		}
		return cardinality;
	}
	

	/* Prints the BitVector's value in hexadecimal */
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		sb.append(Long.toHexString(bitVectorList.get(0)));
		for(int i = 1; i < bitVectorList.size(); ++i) {
			sb.append(",");
			sb.append(Long.toHexString(bitVectorList.get(i)));
		}
		sb.append("]");
		return sb.toString();
	}
	
	/* Prints the BitVector's value in binary */
	public String toBinaryString() {
		StringBuilder sb = new StringBuilder("[");
		sb.append(Long.toBinaryString(bitVectorList.get(0)));
		for(int i = 1; i < bitVectorList.size(); ++i) {
			sb.append(",");
			sb.append(Long.toBinaryString(bitVectorList.get(i)));
		}
		sb.append("]");
		return sb.toString();
	}

	/* Checks for bitwise equality between two BitVectors */
	public boolean equals(BitVector bitVector) {
		int numLongs = (Math.min(bitVector.getSize(), getSize()) / 64) + 1;
		for(int i = 0; i < numLongs; i++) {
			long chunk1 = bitVector.getBitVectorList().get(i);
			long chunk2 = getBitVectorList().get(i);
			if(chunk1 != chunk2) return false;
		}
		return true;
	}
	

	/* Creates a new BitVector object that corresponds to performing a bitwise-or */
	public BitVector union(BitVector bitVector) {
		int size = Math.min(bitVector.getSize(), getSize());
		int numLongs = (size / 64) + 1;
		List<Long> newBitVector = new ArrayList<Long>();
		for(int i = 0; i < numLongs; i++) {
			long chunk1 = bitVector.getBitVectorList().get(i);
			long chunk2 = getBitVectorList().get(i);
			long newChunk = chunk1 | chunk2;
			newBitVector.add(newChunk);
		}
		return new BitVector(size, newBitVector);
	}
	

	/* Creates a new BitVector object that corresponds to performing a bitwise-and */
	public BitVector intersection(BitVector bitVector) {
		int size = Math.min(bitVector.getSize(), getSize());
		int numLongs = (size / 64) + 1;
		List<Long> newBitVector = new ArrayList<Long>();
		for(int i = 0; i < numLongs; i++) {
			long chunk1 = bitVector.getBitVectorList().get(i);
			long chunk2 = getBitVectorList().get(i);
			long newChunk = chunk1 & chunk2;
			newBitVector.add(newChunk);
		}
		return new BitVector(size, newBitVector);
	}
	

	/* Creates a new BitVector object that corresponds to performing a bitwise-xor */
	public BitVector correspondence(BitVector bitVector) {
		int size = Math.min(bitVector.getSize(), getSize());
		int numLongs = (size / 64) + 1;
		List<Long> newBitVector = new ArrayList<Long>();
		for(int i = 0; i < numLongs; i++) {
			long chunk1 = bitVector.getBitVectorList().get(i);
			long chunk2 = getBitVectorList().get(i);
			long newChunk = chunk1 ^ chunk2;
			newBitVector.add(newChunk);
		}
		return new BitVector(size, newBitVector);
	}
}



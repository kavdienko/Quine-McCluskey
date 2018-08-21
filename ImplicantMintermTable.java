import java.util.ArrayList;
import java.util.List;

/**
 * This is the class that you will be working on to implement the
 * Quine-McCluskey algorithm. An example workflow would be:
 * ImplicantMintermTable imt = new ImplicantMintermTable(3, Arrays.asList("ab",
 * "aC", "BC", "bc"), Arrays.asList("ABC", "Abc", "aBC", "abC", "abc")); List<
 * String > finalCover = imt.createFinalCover();
 * System.out.println(Arrays.toString(finalCover)); >> ["ab", "BC", "bc"]
 */
public class ImplicantMintermTable {
	private BitVector[] rows; // Holds the rows as a BitVector array, with one row
														// for each implicant
	private BitVector[] columns; // Holds the cols as a BitVector array, with one
																// col for each minterm
	private BitVector rowCover; // Has a 0 for each row which has been covered
															// and a 1 for each uncovered row
	private BitVector columnCover; // Has a 0 for each column which has been
																 // covered and 1 for each uncovered column
	private List<String> originalMinterms; // Has original minterms as strings
	private List<String> originalImplicants; // Has original implicants as strings
	private List<String> finalImplicants; // Will hold final implicants
	private boolean cyclicCoreRemains = false; // Will be set to true when a
																							// cyclic
																							// core remains

	/**
	 * Takes in an int corresponding to the number of literals, a list of string
	 * corresponding to the prime implicants of the function (what Tabulation
	 * would have generated; see end of write-up), and a list of the minterms that
	 * fully describe the function. You are to mold this information into some
	 * internal form such that a single call to "createFinalCover()" would
	 * successfully yield the appropriate cover for the function in question.
	 */
	public ImplicantMintermTable(int numVars, List<String> implicants, List<String> minterms) {

		//Variable initialization
		this.rows = new BitVector[implicants.size()]; // one row for each implicant
		this.columns = new BitVector[minterms.size()]; // one column for each
																										// minterm
		this.rowCover = new BitVector(implicants.size());
		this.columnCover = new BitVector(minterms.size());
		this.originalMinterms = minterms;
		this.originalImplicants = implicants;
		this.finalImplicants = new ArrayList<String>();
		
		BitVector result = new BitVector(numVars);
		BitVector[] minVectors = new BitVector[minterms.size()];

		// Initializing each BitVector in the rows array, where each row BitVector
		// represents an
		// implicant and has one bit for each minterm
		for (int i = 0; i < rows.length; i++) {
			rows[i] = new BitVector(minterms.size());
			rowCover.setBit(i); // Initialize row cover to all 1's
		}

		// Initializing each BitVector in the columns array, where each column
		// BitVector represents a minterm and has one bit for each implicant
		// Also creating an array of BitVector representations of each minterm
		for (int j = 0; j < columns.length; j++) {
			columns[j] = new BitVector(implicants.size());
			minVectors[j] = stringToBitVector(numVars, minterms.get(j));
			columnCover.setBit(j); // Initialize column cover to all 1's
		}

		// Setting the correct bits in the rows and columns to represent which
		// implicants cover which minterms. "masking" strategy from write-up
		// is being used
		for (int i = 0; i < implicants.size(); i++) {
			BitVector impMask = implicantMask(numVars, implicants.get(i));
			BitVector impVector = stringToBitVector(numVars, implicants.get(i));
			for (int j = 0; j < minterms.size(); j++) {
				result = (impMask.intersection(minVectors[j])).correspondence(impVector);

				if (result.isZero()) {
					rows[i].setBit(j);
					columns[j].setBit(i);
				}
			}
		}
	}

	/**
	 * Creates the final implicant cover using Quine-McCluskey pruning techniques
	 * with Branch and Bound. Returns the unique (considering tie-breaks) minimal
	 * cover for the function; a list of Strings that represent the implicants
	 * (capitalized for complemented, lowercase for uncomplemented).
	 */
	public List<String> createFinalCover() {

		while (!rowCover.isZero() && !columnCover.isZero()) {

			// Assume that a cyclic core remains. If essential prime removal, row
			// domination, and column domination methods yield any results,
			// cyclicCoreRemains will be set to false and the normal procedure will
			// repeat
			cyclicCoreRemains = true;
			coverEssentialPrimes();
			rowDomination();
			columnDomination();
			if (cyclicCoreRemains) {
				pruneAndBranch();
			}
		}

		return finalImplicants;
	}

	/**
	 * Recursive cyclic core resolution method
	 * -Selects a pivot, which is the implicant with most minterms. If several
	 * 	implicants have the same amount of minterms, it picks the earliest
	 *  one lexicographically
	 * -Saves final implicants, rowCover, and columnCover before recursion
	 * -Tries creating minimal cover with pivot included and saves results
	 * -Resets to pre-recursion state
	 * -Tries creating minimal cover with pivot excluded and saves results
	 * -Compares results with/without pivot and keeps the smaller minimal
	 *  cover. If size equal, keeps the results with pivot included
	 */
	private void pruneAndBranch() {
		
		// Select the implicant with the most minterms as the pivot
		BitVector pivot = new BitVector(columns.length);
		int pivotIndex = 0;
		ImplicantComparator comparator = new ImplicantComparator();

		for (int i = 0; i < rows.length; i++) {
			if (rowCover.getBit(i) == 1) {
				int currOnes = rows[i].intersection(columnCover).getCardinality();
				int pivotOnes = pivot.intersection(columnCover).getCardinality();
				if (currOnes > pivotOnes) {
					pivot = rows[i];
					pivotIndex = i;
				} else if (currOnes == pivotOnes) {
					if ((comparator.compare(originalImplicants.get(i), originalImplicants.get(pivotIndex)) < 0)) {
						pivot = rows[i];
						pivotIndex = i;
					}
				}
			}
		}

		// Save final implicants, row cover and column cover before cyclic core
		// recursion
		List<String> implicantsBeforeRecursion = new ArrayList<String>(finalImplicants);
		BitVector rowCoverBeforeRecursion = new BitVector(rowCover.getSize());
		BitVector columnCoverBeforeRecursion = new BitVector(columnCover.getSize());
		for (int i = 0; i < rowCover.getSize(); i++) {
			if (rowCover.getBit(i) == 1) {
				rowCoverBeforeRecursion.setBit(i);
			}
		}
		for (int i = 0; i < columnCover.getSize(); i++) {
			if (columnCover.getBit(i) == 1) {
				columnCoverBeforeRecursion.setBit(i);
			}
		}

		/* Try cover with pivot implicant */

		// Add pivot to final implicants
		finalImplicants.add(originalImplicants.get(pivotIndex));
		// Remove corresponding columns
		for (int i = 0; i < columns.length; i++) {
			if (columnCover.getBit(i) == 1 && rows[pivotIndex].getBit(i) == 1) {
				columnCover.clearBit(i);
			}
		}
		// Remove pivot row
		rowCover.clearBit(pivotIndex);
		// Try creating final cover
		createFinalCover();
		List<String> implicantsWithPivot = new ArrayList<String>(finalImplicants);

		/* Try cover without pivot implicant */

		// Reset final implicants to before recursion state
		finalImplicants = implicantsBeforeRecursion;
		// Reset row cover and column cover to before recursion state
		rowCover = rowCoverBeforeRecursion;
		columnCover = columnCoverBeforeRecursion;
		// Remove row with pivot implicant
		rowCover.clearBit(pivotIndex);
		// Try creating final cover
		createFinalCover();
		List<String> implicantsWithoutPivot = new ArrayList<String>(finalImplicants);

		// Keep the smaller minimal cover. If they are equal, keep the result
		// which includes the pivot implicant
		if (implicantsWithPivot.size() > implicantsWithoutPivot.size()) {
			finalImplicants = implicantsWithoutPivot;
		} else {
			finalImplicants = implicantsWithPivot;
		}

	}

	/**
	 * Helper method for the constructor to create the implicant mask The
	 * implicant mask has a 1 for each literal that the implicant string contains
	 * and a 0 for each literal that the implicant string doesn't contain. For
	 * example, implicant string aBe in 5-literal space --> implicant mask 10011
	 * (contains a,b,e, but doesn't contain c,d. Should be read 'backwards')
	 */
	private BitVector implicantMask(int numVars, String implicant) {
		char lowercase;
		char uppercase;
		BitVector impMask = new BitVector(numVars);
		for (int i = 0; i < numVars; i++) {
			lowercase = (char) (i + (int) 'a');
			uppercase = (char) (i + (int) 'A');
			if (implicant.indexOf(lowercase) >= 0 || implicant.indexOf(uppercase) >= 0)
				impMask.setBit(i);
		}

		return impMask;
	}

	/**
	 * Helper method for the constructor to create the implicant or minterm bit
	 * vector The resulting vector will have 1's for all the un-complemented
	 * literals and 0 for complemented literals or literals which don't exist. For
	 * example, minterm aBcDE --> 00101 Implicant dE --> 01000
	 */
	private BitVector stringToBitVector(int numVars, String implicant) {
		char lowercase;
		BitVector vector = new BitVector(numVars);
		for (int i = 0; i < numVars; i++) {
			lowercase = (char) (i + (int) 'a');
			if (implicant.indexOf(lowercase) >= 0)
				vector.setBit(i);
		}

		return vector;
	}

	/**
	 * Cover essential primes helper method
	 * -Adds any essential prime that it finds to the finalImplicants
	 * -Covers the row of the essential prime implicant
	 * -Covers all of the corresponding columns of the essential prime implicant
	 * -Sets cyclicCoreRemains to false if any essential primes were found
	 */
	private void coverEssentialPrimes() {
		/* Covering essential primes */
		int rowToCover;

		for (int i = 0; i < columns.length; i++) {
			/*
			 * if the column/minterm is not already covered and only one implicant
			 * covers it, cover the column, the implicant, and the implicant's other
			 * corresponding minterms
			 */

			if (columnCover.getBit(i) == 1 && columns[i].intersection(rowCover).getCardinality() == 1) {
				rowToCover = columns[i].getFirstBitIdx();
				// Finding rowToCover (getFirstBitIdx() may return a bit which has already
				// been covered)
				for (int j = 0; j < rows.length; j++) {
					if (columns[i].getBit(j) == 1 && rowCover.getBit(j) == 1) {
						rowToCover = j;
						break;
					}
				}
				cyclicCoreRemains = false; // No cyclic core yet
				for (int j = 0; j < columns.length; j++) {
					/*
					 * Cover all columns covered by essential prime implicant row,
					 * including the original column
					 */
					if (columnCover.getBit(j) == 1 && rows[rowToCover].getBit(j) == 1) {
						columnCover.clearBit(j);
					}
				}
				rowCover.clearBit(rowToCover); // Cover the essential prime implicant
																				// row
				finalImplicants.add(originalImplicants.get(rowToCover));
			}
		}
	}

	/**
	 * Row domination helper method
	 * Covers/removes any rows which are subsets of other rows
	 * Sets cyclicCoreRemains to false if any rows were removed
	 */
	private void rowDomination() {
		ImplicantComparator comparator = new ImplicantComparator();
		BitVector firstRow = new BitVector(columns.length);
		BitVector secondRow = new BitVector(columns.length);

		for (int i = 0; i < rows.length; i++) {
			for (int j = i + 1; j < rows.length; j++) {
				firstRow = rows[i].intersection(columnCover);
				secondRow = rows[j].intersection(columnCover);

				// If both rows are uncovered and the first row is a superset of the
				// second
				if (rowCover.getBit(i) == 1 && rowCover.getBit(j) == 1 && firstRow.union(secondRow).equals(firstRow)) {
					cyclicCoreRemains = false; // No cyclic core yet

					// If the second row is also a superset of the first
					if (firstRow.union(secondRow).equals(secondRow)) {
						// Remove the lexicographically later one
						if (comparator.compare(originalImplicants.get(i), originalImplicants.get(j)) < 0)
							rowCover.clearBit(j);
						else
							rowCover.clearBit(i);
					} else // Else, first row is the only superset, so remove second row
						rowCover.clearBit(j);
					// Else if both rows are uncovered and the second row is a superset of
					// the first
				} else if (rowCover.getBit(i) == 1 && rowCover.getBit(j) == 1 && firstRow.union(secondRow).equals(secondRow)) {
					cyclicCoreRemains = false; // No cyclic core yet
					rowCover.clearBit(i);
				}
			}
		}

	}

	/**
	 * Column domination helper method 
	 * Covers/removes any columns which are supersets of other columns
	 * Sets cyclicCoreRemains to false if any columns were removed
	 */
	private void columnDomination() {
		ImplicantComparator comparator = new ImplicantComparator();
		BitVector firstCol = new BitVector(rows.length);
		BitVector secondCol = new BitVector(rows.length);

		for (int i = 0; i < columns.length; i++) {
			for (int j = i + 1; j < columns.length; j++) {
				firstCol = columns[i].intersection(rowCover);
				secondCol = columns[j].intersection(rowCover);

				// If both columns are uncovered and the first column is a superset of
				// the second
				if (columnCover.getBit(i) == 1 && columnCover.getBit(j) == 1 && firstCol.union(secondCol).equals(firstCol)) {
					cyclicCoreRemains = false; // No cyclic core yet

					// If the second column is also a superset of the first
					if (firstCol.union(secondCol).equals(secondCol)) {
						// Remove the lexicographically later one
						if (comparator.compare(originalMinterms.get(i), originalMinterms.get(j)) < 0)
							columnCover.clearBit(j);
						else
							columnCover.clearBit(i);
					} else // Else, first column is the only superset, so remove first
									// column
						columnCover.clearBit(i);
					// Else if both rows are uncovered and the second row is a superset of
					// the first
				} else if (columnCover.getBit(i) == 1 && columnCover.getBit(j) == 1
						&& firstCol.union(secondCol).equals(secondCol)) {
					cyclicCoreRemains = false; // No cyclic core yet
					columnCover.clearBit(j);
				}
			}
		}

	}

	/**
	 * Helper method to print rows (for testing purposes)
	 */
	private void printRows() {
		for (int i = 0; i < this.rows.length; i++) {
			System.out.println(rows[i].toBinaryString());
		}
	}

	/**
	 * Helper method to print columns (for testing purposes)
	 */
	private void printColumns() {
		for (int i = 0; i < this.columns.length; i++) {
			System.out.println(columns[i].toBinaryString());
		}
	}

}
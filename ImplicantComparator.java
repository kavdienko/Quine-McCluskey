import java.util.*;
/**
 * Comparator class that provides the type of lexicographic sorting that
 * is used for the conventions in this lab. It will internally sort the 
 * characters within the strings provided using the same lexicographic 
 * scheme for convenience. Example usage is as follows:
 * e.g.
 *     Collections.sort(strList, new ImplicantComparator());
 * or
 *     Arrays.sort(strArr, new ImplicantComparator());
 * or
 *     int cmp = new ImplicantComparator().compare(str1, str2);
 */
public class ImplicantComparator implements Comparator<String> {
	public int compare(String str1, String str2) {
		if(str1 == null || str1.equals("")) {
			return -1;
		}
		if(str2 == null || str2.equals("")) {
			return 1;
		}
		char[] arr1 = str1.toCharArray();
		char[] arr2 = str2.toCharArray();
		sortChars(arr1, 0, str1.length());
		sortChars(arr2, 0, str2.length());
		
		int len = Math.min(str1.length(), str2.length());
		for(int i = 0; i < len; ++i) {
			int diff = Character.toLowerCase(arr1[i]) - Character.toLowerCase(arr2[i]);
			if(diff != 0) {
				return diff;
			}
			diff = arr1[i] - arr2[i];
			if(diff != 0) {
				return diff;
			}
		}
		System.out.println(str1.length() - str2.length());
		return str1.length() - str2.length();

	}


	/** 
	 * Public method to lexicographically sort one single string (i.e. the characters
	 * in it). This is here just in case (it is not necessary for the lab). 
	 */
	public String sortString(String str) {
		if(str == null || str.equals(""))
			return str;
		char[] arr = str.toCharArray();
		sortChars(arr, 0, str.length());
		return new String(arr);
	}


	/* A mergesort implementation to lexicographically order a string internally */
	private void sortChars(char[] arr, int l, int r) {
		if(r - l != 1) {
			int m = (r + l + 1) / 2;
			sortChars(arr, l, m);
			sortChars(arr, m, r);
			mergeChars(arr, l, m, r);
		}
	}


	/* The merging bit of the mergesort */
	private void mergeChars(char[] arr, int l, int m, int r) {
		char[] temp = new char[r - l];
		int i = 0;
		int j = 0;
		int diff = Character.toLowerCase(arr[l+i]) - Character.toLowerCase(arr[m+j]);
		for(int k = 0; k < r-l; ++k) {
			if(i >= m-l) {
				temp[k] = arr[m+j];
				++j;
			}
			else if(j >= r-m) {
				temp[k] = arr[l+i];
				++i;
			}
			else if(diff < 0) {
				temp[k] = arr[l+i];
				++i;
			}
			else if(diff > 0) {
				temp[k] = arr[m+j];
				++j;
			}
			else if(arr[l+i] < arr[m+j]) {
				temp[k] = arr[l+i];
				++i;
			}
			else {
				temp[k] = arr[m+j];
				++j;
			}
		}
		for(int k = 0; k < r-l; ++k) {
			arr[l+k] = temp[k];
		}
	}
}













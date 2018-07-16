package sk.linhard.exactly.impl;

import static sk.linhard.exactly.impl.sais.suffixsort;

import java.util.Arrays;
import java.util.Stack;

/**
 * Creates an extended suffix array based on algorithms described in "Replacing
 * suffix trees with enhanced suffix arrays" by Abouelhoda, Kurtz Ohlebusch
 * 
 * The suffix array is constructed using SA-IS algorithm implementation by Yuta
 * Mori.
 * 
 */
class EnhancedSuffixArray {

	protected final static int UNDEF = -1;

	public byte[] data;
	public int[] SA;
	public int[] lcp;
	public int[] rank; // byproduct of lcp computation
	public int[] up;
	public int[] down;
	public int[] next;

	EnhancedSuffixArray(byte[] data) {
		this.data = data;
		this.SA = new int[data.length + 1];
		suffixsort(data, this.SA, data.length);
	}

	void computeLCP() {
		computeLCP(false);
	}

	void computeLCP(boolean keepRank) {
		int start = 0;
		int length = data.length;
		this.rank = new int[length];
		for (int i = 0; i < length; i++)
			rank[SA[i]] = i;
		int h = 0;
		this.lcp = new int[length + 1];
		for (int i = 0; i < length; i++) {
			int k = rank[i];
			if (k == 0) {
				lcp[k] = -1;
			} else {
				final int j = SA[k - 1];
				while (i + h < length && j + h < length && data[start + i + h] == data[start + j + h]) {
					h++;
				}
				lcp[k] = h;
			}
			if (h > 0)
				h--;
		}
		lcp[0] = 0;
		lcp[length] = 0;
		if (!keepRank) {
			rank = null;
		}
	}

	void computeUpDown() {
		up = new int[lcp.length];
		down = new int[lcp.length];
		Arrays.fill(up, UNDEF);
		Arrays.fill(down, UNDEF);

		int lastIndex = UNDEF;
		Stack<Integer> stack = new Stack<>();
		stack.push(0);
		for (int i = 1; i < lcp.length; i++) {
			while (lcp[i] < lcp[stack.peek()]) {
				lastIndex = stack.pop();
				if (lcp[i] <= lcp[stack.peek()] && lcp[stack.peek()] != lcp[lastIndex]) {
					down[stack.peek()] = lastIndex;
				}
			}
			if (lastIndex != UNDEF) {
				up[i] = lastIndex;
				lastIndex = UNDEF;
			}
			stack.push(i);
		}
	}

	void computeNext() {
		next = new int[lcp.length];
		Arrays.fill(next, UNDEF);

		Stack<Integer> stack = new Stack<>();
		stack.push(0);
		for (int i = 1; i < lcp.length; i++) {
			while (lcp[i] < lcp[stack.peek()]) {
				stack.pop();
			}
			if (lcp[i] == lcp[stack.peek()]) {
				int lastIndex = stack.pop();
				next[lastIndex] = i;
			}
			stack.push(i);
		}
	}

	void introduceSeparators(int[] offsets, byte[] separator) {
		int separatorExtraSpace = (offsets.length - 1) * separator.length;
		byte[] newData = new byte[data.length + separatorExtraSpace];
		int lastIdx = offsets.length - 1;
		for (int i = 0; i < lastIdx; i++) {
			int oldOffset = offsets[i];
			separatorExtraSpace = i * separator.length;
			moveSegment(oldOffset, offsets[i + 1], separatorExtraSpace, newData);
			offsets[i] = oldOffset + separatorExtraSpace;
		}
		int oldOffset = offsets[lastIdx];
		separatorExtraSpace = lastIdx * separator.length;
		moveSegment(oldOffset, data.length, separatorExtraSpace, newData);
		offsets[lastIdx] = oldOffset + separatorExtraSpace;

		for (int i = 0; i < separator.length; i++) {
			byte sepChar = separator[i];
			for (int j = 1; j < offsets.length; j++) {
				newData[offsets[j] - separator.length + i] = sepChar;
			}
		}

		data = newData;
	}

	void moveSegment(int start, int end, int separatorExtraSpace, byte[] newData) {
		System.arraycopy(data, start, newData, start + separatorExtraSpace, end - start);
		for (int j = start; j < end; j++) {
			SA[rank[j]] += separatorExtraSpace;
		}
	}
}

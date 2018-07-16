package sk.linhard.exactly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import sk.linhard.exactly.impl.DefaultSearch;
import sk.linhard.exactly.impl.MultiDocumentSearch;

public class SearchBuilder {

	private List<DocumentReader> documents = new ArrayList<>();

	public void add(String id, byte[] content) {
		documents.add(new BytesCopier(id, content));
	}

	public void add(String id, File file, int fileLength) {
		documents.add(new FileReader(id, file, fileLength));
	}

	public int size() {
		return documents.size();
	}

	public int totalLength() {
		return documents.stream().map(d -> d.length()).reduce(0, (a, b) -> a + b);
	}

	public Search<byte[]> build() {
		return build(null);
	}

	public Search<byte[]> build(byte[] separator) {
		if (documents.isEmpty()) {
			throw new IllegalStateException("No documents");
		} else if (documents.size() == 1) {
			return DefaultSearch.compute(documents.get(0));
		} else {
			byte[] data = new byte[totalLength()];
			int[] offsets = new int[documents.size()];
			String[] ids = new String[documents.size()];
			int offset = 0;
			for (int i = 0; i < offsets.length; i++) {
				DocumentReader reader = documents.get(i);
				int read = reader.read(data, offset);
				if (read != reader.length()) {
					throw new RuntimeException("Read unexpected length " + read + " of document " + reader.id());
				}
				offsets[i] = offset;
				offset += read;
				ids[i] = reader.id();
			}
			if (separator == null) {
				return MultiDocumentSearch.compute(data, offsets, ids);
			} else {
				return MultiDocumentSearch.compute(data, offsets, ids, separator);
			}
		}
	}

	private static abstract class DocumentReader implements Document<byte[]> {

		private String id;

		public DocumentReader(String id) {
			this.id = id;
		}

		public abstract int read(byte[] buffer, int offset);

		public abstract int length();

		@Override
		public int index() {
			return -1;
		}

		@Override
		public String id() {
			return id;
		}
	}

	private static class BytesCopier extends DocumentReader {

		private byte[] content;

		public BytesCopier(String id, byte[] content) {
			super(id);
			this.content = content;
		}

		@Override
		public byte[] content() {
			return content;
		}

		@Override
		public int length() {
			return content.length;
		}

		@Override
		public int read(byte[] buffer, int offset) {
			System.arraycopy(content, 0, buffer, offset, content.length);
			return content.length;
		}

	}

	private static class FileReader extends DocumentReader {
		private File file;
		private int length;

		public FileReader(String id, File file, int length) {
			super(id);
			this.file = file;
			this.length = length;
		}

		@Override
		public int read(byte[] buffer, int offset) {
			try {
				return IOUtils.read(new FileInputStream(file), buffer, offset, length);
			} catch (IOException e) {
				throw new RuntimeException("Error reading file " + file.getAbsolutePath(), e);
			}
		}

		@Override
		public byte[] content() {
			byte[] content = new byte[length];
			read(content, 0);
			return content;
		}

		@Override
		public int length() {
			return length;
		}
	}

}

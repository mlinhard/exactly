package sk.linhard.exactly.tika;

import java.io.File;

public class ParseResult implements Comparable<ParseResult> {

	private final Type type;
	private final File file;
	private final long fileSize;
	private final int textLenght;
	private final String contentSample;
	private final long parseTime;

	public ParseResult(Type type, File file, long fileSize, int textLenght, String contentSample, long parseTime) {
		super();
		this.type = type;
		this.file = file;
		this.fileSize = fileSize;
		this.textLenght = textLenght;
		this.contentSample = contentSample;
		this.parseTime = parseTime;
	}

	public Type getType() {
		return type;
	}

	public File getFile() {
		return file;
	}

	public long getFileSize() {
		return fileSize;
	}

	public Integer getTextLenght() {
		return textLenght;
	}

	public String getContentSample() {
		return contentSample;
	}

	public long getParseTime() {
		return parseTime;
	}

	public static enum Type {
		PARSED, BIG, EMPTY, ERROR;
	}

	@Override
	public int compareTo(ParseResult o) {
		return this.file.compareTo(o.file);
	}

}

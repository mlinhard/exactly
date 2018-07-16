package sk.linhard.exactly.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneSearchBuilder {

	private StandardAnalyzer analyzer;
	private Directory index;
	private IndexWriterConfig config;
	private IndexWriter w;

	public LuceneSearchBuilder() {
		try {
			analyzer = new StandardAnalyzer();
			index = new RAMDirectory();
			config = new IndexWriterConfig(analyzer);
			w = new IndexWriter(index, config);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addDocument(File file) {
		try {
			Document doc = new Document();
			doc.add(new TextField("text",
					new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))));
			doc.add(new StringField("path", file.getAbsolutePath(), Field.Store.YES));
			w.addDocument(doc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public LuceneSearch build() {
		return null;
	}
}

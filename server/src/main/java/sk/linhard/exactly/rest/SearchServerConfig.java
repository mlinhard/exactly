package sk.linhard.exactly.rest;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchServerConfig {

	@Value("${root.folder}")
	private String indexedFolderRoot;

	public File getIndexedFolderRoot() {
		return new File(indexedFolderRoot);
	}
}

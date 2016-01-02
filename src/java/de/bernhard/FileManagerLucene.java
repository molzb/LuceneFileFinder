package de.bernhard;

import java.io.File;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.jackson.map.ObjectMapper;

public class FileManagerLucene {

	private static final Logger LOG = Logger.getLogger(FileManagerLucene.class.getName());
	private static final int HITS_PER_PAGE = 1000;
	
	private final Directory index;
	private final StandardAnalyzer analyzer;
	private IndexWriter indexWriter;
	private final String indexDir = "/usr/local/tomcat8/webapps/LuceneFileFinder/index";
	
	public String indexedDir = "/";
	public String term;
	public int numDocs = 0;
	public static final String FIELD_NAME = "name",
			FIELD_PATH = "path",
			FIELD_SIZE_STR = "size",
			FIELD_DATE_MODIFIED = "modified",
			FIELD_DIR = "dir",
			FIELD_HIDDEN = "hidden";

	public FileManagerLucene() throws IOException {
		analyzer = new StandardAnalyzer();
		if (!new File(indexDir).exists()) {
			if (!new File(indexDir).mkdir()) {
				throw new IOException("Couldn't create dir " + new File(indexDir));
			}
		}
		index = FSDirectory.open(Paths.get(indexDir));
	}

	private String padTo10(long num) {
		return String.format("%010d", num);
	}
	
	private String padTo13(long num) {
		return String.format("%013d", num);
	}

	public String toJson(List<FileModel> fms) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(fms).replaceAll("},", "},\n");
	}

	public void createIndex(String path) throws IOException {
		if (new File(indexDir).exists() && new File(indexDir).list().length > 0) {
			LOG.warning("Index-Verz. besteht bereits. Es wird kein neuer Index angelegt!");
			return;
		}
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		indexWriter = new IndexWriter(index, config);
		walkThroughDirectories(new File(path).listFiles());
		indexWriter.close();
	}

	public List<FileModel> searchTerm(String querystr) throws ParseException, IOException {
		this.term = querystr;
		Query q = new QueryParser(FIELD_NAME, analyzer).parse(querystr);
		return showQueryResults(q);
	}

	public List<FileModel> search(String field, String value) throws ParseException, IOException {
		Query q = new QueryParser(field, analyzer).parse(value);
		return showQueryResults(q);
	}

	public List<FileModel> searchNumericRange(String field, long lower, long upper) throws ParseException, IOException {
		NumericRangeQuery<Long> q = NumericRangeQuery.newLongRange(field, lower, upper, true, true);
		return showQueryResults(q);
	}

	private List<FileModel> showQueryResults(Query q) throws IOException {
		List<FileModel> fms = new ArrayList<>();
		IndexReader reader = DirectoryReader.open(index);
		numDocs = reader.numDocs();
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(HITS_PER_PAGE);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		LOG.info("Found " + hits.length + " hits for " + q);
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document doc = searcher.doc(docId);
			long modified = Long.parseLong(doc.get(FIELD_DATE_MODIFIED));
			FileModel fm = new FileModel(doc.get(FIELD_NAME), Long.parseLong(doc.get(FIELD_SIZE_STR)),
					doc.get(FIELD_DIR).equals("1"), doc.get(FIELD_HIDDEN).equals("1"), doc.get(FIELD_PATH),
					new Date(modified), FileModel.df.format(new Date(modified)));
			fms.add(fm);
		}

		reader.close();
		return fms;
	}

	public void walkThroughDirectories(File[] files) throws IOException {
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				addDoc(indexWriter, file);
				walkThroughDirectories(file.listFiles());
			} else {
				addDoc(indexWriter, file);
			}
		}
	}

	private void addDoc(IndexWriter w, File f) throws IOException {
		boolean isDir = f.isDirectory();
		boolean isHidden = f.isHidden();
		Document doc = new Document();
		doc.add(new TextField(FIELD_NAME, f.getName(), Field.Store.YES));
		doc.add(new StringField(FIELD_PATH, f.getParent(), Field.Store.YES));
		doc.add(new StringField(FIELD_DATE_MODIFIED, padTo13(f.lastModified()), Field.Store.YES));
		doc.add(new StringField(FIELD_SIZE_STR, padTo10(isDir ? 0L : f.length()), Field.Store.YES));
		doc.add(new StringField(FIELD_DIR, isDir ? "1" : "0", Field.Store.YES));
		doc.add(new StringField(FIELD_HIDDEN, isHidden ? "1" : "0", Field.Store.YES));
		w.addDocument(doc);
	}
}

package com.infobox.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Generic methods for querying index
 * 
 * Singleton to extract information from index
 * 
 * @author jms
 *
 */
public class Searcher {

	private static Searcher instance = null;
	private Analyzer analyzer;
	private Searcher() {}

	public static Searcher getInstance() {
		if (instance == null) {
			instance = new Searcher();
		}
		return instance;
	}

	/**
	 * 
	 * @param directory
	 * @return IndexSearcher
	 * @throws IOException
	 */
	public IndexSearcher createSearcher(String directory, Analyzer analyzer) throws IOException {
		Directory dir = FSDirectory.open(Paths.get(directory));
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		this.analyzer = analyzer;
		return searcher;
	}
	
	private String convertText2MatchEntireText(String query) {
		return "\"" + query + "\"";
	}
	
	/**
	 * 
	 * @param object
	 * @param searcher
	 * @return TopDocs
	 * @throws Exception
	 */
	public TopDocs searchByObject(String object, IndexSearcher searcher) throws Exception {
		QueryParser qp = new QueryParser("object", this.analyzer);
		String query = QueryParser.escape(object);
		query = convertText2MatchEntireText(query);
		Query objectQuery = qp.parse(query);
		TopDocs hits = searcher.search(objectQuery,  1000000);
		return hits;
	}

	/**
	 * 
	 * @param predicate
	 * @param searcher
	 * @return TopDocs
	 * @throws Exception
	 */
	public TopDocs searchByPredicate(String predicate, IndexSearcher searcher) throws Exception {
		QueryParser qp = new QueryParser("predicate", this.analyzer);
		String query = QueryParser.escape(predicate);
		query = convertText2MatchEntireText(query);
		Query predicateQuery = qp.parse(query);
		TopDocs hits = searcher.search(predicateQuery,  1000000);
		return hits;
	}

	/**
	 * 
	 * @param subject
	 * @param searcher
	 * @return TopDocs
	 */
	public TopDocs searchBySubject(String subject, IndexSearcher searcher){
		
		QueryParser qp = new QueryParser("subject", this.analyzer);
		String query = QueryParser.escape(subject);
		query = convertText2MatchEntireText(query);
		
		TopDocs hits = null;
		
		try {
			Query subjectQuery = qp.parse(query);
			hits = searcher.search(subjectQuery,  1000000);
			
		} catch (ParseException e) {
			System.out.println("ERRO Parse: " + subject);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERRO IO: " + subject);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("ERRO General: " + subject);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}		
		return hits;
	}
	
	/**
	 * Returns all subjects from given searcher
	 * @param searcher
	 * @return
	 * @throws IOException
	 */
	public TopDocs searchAllSubjects(IndexSearcher searcher) throws IOException {
		
		Term term = new Term("subject", "*");
		Query subjectQuery = new WildcardQuery(term);
		TopDocs hits = searcher.search(subjectQuery, 100000000);
		return hits;
	}
	
	/**
	 * Returns all objects from given searcher
	 * @param searcher
	 * @return TopDocs
	 * @throws IOException
	 */
	public TopDocs searchAllObjects(IndexSearcher searcher) throws IOException {
		Term term = new Term("object", "*");
		Query objectQuery = new WildcardQuery(term);
		TopDocs hits = searcher.search(objectQuery, 100000000);
		return hits;
	}
	
	/**
	 * Returns all unique terms from given searcher and term name
	 * @param searcher
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public Terms searchAllUniqueTerms(IndexSearcher searcher, String term) throws IOException {

		Fields fields = MultiFields.getFields(searcher.getIndexReader());
        Terms terms = fields.terms(term);
        
        return terms;
	}

}

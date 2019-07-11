package com.infobox.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

import com.infobox.beans.Entity;
import com.infobox.beans.InfoboxSchema;
import com.infobox.beans.InfoboxTuple;
import com.infobox.util.Constants;


/**
 * 
 * Specific queries using generic searcher
 * 
 * @author jms
 *
 */
public class QueryData {
	
	private static Searcher searcher;
	private static IndexSearcher indexTemplateSearcher;
	private static IndexSearcher indexInfoboxSearcher;
	
	public QueryData() throws IOException {
		searcher = Searcher.getInstance();
		indexTemplateSearcher = searcher.createSearcher(Constants.INDEX_TEMPLATES_DIR, new KeywordAnalyzer());
		indexInfoboxSearcher = searcher.createSearcher(Constants.INDEX_INFOBOXES_DIR,  new KeywordAnalyzer());
	}
	
	/**
	 * Queries all indexed infoboxes
	 * @return List of entities
	 * @throws Exception 
	 */
	public List<Entity> queryAllInfoboxes() throws Exception {
		
		List<Entity> EntitysWithInfoboxes = new ArrayList<Entity>();
		
		Terms foundEntitys = searcher.searchAllUniqueTerms(indexInfoboxSearcher, "subject");
		
		TermsEnum termsIterator = foundEntitys.iterator();
        BytesRef byteRef = null;
        while((byteRef = termsIterator.next()) != null) {
            if(byteRef.isValid() && byteRef.length > 0) {
            	String EntityTitle  = byteRef.utf8ToString();
            	
            	Entity a = queryInfobox(EntityTitle);
            	if(a != null)
            		EntitysWithInfoboxes.add(a);
            }
            
        }
        
		System.out.println(">>> Found Entities with Infobox: " + EntitysWithInfoboxes.size());
		
		return EntitysWithInfoboxes;
	}
	
	/**
	 * Queries the title of all indexed entities with infoboxes
	 * @return List of entities
	 * @throws IOException 
	 */
	public List<String> queryAllIndexedEntities() throws IOException {
		
		Terms foundEntitys = searcher.searchAllUniqueTerms(indexInfoboxSearcher, "subject");
		TermsEnum termsIterator = foundEntitys.iterator();
		
        List<String> EntitysTitle = new ArrayList<>();
        BytesRef byteRef = null;
       
        while((byteRef = termsIterator.next()) != null) {
                	
            if(byteRef.isValid() && byteRef.length > 0) 
            	EntitysTitle.add(byteRef.utf8ToString());
            
        }
		
		System.out.println(">>> foundEntities: " + EntitysTitle.size());
		
		return EntitysTitle;
	}
	
	/**
	 * Queries infobox instance of a given entity
	 * @param entityTitle
	 * @return
	 * @throws Exception
	 */
	public Entity queryInfobox(String entityTitle) throws Exception {

		TopDocs foundInfoboxes = searcher.searchBySubject(entityTitle, indexInfoboxSearcher);
		
		List<InfoboxTuple> tuples = new ArrayList<>();
		// get other infobox properties
		if(foundInfoboxes != null && foundInfoboxes.totalHits > 0) {
			for (ScoreDoc scoreProperty : foundInfoboxes.scoreDocs) {
				Document property = indexInfoboxSearcher.doc(scoreProperty.doc);
				String infoboxProperty = property.get("predicate");
				String infoboxValue = property.get("object");

				if(!tuples.stream().anyMatch(tuple -> tuple.getProperty().equals(infoboxProperty))) 
					tuples.add(new InfoboxTuple(infoboxProperty, infoboxValue));

			}
		}
		
		String templateName = queryTemplateUsedByEntity(entityTitle);
		
		if(templateName!=null && !tuples.isEmpty())
			return new Entity(entityTitle, new InfoboxSchema(templateName, tuples));
		else
			return null;
	}
	
	/**
	 * Queries template used by entity infobox
	 * @param entityTitle
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public String queryTemplateUsedByEntity(String entityTitle) throws Exception, IOException{
		String templateType = null;
		
		TopDocs foundTemplate = searcher.searchBySubject(entityTitle, indexTemplateSearcher);
		if(foundTemplate.totalHits > 0) {
			Document doc = indexTemplateSearcher.doc(foundTemplate.scoreDocs[0].doc);
			return doc.get("object");
		}
		
		return templateType;
	}
	
	/**
	 * Queries all entities using a given template class
	 * @param templateName
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public List<Entity> queryEntitiesUsingTemplate(String templateName) throws Exception, IOException {
		List<Entity> entities = new ArrayList<Entity>();
		
		TopDocs foundEntities = searcher.searchByObject(templateName, indexTemplateSearcher);
		
		if(foundEntities.totalHits > 0) {
			for(ScoreDoc scoreEntity : foundEntities.scoreDocs){
				Document entityDoc = indexTemplateSearcher.doc(scoreEntity.doc);
				String entityName = entityDoc.get("subject");
				
				Entity e = queryInfobox(entityName);
				if(e != null){
					entities.add(e);
				}
			}
		}

		return entities;
	}
	
	/**
	 * Queries all tuples for a given infobox schema
	 * 
	 * @param schema
	 * @return
	 * @throws Exception
	 */
	public List<InfoboxTuple> queryAllTuplesFromSchema(List<String> schema) throws Exception{
		List<InfoboxTuple> tuples = new ArrayList<InfoboxTuple>();
		
		for(String attribute : schema) {
			TopDocs foundProperties = searcher.searchByPredicate(attribute, indexInfoboxSearcher);
			
			if(foundProperties.totalHits > 0) {
				
				for(ScoreDoc scoreProps : foundProperties.scoreDocs) {
					Document propsDoc = indexInfoboxSearcher.doc(scoreProps.doc);
					String attributeValue = propsDoc.get("object");
					if(attributeValue != null && attributeValue.length() > 0) {
						InfoboxTuple tuple = new InfoboxTuple(attribute, attributeValue);
						tuples.add(tuple);
					}
				}
			}
		}
		return tuples;
	}
	
	/***
	 * Queries lucene indexes in order to define a template schema according to informed threshold.
	 * It receives an Entitys title list and a threshold. 
	 * 
	 * @param EntitysTitle
	 * @param threshold
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public List<String> querySchemaOfEntityWithThreshold(List<String> EntitysTitle, double threshold) throws Exception, IOException{
		Map<String, Integer> countProperties = new HashMap<String, Integer>();
		int countEntitysWithInfoboxMap = 0;
		
		for(String Entity: EntitysTitle) {
			
			TopDocs foundProperties = searcher.searchBySubject(Entity, indexInfoboxSearcher);
			
			if(foundProperties.totalHits > 0) {
				countEntitysWithInfoboxMap += 1;
				
				for(ScoreDoc scoreProps : foundProperties.scoreDocs) {
					
					Document propsDoc = indexInfoboxSearcher.doc(scoreProps.doc);
					String propertyName = propsDoc.get("predicate");
					String propertyValue = propsDoc.get("object").trim();
					
					if(propertyValue==null || propertyValue.length()<1) {
						continue;
					}else if(propertyValue != null && propertyValue.length()>0
							&& countProperties.containsKey(propertyName)) {
						countProperties.replace(propertyName, countProperties.get(propertyName).intValue() + 1);
					}else if(propertyValue != null&& propertyValue.length()>0 
							&& countProperties.containsKey(propertyName)==false) {
						countProperties.put(propertyName, 1);
					}
				}
			}
		}
		
		if(countProperties.size() > 0 && countEntitysWithInfoboxMap > 0) {
			
			Map<String, Double> tempProperties = new LinkedHashMap<String, Double>();
			
			for (Map.Entry<String, Integer> prop : countProperties.entrySet())
				tempProperties.put(prop.getKey(), (double) prop.getValue().intValue() / (double)countEntitysWithInfoboxMap);
			
			tempProperties = tempProperties.entrySet().stream()
			.filter(prop -> prop.getValue().doubleValue() > threshold)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			Map<String, Double> newMap = new LinkedHashMap<>();
			
			tempProperties.entrySet().stream()
					.sorted(Map.Entry.<String, Double>comparingByValue().reversed())
					.forEachOrdered(x -> newMap.put(x.getKey(), x.getValue()));
			
			newMap.forEach((k,v) -> System.out.println(k + ": " + v));
			
			List<String> selectedProperties = newMap.entrySet()
					 .stream().map(Map.Entry::getKey).collect(Collectors.toList());
			 
			return selectedProperties;
		}else
			return null;
	}
	
	/**
	 * Queries Infoboxes and Templates indexes searching for all entities using a given property
	 * from an informed template
	 * 
	 * @param templateName
	 * @param propertyName
	 * @return a list of entities using that property
	 * @throws Exception 
	 */
	public List<Entity> queryEntitiesUsingProperty(String templateName, String propertyName) throws Exception {
		List<Entity> entities = new ArrayList<Entity>();
		
		TopDocs foundEntitiesUsingTemplate = searcher.searchByObject(templateName, indexTemplateSearcher);
		if(foundEntitiesUsingTemplate.totalHits > 0) {
			
			List<String> tempEntitiesName = new ArrayList<String>();
			for(ScoreDoc scoreEntity: foundEntitiesUsingTemplate.scoreDocs) {
				
				Document entityDoc = indexTemplateSearcher.doc(scoreEntity.doc);
				String entityName = entityDoc.get("subject");
				tempEntitiesName.add(entityName);
			}
			
			TopDocs foundEntities = searcher.searchByPredicate(propertyName, indexInfoboxSearcher);
			if(foundEntities.totalHits > 0) {
				
				for (ScoreDoc scoreEntity: foundEntities.scoreDocs) {
					
					Document entityDoc = indexInfoboxSearcher.doc(scoreEntity.doc);
					String entityName = entityDoc.get("subject");
					
					if(tempEntitiesName.stream().anyMatch(x -> x.equalsIgnoreCase(entityName))) {
						String propertyValue = entityDoc.get("object");
						entities.add(new Entity(entityName, 
								new InfoboxSchema(Arrays.asList(
										new InfoboxTuple(propertyName, propertyValue)))));
					}
				}
			}
		}
		return entities;
	}
	
}

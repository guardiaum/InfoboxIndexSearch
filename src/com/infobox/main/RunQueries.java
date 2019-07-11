package com.infobox.main;

import java.io.IOException;
import java.util.List;

import com.infobox.beans.Entity;
import com.infobox.beans.InfoboxSchema;
import com.infobox.beans.InfoboxTuple;
import com.infobox.lucene.QueryData;

/**
 * 
 * Usage examples of index searches
 * 
 * @author jms
 *
 */
public class RunQueries {

	public static void main(String[] args) throws IOException, Exception {
		
		QueryData qData = new QueryData();
		
		// Queries all indexed infoboxes
		// List<Entity> entitiesWithInfoboxes = qData.queryAllInfoboxes();
		// System.out.println(entitiesWithInfoboxes.size());
		
		// Queries only entities name
		List<String> entitiesName = qData.queryAllIndexedEntities();
		
		// Query for Pablo Picasso infobox instance
		Entity picasso = qData.queryInfobox("Pablo_Picasso"); 
		
		InfoboxSchema picassoInfoboxInstance = picasso.getInfobox();
		
		System.out.println("=====================================");
		System.out.println(picasso.getEntityTitle());
		System.out.println("Template Name: " + picassoInfoboxInstance.getTemplateName());
		
		for (InfoboxTuple tuple : picassoInfoboxInstance.getTuples()) {
			System.out.println(tuple.toString());
		}
	}

}

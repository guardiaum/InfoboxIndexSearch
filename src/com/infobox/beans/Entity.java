package com.infobox.beans;
/**
 * POJO for Entity
 * @author jms
 *
 */
public class Entity {
	
	private String articleTitle;
	private InfoboxSchema infobox;

	public Entity() {
		// TODO Auto-generated constructor stub
	}
	
	public Entity(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	
	public Entity(String articleTitle, InfoboxSchema infobox) {
		super();
		this.articleTitle = articleTitle;
		this.infobox = infobox;
	}

	public InfoboxSchema getInfobox() {
		return infobox;
	}

	public void setInfobox(InfoboxSchema infobox) {
		this.infobox = infobox;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String article_title) {
		this.articleTitle = article_title;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean same = false;
		
		if( obj!=null && obj instanceof Entity){
			same = this.articleTitle.equals(((Entity) obj).getArticleTitle());
		}
		
		return same;
	}
}

package com.infobox.beans;
/**
 * POJO for Entity
 * @author jms
 *
 */
public class Entity {
	
	private String entityTitle;
	private InfoboxSchema infobox;

	public Entity() {
		// TODO Auto-generated constructor stub
	}
	
	public Entity(String entityTitle) {
		this.entityTitle = entityTitle;
	}
	
	public Entity(String entityTitle, InfoboxSchema infobox) {
		super();
		this.entityTitle = entityTitle;
		this.infobox = infobox;
	}

	public InfoboxSchema getInfobox() {
		return infobox;
	}

	public void setInfobox(InfoboxSchema infobox) {
		this.infobox = infobox;
	}

	public String getEntityTitle() {
		return entityTitle;
	}

	public void setEntityTitle(String entityTitle) {
		this.entityTitle = entityTitle;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean same = false;
		
		if( obj!=null && obj instanceof Entity){
			same = this.entityTitle.equals(((Entity) obj).getEntityTitle());
		}
		
		return same;
	}
}

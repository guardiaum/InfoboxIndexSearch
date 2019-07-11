package com.infobox.beans;
/**
 * POJO for infobox tuple
 * @author jms
 *
 */
public class InfoboxTuple {
	
	private String property;
	private String value;
	
	public InfoboxTuple(String property, String value) {
		super();
		this.property = property;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return property + " = " + value;
	}
}

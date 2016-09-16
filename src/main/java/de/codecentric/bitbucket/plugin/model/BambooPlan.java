package de.codecentric.bitbucket.plugin.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize
public class BambooPlan {
	private String key;
	private String name;
	private String description;
	private String url;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

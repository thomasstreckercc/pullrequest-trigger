package de.codecentric.bitbucket.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class BambooProject {
	private String name;
	private String key;
	private List<BambooPlan> plans = new ArrayList<BambooPlan>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<BambooPlan> getPlans() {
		return plans;
	}

}

package de.codecentric.bitbucket.plugin.rest;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.atlassian.bitbucket.rest.RestMapEntity;

@JsonSerialize
public class RestBambooInstance extends RestMapEntity {
	
    private static final long serialVersionUID = -3236313813601793027L;

    public RestBambooInstance(String id, String name) {
		put("id", id);
		put("name", name);
	}

}

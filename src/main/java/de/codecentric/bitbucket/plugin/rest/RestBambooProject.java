package de.codecentric.bitbucket.plugin.rest;

import java.util.List;
import java.util.function.Function;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.atlassian.bitbucket.rest.RestMapEntity;
import com.atlassian.bitbucket.rest.annotation.JsonSurrogate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.codecentric.bitbucket.plugin.model.BambooProject;

@JsonSerialize
@JsonSurrogate(BambooProject.class)
public class RestBambooProject extends RestMapEntity {

    public static final Function<BambooProject, RestBambooProject> REST_TRANSFORM = RestBambooProject::new;

    private static final long serialVersionUID = 8783992170419300210L;

	public RestBambooProject(BambooProject project) {
		this(project.getKey(), 
				project.getName(),
				ImmutableList.copyOf(Lists.transform(project.getPlans(), RestBambooPlan.REST_TRANSFORM::apply)));
	}

	private RestBambooProject(String key, String name, List<RestBambooPlan> plans) {
		this.put("key", key);
		this.put("name", name);
		this.put("plans", plans);
	}
}

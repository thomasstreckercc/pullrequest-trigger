package de.codecentric.bitbucket.plugin.rest;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.atlassian.bitbucket.rest.RestMapEntity;

import de.codecentric.bitbucket.plugin.dao.AoRepositoryPlan;

@JsonSerialize
public class RestRepositoryPlan extends RestMapEntity {
    private static final long serialVersionUID = 1703305539977790853L;

    public RestRepositoryPlan(AoRepositoryPlan plan) {
        this.put("repositoryId", plan.getRepositoryId());
        this.put("bambooInstance", plan.getBambooInstanceId());
        this.put("plan", plan.getPlanKey());
    }
}

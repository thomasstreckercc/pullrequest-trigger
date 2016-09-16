package de.codecentric.bitbucket.plugin.rest;

import java.util.function.Function;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.atlassian.bitbucket.rest.RestMapEntity;
import com.atlassian.bitbucket.rest.annotation.JsonSurrogate;

import de.codecentric.bitbucket.plugin.model.BambooPlan;

@JsonSerialize
@JsonSurrogate(BambooPlan.class)
public class RestBambooPlan extends RestMapEntity {
    private static final long serialVersionUID = -7360556344342388498L;
    
    public static final Function<BambooPlan, RestBambooPlan> REST_TRANSFORM = RestBambooPlan::new;

    public RestBambooPlan(BambooPlan plan) {
        this.put("name", plan.getName());
        this.put("key", plan.getKey());
        this.put("url", plan.getUrl());
    }
}

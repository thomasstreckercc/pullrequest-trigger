package de.codecentric.bitbucket.plugin.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.applinks.api.*;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.bitbucket.rest.util.ResponseFactory;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.Request.MethodType;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.resource.Singleton;

import de.codecentric.bitbucket.plugin.dao.RepositoryPlanDao;
import de.codecentric.bitbucket.plugin.model.BambooPlan;
import de.codecentric.bitbucket.plugin.model.BambooProject;

@Path("")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
@Component
public class PullRequestTriggerResource {

    private final ApplicationLinkService applicationLinkService;
    private final RepositoryPlanDao repositoryPlanDao;

    @Autowired
    public PullRequestTriggerResource(@ComponentImport ApplicationLinkService applicationLinkService,
            RepositoryPlanDao repositoryPlanDao) {
        this.applicationLinkService = applicationLinkService;
        this.repositoryPlanDao = repositoryPlanDao;
    }

    @DELETE
    @Path("configuration/{repositoryId}")
    public Response deleteConfiguration(@PathParam("repositoryId") int repositoryId) {
        repositoryPlanDao.deleteForRepositoryId(repositoryId);
        return ResponseFactory.ok().build();
    }

    @GET
    @Path("configuration/{repositoryId}")
    public Response doPost(@PathParam("repositoryId") int repositoryId, @QueryParam("instanceId") String instanceId,
            @QueryParam("planKey") String planKey) throws ServletException, IOException {
        repositoryPlanDao.update(repositoryId, instanceId, planKey);
        return ResponseFactory.ok().build();
    }

    @GET
    @Path("bambooInstances")
    public Response getBambooInstances(@Context ContainerRequest containerRequest) {
        Iterable<ApplicationLink> bambooInstances = applicationLinkService
                .getApplicationLinks(BambooApplicationType.class);
        List<RestBambooInstance> instances = new ArrayList<>();
        bambooInstances
                .forEach(instance -> instances.add(new RestBambooInstance(instance.getId().get(), instance.getName())));
        return ResponseFactory.ok(instances).build();
    }

    @GET
    @Path("plans")
    public Response getPlans(@QueryParam("bambooInstance") String bambooRepository,
            @Context ContainerRequest containerRequest) throws TypeNotInstalledException {
        ApplicationId id = new ApplicationId(bambooRepository);
        ApplicationLink bamboo = applicationLinkService.getApplicationLink(id);
        List<RestBambooProject> projects = queryProjects(bamboo);

        return ResponseFactory.ok(projects).build();
    }

    private List<RestBambooProject> queryProjects(ApplicationLink bamboo) {
        try {
            return bamboo.createAuthenticatedRequestFactory()
                    .createRequest(MethodType.GET, "/rest/api/latest/project?expand=projects.project.plans.plan")
                    .addHeader("Accept", "application/json")
                    .executeAndReturn(response -> {
                        JsonParser parser = new JsonParser();
                        JsonObject responseObject = parser.parse(response.getResponseBodyAsString()).getAsJsonObject();
                        return ImmutableList.copyOf(Lists.transform(extractProjects(responseObject),
                                RestBambooProject.REST_TRANSFORM::apply));
                    });
        } catch (CredentialsRequiredException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResponseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private List<BambooProject> extractProjects(JsonObject response) {

        List<BambooProject> projects = new ArrayList<>();
        JsonObject jsonProjects = response.get("projects").getAsJsonObject();
        JsonArray jsonProjectArray = jsonProjects.get("project").getAsJsonArray();
        for (int i = 0; i < jsonProjectArray.size(); i++) {
            BambooProject project = extractProject(jsonProjectArray.get(i).getAsJsonObject());
            projects.add(project);
        }

        return projects;
    }

    private BambooProject extractProject(JsonObject jsonProject) {
        String name = jsonProject.get("name").getAsString();
        String key = jsonProject.get("key").getAsString();

        BambooProject project = new BambooProject();
        project.setKey(key);
        project.setName(name);
        project.getPlans().addAll(extractPlans(jsonProject));

        return project;
    }

    private List<BambooPlan> extractPlans(JsonObject jsonProject) {
        List<BambooPlan> plans = new ArrayList<>();

        JsonObject jsonPlans = jsonProject.get("plans").getAsJsonObject();
        JsonArray jsonPlanArray = jsonPlans.get("plan").getAsJsonArray();
        for (int i = 0; i < jsonPlanArray.size(); i++) {
            BambooPlan plan = extractPlan(jsonPlanArray.get(i).getAsJsonObject());
            plans.add(plan);
        }

        return plans;
    }

    private BambooPlan extractPlan(JsonObject jsonPlan) {
        String name = jsonPlan.get("name").getAsString();
        String key = jsonPlan.get("key").getAsString();
        JsonObject jsonLink = jsonPlan.get("link").getAsJsonObject();
        String url = jsonLink.get("href").getAsString();

        BambooPlan plan = new BambooPlan();
        plan.setKey(key);
        plan.setName(name);
        plan.setUrl(url);

        return plan;
    }
}

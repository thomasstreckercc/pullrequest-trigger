package de.codecentric.bitbucket.plugin.events;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.applinks.api.*;
import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.Request.MethodType;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.codecentric.bitbucket.plugin.dao.AoPullRequestPlan;
import de.codecentric.bitbucket.plugin.dao.AoPullRequestPlanDao;
import de.codecentric.bitbucket.plugin.dao.AoRepositoryPlan;
import de.codecentric.bitbucket.plugin.dao.AoRepositoryPlanDao;

@Component
public class PullRequestEventListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(PullRequestEventListener.class);

    private final AoRepositoryPlanDao repositoryPlanDao;
    private final AoPullRequestPlanDao pullrequestPlanDao;
    private final ApplicationLinkService applicationLinkService;

    @Autowired
    public PullRequestEventListener(AoRepositoryPlanDao repositoryPlanDao, AoPullRequestPlanDao pullrequestPlanDao,
            @ComponentImport ApplicationLinkService applicationLinkService) {
        this.repositoryPlanDao = repositoryPlanDao;
        this.pullrequestPlanDao = pullrequestPlanDao;
        this.applicationLinkService = applicationLinkService;
    }

    @EventListener
    public void onPullRequestOpen(PullRequestOpenedEvent event) {
        createAndLaunchPlanBranch(event);
    }

    @EventListener
    public void onPullRequestReopen(PullRequestReopenedEvent event) {
        createAndLaunchPlanBranch(event);
    }

    @EventListener
    public void onPullRequestMerged(PullRequestMergedEvent event) {
        disablePlanBranch(event);
        deletePlanBranch(event);
    }

    @EventListener
    public void onPullRequestDeclined(PullRequestDeclinedEvent event) {
        disablePlanBranch(event);
    }

    private void createAndLaunchPlanBranch(PullRequestEvent event) {
        PullRequest pullrequest = event.getPullRequest();
        // enabling tries to enable an existing branch for this pull request; it fails if no plan branch exists
        if (!enablePlanBranch(event)) {
            AoRepositoryPlan plan = findRepositoryPlanForPullRequest(pullrequest);
            if (plan != null) {
                String planKey = createPlanBranchForBranch(plan.getBambooInstanceId(), plan.getPlanKey(),
                        pullrequest.getFromRef());
                pullrequestPlanDao.create(pullrequest.getId(), plan.getBambooInstanceId(), planKey);
            }
        }
        triggerBuildForPlanBranch(event);
    }

    private Optional<ApplicationLink> getBambooInstanceForPlan(String instanceId) {
        try {
            return Optional.ofNullable(applicationLinkService.getApplicationLink(new ApplicationId(instanceId)));
        } catch (TypeNotInstalledException e) {
            LOGGER.warn("Unable to find application link for bamboo instance id " + instanceId, e);
            return Optional.empty();
        }
    }

    private AoRepositoryPlan findRepositoryPlanForPullRequest(PullRequest pullrequest) {
        Repository repository = pullrequest.getFromRef().getRepository();
        return repositoryPlanDao.findByRepositoryId(repository.getId());
    }

    private AoPullRequestPlan findPullRequestPlan(PullRequest pullrequest) {
        return pullrequestPlanDao.findByPullRequestId(pullrequest.getId());
    }

    private String createPlanBranchForBranch(String instanceId, String plan, PullRequestRef branch) {
        String id = branch.getId();
        String branchName = extractBranchName(id);
        return executeBambooRequest(
                instanceId, 
                MethodType.PUT, 
                "/rest/api/latest/plan/" + plan + "/branch/" + branchName + "?vcsBranch=" + id + "&enabled=true", 
                response -> extractPlankey(response.getResponseBodyAsString()),
                null);
    }

    private String extractBranchName(String id) {
        return id.substring(id.lastIndexOf('/') + 1);
    }

    private boolean triggerBuildForPlanBranch(PullRequestEvent event) {
        AoPullRequestPlan plan = pullrequestPlanDao.findByPullRequestId(event.getPullRequest().getId());
        if (plan != null) {
            return executeBambooRequest(
                    plan.getBambooInstanceId(), 
                    MethodType.POST, 
                    "/rest/api/latest/queue/" + plan.getPlanKey(), 
                    response -> response.isSuccessful(), 
                    false);
        }
        return false;
    }

    private String extractPlankey(String response) {
        JsonParser parser = new JsonParser();
        JsonObject responseObject = parser.parse(response).getAsJsonObject();
        return responseObject.get("key").getAsString();
    }

    public boolean disablePlanBranch(PullRequestEvent event) {
        return setEnablementForPlanBranch(event.getPullRequest(), MethodType.DELETE);
    }

    public boolean enablePlanBranch(PullRequestEvent event) {
        return setEnablementForPlanBranch(event.getPullRequest(), MethodType.POST);
    }

    private boolean setEnablementForPlanBranch(PullRequest pullrequest, MethodType method) {
        AoPullRequestPlan plan = findPullRequestPlan(pullrequest);
        if (plan != null) {
            return executeBambooRequest(
                    plan.getBambooInstanceId(), 
                    method, 
                    "/rest/api/latest/plan/" + plan.getPlanKey() + "/enable", 
                    response -> response.isSuccessful(),
                    false);
        } else {
            return false;
        }
    }
    
    private void deletePlanBranch(PullRequestEvent event) {
        AoPullRequestPlan plan = findPullRequestPlan(event.getPullRequest());
        if (plan != null) {
            executeBambooRequest(
                    plan.getBambooInstanceId(), 
                    MethodType.POST, 
                    "/chain/admin/deleteChain!doDelete.action?os_authType=basic&buildKey=" + plan.getPlanKey(), 
                    response -> true, 
                    null);
        }
    }

    private <T> T executeBambooRequest(String bambooInstanceId, MethodType method, String url,
            ReturningResponseHandler<? super Response, T> responseHandler, T fallbackValue) {
        
        Optional<ApplicationLink> maybeBamboo = getBambooInstanceForPlan(bambooInstanceId);
        return maybeBamboo.map(bamboo -> {
            try {
                return bamboo
                        .createAuthenticatedRequestFactory()
                        .createRequest(method, url)
                        .addHeader("Accept", "application/json")
                        .executeAndReturn(responseHandler);
            } catch (ResponseException | CredentialsRequiredException e) {
                LOGGER.info(
                        "Unable to invoke method + " + method + " on url " + url + " of bamboo instance " + bamboo,  e);
                return fallbackValue;
            }
        }).orElse(fallbackValue);
    }
}

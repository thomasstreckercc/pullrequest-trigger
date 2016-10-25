package de.codecentric.bitbucket.plugin.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import de.codecentric.bitbucket.plugin.dao.AoRepositoryPlan;
import de.codecentric.bitbucket.plugin.dao.RepositoryPlanDao;
import de.codecentric.bitbucket.plugin.rest.RestRepositoryPlan;

@Component
public class PullRequestTriggerSettings extends HttpServlet {
    private static final long serialVersionUID = 7625541289550680531L;
    private static final Logger log = LoggerFactory.getLogger(PullRequestTriggerSettings.class);
    private static final String RESOURCE_KEY = "de.codecentric.bitbucket.plugin.pullrequest-trigger:pullrequest-trigger-page-templates";
    private static final String TEMPLATE_KEY = "de.codecentric.bitbucket.plugin.pullrequestTriggerSettings";
    private static final String SETTINGS_PAGE_CONTEXT = "pullrequest-trigger-settings";

    private static final String PROJECTS = "projects";
    private static final String REPOS = "repos";

    private final RepositoryService repositoryService;
    private final PageBuilderService pageBuilderService;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final RepositoryPlanDao repositoryPlanDao;

    @Autowired
    public PullRequestTriggerSettings(@ComponentImport final RepositoryService repositoryService,
            @ComponentImport final PageBuilderService pageBuilderService,
            @ComponentImport final SoyTemplateRenderer soyTemplateRenderer,
            final RepositoryPlanDao repositoryPlanDao) {
        this.repositoryService = repositoryService;
        this.pageBuilderService = pageBuilderService;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.repositoryPlanDao = repositoryPlanDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Repository repository = getRepository(req, resp);
        if (repository == null) {
            return;
        }
        render(resp, createViewContext(repository, restify(repositoryPlanDao.findByRepositoryId(repository.getId()))));
    }

//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Repository repository = getRepository(req, resp);
//        if (repository == null) {
//            return;
//        }
//
//        String bambooInstance = req.getParameter("bambooInstance");
//        String planKey = req.getParameter("plan");
//        render(resp, createViewContext(repository,
//                restify(repositoryPlanDao.update(repository.getId(), bambooInstance, planKey))));
//    }

    private RestRepositoryPlan restify(AoRepositoryPlan plan) {
        return plan == null ? null : new RestRepositoryPlan(plan);
    }

    private Repository getRepository(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        String[] pathParts = pathInfo.substring(1).split("/");
        if (!isRepoPath(pathParts)) {
            log.error("No repository path found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        String projectKey = pathParts[1];
        String repoSlug = pathParts[3];
        Repository repository = repositoryService.getBySlug(projectKey, repoSlug);
        if (repository == null) {
            log.error("Repository with key {} and slug {} does not exist", projectKey, repoSlug);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        return repository;
    }

    private void prepareResponse(HttpServletResponse response) {
        pageBuilderService.assembler().resources().requireContext(SETTINGS_PAGE_CONTEXT);
        response.setContentType("text/html;charset=UTF-8");
    }

    private void render(HttpServletResponse response, Map<String, Object> context)
            throws ServletException, IOException {
        prepareResponse(response);

        try {
            soyTemplateRenderer.render(response.getWriter(), RESOURCE_KEY, TEMPLATE_KEY, context);
        } catch (SoyException soyException) {
            handleSoyError(soyException);
        }
    }

    private void handleSoyError(SoyException e) throws IOException, ServletException {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            throw (IOException) cause;
        }
        throw new ServletException(e);
    }

    private Map<String, Object> createViewContext(Repository repository, RestRepositoryPlan plan) {
        ImmutableMap.Builder<String, Object> contextBuilder = ImmutableMap.builder();
        contextBuilder.put("repository", repository);
        if (plan != null) {
            contextBuilder.put("plan", plan);
        }
        return contextBuilder.build();
    }

    private boolean isRepoPath(String[] pathParts) {
        return pathParts.length > 3 && PROJECTS.equals(pathParts[0]) && REPOS.equals(pathParts[2]);
    }
}
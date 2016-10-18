package de.codecentric.bitbucket.plugin.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestReopenedEvent;
import com.atlassian.event.api.EventListener;

import de.codecentric.bitbucket.plugin.service.BambooService;

@Component
public class PullRequestEventListener {

    private final BambooService bambooService;

    @Autowired
    public PullRequestEventListener(BambooService bambooService) {
        this.bambooService = bambooService;
    }

    @EventListener
    public void onPullRequestOpen(PullRequestOpenedEvent event) {
        bambooService.createAndLaunchPlanBranch(event);
    }

    @EventListener
    public void onPullRequestReopen(PullRequestReopenedEvent event) {
        bambooService.createAndLaunchPlanBranch(event);
    }

    @EventListener
    public void onPullRequestMerged(PullRequestMergedEvent event) {
        bambooService.disablePlanBranch(event);
        bambooService.deletePlanBranch(event);
    }

    @EventListener
    public void onPullRequestDeclined(PullRequestDeclinedEvent event) {
        bambooService.disablePlanBranch(event);
    }

    
}

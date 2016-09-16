package de.codecentric.bitbucket.plugin.dao;

import javax.annotation.Nonnull;

public interface PullRequestPlanDao {

	@Nonnull
	AoPullRequestPlan create(long pullrequestId, String bambooInstanceId, String planKey);

	void delete(@Nonnull AoPullRequestPlan plan);

	void deleteForPullRequestId(long pullrequestId);

	AoPullRequestPlan findByPullRequestId(long pullrequestId);

	AoPullRequestPlan update(long pullrequestId, String newBambooInstanceId, String newPlanKey);

}

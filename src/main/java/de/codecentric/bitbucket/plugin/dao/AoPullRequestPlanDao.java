package de.codecentric.bitbucket.plugin.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bitbucket.ao.AbstractAoDao;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;

@Service
public class AoPullRequestPlanDao extends AbstractAoDao implements PullRequestPlanDao {

	@Autowired
	public AoPullRequestPlanDao(@ComponentImport ActiveObjects ao) {
		super(ao);
	}

	@Override
	public AoPullRequestPlan create(long pullrequestId, String bambooInstanceId, String planKey) {
		return ao.create(AoPullRequestPlan.class,
				ImmutableMap.<String, Object> builder().put(AoPullRequestPlan.PULLREQUEST_ID_COLUMN, pullrequestId)
						.put(AoPullRequestPlan.INSTANCE_ID_COLUMN, bambooInstanceId)
						.put(AoPullRequestPlan.PLAN_KEY_COLUMN, planKey).build());
	}

	@Override
	public void delete(AoPullRequestPlan plan) {
		ao.delete(plan);
	}

	@Override
	public void deleteForPullRequestId(long pullrequestId) {
		getByPullRequestId(pullrequestId).ifPresent(this::delete);
	}

	@Override
	public AoPullRequestPlan findByPullRequestId(long pullrequestId) {
		return getByPullRequestId(pullrequestId).orElse(null);
	}

	@Override
	public AoPullRequestPlan update(long pullrequestId, String newBambooInstanceId, String newPlanKey) {
		Optional<AoPullRequestPlan> maybeExisting = getByPullRequestId(pullrequestId);
		if (maybeExisting.isPresent()) {
			AoPullRequestPlan existing = maybeExisting.get();
			existing.setBambooInstanceId(newBambooInstanceId);
			existing.setPlanKey(newPlanKey);
			existing.save();
			return existing;
		} else {
			return create(pullrequestId, newBambooInstanceId, newPlanKey);
		}
	}

	private Optional<AoPullRequestPlan> getByPullRequestId(long pullrequestId) {
		return Optional.ofNullable(ao.get(AoPullRequestPlan.class, pullrequestId));
	}
}

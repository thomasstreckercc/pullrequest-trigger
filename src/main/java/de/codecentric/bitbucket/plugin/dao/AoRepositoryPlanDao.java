package de.codecentric.bitbucket.plugin.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bitbucket.ao.AbstractAoDao;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;

@Service
public class AoRepositoryPlanDao extends AbstractAoDao implements RepositoryPlanDao {

	@Autowired
	public AoRepositoryPlanDao(@ComponentImport ActiveObjects ao) {
		super(ao);
	}

	@Override
	public AoRepositoryPlan create(int repositoryId, String bambooInstanceId, String planKey) {
		return ao.create(AoRepositoryPlan.class,
				ImmutableMap.<String, Object> builder().put(AoRepositoryPlan.REPOSITORY_ID_COLUMN, repositoryId)
						.put(AoRepositoryPlan.INSTANCE_ID_COLUMN, bambooInstanceId)
						.put(AoRepositoryPlan.PLAN_KEY_COLUMN, planKey).build());
	}

	@Override
	public void delete(AoRepositoryPlan plan) {
		ao.delete(plan);
	}

	@Override
	public void deleteForRepositoryId(int repositoryId) {
		getByRepositoryId(repositoryId).ifPresent(this::delete);
	}

	@Override
	public AoRepositoryPlan findByRepositoryId(int repositoryId) {
		return getByRepositoryId(repositoryId).orElse(null);
	}

	@Override
	public AoRepositoryPlan update(int repositoryId, String newBambooInstanceId, String newPlanKey) {
		Optional<AoRepositoryPlan> maybeExisting = getByRepositoryId(repositoryId);
		if (maybeExisting.isPresent()) {
			AoRepositoryPlan existing = maybeExisting.get();
			existing.setBambooInstanceId(newBambooInstanceId);
			existing.setPlanKey(newPlanKey);
			existing.save();
			return existing;
		} else {
			return create(repositoryId, newBambooInstanceId, newPlanKey);
		}
	}

	private Optional<AoRepositoryPlan> getByRepositoryId(int repositoryId) {
		return Optional.ofNullable(ao.get(AoRepositoryPlan.class, repositoryId));
	}
}

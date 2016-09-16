package de.codecentric.bitbucket.plugin.dao;

import javax.annotation.Nonnull;

public interface RepositoryPlanDao {

	@Nonnull
	AoRepositoryPlan create(int repositoryId, String bambooInstanceId, String planKey);

	void delete(@Nonnull AoRepositoryPlan plan);

	void deleteForRepositoryId(int repositoryId);

	AoRepositoryPlan findByRepositoryId(int repositoryId);

	AoRepositoryPlan update(int repositoryId, String newBambooInstanceId, String newPlanKey);
}

package de.codecentric.bitbucket.plugin.dao;

import net.java.ao.Accessor;
import net.java.ao.Mutator;
import net.java.ao.Preload;
import net.java.ao.RawEntity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("REPO_PLAN")
@Preload
public interface AoRepositoryPlan extends RawEntity<Integer> {
	
	String REPOSITORY_ID_COLUMN = "REPOSITORY_ID";
	String INSTANCE_ID_COLUMN = "INSTANCE_ID";
	String PLAN_KEY_COLUMN = "PLAN_KEY";

    @NotNull
    @PrimaryKey(REPOSITORY_ID_COLUMN)
    Integer getRepositoryId();
    
    @Mutator(REPOSITORY_ID_COLUMN)
    void setRepositoryId(Integer repositoryId);
    
    @NotNull
    @Accessor(INSTANCE_ID_COLUMN)
    @StringLength(36)
    String getBambooInstanceId();

    @Mutator(INSTANCE_ID_COLUMN)
    void setBambooInstanceId(String instanceId);

    @NotNull
    @Accessor(PLAN_KEY_COLUMN)
    @StringLength(255)
    String getPlanKey();

    @Mutator(PLAN_KEY_COLUMN)
    void setPlanKey(String planKey);
}

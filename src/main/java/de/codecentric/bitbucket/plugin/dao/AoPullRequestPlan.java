package de.codecentric.bitbucket.plugin.dao;

import net.java.ao.Accessor;
import net.java.ao.Mutator;
import net.java.ao.Preload;
import net.java.ao.RawEntity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("PR_PLAN")
@Preload
public interface AoPullRequestPlan extends RawEntity<Long> {
	String PULLREQUEST_ID_COLUMN = "PR_ID";
	String INSTANCE_ID_COLUMN = "INSTANCE_ID";
	String PLAN_KEY_COLUMN = "PLAN_KEY";

    @NotNull
    @PrimaryKey(PULLREQUEST_ID_COLUMN)
    Long getPullRequestId();
    
    @Mutator(PULLREQUEST_ID_COLUMN)
    void setPullRequestId(Long pullrequestId);
    
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

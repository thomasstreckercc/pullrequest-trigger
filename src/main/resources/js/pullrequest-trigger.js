define('pullrequest-trigger/module', [
    'aui',
    'jquery',
    'lodash',
    'bitbucket/util/navbuilder',
    'bitbucket/internal/util/ajax',
    'exports'
], function (
	AJS,
    $,
    _,
    nav,
    ajax,
    exports
) {
	var selectedInstanceId;
	var selectedPlanKey;
	
	function handleNewBambooInstanceSelection(event) {
		var instanceId = event.target.value;
		loadPlansForInstance(instanceId);
	}
	
	function loadPlansForInstance(instanceId) {
		$('#plan optgroup').remove();
		$('#plan').prop('disabled', true);
		if (instanceId) {
			AJS.$('#plan-spinner').spin();
			ajax
			  .ajax({
	            url: getPlansUrl(instanceId),
	            type: 'GET'})
	          .done(onPlansSuccess)
	          .always(function() {
	        	  AJS.$('#plan-spinner').spinStop();
	        	  $('#plan').prop('disabled', false);
	          });
		}
	}
	
	function loadInstances() {
		$('#bambooInstance').prop('disabled', true);
		$('#plan').prop('disabled', true);
		AJS.$('#instance-spinner').spin();
		ajax
		  .ajax({
          url: getBambooInstancesUrl(),
          type: 'GET'})
        .done(onInstancesSuccess)
        .always(function() {
        	AJS.$('#instance-spinner').spinStop();
        });
	}
	
	function initBambooSelect() {
		loadInstances();
		$('#bambooInstance').on('change', handleNewBambooInstanceSelection);
	}
	
	function getPlansUrl(instanceId) {
		var urlBuilder = nav
			.rest('pullrequest-trigger', '1.0')
			.addPathComponents('plans')
			.withParams({bambooInstance: instanceId});
		return urlBuilder.build();
	}
	
	function getBambooInstancesUrl() {
		var urlBuilder = nav
			.rest('pullrequest-trigger', '1.0')
			.addPathComponents('bambooInstances');
		return urlBuilder.build();
	}
	
	function onInstancesSuccess(data) {
		if (!(data && Array.isArray(data) && data.length > 0)) {
			AJS.messages.error({
				title: 'Unable to find any linked Bamboo instances',
				body: '<p>Please <a href="' + nav.pluginServlets().path('applinks', 'listApplicationLinks').build() + '">add links to Bamboo instances</a> to your installation or have an administrator add them. </p>',
				closeable: false
			});
		} else {
			_.forEach(data, addInstanceToSelection);
			if (selectedInstanceId) {
				$('#bambooInstance').val(selectedInstanceId);
			}
			$('#bambooInstance').prop('disabled', false);
			if (selectedInstanceId) {
				loadPlansForInstance(selectedInstanceId);
			}
		}
	}
	
	function addInstanceToSelection(bambooInstance) {
		$('<option/>').val(bambooInstance.id).text(bambooInstance.name).appendTo('#bambooInstance');
	}
	
	function onPlansSuccess(data) {
		_.forEach(data, addProjectToSelection);
		if(selectedPlanKey) {
		  $('#plan').val(selectedPlanKey);
		}
	}
	
	function addProjectToSelection(project) {
		var group = $('<optgroup/>').attr('label', project.name).appendTo('#plan');
		addPlansToProjectGroup(group, project.plans);
	}
	
	function addPlansToProjectGroup(group, plans) {
		_.forEach(plans, function (plan) {
			$('<option/>').val(plan.key).text(plan.name).appendTo(group);
		});
	}
	
	function initBambooCancel() {
		$('#cancel-button').attr('href', nav.currentRepo().browse());
	}
	
    exports.onReady = function(instanceId, planKey) {
		selectedInstanceId = instanceId;
		selectedPlanKey = planKey;
    	initBambooSelect();
    	initBambooCancel();
    };
});
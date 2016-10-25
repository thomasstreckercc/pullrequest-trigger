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
	'use strict';
	var selectedRepositoryId;
	var selectedInstanceId;
	var selectedPlanKey;

	function initBambooSelect() {
		loadInstances();
		$('#bambooInstance').on('change', handleNewBambooInstanceSelection);
	}

	function initPlanSelect() {
		disableSaveButtonIfNecessary(!selectedPlanKey);
		$('#plan').on('change', handleNewPlanSelection);
	}

	function initSave() {
		$('#save-button').on('click', saveConfiguration);
	}

	function initDelete() {
		$('#delete-button').on('click', deleteConfiguration);
	}

	function deleteConfiguration(event) {
		event.preventDefault();
		event.stopPropagation();
		ajax
		 	.ajax({
		 		url: getDeleteConfigurationUrl(selectedRepositoryId),
		 		type: 'DELETE'})
        	.always(function() {
        		window.location.reload(true);
        	});
	}

	function saveConfiguration(event) {
		event.preventDefault();
		event.stopPropagation();
		if (selectedInstanceId && selectedPlanKey) {
  		ajax
  		 	.ajax({
  		 		url: getSaveConfigurationUrl(selectedRepositoryId, selectedInstanceId, selectedPlanKey),
  		 		type: 'GET'})
          	.always(function() {
          		window.location.reload(true);
          	});
		}
	}

	function handleNewBambooInstanceSelection(event) {
		selectedInstanceId = event.target.value;
		loadPlansForInstance(selectedInstanceId);
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
	          });
		} else {
			disableSaveButtonIfNecessary(true);
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
	
	function disableSaveButtonIfNecessary(disabled) {
		$('#save-button').attr('disabled', disabled);
	}
	
	function handleNewPlanSelection(event) {
		selectedPlanKey = event.target.value;
		disableSaveButtonIfNecessary(!selectedPlanKey);
	}
	
	function getDeleteConfigurationUrl(repositoryId) {
		var urlBuilder = nav
			.rest('pullrequest-trigger', '1.0')
			.addPathComponents('configuration', repositoryId);
		return urlBuilder.build();
	}
	
	function getSaveConfigurationUrl(repositoryId, instanceId, planKey) {
		var urlBuilder = nav
			.rest('pullrequest-trigger', '1.0')
			.addPathComponents('configuration', repositoryId)
			.withParams({instanceId: instanceId, planKey: planKey});
		return urlBuilder.build();
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
		if (!(data && Array.isArray(data) && data.length)) {
			AJS.messages.error({
				title: 'Unable to find any linked Bamboo instances',
				body: '<p>Please <a href="' + nav.pluginServlets().path('applinks', 'listApplicationLinks').build() + '">add links to Bamboo instances</a> to your installation or have an administrator add them.</p>',
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
		if (!(data && Array.isArray(data) && data.length)) {
			AJS.messages.error({
				title: 'Unable to find any plans in the selected Bamboo instance',
				body: '<p>Please define some build plans for this Bamboo instance which could be triggered for pull requests</p>',
				closeable: false
			});
		} else {
			_.forEach(data, addProjectToSelection);
			var plan = $('#plan');
			plan.prop('disabled', false);
			if(selectedPlanKey) {
				plan.val(selectedPlanKey);
			}
			disableSaveButtonIfNecessary(!plan.val());
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
	
  exports.onReady = function(repositoryId, instanceId, planKey) {
		selectedRepositoryId = repositoryId;
    selectedInstanceId = instanceId;
		selectedPlanKey = planKey;
		
    initBambooSelect();
    initPlanSelect();
    initSave();
    initDelete();
  };
});
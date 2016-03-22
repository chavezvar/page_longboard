'use strict';
/**
 * 
 */

(function() {


var appCommand = angular.module('longboardmonitor', ['googlechart', 'ui.bootstrap']);


// appCommand.config();
$('#waitanswer').hide();

// Constant used to specify resource base path (facilitates integration into a Bonita custom page)
appCommand.constant('RESOURCE_PATH', 'pageResource?page=custompage_longboard&location=');


 
	   
// --------------------------------------------------------------------------
//
// Controler DashboardMonitorController
//
// --------------------------------------------------------------------------
	   
// User app list controller
appCommand.controller('DashboardMonitorController', 
	function () {
	
	this.messageList='';
	
		
	var myProcessList = [ {
	   Name : 'processName',
	   Version : '1.0',
	   } ];
	    
	
	
	this.listprocess = function()
	{
	  // retrieve the list of process
	};
		
});

	   
// --------------------------------------------------------------------------
//
// Controler MainControler
//
// --------------------------------------------------------------------------
	
appCommand.controller('MainController', 
	function () {
	
	this.isshowhistory = false;
	
	this.showhistory = function( show )
	{
	   this.isshowhistory = show;
	}

	
		
});
	
// --------------------------------------------------------------------------
//
// Controler ShowHistoryController
//
// --------------------------------------------------------------------------
	
appCommand.controller('ShowHistoryController',
	function ($scope, $http) {
		this.caseid;
		this.showSubProcess = false;
		this.msg="";
		this.myActivityHistory = [];
		this.synthesis = [];
		this.TimerListEvents =[];
		


		// alert('init getActivity '+myActivityHistory+'');
		$('#showhistorybtn').show();
		$('#showhistorywait').hide();

								
		this.showcasehistory = function()
		{
			var self=this;	
			
			$('#showhistorybtn').hide();
			$('#showhistorywait').show();
			
			var url='?page=custompage_longboard&action=casehistory&caseid='+this.caseid+"&showSubProcess="+this.showSubProcess;
								
			$http.get( url )
				.success( function ( jsonResult ) {								
								console.log("history",jsonResult);
								self.caseState 					= jsonResult.casestate;
								self.startdate					= jsonResult.startdate;
								self.enddate					= jsonResult.enddate;
								self.stringindex				= jsonResult.stringindex;
								self.processdefinition			= jsonResult.processdefinition
								self.synthesis			        = jsonResult.synthesis;
								self.TimerListEvents            = jsonResult.TimerListEvents;
								self.myActivityHistory 			= jsonResult.Activities;
								self.errormessage 				= jsonResult.errormessage; 								
								
								$scope.chartTimeline		 	= JSON.parse(jsonResult.chartTimeline);
								
								$('#showhistorybtn').show();
								$('#showhistorywait').hide();
								
								console.log("Chart=>>",jsonResult.chartTimeline);
								
							}
						)
				.error( function ( result ) {
								alert('error on showHistory ');
								var jsonResult = JSON.parse(result);
								self.myActivityHistory=jsonResult;
								$('#showhistorybtn').show();
								$('#showhistorywait').hide();

								}
				);			
		};
		this.cancelCase = function()
		{
			alert('cancel case');
			var self=this;			
			var url='?page=custompage_longboard&action=cancelcase&caseid='+this.caseid;
								
			$http.get( url )
				.success( function ( jsonResult ) {
					alert('Case canceled');				
					this.showcasehistory();
					}	
				)
				.error( function ( result ) {
					alert('error on caseCancel ');
					var jsonResult = JSON.parse(result);
					self.myActivityHistory=jsonResult;
					}
				);
		};
		
	} );
	
	
	
// --------------------------------------------------------------------------
//
// Controler MonitoringController
//
// --------------------------------------------------------------------------

appCommand.controller('MonitoringController', 
	function ($scope,$http) {
		$('#collectwait').hide();
		this.AvailableProcessor=0;
		this.JvmName="";
		this.MemUsage=0;
		this.MemFree=0;
		this.MemFreeSwap=0;
		this.JvmSystemProperties=0;
		this.JvmVendor=0;
		this.JvmVersion=0;
		this.LastGCInfo=0;
		this.MemUsagePercentage=0;
		this.NumberActiveTransaction=0;
		this.OSArch=0;
		this.OSName=0;
		this.OSVersion=0;
		this.ProcessCPUTime=0;
		this.StartTimeHuman=0;
		this.LoadAverageLastMn=0;
		this.ThreadCount=0;
		this.MemTotalPhysicalMemory=0;
		this.MemTotalSwapSpace=0;
		this.TotalThreadsCpuTime=0;
		this.UpTime=0;
		this.IsSchedulerStarted=0;
		this.CommitedVirtualMemorySize=0;
		this.DatabaseMajorVersion="";
		this.DatabaseMinorVersion="";
		this.DatabaseProductName="";
		this.DatabaseProductVersion="";
		this.errormessage="";
		this.JVMArgs = "";
		
		this.isSowJvmArgs=false;
		this.showJvmArgs = function( show ) 
		{
			this.isSowJvmArgs = show;
		}
		
		this.isshowjvmsystemproperties = false;
		this.showjvmsystemproperties = function( show ) 
		{
			this.isshowjvmsystemproperties = show;
		}
		
		
		this.refresh = function()
		{		    
			// alert('current availableprocess '+this.AvailableProcessor);
			
			$('#collectwait').show();			
			$('#collectbtn').hide();
			var self = this;
	
			$http.get( '?page=custompage_longboard&action=monitoringapi')
			 .success(function success(jsonResult) {	

								console.log('receive ',jsonResult);
								self.AvailableProcessor = jsonResult.AvailableProcessor;
								self.JvmName					= jsonResult.JvmName;
								self.MemUsage					= jsonResult.MemUsage;
								self.MemFree					= jsonResult.MemFree;
								self.MemFreeSwap				= jsonResult.MemFreeSwap;
								self.JvmSystemProperties		= jsonResult.JvmSystemProperties;
								self.JvmVendor					= jsonResult.JvmVendor;
								self.JvmVersion					= jsonResult.JvmVersion;
								self.LastGCInfo					= jsonResult.LastGCInfo;
								self.MemUsagePercentage			= jsonResult.MemUsagePercentage;
								self.NumberActiveTransaction	= jsonResult.NumberActiveTransaction;
								self.OSArch						= jsonResult.OSArch;
								self.OSName						= jsonResult.OSName;								
								self.OSVersion					= jsonResult.OSVersion;
								self.ProcessCPUTime				= jsonResult.ProcessCPUTime;
								self.StartTimeHuman				= jsonResult.StartTimeHuman;
								self.LoadAverageLastMn			= jsonResult.LoadAverageLastMn;
								self.ThreadCount				= jsonResult.ThreadCount;
								self.MemTotalPhysicalMemory		= jsonResult.MemTotalPhysicalMemory;
								self.MemTotalSwapSpace			= jsonResult.MemTotalSwapSpace;
								self.JavaFreeMemory 			= jsonResult.JavaFreeMemory;
								self.JavaTotalMemory 			= jsonResult.JavaTotalMemory;
								self.JavaUsedMemory 			= jsonResult.JavaUsedMemory;
								self.TotalThreadsCpuTime		= jsonResult.TotalThreadsCpuTime;
								self.UpTime						= jsonResult.UpTime;
								self.IsSchedulerStarted			= jsonResult.IsSchedulerStarted;
								self.CommitedVirtualMemorySize	= jsonResult.CommitedVirtualMemorySize;
								self.DatabaseMajorVersion		= jsonResult.DatabaseMajorVersion;
								self.DatabaseMinorVersion		= jsonResult.DatabaseMinorVersion;
								self.DatabaseProductName		= jsonResult.DatabaseProductName;
								self.DatabaseProductVersion		= jsonResult.DatabaseProductVersion;
								self.errormessage 				= jsonResult.errormessage;
								self.JVMArgs					= jsonResult.JVMArgs;
								
								$('#collectwait').hide();
								$('#collectbtn').show();
								}
						)
			.error( function ( result ) {
								$('#collectwait').hide();
								$('#collectbtn').show();
								alert('error on monitoring');								
								}
					);
							
		
		};
		this.refresh();
	} );
	
	
// --------------------------------------------------------------------------
//
// Controler PerformanceController
//
// --------------------------------------------------------------------------
			
appCommand.controller('PerformanceController', 
	function ($scope,$http) {
		$('#performancemesurewait').hide();
		
		$scope.BBonitaHomeWriteBASE=0;
		this.runprocesstest = false;
		this.BonitaHomeWriteMS=0;
		this.BonitaHomeWriteFACTOR =0;
		
		this.BonitaHomeReadBASE=0;
		this.BonitaHomeReadMS=0;
		this.BonitaHomeReadFACTOR=0;
		
		this.DatabaseBASE =0;
		this.DatabaseMS =0;
		this.DatabaseFACTOR =0;
		
		this.ProcessDeployBASE =0;
		this.ProcessDeployMS =0;
		this.ProcessDeployFACTOR =0;
		
		this.runbonitahometest=true;
		this.rundatabasetest=true;
		this.runprocesstest=true;
		this.runprocesstestnumber=100;
	
		this.runtest = function()
		{		    
			$('#performancemesurewait').show();
			$('#performancemesurebtn').hide();

			var self = this;
			var url ='?page=custompage_longboard&action=testperf';
			url = url + '&runbonitahometest='+this.runbonitahometest;
			url = url + '&rundatabasetest='+this.rundatabasetest;
			url = url + '&runprocesstest='+this.runprocesstest;
			url = url + '&runprocesstestnumber='+this.runprocesstestnumber;
		
			$http.get( url )
			  .success(function success(jsonResult) {	
				console.log('receive ',jsonResult);
								
				self.BonitaHomeWriteBASE			= jsonResult.BonitaHomeWriteBASE;
				self.BonitaHomeWriteMS				= jsonResult.BonitaHomeWriteMS;
				self.BonitaHomeWriteFACTOR			= jsonResult.BonitaHomeWriteFACTOR;
				
				self.BonitaHomeReadBASE				= jsonResult.BonitaHomeReadBASE;
				self.BonitaHomeReadMS				= jsonResult.BonitaHomeReadMS;
				self.BonitaHomeReadFACTOR			= jsonResult.BonitaHomeReadFACTOR;
					
				self.DatabaseBASE					= jsonResult.DatabaseBASE;
				self.DatabaseMS						= jsonResult.DatabaseMS,
				self.DatabaseFACTOR					= jsonResult.DatabaseFACTOR;				
				
				self.DataMetaBASE					= jsonResult.DataMetaBASE;
				self.DataMetaMS						= jsonResult.DataMetaMS,
				self.DataMetaFACTOR					= jsonResult.DataMetaFACTOR;
				
				self.ProcessDeployBASE				= jsonResult.ProcessDeployBASE;
				self.ProcessDeployMS				= jsonResult.ProcessDeployMS,
				self.ProcessDeployFACTOR			= jsonResult.ProcessDeployFACTOR;
			
				self.ProcessCreateBASE				= jsonResult.ProcessCreateBASE;
				self.ProcessCreateMS				= jsonResult.ProcessCreateMS,
				self.ProcessCreateFACTOR			= jsonResult.ProcessCreateFACTOR;
			
				self.ProcessRunBASE					= jsonResult.ProcessRunBASE;
				self.ProcessRunMS					= jsonResult.ProcessRunMS,
				self.ProcessRunFACTOR				= jsonResult.ProcessRunFACTOR;

				self.errormessage 					= jsonResult.errormessage;

				$scope.chartObject 					= JSON.parse(jsonResult.chartObject);
				console.log("Chart=>>",jsonResult.chartObject);
				$('#performancemesurewait').hide();
				$('#performancemesurebtn').show();
			
				
			  })
			  .error( function error( result ) {
								$('#performancemesurewait').hide();
								$('#performancemesurebtn').show();
								
								alert('error on testperf');								
								}
			);
							
		};
	} );
	
	
// --------------------------------------------------------------------------
//
// Controler Connector
//
// --------------------------------------------------------------------------
			
appCommand.controller('TimeTrackerController', 
	function ($scope,$http) {
		this.startedstate=false;
		this.info="";
		this.msg="";
		this.startedmsg="";
		this.errormessage="";
		this.info="";
		this.isshowexplanation=false;
		this.allrecords = [];
		this.issimulation=false;
		this.showallinformations=false;
		this.rangedisplayinhour=10;
		this.showdetaildescriptionallinformations=false;
		this.showdetaildescriptiontop10=false;
		$('#refreshTimeTracker').show();
		$('#collectTimeTrackerwait').hide();

		
		this.isstarted = function()
		{
			return this.startedstate;
		}
		
		this.showexplanation = function( show )
		{		
			this.isshowexplanation = show;
		}
			
		
		this.runService= function( start )
		{	
			// alert('runService '+start);
			var self = this;
			$http.get( '?page=custompage_longboard&action=timetrackerservice&start='+start)
			  .success(function success(jsonResult) {	
				console.log('receive ',jsonResult);
				self.startedstate = jsonResult.isregistered;
				self.startedmsg = jsonResult.startedmsg;
				self.errormessage = jsonResult.errormessage;
				self.msg = jsonResult.msg;
				})
			  .error( function error( result ) {
					self.errormessage = 'error during start';								
				}
			);
		}
		
		this.refresh = function()
		{	
			$('#refreshTimeTracker').hide();
			$('#collectTimeTrackerwait').show();
		
			var self = this;
			$http.get( '?page=custompage_longboard&action=timetrackergetinfos&issimulation='+self.issimulation+'&showallinformations='+this.showallinformations+'&rangedisplayinhour='+this.rangedisplayinhour)
			  .success(function success(jsonResult) {	
				$('#refreshTimeTracker').show();
				$('#collectTimeTrackerwait').hide();
				// alert('TimeTracker Success '+jsonResult.startedmsg  );
				console.log('receive ',jsonResult);
				self.info = jsonResult.info;
				self.startedstate = jsonResult.isregistered;
				self.startedmsg = jsonResult.startedmsg;
				self.errormessage = jsonResult.errormessage;
				self.allrecords = jsonResult.allinformations;
				self.rangeinformations 	= jsonResult.rangeinformations;
				self.top10informations	= jsonResult.top10informations;
				$scope.chartRange 			= JSON.parse(jsonResult.chartRange);
				$scope.chartRepartitionConnector		= JSON.parse(jsonResult.chartRepartitionConnector);
				$scope.chartRepartitionWork			= JSON.parse(jsonResult.chartRepartitionWork);
				})
			  .error( function error( result ) {
				$('#refreshTimeTracker').show();
				$('#collectTimeTrackerwait').hide();
				self.errormessage = 'error during getInfo';		
				alert('Error on TimeTracker');				
				}
			);  
   
		}
		
		this.getstate  = function ()
		{
			var self = this;
			$http.get( '?page=custompage_longboard&action=timetrackerservicestate')
			  .success(function success(jsonResult) {	

				console.log('receive ',jsonResult);
				self.startedstate = jsonResult.isregistered;
				self.startedmsg = jsonResult.startedmsg;
				self.errormessage = jsonResult.errormessage;
				})
			  .error( function error( result ) {
				self.errormessage = 'error during getState';		
				
				}
			);  
			};
		this.getstate();
	});
	

// --------------------------------------------------------------------------
//
// Controler ServerParamController
//
// --------------------------------------------------------------------------
			
appCommand.controller('ServerParamController', 
	function ($scope,$http) {
		this.CustompageDebug=false;
		this.PersistencehibernateEnableWordSearch='';
		$('#collectserverparambtn').show();
		$('#collectserverparamwait').hide();

	
		this.refresh = function()
		{	
			$('#collectserverparambtn').hide();
			$('#collectserverparamwait').show();

			var self = this;
			$http.get( '?page=custompage_longboard&action=serverparams')
			  .success(function success(jsonResult) {	
				console.log('receive ',jsonResult);
				self.CustompageDebug 						= jsonResult.CustompageDebug;
				self.errormessage 							= jsonResult.errormessage;
				
				self.PersistencehibernateEnableWordSearch = jsonResult.PersistencehibernateEnableWordSearch;
				$('#collectserverparambtn').show();
				$('#collectserverparamwait').hide();
				})
			  .error( function error( result ) {
				self.errormessage = 'error during getInfo';								
				$('#collectserverparambtn').show();
				$('#collectserverparamwait').hide();
				}
			);  
   
		}
		this.refresh();
		
	});
		
	
	
// --------------------------------------------------------------------------
//
// Controler MonitorProcessController
//
// --------------------------------------------------------------------------
			
appCommand.controller('MonitorProcessController', 
	function ($scope,$http) {
		$('#collectProcessesbtn').show();
		$('#collectProcessesWait').hide();
		this.alldetails=true;
		this.processes = [ ];
		this.isshowlegend=false;
		this.defaultWarningNbOverflowTasks=20;
		this.defaultWarningNearbyTasks=50;
		this.defaultWarningNbTasks=0;
		this.activityPeriodInMn=120;
		this.defaultmaxitems=1000;
		
		this.showlegend = function( show )
		{
			this.isshowlegend = show;
		};
		this.refresh = function()
		{	
		
			$('#collectProcessesbtn').hide();
			$('#collectProcessesWait').show();

			var postMsg = {
					defaultWarningNearbyTasks: this.defaultWarningNearbyTasks,
					defaultWarningNbOverflowTasks: this.defaultWarningNbOverflowTasks,
					defaultWarningNbTasks: this.defaultWarningNbTasks,
					activityPeriodInMn: this.activityPeriodInMn,
					defaultmaxitems: this.defaultmaxitems,
					studypastactivities: this.studypastactivities,
				};
			
			
			
			var self = this;
			$http.get( '?page=custompage_longboard&action=monitoringprocess&paramjson='+ angular.toJson(postMsg, true))
			  .success(function success(jsonResult) {	
				console.log('receive ',jsonResult);
				self.processes 						= jsonResult.processes;
				self.errormessage 					= jsonResult.errormessage;

				console.log(self.processes);
				$('#collectProcessesbtn').show();
				$('#collectProcessesWait').hide();
				})
			  .error( function error( result ) {
				self.errormessage = 'error during getInfo';								
				$('#collectProcessesbtn').show();
				$('#collectProcessesWait').hide();
				}
			);  
   
		};
		
		
		
	});
		
	
		
	
	
})();
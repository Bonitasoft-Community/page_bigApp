'use strict';
/**
 *
 */

(function() {


var appCommand = angular.module('bigapp', ['ui.bootstrap','ngSanitize']);






// --------------------------------------------------------------------------
//
// Controler BigApp
//
// --------------------------------------------------------------------------


appCommand.controller('BigAppControler',
	function ( $http, $scope, $sce ) {
	this.isshowhistory=false;
	this.pingdate='';
	this.pinginfo='';
	this.inprogress=false;

	
	this.showhistory = function( show )
	{
	   this.isshowhistory = show;
	}

	this.navbaractiv='environment';
	
	this.getNavClass = function( tabtodisplay )
	{
		if (this.navbaractiv === tabtodisplay)
		 return 'ng-isolate-scope active';
		return 'ng-isolate-scope';
	}

	// ------------------------------------------------------------------------------
    //    Environment
    // ------------------------------------------------------------------------------

    this.environment = {};
    this.getEnvironment= function() {

        		var self=this;
        		self.inprogress=true;

        		$http.get( '?page=custompage_bigapp&action=getEnvironment&t='+Date.now() )
        				.success( function ( jsonResult, statusHttp, headers, config ) {
        					// connection is lost ?
        					if (statusHttp==401 || typeof jsonResult === 'string') {
        						console.log("Redirected to the login page !");
        						window.location.reload();
        					}

        						console.log("environment",jsonResult);
        						self.environment 		= jsonResult;
        						self.inprogress			= false;
        					})
        				.error( function() {
        						self.inprogress			= false;
        					});
    }
    // Run the initialization environment when the page is displayed
    this.getEnvironment();


    	// ------------------------------------------------------------------------------
        //    Logs
        // ------------------------------------------------------------------------------
        this.navbaractiv='logs';
        this.logs = {};
        this.getLogs= function() {

            		var self=this;
            		self.inprogress=true;

            		$http.get( '?page=custompage_bigapp&action=getLogs&t='+Date.now() )
            				.success( function ( jsonResult, statusHttp, headers, config ) {
            					// connection is lost ?
            					if (statusHttp==401 || typeof jsonResult === 'string') {
            						console.log("Redirected to the login page !");
            						window.location.reload();
            					}

            						console.log("logs",jsonResult);
            						self.logs 		= jsonResult;
            						self.inprogress			= false;
            					})
            				.error( function() {
            						self.inprogress			= false;
            					});
        }
        // Run the initialization Logs when the page is displayed
        this.getLogs();

	// ------------------------------------------------------------------------------
	//    Timer
	// ------------------------------------------------------------------------------
	this.createmissingtimers = function( typeCreation)
	{
		var self=this;
		self.inprogress=true;
		
		$http.get( '?page=custompage_bigapp&action=createmissingtimers&typecreation='+typeCreation+'&t='+Date.now() )
				.success( function ( jsonResult, statusHttp, headers, config ) {
					// connection is lost ?
					if (statusHttp==401 || typeof jsonResult === 'string') {
						console.log("Redirected to the login page !");
						window.location.reload();
					}

						console.log("history",jsonResult);
						self.listtimers 		= jsonResult.listtimers;
						self.missingtimerstatus	= jsonResult.missingtimerstatus;
						self.timererror 		= jsonResult.timererror;
						self.inprogress			= false;
					})
				.error( function() {
					
						self.timerstatus 		= jsonResult.timerstatus;
						self.timererror 		= jsonResult.timererror;
						self.inprogress			= false;
					});
				
	}; 
	
	this.getmissingtimer = function()
	{
		
		var self=this;
		self.inprogress=true;

		$http.get( '?page=custompage_bigapp&action=getmissingtimer'+'&t='+Date.now() )
				.success( function ( jsonResult, statusHttp, headers, config ) {
					// connection is lost ?
					if (statusHttp==401 || typeof jsonResult === 'string') {
						console.log("Redirected to the login page !");
						window.location.reload();
					}

						console.log("history",jsonResult);
						self.listtimers 		= jsonResult.listtimers;
						self.timerstatus 		= jsonResult.timerstatus;
						self.timererror 		= jsonResult.timererror;
								
						self.inprogress=false;

				})
				.error( function() {
					alert('an error occured');
						self.timererror 		= jsonResult.timererror;
						self.inprogress=false;
					});
				
	}; // end getmissingtimer

	
	this.deletetimers = function()
	{
		
		var self=this;
		self.inprogress=true;

		$http.get( '?page=custompage_bigapp&action=deletetimers'+'&t='+Date.now() )
				.success( function ( jsonResult, statusHttp, headers, config ) {
					// connection is lost ?
					if (statusHttp==401 || typeof jsonResult === 'string') {
						console.log("Redirected to the login page !");
						window.location.reload();
					}

						console.log("history",jsonResult);
						self.listtimers 		= jsonResult.listtimers;
						self.timerstatus 		= jsonResult.timerstatus;
						self.timererror 		= jsonResult.timererror;
								
						self.inprogress=false;
				})
				.error( function() {

						self.timerstatus 		= jsonResult.timerstatus;
						self.timererror 		= jsonResult.timererror;
						self.inprogress=false;
					});
				
	}; // end deletetimer

	
	// ------------------------------------------------------------------------------
	//    Groovy
	// ------------------------------------------------------------------------------

	this.groovy = { "type": '', "code":"", "src": 'return "Hello Word";' };
	this.listUrlCall=[];
	this.groovyLoad = function() 
	{
		var self=this;
		self.inprogress	=true;
		self.groovy.result="";
		self.groovy.type			= 'code';
		self.groovy.listevents=""
		self.groovy.result="";
		self.groovy.exception="";
		
		$http.get( '?page=custompage_bigapp&action=groovyload&code='+ this.groovy.code+'&t='+Date.now() )
		.success( function ( jsonResult, statusHttp, headers, config ) {
			// connection is lost ?
			if (statusHttp==401 || typeof jsonResult === 'string') {
				console.log("Redirected to the login page !");
				window.location.reload();
			}

				console.log("history",jsonResult);
				self.groovy.loadstatus 	= "Script loaded";
				
				self.groovy.parameters 		= jsonResult.placeholder;
				self.groovy.listeventsload	= jsonResult.listevents;
				self.groovy.directRestApi	= jsonResult.directRestApi;
				self.groovy.groovyResolved  = jsonResult.groovyResolved;
				self.groovy.title			= jsonResult.title;
				self.groovy.description		= jsonResult.description;
				
				self.inprogress				= false;
		})
		.error( function() {
				self.groovy.loadstatus="Error at loading."
				self.inprogress=false;
			});
	}
	
	
	
	this.groovyInterpretation = function() 
	{
		var self=this;
		self.inprogress	=true;
		self.groovy.result="";
		self.groovy.listevents=""
		self.groovy.result="";
		self.groovy.exception="";
		var param = { 'src': self.groovy.src };
		console.log("groovyinterpretation param="+angular.toJson(param));
		
		this.httpCall( param, 'groovyinterpretation' );
		
		
	}
	
	
	this.groovyexecute = function()
	{
		var self=this;
		self.inprogress=true;
		self.groovy.result="";
		self.groovy.listeventsload="";
		self.groovy.listevents='';
		self.groovy.result="";
		self.groovy.exception="";
		
		var param = {'type': self.groovy.type};
		
		if (self.groovy.type=='code' )	{
			param.placeholder = self.groovy.parameters;
			param.code = self.groovy.code;
		}
		else if (self.groovy.type=='srcparameter') {
			param.placeholder = self.groovy.parameters;
			param.code = self.groovy.code;
			param.src = self.groovy.src;
		}
		else {
			param.src = self.groovy.src;
		}
		
		
		// this.listUrlCall.push( "action=collect_reset");
		
		// prepare the string
		this.httpCall( param, 'groovyexecute' );
	}
	
	this.httpCall = function( param, actionToExecute ) {
		// groovy page does not manage the POST, and the groovy may be very big : so, let's trunk it
		this.listUrlCall=[];
		this.actionToExecute = actionToExecute;
		var json = angular.toJson( param, false);
		var firstUrl="1";
		// split the string by packet of 5000 
		while (json.length>0)
		{
			var jsonFirst = encodeURIComponent( json.substring(0,5000));
			json =json.substring(5000);
			var action="";
			if (json.length==0)
				action=actionToExecute;
			else
				action="collect_add";
			this.listUrlCall.push( "action="+action+"&firstUrl="+firstUrl+"&paramjson="+jsonFirst);
			firstUrl="0";
		}
		var self=this;
		// self.listUrlCall.push( "action=groovyexecute");
		
		
		self.listUrlIndex=0;
		self.executeListUrl( self ) // , self.listUrlCall, self.listUrlIndex );
		
	
	}
	
	
	// ------------------------------------------------------------------------------------------------------
	// List Execution
	
	this.executeListUrl = function( self ) // , listUrlCall, listUrlIndex )
	{
		console.log(" Call "+self.listUrlIndex+" : "+self.listUrlCall[ self.listUrlIndex ]);
		self.listUrlPercent= Math.round( (100 *  self.listUrlIndex) / self.listUrlCall.length);
		
		$http.get( '?page=custompage_bigapp&'+self.listUrlCall[ self.listUrlIndex ]+'&t='+Date.now() )
			.success( function ( jsonResult, statusHttp, headers, config ) {
				// connection is lost ?
				if (statusHttp==401 || typeof jsonResult === 'string') {
					console.log("Redirected to the login page !");
					window.location.reload();
				}

				// console.log("Correct, advance one more",
				// angular.toJson(jsonResult));
				self.listUrlIndex = self.listUrlIndex+1;
				if (self.listUrlIndex  < self.listUrlCall.length )
					self.executeListUrl( self ) // , self.listUrlCall,
												// self.listUrlIndex);
				else
				{
					console.log("Finish", angular.toJson(jsonResult));
					self.inprogress=false;
					self.listUrlPercent= 100; 
					self.groovy.result			= jsonResult.result;
					self.groovy.listevents		= jsonResult.listevents;
					
					self.groovy.exception   	= jsonResult.exception;
					self.groovy.directRestApi	= jsonResult.directRestApi;
					if (self.actionToExecute =="groovyexecute") {
						self.groovy.groovyResolved  = jsonResult.groovyResolved;
					}
					if (self.actionToExecute =="groovyinterpretation") {
						self.groovy.parameters 		= jsonResult.placeholder;
						self.groovy.listeventsload	= jsonResult.listevents;
					}

				}
			})
			.error( function() {
				self.inprogress=false;

				// alert('an error occure');
				});	
		};
	
		
		this.getListEvents = function ( listevents ) {
			return $sce.trustAsHtml(  listevents );
		}

});



})();
/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// JavaScript Document

//**********************************common*********************************

//**********************global variable**************************
var delay = 10;
var cycle_1s = 1000;
var cycle_3s = 3000;
var cycle_1m = 60 * 1000;
var digit_1 = 1;
var digit_3 = 3;

var processor_timer_id = 0;
var parser_timer_id = 0;
var router_timer_id = 0;
var threadpool_timer_id = 0;
var commandtps_timer_id = 0;
var connectionPool_timer_id = 0;

function Trim(str) {
	return str.replace(/(^\s*)|(\s*$)/g, "");
}
function B2M(Bytes) {
	/*
	 * Byte -> MB 1MB = 1024*1024 Bytes
	 */
	return parseInt(Bytes) / 1048576;
}
function isValidInt(str) {
	var pattern = /^-?\d+$/;
	if (!pattern.test(str))
		return false;
	return true;
}
function toMyProcessorJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"netIn\": ' + arr[i].netIn + ',\n');
		jsonString += ('\"netOut\": ' + arr[i].netOut + ',\n');
		jsonString += ('\"requestCount": ' + arr[i].requestCount + ',\n');
		jsonString += ('\"sampleTimeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]';
	return jsonString;
}

function toMyProcessor1JSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"id\": \"' + arr[i].id + '\",\n');
		jsonString += ('\"netIn\": ' + arr[i].netIn + ',\n');
		jsonString += ('\"netOut\": ' + arr[i].netOut + ',\n');
		jsonString += ('\"sampleTimeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]';
	return jsonString;
}

function toMyNetworkJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"netIn\": ' + arr[i].netIn + ',\n');
		jsonString += ('\"netOut\": ' + arr[i].netOut + ',\n');
		jsonString += ('\"timeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]';
	return jsonString;
}

function toMyParserJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"id\": \"' + arr[i].id + '\",\n');
		jsonString += ('\"parseCount\": ' + arr[i].parseCount + ',\n');
		jsonString += ('\"cachedCount\": ' + arr[i].cachedCount + ',\n');
		jsonString += ('\"timeCount\": ' + arr[i].timeCount + ',\n');
		jsonString += ('\"sampleTimeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]';
	return jsonString;
}

function toMyRouterJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"id\": \"' + arr[i].id + '\",\n');
		jsonString += ('\"routeCount\": ' + arr[i].routeCount + ',\n');
		jsonString += ('\"timeCount\": ' + arr[i].timeCount + ',\n');
		jsonString += ('\"sampleTimeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]';
	return jsonString;
}
function toMyConnectionPoolJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"db\": \"' + arr[i].db + '\",\n');
		jsonString += ('\"ds\": \"' + arr[i].ds + '\",\n');
		jsonString += ('\"executeCount\": ' + arr[i].executeCount + ',\n');
		jsonString += ('\"timeCount\": ' + arr[i].timeCount + ',\n');
		jsonString += ('\"sampleTimeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]'
	return jsonString;
}

function toMyCMDJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"id\": \"' + arr[i].id + '\",\n');
		jsonString += ('\"query\": ' + arr[i].query + ',\n');
		jsonString += ('\"stmtPrepared\": ' + arr[i].stmtPrepared + ',\n');
		jsonString += ('\"stmtExecute\": ' + arr[i].stmtExecute + ',\n');
		jsonString += ('\"stmtClose\": ' + arr[i].stmtClose + ',\n');
		jsonString += ('\"ping\": ' + arr[i].ping + ',\n');
		jsonString += ('\"quit\": ' + arr[i].quit + ',\n');
		jsonString += ('\"other\": ' + arr[i].other + ',\n');
		jsonString += ('\"sampleTimeStamp\": ' + arr[i].sampleTimeStamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]'
	return jsonString;
}

function loadDigitalInfo(pre, index, value) {
	var oTag = pre + index;
	var unit = " ";
	if (arguments[3] != null)
		unit = arguments[3];
	document.getElementById(oTag).innerHTML = " " + value + unit;
}

// -------------------Network Flash--------------------------

function createNetworkFlash(nodeId) {
	var t = 0;
	var oStr = "";
	var nStr = "";

	var last = new Array();
	var o = new Object();
	o.netIn = -1;
	o.netOut = -1;
	o.sampleTimeStamp = -1;
	last.push(o);

	// createFlash(i)
	var params = {
		wmode : "transparent"
	};
	var flashVars = {
		path : "flash/",
		settings_file : encodeURIComponent("flash/network_settings.xml"),
		data_file : encodeURIComponent("flash/network_data.txt")
	};
	swfobject.embedSWF("flash/amline.swf", "network", "594", "216", "8.0.0",
			"flash/expressInstall.swf", flashVars, params);

	// setInterval(reloadData,cycle_1s);
	setTimeout(reloadData, cycle_1s);

	function reloadData() {
		getJsonFromServer(
				"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "serverStatus",
					"last" : toMyNetworkJSONString(last),
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					// update server info
					if (t % 60 == 0) {
						loadDigitalInfo("uptime", "", returnedJson["uptime"]);
					}
					loadDigitalInfo("version", "", returnedJson["version"]);
					loadDigitalInfo("starttime", "", returnedJson["starttime"]);
					loadDigitalInfo("status", "", returnedJson["status"]);
					loadDigitalInfo("connection_count", "",
							returnedJson["connectionCount"]);
					loadDigitalInfo("mem_used", "", returnedJson["usedMemory"]);
					loadDigitalInfo("mem_max", "", returnedJson["maxMemory"]);
					loadDigitalInfo("mem_total", "",
							returnedJson["totalMemory"]);

					if (t > 20) {
						var index = oStr.indexOf("\n");
						oStr = oStr.substring(index + 1);
					}
					nStr = t + ";" + parseInt(returnedJson['netIn_deriv'])
							+ ";" + parseInt(returnedJson['netOut_deriv'])
							+ ";" + "\n";
					oStr += nStr;

					var flashMovie = document.getElementById("network");
					if (typeof (flashMovie) != "undefined") {
						if (navigator.userAgent.indexOf("MSIE") > 0) {
							// IE
							if (flashMovie.PercentLoaded() == 100) {
								// load completely
								var scrollTop = document.documentElement.scrollTop;
								var domTop = flashMovie.offsetTop;
								var pdom = flashMovie.offsetParent;
								while (pdom != null) {
									domTop += pdom.offsetTop;
									pdom = pdom.offsetParent;
								}
								var height = window.screen.availHeight;
								var edgeHeight = 150;
								if ((domTop - scrollTop) > 0
										&& (domTop - scrollTop) < (height - edgeHeight)) {
									flashMovie.setData(oStr);
								}
							}
						} else {
							// Other Browser
							flashMovie.setData(oStr);
						}
					}

					// create the new lastJson
					var l = new Object();
					l.netIn = returnedJson["netIn"];
					l.netOut = returnedJson["netOut"];
					l.sampleTimeStamp = returnedJson["timeStamp"];

					while (last.length != 0) {
						last.shift();
					}
					last.push(l);

					t++;
				});

		setTimeout(reloadData, cycle_1s);
	}
}

// ----------------------Cobar Status--------------------

function cobarStatus(nodeId) {

	var last = new Array();
	var o = new Object();
	o.netIn = -1;
	o.netOut = -1;
	o.requestCount = 0;
	o.sampleTimeStamp = -1;
	last.push(o);

	// setInterval(reloadData,cycle_1s);
	setTimeout(reloadData, cycle_1s);

	function reloadData() {
		getJsonFromServer("cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
			"cobarId" : nodeId,
			"valueType" : "serverStatus",
			"last" : toMyProcessorJSONString(last),
			"nowTime" : new Date()
		}), function(returnedJson) {
			while (last.length != 0) {
				last.shift();
			}
			// update server info
			loadDigitalInfo("uptime", "", returnedJson["uptime"]);
			loadDigitalInfo("version", "", returnedJson["version"]);
			loadDigitalInfo("starttime", "", returnedJson["starttime"]);
			loadDigitalInfo("status", "", returnedJson["status"]);
			loadDigitalInfo("connection_count", "",
					returnedJson["connectionCount"]);
			loadDigitalInfo("mem_used", "", returnedJson["usedMemory"]);
			loadDigitalInfo("mem_total", "", returnedJson["totalMemory"]);
			loadDigitalInfo("net_recv", "", returnedJson["netIn_deriv"]);
			loadDigitalInfo("net_send", "", returnedJson["netOut_deriv"]);
			loadDigitalInfo("qps_count", "", returnedJson["reCount_deriv"]);

			var l = new Object();
			l.netIn = returnedJson["netInC"];
			l.netOut = returnedJson["netOutC"];
			l.requestCount = returnedJson["requestCountC"];
			l.sampleTimeStamp = returnedJson["sampleTimeStamp"];

			last.push(l);
			setTimeout(reloadData, cycle_1s);
		});
	}
}

// --------------------------------ProcessorTab-------------------------------

function loadProcessorTab(nodeId) {

	var last = new Array();
	var o = new Object();
	o.netIn = -1;
	o.netOut = -1;
	o.requestCount = 0;
	o.sampleTimeStamp = -1;
	last.push(o);

	// processor_timer_id = setInterval(reloadData,cycle_1s);
	setTimeout(reloadData, cycle_1s);

	function reloadData() {
		getJsonFromServer(
				"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "processorStatus",
					"last" : toMyProcessorJSONString(last),
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					var targetTable = document.getElementById("processorTable");
					while (targetTable.rows.length > 1) {
						targetTable.deleteRow(1);
					}

					if (returnedJson[0] != null)
						for ( var i = 0; i < returnedJson.length; i++) {
							var newRow = targetTable.insertRow(i + 1);

							var ptd = document.createElement('td');
							ptd.setAttribute('style', 'text-align:left');
							ptd
									.appendChild(document
											.createTextNode(returnedJson[i]["processorId"]));
							newRow.appendChild(ptd);

							newRow.insertCell(1).innerHTML = returnedJson[i]["netIn"];
							newRow.insertCell(2).innerHTML = returnedJson[i]["netOut"];
							newRow.insertCell(3).innerHTML = returnedJson[i]["requestCount"];
							newRow.insertCell(4).innerHTML = returnedJson[i]["rQueue"];
							newRow.insertCell(5).innerHTML = returnedJson[i]["wQueue"];
							newRow.insertCell(6).innerHTML = returnedJson[i]["freeBuffer"];
							newRow.insertCell(7).innerHTML = returnedJson[i]["totalBuffer"];
							newRow.insertCell(8).innerHTML = returnedJson[i]["connections"];
							newRow.insertCell(9).innerHTML = returnedJson[i]["bc_count"];

							if ("TOTAL" == returnedJson[i]["processorId"]) {
								var l = new Object();
								l.netIn = returnedJson[i]["netInC"];
								l.netOut = returnedJson[i]["netOutC"];
								l.requestCount = returnedJson[i]["requestCountC"];
								l.sampleTimeStamp = returnedJson[i]["sampleTimeStamp"];
								while (last.length != 0) {
									last.shift();
								}
								last.push(l);
							}
						}

					showtable("processorTable");
				});

		// processor_timer_id = setTimeout(reloadData, cycle_1s);
	}
}

// *********************************ThreadPool**************************
function loadThreadpool(nodeId) {
	var data = [];

	createThreadpoolFlash();

	function createThreadpoolFlash() {
		getJsonFromServer(
				"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "threadPoolStatus",
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					if (returnedJson[0] != null)
						for ( var i = 0; i < returnedJson.length; i++) {
							var poolName = returnedJson[i]["threadPoolName"];

							// createFlash(i)
							var params = {
								wmode : "transparent"
							};
							var flashVars = {
								path : "flash/",
								settings_file : encodeURIComponent("flash/threadpool_settings.xml"),
								data_file : encodeURIComponent("flash/threadpool_data.txt")
							};
							swfobject.embedSWF("flash/ampie.swf", poolName
									+ "_pool_pie", "326", "146", "8.0.0",
									"flash/expressInstall.swf", flashVars,
									params);
							data[i] = "";

							loadDigitalInfo(poolName, "_pool_size",
									returnedJson[i]["poolSize"]);
							loadDigitalInfo(poolName, "_pool_queue_size",
									returnedJson[i]["taskQueue"]);
							loadDigitalInfo(poolName,
									"_pool_completed_task_count",
									returnedJson[i]["completedTask"]);
							loadDigitalInfo(poolName, "_pool_task_count",
									returnedJson[i]["totalTask"]);
						}
					threadpool_timer_id = setInterval(reloadData, cycle_1s);
				});
	}

	function reloadData() {
		getJsonFromServer(
				"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "threadPoolStatus",
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					if (returnedJson[0] != null)
						for ( var i = 0; i < returnedJson.length; i++) {
							var poolName = returnedJson[i]["threadPoolName"];
							if (parseInt(returnedJson[i]["poolSize"]) != 0) {
								data[i] = "Active"
										+ ";"
										+ returnedJson[i]["activeSize"]
										+ "\nIdle"
										+ ";"
										+ eval(parseInt(returnedJson[i]["poolSize"])
												- parseInt(returnedJson[i]["activeSize"]));

								var flashMovie = document
										.getElementById(poolName + "_pool_pie");
								if (typeof (flashMovie) != "undefined") {
									if (navigator.userAgent.indexOf("MSIE") > 0) {
										// IE
										if (flashMovie.PercentLoaded() == 100) {
											// load completely
											var scrollTop = document.documentElement.scrollTop;
											var domTop = flashMovie.offsetTop;
											var pdom = flashMovie.offsetParent;
											while (pdom != null) {
												domTop += pdom.offsetTop;
												pdom = pdom.offsetParent;
											}
											var height = window.screen.availHeight;
											var edgeHeight = 150;
											if ((domTop - scrollTop) > 0
													&& (domTop - scrollTop) < (height - edgeHeight)) {
												flashMovie.setData(data[i]);
											}
										}
									} else {
										// Other Browser
										flashMovie.setData(data[i]);
									}
								}
							}

							loadDigitalInfo(poolName, "_pool_size",
									returnedJson[i]["poolSize"]);
							loadDigitalInfo(poolName, "_pool_queue_size",
									returnedJson[i]["taskQueue"]);
							loadDigitalInfo(poolName,
									"_pool_completed_task_count",
									returnedJson[i]["completedTask"]);
							loadDigitalInfo(poolName, "_pool_task_count",
									returnedJson[i]["totalTask"]);
						}
				});
	}
}

// **********************************Command*********************************
function loadCommandTable(nodeId) {
	getJsonFromServer(
			"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
				"cobarId" : nodeId,
				"valueType" : "command",
				"nowTime" : new Date()
			}),
			function(returnedJson) {
				var targetTable = document.getElementById("commandTable");
				while (targetTable.rows.length > 1) {
					targetTable.deleteRow(1);
				}
				if (returnedJson[0] != null)
					for ( var i = 0; i < returnedJson.length; i++) {
						var newRow = targetTable.insertRow(i + 1);

						var ptd = document.createElement('td');
						ptd.setAttribute('style', 'text-align:left');
						ptd
								.appendChild(document
										.createTextNode(returnedJson[i]["processorId"]));
						newRow.appendChild(ptd);

						// newRow.insertCell(0).innerHTML=returnedJson[i]["processorId"];

						newRow.insertCell(1).innerHTML = returnedJson[i]["initDB"];
						newRow.insertCell(2).innerHTML = returnedJson[i]["query"];
						newRow.insertCell(3).innerHTML = returnedJson[i]["stmtPrepared"];
						newRow.insertCell(4).innerHTML = returnedJson[i]["stmtExecute"];
						newRow.insertCell(5).innerHTML = returnedJson[i]["stmtClose"];
						newRow.insertCell(6).innerHTML = returnedJson[i]["ping"];
						newRow.insertCell(7).innerHTML = returnedJson[i]["kill"];
						newRow.insertCell(8).innerHTML = returnedJson[i]["quit"];
						newRow.insertCell(9).innerHTML = returnedJson[i]["other"];
					}

				showtable("commandTable");
			});
}
// *************************Connection Table*************************
function loadConnectionTable(nodeId) {
	getJsonFromServer(
			"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
				"cobarId" : nodeId,
				"valueType" : "connection",
				"nowTime" : new Date()
			}),
			function(returnedJson) {
				var targetTable = document.getElementById("connectionTable");
				while (targetTable.rows.length > 1) {
					targetTable.deleteRow(1);
				}
				if (returnedJson[0] != null)
					for ( var i = 0; i < returnedJson.length; i++) {
						var newRow = targetTable.insertRow(i + 1);

						var ptd = document.createElement('td');
						ptd.setAttribute('style', 'text-align:left');
						ptd.appendChild(document
								.createTextNode(returnedJson[i]["processor"]));
						newRow.appendChild(ptd);

						// newRow.insertCell(0).innerHTML=returnedJson[i]["processor"];

						var htd = document.createElement('td');
						htd.setAttribute('style', 'text-align:left');
						htd.appendChild(document
								.createTextNode(returnedJson[i]["host"]));
						newRow.appendChild(htd);
						// newRow.insertCell(1).innerHTML=returnedJson[i]["host"];

						newRow.insertCell(2).innerHTML = returnedJson[i]["port"];
						newRow.insertCell(3).innerHTML = returnedJson[i]["local_port"];

						var std = document.createElement('td');
						std.setAttribute('style', 'text-align:left');
						std.appendChild(document
								.createTextNode(returnedJson[i]["schema"]));
						newRow.appendChild(std);
						// newRow.insertCell(4).innerHTML=returnedJson[i]["schema"];

						var ctd = document.createElement('td');
						ctd.setAttribute('style', 'text-align:left');
						ctd.appendChild(document
								.createTextNode(returnedJson[i]["charset"]));
						newRow.appendChild(ctd);
						// newRow.insertCell(5).innerHTML=returnedJson[i]["charset"];

						newRow.insertCell(6).innerHTML = returnedJson[i]["netIn"];
						newRow.insertCell(7).innerHTML = returnedJson[i]["netOut"];
						newRow.insertCell(8).innerHTML = returnedJson[i]["aliveTime"];
						newRow.insertCell(9).innerHTML = returnedJson[i]["attempsCount"];
						newRow.insertCell(10).innerHTML = returnedJson[i]["recvBuffer"];
						newRow.insertCell(11).innerHTML = returnedJson[i]["sendQueue"];
						newRow.insertCell(12).innerHTML = returnedJson[i]["channel"];
					}
				showtable("connectionTable");
			});
}

// ************************ThreadPool Table**************************
function loadThreadPoolTable(nodeId) {
	getJsonFromServer(
			"cobarNodeInstantPerfValue.ajax?" + encodeFormData( {
				"cobarId" : nodeId,
				"valueType" : "threadPoolStatus",
				"nowTime" : new Date()
			}),
			function(returnedJson) {
				var targetTable = document.getElementById("threadpoolTable");
				while (targetTable.rows.length > 1) {
					targetTable.deleteRow(1);
				}
				if (returnedJson[0] != null)
					for ( var i = 0; i < returnedJson.length; i++) {
						var newRow = targetTable.insertRow(i + 1);
						var ptd = document.createElement('td');
						ptd.setAttribute('style', 'text-align:left');
						ptd
								.appendChild(document
										.createTextNode(returnedJson[i]["threadPoolName"]));
						newRow.appendChild(ptd);

						// newRow.insertCell(0).innerHTML=returnedJson[i]["threadPoolName"];

						newRow.insertCell(1).innerHTML = returnedJson[i]["poolSize"];
						newRow.insertCell(2).innerHTML = returnedJson[i]["activeSize"];
						newRow.insertCell(3).innerHTML = returnedJson[i]["taskQueue"];
						newRow.insertCell(4).innerHTML = returnedJson[i]["completedTask"];
						newRow.insertCell(5).innerHTML = returnedJson[i]["totalTask"];
					}
				showtable("threadpoolTable");
			});
}

// *********************************DataNodes
// Table*********************************
function loadDatanodeTable(nodeId) {

	fetchDBData();
	// dataSource_timer_id = setInterval(fetchDBData, cycle_1m);

	function fetchDBData() {
		getJsonFromServerWithPost(
				"cobarNodeInstantPerfValue.ajax",
				encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "datanodes",
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					var targetTable = document.getElementById("datanodeTable");
					while (targetTable.rows.length > 1) {
						targetTable.deleteRow(1);
					}
					if (returnedJson[0] != null)
						for ( var i = 0; i < returnedJson.length; i++) {
							var newRow = targetTable.insertRow(i + 1);
							var tmp = 0;

							newRow.insertCell(tmp++).innerHTML = i + 1;

							var dtd = document.createElement('td');
							dtd.setAttribute('style', 'text-align:left');
							// dtd.setAttribute('title',returnedJson[i]["dataSource"]);
							dtd
									.appendChild(document
											.createTextNode(returnedJson[i]["poolName"]));
							newRow.appendChild(dtd);
							tmp++;

							var dstd = document.createElement('td');
							dstd.setAttribute('style', 'text-align:left');
							dstd
									.appendChild(document
											.createTextNode(returnedJson[i]["dataSource"]));
							newRow.appendChild(dstd);
							tmp++;
							// var cell = newRow.insertCell(0);
							// cell.innerHTML=returnedJson[i]["poolName"];
							// cell.title=returnedJson[i]["dataSource"];

							if (-1 == returnedJson[i]["index"]) {
								newRow.insertCell(tmp++).innerHTML = "NULL";
								newRow.insertCell(tmp++).innerHTML = returnedJson[i]["type"];
								newRow.insertCell(tmp++).innerHTML = "NULL";
								newRow.insertCell(tmp++).innerHTML = "NULL";
								newRow.insertCell(tmp++).innerHTML = "NULL";
							} else {
								newRow.insertCell(tmp++).innerHTML = returnedJson[i]["index"];
								newRow.insertCell(tmp++).innerHTML = returnedJson[i]["type"];
								newRow.insertCell(tmp++).innerHTML = returnedJson[i]["active"];
								newRow.insertCell(tmp++).innerHTML = returnedJson[i]["idle"];
								newRow.insertCell(tmp++).innerHTML = returnedJson[i]["size"];
							}
							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["executeCount"];
							// newRow.insertCell(7).innerHTML=dfn(returnedJson[i]["totalTime"],
							// digit_1);
							// newRow.insertCell(8).innerHTML=dfn(returnedJson[i]["maxTime"],
							// digit_1);
							// newRow.insertCell(9).innerHTML=returnedJson[i]["maxSQL"];
							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["recoveryTime"];
						}
					showtable("datanodeTable");
				});
	}
}

// *************************Databases Table************************
function loadDatabaseTable(nodeId) {

	fetchDatabasesData();
	// dataSource_timer_id = setInterval(fetchDatabasesData, cycle_1m);

	function fetchDatabasesData() {
		getJsonFromServerWithPost(
				"cobarNodeInstantPerfValue.ajax",
				encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "databases",
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					var targetTable = document.getElementById("databaseTable");
					while (targetTable.rows.length > 1) {
						targetTable.deleteRow(1);
					}
					if (returnedJson[0] != null)
						for ( var i = 0; i < returnedJson.length; i++) {
							var newRow = targetTable.insertRow(i + 1);
							var tmp = 0;

							var dtd = document.createElement('td');
							dtd.setAttribute('style', 'text-align:right');
							// dtd.setAttribute('title',returnedJson[i]["dataSource"]);
							dtd.appendChild(document.createTextNode(i + 1));
							newRow.appendChild(dtd);
							tmp++;

							newRow.insertCell(tmp++).innerHTML = returnedJson[i];
						}
					showtable("databaseTable");
				});
	}
}

// *************************Datasources Table****************************
function loadDatasourceTable(nodeId) {

	fetchDatasourceData();
	// dataSource_timer_id = setInterval(fetchDatasourceData, cycle_1m);

	function fetchDatasourceData() {
		getJsonFromServerWithPost(
				"cobarNodeInstantPerfValue.ajax",
				encodeFormData( {
					"cobarId" : nodeId,
					"valueType" : "datasources",
					"nowTime" : new Date()
				}),
				function(returnedJson) {
					var targetTable = document
							.getElementById("datasourceTable");
					while (targetTable.rows.length > 1) {
						targetTable.deleteRow(1);
					}
					if (returnedJson[0] != null)
						for ( var i = 0; i < returnedJson.length; i++) {
							var newRow = targetTable.insertRow(i + 1);
							var tmp = 0;

							var dtd = document.createElement('td');
							dtd.setAttribute('style', 'text-align:right');
							// dtd.setAttribute('title',returnedJson[i]["dataSource"]);
							dtd.appendChild(document.createTextNode(i + 1));
							newRow.appendChild(dtd);
							tmp++;

							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["name"];
							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["type"];
							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["host"];
							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["port"];
							newRow.insertCell(tmp++).innerHTML = returnedJson[i]["schema"];
						}
					showtable("datasourceTable");
				});
	}
}

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
var cycle_d = 1;
var cycle_1s = 1000;

function toMyJSONString(arr) {
	var jsonString = '[\n';
	for ( var i = 0; i < arr.length; i++) {
		jsonString += '{\n';
		jsonString += ('\"id\": ' + arr[i].id + ',\n');
		jsonString += ('\"netIn\": ' + arr[i].netIn + ',\n');
		jsonString += ('\"netOut\": ' + arr[i].netOut + ',\n');
		jsonString += ('\"reCount\": ' + arr[i].reCount + ',\n');
		jsonString += ('\"timestamp\": ' + arr[i].timestamp + ',\n');
		jsonString += ('\"flag\": ' + arr[i].flag + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]'
	return jsonString;
}

function loadMemoryUsage(clusterId) {
	// setInterval(fetchMemoryData, cycle_1s);
	function fetchMemoryData() {
		getJsonFromServer("clusterInstantPerfValue.ajax?" + encodeFormData( {
			"clusterId" : clusterId,
			"valueType" : "cobarServerLevelMemoryUsage",
			"nowTime" : new Date()
		}), function(returnedJson) {

			for ( var i = 0; i < returnedJson.length; i++) {
				loadDigitalInfo("memory_usage_", returnedJson[i]["first"],
						returnedJson[i]["second"] + "%");
			}

			setTimeout(fetchMemoryData, cycle_1s);
		});

	}
	setTimeout(fetchMemoryData, cycle_d);
	// setInterval("fetchMemoryData()", cycle_1s);
}

function loadDigitalInfo(pre, id, value) {
	document.getElementById(pre + id).innerHTML = value;
}

function loadThtoughput(clusterId) {
	// initial 'last' Array;
	var last = new Array();
	var o = new Object();
	o.id = -1;
	o.netIn = -1;
	o.netOut = -1;
	o.timestamp = -1;
	o.reCount = -1;
	o.flag = "cluster";
	last.push(o);

	// setInterval(fetchClusterThroughputData, cycle_1s);

	function fetchClusterThroughputData() {
		getJsonFromServerWithPost("clusterInstantPerfValue.ajax",
				encodeFormData( {
					"clusterId" : clusterId,
					"last" : toMyJSONString(last),
					"valueType" : "cobarClusterLevelThroughput",
					"nowTime" : new Date()
				}), function(returnedJson) {
					while (last.length != 0) {
						last.shift();
					}

					var conCount = 0;
					for (i = 0; i < returnedJson.length; i++) {
						if ("cluster" == returnedJson[i]["flag"]) {
							loadDigitalInfo("cluster_recv", "",
									returnedJson[i]["netIn_deriv"]);
							loadDigitalInfo("cluster_send", "",
									returnedJson[i]["netOut_deriv"]);
							loadDigitalInfo("total_qps_count", "",
									returnedJson[i]["request_deriv"]);

							var l = new Object();
							l.id = returnedJson[i]["id"];
							l.netIn = returnedJson[i]["netIn"];
							l.netOut = returnedJson[i]["netOut"];
							l.timestamp = returnedJson[i]["timestamp"];
							l.reCount = returnedJson[i]["request"];
							l.flag = "cluster";

							last.push(l);
						} else {
							loadDigitalInfo("net_recv_", returnedJson[i]["id"],
									returnedJson[i]["netIn_deriv"]);

							loadDigitalInfo("net_send_", returnedJson[i]["id"],
									returnedJson[i]["netOut_deriv"]);

							loadDigitalInfo("qps_count_",
									returnedJson[i]["id"],
									returnedJson[i]["request_deriv"]);

							loadDigitalInfo("connection_count_",
									returnedJson[i]["id"],
									returnedJson[i]["connection"]);
							conCount += returnedJson[i]["connection"];

							var l = new Object();
							l.id = returnedJson[i]["id"];
							l.netIn = returnedJson[i]["netIn"];
							l.netOut = returnedJson[i]["netOut"];
							l.timestamp = returnedJson[i]["timestamp"];
							l.reCount = returnedJson[i]["request"];
							l.flag = "cobar";

							last.push(l);
						}
					}
					loadDigitalInfo("total_connection_count", "", conCount);
					setTimeout(fetchClusterThroughputData, cycle_1s);
				});

	}
	setTimeout(fetchClusterThroughputData, cycle_d);
	// setInterval("fetchClusterThroughputData()", cycle_1s);
}

function getStatus(clusterId) {
	function getAllStatus() {
		getJsonFromServer("clusterInstantPerfValue.ajax?" + encodeFormData( {
			"clusterId" : clusterId,
			"valueType" : "status",
			"nowTime" : new Date()
		}), function(returnedJson) {
			for ( var i = 0; i < returnedJson.length; i++) {
				var status = "";
				if ("Active" == returnedJson[i]["second"]) {
					status = "连接正常";
				} else if ("InActive" == returnedJson[i]["second"]) {
					status = "禁用";
				} else {
					status = "<font color='red'>连接异常</font>";
				}
				loadDigitalInfo("status_", returnedJson[i]["first"], status);
			}
		});

	}
	setTimeout(getAllStatus, cycle_d);
}

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
		jsonString += ('\"timestamp\": ' + arr[i].timestamp + '\n');
		jsonString += '}';
		if (i + 1 < arr.length)
			jsonString += ',\n';
	}
	jsonString += '\n]'
	return jsonString;
}

function loadDigitalInfo(pre, id, value) {
	document.getElementById(pre + id).innerHTML = value;
}

function loadIndexInfo() {
	// initial 'last' Array;
	var last = new Array();
	var o = new Object();
	o.id = -1;
	o.netIn = -1;
	o.netOut = -1;
	o.timestamp = -1;
	o.reCount = -1;
	last.push(o);

	// setInterval(fetchClusterThroughputData, cycle_1s);

	function fetchClusterThroughputData() {
		getJsonFromServerWithPost("clusterInstantPerfValue.ajax",
				encodeFormData( {
					"last" : toMyJSONString(last),
					"valueType" : "indexInfo",
					"nowTime" : new Date()
				}), function(returnedJson) {
					while (last.length != 0) {
						last.shift();
					}

					for (i = 0; i < returnedJson.length; i++) {
						loadDigitalInfo("cluster_recv_", returnedJson[i]["id"],
								returnedJson[i]["netIn_deriv"]);
						loadDigitalInfo("cluster_send_", returnedJson[i]["id"],
								returnedJson[i]["netOut_deriv"]);
						loadDigitalInfo("total_qps_count_",
								returnedJson[i]["id"],
								returnedJson[i]["reCount_deriv"]);
						loadDigitalInfo("total_connection_count_",
								returnedJson[i]["id"],
								returnedJson[i]["conCount"]);

						var count = returnedJson[i]["activeCount"]
								+ "/<font color='red'>"
								+ returnedJson[i]["errorCount"] + "</font>/"
								+ returnedJson[i]["totalCount"];

						loadDigitalInfo("cobar_count_", returnedJson[i]["id"],
								count);
						loadDigitalInfo("schema_count_", returnedJson[i]["id"],
								returnedJson[i]["schemaCount"]);

						var l = new Object();
						l.id = returnedJson[i]["id"];
						l.netIn = returnedJson[i]["netIn"];
						l.netOut = returnedJson[i]["netOut"];
						l.timestamp = returnedJson[i]["timestamp"];
						l.reCount = returnedJson[i]["reCount"];

						last.push(l);
					}
					if (last.length == 0) {
						var o = new Object();
						o.id = -1;
						o.netIn = -1;
						o.netOut = -1;
						o.timestamp = -1;
						o.reCount = -1;
						last.push(o);
					}
				});
		setTimeout(fetchClusterThroughputData, cycle_1s);
	}
	setTimeout(fetchClusterThroughputData, cycle_d);

}

function loadClusterInfo(clusterId) {
	// initial 'last' Array;
	var last = new Array();
	var o = new Object();
	o.id = -1;
	o.netIn = -1;
	o.netOut = -1;
	o.timestamp = -1;
	o.reCount = -1;
	last.push(o);

	// setInterval(fetchClusterThroughputData, cycle_1s);

	function fetchClusterThroughputData() {
		getJsonFromServerWithPost("clusterInstantPerfValue.ajax",
				encodeFormData( {
					"last" : toMyJSONString(last),
					"valueType" : "clusterInfo",
					"clusterId" : clusterId,
					"nowTime" : new Date()
				}), function(returnedJson) {
					while (last.length != 0) {
						last.shift();
					}

					loadDigitalInfo("cluster_recv_", returnedJson["id"],
							returnedJson["netIn_deriv"]);
					loadDigitalInfo("cluster_send_", returnedJson["id"],
							returnedJson["netOut_deriv"]);
					loadDigitalInfo("total_qps_count_", returnedJson["id"],
							returnedJson["request_deriv"]);
					loadDigitalInfo("total_connection_count_",
							returnedJson["id"], returnedJson["connection"]);

					var count = returnedJson["active"] + "/<font color='red'>"
							+ returnedJson["error"] + "</font>/"
							+ returnedJson["total"];

					loadDigitalInfo("cobar_count_", returnedJson["id"], count);
					loadDigitalInfo("schema_count_", returnedJson["id"],
							returnedJson["schema"]);

					var l = new Object();
					l.id = returnedJson["id"];
					l.netIn = returnedJson["netIn"];
					l.netOut = returnedJson["netOut"];
					l.timestamp = returnedJson["timestamp"];
					l.reCount = returnedJson["request"];

					last.push(l);

					setTimeout(fetchClusterThroughputData, cycle_1s);
				});
	}
	setTimeout(fetchClusterThroughputData, cycle_d);
}

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

function createA(href,display){
	var a=document.createElement("a");
	a.href=href;
	a.innerHTML=display;
	return a;
}

function fromMilliseconds2String(milliseconds){
//  324897987 -> 2010-12-23 12:23:23
	var date=new Date(milliseconds);
	var year=date.getFullYear();
	var month=date.getMonth()+1;
	var day=date.getDate();
	var hour=date.getHours();
	var minu=date.getMinutes();
	var sec=date.getSeconds();
	return year+"-"+month+"-"+day+" "+hour+":"+minu+":"+sec;
}

function encodeFormData(data) {
    var pairs = [];
    var regexp = /%20/g;
    for(var name in data) {
        var value = data[name].toString();
		//	var pair = encodeURIComponent(name).replace(regexp,"+") + '=' +
		//	encodeURIComponent(value).replace(regexp,"+");
        var pair = encodeURIComponent(name) + '=' + encodeURIComponent(value);
        pairs.push(pair);
    }
    return pairs.join('&');
}

function getJsonFromServer(url,callbak){
	var request = null;
	if(window.XMLHttpRequest)
    {
    	//IE7.0+£¬FF,Chrome,Opera...Use xmlhttprequest Object
        request=new XMLHttpRequest();
    }
    else if(window.ActiveXObject) 
    {
    	//IE6.0-, Use activexobject Object,If ActiveX is Forbidden in the browser, may be fail.
        request=new ActiveXObject("Microsoft.XMLHttp");
    }
	//var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
        if (request.readyState == 4 && request.status == 200) {
            callbak(eval('('+request.responseText+')'));
        }
    }
    request.open("GET",url);
    request.send(null);
}

function getJsonFromServerWithPost(url,params,callback){
	var request = null;
	if(window.XMLHttpRequest)
    {
    	//IE7.0+£¬FF,Chrome,Opera...Use xmlhttprequest Object
        request=new XMLHttpRequest();
    }
    else if(window.ActiveXObject) 
    {
    	//IE6.0-, Use activexobject Object,If ActiveX is Forbidden in the browser, may be fail.
        request=new ActiveXObject("Microsoft.XMLHttp");
    }
    request.open("POST", url, true);
    request.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
	request.onreadystatechange = function() {
		//Call a function when the state changes.
		if(request.readyState == 4 && request.status == 200) {
			callback(eval('('+request.responseText+')'));
		}
		
	}
	request.send(params);
}

function fromMilli2Time(milliseconds, precision){
	var Time = "";
	var units = new Array("d ", "h ", "m ", "s");
	var la = new Array(4);
	la[0] = Math.floor((milliseconds/86400000));	
	la[1] = Math.floor((milliseconds/3600000))%24;	
	la[2] = Math.floor((milliseconds/60000))%60;	
	la[3] = Math.floor((milliseconds/1000))%60;	
	
	var index = 0;
    for (var i = 0; i < la.length; i++) {
        if (la[i] != 0) {
            index = i;
            break;
        }
    }	
    var validLength = la.length - index;
    for (var j = 0; (j<validLength && j<precision); j++) {
    	Time += (la[index]+units[index]);
    	index++;
    }
    return Time;
}

function fromFloat2Percent(floatNum){
	var percent = floatNum * 100;
	return percent + "%";
}

function dfn(value, scale) {
	var index = value.toString().indexOf(".");
	if(index == -1)
		return value.toString();
	else{
		return parseFloat(value.toString().substring(0, index+scale+1));
	}
}

function ms2us(value) {
	return parseFloat(value)*1000;
}

function Trim(str) {
	return str.replace(/(^\s*)|(\s*$)/g, "");
} 



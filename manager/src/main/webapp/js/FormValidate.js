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
function isValidUserName(username){
	var pattern = /^[A-Za-z]+\.[A-Za-z]+$/; 
 	if(!pattern.test(username))
 	      return false;
 	return true;
}

function isValidAlphaOrDigit(username)
 {
	var pattern=/^[a-zA-Z]{1}([a-zA-Z0-9]|[._]){3,19}$/;
 	if(!pattern.test(username))
 	      return false;
 	return true;
 }

function isValidName(name)
{
	var pattern=/^([^\x00-\xff]|[a-zA-Z]|[.\s]){2,36}$/;
	if(!pattern.test(name))
	      return false;
	return true;
}
 
  function isValidPassWord(pwd)
 {
 	var pattern = /^([A-Za-z0-9]|_){6,20}$/;
 	if(!pattern.test(pwd))
 	      return false;
 	return true;
 }
 
   function isValidIP(ip)
 {
 	var pattern=/^(\d+)\.(\d+)\.(\d+)\.(\d+)$/g;
    if(pattern.test(ip))
    {
	    if( RegExp.$1 <256 && RegExp.$2<256 && RegExp.$3<256 && RegExp.$4<256) {
			return true;
		}
		else return false;
    }
    else return false;
 }
 
 function isValidEmail(email)
 {
 	var pattern = /^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/;   
 	if(!pattern.test(email)) 
 		return false;
 	return true;
 }
 
 function isValidDate(date)
 {
 	var pattern1 = /^(\d{4})-(\d{2})-(\d{2})$/;
 	var pattern2 = /^(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)$/;
 	if(!pattern1.test(date) || !pattern2.test(date))
 		return false;
 	return true;
 }
 
  function isValidDateAccurate(date)
 {
 	var pattern1 = /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})$/;
 	var pattern2 = /^(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)(([0-1]\d|2[0-3]):[0-5]\d)$/;
 	if(!pattern1.test(date) || !pattern2.test(date))
 		return false;
 	return true;
 }
 
 function focusplease(span,tips)// "focus" Cannot used as a funtion name
 {
    document.getElementById(span).innerHTML="<font color='brown'>"+tips+"</font>";
 }
 
  function checkBlank(id,span,tips)
 {
   var str = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
   if(str.length==0)
   {
       document.getElementById(span).innerHTML="<font color='red'>"+tips+"</font>";
       return false;
   }else{
  	   document.getElementById(span).innerHTML="<font color=green>OK</font>";
   	   return true;
   }
 }
 
 function checkBlankNoOK(id,span,tips)
{
	var str = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(str.length==0){
		document.getElementById(span).innerHTML="<font color='red'>"+tips+"</font>";
		return false;
	}else{
		document.getElementById(span).innerHTML="";
		return true;
   }
}
 
 function checkDeploy(id,span)
 {
 	var str = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	var pattern=/^([^\x00-\xff]|[a-zA-Z]|[.\s]){2,55}$/;
	
 	if(str.length < 2 || str.length > 55 ){
 		document.getElementById(span).innerHTML="<font color='red'>集群描述长度为2-55个字符</font>";
 		return false;
 	}else{
 		document.getElementById(span).innerHTML="";
 		return true;
    }
 }
 
 function checkBlankNothing(id)
 {
   var str = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
   if(str.length==0){
       return false;
   }else{
   	   return true;
   }
 }
 
 function checkBlankDouble(id1, id2, span1, span2, tips)
 {
 	if(checkBlankNothing(id1)|| checkBlankNothing(id2))
 	{
 		document.getElementById(span).innerHTML="";
 		return true;
 	}
 	else
 	{
 		document.getElementById(span).innerHTML="<font color='red'>"+tips+"</font>";
 		return false;
 	}
 }
 
  function checkPWD(id,span)
 {
 	var pwd = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	 if(pwd.length==0)
 	 {
       document.getElementById(span).innerHTML="<font color='red'>密码不能为空</font>";
       return false;
     }
   	 else if(!isValidPassWord(pwd))
   	 {
       document.getElementById(span).innerHTML="<font color='red'>6-20个字符,由字母数字或下划线组成</font>";
       return false;
     }
     else 
       document.getElementById(span).innerHTML="<font color='green'>OK</font>";
     return true;
 }
  
  function checkPWDNoOk(id,span)
  {
  	var pwd = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
  	 if(pwd.length==0)
  	 {
        document.getElementById(span).innerHTML="<font color='red'>密码不能为空</font>";
        return false;
      }
    	 else if(!isValidPassWord(pwd))
    	 {
        document.getElementById(span).innerHTML="<font color='red'>6-20个字符,由字母数字或下划线组成</font>";
        return false;
      }
      return true;
  }
  
  function checkLogPWD(id,span)
  {
  	var pwd = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
  	 if(pwd.length==0)
  	 {
        return true;
      }
     else if(!isValidPassWord(pwd))
     {
        document.getElementById(span).innerHTML="<font color='red'>6-20个字符,由字母数字或下划线组成</font>";
        return false;
      }
      else 
        document.getElementById(span).innerHTML="<font color='green'>OK</font>";
      return true;
  }
 
   function checkIP(id,span)
 {
 	var ip = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	 if(ip.length==0)
 	 {
       document.getElementById(span).innerHTML="<font color='red'>请输入IP</font>";
       return false;
     }
   	 else if(!isValidIP(ip))
   	 {
       document.getElementById(span).innerHTML="<font color='red'>IP格式不正确</font>";
       return false;
     }
     else 
       document.getElementById(span).innerHTML="<font color='green'>OK</font>";
     return true;
 }
 
   function checkPWDbyID(id)
 {
 	var pwd = document.getElementById(id).value;
 	 if(pwd.length==0)
 	 {
       return false;
     }
   	 else if(!isValidPassWord(pwd))
   	 {
       return false;
     }
     else 
     {
     	return true;
 	 }
 }
 
   function checkRePWD(pid,rpid,span)
 {
 	var repassword = document.getElementById(rpid).value;
    var password = document.getElementById(pid).value;
    var tipspan = document.getElementById(span);
   if(repassword.length==0)
   { 
   	 tipspan.innerHTML="<font color='red'>确认密码不能为空！</font>";
   	 return false;
   }
   else if(!(repassword==password))
   {
   	 tipspan.innerHTML="<font color='red'>密码与重复密码不一致！</font>";
   	 return false;
   }
   else if(checkPWDbyID(pid))
   {
     tipspan.innerHTML="<font color='green'>OK</font>";
     return true;
   }
   else 
   {
   		tipspan.innerHTML="";
   		return false;
   }
 }
 
    function checkPWDwithRepwd(pid,rpid,pspan,rpspan)
 {
 	var pwd = document.getElementById(pid).value;
 	 if(pwd.length==0)
 	 {
       document.getElementById(pspan).innerHTML="<font color='red'>密码不能为空</font>";
       return false;
     }
   	 else if(!isValidPassWord(pwd))
   	 {
       document.getElementById(pspan).innerHTML="<font color='red'>6-20个字符,由字母数字或下划线组成</font>";
       return false;
     }
     else{
       document.getElementById(pspan).innerHTML="<font color='green'>OK</font>";
       var repwd = document.getElementById(rpid).value;
       if(repwd == pwd) document.getElementById(rpspan).innerHTML = "<font color='green'>OK</font>";
	}
     return true;
 }
  function checkEmail(id,span)
 {
 	var email = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	var emailspan = document.getElementById(span);
 	 if(email.length==0)
 	 {
       emailspan.innerHTML = "<font color=red>邮箱不能为空！</font>";
       return false;
     }
   	 else if(!isValidEmail(email))
   	 {
       emailspan.innerHTML = "<font color=red>邮箱不合格！</font>";
       return false;
     }
     else 
       emailspan.innerHTML = "<font color=green>OK</font>";
       
     return true;
 }
 
 function checkValidName(id,span)
 {
 	var username = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	var namespan = document.getElementById(span);
 	if(username.length==0)
 	{
 		namespan.innerHTML = "<font color=red>用户名不能为空</font>";
 		return false;
 	}
 	else if(!isValidAlphaOrDigit(username))
 	{
 		namespan.innerHTML = "<font color=red>只能输入4-20个以字母开头、可带数字、“_”、“.”的字串 </font>";
 		return false;
 	}
 	else
 	{
 		namespan.innerHTML = "<font color=green>OK</font>";
 		return true;
 	}
 }
 
  function checkValidNameNoOK(id,span)
 {
 	var username = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	var namespan = document.getElementById(span);
 	if(username.length==0)
 	{
 		namespan.innerHTML = "<font color=red>用户名不能为空</font>";
 		return false;
 	}
 	else if(!isValidAlphaOrDigit(username))
 	{
 		namespan.innerHTML = "<font color=red>只能输入4-20个以字母开头、可带数字、“_”、“.”的字串 </font>";
 		return false;
 	}
 	else
 	{
 		namespan.innerHTML = "";
 		return true;
 	}
 }
 
 function checkName(id,span)
 {
 	var name = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
 	var namespan = document.getElementById(span);
 	if(name.length==0)
 	{
 		namespan.innerHTML = "<font color=red>名称不能为空</font>";
 		return false;
 	}
 	else if(!isValidName(name))
 	{
 		namespan.innerHTML = "<font color=red>只能输入2-36个中文、英文字母、“.”或空格的字串</font>";
 		return false;
 	}
 	else
 	{
 		namespan.innerHTML = "<font color=green>OK</font>";
 		return true;
 	}
 }
 
function checkDate(id,span)
{
	var date = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	var span = document.getElementById(span);
	if(date==""){
		span.innerHTML = "<font color=red>日期不能为空</font>";
		return false;
	}else{
		if(isValidDate(date))
		{
			span.innerHTML = "<font color=green>OK</font>";
			return true;
		}
		else{
			span.innerHTML = "<font color=red>日期格式必须是yyyy-MM-dd，并请留意日期范围</font>";
			return false;
		}	   
	}
}

function checkDateAccurate(id,span)
{
	var date = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	var span = document.getElementById(span);
	if(date==""){
		span.innerHTML = "<font color=red>日期不能为空</font>";
		return false;
	}else{
		if(isValidDateAccurate(date))
		{
			span.innerHTML = "<font color=green>OK</font>";
			return true;
		}
		else{
			span.innerHTML = "<font color=red>日期格式是yyyy-MM-dd HH:mm，留意范围</font>";
			return false;
		}	   
	}
}

function checkDateAllowEmpty(id,span)
{
	var date = document.getElementById(id).value;
	if(date=="") return true;
	date = date.replace(/(^\s*)|(\s*$)/g, "");
	var span = document.getElementById(span);
	if(isValidDate(date))
	{
		span.innerHTML = "<font color=green>OK</font>";
		return true;
	}
	else{
		span.innerHTML = "<font color=red>日期格式是yyyy-MM-dd，并请留意日期范围</font>";
		return false;
	}
}
function checkScore(score,spanElement)
{
	if(score=="" || !isValidFloat(score)){
		spanElement.innerHTML = "<font color='red'>格式有误</font>";
		return false;
	}else{
		if(score>1000 || score<0)
		{
			spanElement.innerHTML = "<font color='red'>范围有误</font>";
			return false;
		}
		spanElement.innerHTML = "";
		return true;
	}
}
function isValidInt(str)
{
	var pattern = /^-?\d+$/;
	if(!pattern.test(str))
 	      return false;
 	return true;
}
function isValidIntArea(str)
{
	var pattern = /^\d+\-\d+$/;
	if(!pattern.test(str))
 	      return false;
 	return true;
}
function isValidFloat(str)
{
	var pattern = /^(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*)|0)$/;
	if(!pattern.test(str))
 	      return false;
 	return true;
}
function isValidPrice(str)
{
	var pattern = /^\d+$|^\d+\.\d{1,2}$/;
	if(!pattern.test(str))
 	      return false;
 	return true;
}

function isValidWeight(str)
{
	var pattern = /^\d+[k]?[g]$|^\d+\.\d{1,4}[k]?[g]$/;
	if(!pattern.test(str))
 	      return false;
 	return true;
}
function isValidWeightArea(str)
{
	var pattern = /^(\d+[k]?[g]$|^\d+\.\d{1,4}[k]?[g])|(\d+(\.\d{1,4})?\-\d+(\.\d{1,4})?[k]?[g])$/;
	if(!pattern.test(str))
 	      return false;
 	return true;
}
function checkWeight(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidWeightArea(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		document.getElementById(span).innerHTML = "";
		return true;
	}
}
function checkWeight2(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidWeight(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		if(val.charAt(val.length-2)!='k'){
			val=val.substr(0,val.length-1);
		}else{
			val=val.substr(0,val.length-2);
		}
		if(val>10000 || val<0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>必须在0-10000之间</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}

function checkAge(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidInt(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		if(val>10000 || val<0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>年龄必须在0-10000之间</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}

function checkAgeArea(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidIntArea(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		var subval = val.split('-');
		if(subval[0]>10000 || subval[0]<0 || subval[1]>10000 || subval[1]<0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>年龄必须在0-10000之间</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}
function checkMount(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidInt(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		if(val>1000000 || val<0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>数量必须在0-1000000之间</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}

function checkPort(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidInt(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		if(val>65535 || val<0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>端口号必须在0-65535之间</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}

function checkSortId(id, span)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidInt(val)){
		document.getElementById(span).innerHTML = "<font color='red'>请输入0-65535正整数</font>";
		return false;
	}
	else{
		if(val>65535 || val<0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>请输入0-65535正整数</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}


function checkStopTime(id, span)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidInt(val)){
		document.getElementById(span).innerHTML = "<font color='red'>请输入1-100正整数</font>";
		return false;
	}
	else{
		if(val>100 || val<1)
		{
			document.getElementById(span).innerHTML = "<font color='red'>请输入1-100正整数</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}

function checkMoney(id, span, tips)
{
	var val = document.getElementById(id).value.replace(/(^\s*)|(\s*$)/g, "");
	if(val=="" || !isValidPrice(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		if(val>1000000 || val<=0)
		{
			document.getElementById(span).innerHTML = "<font color='red'>数额必须大于0，且不大于1000000</font>";
			return false;
		}
		document.getElementById(span).innerHTML = "";
		return true;
	}
}

function isValidTel(str)
{
	alert(str);
	var patrn = /(^[0-9]{3,4}\-[0-9]{3,8}$)|(^[0-9]{3,8}$)|(^\([0-9]{3,4}\)[0-9]{3,8}$)|(^0{0,1}13[0-9]{9}$)/;
 	if(!pattern.test(str))
 	      return false;
 	return true;
}

function checkTel(id, span, tips)
{
	var val = document.getElementById(id).value;
	alert(isValidTel(val));
	if(!isValidTel(val)){
		document.getElementById(span).innerHTML = "<font color='red'>"+tips+"</font>";
		return false;
	}
	else{
		document.getElementById(span).innerHTML = "";
		return true;
	}
}


function setTipsOK(spanId, tips){
	document.getElementById(spanId).innerHTML = "<font color='green'>" +tips+ "</font>";
}
function setTipsWarn(spanId, tips){
	document.getElementById(spanId).innerHTML = "<font color='red'>" +tips+ "</font>";
}
function setTipsClean(spanId){
	document.getElementById(spanId).innerHTML = "";
}
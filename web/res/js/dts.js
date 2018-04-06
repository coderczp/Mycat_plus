Date.prototype.Format = function(fmt){ 
	  var o = {   
	    "M+" : this.getMonth()+1,                 // 月份
	    "d+" : this.getDate(),                    // 日
	    "h+" : this.getHours(),                   // 小时
	    "m+" : this.getMinutes(),                 // 分
	    "s+" : this.getSeconds(),                 // 秒
	    "q+" : Math.floor((this.getMonth()+3)/3), // 季度
	    "S"  : this.getMilliseconds()             // 毫秒
	  };   
	  if(/(y+)/.test(fmt)){
		  fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));   
	  }   
	  for(var k in o)   {
		  if(new RegExp("("+ k +")").test(fmt))   {
			  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));  
		  }
	  }
	  return fmt;   
} 

jQuery.ajaxSetup({error:function(xhr,status,error){
	alert('Ajax调用出错,请刷新,'+error);
	return false;
}});

jQuery.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
    	var name = this.name;
    	var val = this.value;
    	if(val){
    		if (o[name]) {
    			if (!o[name].push) {
    				o[name] = [o[name]];
    			}
    			o[name].push(val.trim());
    		} else {
    			o[name] = val.trim();
    		}
    	}
    });
    return o;
};


Date.fromLong = function(val){ 
	return new Date(val).Format('MM-dd hh:mm:ss');
}

JSON.toStr = function(json){
	return JSON.stringify(json)
}
JSON.toJson = function(str){
	return JSON.parse(str)
}


var HTML = {
	td:	 function(d){	
		return "<td>" + d + "</td>"
	},
	li : function(name, data){	
		return "<li class='list-group-item'>" +  name + ":" + data + "</li>";
	},
	a : function(href, label){	
		return "<a target='_blank' href='"+href+"'>"+ label + "</a>";
	},
	p: function(text){
		return "<p>"+text+"</p>";
	},
	span : function(text){	
		return "<span>"+ text + "</span>";
	},
	ta:function(label,row,col,val){
		return label+"<textarea rows='"+row+"' cols='"+col+"'>" + val+ "</textarea>";
	},
	ab:function(func,label){
		return "<a href='javascript:void(0)' onclick='"+func+"'>"+label+"</a>";
	},
	div:function(html){
		return "<div>"+html+"</div>";
	},
	code:function(code){
		// return "<p><code>"+code+"</code>"
		return '<textarea class="comments" readonly=readonly>'+code+'</textarea>';
	},
	option:function(val,text){
		return "<option value='"+val+"'>" + text+ "</option>";
	},
	select:function(id,datas,key,val,isAddAll){
		var select = $(id);
		if(isAddAll){
			select.append(HTML.option('','---ALL--'));
		}
		var uniq = {};
		for (var i = 0; i < datas.length; i++) {
			var app = datas[i];
			var keys = app[key];
			var vals = app[val];
			if(uniq[keys]){
				// 去重
				continue;
			}
			uniq[keys] = true;
			
			select.append(HTML.option(keys,vals));
		}
		select.selectpicker('val', '');
		select.selectpicker('refresh');
	},
	args: function(name) {
	    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	    var r = window.location.search.substr(1).match(reg);
	    if (r != null) return unescape(r[2]);
	}
	
};

var DTS = {
    menus:function(id){
    	var html = '<div class="container-fluid">'+
			'<div>'+
				'<ul class="nav navbar-nav">'+
					'<li class="active"><a href="/">Mysql-sharding</a></li>'+
					'<li><a href="/res/mysql.html">Mysql配置</a></li>'+
					'<li><a href="/res/db.html">Database管理</a></li>'+
					'<li><a href="/res/table.html">Table管理</a></li>'+
				'</ul>'+
			'</div>'+
       '</div>';
    	$(id).html(html);
    },
    substr:function(str,len){
    	if(!str ||str.length<=len)
    		return str;
    	return str.substr(0,len)+"...";
    },
    subsuf:function(str,len){
    	if(!str ||str.length<=len)
    		return str;
    	return "..."+str.substr(str.length-len);
    },
    spanDetail:function(eId,info){
    		var html = "";
    		var data = info.span;
    		var args = info.args;
    		html += HTML.li("追踪ID", data.traceId);
    		html += HTML.li("时间", Date.fromLong(info.time));

    		var pid = data.pid;
    		if (pid && pid != 'null') {
    			var pa = HTML.a("./span.html?spanId=" + pid, pid);
    			html += HTML.li("父ID", pa);
    		}
    		html += HTML.li("主机", data.host);
    		html += HTML.li("工程", data.app);
    		html += HTML.li("方法", data.method);
    		html += HTML.li("耗时(ms)", data.duration);

    		if (args && args != 'null') {
    			html += HTML.li("参数",args);
    		} else {
    			html += HTML.li("参数", '未埋点');
    		}
    		var mdc = info.mdc;
    		if(mdc.indexOf('stack')>-1){
    			html += HTML.li("扩展信息", HTML.code(mdc));
    		}else{
    			html += HTML.li("扩展信息", mdc);
    		}
    		
    		var err = info.error;
    		if (err && err != 'null') {
    			html += HTML.code(info.error);
    		}
    		$(eId).html(html);
    }
};

var API = {
	navSearch:function(){
		var tid = $('#sTraceId').val();
		if(tid){
			window.open('./chain.html?tid='+tid);
		}
	},	
	query:function(arg,callback){
		$.get('/dts/api/list', arg, callback);
	},
	chain:function(tid,callback){
		$.get('/dts/api/span/chain', {
			"traceId" : tid,
		}, callback);
	},
	get:function(spanId,callback){
		$.get('/dts/api/span/get',{
			"spanId":spanId
		},callback);
	},
	listRule:function(callback,app,host,group){
		var arg = {"app":app,"host":host};
		if(group=='name'){
			arg.group=1;
		}else if(group=='name'){
			arg.group=0;
		}
		$.get('/dts/api/rule/list',arg,callback);
	},
	updateRule:function(arg,callback){
		$.get('/dts/api/rule/update', arg, callback);
	},
	listApp:function(callback,app,host,group){
		var arg = {"name":app,"host":host};
		if(group=='name'){
			arg.group=1;
		}else if(group=='name'){
			arg.group=0;
		}
		$.get('/dts/api/app/list',arg,callback);
	},
	listHost:function(callback){
		$.get('/dts/api/app/list',{"group":0},callback);
	},
	statistics:function(arg,callback){
		$.get('/dts/api/method/statistics', arg, callback);
	},
	updateApp:function(arg,callback){
		$.get('/dts/api/app/update', arg, callback);
	},
	}

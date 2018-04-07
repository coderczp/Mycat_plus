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

jQuery.fn.bindForm = function(data) {
    this.find('input,select').each(function(i,item){
    	var name = item['name'];
    	var val = data[name];
    	item['value']=val||'';
    	
    });
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
	pwd : function(text){	
		return "<span>.....</span>";
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
	select:function(id,datas,val,label,isAddAll){
		var select = $(id);
		if(isAddAll){
			select.append(HTML.option('','---ALL--'));
		}
		var uniq = {};
		for (var i = 0; i < datas.length; i++) {
			var app = datas[i];
			var text = app[label];
			var vals = app[val];
			if(uniq[text]){
				continue;
			}
			uniq[text] = true;
			select.append(HTML.option(vals,text));
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
					'<li><a href="/res/mysql.html">物理主机配置</a></li>'+
					'<li><a href="/res/db.html">逻辑主机管理</a></li>'+
					'<li><a href="/res/db.html">分片管理</a></li>'+
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
};

// class API
function API(path){
	
  var base_url = '/api';
   
   this.add = function(json,callback){
	   $.get(base_url+'/'+path+'/add', json, callback);
   };
   
   this.del = function(json,callback){
	   $.get(base_url+'/'+path+'/del', json, callback);
   };
   
   this.list = function(json,callback){
	   $.get(base_url+'/'+path+'/list', json, callback);
   };
   
   this.update = function(json,callback){
	   $.get(base_url+'/'+path+'/update', json, callback);
   };
   
}

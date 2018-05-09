Date.prototype.Format = function(fmt) {
    var o = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),// 季度
        "S": this.getMilliseconds()
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}

jQuery.ajaxSetup({
    error: function(xhr, status, error) {
        alert('调用服务出错,请刷新重试,' + error);
        return false;
    }
});

jQuery.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        var name = this.name;
        var val = this.value;
        if (val) {
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
}
;

jQuery.fn.bindData = function(data) {
    this.find('input,select,textarea').each(function(i, item) {
        var name = item['name'];
        var val = data[name];
        var tmp = $(item);
        var rval = (val == undefined) ? '' : val;
        if (tmp.hasClass('selectpicker')) {
            console.log(name, rval);
            if (typeof (rval) == 'string') {
                tmp.selectpicker('val', rval.split(","));
            } else {
                tmp.selectpicker('val', rval);
            }
        } else {
            tmp.val(rval);
        }
    });
}
;

Date.fromLong = function(val) {
    return new Date(val).Format('MM-dd hh:mm:ss');
}

JSON.toStr = function(json) {
    return JSON.stringify(json)
}
JSON.toJson = function(str) {
    return JSON.parse(str)
}

var HTML = {
    td: function(d) {
        return "<td>" + d + "</td>"
    },
    tr: function(tds) {
        var html = '<tr>';
        for (var i in tds) {
            html += HTML.td(tds[i]);
        }
        html += '</tr>'
        return html;
    },
    li: function(name, data) {
        return "<li class='list-group-item'>" + name + ":" + data + "</li>";
    },
    a: function(href, label) {
        return "<a target='_blank' href='" + href + "'>" + label + "</a>";
    },
    p: function(text) {
        return "<p>" + text + "</p>";
    },
    span: function(text) {
        return "<span>" + text + "</span>";
    },
    pwd: function(text) {
        return "<span>.....</span>";
    },
    ta: function(label, row, col, val) {
        return label + "<textarea rows='" + row + "' cols='" + col + "'>" + val + "</textarea>";
    },
    ab: function(func, label) {
        return "<a href='javascript:void(0)' onclick='" + func + "'>" + label + "</a>";
    },
    div: function(html) {
        return "<div>" + html + "</div>";
    },
    input: function(label, id, name, val) {
        return label + "<input name='" + name + "' id='" + id + " value='" + val + "''/>";
    },
    code: function(code) {
        return '<textarea class="comments" readonly=readonly>' + code + '</textarea>';
    },
    option: function(val, text) {
        return "<option value='" + val + "'>" + text + "</option>";
    },
    select: function(id, datas, val, label) {
        var select = $(id);
        var uniq = {};
        for (var i = 0; i < datas.length; i++) {
            var obj = datas[i];
            var value = obj[val];
            var text = obj[label];
            if (uniq[text]) {
                continue;
            }
            uniq[text] = true;
            select.append(HTML.option(value, text));
        }
        select.selectpicker('val', '');
        select.selectpicker('refresh');
    },
    arrayToSelect: function(id, arrays) {
        var select = $(id);
        for (var i in arrays) {
            var obj = arrays[i];
            select.append(HTML.option(obj, obj));
        }
        select.selectpicker('val', '');
        select.selectpicker('refresh');
    },
    args: function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)","i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null)
            return unescape(r[2]);
    }

};

var Menu = {
    bind: function(id) {
        var html = '<div class="container-fluid"><div><ul class="nav navbar-nav">' +
        '<li class="active"><a href="/">Mysql-sharding</a></li>' + 
        '<li><a href="/res/physicshost.html">物理主机配置</a></li>' +
        '<li><a href="/res/logichost.html">逻辑主机管理</a></li>' +
        '<li><a href="/res/shardingnode.html">分片节点管理</a></li>' +
        '<li><a href="/res/logicdb.html">逻辑库管理</a></li>' + 
        '<li><a href="/res/table.html">Table管理</a></li>' + 
        '<li><a href="/res/user.html">用户设置</a></li>' + 
        '<li><a href="/res/cluster.html">集群管理</a></li>' + 
        '</ul></div></div>';
        $(id).html(html);
    }
};

// class API
function API(path) {

    var base_url = '/api/' + path + '/';
    
    this.add = function(json, callback) {
        this.call(base_url + 'add', json, callback);
    }

    this.get = function(json, callback) {
        this.call(base_url + 'get', json, callback);
    }

    this.del = function(id, info, callback) {
        if (!confirm(info || "确认删除吗?"))
            return;

        this.call(base_url + 'del', {
            "id": id
        }, function(data, resp) {
        	callback(resp.info);
        });
    }

    this.list = function(json, callback) {
        this.call(base_url + 'list', json, callback);
    }

    this.update = function(json, callback) {
        this.call(base_url + 'update', json, callback);
    }

    this.call = function(path, argJson, callback) {
        $.post(path, argJson, function(result) {
            return (result.code != 200) ? alert(result.info) : callback(result.data, result);
        });
    }
    ;
}

var APIFactory = {
    db: new API('db'),
    user: new API('user'),
    tbl: new API('table'),
    func: new API('function'),
    ldb: new API('logicHost'),
    pdb: new API('physicsHost'),
    cluster:new API('cluster'),
    dataNode: new API('dataNode'),
};

// do on html load finish
$(function() {
    // 自动bind菜单
    Menu.bind('#dtsNav');
});

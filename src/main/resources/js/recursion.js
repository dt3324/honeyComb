function recursion(v,ms,sum){
	if(typeof(v.mark) != "undefined"){
		var idx = sum.indexOf(v.mark+'');
		if (-1 === idx) {
			sum.push(v.mark+'');
			var obj = {point:v.mark+'',count:1 + 0};
			ms.push(obj);
		}else{
			var obj = ms[idx];
			obj.count = obj.count + 1;
		}
	}
	else{
		var vs = v.rst;
		for(var x = 0; x < vs.length;x++){
			recursion(vs[x],ms,sum);
		};
	};
};
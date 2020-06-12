function(key, reducedVal){
	var vs = reducedVal.r;
	var percent = ((reducedVal.c / 
			count
	) * 100) + '%';
	var out = [];
	if(typeof(vs) != "undefined"){
		for(var i = 0; i < vs.length;i++){
			var key = reducedVal.k;
			var p = reducedVal.c;
			var o = {PointRange:vs[i].idx};
			out.push(o);
		}
		return {p:percent,r:out};
	}
};
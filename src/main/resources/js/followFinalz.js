function(key, reducedVal) {
	var hs = reducedVal.rst;
	var sum = [];
	var marks = [];
	if(typeof(hs) != "undefined"){
		for(var i = 0; i < hs.length;i++){
			recursion(hs[i],marks,sum);
		}}
	var rst = {PointsCount:marks.length,Points:marks};
	return rst;
};
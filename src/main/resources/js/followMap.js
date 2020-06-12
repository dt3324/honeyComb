function() {
	var points = 
		points.toString;			
	var simei = this.IMEI;
        var mac = this.MAC;
	for(var i = 0;i < points.length;i++){
		check(radius,simei+","+mac,i + 1,points[i],this.Bloc);
	};
};
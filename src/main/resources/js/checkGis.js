function check(dis,imei,idx,org,loc) {
	var s = distance(loc, org);
	if (s < dis) {
		emit(idx, {mark:imei,r:s});
	};
};
function(){
	for(var i = 0;i<this.value.Points.length;i++){
		emit(this.value.Points[i], {idx:this._id,c:1});
	}
};
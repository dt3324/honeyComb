function () {
    var times = dates;
    for (var i = 0; i < times.length - 1; i++) {
        if (this.Stime <= times[i] && this.Stime > times[i + 1]) {
            emit(times[i].toString(), 1);
        }
    }
}
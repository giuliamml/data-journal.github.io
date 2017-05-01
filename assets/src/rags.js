//MapRags receives 2 sets of arguments
// "el", "map", where map is a 2d array or....
// "el", "map", "n", "m", where map is a 1d array and n and m are the numbers of rows and cols...
var MapRags = function (el, map, n, m, scale){

  if (!map[0].length && (!n || !m)) {
    alert('Inititalization error. Checks the logs.');
    console.log('1D array needs number of cols and rows. Usage:');
    console.log('new MapRags(map, rows, cols)');
    return;
  }

  var canvas = document.getElementById(el);
  if (canvas.getContext) {
    this.ctx = canvas.getContext('2d');

    this.w = canvas.width;
    this.h = canvas.height;

    if (this.h/n < 1 || this.w/m < 1)
      throw 'Cell size < 1. Increase Canvas size'
  
  } else {
    throw 'Couldn\'t get canvas context';
  }

  //flatten 2d array
  if (!!map[0].length) {
    this.n = map.length;
    this.m = map[0].length; 
    this.update2D(map);
    //1d array init
  } else {
    this.n = n;
    this.m = m; 
    this.map = map;
  }

  this.c1 = [255, 255, 255];
  this.c2 = [0, 0, 255];//blue
  this.c3 = [0, 255, 0];//green
  this.c4 = [255, 255, 0];//yellow
  this.c5 = [255, 0, 0];//red

  if (scale && scale.max !== undefined && scale.min !== undefined){ 
    this.max = scale.max;
    this.min = scale.min;
    return;
  }

  //If scale is undefined, get max and min from map
  this.max = Math.max.apply(null, this.map);
  this.min = Math.min.apply(null, this.map);  
};

MapRags.prototype.update2D = function (map) {
  var map1d = [];
  this.map =  map1d.concat.apply([], map);//TODO throws errors with big 2d matrix
}

MapRags.prototype.render = function () {

  n = this.n;
  m = this.m;

  for (var i = 0; i < n; i++) {
    for (var j = 0; j < m; j++) {


      if (typeof this.map[i*this.m + j] !== "number") {
        throw 'Array element is not a number'
      }

      if (this.map[i*this.m + j] === Infinity){
        this.ctx.fillStyle = "rgb(0,0,0)";
      } else {

        var color = this.mapColor(this.map[i*this.m + j]);

        var r = color[0];
        var g = color[1];
        var b = color[2];

        this.ctx.fillStyle = "rgb(" + r + "," + g + "," + b + ")";
      }

      var dx = Math.floor(this.w / this.m);
      var dy = Math.floor(this.h / this.n);

      var y = dx * i;
      var x = dy * j;

      this.ctx.fillRect (x, y, dy, dx);

    }
  }
};

MapRags.prototype.mapColor = function(value) {

  var valueMax;
  var valueMin;
  var colorMax;
  var colorMin;

  if (value < this.max/4){
    valueMin = 0;
    valueMax = this.max/4;
    colorMax = this.c2;
    colorMin = this.c1;

  } else if (value < this.max*2/4){
    valueMin = this.max/4;
    valueMax = this.max*2/4;
    colorMax = this.c3;
    colorMin = this.c2;
  } else if (value < this.max*3/4){
    valueMin = this.max*2/4;
    valueMax = this.max*3/4;
    colorMax = this.c4;
    colorMin = this.c3;
  } else {
    valueMin = this.max*3/4;
    valueMax = this.max;
    colorMax = this.c5;
    colorMin = this.c4;
  }

  var mr = (colorMax[0] - colorMin[0])/(valueMax - valueMin);
  var mg = (colorMax[1] - colorMin[1])/(valueMax - valueMin);
  var mb = (colorMax[2] - colorMin[2])/(valueMax - valueMin);

  var br = colorMin[0] - mr * valueMin; 
  var bg = colorMin[1] - mg * valueMin;
  var bb = colorMin[2] - mb * valueMin;

  var r = Math.round( mr * value + br);
  var g = Math.round( mg * value + bg);
  var b = Math.round( mb * value + bb);

  return [r, g, b];
};  

MapRags.prototype.updateMap = function (map) {
  if (!!map[0].length)
    return this.update2D(map);//TODO update 2d breaks with many rows and cols
  
  this.map = map;
}
//            //N   NE   E  SE  S  SW   W  NW
var ncols = [ 0,   1,  1,  1, 0, -1, -1, -1];
var nrows = [ -1, -1,  0,  1, 1,  1,  0, -1];

var cols = 20;
var rows = 20;

//populate with zeros
var rosmap = rosmap(cols, rows);
var ndist = calcDist(ncols, nrows);


function initmap(cols, rows, value){

  var array = new Array(cols*rows);

  for (var i = 0;  i < array.length; i++) {
    array[i] = value;
  }

  array[Math.floor(cols/2) + Math.floor(rows/3)*cols] = 0;

  return array
}

function rosmap(cols, rows){

  var array = new Array(cols*rows);
  var r;

  for (var row = 0;  row < rows; row++) {
    for (var col = 0;  col < cols; col++) {

      if ( col > cols/3 && col < cols*2/3){
        if (row < rows*2/3 && row > rows/3){
          r = Math.random()*2;
          array[col + cols*row] = r >= 0.4 ? r : 0.4;
          continue;
        }
      }

      //cap
      r = Math.random();
      array[col + cols*row] = r >= 0.4 ? r : 0.4;

    }
  }

  return array;
}

function calcDist(ncol, nrow){
  var cellwd = cellht = 1;
  var array = [];

  for ( n = 0; n < ncol.length; n++ ){
    array[n] = Math.sqrt ( ncol[n] * cellwd * ncol[n] * cellwd + nrow[n] * cellht * nrow[n] * cellht );
  }

  return array;
}


//initialize stuff
var dumb, smart, dumb2, smart2;
$(document).ready(function () {

  if (document.getElementById('fgm-parallel')){
    var pRags = new MapRags('fgm-parallel', initmap(cols, rows, 5), rows, cols, {max: 20, min: 0});
    pRags.render();
    smart = new Smart('fgm-parallel');
  }

  if (document.getElementById('fgm-serial')){
    var sRags = new MapRags('fgm-serial', initmap(cols, rows, Infinity), rows, cols, {max: 20, min: 0});
    sRags.render();
    dumb = new Dumb('fgm-serial');
  }

  if (document.getElementById('fgm-parallel-twin')){
    var ptwinRags = new MapRags('fgm-parallel-twin', initmap(cols, rows, 5), rows, cols, {max: 20, min: 0});
    ptwinRags.render();
    smart2 = new Smart('fgm-parallel-twin');
  }

  if (document.getElementById('fgm-serial-twin')){
    var stwinRags = new MapRags('fgm-serial-twin', initmap(cols, rows, Infinity), rows, cols, {max: 20, min: 0});
    stwinRags.render();
    dumb2 = new Dumb('fgm-serial-twin');
  }
});
////////////////////
//Serial
////////////////////
var Dumb = function (el){
  this.el = el;
  this.dumbLock = false;
};

Dumb.prototype.run = function() {

  var that = this;

  if (this.dumbLock)
    return;

  this.dumbLock = true;

  var rags;
  var ignitionMap = initmap(cols, rows, Infinity);
  var tn = t = 0;

  var rags = new MapRags(this.el, ignitionMap, rows, cols, {max: 20, min: 0});

  function dumbSpatialLoop(){

    t = tn;
    tn = Infinity;


    //Spatial loop that looks for active cells, ie, cells with
    //ignition time = t
    for ( row = 0; row < rows; row++){
      for ( col = 0; col < cols; col++){
        var idx = col + cols*row;

        //Update tn, so that tn is the minimum ignition time for all cells,
        //in a given iteration
        if ( ignitionMap[idx] > t && tn > ignitionMap[idx] ){

          tn = ignitionMap[idx];
          continue;
        }

        //skips cells that already burned
        if ( ignitionMap[idx] !== t )
          continue;

        //propagate fire for all 8 neighours
        for (var n = 0; n < 8; n++){

          //neighbour index calc
          var ncol = col + ncols[n];
          var nrow = row + nrows[n];
          var nidx = ncol + nrow*cols;

          //Check if neighbour is inbound
          if ( !(nrow >= 0 && nrow < rows && ncol >= 0 && ncol < cols))
            continue;

          // skip if cell has already burned
          if ( ignitionMap[nidx] < t )
            continue;

          //Compute neighbour cell ignition time, based on the propagation speed
          // tcell = current_time + cell distance / flame_speed
          igntime = t + ndist[n] / rosmap[idx];


          //Update ignition time in the map only if the the current time is smaller
          if(igntime < ignitionMap[nidx]){
            ignitionMap[nidx] = igntime;
          }

          //Update tn
          if( igntime < tn )
            tn = igntime;
        }
      }
    }
  }

  var itt = 0;
  (function call(){
    setTimeout(function () {

      dumbSpatialLoop();
      if (itt++ % 1 === 0){

        rags.updateMap(ignitionMap);
        rags.render();
      }

      if (tn === Infinity) {
        that.dumbLock = false;
        return;
      }

      call();

    }, 100);
  })();
}

////////////////////
//Parallel
////////////////////
var Smart = function(el){
  this.el = el;
  this.smartLock = false;
}

Smart.prototype.run = function() {
  var that = this;
  if (this.smartLock)
    return;

  this.smartLock = true;

  var ignitionMap = initmap(cols, rows, 5);
  var monitor;

  var rags = new MapRags(this.el, ignitionMap, rows, cols, {max: 20, min: 0});

  function smartSpatialLoop(){

    for ( row = 0; row < rows; row++){
      for ( col = 0; col < cols; col++){
        var idx = col + cols*row;

        //skip ignition cell
        if (ignitionMap[idx] === 0)
          continue;

        var minArray = [];
        for (var n = 0; n < 8; n++){

          //neighbour index calc
          var ncol = col + ncols[n];
          var nrow = row + nrows[n];
          var nidx = ncol + nrow*cols;

          //Check if neighbour is inbound
          if ( !(nrow >= 0 && nrow < rows && ncol >= 0 && ncol < cols))
            continue;

          // compute igniton time considering that the flame moves from the neighbour to the
          // center cell
          var igntime = ignitionMap[nidx] + ndist[n] / rosmap[nidx];
          minArray.push(igntime);

        }
        //associate the minimum of the propagation times to the ignition time
        // of the center cell
        ignitionMap[idx] = Math.min.apply(null, minArray);;

      }
    }

  }

  var itt=0;
  (function call(){
    setTimeout(function () {

      smartSpatialLoop();
      if (itt++ % 1 === 0){

        rags.updateMap(ignitionMap);
        rags.render();
      }

      if (monitor === ignitionMap[0]){
        that.smartLock = false;
        return;
      }

      monitor = ignitionMap[0];

      call();

    }, 100);
  })();
}

var is_playing = false;
var counter = 0;
var wait = 0;

var before = "p";
var after = ".png";
var cmax = 10;
var speed = "play";

function next() {
    if (counter >= cmax) {
	stop();
	counter=0;
    }
    else{
	counter = (counter + 1);
    }
    update();
    if(is_playing) {
        if("play" == speed)
            setTimeout("next()", wait);
        else
            setTimeout("next()", 0);
    }
};

function switchPlaySpeed() {
    if("play" == speed)
        speed = "fast";
    else
        speed = "play";
}

function prev() {
    if (counter <= 0) {
	counter=0;
    }
    else{
	counter = counter - 1;
    }
    update();
};

function update() {
    target = document.getElementById("target");
    target.setAttribute("src", before+counter+after);
    document.getElementById("count_number").innerHTML = counter;
    var point = parseInt(counter*100/cmax);
    document.getElementById("progress").style.width=point+"%";
};

function play() {
    document.getElementById("play-button").innerHTML="stop";
    is_playing=true;
    next();
};
function stop() {
    document.getElementById("play-button").innerHTML="play";
    is_playing=false;
};

function switchPlayStop() {
    if(is_playing) stop();
    else play();
};

function draggedHandleTo(x, y) {
    var base = document.getElementById("progress-handle");
    var dx = x-down_x;
    /*alert(base.offsetLeft);
     */ var dy = y-down_y;
    /*alert(down_x);
     */ var pb = document.getElementById("progress-back");
    var width = parseInt(pb.offsetWidth);
    counter = parseInt(dx*cmax/width+down_counter);
    if(counter<0) counter=0;
    else if(counter>cmax) counter=cmax;
    update();
};

document.onmousedown = function(event) {
    return false;
};

document.onmousemove = function(event) {
    return false;
};

document.onmouseup = function(event) {
    down_flag=false;
    return false;
};

var down_flag=false;
var lastx=0;
var lasty=0;
var down_x,down_y,down_counter;
function setFrame(frame) {
    counter=frame;
    update();
};

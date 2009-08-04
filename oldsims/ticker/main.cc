#include <X11/Xlib.h>
#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include <X11/keysym.h>

void setupWindow();
bool updateWindow();
void cleanupWindow();

time_t end;
time_t now;
char* header;
int fontSize;

int main(int argc, char** argv) {
  header = 0;
  fontSize = 24;
  if (argc < 2) {
	printf("Usage: ticker hour minute [--header <text>] [--font-size <size>] \n");
	exit(1);
  }
  int hour = -1;
  int minute = -1;
  for (int i=1;i<argc;++i) {
	if (strcmp(argv[i],"--header")==0) {
	  header = argv[++i];
	}
	else if (strcmp(argv[i],"--font-size")==0) {
	  fontSize = atoi(argv[++i]);
	}
	else if (hour==-1) hour = atoi(argv[i]);
	else if (minute==-1) minute = atoi(argv[i]);
	else {
	  printf("Unrecognised option: %s",argv[i]);
	  exit(1);
	}
  }

  // Sleep until it's time to wake up
  time(&now);
  struct tm* now_time = localtime(&now);
  struct tm end_time;
  end_time.tm_sec = 0;
  end_time.tm_min = atoi(argv[2]);
  end_time.tm_hour = atoi(argv[1]);
  end_time.tm_mday = now_time->tm_mday;
  end_time.tm_mon = now_time->tm_mon;
  end_time.tm_year = now_time->tm_year;
  end_time.tm_wday = now_time->tm_wday;
  end_time.tm_yday = now_time->tm_yday;
  end_time.tm_isdst = now_time->tm_isdst;
  end = mktime(&end_time);
  printf("Now = %ld, end = %ld\n",now,end);
  printf("Sleeping until %2d:%2d, current time is %2d:%2d:%2d\n",end_time.tm_hour,end_time.tm_min,now_time->tm_hour,now_time->tm_min,now_time->tm_sec);
  setupWindow();
  int result = 0;
  while (now < end) {
	struct timespec waitTime;
	waitTime.tv_sec = 0;
	waitTime.tv_nsec = 100000000;
	nanosleep(&waitTime,0);
	time(&now);
	//	now_time = localtime(&now);
	//	printf("Sleeping until %2d:%2d, current time is %2d:%2d:%2d\n",end_time.tm_hour,end_time.tm_min,now_time->tm_hour,now_time->tm_min,now_time->tm_sec);
	// Update the display
	if (updateWindow()) {
	  result = 1;
	  break;
	}
  }
  cleanupWindow();
  exit(result);
}

Window window;
Display* display;
int screenNum;
int windowWidth;
int windowHeight;
unsigned long black;
unsigned long white;
XFontStruct* font;

void setupWindow() {
  display = XOpenDisplay(0);
  if (display) {
	screenNum = DefaultScreen(display);
	black = BlackPixel(display,screenNum);
	white = WhitePixel(display,screenNum);
	Window rootWindow = DefaultRootWindow(display);
	windowWidth = DisplayWidth(display,screenNum);
	windowHeight = DisplayHeight(display,screenNum);
	window = XCreateSimpleWindow(display,rootWindow,0,0,windowWidth,windowHeight,1,black,black);
	XMapWindow(display,window);
	XSelectInput(display,window,KeyPressMask);
	font = XLoadQueryFont(display,"-*-helvetica-*-r-*-*-24-*-*-*-*-*-*-*");
	XFlush(display);
  }
}

bool updateWindow() {
  if (display) {
	XGCValues values;
	values.cap_style = CapButt;
	values.join_style = JoinBevel;
	unsigned long mask = GCCapStyle | GCJoinStyle;
	GC gc = XCreateGC(display,window,mask,&values);
	if (gc) {
	  // Set the GC variables
	  XSetForeground(display,gc,white);
	  XSetBackground(display,gc,black);
	  XSetFillStyle(display,gc,FillSolid);
	  // Get the window details
	  XWindowAttributes windowAttributes;
	  XGetWindowAttributes(display,window,&windowAttributes);
	  // Draw the header
	  XTextItem text;
	  int textWidth;
	  int textHeight;
	  int headerHeight = 0;
	  if (header) {
		text.chars = header;
		text.nchars = strlen(header);
		text.delta = 0;
		text.font = font?font->fid:None;
		textWidth = XTextWidth(font,text.chars,text.nchars);
		textHeight = font->ascent + font->descent;
		XDrawText(display,window,gc,(windowAttributes.width-textWidth)/2,textHeight+5,&text,1);
		headerHeight = textHeight+10;
	  }
	  long int secondsRemaining = end-now;
	  char buffer[256];
	  snprintf(buffer,256,"%ld seconds remaining",secondsRemaining);
	  text.chars = buffer;
	  text.nchars = strlen(buffer);
	  text.delta = 0;
	  text.font = font?font->fid:None;
	  textWidth = XTextWidth(font,text.chars,text.nchars);
	  textHeight = font->ascent + font->descent;
	  // Black out the last display
	  int x = (windowAttributes.width-textWidth)/2-10;
	  int y = (windowAttributes.height+headerHeight)/2-10-font->ascent;
	  int width = textWidth+20;
	  int height = textHeight+20+font->ascent+font->descent;
	  XClearArea(display,window,x,y,width,height,false);
	  //	  XFillRectangle(display,window,gc,x,y,width,height);
	  XDrawText(display,window,gc,(windowAttributes.width-textWidth)/2,(windowAttributes.height+headerHeight)/2,&text,1);
	  XFlush(display);
	}
	// Check for events
	XEvent event;
	if (XCheckWindowEvent(display,window,KeyPressMask,&event)) {
	  // Was it a 'q'?
	  XKeyEvent keyEvent = event.xkey;
	  if (XKeycodeToKeysym(display,keyEvent.keycode,0)==XK_q) {
		return true;
	  }
	}
  }
  return false;
}

void cleanupWindow() {
  if (display) {
	if (font) XFreeFont(display,font);
	XCloseDisplay(display);
  }
}

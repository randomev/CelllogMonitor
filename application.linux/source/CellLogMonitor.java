import processing.core.*; 
import processing.xml.*; 

import processing.serial.*; 
import processing.net.*; 
import eeml.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class CellLogMonitor extends PApplet {

/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */






Serial myPort;  // Create object from Serial class
Server myServer;

// http://workshop.evolutionzone.com/2006/08/14/code-timerpde/
Timer timer;

int val;      // Data received from the serial port
int lf = 10;  // Linefeed
DataOut dataOutToPachube;
int response = 0;

int low_limit = 3000;
int mid_limit = 3800;
int shunt_limit = 4100;
int high_limit = 4150;

public void setup() 
{
  size(800, 400);
  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  String portName = Serial.list()[0];
  myPort = new Serial(this, portName, 9600);
  myPort.clear();
  background(255);             // Set background to white
  myServer = new Server(this, 7373); 
  dataOutToPachube = new DataOut(this,"http://api.pachube.com/v2/feeds/13021.xml","2555aa324115fb0837ed26a1ec01601ca11460be6b6229daad914ee2bf841e93");
  dataOutToPachube.addData(0,"Cell 1");
  dataOutToPachube.addData(1,"Cell 2");
  dataOutToPachube.addData(2,"Cell 3");
  dataOutToPachube.addData(3,"Cell 4");
  dataOutToPachube.addData(4,"Cell 5");
  dataOutToPachube.addData(5,"Cell 6");
  dataOutToPachube.addData(6,"Cell 7");
  dataOutToPachube.addData(7,"Cell 8");

  // 4 min + 60 sek
  timer = new Timer(60*4,60);
  
//  dataOutToPachube.update(0, 3.2);
 
//  int response = dataOutToPachube.updatePachube();
//  println("Pachube response:" + response);

}

public void draw()
{
  
  int i;
  int xadd;
  int w;
  PFont font;
  float max;
  float min;
  String message;
  
  Date now = new Date();
  timer.update();
  
  xadd = width / 10;
  w=20;

//  int sensors[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
  fill(75);                 // set fill to light gray
  // The font must be located in the sketch's 
  // "data" directory to load successfully
  font = loadFont("ArialMT-12.vlw"); 
  textFont(font);

  if ( myPort.available() > 0) {  // If data is available,
    String inBuffer = myPort.readStringUntil(lf);   

    //fill(204);                 // set fill to light gray
    
    if (inBuffer != null) {
        
        myServer.write(inBuffer); // output raw serial data for any clients
        
        float sensors[] = PApplet.parseFloat(split(inBuffer, ';'));
/*
        println(sensors[3]);
        println(sensors[4]);
        println(sensors[5]);
        println(sensors[6]);
        println(sensors[7]);
        println(sensors[8]);
        println(sensors[9]);
        println(sensors[10]);
*/
        background(255);             // Set background to white
        
        max = 0;
        min = 9999;
        int drawarea_h = 250;
        
        //
        
        fill(0xffCCCCCC);

        rect(0,height - drawarea_h, width,drawarea_h);
        
        fill(0);          // green bars for cells
        
        for (int v = 0; v<4;v++)
        {
          float y = map(v,0,5,0,drawarea_h);
          line(0,-y,width,-y);
          text(y + " V",0,-y);
        }
        
        //message = "0";
        
        // Colors as hex values: http://www.computerhope.com/htmcolor.htm
        
        for (i = 3; i<11; i++)
        {
          float y = map(sensors[i],2500,4300,0,drawarea_h);
  
          // coloring of the bars
          if (sensors[i] > low_limit && sensors[i] <= mid_limit)
            {
              fill(0xffFFFF00);          // yellow bars for cells that are between limits
            }

          if (sensors[i] > mid_limit && sensors[i] < shunt_limit)
            {
              fill(0xff00CC00);          // Green bars for cells that are not yet shunting
            }
          
          if (sensors[i] >= shunt_limit && sensors[i] < high_limit)
            {
              fill(0xffF87217);          // Orange bars for cells that are shunting
            }

          if (sensors[i] <= low_limit || sensors[i] >= high_limit)
            {
              fill(0xffFF0000);          // RED bars for cells over or under limits
            }
          
          // bar-graph          
          rect(xadd*i-xadd*3+xadd, height, w, -y);

          // sensor value
          fill(0xff000000);
          
          if (sensors[i]>2800)
          {
            text(sensors[i]/1000, xadd*i-xadd*3+xadd-w/2, height-y);
          } else {
            text(sensors[i]/1000, xadd*i-xadd*3+xadd-w/2, 0);          
          }

          if (max < sensors[i])
            max = sensors[i];
          if (min > sensors[i])
            min = sensors[i];
            
          dataOutToPachube.update(i-3, sensors[i] / 1000); 
  
//          message = message + ',' + str(sensors[i]);
        }        
        
        if (timer.fract>=1 || response<1)
        {
          response = dataOutToPachube.updatePachube();
          println("Pachube response:" + response);
          timer = new Timer(60*4,60);
        }

//        message = message;
//        myServer.write(message); // output raw serial data for any clients

        fill(75);

        println(now + ":" + inBuffer);

        text("Cell MIN",10,70);
        text("Cell MAX",10,50);
        text("Cell DELTA",10,30);

        text(min/1000 + " V",160,70);
        text(max/1000 + " V",160,50);
        text((max-min)/1000 + " V", 160,30);

        text("eCagiva",10,100);
        text("RealTime Cell Monitor - v 0.90 - updated:" + now + ", Pachube: " + response + ", timer.fract:" + timer.fract,10,120);
        text("Palonen LABS", 10,140);

        text("High limit",400,30);
        text("Shunt limit",400,50);
        text("Mid limit",400,70);
        text("Low limit",400,90);

        text(high_limit/1000.0f + " V", 550,30);
        text(shunt_limit/1000.0f + " V",550,50);
        text(mid_limit/1000.0f + " V",550,70);
        text(low_limit/1000.0f + " V",550,90);
        
        //rect(0,height - drawarea_h, width,drawarea_h);

        //fill(#FFFF00);          // yellow bars for cells that are between limits
        //rect(480, 20, 30, 10);  // high

        fill(0xff00CC00);          // Green bars for cells that are not yet shunting
        rect(480, 60, 30, 10);
        
        fill(0xffF87217);          // Orange bars for cells that are shunting
        rect(480, 40, 30, 10);

        fill(0xffFF0000);          // RED bars for cells over or under limits
        rect(480, 20, 30, 10);  // high
        rect(480, 80, 30, 10);  // low

    }
  }

}



/*

// Wiring / Arduino Code
// Code for sensing a switch status and writing the value to the serial port.

int switchPin = 4;                       // Switch connected to pin 4

void setup() {
  pinMode(switchPin, INPUT);             // Set pin 0 as an input
  Serial.begin(9600);                    // Start serial communication at 9600 bps
}

void loop() {
  if (digitalRead(switchPin) == HIGH) {  // If switch is ON,
    Serial.print(1, BYTE);               // send 1 to Processing
  } else {                               // If the switch is not ON,
    Serial.print(0, BYTE);               // send 0 to Processing
  }
  delay(100);                            // Wait 100 milliseconds
}

*/
// Timer.pde  
// Marius Watz - http://workshop.evolutionzone.com  
// Takes a duration and countdown given in seconds.  
// Returns a value in the range [0..1], 0 representing not started and  
// 1 representing time interval complete.  

class Timer {  
  public long start,countdown,duration,durationTotal,elapsed;  
  public float fract;  
  
  // Input: duration and countdown (in seconds)  
  public Timer(long _dur,long _cnt) {  
    countdown=_cnt*1000;  
    duration=_dur*1000;  
    durationTotal=duration+countdown;  
    start=System.currentTimeMillis();  
  }  
  
  public void update() {  
    if(fract>=1) return;  
    elapsed=System.currentTimeMillis()-start;  
    if(elapsed< countdown) fract=0;  
    else if(elapsed>durationTotal) fract=1;  
    else fract=(float)(elapsed-countdown)/(float)duration;  
  }  
}  
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "CellLogMonitor" });
  }
}

/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;
import processing.net.*;

Serial myPort;  // Create object from Serial class
Server myServer;

int val;      // Data received from the serial port
int lf = 10;  // Linefeed

void setup() 
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

}

void draw()
{
  
  int i;
  int xadd;
  int w;
  PFont font;
  float max;
  float min;
  String message;
  
  Date now = new Date();
  
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
        
        //myServer.write(inBuffer); // output raw serial data for any clients
        
        float sensors[] = float(split(inBuffer, ';'));
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
        
        fill(#CCCCCC);

        rect(0,height - drawarea_h, width,drawarea_h);
        
        fill(0);          // green bars for cells
        
        for (int v = 0; v<4;v++)
        {
          float y = map(v,0,5,0,drawarea_h);
          line(0,-y,width,-y);
          text(y + " V",0,-y);
        }
        
        fill(#00CC00);          // green bars for cells
        
        message = "";
        
        for (i = 3; i<11; i++)
        {
          float y = map(sensors[i],0,5000,0,drawarea_h);
          
          rect(xadd*i-xadd*3+xadd, height, w, -y);
          
          if (max < sensors[i])
            max = sensors[i];
          if (min > sensors[i])
            min = sensors[i];
  
          message = message + ',' + str(sensors[i]);
        }        
        
        message = "HTTP/1.1 200 OK\nContent-Type: text/csv\n\n" + message;
        myServer.write(inBuffer); // output raw serial data for any clients

        fill(75);

        println(now + ":" + inBuffer);

        text("Cell MIN",10,30);
        text("Cell MAX",10,50);
        text("Cell DELTA",10,70);

        text(min/1000 + " V",160,30);
        text(max/1000 + " V",160,50);
        text((max-min)/1000 + " mV", 160,70);

        text("eCagiva",10,100);
        text("RealTime Cell Monitor - updated:" + now,10,120);
        text("Palonen LABS", 10,140);

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

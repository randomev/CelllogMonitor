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

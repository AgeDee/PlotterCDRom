package hellopi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import static java.lang.Math.round;

public class Axis {
    
    final int constantResolution = 2400;  //rozdzielczość teoretyczna/stała
    
    GpioController gpio = null;
    GpioPinDigitalOutput stepPin = null;
    GpioPinDigitalOutput dirPin = null;
    GpioPinDigitalInput swt = null;

    static int axisDimension = 40; // Fixed size in [mm] - 4cm
    int stepCounter = 0;
    int resolution = 2436;  //faktyczna liczba kroków danego silnika
    boolean isCalibrated = false; //zmienna która oznacza, czy była resetowana pozycja
    
    double actualPosition = 0;
    double logicPosition = 0;

    public Axis(GpioController gpio, Pin stepPin, Pin dirPin, Pin swt, int resolution){
        this.gpio = gpio;
        this.stepPin = gpio.provisionDigitalOutputPin(stepPin, PinState.LOW);
        this.dirPin = gpio.provisionDigitalOutputPin(dirPin, PinState.LOW);
        this.swt = gpio.provisionDigitalInputPin(swt, PinPullResistance.PULL_DOWN);
        this.resolution = resolution;
        
        this.stepPin.setShutdownOptions(true, PinState.LOW);
        this.dirPin.setShutdownOptions(true, PinState.LOW);
        this.swt.setShutdownOptions(true);
    }


    public void setDirection(boolean direction){
        if(direction) 
            dirPin.high(); //ustawienie kierunku
        else 
            dirPin.low(); //ustawienie kierunku przeciwnego
    }

    public void initPos(int pulseTime){
        int safetyCounter = 6000; //jeżeli liczba pulsów przekroczy 4000, oznacza to błąd i przerwanie operacji
        dirPin.toggle();

        System.out.println("Inicjalizacja pozycji.");

        while(swt.isLow() && safetyCounter > 0){
            stepPin.high();
            stepPin.low();
            busyWaitMicros(pulseTime);
            safetyCounter--;
        }
        dirPin.toggle();
        if(safetyCounter > 0){
            isCalibrated = true;
            System.out.println("Pozycja poczatkowa ustawionia.");
        }else{
            System.out.println("Blad inicjalizacji pozycji!");
        }
    }

    public void stepForward(long delayUs){
        if(stepCounter >= 0 && stepCounter < resolution && isCalibrated){
            stepPin.high();
            stepPin.low();
            busyWaitMicros(delayUs);
            stepCounter++;
        }else{
            System.out.println("StepForward: Nie mozna wykonac kroku. Blad kalibracji lub przekroczono zakres. " + stepCounter );
        }
    }

    public void stepBackwards(long delayUs){
        if(stepCounter > 0 && stepCounter <= resolution && isCalibrated){
            dirPin.toggle();
            stepPin.high();
            stepPin.low();
            busyWaitMicros(delayUs);
            dirPin.toggle();
            stepCounter--;
        }else{
            System.out.println("StepBackwards: Nie mozna wykonac kroku. Blad kalibracji lub przekroczono zakres. " + stepCounter);
        }
    }

    public void setPos(double pos, double speed){ //pos w mm, speed w mm/s
        if(isCalibrated){
            int counterTmp = stepCounter;
            double delayTmp = (1/(double)speed)*1000000; //obliczanie prędkości
            /*
            double ratio = resolution / (float)constantResolution; //stosunek obliczony dla danego silnika
            int posTemp = (int) round(pos * ratio); //obliczanie pozycji faktyczniej dla danego silnika
            
            System.out.println("Delay przed mnozeniem: " + delayTmp + " us");
            long delay = (long) round(delayTmp / (double)ratio); //obliczanie prędkości dla danego silnika
            System.out.println("Delay po mnozeniu: " + delay + " us");
            */
 
            double stepsPerMilimeter = resolution / (double)axisDimension;
            double positionToBeSetSteps = pos * stepsPerMilimeter;
            long delay = (long) round(delayTmp / (double)stepsPerMilimeter);
            actualPosition = positionToBeSetSteps;
            logicPosition = pos;
            
            //System.out.println("Pozycja ustawiana: " + positionToBeSetSteps + " to jest: " + pos + "mm");
            if(actualPosition > stepCounter){
                for(int i = 0; i < actualPosition - counterTmp; i++){
                    stepForward(delay);
                }
            }else{
                for(int i = 0; i < counterTmp - actualPosition; i++){
                    stepBackwards(delay);
                }
            }
        }else{
            System.out.println("Pozycja nie zostala zainicjalizowana!");
            //initPos(1);
        }
    }

    public int getResolution(){
        return resolution;
    }

    public int getStepCounter(){
        return stepCounter;
    }

    public int measureResolution(){
        dirPin.toggle();

        System.out.println("Rozpoczynam pomiar rozdzielczosci.");

        int resTemp = 0;

        while(swt.isLow()){
            stepPin.pulse(1, true);
            resTemp++;
        }

        return resTemp;
    }
    
    public double getLogicPosition(){
        return logicPosition;
    }  
    
    public double getActualPosition(){
        return actualPosition;
    }
    
    public static void busyWaitMicros(long micros){
        long waitUntil = System.nanoTime() + (micros * 1_000);
        while(waitUntil > System.nanoTime()) {
            ;
        }
                
    }
}

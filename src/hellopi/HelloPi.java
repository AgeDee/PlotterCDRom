package hellopi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Lenovo
 */
public class HelloPi {
    static int stepResolution_A = 2400;
    static int stepResolution_B = 3900;
    
    public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
        System.out.println("<--Pi4J--> GPIO Control ... started.");

        // create gpio controller
        GpioController gpio = GpioFactory.getInstance();
        
        Servomotor serwo = new Servomotor(gpio);
        
        Axis osGorna = new Axis(gpio, RaspiPin.GPIO_29, RaspiPin.GPIO_25, RaspiPin.GPIO_24, stepResolution_A); //2446
        Axis osDolna = new Axis(gpio, RaspiPin.GPIO_27, RaspiPin.GPIO_28, RaspiPin.GPIO_26, stepResolution_B); //3900
        
        GpioPinDigitalOutput pwrSwitch = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, PinState.HIGH);
        
        osGorna.setDirection(false); //ustawienie kierunku
        osDolna.setDirection(true); //ustawienie kierunku
        
        serwo.penUp();
        
        osGorna.initPos(900);
        osDolna.initPos(900);  
      
        //tutaj działanie wysyłanie do interpretera linia po lini gocode
//        
//        for(int i = 0; i < 3500; i++){
//            
//            double y = i/(double)2;
//            y = y/(double)100; //zmiana na mm
//            
//            double x = i/(double)100;
//            
//            osGorna.setPos(y, 40); //oś x
//            osDolna.setPos(x, 40); //oś y
//            
//            //Axis.busyWaitMicros(100);
//        }
//        for(int i = 0; i < 2; i++){
//            for(int a = 0; a < 3600; a++){
//
//                double degrees = a/(double)10;
//                double radians = Math.toRadians(degrees);
//
//                double x = Math.sin(radians) * 10 + 20; //R = 10mm, środek w 20mm
//                double y = Math.cos(radians) * 10 + 20; //R = 10mm, środek w 20mm
//
//                osGorna.setPos(y, 40); //oś x
//                osDolna.setPos(x, 40); //oś y
//            }
//        }
        
        
        Interpreter interpreter = new Interpreter(false, osGorna, osDolna, serwo);
        File file = new File("/home/pi/gcode/kwadraty.ngc");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String text;

        while ((text = reader.readLine()) != null) {
            interpreter.interpretLine(text);
        }

        if (reader != null) {
            reader.close();
        }
        
        //////
        
        serwo.penUp();
        pwrSwitch.low();
        
        gpio.shutdown();
    }
    
}

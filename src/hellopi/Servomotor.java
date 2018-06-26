package hellopi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import static com.pi4j.wiringpi.Gpio.pwmSetClock;
import static com.pi4j.wiringpi.Gpio.pwmSetMode;
import static com.pi4j.wiringpi.Gpio.pwmSetRange;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servomotor {
    
    private GpioPinPwmOutput pwm;
    private int pwmValueUp = 160;
    private int pwmValueDown = 200;
    private int pwmValue;
    
    public Servomotor(GpioController gpio){
        pwm = gpio.provisionPwmOutputPin(RaspiPin.GPIO_01);
        pwm.setPwm(pwmValueUp);
        pwmSetMode(Gpio.PWM_MODE_MS);
        pwmSetRange(2000);
        pwmSetClock(192);
        pwmValue = pwmValueUp;
    }
    
    public void penUp() throws InterruptedException{
        
        if(pwmValue != pwmValueUp){
            for(int i = 0; i < pwmValueDown-pwmValueUp; i++){
                pwm.setPwm(pwmValueDown-i);
                Thread.sleep(50);
            }
        }
        pwm.setPwm(pwmValueUp);
        pwmValue = pwmValueUp;
    }
    
    public void penDown() throws InterruptedException{
        if(pwmValue != pwmValueDown){
            for(int i = 0; i < pwmValueDown-pwmValueUp; i++){
                pwm.setPwm(pwmValueUp+i);
                Thread.sleep(50);
            }
        }
        pwm.setPwm(pwmValueDown);
        pwmValue = pwmValueDown;
    }
    
}

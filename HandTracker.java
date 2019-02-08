/*
 * Works with Arduino sketch LeapManipulator.Servo
 * Tracks palm position and sends direction angles to Arduino servos
 */
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import com.leapmotion.leap.*;

class HandTrackerListener extends Listener {
	SerialComm serial = new SerialComm("COM6", 9600);
	int baseCurrent, wristCurrent; //Horizontal and vertical angles for base and wrist servos control in arduino
    public void onInit(Controller controller){
		try {
			serial.initialize();
			Thread.sleep(2000);	// Wait for Serial initialization
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
        System.out.println("Leap Motion Initialized");
    }

    public void onExit(Controller controller) {
    	serial.close();
        System.out.println("Leap Motion Exited");
    }

    public void onFrame(Controller controller) {
        Frame frame = controller.frame();

        //Get hands
        Vector v;
        for(Hand hand : frame.hands()) {
        	v = hand.palmPosition();
        	sendToArduino((int)v.getX(), (int)v.getY(), (int)v.getZ());
        }
    }
    
    public void sendToArduino(int x, int y, int z){
    	try {
			baseCurrent = (int)(Math.atan2(x, y) *180/Math.PI);
			wristCurrent = (int)(Math.atan2(z, y) *180/Math.PI);
			System.out.println("Base = "+baseCurrent+"\tWrist = "+wristCurrent+"\tDistance = "+y);
			serial.write("B"+baseCurrent+"W"+wristCurrent+"D"+y);
			serial.write("*");//Instruction end as agreed with arduino code
			Thread.sleep(25); //Wait for arduino and servos to process received data
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}

class HandTracker {
    public static void main(String[] args) throws Exception {
        // Create a Leap Motion listener and controller. Also starts Serial Communication with Arduino
        HandTrackerListener listener = new HandTrackerListener();
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the leap motion listener when done
        controller.removeListener(listener);
    }
}
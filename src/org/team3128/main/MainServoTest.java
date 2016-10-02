package org.team3128.main;

import org.team3128.common.NarwhalRobot;
import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controllers.ControllerExtreme3D;
import org.team3128.common.util.Log;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;

/**
 * Tests Joystick Values
 */
public class MainServoTest extends NarwhalRobot
{
	
	public ListenerManager lm;

	public Servo testServo;
	
	@Override
	protected void updateDashboard()
	{
		
	}

	@Override
	protected void constructHardware()
	{
		lm = new ListenerManager(new Joystick(1));
		
		testServo = new Servo(5);
	}

	@Override
	protected void setupListeners()
	{
		addListenerManager(lm);
		
		lm.nameControl(ControllerExtreme3D.JOYY, "MoveServo");
		
		lm.addListener("MoveServo", (double value) ->
		{
			Log.debug("MainServoTest", "Moving Servo: " + value);
			testServo.set(value);
		});
	}

	@Override
	protected void teleopInit()
	{
		
	}

	@Override
	protected void autonomousInit()
	{
		
	}
	
	

}

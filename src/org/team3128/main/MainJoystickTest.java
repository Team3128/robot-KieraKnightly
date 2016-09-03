package org.team3128.main;

import org.team3128.common.NarwhalRobot;
import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controllers.ControllerExtreme3D;
import org.team3128.common.util.Log;

import edu.wpi.first.wpilibj.Joystick;

/**
 * Tests Joystick Values
 */
public class MainJoystickTest extends NarwhalRobot
{
	
	public ListenerManager lmLeftJoy, lmRightJoy;
	public Joystick leftJoystick, rightJoystick;
	
	//joystick object for the operator interface 
	//public Joystick launchpad;
	public ListenerManager listenerManagerLaunchpad;
	

	@Override
	protected void updateDashboard()
	{
		
	}

	@Override
	protected void constructHardware()
	{
        Log.info("MainJoystickTest", "Activating the Joystick Test");
		
	}

	@Override
	protected void setupListeners()
	{
		addListenerManager(lmRightJoy);
		addListenerManager(lmLeftJoy);
		
		rightJoystick = new Joystick(1);
		leftJoystick = new Joystick(0);

		lmRightJoy = new ListenerManager(rightJoystick);	
		lmLeftJoy = new ListenerManager(leftJoystick);	
		
		lmRightJoy.nameControl(ControllerExtreme3D.TRIGGER, "FullSpeed");
		lmRightJoy.nameControl(ControllerExtreme3D.TWIST, "JoyTurn");
		lmRightJoy.nameControl(ControllerExtreme3D.JOYY, "JoyForward");

		//-----------------------------------------------------------
		// Drive code, on Logitech Extreme3D joystick
		//-----------------------------------------------------------
		lmRightJoy.addMultiListener(() ->
		{
			double joyX = .5 * lmRightJoy.getAxis("JoyTurn");
			double joyY = lmRightJoy.getAxis("JoyForward");
			
		});
		
		lmLeftJoy.addListener("JoyForward", () ->
		{	
				Log.debug("MainJoystickTest", "" + lmLeftJoy.getAxis("JoyForward"));
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

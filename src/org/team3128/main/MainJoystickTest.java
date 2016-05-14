package org.team3128.main;

import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controller.ControllerExtreme3D;
import org.team3128.common.multibot.MainClass;
import org.team3128.common.multibot.RobotTemplate;
import org.team3128.common.util.GenericSendableChooser;
import org.team3128.common.util.Log;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * Tests Joystick Values
 */
public abstract class MainJoystickTest extends MainClass
{
	
	public ListenerManager lmLeftJoy, lmRightJoy;
	public Joystick leftJoystick, rightJoystick;
	
	//joystick object for the operator interface 
	//public Joystick launchpad;
	public ListenerManager listenerManagerLaunchpad;
	
	public MainJoystickTest()
	{

		rightJoystick = new Joystick(1);
		leftJoystick = new Joystick(0);

		lmRightJoy = new ListenerManager(rightJoystick);	
		lmLeftJoy = new ListenerManager(leftJoystick);	

		//launchpad = new Joystick(2);
		
	}

	protected void initializeRobot(RobotTemplate robotTemplate)
	{	
		
		robotTemplate.addListenerManager(lmRightJoy);
		robotTemplate.addListenerManager(lmLeftJoy);
		
        Log.info("MainJoystickTest", "Activating the Joystick Test");
	}

	protected void initializeDisabled()
	{
		// clear the motor speed set in autonomous, if there was one (because the robot was manually stopped)
	}

	protected void initializeAuto()
	{
	}
	
	protected void initializeTeleop()
	{	
		//-----------------------------------------------------------
		// Drive code, on Logitech Extreme3D joystick
		//-----------------------------------------------------------
		lmRightJoy.addListener(() ->
		{
			double joyX = .5 * lmRightJoy.getRawAxis(ControllerExtreme3D.TWIST);
			double joyY = lmRightJoy.getRawAxis(ControllerExtreme3D.JOYY);
			
		}, ControllerExtreme3D.TRIGGERUP, ControllerExtreme3D.TWIST, ControllerExtreme3D.JOYY, ControllerExtreme3D.THROTTLE, ControllerExtreme3D.TRIGGERDOWN);
		
		lmLeftJoy.addListener(ControllerExtreme3D.JOYY, () ->
		{	
				Log.debug("MainJoystickTest", "" + lmLeftJoy.getRawAxis(ControllerExtreme3D.JOYY));
		});

		

	}

	@Override
	protected void addAutoPrograms(GenericSendableChooser<CommandGroup> autoChooser)
	{


	}

	@Override
	protected void updateDashboard()
	{
		
	}
	
	

}

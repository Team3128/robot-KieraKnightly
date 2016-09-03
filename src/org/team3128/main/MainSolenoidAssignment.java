package org.team3128.main;

import org.team3128.common.NarwhalRobot;
import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controltypes.Button;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;

public class MainSolenoidAssignment extends NarwhalRobot {
	
	private ListenerManager lm;

	final static int numSolenoids = 8;
	
	Solenoid[] solenoids = new Solenoid[numSolenoids];
	
	@Override
	protected void constructHardware()
	{
		lm = new ListenerManager(new Joystick(0));
		addListenerManager(lm);
		
		for(int counter = 0; counter < numSolenoids; ++counter)
		{
			solenoids[counter] = new Solenoid(counter);
		}

	}


	@Override
	protected void setupListeners() 
	{
		for(int counter = 0; counter < numSolenoids; ++counter)
		{
			Solenoid currentSolenoid = solenoids[counter];
			
			String buttonName = String.format("Button%d", counter);
			
			lm.nameControl(new Button(counter + 1), buttonName);
			
			lm.addButtonDownListener(buttonName, () -> currentSolenoid.set(true));
			lm.addButtonUpListener(buttonName, () -> currentSolenoid.set(false));

		}

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

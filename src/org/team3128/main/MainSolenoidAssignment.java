package org.team3128.main;

import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controltype.Button;
import org.team3128.common.multibot.MainClass;
import org.team3128.common.multibot.RobotTemplate;
import org.team3128.common.util.GenericSendableChooser;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.CommandGroup;

public class MainSolenoidAssignment extends MainClass {
	
	private ListenerManager lm;

	final static int numSolenoids = 8;
	
	Solenoid[] solenoids = new Solenoid[numSolenoids];
	
	@Override
	protected void initializeRobot(RobotTemplate robotTemplate)
	{
		lm = new ListenerManager(new Joystick(0));
		robotTemplate.addListenerManager(lm);
		
		for(int counter = 0; counter < numSolenoids; ++counter)
		{
			solenoids[counter] = new Solenoid(counter);
		}

	}

	@Override
	protected void addAutoPrograms(
			GenericSendableChooser<CommandGroup> autoChooser) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initializeDisabled() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDashboard() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initializeAuto() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initializeTeleop() 
	{
		for(int counter = 0; counter < numSolenoids; ++counter)
		{
			Solenoid currentSolenoid = solenoids[counter];
			lm.addListener(new Button(counter + 1, false), () -> currentSolenoid.set(true));
			lm.addListener(new Button(counter + 1, true), () -> currentSolenoid.set(false));

		}

	}

}

package org.team3128.autonomous.commands;

import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.Command;

/**
 * Command to set the position of the ball intake.
 * @author Jamie
 *
 */
public class CmdSetIntake extends Command
{
	/**
	 * 
	 */
	private final MainUnladenSwallow robot;
	boolean setToUp;
	
	
	public CmdSetIntake(MainUnladenSwallow mainUnladenSwallow, boolean up)
	{
		robot = mainUnladenSwallow;
		setToUp = up;
	}

	@Override
	protected void initialize()
	{
		
	}

	@Override
	protected void execute() {
		if(setToUp)
		{
			robot.leftIntakePiston.setPistonOn();
			robot.rightIntakePiston.setPistonOn();
		}
		else
		{
			robot.leftIntakePiston.setPistonOff();
			robot.rightIntakePiston.setPistonOff();
		}
		
		robot.intakeUp = setToUp;
	}

	@Override
	protected boolean isFinished() {
		return timeSinceInitialized() > 1;
	}

	@Override
	protected void end() {
		
	}

	@Override
	protected void interrupted() {
		
	}
	
}
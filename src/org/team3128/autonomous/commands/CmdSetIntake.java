package org.team3128.autonomous.commands;

import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.Command;

public class CmdSetIntake extends Command
{

	MainUnladenSwallow robot;
	boolean setToUp;
	
	public CmdSetIntake(MainUnladenSwallow robot, boolean setToUp)
	{
		this.robot = robot;
		this.setToUp = setToUp;
	}
	
	@Override
	protected void initialize()
	{
		
	}

	@Override
	protected void execute()
	{
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
	protected boolean isFinished()
	{
		return true;
	}

	@Override
	protected void end()
	{

	}

	@Override
	protected void interrupted()
	{

	}

}

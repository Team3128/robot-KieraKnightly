package org.team3128.autonomous.commands;

import org.team3128.main.MainUnladenSwallow;
import org.team3128.main.MainUnladenSwallow.IntakeState;

import edu.wpi.first.wpilibj.command.Command;

public class CmdMoveRollers extends Command {

	private final MainUnladenSwallow robot;
	int _msec;
	long startTime;
	//dir is for direction, false is in, true is out
	boolean in;
	
	public CmdMoveRollers(MainUnladenSwallow robot, int msec, boolean in){
		this.robot = robot;
		_msec = msec;
		in = this.in;
	}
	
	protected void initialize()
    {
		startTime = System.currentTimeMillis();
		if(!in){
			this.robot.innerRoller.setTarget(-0.7);
			this.robot.intakeSpinner.setTarget(IntakeState.OUTTAKE.motorPower);
		}else{
			this.robot.innerRoller.setTarget(0.7);
			this.robot.intakeSpinner.setTarget(IntakeState.INTAKE.motorPower);
		}
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute()
    {
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished()
    {
    	if(System.currentTimeMillis() - startTime > _msec){
    		return true;
    	}
        return false;
    }

    // Called once after isFinished returns true
    protected void end()
    {
    	this.robot.innerRoller.setTarget(0);
    	this.robot.intakeSpinner.setTarget(IntakeState.STOPPED.motorPower);
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted()
    {
    	end();
    }
}
package org.team3128.mechanisms;

import org.team3128.common.hardware.misc.Piston;
import org.team3128.common.hardware.motor.MotorGroup;
import org.team3128.common.listener.POVValue;

import edu.wpi.first.wpilibj.command.Command;

/**
 * Class for the intake arm and its two rollers
 * @author Jamie
 *
 */
public class Intake
{
	public enum RollerState
	{
		STOPPED(0, 0),
		INTAKE(.7, 1),
		OUTTAKE(-.7, -1);
		
		public final double innerRollerPower;
		public final double outerRollerPower;
		
		private RollerState(double innerPower, double outerPower)
		{
			this.innerRollerPower = innerPower;
			this.outerRollerPower = outerPower;
		}
		

	}
	
	private MotorGroup outerRoller;
	private MotorGroup innerRoller;
	
	private Piston leftIntakePiston, rightIntakePiston;
	
	private RollerState rollerState;

	private boolean intakeUp;

	public Intake(MotorGroup outerRoller, MotorGroup innerRoller, Piston leftIntakePiston, Piston rightIntakePiston)
	{
		this.outerRoller = outerRoller;
		this.innerRoller = innerRoller;
		this.leftIntakePiston = leftIntakePiston;
		this.rightIntakePiston = rightIntakePiston;
	}
	
	public void onPOVUpdate(POVValue newValue)
	{
		switch(newValue.getDirectionValue())
		{
		case 0:
			setRollerState(RollerState.STOPPED);
			break;
		case 1:
		case 2:
		case 8:
			setRollerState(RollerState.OUTTAKE);
			break;
		case 4:
		case 5:
		case 6:
			setRollerState(RollerState.INTAKE);
			break;
		}

		
	}
	
	public RollerState getRollerState()
	{
		return rollerState;
	}

	public boolean isIntakeUp()
	{
		return intakeUp;
	}
	
	public void setRollerState(RollerState state)
	{
		outerRoller.setTarget(state.outerRollerPower);
		innerRoller.setTarget(state.innerRollerPower);
		
		rollerState = state;
	}
	
	public void toggleUp()
	{
		setUp(!intakeUp);
	}
	
	public void setUp(boolean up)
	{
		if(up)
		{
			leftIntakePiston.setPistonOn();
			rightIntakePiston.setPistonOn();
		}
		else
		{	
			leftIntakePiston.setPistonOff();
			rightIntakePiston.setPistonOff();
		}
		
		intakeUp = !intakeUp;
	}
	
	public class CmdMoveRollers extends Command 
	{

		boolean in;
		
		public CmdMoveRollers(int msec, boolean in)
		{
			super(msec / 1000.0);
			this.in = in;
		}
		
		protected void initialize()
	    {
			setRollerState(in ? RollerState.INTAKE : RollerState.OUTTAKE);
	    }

	    // Called repeatedly when this Command is scheduled to run
	    protected void execute()
	    {
	    }

	    protected boolean isFinished()
	    {
	    	//wait for timeout
	    	return false;
	    }

	    protected void end()
	    {
	    	setRollerState(RollerState.STOPPED);
	    }

	    protected void interrupted()
	    {
	    	end();
	    }
	    
	}
	
	public class CmdSetIntake extends Command
	{

		boolean setToUp;
		
		public CmdSetIntake(boolean setToUp)
		{
			this.setToUp = setToUp;
		}
		
		@Override
		protected void initialize()
		{
			
		}

		@Override
		protected void execute()
		{
			setUp(setToUp);
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
}


 package org.team3128.autonomous.defensecrossers;

import org.team3128.common.autonomous.primitives.CmdDelay;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class CmdGoAcrossShovelFries extends CommandGroup{
	public CmdGoAcrossShovelFries(MainUnladenSwallow robot)
	{		 
		addSequential(robot.drive.new CmdMoveForward(105*Length.cm,4000, .6));
		

		addSequential(robot.new CmdSetIntake(false));
		

		addSequential(robot.drive.new CmdMoveForward(-5*Length.cm, 1000, .4));

		addSequential(new CmdDelay(1000));
		addSequential(robot.drive.new CmdMoveForward(230*Length.cm, 5000,.7));

		

	}
}



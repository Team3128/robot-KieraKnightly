package org.team3128.autonomous.defensecrossers;

import org.team3128.autonomous.commands.CmdSetIntake;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class CmdGoAcrossLowBar extends CommandGroup {
	/*
	    *        _
	    *       / \ 
	    *      / _ \
	    *     / | | \
	    *    /  |_|  \
	    *   /    _    \
	    *  /    (_)    \
	    * /_____________\
	    * -----------------------------------------------------
	    * UNTESTED CODE!
	    * This class has never been tried on an actual robot.
	    * It may be non or partially functional.
	    * Do not make any assumptions as to its behavior!
	    * And don't blink.  Not even for a second.
	    * -----------------------------------------------------*/
	 public CmdGoAcrossLowBar(MainUnladenSwallow robot)
	 {
		 addSequential(new CmdSetIntake(robot, false));
		 addSequential(robot.drive.new CmdMoveForward(200 * Length.cm, 6000, .6));
	 }
}

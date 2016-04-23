package org.team3128.autonomous.defensecrossers;

import org.team3128.autonomous.commands.CmdSetIntake;
import org.team3128.common.autonomous.primitives.CmdRunInParallel;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class CmdGoAcrossRoughTerrain extends CommandGroup {
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
	 public CmdGoAcrossRoughTerrain(MainUnladenSwallow robot)
	 {
		 addSequential(new CmdRunInParallel(new CmdSetIntake(robot, false), robot.gearshift.new CmdUpshift()));

		 addSequential(robot.drive.new CmdMoveForward(600 * Length.cm, 5000, true));
	 }
}

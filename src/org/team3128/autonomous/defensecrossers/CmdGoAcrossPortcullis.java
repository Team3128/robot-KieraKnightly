package org.team3128.autonomous.defensecrossers;

import org.team3128.autonomous.commands.CmdSetIntake;
import org.team3128.common.util.units.Angle;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class CmdGoAcrossPortcullis extends CommandGroup {

	 public CmdGoAcrossPortcullis(MainUnladenSwallow robot)
	 {
		 addSequential(new CmdSetIntake(robot, false));
		 addSequential(robot.backArm.new CmdMoveToAngle(3000, 200 * Angle.DEGREES));
		 
		 //addSequential(robot.drive.new CmdMoveStraightForward(-350 * Length.cm, MainUnladenSwallow.STRAIGHT_DRIVE_KP, 5000, .4));
		 //Distance was changed because didn't go far enough, probably a bad idea, original was -350 cm
		 addSequential(robot.drive.new CmdMoveForward(-600 * Length.cm, 5000, .7));

	 }
}
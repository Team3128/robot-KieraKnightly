package org.team3128.autonomous.scorers;

import org.team3128.autonomous.StrongholdStartingPosition;
import org.team3128.common.autonomous.primitives.CmdRunInParallel;
import org.team3128.common.util.enums.Direction;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * try to score the boulder in auto using vision to realign ourselves
 * @author Jamie
 *
 */
public class CmdScoreVision extends CommandGroup 
{
	MainUnladenSwallow robot;

	public CmdScoreVision(MainUnladenSwallow robot, StrongholdStartingPosition startingPosition)
	{
		this.robot = robot;
		
		addSequential(new CmdRunInParallel(robot.intake.new CmdSetIntake(false), robot.gearshift.new CmdDownshift()));
		 
		 switch(startingPosition)
		 {
		 case FAR_LEFT:
			 addSequential(robot.drive.new CmdMoveForward(6 * Length.ft, 5000, .45));
			 addSequential(robot.drive.new CmdInPlaceTurn(45, 2000, Direction.RIGHT));
			 addSequential(new CmdAlignToLowGoal(robot, 1280 / 2)); //line up the goal in the center
			 addSequential(robot.drive.new CmdMoveForward(10 * Length.ft, 7000, .45));
			 break;
		 case CENTER_LEFT:
			 addSequential(robot.drive.new CmdMoveForward(250 * Length.cm, 5000, .3));
			 addSequential(robot.drive.new CmdInPlaceTurn(45, 2000, Direction.RIGHT));
			 addSequential(robot.drive.new CmdMoveForward(75 * Length.cm, 2000, .4));
			 break;
		 case MIDDLE:
			 addSequential(robot.drive.new CmdInPlaceTurn(30, 2000, Direction.LEFT));
			 addSequential(robot.drive.new CmdMoveForward(300 * Length.cm, 5000, .3));
			 addSequential(robot.drive.new CmdInPlaceTurn(75, 3000, Direction.RIGHT));
			 addSequential(robot.drive.new CmdMoveForward(100 * Length.cm, 2000, .4));
			 break;
		 case CENTER_RIGHT:
			 addSequential(robot.drive.new CmdInPlaceTurn(30, 2000, Direction.RIGHT));
			 addSequential(robot.drive.new CmdMoveForward(350 * Length.cm, 5000, .3));
			 addSequential(robot.drive.new CmdInPlaceTurn(75, 3000, Direction.LEFT));
			 addSequential(robot.drive.new CmdMoveForward(100 * Length.cm, 2000, .4));
			 break;
		 case FAR_RIGHT:
			 addSequential(robot.drive.new CmdMoveForward(300 * Length.cm, 5000, .3));
			 addSequential(robot.drive.new CmdInPlaceTurn(45, 2000, Direction.RIGHT));
			 addSequential(robot.drive.new CmdMoveForward(50 * Length.cm, 2000, .4));
			 break;
		 }
		 

		 addSequential(robot.intake.new CmdMoveRollers(3000,true));
	}

}

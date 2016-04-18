package org.team3128.autonomous.scorers;

import org.team3128.autonomous.StrongholdStartingPosition;
import org.team3128.autonomous.commands.CmdMoveRollers;
import org.team3128.common.util.enums.Direction;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * try to score the boulder in auto using encoder-based movement
 * @author Jamie
 *
 */
public class CmdScoreEncoders extends CommandGroup 
{
	MainUnladenSwallow robot;

	public CmdScoreEncoders(MainUnladenSwallow robot, StrongholdStartingPosition startingPosition)
	{
		this.robot = robot;
		
		 
		 switch(startingPosition)
		 {
		 case FAR_LEFT:
		 addSequential(robot.drive.new CmdMoveForward(10.0  * Length.ft, 0, .6), 7000);
		 addSequential(robot.drive.new CmdInPlaceTurn(53, 5000, Direction.RIGHT));
		 addSequential(robot.drive.new CmdMoveForward(11.7  * Length.ft, 0, .6), 4000);
		 addSequential(new CmdMoveRollers(robot, 1000,true));
		 addSequential(new CmdMoveRollers(robot, 1000,false));
		 addSequential(new CmdMoveRollers(robot, 1000,true));

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
		 
	}



}

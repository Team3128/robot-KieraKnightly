package org.team3128.autonomous.programs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.team3128.autonomous.StrongholdStartingPosition;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossPortcullis;
import org.team3128.common.util.Log;
import org.team3128.common.util.units.Length;
import org.team3128.main.MainUnladenSwallow;

import edu.wpi.first.wpilibj.command.CommandGroup;
/**
 *	This auto crosses defenses and scores based on the driver's input of the field data.
 */
public class StrongholdCompositeAuto extends CommandGroup {
    
	public StrongholdCompositeAuto(MainUnladenSwallow robot)
	{		
		CommandGroup defenseCrosser = robot.defenseChooser.getSelected();
		//note: we can't instantiate the scorer class until now because it needs to know the field position
		Class<? extends CommandGroup> scorerClass = robot.scoringChooser.getSelected(); 

		StrongholdStartingPosition startingPosition = robot.fieldPositionChooser.getSelected();
		
		if(defenseCrosser != null)
		{
			Log.info("StrongholdCompositeAuto", "Running defense crosser: " + defenseCrosser.getClass().getCanonicalName());
			addSequential(defenseCrosser);
			Log.info("StrongholdCompositeAuto", "Added Defense Sequential");
						
			if(scorerClass == null)
			{
				//make the robot fit for driving

				//Jamie please fix when you read this, portcullis was changed
				
				// Temp competiton fix @ronak at Worlds
				
				if (defenseCrosser instanceof CmdGoAcrossPortcullis) {
					addSequential(robot.drive.new CmdMoveForward(50 * Length.cm, 4000, .5));
				}
				
				// addSequential(robot.drive.new CmdMoveForward(50 * Length.cm, 4000, .5));
				
				//addSequential(robot.drive.new CmdInPlaceTurn(180, 4000, Direction.LEFT));
			}
			else
			{

				
				Log.info("StrongholdCompositeAuto", "Running scorer: " + scorerClass.getCanonicalName());

				Constructor<? extends CommandGroup> ctor;
				try
				{
					
					ctor = scorerClass.getConstructor(MainUnladenSwallow.class, StrongholdStartingPosition.class);
					addSequential(ctor.newInstance(robot, startingPosition));

				}
				catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException| IllegalArgumentException| InvocationTargetException e1)
				{
					Log.recoverable("StrongholdCompositeAuto", "Could not construct second stage auto");
					e1.printStackTrace();
				}
				
			}
		}
		
	}
	
	
}

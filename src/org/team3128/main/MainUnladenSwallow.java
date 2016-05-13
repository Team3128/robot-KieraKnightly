package org.team3128.main;

import org.team3128.autonomous.StrongholdStartingPosition;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossLowBar;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossMoat;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossPortcullis;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossRamparts;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossRockWall;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossRoughTerrain;
import org.team3128.autonomous.defensecrossers.CmdGoAcrossShovelFries;
import org.team3128.autonomous.programs.StrongholdCompositeAuto;
import org.team3128.autonomous.scorers.CmdScoreEncoders;
import org.team3128.common.drive.TankDrive;
import org.team3128.common.hardware.encoder.velocity.QuadratureEncoderLink;
import org.team3128.common.hardware.lights.LightsColor;
import org.team3128.common.hardware.lights.PWMLights;
import org.team3128.common.hardware.misc.Piston;
import org.team3128.common.hardware.misc.TwoSpeedGearshift;
import org.team3128.common.hardware.motor.MotorGroup;
import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.POVValue;
import org.team3128.common.listener.controllers.ControllerExtreme3D;
import org.team3128.common.listener.controltypes.Button;
import org.team3128.common.listener.controltypes.POV;
import org.team3128.common.multibot.MainClass;
import org.team3128.common.multibot.RobotTemplate;
import org.team3128.common.util.GenericSendableChooser;
import org.team3128.common.util.Log;
import org.team3128.common.util.RobotMath;
import org.team3128.common.util.units.Length;
import org.team3128.mechanisms.Finger;
import org.team3128.testmainclasses.MainLightsTest;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Main class for our 2016 robot, the Unladen Swallow.
 */
public abstract class MainUnladenSwallow extends MainClass
{
	
	public ListenerManager lmExtreme;
	public Joystick /*leftJoystick, */rightJoystick;
	
	//joystick object for the operator interface 
	//public Joystick launchpad;
	public ListenerManager listenerManagerLaunchpad;

	
	public MotorGroup leftMotors;
	public MotorGroup rightMotors;
	public MotorGroup intakeSpinner;
	public MotorGroup innerRoller;

	public QuadratureEncoderLink leftDriveEncoder;
	public QuadratureEncoderLink rightDriveEncoder;
	public PowerDistributionPanel powerDistPanel;
	
	public CANTalon backArmMotor;
	public Finger backArm;
	
	public TankDrive drive;
	
	//public MaxSonar ultrasonic;
	public PWMLights lights;
	
	public TwoSpeedGearshift gearshift;
		
	
	Piston leftGearshiftPiston, rightGearshiftPiston;
	Piston leftIntakePiston, rightIntakePiston;
	Compressor compressor;
	
	//Air tracker code
	final static int NUMBER_OF_AIR_TANKS = 2;
	final static double AIR_PER_AIR_TANK = 574 ; //cm3
	final static double AIR_FOR_MICRO_PISTON = AIR_PER_AIR_TANK / 140; //140 extensions/retractions per tank at 40 psi
	final static double AIR_FOR_MEDIUM_PISTON = AIR_PER_AIR_TANK / 40; //40 extensions/retractions per tank at 40 psi
	final static double TOTAL_STARTING_AIR = NUMBER_OF_AIR_TANKS * AIR_PER_AIR_TANK;
	
	
	final static public double STRAIGHT_DRIVE_KP = .0005;
	static public double INTAKE_BALL_DISTANCE_THRESHOLD = .4; //voltage
	
	
	double microPistonExtensions = 0;
	double mediumPistonExtensions = 0;
	
	boolean usingBackCamera;
	
	final static double DRIVE_WHEELS_GEAR_RATIO = 1/((84/20.0) * 3);
	
	//offset from zero degrees for the heading readout
	double robotAngleReadoutOffset;
	
	final static int fingerWarningFlashWavelength = 2; // in updateDashboard() ticks
	boolean fingerWarningShowing = false;
	
	boolean intakeUp = true;
	Thread intakeSmootherThread = null;
	boolean intakeThreadRunning = false;
	
	boolean innerRollerStopEnabled = true;
	boolean innerRollerCurrentlyIntaking = false;
	boolean innerRollerBallAtMaxPos = false;
	int fingerFlashTimeLeft = fingerWarningFlashWavelength;
	
	public enum IntakeState
	{
		STOPPED(0),
		INTAKE(1),
		OUTTAKE(-1);
		public final double motorPower;
		
		private IntakeState(double motorPower)
		{
			this.motorPower = motorPower;
		}
	}
	
	IntakeState intakeState;
	
	public GenericSendableChooser<CommandGroup> defenseChooser;
	public GenericSendableChooser<StrongholdStartingPosition> fieldPositionChooser;
	
	GenericSendableChooser<LightsColor> lightsChooser;
	
	//we have to pass an argument to the constructors of these commands, so we have to instantiate them when the user presses the button.
	public GenericSendableChooser<Class<? extends CommandGroup>> scoringChooser;
	
	public MainUnladenSwallow()
	{
		defenseChooser = new GenericSendableChooser<>();
		fieldPositionChooser = new GenericSendableChooser<>();
		scoringChooser = new GenericSendableChooser<>();
		
		fieldPositionChooser.addDefault("Far Right", StrongholdStartingPosition.FAR_LEFT);
		fieldPositionChooser.addObject("Center Right", StrongholdStartingPosition.CENTER_RIGHT);
		fieldPositionChooser.addObject("Middle", StrongholdStartingPosition.MIDDLE);
		fieldPositionChooser.addObject("Center Left", StrongholdStartingPosition.CENTER_LEFT);
		fieldPositionChooser.addObject("Far Left (low bar)", StrongholdStartingPosition.FAR_LEFT);
		
		SmartDashboard.putData("Field Position Chooser", fieldPositionChooser);
		SmartDashboard.putData("Defense Chooser", defenseChooser);
		SmartDashboard.putData("Scoring Type Chooser", scoringChooser);
		
		lightsChooser = new GenericSendableChooser<>();
		lightsChooser.addDefault("Red Lights", LightsColor.red);
		lightsChooser.addDefault("Blue Lights", LightsColor.blue);

		SmartDashboard.putData("Lights Chooser", lightsChooser);

		rightJoystick = new Joystick(0);
		//leftJoystick = new Joystick(1);

		lmExtreme = new ListenerManager(rightJoystick);	
		
		//launchpad = new Joystick(2);
		//listenerManagerLaunchpad = new ListenerManager(launchpad);
//		try
//		{
//			ultrasonic = new MaxSonar(9, MaxSonar.Resolution.MM, SerialPort.Port.kOnboard);
//			ultrasonic.setAutoPing(true);
//		}
//		catch(Exception ex)
//		{
//			ex.printStackTrace();
//		}
		
	}

	protected void initializeRobot(RobotTemplate robotTemplate)
	{	
		powerDistPanel = new PowerDistributionPanel(0);

		
		CameraServer camera = CameraServer.getInstance();
		camera.setQuality(10);
		camera.startAutomaticCapture("cam0");
		
		robotTemplate.addListenerManager(lmExtreme);
		//robotTemplate.addListenerManager(listenerManagerLaunchpad);	
		
		//must run after subclass constructors
		drive = new TankDrive(leftMotors, rightMotors, leftDriveEncoder, rightDriveEncoder, 7.65 * Length.in * Math.PI, DRIVE_WHEELS_GEAR_RATIO, 28.33 * Length.in);

		gearshift.shiftToLow();
		
		//-----------------------------------------------------------
		// Teleop listeners
		//-----------------------------------------------------------
		lmExtreme.nameControl(ControllerExtreme3D.TWIST, "JoyTurn");
		lmExtreme.nameControl(ControllerExtreme3D.JOYY, "JoyForwardBackward");
		lmExtreme.nameControl(ControllerExtreme3D.THROTTLE, "DriveThrottle");
		
		lmExtreme.nameControl(new Button(4), "ClearStickyFaults");
		lmExtreme.nameControl(new Button(2), "Shift");
		lmExtreme.nameControl(new Button(8), "StartCompressor");
		lmExtreme.nameControl(new Button(9), "StopCompressor");
		lmExtreme.nameControl(new Button(10), "RaiseLowerIntake");
		lmExtreme.nameControl(new Button(7), "ResetHeadingReadout");
		lmExtreme.nameControl(new Button(5), "FingerExtend");
		lmExtreme.nameControl(new Button(6), "RaiseLowerIntake");
		lmExtreme.nameControl(new Button(11), "ZeroFinger");
		lmExtreme.nameControl(new Button(12), "RemoveFingerSoftLimit");

		lmExtreme.nameControl(new POV(0), "IntakePOV");

		
		lmExtreme.addMultiListener(() ->
		{
			double joyX = .5 * lmExtreme.getAxis("JoyTurn");
			double joyY = lmExtreme.getAxis("JoyForwardBackward");
			
			drive.arcadeDrive(joyX, joyY, -lmExtreme.getAxis("DriveThrottle"), true);
		}, "JoyTurn", "JoyForwardBackward", "DriveThrottle");
		
		lmExtreme.addButtonDownListener("ClearStickyFaults", () -> powerDistPanel.clearStickyFaults());
		
		lmExtreme.addButtonDownListener("Shift", () -> 
		{
			gearshift.shiftToOtherGear();
		
		});
		
		lmExtreme.addButtonDownListener("StartCompressor", () -> 
		{
			compressor.start();
		});
		
		lmExtreme.addButtonDownListener("StopCompressor", () -> 
		{
			compressor.stop();
		});
		
		lmExtreme.addButtonDownListener("RaiseLowerIntake", () ->
		{
			if(intakeUp)
			{

				leftIntakePiston.setPistonOff();
				rightIntakePiston.setPistonOff();
			}
			
			
			else
			{
				
				leftIntakePiston.setPistonOn();
				rightIntakePiston.setPistonOn();
			}
			
			intakeUp = !intakeUp;
			mediumPistonExtensions += 2;
		});
		
		lmExtreme.addButtonDownListener("ResetHeadingReadout", () ->
		{
			robotAngleReadoutOffset = drive.getRobotAngle();
		});
		
		lmExtreme.addButtonDownListener("FingerExtend", () -> {
			backArmMotor.set(.5);	
		});
		
		lmExtreme.addButtonDownListener("FingerRetract", () -> {
			backArmMotor.set(-.5);	
		});
		
		lmExtreme.addButtonUpListener("FingerExtend", () -> {
			backArmMotor.set(0);	
		});

		lmExtreme.addButtonUpListener("FingerRetract", () -> {
			backArmMotor.set(0);	
		});
		
		lmExtreme.addButtonDownListener("ZeroFinger", () -> {
			backArmMotor.setEncPosition(0);	
			backArmMotor.enableForwardSoftLimit(true);
		});

		lmExtreme.addButtonDownListener("RemoveFingerSoftLimit", () -> {
			backArmMotor.enableForwardSoftLimit(false);
		});
		
		lmExtreme.addListener("IntakePOV", (POVValue newValue) -> 
		{
			switch(newValue.getDirectionValue())
			{
			case 0:
				intakeSpinner.setTarget(IntakeState.STOPPED.motorPower);
				innerRoller.setTarget(0);
				
				
				innerRollerCurrentlyIntaking = false;
				break;
			case 1:
			case 2:
			case 8:
				intakeSpinner.setTarget(IntakeState.OUTTAKE.motorPower);
				
				innerRoller.setTarget(-.7);
			
				
				innerRollerCurrentlyIntaking = false;
				innerRollerBallAtMaxPos = false;
				break;
			case 4:
			case 5:
			case 6:
				intakeSpinner.setTarget(IntakeState.INTAKE.motorPower);
				
				if(innerRollerStopEnabled && innerRollerBallAtMaxPos)
				{
					innerRoller.setTarget(0);
				}
				else
				{
					innerRoller.setTarget(.7);
				}
				innerRollerCurrentlyIntaking = true;

				break;
			}

			

		});
		
		

		
        Log.info("MainUnladenSwallow", "Activating the Unladen Swallow");
        Log.info("MainUnladenSwallow", "...but which one, an African or a European?");
	}

	protected void initializeDisabled()
	{
		// clear the motor speed set in autonomous, if there was one (because the robot was manually stopped)
		drive.arcadeDrive(0, 0, 0, false);
	}

	protected void initializeAuto()
	{
		backArm.setLocked();
		backArmMotor.clearIAccum();
		
		Scheduler.getInstance().add(new StrongholdCompositeAuto(this));
		
		lights.executeSequence(MainLightsTest.lightsRainbowSequence);
	}
	
	protected void initializeTeleop()
	{	
		backArm.setForTeleop();
		backArmMotor.clearIAccum();
		backArmMotor.set(0);
		intakeSpinner.setTarget(IntakeState.STOPPED.motorPower);
		intakeState = IntakeState.STOPPED;
		
		//gearshift.shiftToHigh();
		

	}

	@Override
	protected void addAutoPrograms(GenericSendableChooser<CommandGroup> autoChooser)
	{
		//autoChooser.addObject("Test Ultrasonic Movement", new UnladenSwallowTestAuto(this));
		
		//-------------------------------------------------------------------------------

		defenseChooser.addObject("Portcullis", new CmdGoAcrossPortcullis(this));
		defenseChooser.addObject("Shovel Fries", new CmdGoAcrossShovelFries(this));
		defenseChooser.addObject("Moat", new CmdGoAcrossMoat(this));
		defenseChooser.addObject("Rock Wall", new CmdGoAcrossRockWall(this));
		defenseChooser.addDefault("Low Bar", new CmdGoAcrossLowBar(this));
		defenseChooser.addObject("Rough Terrain", new CmdGoAcrossRoughTerrain(this));
		defenseChooser.addObject("Ramparts", new CmdGoAcrossRamparts(this));

		defenseChooser.addObject("No Crossing", null);


		scoringChooser.addDefault("No Scoring", null);
		scoringChooser.addObject("Encoder-Based (live reckoning) Scoring", CmdScoreEncoders.class);
		//scoringChooser.addObject("Ultrasonic & Encoder Scoring (experimental)", CmdScoreUltrasonic.class);


	}

	@Override
	protected void updateDashboard()
	{
		//SmartDashboard.putNumber("Total Current: ", powerDistPanel.getTotalCurrent());
				
		SmartDashboard.putString("Current Gear", gearshift.isInHighGear() ? "High" : "Low");
		
		SmartDashboard.putNumber("Back Arm Angle:", backArm.getAngle());
		//Log.debug("MainUnladenSwallow", String.format("Back arm encoder position: %f, angle: %f", backArmMotor.getPosition(), backArm.getAngle()));
		//SmartDashboard.putNumber("Left Drive Enc Distance:", leftDriveEncoder.getDistanceInDegrees());
		
		SmartDashboard.putNumber("Robot Heading", RobotMath.normalizeAngle(drive.getRobotAngle() - robotAngleReadoutOffset));
//		SmartDashboard.putNumber("Ultrasonic Distance:", ultrasonic.getDistance());
		
		if(backArm.getAngle() < -30)
		{
			--fingerFlashTimeLeft;
			if(fingerFlashTimeLeft < 1)
			{
				fingerFlashTimeLeft = fingerWarningFlashWavelength;
				fingerWarningShowing = !fingerWarningShowing;
			}
		}
		else
		{
			fingerWarningShowing = false;
		}
		
		SmartDashboard.putString("Finger", fingerWarningShowing ? "Extended" : "");
		
		SmartDashboard.putString("Robot Mode", getRobotMode().toString().toLowerCase());
		if(getRobotMode() != RobotMode.AUTONOMOUS)
		{
			lights.setColor(lightsChooser.getSelected());	
		}
		
	}
	
public class CmdMoveRollers extends Command {

    	int _msec;
    	long startTime;
    	//dir is for direction, false is in, true is out
    	boolean dir;
    	
    	public CmdMoveRollers(int msec, boolean dir){
    		_msec = msec;
    		dir = this.dir;
    	}
    	protected void initialize()
        {
    		startTime = System.currentTimeMillis();
    		if(dir){
    			innerRoller.setTarget(-0.3);
    			intakeSpinner.setTarget(-0.3);
    		}else{
    			innerRoller.setTarget(0.3);
    			intakeSpinner.setTarget(0.3);
    		}
        }

        // Called repeatedly when this Command is scheduled to run
        protected void execute()
        {
        	if(dir){
    			innerRoller.setTarget(-0.3);
    			intakeSpinner.setTarget(-0.3);
    		}else{
    			innerRoller.setTarget(0.3);
    			intakeSpinner.setTarget(0.3);
    		}
        }

        // Make this return true when this Command no longer needs to run execute()
        protected boolean isFinished()
        {
        	if(startTime > _msec){
        		return true;
        	}
            return false;
        }

        // Called once after isFinished returns true
        protected void end()
        {
        	innerRoller.setTarget(0);
        	intakeSpinner.setTarget(0);
        }

        // Called when another command which requires one or more of the same
        // subsystems is scheduled to run
        protected void interrupted()
        {
        	
        }
    }
	/**
	 * Command to set the position of the ball intake.
	 * @author Jamie
	 *
	 */
	public class CmdSetIntake extends Command
	{
		boolean setToUp;
		
		
		public CmdSetIntake(boolean up)
		{
			setToUp = up;
		}

		@Override
		protected void initialize()
		{
			
		}

		@Override
		protected void execute() {
			if(setToUp)
			{
				leftIntakePiston.setPistonOn();
				rightIntakePiston.setPistonOn();
			}
			else
			{
				leftIntakePiston.setPistonOff();
				rightIntakePiston.setPistonOff();
			}
			
			intakeUp = setToUp;
		}

		@Override
		protected boolean isFinished() {
			return timeSinceInitialized() > 1;
		}

		@Override
		protected void end() {
			
		}

		@Override
		protected void interrupted() {
			
		}
		
	}
	
	

}

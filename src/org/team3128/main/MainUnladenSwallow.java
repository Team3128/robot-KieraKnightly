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
import org.team3128.common.NarwhalRobot;
import org.team3128.common.drive.TankDrive;
import org.team3128.common.hardware.encoder.both.QuadratureEncoder;
import org.team3128.common.hardware.lights.LightsColor;
import org.team3128.common.hardware.lights.PWMLights;
import org.team3128.common.hardware.misc.Piston;
import org.team3128.common.hardware.misc.TwoSpeedGearshift;
import org.team3128.common.hardware.motor.MotorGroup;
import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controllers.ControllerExtreme3D;
import org.team3128.common.listener.controltypes.Button;
import org.team3128.common.listener.controltypes.POV;
import org.team3128.common.util.GenericSendableChooser;
import org.team3128.common.util.Log;
import org.team3128.common.util.RobotMath;
import org.team3128.common.util.datatypes.PIDConstants;
import org.team3128.common.util.units.Length;
import org.team3128.mechanisms.Finger;
import org.team3128.mechanisms.Intake;
import org.team3128.testmainclasses.MainLightsTest;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Main class for our 2016 robot, the Unladen Swallow.
 */
public abstract class MainUnladenSwallow extends NarwhalRobot
{
	
	public ListenerManager lmLeftJoy, lmRightJoy;
	public Joystick leftJoystick, rightJoystick;
	
	//joystick object for the operator interface 
	//public Joystick launchpad;
	public ListenerManager listenerManagerLaunchpad;

	
	public MotorGroup leftMotors;
	public MotorGroup rightMotors;
	public MotorGroup intakeSpinner;
	public MotorGroup innerRoller;
	public MotorGroup winch;
	
	public QuadratureEncoder leftDriveEncoder;
	public QuadratureEncoder rightDriveEncoder;
	public PowerDistributionPanel powerDistPanel;
	
	public CANTalon backArmMotor;
	public Finger backArm;
	
	public TankDrive drive;
	
	public Intake intake;
	
	public PWMLights lights;
	
	public TwoSpeedGearshift gearshift;
		
	
	public Piston gearshiftPistons;
	public Piston leftIntakePiston, rightIntakePiston;
	public Piston grapplingHook;
	Compressor compressor;
	
	
	final static public PIDConstants STRAIGHT_DRIVE_CONST = new PIDConstants(.0005);
	static public double INTAKE_BALL_DISTANCE_THRESHOLD = .4; //voltage
	
	boolean usingBackCamera;
	
	final static double DRIVE_WHEELS_GEAR_RATIO = 1/((84/20.0) * 3);
	
	//offset from zero degrees for the heading readout
	double robotAngleReadoutOffset;
	
	final static int fingerWarningFlashWavelength = 2; // in updateDashboard() ticks
	final static int endGameFlashWaveLength = 2;
	boolean fingerWarningShowing = false;
	boolean endGameWarningShowing = false;
	
	int fingerFlashTimeLeft = fingerWarningFlashWavelength;
	int endGameFLashTimeLeft = endGameFlashWaveLength;
		
	public GenericSendableChooser<CommandGroup> defenseChooser;
	public GenericSendableChooser<StrongholdStartingPosition> fieldPositionChooser;
	
	GenericSendableChooser<LightsColor> lightsChooser;
	
	//we have to pass an argument to the constructors of these commands, so we have to instantiate them when the user presses the button.
	public GenericSendableChooser<Class<? extends CommandGroup>> scoringChooser;
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

		rightJoystick = new Joystick(1);
		leftJoystick = new Joystick(0);
		
		Joystick buddyBoxJoystick = new Joystick(2);

		lmRightJoy = new ListenerManager(rightJoystick, buddyBoxJoystick);	
		lmLeftJoy = new ListenerManager(leftJoystick);	

		//launchpad = new Joystick(2);		
		powerDistPanel = new PowerDistributionPanel();

	}
	protected void constructHardware()
	{	

		addListenerManager(lmRightJoy);
		addListenerManager(lmLeftJoy);
		//robotTemplate.addListenerManager(listenerManagerLaunchpad);	
		
		//must run after subclass constructors
		//TODO: Measure track & wheelbase
		drive = new TankDrive(leftMotors, rightMotors, leftDriveEncoder, rightDriveEncoder, 7.65 * Length.in * Math.PI, DRIVE_WHEELS_GEAR_RATIO, 28.33 * Length.in, 28 * Length.in);
		intake = new Intake(intakeSpinner, innerRoller, leftIntakePiston, rightIntakePiston);	
		
		intake.setUp(true);
		gearshift.shiftToLow();
		grapplingHook.setPistonOff();
		
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
		
        Log.info("MainUnladenSwallow", "Activating the Unladen Swallow");
        Log.info("MainUnladenSwallow", "...but which one, an African or a European?");
	}
	
	@Override
	protected void setupListeners()
	{
		//-----------------------------------------------------------
		// Teleop listeners
		//-----------------------------------------------------------
		
		lmRightJoy.nameControl(ControllerExtreme3D.TWIST, "JoyTurn");
		lmRightJoy.nameControl(ControllerExtreme3D.JOYY, "JoyForwardBackward");
		lmRightJoy.nameControl(ControllerExtreme3D.THROTTLE, "DriveThrottle");
		lmRightJoy.nameControl(new Button(4), "ClearStickyFaults");
		lmRightJoy.nameControl(new Button(2), "Shift");
		lmRightJoy.nameControl(new Button(8), "StartCompressor");
		lmRightJoy.nameControl(new Button(9), "StopCompressor");
		lmRightJoy.nameControl(new Button(10), "RaiseLowerIntake");
		lmRightJoy.nameControl(new Button(7), "ResetHeadingReadout");
		lmRightJoy.nameControl(new Button(5), "FingerExtend");
		lmRightJoy.nameControl(new Button(6), "FingerRetract");
		lmRightJoy.nameControl(new Button(11), "ZeroFinger");
		lmRightJoy.nameControl(new Button(12), "RemoveFingerSoftLimit");
		
		lmLeftJoy.nameControl(new Button(1), "FireHook");
		lmLeftJoy.nameControl(ControllerExtreme3D.JOYY, "Winch");


		lmRightJoy.nameControl(new POV(0), "IntakePOV");

		
		lmRightJoy.addMultiListener(() ->
		{
			double joyX = .5 * lmRightJoy.getAxis("JoyTurn");
			double joyY = lmRightJoy.getAxis("JoyForwardBackward");
			
			drive.arcadeDrive(joyX, joyY, -lmRightJoy.getAxis("DriveThrottle"), true);
		}, "JoyTurn", "JoyForwardBackward", "DriveThrottle");
		
		lmRightJoy.addListener("ClearStickyFaults", () ->
		{
			powerDistPanel.clearStickyFaults();
		});
		
		lmRightJoy.addListener("Shift", () -> 
		{
			gearshift.shiftToOtherGear();
		
		});
		
		lmRightJoy.addListener("StartCompressor", () -> 
		{
			compressor.start();
		});
		
		lmRightJoy.addListener("StopCompressor", () -> 
		{
			compressor.stop();
		});
		
		lmRightJoy.addButtonDownListener("RaiseLowerIntake", intake::toggleUp);
		
		lmRightJoy.addListener("ResetHeadingReadout", () ->
		{
			robotAngleReadoutOffset = drive.getRobotAngle();
		});
		
		lmRightJoy.addButtonDownListener("FingerExtend", () -> {
			backArmMotor.set(.5);	
		});
		lmRightJoy.addButtonUpListener("FingerExtend", () -> {
			backArmMotor.set(0);	
		});
		
		lmRightJoy.addButtonDownListener("FingerRetract", () -> {
			backArmMotor.set(-.5);	
		});
		lmRightJoy.addButtonUpListener("FingerRetract", () -> {
			backArmMotor.set(0);	
		});
		
		lmRightJoy.addListener("ZeroFinger", () -> {
			backArmMotor.setEncPosition(0);	
			backArmMotor.enableForwardSoftLimit(true);
		});

		lmRightJoy.addListener("RemoveFingerSoftLimit", () -> {
			backArmMotor.enableForwardSoftLimit(false);
		});
		
		lmLeftJoy.addListener("FireHook", () ->
		{
			grapplingHook.setPistonInvert();
			Log.debug("MUS", "Firing hook");
		});
		
		lmLeftJoy.addListener("Winch", () ->
		{
			winch.setTarget(lmLeftJoy.getAxis("Winch"));
		});
		
		lmRightJoy.addListener("IntakePOV", intake::onPOVUpdate);
	}

	@Override
	protected void disabledInit()
	{
		// clear the motor speed set in autonomous, if there was one (because the robot was manually stopped)
		drive.arcadeDrive(0, 0, 0, false);
		
	}

	@Override
	protected void autonomousInit()
	{
		backArm.setLocked();
		backArmMotor.clearIAccum();
		
		Scheduler.getInstance().add(new StrongholdCompositeAuto(this));
		
		lights.executeSequence(MainLightsTest.lightsRainbowSequence);
	}
	
	@Override
	protected void teleopInit()
	{	
		backArm.setForTeleop();
		backArmMotor.ClearIaccum();
		backArmMotor.set(0);
		intake.setRollerState(Intake.RollerState.STOPPED);
		
		gearshift.shiftToLow();
		
	}
	

	


	@Override
	protected void constructAutoPrograms(GenericSendableChooser<CommandGroup> autoChooser)
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
		
		//workaround for autonomous
		Scheduler.getInstance().add(new StrongholdCompositeAuto(this));


	}

	@Override
	protected void updateDashboard()
	{
		//SmartDashboard.putNumber("Total Current: ", powerDistPanel.getTotalCurrent());
		
		SmartDashboard.putString("Current Gear", gearshift.isInHighGear() ? "High" : "Low");
		//Log.debug("MainUnladenSwallow", String.format("Back arm encoder position: %f, angle: %f", backArmMotor.getPosition(), backArm.getAngle()));
		//SmartDashboard.putNumber("Left Drive Enc Distance:", leftDriveEncoder.getDistanceInDegrees());
		
		SmartDashboard.putNumber("Robot Heading", RobotMath.normalizeAngle(drive.getRobotAngle() - robotAngleReadoutOffset));
//		SmartDashboard.putNumber("Ultrasonic Distance:", ultrasonic.getDistance());
		
		
		if(!isAutonomous())
		{
			lights.setColor(lightsChooser.getSelected());	
		}
		
	}

}

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
	
	public QuadratureEncoderLink leftDriveEncoder;
	public QuadratureEncoderLink rightDriveEncoder;
	public PowerDistributionPanel powerDistPanel;
	
	public CANTalon backArmMotor;
	public Finger backArm;
	
	public TankDrive drive;
	
	public PWMLights lights;
	
	public TwoSpeedGearshift gearshift;
		
	
	public Piston gearshiftPistons;
	public Piston leftIntakePiston, rightIntakePiston;
	public Piston grapplingHook;
	Compressor compressor;
	
	
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
	
	public boolean intakeUp = true;
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


	@Override
	protected void constructHardware()
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
		leftJoystick = new Joystick(1);

		lmRightJoy = new ListenerManager(rightJoystick);	
		lmLeftJoy = new ListenerManager(leftJoystick);	

		//launchpad = new Joystick(2);		
		
		powerDistPanel = new PowerDistributionPanel();

		
		CameraServer camera = CameraServer.getInstance();
		camera.setQuality(10);
		camera.startAutomaticCapture("cam0");	
		
		//must run after subclass constructors
		drive = new TankDrive(leftMotors, rightMotors, leftDriveEncoder, rightDriveEncoder, 7.65 * Length.in * Math.PI, DRIVE_WHEELS_GEAR_RATIO, 28.33 * Length.in);

		gearshift.shiftToLow();
		lights.executeSequence(MainLightsTest.lightsRainbowSequence);
		grapplingHook.setPistonOff();
		
        Log.info("MainUnladenSwallow", "Activating the Unladen Swallow");
        Log.info("MainUnladenSwallow", "...but which one, an African or a European?");
	}

	@Override
	protected void setupListeners()
	{
		addListenerManager(lmRightJoy);
		addListenerManager(lmLeftJoy);
		//robotTemplate.addListenerManager(listenerManagerLaunchpad);
		
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
		
        Log.info("MainUnladenSwallow", "Activating the Unladen Swallow");
        Log.info("MainUnladenSwallow", "...but which one, an African or a European?");
	

		lmRightJoy.nameControl(new POV(0), "IntakePOV");

		
		lmRightJoy.addMultiListener(() ->
		{
			double joyX = .5 * lmRightJoy.getAxis("JoyTurn");
			double joyY = lmRightJoy.getAxis("JoyForwardBackward");
			
			drive.arcadeDrive(joyX, joyY, -lmRightJoy.getAxis("DriveThrottle"), true);
		}, "JoyTurn", "JoyForwardBackward", "DriveThrottle");
		
		
		lmRightJoy.addButtonDownListener("ClearStickyFaults", () -> powerDistPanel.clearStickyFaults());
		
		lmRightJoy.addButtonDownListener("Shift", () -> gearshift.shiftToOtherGear());
		
		lmRightJoy.addButtonDownListener("StartCompressor", () -> 
		{
			compressor.start();
		});
		
		lmRightJoy.addButtonDownListener("StopCompressor", () -> 
		{
			compressor.stop();
		});
		
		lmRightJoy.addButtonDownListener("RaiseLowerIntake", () ->
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
		
		lmRightJoy.addButtonDownListener("ResetHeadingReadout", () ->
		{
			robotAngleReadoutOffset = drive.getRobotAngle();
		});
		
		lmRightJoy.addButtonDownListener("FingerExtend", () -> {
			backArmMotor.set(.5);	
		});
		
		lmRightJoy.addButtonDownListener("FingerRetract", () -> {
			backArmMotor.set(-.5);	
		});
		
		lmRightJoy.addButtonUpListener("FingerExtend", () -> {
			backArmMotor.set(0);	
		});

		lmRightJoy.addButtonUpListener("FingerRetract", () -> {
			backArmMotor.set(0);	
		});
		
		lmRightJoy.addButtonDownListener("ZeroFinger", () -> {

			backArmMotor.setEncPosition(0);	
			backArmMotor.enableForwardSoftLimit(true);
		});

		lmRightJoy.addButtonDownListener("RemoveFingerSoftLimit", () -> {
			backArmMotor.enableForwardSoftLimit(false);
		});
		
		lmLeftJoy.addButtonDownListener("FireHook", () ->
		{
			grapplingHook.invertPiston();
		});
		
		lmLeftJoy.addListener("Winch", (double value) ->
		{
			winch.setTarget(value);
		});
		
		lmRightJoy.addListener("IntakePOV", (POVValue newValue) -> 
		{
			switch(newValue.getDirectionValue())
			{
			case 0:
				intakeSpinner.setTarget(IntakeState.STOPPED.motorPower);
				innerRoller.setTarget(0);
				
				break;
			case 1:
			case 2:
			case 8:
				intakeSpinner.setTarget(IntakeState.OUTTAKE.motorPower);
				
				innerRoller.setTarget(-.7);
				break;
			case 4:
			case 5:
			case 6:
				intakeSpinner.setTarget(IntakeState.INTAKE.motorPower);

				innerRoller.setTarget(.7);
				break;
			}

			

		});
						
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
		intakeSpinner.setTarget(IntakeState.STOPPED.motorPower);
		intakeState = IntakeState.STOPPED;
		
		//gearshift.shiftToHigh();
	}
	

	@Override
	protected void disabledInit()
	{
		// clear the motor speed set in autonomous, if there was one (because the robot was manually stopped)
		drive.arcadeDrive(0, 0, 0, false);
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
		
		if(!isAutonomous())
		{
			lights.setColor(lightsChooser.getSelected());	
		}
		
	}

}

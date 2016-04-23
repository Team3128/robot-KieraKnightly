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
import org.team3128.common.listener.controller.ControllerExtreme3D;
import org.team3128.common.listener.controltype.POV;
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
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Main class for our 2016 robot, the Unladen Swallow.
 */
public abstract class MainUnladenSwallow extends MainClass
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

		rightJoystick = new Joystick(1);
		leftJoystick = new Joystick(0);

		lmRightJoy = new ListenerManager(rightJoystick);	
		lmLeftJoy = new ListenerManager(leftJoystick);	

		//launchpad = new Joystick(2);
		
	}

	protected void initializeRobot(RobotTemplate robotTemplate)
	{	
		powerDistPanel = new PowerDistributionPanel(0);

		
		CameraServer camera = CameraServer.getInstance();
		camera.setQuality(10);
		camera.startAutomaticCapture("cam0");
		
		robotTemplate.addListenerManager(lmRightJoy);
		robotTemplate.addListenerManager(lmLeftJoy);
		//robotTemplate.addListenerManager(listenerManagerLaunchpad);	
		
		//must run after subclass constructors
		drive = new TankDrive(leftMotors, rightMotors, leftDriveEncoder, rightDriveEncoder, 7.65 * Length.in * Math.PI, DRIVE_WHEELS_GEAR_RATIO, 28.33 * Length.in);

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
		
		lights.executeSequence(MainLightsTest.lightsRainbowSequence);
	}
	
	protected void initializeTeleop()
	{	
		//-----------------------------------------------------------
		// Drive code, on Logitech Extreme3D joystick
		//-----------------------------------------------------------
		lmRightJoy.addListener(() ->
		{
			double joyX = .5 * lmRightJoy.getRawAxis(ControllerExtreme3D.TWIST);
			double joyY = lmRightJoy.getRawAxis(ControllerExtreme3D.JOYY);
			
			drive.arcadeDrive(joyX, joyY, -lmRightJoy.getRawAxis(ControllerExtreme3D.THROTTLE), true);
		}, ControllerExtreme3D.TRIGGERUP, ControllerExtreme3D.TWIST, ControllerExtreme3D.JOYY, ControllerExtreme3D.THROTTLE, ControllerExtreme3D.TRIGGERDOWN);
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN7, () ->
		{
			powerDistPanel.clearStickyFaults();
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN2, () -> 
		{
			gearshift.shiftToOtherGear();
		
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN8, () -> 
		{
			compressor.start();
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN9, () -> 
		{
			compressor.stop();
			
			//reset air counter
			// mediumPistonExtensions = 0;
			microPistonExtensions = 0;
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN10, () ->
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
			// mediumPistonExtensions += 2;
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN4, () ->
		{
			robotAngleReadoutOffset = drive.getRobotAngle();
		});
		
		lmRightJoy.addListener(() -> 
		{
			intakeSpinner.setTarget(IntakeState.OUTTAKE.motorPower);
			
			innerRoller.setTarget(-.7);
		
			
			innerRollerCurrentlyIntaking = false;
			innerRollerBallAtMaxPos = false;
			

		}, new POV(0, 8), new POV(0, 1), new POV(0, 2));
		
		lmRightJoy.addListener(() -> 
		{
			intakeSpinner.setTarget(IntakeState.STOPPED.motorPower);
			innerRoller.setTarget(0);
			
			
			innerRollerCurrentlyIntaking = false;
		}, new POV(0, 0));
		
		lmRightJoy.addListener(() -> 
		{
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



		}, new POV(0, 4), new POV(0, 5), new POV(0, 6));
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN5, () -> {
			backArmMotor.set(.5);	
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN6, () -> {
			backArmMotor.set(-.5);	
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.UP6, () -> {
			backArmMotor.set(0);	
		});

		lmRightJoy.addListener(ControllerExtreme3D.UP5, () -> {
			backArmMotor.set(0);	
		});
		
		lmRightJoy.addListener(ControllerExtreme3D.DOWN11, () -> {
			backArmMotor.setEncPosition(0);	
			backArmMotor.enableForwardSoftLimit(true);
		});

		lmRightJoy.addListener(ControllerExtreme3D.DOWN12, () -> {
			backArmMotor.enableForwardSoftLimit(false);
		});
		
		lmLeftJoy.addListener(ControllerExtreme3D.TRIGGERDOWN, () ->
		{
			grapplingHook.invertPiston();
		});
		
		lmLeftJoy.addListener(ControllerExtreme3D.JOYY, () ->
		{
			winch.setTarget(RobotMath.threshold(lmLeftJoy.getRawAxis(ControllerExtreme3D.JOYY), .20));
		});

		//-----------------------------------------------------------------
		//joystick chooser listeners
		//-----------------------------------------------------------------
		
		//switch should be plugged in to pin 3.0 on the right side of the LaunchPad
		//active high
//		listenerManagerLaunchpad.addListener(new Button(8, true), () ->
//		{
//			listenerManagerExtreme.setJoysticks(leftJoystick);
//		});
//		
//		listenerManagerLaunchpad.addListener(new Button(8, false), () ->
//		{
//			listenerManagerExtreme.setJoysticks(rightJoystick);
//		});
		
//		listenerManagerExtreme.addListener(Always.instance, () -> {
//			int red = RobotMath.clampInt(RobotMath.floor_double_int(255 * (powerDistPanel.getTotalCurrent() / 30.0)), 0, 255);
//			int green = 255 - red;
//			
//			LightsColor color = LightsColor.new8Bit(red, green, 0);
//			lights.setColor(color);
//			
//			//Log.debug("ArmAngle", armRotateEncoder.getAngle() + " degrees");
//		});

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
	
	

}

package org.team3128.main;

import org.team3128.common.hardware.encoder.velocity.QuadratureEncoderLink;
import org.team3128.common.hardware.lights.PWMLights;
import org.team3128.common.hardware.misc.Piston;
import org.team3128.common.hardware.misc.TwoSpeedGearshift;
import org.team3128.common.hardware.motor.MotorGroup;
import org.team3128.common.multibot.RobotTemplate;
import org.team3128.common.util.Log;
import org.team3128.mechanisms.Finger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Main class for our 2016 robot, the Unladen Swallow.
 */
public  class MainUnladenSwallowCompetition extends MainUnladenSwallow
{
	long teleStart = 0;

	public MainUnladenSwallowCompetition()
	{
		
		leftDriveEncoder = new QuadratureEncoderLink(0,	1, 128, false);
		rightDriveEncoder = new QuadratureEncoderLink(2, 3, 128, true);
		
		leftMotors = new MotorGroup();
		leftMotors.addMotor(new Talon(1));
		leftMotors.addMotor(new Talon(2));
		leftMotors.invert();		
		leftMotors.setSpeedScalar(1.07);
		
		rightMotors = new MotorGroup();
		rightMotors.addMotor(new Talon(3));
		rightMotors.addMotor(new Talon(4));
		rightMotors.setSpeedScalar(1.05);
		
		intakeSpinner = new MotorGroup();
		intakeSpinner.addMotor(new Talon(0));
		intakeSpinner.invert();

		
		innerRoller = new MotorGroup();
		innerRoller.addMotor(new Talon(5));
		innerRoller.invert();
		
		winch = new MotorGroup();
		winch.addMotor(new CANTalon(1));
	
		//
		grapplingHook = new Piston(new Solenoid(3), new Solenoid(5),false,false);
		grapplingHook.invertPiston();
		gearshiftPistons = new Piston(new Solenoid(2), new Solenoid(6),false,false);
		
		gearshift = new TwoSpeedGearshift(true, gearshiftPistons);

		leftIntakePiston = new Piston(new Solenoid(1), new Solenoid(4),true,false);
		rightIntakePiston = new Piston(new Solenoid(0), new Solenoid(7),true,false);
		compressor = new Compressor();		
		
		backArmMotor = new CANTalon(0);
		
		backArmMotor.setEncPosition(0);
		backArmMotor.setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Relative);
		backArmMotor.setForwardSoftLimit(0);
		backArmMotor.enableForwardSoftLimit(true);

		backArm = new Finger(backArmMotor, 0.022428);
		
		lights = new PWMLights(17, 18, 19);
		
	}
	
	@Override
	public void initializeRobot(RobotTemplate robotTemplate)
	{
		super.initializeRobot(robotTemplate);
		CameraServer camera = CameraServer.getInstance();
		camera.setQuality(10);
		camera.startAutomaticCapture("cam0");
	}
	
	@Override
	public void initializeTeleop()
	{
		super.initializeTeleop();
		teleStart = System.currentTimeMillis();

		
		CameraServer cameraTele = CameraServer.getInstance();
		cameraTele.setQuality(10);
		if (!cameraTele.isAutoCaptureStarted()) {
			cameraTele.startAutomaticCapture("cam0");
			Log.debug("MainUnladenSwallowCompetition", "Restarted Camera");
		}
		Log.debug("MainUnladenSwallowCompetition", "Restarted Camera");

	}
	
	public void updateDashboard()
	{
		super.updateDashboard();
		
		//the finger only exists on the competition robot, so this stuff has to be here.
		SmartDashboard.putNumber("Back Arm Angle:", backArm.getAngle());
		
		long timeLeft = 135 - (System.currentTimeMillis() - teleStart)/1000;
		
		
		if (timeLeft < -1) {
			SmartDashboard.putString("Time Left: ", "0");
		}
		else if (timeLeft < 20) {
			--endGameFLashTimeLeft;
			if(endGameFLashTimeLeft <= 1)
			{
				endGameFLashTimeLeft = endGameFlashWaveLength;
				endGameWarningShowing = !endGameWarningShowing;
			}
			
			SmartDashboard.putString("Time Left: ", endGameWarningShowing ? String.valueOf(timeLeft) : "");
		}
		else {
			endGameWarningShowing = false;
			SmartDashboard.putString("Time Left: ", String.valueOf(timeLeft));
		}
		
		if(backArm.getAngle() > -90 && timeLeft <= -1)
		{
			fingerFlashTimeLeft -= 1;
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
		
		SmartDashboard.putString("Cleared for Hook Launch?", fingerWarningShowing ? "|||||||||||||NO||||||||||||" : "");
		
		//SmartDashboard.putString("Cleared for Hook Launch?", fingerWarningShowing ? "|||||||||||||NO||||||||||||" : "");

	}
	
	

}

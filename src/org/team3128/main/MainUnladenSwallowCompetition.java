package org.team3128.main;

import org.team3128.common.hardware.encoder.velocity.QuadratureEncoderLink;
import org.team3128.common.hardware.lights.PWMLights;
import org.team3128.common.hardware.misc.Piston;
import org.team3128.common.hardware.misc.TwoSpeedGearshift;
import org.team3128.common.hardware.motor.MotorGroup;
import org.team3128.mechanisms.Finger;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;

/**
 * Main class for our 2016 robot, the Unladen Swallow.
 */
public  class MainUnladenSwallowCompetition extends MainUnladenSwallow
{
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
	
	

}

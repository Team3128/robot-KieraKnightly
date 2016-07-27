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
import edu.wpi.first.wpilibj.Victor;

/**
 * Main class for our 2016 practice robot, the Unladen Swallow.
 */
public  class MainUnladenSwallowPractice extends MainUnladenSwallow
{
	static final double BACK_ARM_GEAR_RATIO = 1 / 180.0;

	public MainUnladenSwallowPractice()
	{
		
		leftDriveEncoder = new QuadratureEncoderLink(0,	1, 128, false);
		rightDriveEncoder = new QuadratureEncoderLink(2, 3, 128, true);
		
		leftMotors = new MotorGroup();
		leftMotors.addMotor(new Talon(8));
		leftMotors.addMotor(new Talon(9));
		leftMotors.invert();		
		leftMotors.setSpeedScalar(1.02);
		
		rightMotors = new MotorGroup();
		rightMotors.addMotor(new Talon(0));
		rightMotors.addMotor(new Talon(1));
		
		intakeSpinner = new MotorGroup();
		intakeSpinner.addMotor(new Victor(2));
		
		innerRoller = new MotorGroup();
		innerRoller.addMotor(new Victor(3));
		innerRoller.invert();
		
		winch = new MotorGroup();
		winch.addMotor(new CANTalon(1));
	
		//
		grapplingHook = new Piston(new Solenoid(0), new Solenoid(4),true,false);
		gearshiftPistons = new Piston(new Solenoid(3), new Solenoid(5),true,false);
		
		gearshift = new TwoSpeedGearshift(true, gearshiftPistons);

		rightIntakePiston = new Piston(new Solenoid(2), new Solenoid(7),true,false);
		leftIntakePiston = new Piston(new Solenoid(1), new Solenoid(6),true,false);
		compressor = new Compressor();
		
		
		backArmMotor = new CANTalon(0);
		
		backArmMotor.setEncPosition(0);
		backArmMotor.setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Relative);
		backArmMotor.setForwardSoftLimit(0);
		backArmMotor.enableForwardSoftLimit(true);

		backArm = new Finger(backArmMotor, BACK_ARM_GEAR_RATIO);
		
		lights = new PWMLights(17, 18, 19);
	}
	
	

}

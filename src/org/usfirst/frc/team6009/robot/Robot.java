/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/


//|**************************************************************|
//|				Cyberheart 2018 First Power Up 					 |
//|					  Katarina Mavric							 |
//|																 |
//|**************************************************************|



package org.usfirst.frc.team6009.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// New Imports
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSourceType;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> chooser = new SendableChooser<>();
	
	SpeedController leftFront, leftBack, rightFront, rightBack;
	
	// Speed controller group used for new differential drive class
	SpeedControllerGroup leftChassis, rightChassis;
	
	PIDController rotationalPID;
	
	Encoder leftEncoder, rightEncoder;
	
	DifferentialDrive chassis;
	
	Joystick driver; 
	
	ADXRS450_Gyro gyroscope;
	
	double kp = 0.1;
	double ki = 0.001;
	double kd = 0.0;
	double previous_error = 0;
	double setpoint = driver.getRawAxis(4);
	double error = setpoint - gyroscope.getAngle()%360.0;
	double integral = 0 + error;
	double derivative = error - previous_error;
	double output = (kp*error) + (ki*integral) + (kd*derivative);
	
	boolean aButton, bButton, xButton, yButton, startButton, selectButton, upButton, downButton, lbumperButton, rbumperButton;
	

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		chooser.addDefault("Default Auto", kDefaultAuto);
		chooser.addObject("My Auto", kCustomAuto);
		
		SmartDashboard.putData("Auto choices", chooser);
		SmartDashboard.putData("gameData", chooser);
		
		leftFront = new Spark(0);
		leftBack = new Spark(1);
		
		rightFront = new Spark(2);
		rightBack = new Spark(3);
		
		leftEncoder = new Encoder(0, 1);
		rightEncoder = new Encoder(2, 3);
		
		leftChassis = new SpeedControllerGroup(leftFront, leftBack);
		rightChassis = new SpeedControllerGroup(rightFront, rightBack);
		
		rightChassis.setInverted(true);
		
		driver = new Joystick(0);
		
		chassis = new DifferentialDrive(leftChassis, rightChassis);
		
		gyroscope = new ADXRS450_Gyro();
		
		rotationalPID = new PIDController(kp, ki, kd, gyroscope, leftChassis);
		
		
		
	}
	
	


	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		String gameData;
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
		
		
	}
	

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
			default:
				// Put default auto code here
				break;
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		aButton = driver.getRawButton(1);
		bButton = driver.getRawButton(2);
		xButton = driver.getRawButton(3);
		yButton = driver.getRawButton(4);
		lbumperButton = driver.getRawButton(5);
		rbumperButton = driver.getRawButton(6);
		
		
		error = setpoint - gyroscope.getAngle()%360.0;
		integral = 0 + error;
		derivative = error - previous_error;
		output = (kp*error) + (ki*integral) + (kd*derivative);
		
		leftBack.set(output);
		leftFront.set(output);
		rightBack.set(-output);
		rightFront.set(-output);
		
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}

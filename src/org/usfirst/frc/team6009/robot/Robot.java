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
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SPI;
//import roborio.java.src.com.kauailabs.navx.frc.*;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.DigitalInput;

import com.kauailabs.navx.frc.AHRS;




/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */

public class Robot extends IterativeRobot implements PIDOutput {
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	final String leftSwitch = "leftSwitch";
	final String rightSwitch = "rightSwitch";
	final String leftScale = "leftScale";
	final String rightScale = "rightScale";
	final String rightAngleScale = "rightAngleScale";
	final String leftAngleScale = "leftAngleScale";
	final String rightOppositeSideScale = "rightOppositeSideScale";
	final String leftOppositeSideScale = "leftOppositeSideScale";
	final String rightOppositeSideSwitch = "rightOppositeSideSwitch";
	final String leftOppositeSideSwitch = "leftOppositeSideSwtich";
	private String autoSelected;

	
	// Constant defining encoder turns per inch of robot travel
	final static double ENCODER_COUNTS_PER_INCH = 13.49;
	//Constant defining encoder turn for the elevator lifting
	final static double ENCODER_COUNTS_PER_INCH_HEIGHT = 182.13;
	//ENCODER_COUNTS_PER_INCH_HEIGHT = 213.9
	
	// Auto Modes Setup
	String gameData;
	
	//auto cases
		public enum Step { Straight, Turn, Straight2, Turn2, SHORT_STRAIGHT, LAUNCH, Lift, Straight3, Turn3, Straight4, Straight5, Turn4, Straight6, Straight7, Elevator, Elevator2, CubeOut, CubeIn, CubeOut2,  Done }
		public Step autoStep = Step.Straight;
		public long timerStart;

	//Limit Switches
	DigitalInput limitSwitchUpElevator, limitSwitchDownElevator, limitSwitchUpClimber, limitSwitchDownClimber, limitSwitchGripper;

    SpeedController leftFront, leftBack, rightFront, rightBack, gripper, elevator1, elevator2, climber1, climber2, PIDSpeed;
	
	// Speed controller group used for new differential drive class
	SpeedControllerGroup leftChassis, rightChassis, elevator;
	
	Encoder leftEncoder, rightEncoder, elevatorEncoder;
	
	DifferentialDrive chassis;
	
	Joystick driver; 
	
	//gyroscope
	AHRS gyroscope;
	
	boolean aButton, bButton, xButton, yButton, startButton, selectButton, upButton, downButton, lbumper, rbumper, start, select, leftThumbPush, rightThumbPush;
	
		//PID Variables
		PIDController rotationPID;
		
		// CONSTANT VARIABLES FOR PID
		//double Kp = 0.075;
		double Kp = 0.03;
		double Kp2 = 0.015;
		double Ki = 0;
		//double Kd = 0.0075;
		double Kd = 0.0085;
	

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser = new SendableChooser<String>();
		m_chooser.addObject("leftSwitch", leftSwitch);
		m_chooser.addObject("rightSwitch", rightSwitch);
		m_chooser.addDefault("leftScale", leftScale);
		m_chooser.addDefault("rightScale", rightScale);
		m_chooser.addObject("rightAngleScale", rightAngleScale);
		m_chooser.addObject("leftAngleScale", leftAngleScale);
		m_chooser.addObject("rightOppositeSideScale", rightOppositeSideScale);
		m_chooser.addObject("leftOppositeSideScale", leftOppositeSideScale);
		m_chooser.addObject("rightOppositeSideSwitch", rightOppositeSideSwitch);
		m_chooser.addObject("leftOppositeSideSwitch", leftOppositeSideSwitch);
		
		//Display these choices in whatever interface we are using
		SmartDashboard.putData("Auto Selected", m_chooser);
		
		/*limitSwitchUpClimber = new DigitalInput(6);
		limitSwitchDownClimber = new DigitalInput(7);*/
		limitSwitchGripper = new DigitalInput(4);

		elevator1 = new Spark(6);
		elevator2 = new Spark(7);
		climber1 = new Spark(4);
		climber2 = new Spark(5);
		gripper = new Spark(9);
		
		elevator = new SpeedControllerGroup(elevator1, elevator2);
		
		//leftElevator = new Encoder(4, 5);
		//rightElevator = new Encoder(6, 7);
		
		elevator2.setInverted(true);
		climber2.setInverted(true);
		
		leftFront = new Spark(0);
		leftBack = new Spark(1);
		
		rightFront = new Spark(2);
		rightBack = new Spark(3);
		
		leftEncoder = new Encoder(0, 1);
		rightEncoder = new Encoder(2, 3);
		elevatorEncoder = new Encoder(8,9);
		
		leftChassis = new SpeedControllerGroup(leftFront, leftBack);
		rightChassis = new SpeedControllerGroup(rightFront, rightBack);
		
		rightChassis.setInverted(true);
		
		driver = new Joystick(0);
		
		chassis = new DifferentialDrive(leftChassis, rightChassis);
		
		gyroscope = new AHRS(SPI.Port.kMXP);
		
		
		rotationPID = new PIDController(Kp2, Ki, Kd, gyroscope, this);
		rotationPID.setSetpoint(0);
		
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
		//Assign selected modes to a variable
		autoSelected = (String) m_chooser.getSelected();
		System.out.println("Auto selected: " + autoSelected);
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		//Reset the gyro so the heading at the start of the match is 0
		resetEncoders();
		gyroscope.reset();
		autoStep = Step.Straight;
		}
	

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		if (autoSelected.equalsIgnoreCase(rightSwitch) || autoSelected.equalsIgnoreCase(leftSwitch)) {
			double distance = getDistance();
			switch (autoStep) {
			case Straight:
				if (distance < 155){
					driveStraight(0, 0.5);
				}
				else{
					stop();
					autoStep = Step.Turn;
				}
				break;
			case Turn:
				if (gameData.charAt(0) == 'R' && autoSelected == rightSwitch){
					if (turnLeft(-90)) {
						resetEncoders();
						timerStart = System.currentTimeMillis();
						autoStep = Step.SHORT_STRAIGHT;
					}
				}
				else if(gameData.charAt(0) == 'L' && autoSelected == leftSwitch){
					if (turnRight(90)) {
						resetEncoders();
						timerStart = System.currentTimeMillis();
						autoStep = Step.SHORT_STRAIGHT;
					}
				}
				else {
					stop();
					autoStep = Step.Done;
				}
				break;
			case SHORT_STRAIGHT:
				if (autoSelected == leftSwitch){
					if (distance > 30 || ((System.currentTimeMillis() - timerStart) > 1000)){
						stop();
						timerStart = System.currentTimeMillis();
						autoStep = Step.LAUNCH;
					}
					else{
						driveStraight(90, 0.4);
					}
				}
				else {
					if (distance < 30 || ((System.currentTimeMillis() - timerStart) > 1000)){
						stop();
						timerStart = System.currentTimeMillis();
						autoStep = Step.LAUNCH;
					}
					else{
						driveStraight(-90, 0.4);
					}
				}				
				break;
			case LAUNCH:
				if ((System.currentTimeMillis() - timerStart) < 2000) {
					gripper.set(1);
				}
				else{
					gripper.set(0);
					autoStep = Step.Done;
				}
				break;
			case Done:
				break;
			}
			return;
		}
		if (autoSelected.equalsIgnoreCase(leftScale) || autoSelected.equalsIgnoreCase(rightScale)) {
			double distance = getDistance();
					switch (autoStep) {
					case Straight:
						if (distance < 285) {
							driveStraight(0, 0.4);
						} else {
							stop();
							autoStep = Step.Turn;
						}
						break;
					case Turn:
						if (gameData.charAt(1) == 'L' && autoSelected == leftScale){
							if (turnRight(90)) {
								resetEncoders();
								autoStep = Step.Done;
								completeStop();
							}
						}
						else if(gameData.charAt(1) == 'R' && autoSelected == rightScale){
							if (turnLeft(-90)) {
								resetEncoders();
								completeStop();
								autoStep = Step.Done;
							}
						}
						else{
							stop();
							autoStep = Step.Done;
						}
						break;
					case CubeOut:
						if (getElevatorheight() < 75) {
							elevator.set(0.5);
						} else {
							elevator.set(0.05);
							
						}
						break;
					case Done:
						completeStop();
						break;	
			}
		return;
		}
		if (autoSelected.equalsIgnoreCase(leftAngleScale) || autoSelected.equalsIgnoreCase(rightAngleScale)) {
			double distance = getDistance();
			switch (autoStep) {
			case Straight:
				if (gameData.charAt(1) == 'R' && autoSelected == rightAngleScale){
					if (distance < 244){
						driveStraight(-4, 0.5);
					}
					else{
						stop();
						autoStep = Step.Turn;
					}
				}
				else if (gameData.charAt(1) == 'L' && autoSelected == leftAngleScale){
					if (distance < 244){
						driveStraight(4, 0.5);
					}
					else{
						stop();
						autoStep = Step.Turn;
					}
				}
				else{
					if (distance < 180){
						driveStraight(0, 0.5);
					}
					else{
						stop();
						autoStep = Step.Done;
					}
				}
				break;
			case Turn:
				if (gameData.charAt(1) == 'R' && autoSelected == rightAngleScale){
					if (turnLeft(-30)) {
						resetEncoders();
						timerStart = System.currentTimeMillis();
						autoStep = Step.Lift;
					}
				}
				else if (gameData.charAt(1) == 'L' && autoSelected == leftAngleScale){
					if (turnRight(30)) {
						resetEncoders();
						timerStart = System.currentTimeMillis();
						autoStep = Step.Lift;
					}
				}
				else{
					autoStep = Step.Done;
					}
				break;

			case Lift:
				// lift to 127 !!!
				if (getElevatorheight() < 50 || (System.currentTimeMillis() - timerStart) > 2000){
					elevator.set(-0.5);
				}
				else{
					stop();
					elevator.set(0);
					timerStart = System.currentTimeMillis();
					autoStep = Step.LAUNCH;
				}
				break;
			case LAUNCH:
				if ((System.currentTimeMillis() - timerStart) < 2000) {
					gripper.set(1);
				}
				else{
					gripper.set(0);
					autoStep = Step.Done;
				}
				break;
			case Done:
				break;
			}
			return;
		}
		if (autoSelected.equalsIgnoreCase(leftOppositeSideScale) || autoSelected.equalsIgnoreCase(rightOppositeSideScale)) {
			double distance = getDistance();
			switch (autoStep) {
			case Straight:
				if (autoSelected == leftOppositeSideScale && gameData.charAt(1) == 'L') {
					if (distance < 244){
						driveStraight(4, 0.5);
					}
					else{
						stop();
						autoStep = Step.Turn;
					}
				} else if (autoSelected == rightOppositeSideScale && gameData.charAt(1) == 'R') {
					if (distance < 244){
						driveStraight(-4, 0.5);
					}
					else{
						stop();
						autoStep = Step.Turn;
					}
				} else {
					if (distance < 207) {
						driveStraight(0, 0.5);
					} else {
						stop();
						autoStep = Step.Turn;
					}
				} 
				break;
			case Turn:
				if (autoSelected == leftOppositeSideScale && gameData.charAt(1) == 'L') {
					if (turnRight(30)) {
						resetEncoders();
						timerStart = System.currentTimeMillis();
						autoStep = Step.Lift;
					}
				} else if (autoSelected == rightOppositeSideScale && gameData.charAt(1) == 'R') {
					if (turnLeft(-30)) {
						resetEncoders();
						timerStart = System.currentTimeMillis();
						autoStep = Step.Lift;
					}
				} else if (autoSelected == leftOppositeSideScale && gameData.charAt(1) == 'R') {
					if (turnRight(90)) {
						resetEncoders();
						autoStep = Step.Straight2;
					}
				} else {
					if (turnLeft(-90)) {
						resetEncoders();
						autoStep = Step.Straight2;
					}
				}
				break;
			case Straight2:
				if (autoSelected == leftOppositeSideScale && gameData.charAt(1) == 'R') {
					if (distance < 198) {
						driveStraight(90, 0.5);
					} else {
						stop();
						autoStep = Step.Turn2;
					}
				} else if (autoSelected == rightOppositeSideScale && gameData.charAt(1) == 'L') {
					if (distance < 198) {
						driveStraight(-90, 0.5);
					}
				} else {
					autoStep = Step.Done;
				}
			case Turn2:
				if (autoSelected == leftOppositeSideScale && gameData.charAt(1) == 'R') {
					if (turnLeft(0)) {
						resetEncoders();
						autoStep = Step.Straight3;
					} 
				} else if (autoSelected == rightOppositeSideScale && gameData.charAt(1) == 'L') {
					if (turnRight(0)) {
						resetEncoders();
						autoStep = Step.Straight3;
					}
				} else {
					autoStep = Step.Done;
				}
			case Straight3:
				if (distance < 58) {
					driveStraight(0, 0.5);
				} else {
					stop();
					autoStep = Step.Lift;
				}
			case Lift:
				if (getElevatorheight() < 50 || (System.currentTimeMillis() - timerStart) > 2000){
					elevator.set(-0.5);
				}
				else{
					stop();
					elevator.set(0);
					timerStart = System.currentTimeMillis();
					autoStep = Step.LAUNCH;
				}
				break;
			case LAUNCH:
				if ((System.currentTimeMillis() - timerStart) < 2000) {
					gripper.set(1);
				}
				else{
					gripper.set(0);
					autoStep = Step.Done;
				}
				break;
			case Done:
				completeStop();
				break;
			}
			return;
		}
		if (autoSelected.equalsIgnoreCase(leftOppositeSideSwitch) || autoSelected.equalsIgnoreCase(rightOppositeSideSwitch)) {
			double distance = getDistance();
			switch (autoStep) {
			case Straight:
				
			}
		}
	}
			

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		chassis.arcadeDrive(driver.getX(), -driver.getY());
		
		aButton = driver.getRawButton(1);
		bButton = driver.getRawButton(2);
		xButton = driver.getRawButton(3);
		yButton = driver.getRawButton(4);
		lbumper = driver.getRawButton(5);
		rbumper = driver.getRawButton(6);
		select = driver.getRawButton(7);
		start = driver.getRawButton(8);
		leftThumbPush = driver.getRawButton(9);
		rightThumbPush = driver.getRawButton(10);
		
		//boolean rotateToAngle = false;
		
		if(rotationPID.isEnabled()){
			rightChassis.set(-(rotationPID.get()));
			leftChassis.set((rotationPID.get()));
		}
		
		if (bButton){
			//rotateToAngle = true;
			//rotationPID.setSetpoint(0);
					
			rotationPID.setEnabled(false);
		} 
		else if (aButton) {
			//rotateToAngle = true;
			//rotationPID.setSetpoint(180);
			//rotationPID.setEnabled(true);
			//rightChassis.set(-(rotationPID.get()));
			//leftChassis.set((rotationPID.get()));
			if (turnInPlace(0)) {
				System.out.println("worked");
			}
		}
		if(xButton){
			gyroscope.reset();
			resetEncoders();
		}
		if(yButton){
			driveStraight(0, 0.4);
			//chassis.tankDrive(0.5, -0.5);
			//rotationPID.setEnabled(false);
			//System.out.println("yButton");
		}
		if (lbumper) {
			Kp -= 0.005;
		} else if (rbumper) {
			Kp += 0.005;
		}
		if (select) {
			Ki -= 0.005;
		} else if (start) {
			Ki += 0.005;
		}
		if (leftThumbPush) {
			Kd -= 0.005;
		} else if (rightThumbPush) {
			Kd += 0.005;
		}
		
		
       /* if ( rotateToAngle ) {
            rotationPID.enable();
            rightChassis.set(-(rotationPID.get()));
			leftChassis.set((rotationPID.get()));
            
        } else {
            rotationPID.disable();
            
        }*/
		
		//System.out.println(rotationPID.get());
		updateSmartDashboard();
		 
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
	public void disabledPeriodic(){
		//System.out.println(rotationPID);
		updateSmartDashboard();
		autoSelected = (String) m_chooser.getSelected();
		SmartDashboard.putString("Selected Auto", autoSelected);
	}
	public void resetEncoders(){
		leftEncoder.reset();
		rightEncoder.reset();
	}
	public void resetElevatorEncoder(){
		elevatorEncoder.reset();
	}
	public double getElevatorheight(){
		return (double)(elevatorEncoder.get()/ENCODER_COUNTS_PER_INCH_HEIGHT);
	}
	public double getDistance(){
		return ((double)(leftEncoder.get() + rightEncoder.get()) / (ENCODER_COUNTS_PER_INCH * 2));
	}
	
	private void PIDTurn(double setpoint) {
		resetEncoders();
		rotationPID.setSetpoint(setpoint);
		rotationPID.setEnabled(true);
		leftChassis.set(rotationPID.get());
		rightChassis.set(-rotationPID.get());
	}
	private boolean turnRight(double targetAngle){
		// We want to turn in place to 60 degrees 
		leftBack.set(0.3);
		leftFront.set(0.3);
		rightBack.set(0.3);
		rightFront.set(0.3);

		System.out.println("Turning Right");
		
		double currentAngle = gyroscope.getAngle();
		if (currentAngle >= targetAngle - 2){
			System.out.println("Stopped Turning Right");
			return true;
		}
		return false;
	}
	private boolean turnLeft(double targetAngle){
		// We want to turn in place to 60 degrees 
		leftBack.set(-0.3);
		leftFront.set(-0.3);
		rightBack.set(-0.3);
		rightFront.set(-0.3);

		double currentAngle = gyroscope.getAngle();
		if (currentAngle <= targetAngle + 2){
			return true;
		}
		return false;
	}
	
	public boolean turnInPlace(double setPoint) {
		/*if (gyroscope.getAngle() >= (setPoint - 2) && gyroscope.getAngle() <= (setPoint + 2)) {
			rotationPID.setEnabled(false);
			return true;
		} else {
			rotationPID.setSetpoint(setPoint);
			rotationPID.setEnabled(true);
			leftChassis.set(rotationPID.get());
			rightChassis.set(-rotationPID.get());
			return false;
		}*/
		// We want to turn in place to 60 degrees 
		double currentAngle = gyroscope.getAngle();
		if (currentAngle - setPoint > 0) {
			leftBack.set(-0.3);
			leftFront.set(-0.3);
			rightBack.set(-0.3);
			rightFront.set(-0.3);
	
			if (currentAngle <= setPoint + 2){
				return true;
			}
			return false;
		} else if (currentAngle - setPoint < 0) {
			leftBack.set(0.3);
			leftFront.set(0.3);
			rightBack.set(0.3);
			rightFront.set(0.3);

			System.out.println("Turning Right");
		
			if (currentAngle >= setPoint - 2){
				System.out.println("Stopped Turning Right");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private void driveStraight(double heading, double speed) {
		// get the current heading and calculate a heading error
		double currentAngle = gyroscope.getAngle()%360.0;
		System.out.println("driveStraight");
		double error = heading - currentAngle;

		// calculate the speed for the motors
		double leftSpeed = speed;
		double rightSpeed = speed;

		// FIXME: This code can make the robot turn very 
		//        quickly if it is not pointed close to the
		//        correct direction.  I bet this is the 
		//        problem you are experiencing.
		//        I think if you correct the state machine
		//        if statements above, you will be able to 
		//        control the turning speed.
		
		// adjust the motor speed based on the compass error
		if (error < 0) {
			// turn left
			// slow down the left motors
			leftSpeed += error * Kp;
		}
		else {
			// turn right
			// Slow down right motors
			rightSpeed -= error * Kp;
		}
	
		// set the motors based on the inputted speed
		leftBack.set(leftSpeed);
		leftFront.set(leftSpeed);
		rightBack.set(-rightSpeed);
		rightFront.set(-rightSpeed);
	}
	
    private void updateSmartDashboard() {
		
		SmartDashboard.putData("Gyro", gyroscope);
		SmartDashboard.putNumber("Gyro Angle", gyroscope.getAngle());
		SmartDashboard.putNumber("Gyro Rate", gyroscope.getRate());

		SmartDashboard.putNumber("Left Encoder Count", leftEncoder.get());
		SmartDashboard.putNumber("Right Encoder Count", rightEncoder.get());
		SmartDashboard.putNumber("Encoder Distance", getDistance());
		
		SmartDashboard.putNumber("Kp: Bumpers", Kp);
		SmartDashboard.putNumber("Ki: StartSelect", Ki);
		SmartDashboard.putNumber("Kd: JoyPress", Kd);
	}
    
	private void stop(){
		leftBack.set(0);
		leftFront.set(0);
		rightBack.set(0);
		rightFront.set(0);
	}
	private void stopElevator() {
		elevator1.set(0);
		elevator2.set(0);
	}
	private void stopGripper() {
		gripper.set(0);
	}
	private void completeStop() {
		stop();
		stopElevator();
		stopGripper();
	}
	
	@Override
	   /*This function is invoked periodically by the PID Controller, */
	  /* based upon navX-MXP yaw angle input and PID Coefficients.    */
	  public void pidWrite(double output) {
	      
	  }
	
}
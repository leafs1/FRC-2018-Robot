/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/


//|**************************************************************|
//|				Cyberheart 2018 First Power Up 					 |
//|																 |
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
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;

import org.spectrum3847.RIOdroid.RIOadb;
import org.spectrum3847.RIOdroid.RIOdroid;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;

import com.kauailabs.navx.frc.AHRS;

/**
	THIS BRANCH WAS CREATED TO JOIN CODE FROM BRANCHES JB, KM, RZ.
	THIS CODE IS FOR THE RYERSON DISTRICT EVENT
 */



public class Robot extends IterativeRobot implements PIDOutput {
	String gameData;
	//Starting positions
	SendableChooser<String> positionChooser;
	final String left = "left";
	final String right = "right";
	final String center = "center";
	
	double buttonTimer;
	
	//what the robot does
	SendableChooser<String> movementChooser;
	final String Switch = "Switch";
	final String SwitchSwitch = "SwitchSwitch";
	final String Scale = "Scale";
	final String ScaleSwitch = "ScaleSwitch";
	final String SwitchScale = "SwitchScale";
	final String Portal = "Portal";
	final String SwitchLine = "SwitchLine";
	final String SwitchSwitchLine = "SwitchSwitchLine";
	final String ScaleLine = "ScaleLine";
	final String Straight = "Straight";
	final String joeSwitch = "joeSwitch";
	String positionSelected;
	String movementSelected;
	//auto cases
	public enum Step { Straight, Turn, Straight2, Turn2, Straight3, Turn3, Straight4, Straight5, Turn4, Straight6, Straight7, Elevator, elevatorTwo, CubeOut, CubeIn, CubeOut2,  Done }
	public Step autoStep = Step.Straight;
	public long timerStart;

	//Variables
	final static double ENCODER_COUNTS_PER_INCH = 13.49;
	final static double ELEVATOR_ENCODER_COUNTS_PER_INCH = 182.13;
	final static double ENCODER_COUNTS_PER_INCH_HEIGHT = 182.13;

	final static double DEGREES_PER_PIXEL = 0.100;
	double currentSpeed;
	double oldEncoderCounts = 0;
	long old_time = 0;
	String box_position = "NO DATA";
	boolean initializeADB = false;
	
	// Smartdashboard Chooser object for Auto modes
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	// SpeedController Object creations - Define all names of motors here
	SpeedController leftFront, leftBack, rightFront, rightBack, gripper, elevatorOne, elevatorTwo, climberOne, climberTwo, gripperOne, gripperTwo;
	
	// Speed controller group used for new differential drive class
	SpeedControllerGroup leftChassis, rightChassis, climberGroup, gripperGroup, elevator;
	
	// DifferentialDrive replaces the RobotDrive Class from previous years
	DifferentialDrive chassis;
	
	//LimitSwitch
	DigitalInput limitSwitchGripper, limitSwitchUpElevator, limitSwitchDownElevator, limitSwitchUpClimber, limitSwitchDownClimber;
	
	// Joystick Definitions
	Joystick driver;
	Joystick operator;
	
	//Boolean for buttons
	boolean aButton, bButton, xButton, yButton, startButton, selectButton, upButton, downButton, lbumper, rbumper, start, select, leftThumbPush, rightThumbPush, aButtonOp, bButtonOp,xButtonOp,yButtonOp;
	
	// Encoders
	Encoder leftEncoder, rightEncoder, elevatorEncoder;
	
	// Gyro
	//ADXRS450_Gyro gyroscope;
	AHRS gyroscope;
	// PID Variables -WIP
	double kP = 0.03;
	//PID Variables
	PIDController rotationPID;
	
	// CONSTANT VARIABLES FOR PID
	//double Kp = 0.075;
	double Kp = 0.03;
	double Ki = 0;
	//double Kd = 0.195;
	double Kd = 0.0075;
	
	
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		//Chooser for the starting position
		positionChooser = new SendableChooser<String>();
		positionChooser.addObject("right", right);
		positionChooser.addObject("center", center);
		positionChooser.addDefault("left", left);
		
		//Chooser for the movement of the robot
		movementChooser = new SendableChooser<String>();
		movementChooser.addObject("Switch", Switch);
		movementChooser.addObject("SwitchSwitch", SwitchSwitch);
		movementChooser.addObject("Scale", Scale);
		movementChooser.addObject("ScaleSwitch", ScaleSwitch);
		movementChooser.addObject("SwitchScale", SwitchScale);
		movementChooser.addObject("Portal", Portal);
		movementChooser.addObject("SwitchLine", SwitchLine);
		movementChooser.addObject("SwitchSwitchLine", SwitchSwitchLine);
		movementChooser.addObject("ScaleLine", ScaleLine);
		movementChooser.addDefault("Straight", Straight);
		movementChooser.addObject("joeSwitch", joeSwitch);
		
		// Defines all the ports of each of the motors
		leftFront = new Spark(0);
		leftBack = new Spark(1);
		rightFront = new Spark(2);
		rightBack = new Spark(3);
		climberOne = new Spark(4);
		climberTwo = new Spark(5);
		elevatorOne = new Spark(6);
		elevatorTwo = new Spark(7);
		gripperOne = new Spark(8);
		gripperTwo = new Spark(9);
		
		//Inverting Sparks
		gripperTwo.setInverted(true);
		climberOne.setInverted(true);
		
		// Defines Joystick ports
		driver = new Joystick(0);
		operator = new Joystick(1);
		
		//LimitSwitch Port Assignment
		limitSwitchGripper = new DigitalInput(4);

		// Defines the left and right SpeedControllerGroups for our DifferentialDrive class
		leftChassis = new SpeedControllerGroup(leftFront, leftBack);
		rightChassis = new SpeedControllerGroup(rightFront, rightBack);
		climberGroup = new SpeedControllerGroup(climberOne, climberTwo);
		gripperGroup = new SpeedControllerGroup(gripperOne, gripperTwo);
		elevator = new SpeedControllerGroup(elevatorOne, elevatorTwo);
		
		// Inverts the right side of the drive train to account for the motors being physically flipped
		rightChassis.setInverted(true);
		
		// Defines our DifferentalDrive object with both sides of our drivetrain
		chassis = new DifferentialDrive(leftChassis, rightChassis);
		
		// Set up Encoder ports
		leftEncoder = new Encoder(0,1);
		rightEncoder = new Encoder(3,2);
		elevatorEncoder = new Encoder(8,9);
		
		//Gyroscope (NAV-X) Setup
		gyroscope = new AHRS(SPI.Port.kMXP);
	
		// Initialize ADB Communication 
		if (initializeADB){
			RIOdroid.init();
			RIOadb.init();
			System.out.println("Start ADB" + RIOdroid.executeCommand("adb start-server"));
			System.out.println("ADB Initialization Complete");
		}
		else{
			System.out.println("ADB INIT NOT RAN");
		}
		SmartDashboard.putData("Robot Position", positionChooser);
		SmartDashboard.putData("Movement Type", movementChooser);
	}
	
	@Override
	public void autonomousInit() {
		//Assign selected modes to a variable
				positionSelected = positionChooser.getSelected();
				movementSelected = movementChooser.getSelected();
				System.out.println("Position Selected: " + positionSelected);
				System.out.println("Movement Selected" + movementSelected);
				//Get Orientation of scale and store it in gameData
				gameData = DriverStation.getInstance().getGameSpecificMessage();
				//Reset the gyro so the heading at the start of the match is 0
				resetEncoders();
				autoStep = Step.Straight;
				gyroscope.reset();	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		updateSmartDashboard();
		double distance = getDistance();
		double height = getElevatorheight();
		if (positionSelected == left) {
			if (movementSelected == Straight){
				System.out.println("Position: Left; Movement:Straight");
				if (distance < 200) {
					driveStraight(0, 0.5);
				} else {
					stop();
					autoStep = Step.Done;
				}
			}
			if (movementSelected == joeSwitch) {
				System.out.println("Position: Left; Movement:joeSwitch");
				if (autoStep == Step.Straight){
					if (distance < 180) {
						driveStraight(0, 0.5);
					} else {
						stop();
						autoStep = Step.Turn;
					}
				}
				if (gameData.charAt(0) == 'L') {
					switch (autoStep){
					case Turn:
						if (turnLeft(90)) {
							resetEncoders();
							autoStep = Step.Straight2;
						}
						break;
					case Straight2:
						if (distance < 28) {
							driveStraight(0, 0.5);
						} else {
							stop();
							autoStep = Step.Elevator;
						}
						break;
					case Elevator:
						if (height < 28) {
							elevator.set(0.4);
						} else {
							elevator.set(0.05);
							resetEncoders();
							autoStep = Step.Straight3;
						}
						break;
					case Straight3:
						if (distance < 7) {
							driveStraight(90, 0.4);
							elevator.set(0.05);
						} else {
							stop();
							elevator.set(0.05);
							autoStep = Step.CubeOut;
						}
						break;
					case CubeOut:
						if (!limitSwitchGripper.get()) {
							elevator.set(0.05);
							resetEncoders();
							stopGripper();
							autoStep = Step.Straight4;
						} else {
							elevator.set(0.05);
							gripper.set(1);
						}
						break;
						
					}
				}
				
			}
			if (movementSelected.equalsIgnoreCase(Switch)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("leftSwitch1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight4;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(90, -0.4);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("leftSwitch3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 220) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 240) {
								driveStraight(90, 0.4);
							} else {
								stop();
								autoStep = Step.Turn2;
							}
							break;
						case Turn2:
							if (turnInPlace(180)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 40) {
								driveStraight(180, 0.4);
							} else {
								stop();
								autoStep = Step.Turn3;
							}
							break;
						case Turn3:
							if (turnInPlace(270)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight4;
							}
							break;
						case Straight4:
							if (distance < 10) {
								driveStraight(270, 0.4);
								elevator.set(0.05);
							} else {
								elevator.set(0.05);
								stop();
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight5;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight5:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(270, -0.4);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(SwitchSwitch)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("leftSwitchSwitch1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight4;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}	
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(90, -0.4);
							} else {
								completeStop();
								resetElevatorEncoder();
								autoStep = Step.Done;
							}
							break;
						case Turn2:
							if (turnInPlace(-0)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight5;
							}
							break;
						case Straight5:
							if (distance < 40) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn3;
							}
							break;
						case Turn3:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight6;
							}
							break;
						case Straight6:
							if (distance < 15) {
								driveStraight(90, 0.4) ;
							} else {
								stop();
								autoStep = Step.Turn4;
							}
							break;
						case Turn4:
							if (turnInPlace(180)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.CubeIn;
							}
							break;
						case CubeIn:
							if (distance < 20) {
								driveStraight(180, 0.4);
								gripper.set(-1);
							} else {
								completeStop();
								autoStep = Step.elevatorTwo;
							}
							break;
						case elevatorTwo:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.CubeOut2;
							}
							break;
						case CubeOut2:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight7;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight7:
							if (distance < 15) {
								driveStraight(180, -0.4);
								elevator.set(0.05);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("leftSwitchSwitch3/4");
				}
			} else if (movementSelected.equalsIgnoreCase(Scale)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(1) == 'L') {
						System.out.println("leftScale1/3");
						switch (autoStep) {
						case Straight:
							if (distance < 315) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (height < 80) {
								elevator.set(0.4);
							} else {
								if (distance < 10) {
									driveStraight(90, 0.4);
									elevator.set(0.05);
								} else {
									if (!limitSwitchGripper.get()) {
										completeStop();
										resetEncoders();
										autoStep = Step.Straight2;
									} else {
										elevator.set(0.05);
										gripper.set(1);
									}
								}
							}
							break;
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("leftScale2/4");
					}
				}
			} else if (movementSelected.equalsIgnoreCase(ScaleSwitch)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(1) == 'L') {
						if (gameData.charAt(0) == 'L') {
							System.out.println("leftScaleSwitch1");
						} else {
							System.out.println("leftScaleSwitch3");
						}
					} else {
						if (gameData.charAt(0) == 'L') {
							System.out.println("leftScaleSwitch2");
						} else {
							System.out.println("leftScaleSwitch4");
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(SwitchScale)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						if (gameData.charAt(1) == 'L') {
							System.out.println("leftSwitchScale1");
						} else {
							System.out.println("leftSwitchScale2");
						}
					} else {
						if (gameData.charAt(1) == 'L') {
							System.out.println("leftSwitchScale3");
						} else {
							System.out.println("leftSwitchScale4");
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(SwitchLine)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("leftSwitchLine1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (height < 28) {
								elevator.set(0.3);
							} else {
								if (!limitSwitchGripper.get()) {
									elevator.set(0.05);
									resetEncoders();
									stopGripper();
									autoStep = Step.Straight4;
								} else {
									elevator.set(0.05);
									gripper.set(1);
								}
							}
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(90, -0.4);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("leftSwitchLine3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(SwitchSwitchLine)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("leftSwitchSwitchLine1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight4;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}	
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(90, -0.4);
							} else {
								completeStop();
								resetElevatorEncoder();
								autoStep = Step.Done;
							}
							break;
						case Turn2:
							if (turnInPlace(-0)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight5;
							}
							break;
						case Straight5:
							if (distance < 40) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn3;
							}
							break;
						case Turn3:
							if (turnInPlace(90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight6;
							}
							break;
						case Straight6:
							if (distance < 15) {
								driveStraight(90, 0.4) ;
							} else {
								stop();
								autoStep = Step.Turn4;
							}
							break;
						case Turn4:
							if (turnInPlace(180)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.CubeIn;
							}
							break;
						case CubeIn:
							if (distance < 20) {
								driveStraight(180, 0.4);
								gripper.set(-1);
							} else {
								completeStop();
								autoStep = Step.elevatorTwo;
							}
							break;
						case elevatorTwo:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.CubeOut2;
							}
							break;
						case CubeOut2:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight7;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight7:
							if (distance < 15) {
								driveStraight(180, -0.4);
								elevator.set(0.05);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("leftSwitchSwitchLine3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					}
				}
			}
		} else if (positionSelected.equalsIgnoreCase(right)) {
			if (movementSelected.equalsIgnoreCase(Straight)){
				if (distance < 200) {
					driveStraight(0, 0.4);
				} else {
					stop();
					autoStep = Step.Turn;
				}
			}
			if (movementSelected.equalsIgnoreCase(Switch)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("rightSwitch1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 220) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 240) {
								driveStraight(-90, 0.4);
							} else {
								stop();
								autoStep = Step.Turn2;
							}
							break;
						case Turn2:
							if (turnInPlace(-180)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 40) {
								driveStraight(-180, 0.4);
							} else {
								stop();
								autoStep = Step.Turn3;
							}
							break;
						case Turn3:
							if (turnInPlace(-270)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight4;
							}
							break;
						case Straight4:
							if (distance < 10) {
								driveStraight(-270, 0.4);
								elevator.set(0.05);
							} else {
								elevator.set(0.05);
								stop();
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight5;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight5:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(-270, -0.4);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("rightSwitch3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(-90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(-90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight4;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(-90, -0.4);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(SwitchSwitch)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("rightSwitchSwitch1/2");
					} else {
						System.out.println("rightSwitchSwitch3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(-90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(-90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight4;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}	
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(-90, -0.4);
							} else {
								completeStop();
								resetElevatorEncoder();
								autoStep = Step.Done;
							}
							break;
						case Turn2:
							if (turnInPlace(0)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight5;
							}
							break;
						case Straight5:
							if (distance < 40) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn3;
							}
							break;
						case Turn3:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight6;
							}
							break;
						case Straight6:
							if (distance < 15) {
								driveStraight(-90, 0.4) ;
							} else {
								stop();
								autoStep = Step.Turn4;
							}
							break;
						case Turn4:
							if (turnInPlace(-180)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.CubeIn;
							}
							break;
						case CubeIn:
							if (distance < 20) {
								driveStraight(-180, 0.4);
								gripper.set(-1);
							} else {
								completeStop();
								autoStep = Step.elevatorTwo;
							}
							break;
						case elevatorTwo:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.CubeOut2;
							}
							break;
						case CubeOut2:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight7;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight7:
							if (distance < 15) {
								driveStraight(-180, -0.4);
								elevator.set(0.05);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
						case Done:
							completeStop();
							break;
						}
					}
			} else if (movementSelected.equalsIgnoreCase(Scale)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(1) == 'l') {
						System.out.println("rightScale1/3");
					} else {
						System.out.println("rightScale2/4");
						switch (autoStep) {
						case Straight:
							if (distance < 315) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (height < 80) {
								elevator.set(0.4);
							} else {
								if (distance < 10) {
									driveStraight(-90, 0.4);
									elevator.set(0.05);
								} else {
									if (!limitSwitchGripper.get()) {
										completeStop();
										resetEncoders();
										autoStep = Step.Straight2;
									} else {
										elevator.set(0.05);
										gripper.set(1);
									}
								}
							}
							break;
						case Done:
							completeStop();
							break;
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(ScaleSwitch)) {
				
			} else if (movementSelected.equalsIgnoreCase(SwitchScale)) {
				
			} else if (movementSelected.equalsIgnoreCase(SwitchLine)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("rightSwitchLine1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("rightSwitchLine3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(-90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(-90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (height < 28) {
								elevator.set(0.3);
							} else {
								if (!limitSwitchGripper.get()) {
									elevator.set(0.05);
									resetEncoders();
									stopGripper();
									autoStep = Step.Straight4;
								} else {
									elevator.set(0.05);
									gripper.set(1);
								}
							}
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(-90, -0.4);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(SwitchSwitchLine)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(0) == 'L') {
						System.out.println("rightSwitchSwitchLine1/2");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Done;
							}
							break;
						case Done:
							completeStop();
							break;
						}
					} else {
						System.out.println("rightSwitchSwitchLine3/4");
						switch (autoStep) {
						case Straight:
							if (distance < 180) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn;
							}
							break;
						case Turn:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight2;
							}
							break;
						case Straight2:
							if (distance < 28) {
								driveStraight(-90, 0.4);
							} else {
								stop();
								autoStep = Step.Elevator;
							}
							break;
						case Elevator:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.Straight3;
							}
							break;
						case Straight3:
							if (distance < 7) {
								driveStraight(-90, 0.4);
								elevator.set(0.05);
							} else {
								stop();
								elevator.set(0.05);
								autoStep = Step.CubeOut;
							}
							break;
						case CubeOut:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight4;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}	
							break;
						case Straight4:
							if (distance < 15) {
								elevator.set(0.05);
								driveStraight(-90, -0.4);
							} else {
								completeStop();
								resetElevatorEncoder();
								autoStep = Step.Done;
							}
							break;
						case Turn2:
							if (turnInPlace(0)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight5;
							}
							break;
						case Straight5:
							if (distance < 40) {
								driveStraight(0, 0.4);
							} else {
								stop();
								autoStep = Step.Turn3;
							}
							break;
						case Turn3:
							if (turnInPlace(-90)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.Straight6;
							}
							break;
						case Straight6:
							if (distance < 15) {
								driveStraight(-90, 0.4) ;
							} else {
								stop();
								autoStep = Step.Turn4;
							}
							break;
						case Turn4:
							if (turnInPlace(-180)) {
								resetEncoders();
								rotationPID.setEnabled(false);
								autoStep = Step.CubeIn;
							}
							break;
						case CubeIn:
							if (distance < 20) {
								driveStraight(-180, 0.4);
								gripper.set(-1);
							} else {
								completeStop();
								autoStep = Step.elevatorTwo;
							}
							break;
						case elevatorTwo:
							if (height < 28) {
								elevator.set(0.4);
							} else {
								elevator.set(0.05);
								resetEncoders();
								autoStep = Step.CubeOut2;
							}
							break;
						case CubeOut2:
							if (!limitSwitchGripper.get()) {
								elevator.set(0.05);
								resetEncoders();
								stopGripper();
								autoStep = Step.Straight7;
							} else {
								elevator.set(0.05);
								gripper.set(1);
							}
							break;
						case Straight7:
							if (distance < 15) {
								driveStraight(-180, -0.4);
								elevator.set(0.05);
							} else {
								completeStop();
								autoStep = Step.Done;
							}
						case Done:
							completeStop();
							break;
						}
					}
				}
			} else if (movementSelected.equalsIgnoreCase(ScaleLine)) {
				if (gameData.length() > 0) {
					if (gameData.charAt(1) == 'L') {
						System.out.println("rightScaleLine");
					}
				}
			}
		} else if (positionSelected.equalsIgnoreCase(center)){
			
		} 
		}
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		chassis.arcadeDrive(driver.getX(), -driver.getY());
		
		// Get Joystick Buttons
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

		
		aButtonOp = operator.getRawButton(1);
		bButtonOp = operator.getRawButton(2);
		xButtonOp = operator.getRawButton(3);
		yButtonOp = operator.getRawButton(4);

		
		//	FOR DEBUGGING
		if (xButton) {
			//System.out.print(androidData());
			elevatorEncoder.reset();
			resetEncoders();
			gyroscope.reset();
			resetElevatorEncoder();
		} 
		
		if (aButton){
			cubeOUT();
		}
		if (bButton){
			elevator.set(0.8);
		}
		if (lbumper) {
		}
		
		// OPERATOR CONTROLS
		if (aButtonOp) {
			gripperGroup.set(1);
		} 
	
		else if (bButtonOp) {
			gripperGroup.set(-1);
		}
		else if (yButtonOp) {
			gripperGroup.set(-0.5);
		}
		else {
			gripperGroup.set(0);
		}
		climberGroup.set(operator.getRawAxis(5));
		elevator.set(operator.getRawAxis(1));

		updateSmartDashboard();
	}
				
	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
	
	public void disabledPeriodic(){
		updateSmartDashboard();
	}
	
	
	
	
	//--------------------------//
	//		Custom Functions	//
	//--------------------------//
	
	// Resets encoder values to 0
	public void resetEncoders(){
		leftEncoder.reset();
		rightEncoder.reset();
	}
	
	// Calculates and returns the Robot distance using the encoders attached to each side of the drive train
	public double getDistance(){
		return ((double)(leftEncoder.get() + rightEncoder.get()) / (ENCODER_COUNTS_PER_INCH * 2));
	}
	
	private double getElevatorHeight(){
		return (double)(elevatorEncoder.get()/ELEVATOR_ENCODER_COUNTS_PER_INCH);
	}
	
	// Calculates the robotSpeed
	public double robotSpeed() {
		// Calculates current speed of the robot in m/s
		currentSpeed = ((getDistance() - oldEncoderCounts)/(System.currentTimeMillis() - old_time)) * 0.0254;
		
		old_time = System.currentTimeMillis();
		oldEncoderCounts = getDistance();
		return (double) currentSpeed;
	}

	private double androidData(){
		double Center_X = 0;
		String box_data = RIOdroid.executeCommand("adb logcat -t 150 ActivityManager:I native:D *:S");
		String[] seperated_data = box_data.split("\n");
		//System.out.println(seperated_data[(seperated_data.length-1)]);
		// if split_data == 1 that means there was no line break & therefore no data
		if (seperated_data.length == 1){
			Center_X = 0;
		}
		else{	// if the length of the split array is longer than 1 then 
			box_position = seperated_data[(seperated_data.length-1)];
			String[] splitted;
	        double[] final_array = new double[4];
	        String[] split_data = box_position.split(",");
	        
	        if (split_data.length != 4){
	        	Center_X = 0;
	            //System.out.println("Invalid");
	        }
	        else{
	        	// this is not running
	            for (int i=0; i<split_data.length; i++){
	                if (i == 0){
	                    splitted = split_data[0].split(" ");
	                    final_array[0] = Double.parseDouble(splitted[splitted.length-1]);
	                }
	                else if(i == 3){
	                    splitted = split_data[3].split(" ");
	                    final_array[3] = Double.parseDouble(splitted[0]);
	                }
	                else{
	                    final_array[i] = Double.parseDouble(split_data[i]);
	                }
	            }
	            
	            /*	---		Uncomment to get the coordinates of the square tracking the cube --
	            for (int x=0; x<final_array.length; x++){
	                System.out.println(final_array[x]);
	            }*/
	            
	            Center_X = (final_array[3]+final_array[1])/2;
	        }
		}
		System.out.println("Center x: " + Center_X);
		return Center_X;
	}
	
	private void turnToBox(){
		double Center_X = androidData();
		//System.out.print("Offset: " + degreeOffset);
		
		// FIXME: Already removed the not before turnLeft/ turnRight
		if (Center_X > 170 && Center_X != 0){
			double degreeOffset = (170 - Center_X)*DEGREES_PER_PIXEL;
			double boxAngle = gyroscope.getAngle()%360 + degreeOffset;

			System.out.println("Should be turning left, Offset: " + degreeOffset);
			while (gyroscope.getAngle()%360 > boxAngle){
				turnLeft(boxAngle);
			}
		}
		if (Center_X < 170 && Center_X != 0){
			double degreeOffset = (170 - Center_X)*DEGREES_PER_PIXEL;
			double boxAngle = gyroscope.getAngle()%360 + degreeOffset;
			System.out.println("Should be turning right, Offset: " + degreeOffset);
			while(gyroscope.getAngle()%360 < boxAngle){
				turnRight(boxAngle);
			}
		}
		else{
			System.out.println("Should not be moving, NO DATA");
			stop();
		}
	}
	
	
	private void tipPrevention(){
		if (gyroscope.getPitch() > 10){
			leftChassis.set(0.5);
			rightChassis.set(0.5);
		}
		if (gyroscope.getPitch() < -10){
			leftChassis.set(-0.5);
			rightChassis.set(-0.5);
		}
	}
	
	private void driveStraight(double heading, double speed) {
		// get the current heading and calculate a heading error
		double currentAngle = (gyroscope.getAngle()%360.0);
		//System.out.println("driveStraight");
		double error = heading - currentAngle;
		// calculate the speed for the motors
		double leftSpeed = speed;
		double rightSpeed = speed;
		
		// adjust the motor speed based on the compass error
		if (error < 0) {
			// turn left
			// slow down the left motors
			leftSpeed += error * kP;
		}
		else {
			// turn right
			// Slow down right motors
			rightSpeed -= error * kP;
		}
	
		// set the motors based on the inputed speed
		leftChassis.set(leftSpeed);
		rightChassis.set(rightSpeed);
	}
	
	//slow motor speeds while turning
	
	private boolean turnRight(double targetAngle){
		// We want to turn in place to 60 degrees 
		leftBack.set(0.35);
		leftFront.set(0.35);
		rightBack.set(0.35);
		rightFront.set(0.35);

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
		leftBack.set(-0.35);
		leftFront.set(-0.35);
		rightBack.set(-0.35);
		rightFront.set(-0.35);

		double currentAngle = gyroscope.getAngle();
		if (currentAngle <= targetAngle + 2){
			return true;
		}
		return false;
	}
	// Pushes all data the Smart Dashboard when called
	private void updateSmartDashboard() {
		SmartDashboard.putData("Gyro", gyroscope);
		SmartDashboard.putNumber("Gyro Angle", gyroscope.getAngle());
		SmartDashboard.putNumber("Gyro Rate", gyroscope.getRate());

		SmartDashboard.putNumber("Left Encoder Count", leftEncoder.get());
		SmartDashboard.putNumber("Right Encoder Count", rightEncoder.get());
		SmartDashboard.putNumber("Encoder Distance", getDistance());
		
		SmartDashboard.putNumber("Elevator Height", getElevatorHeight());
		
		SmartDashboard.putBoolean("Cube Held", limitSwitchGripper.get());
		
		SmartDashboard.putNumber("Robot Speed", robotSpeed());
	}
	
	public boolean turnInPlace(double setPoint) {
		/* FIXME Setpoint is currently required to be exact, implement a range*/
		if (gyroscope.getAngle() >= (setPoint - 2) && gyroscope.getAngle() <= (setPoint + 2)) {
			return true;
		} else {
			rotationPID.setSetpoint(setPoint);
			rotationPID.setEnabled(true);
			leftChassis.set(rotationPID.get());
			rightChassis.set(-rotationPID.get());
			return false;
		}
		
	}
	
	private void stop(){
		leftBack.set(0);
		leftFront.set(0);
		rightBack.set(0);
		rightFront.set(0);
	}
	private void stopElevator() {
		elevatorOne.set(0);
		elevatorTwo.set(0);
	}
	private void stopGripper() {
		gripperOne.set(0);
		gripperTwo.set(0);
	}
	private void completeStop() {
		stop();
		stopElevator();
		stopGripper();
	}
	private void cubeOUT() {
		double distance = getDistance();
		double height = getElevatorheight();
		if (height < 80) {
			elevator.set(0.7);
		} else {
			if (!limitSwitchGripper.get()) {
				completeStop();
				resetEncoders();
			} else {
				elevator.set(0.05);
				gripper.set(1);
			}
		}
	}
	public void resetElevatorEncoder(){
		elevatorEncoder.reset();
	}
	public double getElevatorheight(){
		return (double)(elevatorEncoder.get()/ENCODER_COUNTS_PER_INCH_HEIGHT);
	}
	@Override
	public void pidWrite(double output) {
		// TODO Auto-generated method stub
		
	}
}

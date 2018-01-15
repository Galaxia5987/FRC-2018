package org.usfirst.frc.team5987.robot.commands;

import org.usfirst.frc.team5987.robot.Robot;
import org.usfirst.frc.team5987.robot.subsystems.LiftSubsystem;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class liftCommand extends Command {
	double position;
	LiftSubsystem liftSubsystem;
    public liftCommand(double pos) {
        this.position=pos;
        this.liftSubsystem=Robot.liftSubsystem;
    	// Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	liftSubsystem.setPoint(position);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	liftSubsystem.update();
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return Math.abs(liftSubsystem.getHeight()-position)<0.1;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}

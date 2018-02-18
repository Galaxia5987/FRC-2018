package org.usfirst.frc.team5987.robot.commands;

import org.usfirst.frc.team5987.robot.Constants;

import auxiliary.Point;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class AutoDriveToScaleCommand extends CommandGroup {
	/**
	 * The game specific message which includes data about the Scale and
	 * Switches Plates randomanization.
	 */
	String gameData = DriverStation.getInstance().getGameSpecificMessage();

	// Positions of the alliance Switch and Scale Plates.
	char scalePosition = gameData.charAt(1);
	private static double END_Y = Constants.toMeter(41.88) + Constants.AUTO_SCALE_SHIFT_T0_FIELD_CENTER - Constants.CENTER_TO_BACK_BUMPER;

	public AutoDriveToScaleCommand(char robotPosition) {
		addSequential(new IntakeSolenoidCommand(true));
		addParallel(new LiftCommand(0.1, 1.7)); // move the lift up a bit to prevent the cube from touching the floor  
		if(robotPosition == scalePosition){
			addParallel(new LiftCommand(Constants.LiftCommandStates.SCALE_TOP, Constants.AUTO_SCALE_CLOSE_LIFT_DELAY));
			addSequential(new PathPointsCommand(new Point[] { 
					new Point(0, 0),
					new Point(Constants.toMeter(196) - Constants.CENTER_TO_BACK_BUMPER, -0.15 * ((robotPosition == 'R') ? 1 : -1)),
					new Point(Constants.toMeter(299.65) - Constants.CENTER_TO_BACK_BUMPER - Constants.AUTO_TURN_DISTANCE_BEFORE_SCALE,
							END_Y * ((robotPosition == 'R') ? 1 : -1)),
					new Point(Constants.toMeter(299.65) - Constants.CENTER_TO_BACK_BUMPER - Constants.AUTO_END_DISTANCE_BEFORE_SCALE,
							END_Y * ((robotPosition == 'R') ? 1 : -1))
					})
			);
		}else{
			addParallel(new LiftCommand(Constants.LiftCommandStates.SCALE_TOP, Constants.AUTO_SCALE_FAR_LIFT_DELAY));
		}
		addSequential(new ShootCubeCommand(0.4, true));
		
	}

}

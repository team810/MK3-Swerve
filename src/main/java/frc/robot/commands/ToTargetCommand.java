package frc.robot.commands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Conveyor;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Limelight;


public class ToTargetCommand extends CommandBase {
    private final Conveyor conveyor;
    private final Drivetrain drivetrain;
    private final Limelight limelight;
    private final double ERROR_AMOUNT_X = .75;
    private final double X_OFFSET = 8.25;
    private final PIDController Xcontroller;

    public ToTargetCommand(Conveyor conveyor, Drivetrain drivetrain, Limelight limelight) {
        this.conveyor = conveyor;
        this.drivetrain = drivetrain;
        this.limelight = limelight;

        Xcontroller = Constants.DrivetrainConstants.Auto.XY_CONTROLLER;

        Xcontroller.setTolerance(ERROR_AMOUNT_X);
        Xcontroller.setSetpoint(X_OFFSET);
        Xcontroller.disableContinuousInput();


        addRequirements(this.conveyor, this.drivetrain, this.limelight);
    }

    @Override
    public void initialize() {
//        if (conveyor.getGamePiece() == 1)
//        {
//
//        } else if (conveyor.getGamePiece() == 2) {
//            limelight.setMode("AprilTag");
//        }else{
//            System.out.println("BAD");
//        }
        limelight.setMode("Reflective Tape");
    }

    @Override
    public void execute() {

        if (limelight.hasTarget())
        {
            SmartDashboard.putData("Pid controller" , Xcontroller);
            SmartDashboard.putNumber("Pid calc point",limelight.getBestTarget().getYaw());
            double xSpeed = Xcontroller.calculate(limelight.getBestTarget().getYaw(),X_OFFSET);
            SmartDashboard.putBoolean("At setpoint", Xcontroller.atSetpoint());
            System.out.println(xSpeed);

            xSpeed = Math.min(xSpeed, .75);
            xSpeed = Math.max(xSpeed, -.75);

            ChassisSpeeds speeds = new ChassisSpeeds(0,-1 * xSpeed,0);

            if (RobotState.isAutonomous())
            {
                SwerveModuleState[] states = Constants.DrivetrainConstants.KINEMATICS.toSwerveModuleStates(speeds);
                SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.DrivetrainConstants.MAX_VELOCITY_METERS_PER_SECOND);

                drivetrain.setStates(states);
            }
            if (RobotState.isTeleop())
            {
                drivetrain.drive(speeds);
            }

        }

        if (isFinished())
        {
            ChassisSpeeds speeds = new ChassisSpeeds(0,0,0);

            if (RobotState.isAutonomous())
            {
                SwerveModuleState[] states = Constants.DrivetrainConstants.KINEMATICS.toSwerveModuleStates(speeds);
                SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.DrivetrainConstants.MAX_VELOCITY_METERS_PER_SECOND);

                drivetrain.setStates(states);
            }
            if (RobotState.isTeleop())
            {
                drivetrain.drive(speeds);
            }
        }

    }

    @Override
    public boolean isFinished() {
        return Xcontroller.atSetpoint();
    }

    @Override
    public void end(boolean interrupted) {

    }
}
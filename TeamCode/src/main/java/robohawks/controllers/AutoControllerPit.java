package robohawks.controllers;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

import robohawks.modules.base.HolonomicDriveModule;

@Autonomous(name = "AutoPit")
public class AutoControllerPit extends LinearOpMode {

    HolonomicDriveModule drive;
    DcMotor liftArm;
    DcMotor liftArm2;
    Servo phone;
    Servo drop;

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    private TFObjectDetector tfod;

    private static final String VUFORIA_KEY = "AYwG39//////AAABmUWlzi8FCUAtlfkPEOcymgNiXqcEjwtTA47wJtEpLEIRA5x8zjycsIni/ha1376bp2jRAd7+WTWd9cZm+R1u9Yew+r5XXw+jwYNxFQeyLJh+xwXxaupmfLI/XDbo6KkGVbSncae/OIWzwRaEOreULZc7ow08NxpyBgYrAf0ri5d4AIJggSlQKMwhfTC1IEtrrJ9CxuikYZSEY3tSqdg9EeP+WgoqnTyE1kFiZeGUP0xMUMOU8FqiSD2S7Jg10upcVn5M5uO0Swohvjyov1YlAws7KxJAeZJUavEa+E427U7ti6ez1jfGxXSfqd3dNH01yDUxSaG9cP8YPlZldSjqsBrP7pNzM0L/TC9bhetU00Qd";
    private VuforiaLocalizer vuforia;

    ElapsedTime time;

    @Override
    public void runOpMode() {

        //**********INIT START**********//

        drive = new HolonomicDriveModule(hardwareMap);
        liftArm = hardwareMap.dcMotor.get("liftArm");
        liftArm2 = hardwareMap.dcMotor.get("liftArm2");
        phone = hardwareMap.servo.get("phone");
        drop = hardwareMap.servo.get("drop");
        time = new ElapsedTime();

        phone.setPosition(.15);
        drop.setPosition(0);

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        waitForStart();

        //**********INIT STOP**********//
        if (opModeIsActive()){
            if (tfod != null) {
                tfod.activate();
            }

            //**********DETECT START**********//

            time.reset();

            String pos = "right";

            while (time.seconds() < 5) {

                if (tfod != null) {
                    // getUpdatedRecognitions() will return null if no new information is available since
                    // the last time that call was made.
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    if (updatedRecognitions != null) {
                        telemetry.addData("# Object Detected", updatedRecognitions.size());
                        if (updatedRecognitions.size() == 2) {
                            int goldMineralX = -1;
                            int silverMineral1X = -1;
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) recognition.getLeft();
                                } else if (silverMineral1X == -1) {
                                    silverMineral1X = (int) recognition.getLeft();
                                }
                                if (goldMineralX != -1 && silverMineral1X != -1) {
                                    if (goldMineralX < silverMineral1X) {
                                        pos = "left";
                                    } else if (goldMineralX > silverMineral1X) {
                                        pos = "center";
                                    }
                                } else {
                                    pos = "right";
                                }
                            }
                            telemetry.addData("Position", pos);
                            telemetry.update();
                        }
                    }
                }

            }

            if (tfod != null) {
                tfod.shutdown();
            }
            //**********DETECT STOP**********//

            //**********DROP START**********//

            //DROP
            liftArm.setPower(-1);
            liftArm2.setPower(1);
            sleep(4000);
            liftArm.setPower(0);
            liftArm2.setPower(0);

            //CLOCKWISE
            drive.setPowerOne(-1);
            drive.setPowerTwo(1);
            drive.setPowerThree(1);
            drive.setPowerFour(-1);
            sleep(300);
            drive.setPowerOne(0);
            drive.setPowerTwo(0);
            drive.setPowerThree(0);
            drive.setPowerFour(0);
            sleep(500);

            //BACK
            drive.setPowerOne(-1);
            drive.setPowerTwo(-1);
            drive.setPowerThree(-1);
            drive.setPowerFour(-1);
            sleep(100);
            drive.setPowerOne(0);
            drive.setPowerTwo(0);
            drive.setPowerThree(0);
            drive.setPowerFour(0);
            sleep(500);

            //COUNTER CLOCKWISE
            drive.setPowerOne(1);
            drive.setPowerTwo(-1);
            drive.setPowerThree(-1);
            drive.setPowerFour(1);
            sleep(300);
            drive.setPowerOne(0);
            drive.setPowerTwo(0);
            drive.setPowerThree(0);
            drive.setPowerFour(0);
            sleep(500);

            //**********DROP STOP**********//



            //**********MINERAL TAP START**********//

            //SET SERVO
            phone.setPosition(.7);

            switch (pos) {
                case "left":
                    //COUNTER CLOCKWISE
                    drive.setPowerOne(1);
                    drive.setPowerTwo(-1);
                    drive.setPowerThree(-1);
                    drive.setPowerFour(1);
                    sleep(500);
                    drive.setPowerOne(0);
                    drive.setPowerTwo(0);
                    drive.setPowerThree(0);
                    drive.setPowerFour(0);
                    sleep(500);
                    break;
                case "right":
                    //CLOCKWISE
                    drive.setPowerOne(-1);
                    drive.setPowerTwo(1);
                    drive.setPowerThree(1);
                    drive.setPowerFour(-1);
                    sleep(500);
                    drive.setPowerOne(0);
                    drive.setPowerTwo(0);
                    drive.setPowerThree(0);
                    drive.setPowerFour(0);
                    sleep(500);
                    break;
                default:
                    //FORWARD
                    drive.setPowerOne(1);
                    drive.setPowerTwo(1);
                    drive.setPowerThree(1);
                    drive.setPowerFour(1);
                    sleep(500);
                    drive.setPowerOne(0);
                    drive.setPowerTwo(0);
                    drive.setPowerThree(0);
                    drive.setPowerFour(0);
                    sleep(500);
                    break;
            }

            phone.setPosition(1);

            //FORWARD
            drive.setPowerOne(1);
            drive.setPowerTwo(1);
            drive.setPowerThree(1);
            drive.setPowerFour(1);
            sleep(500);
            drive.setPowerOne(0);
            drive.setPowerTwo(0);
            drive.setPowerThree(0);
            drive.setPowerFour(0);
            sleep(500);

            //DROP
            drop.setPosition(1);

            //**********MINERAL TAP STOP**********//
        }

    }

    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

}
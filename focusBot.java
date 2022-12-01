package focus;
import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
/**
 * focusBot - a robot by @author cdnex
 */

public class focusBot extends AdvancedRobot{
    boolean movingForward,peek;
    double moveAmount, oldEnemyHeading;

    public void run(){
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(isAdjustGunForRobotTurn());
        setColors(Color.black, Color.yellow, Color.red);
        setBulletColor(Color.MAGENTA);

        // moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        // peek=false;

        // turnLeft(getHeading()%90);
        // ahead(moveAmount);

        // peek=true;
        // turnRight(90);

        while(true){
            if(getRadarTurnRemaining()==0.0){
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
            crazyMovement();
            // if(getOthers()>4){
            //     wallMovement();
            // }else{
            //     crazyMovement();
            // }
            
        }
    }

    public void onScannedRobot(ScannedRobotEvent e){
        angularPredictedAim(e);
    }

    public void onHitRobot(HitRobotEvent e){
        if (e.getBearing() > -90 && e.getBearing() < 90){
			back(50);
		}
		else{
			ahead(50);
		}
    }
    public void onHitWall(HitWallEvent e){
        reverseDirection();
        // if(getOthers()>4){
        //     reverseDirection();
        // }
    }

    // ------------------------ Functions ------------------------
	public void crazyMovement(){
		setAhead(4000);
		movingForward=true;
		setTurnRight(90);
		waitFor(new TurnCompleteCondition(this));
		setTurnLeft(180);
		waitFor(new TurnCompleteCondition(this));
		setTurnRight(180);
		waitFor(new TurnCompleteCondition(this));
	}

    public void wallMovement(){
        peek=true;
        ahead(moveAmount);
        peek=false;
        turnRight(90);       
    }

	public void reverseDirection() {
		if (movingForward) {
			setBack(40000);
			movingForward = false;
		} else {
			setAhead(40000);
			movingForward = true;
		}
	}
  
    public void lockEnemy(ScannedRobotEvent e){
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
		double radarTurn = Utils.normalRelativeAngle(angleToEnemy-getRadarHeadingRadians());
		double extraTurn = Math.min( Math.atan( 36 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
		if(radarTurn<0){
			radarTurn -= extraTurn;
		}else{
			radarTurn += extraTurn;
		}
		setTurnRadarRightRadians(radarTurn);
        linearPredictedAim(e, determinePower(e));        
    }

    public void angularPredictedAim(ScannedRobotEvent e){
        double bulletPower = determinePower(e);
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = e.getVelocity();
        oldEnemyHeading = enemyHeading;
        
        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight(), 
               battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX, predictedY = enemyY;
        while((++deltaTime) * (20.0 - 3.0 * bulletPower) < Math.hypot(Math.abs(getX()-predictedX), Math.abs(getY()-predictedY))){		
            predictedX += Math.sin(enemyHeading) * enemyVelocity;
            predictedY += Math.cos(enemyHeading) * enemyVelocity;
            enemyHeading += enemyHeadingChange;
            if(	predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0){
                predictedX = Math.min(Math.max(18.0, predictedX), 
                    battleFieldWidth - 18.0);	
                predictedY = Math.min(Math.max(18.0, predictedY), 
                    battleFieldHeight - 18.0);
                break;
            }
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
        setFire(bulletPower);
    }

	public void linearPredictedAim(ScannedRobotEvent e, double bulletPower){ //Linear predicted aim.
		double bulletSpeed = 20-(3*bulletPower);
		double deltaTime = 0;
		double predictedX = enemyCoords(e)[0];
		double predictedY = enemyCoords(e)[1];
		double distanceBetween = Math.hypot(Math.abs(getX()-predictedX), Math.abs(getY()-predictedY)); //Distance between self and predicted position.

		while(((++deltaTime)*bulletSpeed)<distanceBetween){ // Falta ajustar con los limites del mapa.
			predictedX += Math.sin(e.getHeading())*e.getVelocity();
			predictedY += Math.cos(e.getHeading())*e.getVelocity();
		}

		double angle = Utils.normalNearAbsoluteAngle(Math.atan2(predictedX-getX(), predictedY-getY()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(angle-getGunHeadingRadians()));
		setFire(bulletPower);
	}

	public double[] enemyCoords(ScannedRobotEvent e){ //Returns enemy coords.
		double myX = getX();
		double myY = getY();
		double absBearing = e.getBearingRadians()+getHeadingRadians();
		double enemyX = myX+e.getDistance()*Math.sin(absBearing);
		double enemyY = myY+e.getDistance()*Math.cos(absBearing);
		double[] coords = {enemyX, enemyY};
		return coords;
	}

	public double determinePower(ScannedRobotEvent e){
		if(e.getDistance()>300){
			return 2;
		}
		if(e.getDistance()>60 && e.getDistance()<=300){
			return 2.5;
		}
		if(e.getDistance()<=60){
			return 3;
		}else{
			return 2;
		}		
	}

    
}

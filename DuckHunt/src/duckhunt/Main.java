package duckhunt;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import sun.awt.windows.WWindowPeer;

public class Main extends PApplet implements ApplicationConstants {

	private static final long serialVersionUID = 1L;

	private boolean initApp_ = setAppForAllClasses();
	/**
	 * The list containing all of the ducks
	 */
	ArrayList<Duck> duck_;
	/**
	 * instance of the Dog
	 */
	Dog dog_;

	/**
	 * Keeps track of how many times the person has hit a duck this is for when
	 * we implement more that one duck
	 */
	private int shotCount = 0;
	/**
	 * Keeps track of how many times the person has tried and shot
	 */
	private int tryCount = 0;
	/**
	 * The background texture
	 */
	PImage _backgroundImage;
	/**
	 * the background instance object
	 */
	Background background_;
	/**
	 * frame counter variable for animation
	 */
	private int frameCounter = 0;
	/**
	 * The sprite containing all of the characters
	 */
	PImage sprite_;
	/**
	 * The timer instance
	 */
	Timer time_;
	/**
	 * the current value when the dog vs duck on screen location current working
	 * on
	 */
	boolean dogAnimate_ = true; // The value of dogAnimate we will be checking
	/**
	 * the current value when the dog vs duck on screen location the value the
	 * timer is returning
	 */
	private static boolean dogAnimateValueFromThread = false;
	/**
	 * determine when to clear the duck
	 */
	boolean offScreen = false;

	/**
	 * end early switch for timer
	 */
	private static boolean switchTimerValue = false;

	public void setup() {
		size(WINDOW_WIDTH, WINDOW_HEIGHT, P3D);
		// Timer(dog, duck)
		Timer time_ = new Timer(5000, 15000);
		Thread timerThread = new Thread(time_);
		timerThread.start();

		frameRate(2000);

		duck_ = new ArrayList<Duck>();

		// load image 0-1
		textureMode(NORMAL);

		// Image loading section
		sprite_ = loadImage("sprite.png");
		_backgroundImage = loadImage("background.jpg");
		PImage ground_ = loadImage("ground.jpg");
		PImage allSprite_ = loadImage("duckhunt_various_sheet.png");

		background_ = new Background(ground_, _backgroundImage, allSprite_);
	}

	public void draw() {
		// Background world setup - etc
		background_.draw();
		// We are still in pixel units.
		translate(1, WINDOW_HEIGHT);
		// change to world units
		scale(WORLD_TO_PIXELS_SCALE, -WORLD_TO_PIXELS_SCALE);

		if (dogAnimateValueFromThread != dogAnimate_) {
			dogAnimate_ = getDogAnimateValueFromThread();
			if (dogAnimate_ == false) {
				// Destroy dog object
				dog_ = null;
				// Instantiate duck object
				// the y parameter needs to be above WORLD_HEIGHT/3
				duck_.add(new Duck(sprite_, WORLD_WIDTH / 2, WORLD_HEIGHT / 2,
						-PI / 4));
				tryCount = 0;
			} else {
				// Destroy duck object
				for (Duck obj : duck_) {
					obj.setVx(0); // temporarily make it not move
					obj.setVy(0);
					obj.setVy(1f); // make it fly up screen
					obj.setLevelEnded();
				}
				tryCount = 0;
				// Instantiate dog object
				dog_ = new Dog(sprite_, 1);
			}
		}
		// draw and animate dog
		if (dog_ != null) {
			dog_.animate();
			dog_.draw();
		}

		// create the wing animation for the duck
		if (frameCounter++ % 6 == 0) {
			for (Duck obj : duck_) {
				if (obj.getShot()) {
					obj.setBirdWing(7);
				} else if (obj.getLevelEnded() && !obj.getShot()) {
					obj.switchBirdWingAway();
				} else
					obj.switchBirdWingFlying();
			}
		}

		// move the duck
		for (Duck obj : duck_)
			obj.animate();

		// make sure the bird is in range and in play, if not remove
		for (int i = 0; i < duck_.size(); i++) {
			duck_.get(i).draw();
			if ((duck_.get(i).getY() < -(WORLD_HEIGHT) && duck_.get(i).getLevelEnded()&& duck_.get(i).getShot())
					|| (duck_.get(i).getY() > (WORLD_HEIGHT) && duck_.get(i).getLevelEnded() && duck_.get(i).getShot())) {
				duck_.remove(duck_.get(i));
			}
		}
		background_.drawGround();
	}

	/**
	 * This controls what happens when you shoot the duck
	 */
	public void mouseReleased() {
		float x = xPixelToWorld(mouseX), y = yPixelToWorld(mouseY);
		boolean wasInside = false;
		for (Duck obj : duck_) {
			if (obj.isInside(x, y)) {
				obj.setVx(0); // temporarily make it not move
				obj.setVy(0);
				delay(500); // do the "pause" thing
				obj.setVy(-1f); // make it fall off screen
				obj.setShot(); // Set "shot" to "false"
				shotCount++;
				wasInside = true;
				break;
			}
		}
		tryCount++;
		println("Try count for click:" + tryCount);
		checkLevelComplete(wasInside);
	}

	/**
	 * this checks if the person has shot all of the ducks in play 1 for now but
	 * added so that can expand to multiple ducks
	 * 
	 * @param wasInside
	 */
	private void checkLevelComplete(boolean wasInside) {
		// if the person has used all of their chances.

		if (tryCount >= 3) {
			for (Duck obj : duck_) {
				obj.levelEnded();
				if (!wasInside) {
					obj.setVx(0); // temporarily make it not move
					obj.setVy(0);
					delay(500); // do the "pause" thing
					obj.setVy(1f); // make it fly up screen
				}
			}
		}

	}

	private float xPixelToWorld(int ix) {
		return X_MIN + PIXELS_TO_WORLD_SCALE * ix;
	}

	private float yPixelToWorld(int iy) {
		return Y_MIN + PIXELS_TO_WORLD_SCALE * (WINDOW_HEIGHT - iy);
	}

	/**
	 * 
	 * @param value
	 */
	public static void setDogAnimateValueFromThread(boolean value) {
		dogAnimateValueFromThread = value;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean getDogAnimateValueFromThread() {
		return dogAnimateValueFromThread;
	}

	/**
	 * 
	 */
	public static void resetSwitchTimerValue() {
		switchTimerValue = false;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean getSwitchTimerValue() {
		return switchTimerValue;
	}

	/**
	 * 
	 * @return
	 */
	private boolean setAppForAllClasses() {
		Duck.setApp(this);
		Dog.setApp(this);
		Background.setApp(this);
		Timer.setApp(this);
		return true;
	}
	
	private float PixelToWorld(int var) {
		return PIXELS_TO_WORLD_SCALE *var;
	}
}

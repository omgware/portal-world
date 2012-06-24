package com.portalworld;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.portalworld.gameobjects.MainCharObject;
import com.portalworld.gameobjects.MovableBoxObject;
import com.portalworld.gameobjects.ObjectInfo;
import com.portalworld.gameobjects.Piece;
import com.portalworld.gameobjects.Portal;

public class GameScreen implements Screen, InputProcessor {

	private GL10 gl = null;
	public static final int WORLD_WIDTH = 48;
	public static final int WORLD_HEIGHT = 32;
	public static final int CAMERA_RIGHT_LIMIT = WORLD_WIDTH - 23;
	public static final int CAMERA_LEFT_LIMIT = -WORLD_WIDTH + 23;
	public static final int CAMERA_UP_LIMIT = (WORLD_HEIGHT*2) - 16;
	public static final int CAMERA_DOWN_LIMIT = 15;
	public static final float GRAVITY_DEFAULT 			= -13.0f;
	public static final boolean IS_DESKTOP 				= true;
	public static final float FORCEMUL_DEFAULT 			= IS_DESKTOP ? 60000 : 60000 / 70;
	public static final float SPEED_BOOST_FORCE			= IS_DESKTOP ? FORCEMUL_DEFAULT * 0.06f : FORCEMUL_DEFAULT * 4;
	public static final float FRICTION_DAMPING 			= IS_DESKTOP ? 0.0001f : 0.005f;
	public static final Vector2 zeroVector				= new Vector2(0,0);
	public static final float MOVEMENT_TIMESTEP 		= 0.05f;
	public static final float MOVEMENT_BOOST_LIMIT 		= 5.0f;
	public static final float MOVEMENT_MIN_RECHARGE 	= 2.0f;
	public static final float PORTAL_FORCE_OUT 			= IS_DESKTOP ? 15000 : 15000 / 60f;
	
	public boolean drawBodies = true;
	public boolean drawJoints = false;
	public boolean drawAAAB = false;
	public float gravity;
	public float forceMul;
	public float dragForceMult = 1000.0f;
	public PortalWorld game;
	
	ArrayList<MovableBoxObject> movableBoxes = new ArrayList<MovableBoxObject>();
	ArrayList<Piece> pieceTemplates = new ArrayList<Piece>();
	public ArrayList<Portal> portalIn = new ArrayList<Portal>();
	public ArrayList<Portal> portalOut = new ArrayList<Portal>();
	public OrthographicCamera camera;
	public Box2DDebugRenderer renderer;
	public SpriteBatch batch;
	public BitmapFont font;
	public World world;
	public Body groundBody;
	public MouseJoint mouseJoint = null;
	public MainCharObject mainChar = null;
	
	// Camera Status variables
	float zoom = 1.0f;
	
	// Char Status variables
	boolean charIsMoving;
	boolean charMovementRecharging;
	boolean destroyMainCharJoints;
	boolean triggerBoxLaunch;
	long leaveBoxDoubleTapWindow;
	
	// for pinch-to-zoom
	int numberOfFingers = 0;
	int fingerOnePointer;
	int fingerTwoPointer;
	float distance;
	float factor;
	float lastDistance = 0;
	Vector3 fingerOne = new Vector3();
	Vector3 fingerTwo = new Vector3();

	// Speed boost
	boolean triggerSpeedBoost;
	boolean triggerSpeedBoostRecharging;
	Vector2 lastTouchCoord = new Vector2(0,0);
	float speedBoostRechargeTime;
	int highSpeedStepCounter;
	boolean startSpeedBoostChecks;
	long speedBoostCheckWindow;
	long speedBoostDoubleTapCheckWindow;
	boolean canApplySpeedBoost;
	
	
	// Temp variables
	public Fixture tempFixture = null;
	public Vector2 forceDirection = new Vector2();
	public Piece newPiece;
	public Body hitBody = null;
	public BodyDef def = new BodyDef();
	public FixtureDef fd = new FixtureDef();
	public Body logicHitBody = null;
	public Vector2 tmp = new Vector2();
	public Vector2 target = new Vector2();
	public Vector3 testPoint = new Vector3();
	public Vector2 testPoint2D = new Vector2();
	public Iterator<Body> bodyIterator = null;
	float tempFloat;
	float movementTimeCheck = 0;
	float movementLimitTimeCheck = MOVEMENT_BOOST_LIMIT;
	
	
	public GameScreen(PortalWorld g) {
    	this(g, GRAVITY_DEFAULT, FORCEMUL_DEFAULT);
    }
	
	public GameScreen(PortalWorld g, float gravity, float forceMul) {
    	this(g, gravity, forceMul, 1000.0f, true, false, false);
    }
	
	public GameScreen(PortalWorld g, float gravity, float forceMul, float dragForceMult, boolean drawBodies, boolean drawJoints, boolean drawAAAB) {
    	game = g;
    	this.gravity = gravity;
    	this.forceMul = forceMul;
    	this.dragForceMult = dragForceMult;
    	this.drawBodies = drawBodies;
    	this.drawJoints = drawJoints;
    	this.drawAAAB = drawAAAB;
    }

	public void createWorld (World world) {
		// Create ground cage
		{
			ChainShape shape = new ChainShape();
			shape.createLoop(new Vector2[] {new Vector2(-WORLD_WIDTH, 0), new Vector2(WORLD_WIDTH, 0), new Vector2(WORLD_WIDTH, WORLD_HEIGHT*2 - 1), new Vector2(-WORLD_WIDTH, WORLD_HEIGHT*2 - 1)});
			fd.shape = shape;
			fd.friction = 0.8f;
			fd.restitution = 0.3f;
			BodyDef bd = new BodyDef();
			Body cage = world.createBody(bd);
			cage.createFixture(fd);
			// dispose shape
			shape.dispose();
		}

		// Populate level with elements
		// PortalIns
		createBodyAndFixture(getNewPieceInstanceFromTemplate(7).setSensor(true).setPortalIn(true), -8, 1);
		portalIn.add(new Portal(tempFixture, 90, PORTAL_FORCE_OUT));
		createBodyAndFixture(getNewPieceInstanceFromTemplate(0).setSensor(true).setPortalIn(true), 22, 2);
		portalIn.add(new Portal(tempFixture, 180, PORTAL_FORCE_OUT));
		createBodyAndFixture(getNewPieceInstanceFromTemplate(0).setSensor(true).setPortalIn(true), -22, 2);
		portalIn.add(new Portal(tempFixture, 0, PORTAL_FORCE_OUT));
		createBodyAndFixture(getNewPieceInstanceFromTemplate(0).setSensor(true).setPortalIn(true), -20, 40);
		portalIn.add(new Portal(tempFixture, 0, PORTAL_FORCE_OUT));
		// PortalOut
		createBodyAndFixture(getNewPieceInstanceFromTemplate(8).setSensor(true).setPortalOut(true), 23, 19);
		portalOut.add(new Portal(tempFixture, 90, PORTAL_FORCE_OUT));
		createBodyAndFixture(getNewPieceInstanceFromTemplate(1).setSensor(true).setPortalIn(true), -23, 28);
		portalOut.add(new Portal(tempFixture, 0, PORTAL_FORCE_OUT));
		createBodyAndFixture(getNewPieceInstanceFromTemplate(1).setSensor(true).setPortalIn(true), -1, 28);
		portalOut.add(new Portal(tempFixture, 0, PORTAL_FORCE_OUT));
		createBodyAndFixture(getNewPieceInstanceFromTemplate(1).setSensor(true).setPortalIn(true), 5, 45);
		portalOut.add(new Portal(tempFixture, 230, PORTAL_FORCE_OUT));
		// All the other elements
		createBodyAndFixture(getNewPieceInstanceFromTemplate(2), -19, 24);
		createBodyAndFixture(getNewPieceInstanceFromTemplate(3), -19, 19);
		createBodyAndFixture(getNewPieceInstanceFromTemplate(3), 0, 12);
		createBodyAndFixture(getNewPieceInstanceFromTemplate(4), 18, 16);
		// Main Char
		newPiece = getNewPieceInstanceFromTemplate(5).setMainChar(true);
		createBodyAndFixture(newPiece, -3, 14);
		mainChar = new MainCharObject(newPiece, newPiece.body);
		// Movable Boxes
		newPiece = getNewPieceInstanceFromTemplate(14).setPortalAllowed(true);
		createBodyAndFixture(newPiece, 2, 14);
		movableBoxes.add(new MovableBoxObject(newPiece, newPiece.body));
		newPiece = getNewPieceInstanceFromTemplate(14).setPortalAllowed(true);
		createBodyAndFixture(newPiece, 13, 19);
		newPiece = getNewPieceInstanceFromTemplate(14).setPortalAllowed(true);
		createBodyAndFixture(newPiece, 32, 3);
		movableBoxes.add(new MovableBoxObject(newPiece, newPiece.body));
		movableBoxes.add(new MovableBoxObject(newPiece, newPiece.body));
	}

	/** Create and save body templates **/
	public void setupPieces() {
		// Reallocate arrays
		pieceTemplates = new ArrayList<Piece>(60);
		portalIn = new ArrayList<Portal>(20);
		portalOut = new ArrayList<Portal>(20);
		/** Portal In vertical **/
		addNewPieceTemplate((new Piece(0.5f, 1.5f, 0, BodyType.StaticBody)).setSensor(true).setPortalIn(true)); // 0
		/** Portal Out vertical **/
		addNewPieceTemplate((new Piece(0.5f, 1.5f, 0, BodyType.StaticBody)).setSensor(true).setPortalOut(true)); // 1
		/** Box (2,4) DynamicBody **/
		addNewPieceTemplate(new Piece(1, 2, 0, BodyType.DynamicBody)); // 2
		/** Box (8,2) DynamicBody **/
		addNewPieceTemplate(new Piece(4, 1, 0, BodyType.StaticBody)); // 3
		/** Box (8,2) DynamicBody **/
		addNewPieceTemplate(new Piece(6, 1, 0, BodyType.StaticBody)); // 4
		/** Circle 1.0 DynamicBody **/
		addNewPieceTemplate(new Piece(1, BodyType.DynamicBody)); // 5
		/** Circle 2.0 DynamicBody **/
		addNewPieceTemplate(new Piece(2, BodyType.DynamicBody)); // 6
		/** Portal In horizontal **/
		addNewPieceTemplate((new Piece(1.5f, 0.5f, 0, BodyType.StaticBody)).setSensor(true).setPortalIn(true)); // 7
		/** Portal Out horizontal **/
		addNewPieceTemplate((new Piece(1.5f, 0.5f, 0, BodyType.StaticBody)).setSensor(true).setPortalOut(true)); // 8
		/** Mini Circle **/
		addNewPieceTemplate(new Piece(0.5f, BodyType.DynamicBody)); // 9
		/** Mini Circle **/
		addNewPieceTemplate(new Piece(0.3f, BodyType.DynamicBody)); // 10
		/** Mini Circle **/
		addNewPieceTemplate(new Piece(0.1f, BodyType.KinematicBody)); // 11
		/** Mini Circle Sensor**/
		addNewPieceTemplate(new Piece(0.3f, BodyType.DynamicBody)).setSensor(true); // 12
		/** Box (20,30) DynamicBody **/
		addNewPieceTemplate(new Piece(10, 15, 0, BodyType.DynamicBody)); // 13
		/** Box (1,1) DynamicBody **/
		addNewPieceTemplate(new Piece(1, 1, 0, BodyType.DynamicBody)); // 14
	}
	
	private Piece getNewPieceInstanceFromTemplate(int templateIndex) {
		return new Piece(pieceTemplates.get(templateIndex));
	}

	@Override
	public void show() {
		// Setup all the game elements once
		setupPieces();
		camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
		camera.position.set(0, (WORLD_HEIGHT/2)-1, 0);

		renderer = new Box2DDebugRenderer(drawBodies, drawJoints, drawAAAB, true);
		world = new World(new Vector2(0, gravity), true);

		BodyDef bodyDef = new BodyDef();
		groundBody = world.createBody(bodyDef);
		createWorld(world);

		batch = new SpriteBatch(1000);
		font = new BitmapFont();

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render(float deltaTime) {
		world.step(deltaTime, 3, 3);
		performLogic(deltaTime);
		if (gl == null)
			gl = Gdx.app.getGraphics().getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		updateCameraPosition();
		camera.zoom = zoom;
		camera.update();
		camera.apply(gl);

		renderer.render(world, camera.combined);

		batch.begin();
		font.draw(batch,  "GAME SCREEN " + "fps:" + Gdx.graphics.getFramesPerSecond() + " fuel: " + movementLimitTimeCheck, 0, 15);
		font.draw(batch,  "(" + (int)testPoint.x + "," + (int)testPoint.y + ")", 10, Gdx.graphics.getHeight());
		batch.end();
	}
	
	private void updateCameraPosition() {
		tmp.set(mainChar.body.getPosition());
		// limit the view on the level angles
		if (tmp.x > CAMERA_RIGHT_LIMIT)
			tmp.x = CAMERA_RIGHT_LIMIT;
		else if (tmp.x < CAMERA_LEFT_LIMIT)
			tmp.x = CAMERA_LEFT_LIMIT;
		if (tmp.y > CAMERA_UP_LIMIT)
			tmp.y = CAMERA_UP_LIMIT;
		else if (tmp.y < CAMERA_DOWN_LIMIT)
			tmp.y = CAMERA_DOWN_LIMIT;
		camera.position.set(tmp.x, tmp.y, 0);
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// Change Zoom
		if (keycode == Input.Keys.PLUS)
			zoom -= 0.2f;
		else if (keycode == Input.Keys.MINUS)
			zoom += 0.2f;
		
		// Exit
		else if (keycode == Input.Keys.ESCAPE)
			Gdx.app.exit();
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture (Fixture fixture) {
			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// for pinch-to-zoom
		numberOfFingers++;
		if (!IS_DESKTOP) {
			if (numberOfFingers == 1) {
				fingerOnePointer = pointer;
				fingerOne.set(x, y, 0);
			}
			else if (numberOfFingers == 2) {
				fingerTwoPointer = pointer;
				fingerTwo.set(x, y, 0);
				lastDistance = fingerOne.dst2(fingerTwo);
				return false;
			}
		}
		// translate the mouse coordinates to world coordinates
		camera.unproject(testPoint.set(x, y, 0));
		testPoint2D.x = testPoint.x;
		testPoint2D.y = testPoint.y;
		if ((testPoint2D.dst2(mainChar.body.getPosition()) <= 200)) {
			startSpeedBoostChecks = true;
			lastTouchCoord.set(testPoint2D);
			speedBoostCheckWindow = System.currentTimeMillis();
		}
		if (mainChar.boxAttached == null && ((System.currentTimeMillis() - speedBoostDoubleTapCheckWindow) < 150)) {
			triggerSpeedBoost = true;
			highSpeedStepCounter = 0;
			speedBoostDoubleTapCheckWindow = 0;
		}
		else if (forceMul > 0 && (testPoint2D.dst2(mainChar.body.getPosition()) <= 200)){
			charIsMoving = true;
		}
		// check for movable boxes
		if (mainChar.boxAttached != null && ((System.currentTimeMillis() - leaveBoxDoubleTapWindow) < 200)) {
			if ((testPoint2D.dst2(mainChar.boxAttached.body.getPosition()) <= 5)) {
				startSpeedBoostChecks = false;
				destroyMainCharJoints = true;
				lastTouchCoord.set(testPoint2D);
				leaveBoxDoubleTapWindow = 0;
			}
		}
		if (mainChar.boxAttached == null) {
			for (MovableBoxObject box: movableBoxes) {
				if (!box.isAttached && (testPoint2D.dst2(box.body.getPosition()) < 5) && (mainChar.body.getPosition().dst2(box.body.getPosition()) < 25)) {
					mainChar.attachBox(box);
					box.isAttached = true;
					box.body.setGravityScale(0);
					//System.out.println("BOX ATTACHED");
					// create distance joint
					DistanceJointDef djd = new DistanceJointDef();
					djd.initialize(mainChar.body, box.body, mainChar.body.getPosition(), box.body.getPosition());
					djd.collideConnected = true;
					djd.dampingRatio = 0;
					djd.length = 3;
					mainChar.joint1 = world.createJoint(djd);
					// create revolute joint
					/*RevoluteJointDef rjd = new RevoluteJointDef();
					rjd.initialize(mainChar.body, box.body, mainChar.body.getPosition());
					rjd.maxMotorTorque = 50.0f;
					rjd.enableMotor = true;
					rjd.motorSpeed = 10;
					mainChar.joint2 = world.createJoint(rjd);*/
					break;
				}
			}
		}

		return false;
	}
	
	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// for pinch-to-zoom
		if (!IS_DESKTOP) {
			if (numberOfFingers == 2) {
				if (pointer == fingerOnePointer)
				       fingerOne.set(x, y, 0);
				if (pointer == fingerTwoPointer)
				       fingerTwo.set(x, y, 0);
				distance = fingerOne.dst2(fingerTwo);
				//factor = distance / lastDistance / 4;
				if (lastDistance > distance)
					zoom += 0.01f;
				else if (lastDistance < distance)
					zoom -= 0.01f;
				lastDistance = distance;
				return false;
			}
		}
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		camera.unproject(testPoint.set(x, y, 0));
		testPoint2D.x = testPoint.x;
		testPoint2D.y = testPoint.y;
		// Check speed boost
		if (!triggerSpeedBoostRecharging && startSpeedBoostChecks && mainChar.boxAttached == null) {
			//1 second window for these checks, otherwise it's surely a normal movement
			if ((System.currentTimeMillis() - speedBoostCheckWindow) >= 1000) {
				startSpeedBoostChecks = false;
				speedBoostCheckWindow = 0;
			}
			tempFloat = lastTouchCoord.dst2(testPoint2D);
			//System.out.println("distFromLast: " + tempFloat);
			if (tempFloat > 1 && tempFloat < 3)
				highSpeedStepCounter++;
			else
				highSpeedStepCounter = 0;
			if (highSpeedStepCounter >= 2) {
				triggerSpeedBoost = true;
				highSpeedStepCounter = 0;
			}
			lastTouchCoord.set(testPoint2D);
		}
		// Box launch check
		else if (startSpeedBoostChecks && mainChar.boxAttached != null) {
			//1 second window for these checks, otherwise it's surely a normal movement
			if ((System.currentTimeMillis() - speedBoostCheckWindow) >= 1000) {
				startSpeedBoostChecks = false;
				speedBoostCheckWindow = 0;
			}
			tempFloat = lastTouchCoord.dst2(testPoint2D);
			if (tempFloat > 1 && tempFloat < 3)
				highSpeedStepCounter++;
			else
				highSpeedStepCounter = 0;
			if (highSpeedStepCounter >= 2) {
				startSpeedBoostChecks = false;
				triggerBoxLaunch = true;
				highSpeedStepCounter = 0;
			}
			lastTouchCoord.set(testPoint2D);
		}
			
		if (testPoint2D.dst2(mainChar.body.getPosition()) <= 150)
			charIsMoving = true;
		else
			charIsMoving = false;
		if (mouseJoint != null) {
			mouseJoint.setTarget(target.set(testPoint.x, testPoint.y));
		}
		
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		camera.unproject(testPoint.set(x, y, 0));
		testPoint2D.x = testPoint.x;
		testPoint2D.y = testPoint.y;
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		hitBody = null;
		charIsMoving = false;
		
		// for pinch-to-zoom     
		 numberOfFingers--;
		// just some error prevention... clamping number of fingers (ouch! :-)
		 if(numberOfFingers<0)
		        numberOfFingers = 0;
		lastDistance = 0;
		if (numberOfFingers == 0) {
			startSpeedBoostChecks = false;
			speedBoostCheckWindow = 0;
			highSpeedStepCounter = 0;
			triggerSpeedBoost = false;
		}
		speedBoostDoubleTapCheckWindow = System.currentTimeMillis();
		if (mainChar.boxAttached != null) {
			if ((testPoint2D.dst2(mainChar.boxAttached.body.getPosition()) <= 5)) {
				leaveBoxDoubleTapWindow = System.currentTimeMillis();
			}
			else
				leaveBoxDoubleTapWindow = 0;
		}
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		camera.unproject(testPoint.set(x, y, 0));
		testPoint2D.x = testPoint.x;
		testPoint2D.y = testPoint.y;
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		if (renderer != null)
			renderer.dispose();
		if (world != null)
			world.dispose();
		pieceTemplates.clear();
		portalIn.clear();
		portalOut.clear();

		renderer = null;
		world = null;
		mouseJoint = null;
		hitBody = null;
		pieceTemplates = null;
		portalIn = null;
		portalOut = null;
		bodyIterator = null;
	}

	@Override
	public void pause() {
		// some error prevention...
		 numberOfFingers = 0;
	}

	@Override
	public void hide() {
		dispose();
	}
	
	public Piece addNewPieceTemplate(Piece piece) {
		pieceTemplates.add(piece);
		return piece;
	}	
	
	public Body createBodyAndFixture(Piece piece, float x, float y) {
		piece.pos.x = x;
		piece.pos.y = y;
		def.position.x = x;
		def.position.y = y;
		def.type = piece.type;
		Body body = world.createBody(def);
		if (body.getType() == BodyType.StaticBody) {
			fd.shape = piece.shape;
			tempFixture = body.createFixture(fd);
		}
		else {
			tempFixture = body.createFixture(piece.shape, 1);
			tempFixture.setFriction(0.6f);
			tempFixture.setRestitution(0.4f);
		}
		tempFixture.setSensor(piece.isSensor);
		piece.setBody(body);
		// introduce ObjectInfo as UserData
		if (body.getType() == BodyType.DynamicBody || body.getType() == BodyType.KinematicBody) {
			tempFixture.getBody().setUserData(new ObjectInfo(piece));
			if (piece.shape instanceof CircleShape) {
				((ObjectInfo)tempFixture.getBody().getUserData()).isSphere = true;
			}
			if (piece.isPortalAllowed) {
				((ObjectInfo)tempFixture.getBody().getUserData()).isPortalAllowed = true;
			}
			if (piece.isMainChar) {
				((ObjectInfo)tempFixture.getBody().getUserData()).isMainChar = true;
			}
		}
		return body;
	}

	public void createMouseJoint() {
		MouseJointDef def = new MouseJointDef();
		def.bodyA = groundBody;
		def.bodyB = hitBody;
		def.collideConnected = true;
		def.target.set(testPoint.x, testPoint.y);
		def.maxForce = dragForceMult * hitBody.getMass();
		
		mouseJoint = (MouseJoint)world.createJoint(def);
		hitBody.setAwake(true);
	}
	
	private void destroyMainCharJoints() {
		while(!mainChar.body.getJointList().isEmpty()) {
			 world.destroyJoint(mainChar.body.getJointList().get(0).joint);	
		}
		destroyMainCharJoints = false;
		if (mainChar.boxAttached != null) {
			mainChar.boxAttached.isAttached = false;
			mainChar.boxAttached.body.setGravityScale(1);
		}
	}
	
	public QueryCallback portalInCallback = new QueryCallback() {
		@Override
		public boolean reportFixture (Fixture fixture) {
			if (fixture.getBody().getType() != BodyType.StaticBody && !fixture.isSensor() && ((ObjectInfo)fixture.getBody().getUserData()).isPortalAllowed) {
				// Prevent portal looping
				if (!((ObjectInfo)fixture.getBody().getUserData()).hasTimePassed(300))
					return true;
				for (int i=0; i<portalIn.size(); i++) {
					if (portalIn.get(i).fixture.testPoint(fixture.getBody().getPosition().x, fixture.getBody().getPosition().y)) {
						logicHitBody = fixture.getBody();
						if (logicHitBody != null) {
							logicHitBody.setTransform(portalOut.get(i).getBody().getPosition(), 0);
							if (portalOut.get(i).normal != null) {
								// New velocity angle
								//System.out.println("vel: "+logicHitBody.getLinearVelocity().angle()+" norm: " + portalOut.get(i).normal.angle()+" angle: " + portalOut.get(i).angle);
								logicHitBody.setLinearVelocity(logicHitBody.getLinearVelocity().rotate(portalOut.get(i).angle - logicHitBody.getLinearVelocity().angle()));
								// Apply a little more linear force
								logicHitBody.applyForceToCenter(portalOut.get(i).transferForce);
								// Disable boost
								if (triggerSpeedBoostRecharging && mainChar.body == logicHitBody)
									canApplySpeedBoost = false;
							}
							// Destroy Joint
							if (mainChar.body == logicHitBody || (mainChar.boxAttached != null && mainChar.boxAttached.body == logicHitBody)) {
								destroyMainCharJoints = true;
							}
							if (fixture.getBody().getUserData() != null)
								((ObjectInfo)fixture.getBody().getUserData()).updateTime();
						}
					}
				}
			}
			return true;
		}
	};
	
	
	public QueryCallback portalOutCallback = new QueryCallback() {
		@Override
		public boolean reportFixture (Fixture fixture) {
			if (fixture.getBody().getType() != BodyType.StaticBody && !fixture.isSensor() && ((ObjectInfo)fixture.getBody().getUserData()).isPortalAllowed) {
				// Prevent portal looping
				if (!((ObjectInfo)fixture.getBody().getUserData()).hasTimePassed(300))
					return true;
				for (int i=0; i<portalIn.size(); i++) {
					if (portalOut.get(i).fixture.testPoint(fixture.getBody().getPosition().x, fixture.getBody().getPosition().y)) {
						logicHitBody = fixture.getBody();
						if (logicHitBody != null) {
							logicHitBody.setTransform(portalIn.get(i).getBody().getPosition(), 0);
							if (portalIn.get(i).normal != null) {
								// New velocity angle
								logicHitBody.setLinearVelocity(logicHitBody.getLinearVelocity().rotate(portalIn.get(i).normal.angle() - logicHitBody.getLinearVelocity().angle()));
								// Apply a little more linear force
								logicHitBody.applyForceToCenter(portalIn.get(i).transferForce);
								// Disable boost
								if (triggerSpeedBoostRecharging && mainChar.body == logicHitBody)
									canApplySpeedBoost = false;
							}
							// Destroy Joint
							if (mainChar.body == logicHitBody || (mainChar.boxAttached != null && mainChar.boxAttached.body == logicHitBody)) {
								destroyMainCharJoints = true;
							}
							if (fixture.getBody().getUserData() != null)
								((ObjectInfo)fixture.getBody().getUserData()).updateTime();
						}
					}
				}
			}
			return true;
		}
	};
	
	
	public void performLogic(float deltaTime) {
		/** CHAR MOVEMENT **/
		// Speed Boost handling
		if (triggerSpeedBoost && movementLimitTimeCheck >= 2) {
			movementLimitTimeCheck -= 2;
			triggerSpeedBoost = false;
			canApplySpeedBoost = true;
			triggerSpeedBoostRecharging = true;
			forceDirection.set(testPoint2D.x, testPoint2D.y);
			forceDirection.sub(mainChar.body.getPosition());
			forceDirection.nor();
			forceDirection.mul(MOVEMENT_TIMESTEP * SPEED_BOOST_FORCE);
			//System.out.println("SPEED BOOST!!");
			//System.out.println("velocity (" + mainChar.body.getLinearVelocity().x + " , " + mainChar.body.getLinearVelocity().y + ")");
		}
		if (triggerSpeedBoostRecharging) {
			speedBoostRechargeTime += deltaTime;
			if (speedBoostRechargeTime >= 1) {
				speedBoostRechargeTime = 0;
				triggerSpeedBoostRecharging = false;
				canApplySpeedBoost = false;
				// restart checks
				startSpeedBoostChecks = false;
				//System.out.println("RECHARGE END");
			}
			//forceDirection.mul(0.99995f);
			if (canApplySpeedBoost)
				mainChar.body.applyForceToCenter(forceDirection);
		}
		// Normal movement
		else if (charIsMoving && !charMovementRecharging) {
			// increase movement time step check
			movementTimeCheck += deltaTime;
			// apply force if movement time step elapsed
			if (movementTimeCheck >= MOVEMENT_TIMESTEP) {
				movementTimeCheck -= MOVEMENT_TIMESTEP;
				forceDirection.set(testPoint2D.x, testPoint2D.y);
				forceDirection.sub(mainChar.body.getPosition());
				forceDirection.mul(forceMul * MOVEMENT_TIMESTEP);
				mainChar.body.applyForceToCenter(forceDirection);
			}
			// decrease engine fuel
			movementLimitTimeCheck -= deltaTime;
			// put the engine in forced recharge mode
			if (movementLimitTimeCheck <= 0) {
				charMovementRecharging = true;
			}
		}
		// when under forced recharge mode, the engine stops working until it recharges at least for 1 second
		else if (charMovementRecharging) {
			movementLimitTimeCheck += deltaTime;
			if (movementLimitTimeCheck >= MOVEMENT_MIN_RECHARGE)
				charMovementRecharging = false;
			movementTimeCheck = 0;
		}
		else {
			movementTimeCheck = 0;
			// increase engine fuel till the limit
			movementLimitTimeCheck += deltaTime;
			if (movementLimitTimeCheck > MOVEMENT_BOOST_LIMIT)
				movementLimitTimeCheck = MOVEMENT_BOOST_LIMIT;
		}
		/** ATTACHED BOXES MOVEMENT **/
		if (destroyMainCharJoints) {
			destroyMainCharJoints();
			mainChar.boxAttached = null;
		}
		if (triggerBoxLaunch) {
			if (mainChar.boxAttached != null) {
				destroyMainCharJoints();
				forceDirection.set(testPoint2D.x, testPoint2D.y);
				forceDirection.sub(mainChar.body.getPosition());
				forceDirection.nor();
				if (!IS_DESKTOP)
					forceDirection.mul(SPEED_BOOST_FORCE * 2);
				else
					forceDirection.mul(SPEED_BOOST_FORCE * 80);
				// TODO: HANDLE A TRANSITION HERE!!!!
				mainChar.boxAttached.body.setTransform(mainChar.body.getPosition(), 0);
				mainChar.boxAttached.body.setLinearVelocity(0, 0);
				mainChar.boxAttached.body.setAngularVelocity(0);
				mainChar.boxAttached.body.applyForceToCenter(forceDirection);
				mainChar.boxAttached = null;
			}
			triggerBoxLaunch = false;
		}
		
		/** PORTALS **/
		logicHitBody = null;
		if (portalIn.size() > 0 && portalOut.size() > 0 && portalIn.size() == portalOut.size()) {
			for (int i=0; i<portalIn.size(); i++) {
				world.QueryAABB(portalInCallback, portalIn.get(i).getX() - 0.5f, portalIn.get(i).getY() - 1.5f, portalIn.get(i).getX() + 0.5f, portalIn.get(i).getY() + 1.5f);
				world.QueryAABB(portalOutCallback, portalOut.get(i).getX() - 0.5f, portalOut.get(i).getY() - 1.5f, portalOut.get(i).getX() + 0.5f, portalOut.get(i).getY() + 1.5f);
			}
		}
		
		/** PHYSICS **/
		bodyIterator = world.getBodies();
		while (bodyIterator.hasNext()) {
			logicHitBody = bodyIterator.next();
			if (logicHitBody.getType() != BodyType.DynamicBody || (logicHitBody.getUserData() != null && (!((ObjectInfo)logicHitBody.getUserData()).isSphere)))
				continue;
			// apply air friction
			logicHitBody.setLinearVelocity(logicHitBody.getLinearVelocity().mul(1 - (FRICTION_DAMPING / logicHitBody.getMass())));
			logicHitBody.setAngularVelocity(logicHitBody.getAngularVelocity() * (1 - (FRICTION_DAMPING / logicHitBody.getMass())));
		}
	}

}

package com.portalworld.gameobjects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public class Piece {
	
	public Shape shape;
	public Vector2 pos;
	public BodyType type;
	public Body body;
	public float friction = 0.5f;
	public float restitution = 0.5f;
	public float gravityScale = 1.0f;
	public boolean isMainChar = false;
	public boolean isPortalAllowed = false;
	public boolean isSensor = false;
	public boolean isPortalIn = false;
	public boolean isPortalOut = false;
	public boolean isCreated = false;
	public static final Vector2 zeroVector 		= new Vector2(0,0);
	// For ropes
	public int numberOfJoints;
	
	public Piece(Piece anotherPiece) {
		this.shape = anotherPiece.shape;
		if (anotherPiece.pos != null)
			this.pos = new Vector2(anotherPiece.pos);
		this.type = anotherPiece.type;
		this.body = anotherPiece.body;
		this.friction = anotherPiece.friction;
		this.restitution = anotherPiece.restitution;
		this.gravityScale = anotherPiece.gravityScale;
		this.isMainChar = anotherPiece.isMainChar;
		this.isPortalAllowed = anotherPiece.isPortalAllowed;
		this.isSensor = anotherPiece.isSensor;
		this.isPortalIn = anotherPiece.isPortalIn;
		this.isPortalOut = anotherPiece.isPortalOut;
		this.isCreated = anotherPiece.isCreated;
	}
	
	public Piece (float radius, BodyType type) {
		this.shape = new CircleShape();
		((CircleShape)this.shape).setRadius(radius);
		this.pos = new Vector2(0,0);
		this.type = type;
	}
	
	public Piece (float halfWidth, float halfHeight, float angle, BodyType type) {
		this.shape = new PolygonShape();
		((PolygonShape)this.shape).setAsBox(halfWidth, halfHeight, zeroVector, 0);
		this.pos = new Vector2(0,0);
		this.type = type;
	}
	
	public Piece (BodyType type, Vector2... pos) {
		this.shape = new ChainShape();
		((ChainShape)this.shape).createLoop(pos);
		this.pos = null;
		this.type = type;
	}
	
	public Piece (BodyType type, int numberOfJoints) {
		this.shape = new PolygonShape();
		((PolygonShape)this.shape).setAsBox(0.6f, 0.125f);
		this.pos = null;
		this.type = type;
		this.numberOfJoints = numberOfJoints;
	}
	
	public Piece setPhysics(float friction, float restitution, float gravityScale, boolean isSensor) {
		this.friction = friction;
		this.restitution = restitution;
		this.gravityScale = gravityScale;
		this.isSensor = isSensor;
		return this;
	}
	
	public Piece setSensor(boolean value) {
		this.isSensor = value;
		return this;
	}
	
	public Piece setBody(Body body) {
		this.body = body;
		isCreated = true;
		return this;
	}
	
	public Piece setPortalIn(boolean value) {
		this.isPortalIn = value;
		return this;
	}
	
	public Piece setPortalOut(boolean value) {
		this.isPortalOut = value;
		return this;
	}
	
	public Piece setMainChar(boolean value) {
		this.isMainChar = value;
		this.isPortalAllowed = true;
		return this;
	}
	
	public Piece setPortalAllowed(boolean value) {
		this.isPortalAllowed = value;
		return this;
	}
}
package com.portalworld.gameobjects;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;

public class MainCharObject extends GameObject {
	
	public MovableBoxObject boxAttached;
	public Joint joint1;
	public Joint joint2;

	public MainCharObject(Piece pieceInfo, Body body) {
		super(pieceInfo, body);
	}

	public void attachBox(MovableBoxObject box) {
		this.boxAttached = box;
	}
}

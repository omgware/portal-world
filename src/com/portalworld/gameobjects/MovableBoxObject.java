package com.portalworld.gameobjects;

import com.badlogic.gdx.physics.box2d.Body;

public class MovableBoxObject extends GameObject {
	
	public boolean isAttached;

	public MovableBoxObject(Piece pieceInfo, Body body) {
		super(pieceInfo, body);
	}

}

package com.portalworld.gameobjects;

import com.badlogic.gdx.physics.box2d.Body;

public class GameObject {
	public Piece pieceInfo;
	public Body body;
	
	GameObject (Piece pieceInfo, Body body) {
		this.pieceInfo = pieceInfo;
		this.body = body;
	}
}

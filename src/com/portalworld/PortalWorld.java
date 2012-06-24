package com.portalworld;

import com.badlogic.gdx.Game;


public class PortalWorld extends Game {
	GameScreen gameScreen;

	@Override
	public void create() {
		setScreen(switchToGame());
	}
	
	public GameScreen switchToGame() {
		if (gameScreen == null)
			gameScreen = new GameScreen(this);
		return gameScreen;
	}

}

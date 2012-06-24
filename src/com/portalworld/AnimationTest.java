package com.portalworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationTest implements Screen {
	
	PortalWorld game;
	private static final int        FRAME_COLS = 6;
    private static final int        FRAME_ROWS = 5;
    private static final int 		SPEED = 4;
    
    Animation                       walkAnimation;
    Texture                         walkSheet;
    TextureRegion[]                 walkFrames;
    SpriteBatch                     spriteBatch;
    TextureRegion                   currentFrame;
    
    float stateTime;
    int i = -50;
    boolean isAtBorder;
    
    public AnimationTest(PortalWorld g) {
    	game = g;
    }

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        spriteBatch.begin();
        spriteBatch.draw(currentFrame, i, 50);
        if (isAtBorder)
        	spriteBatch.draw(currentFrame, i - Gdx.graphics.getWidth(), 50);
        spriteBatch.end();
        i += SPEED;
        isAtBorder = (i >= Gdx.graphics.getWidth() - 60) ? true : false;
        if (i >= Gdx.graphics.getWidth()) {
        	i = i - Gdx.graphics.getWidth();
        	isAtBorder = false;
        }

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
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		walkSheet = new Texture(Gdx.files.internal("data/animation_sheet.png"));
        TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth() / FRAME_COLS, walkSheet.getHeight() / FRAME_ROWS);
        walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
                for (int j = 0; j < FRAME_COLS; j++) {
                        walkFrames[index++] = tmp[i][j];
                }
        }
        walkAnimation = new Animation(0.025f, walkFrames);
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
	}


}

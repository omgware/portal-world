package com.portalworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderTest implements Screen {
	
	PortalWorld game;
    SpriteBatch batch;
    ShaderProgram shader;
    ShaderProgram postShader;
    Mesh mesh;
    
    public ShaderTest(PortalWorld g) {
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
		shader.begin();
	    mesh.render( shader, GL10.GL_TRIANGLES );
	    shader.end();
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
		batch = new SpriteBatch();
        
		shader = new ShaderProgram(Gdx.files.internal("data/shaders/default1.vert").readString(), 
				Gdx.files.internal("data/shaders/default1.frag").readString());
		if (!shader.isCompiled()) {
			Gdx.app.log("ShaderTest", "couldn't compile scene shader: " + shader.getLog());
		}
        
		postShader = new ShaderProgram(Gdx.files.internal("data/shaders/default1.vert").readString(), 
				Gdx.files.internal("data/shaders/default1.frag").readString());
		if (!postShader.isCompiled()) {
			Gdx.app.log("ShaderTest", "couldn't compile post process shader: " + postShader.getLog());
		}
		mesh = new Mesh(true, 3, 0, new VertexAttribute( Usage.Position, 2, "a_position" ), new VertexAttribute( Usage.ColorPacked, 4, "a_color" ) );
		mesh.setVertices( new float[] { -0.5f, -0.5f, Color.toFloatBits(255, 0, 0, 255),
		                      0.5f, -0.5f, Color.toFloatBits(0, 255, 0, 255),
		                      0, 0.5f, Color.toFloatBits(0, 0, 255, 255) } );

		batch.setShader(postShader);
	}


}

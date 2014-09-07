package com.cannon.craft;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TickerText;
import org.andengine.entity.text.TickerText.TickerTextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;

import android.graphics.Typeface;
import android.opengl.GLES20;

public class CreditsActivity extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private Font mFont;


	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.SERIF, Typeface.ITALIC), 24);
		this.mFont.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

		String credits = 
				"		Designed and developed by Anubha Banerjee\n\n" +		
				"Seagull animation from the Siput Scuba Website-\n" +
		
				"Designer/Illustrator: Marcus Chee\n\n" +

				"Balloons by devianart - userID wisseh \n\n" + "Ship body from marapets wikia user Laimay\n\n" +

				"waves design by Andy Sykes youtube\n\n" +

				"Game background music by by Setuniman on Freesound\n\n" + 
				"		Made using AndEngine ";
		
		final Text text = new TickerText(CAMERA_WIDTH*0.08f, CAMERA_HEIGHT*0.1f, this.mFont, credits, new TickerTextOptions(HorizontalAlign.CENTER, 15), this.getVertexBufferObjectManager());
		text.registerEntityModifier(
			new SequenceEntityModifier(
				new ParallelEntityModifier(
					new AlphaModifier(10, 0.0f, 1.0f),
					new ScaleModifier(10, 0.5f, 1.0f)
				),
				new RotationModifier(5, 0, 360)
			)
		);
		text.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		scene.attachChild(text);

		return scene;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

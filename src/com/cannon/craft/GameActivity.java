package com.cannon.craft;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TickerText;
import org.andengine.entity.text.TickerText.TickerTextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;
import android.view.View;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {

	//===================The game constants====================
	private static final int CAMERA_WIDTH = 960;
	private static final int CAMERA_HEIGHT = 540;
	private static final int FONT_SIZE_MEDIUM = 30;
	private static final int FONT_SIZE_LARGE = 80;
	private static final int INITIAL_CANNONS = 10;
	private static final float INITIAL_MAX_BALLOON_SPEED = 200;
	private static final float INITIAL_MAX_BIRD_SPEED = -0.2f;
	private static final float INITIAL_BIRD_SPEED = -4;
	private static final float INITIAL_BIRD_FREQUENCY = 8;
	private static final float INITIAL_BALOON_FREQUENCY = 1;
	private static final float INITIAL_SHIP_SPEED = -120;
	private static final float MAX_SHIP_SPEED = -350;
	private static final float SHIP_SPEED_GAIN = 0.1f;
	final static short GROUP_SEAGULL = -1;
	final static short GROUP_CANNON = -2;
	//==========================================================
	
	
	//===================The game characters====================
	private static Ship ship; 
	private List<Bird> bird = new ArrayList<Bird>();
	private List<Bird> birdsToClear = new ArrayList<Bird>();
	private List<CannonBall> cannonBall = new ArrayList<CannonBall>();
	private List<Balloon> balloon = new ArrayList<Balloon>();
	Sky sky;
	//==========================================================
	
	
	//===================The game variables====================
	private static int totalCannonLeftCount = INITIAL_CANNONS;
	public static int scoreCount = 0;	
	public boolean isSkyRed = false;
	private Scene scene, gameOverPopup;
	private PhysicsWorld mPhysicsWorld;
	private static int PhysicsHandler_Box2D_velocityRatio = 30;
	Camera camera; 
	private CameraScene mPauseScene;
	private static float screen_touchX = 0;
	private static float screen_touchY = 0;
	//==========================================================

	//===================The game sounds========================
	private Sound cannonSound, splashSound, goodScreamSound, badScreamSound, popSound, gameOverSound, whoopSound;
	private Music gameBackground;
	//==========================================================
	
	
	//===================The game textures====================
	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
	private BuildableBitmapTextureAtlas longBitmapTextureAtlas;
	private ITexture shipTexture;
	private ITextureRegion shipTextureRegion;
	private ITexture wavesTexture;
	private ITextureRegion wavesTextureRegion;
	private ITexture wavesFrontTexture;
	private ITextureRegion wavesFrontTextureRegion;
	private ITexture skyTexture;
	private ITextureRegion skyTextureRegion;
	private TiledTextureRegion seagullTextureRegion;
	private ITexture cannonBallTexture;
	private ITextureRegion cannonBallTextureRegion;	
	private ITexture balloonTexture;
	private List<ITextureRegion> balloonTextureRegion = new ArrayList<ITextureRegion>();
	private ITexture replayTexture;
	private ITextureRegion replayTextureRegion;
	private ITexture shareTexture;
	private ITextureRegion shareTextureRegion;
	private ITexture closeTexture;
	private ITextureRegion closeTextureRegion;
	private ITexture pauseTexture;
	private ITextureRegion pauseTextureRegion;
	private ITexture playTexture;
	private ITextureRegion playTextureRegion;
	//==========================================================
	
	//===================The game fonts=========================
	private Font droidFont;
	private Font mPlokFont;
	
	//==========================================================

	private void resetGame() {
		gameBackground.play();
		// detach birds
		for(int i = 0; i < bird.size(); i++) {
			scene.detachChild(bird.get(i).bird);
		}
		// detach birds which werent removed and rotating on screen
		for(int i = 0; i < birdsToClear.size(); i++) {
			scene.detachChild(birdsToClear.get(i).bird);
		}
		// detach balloons
		for(int i = 0; i < balloon.size(); i++) {
			scene.detachChild(balloon.get(i).balloon);
		}
		// detach cannons
		for(int i = 0; i < cannonBall.size(); i++) {
			scene.detachChild(cannonBall.get(i).cannonBall);
		}
		bird.clear();
		cannonBall.clear();
		balloon.clear();
		totalCannonLeftCount = INITIAL_CANNONS;
		scoreCount = 0;

		Balloon.currentMaxBalloonSpeed = INITIAL_MAX_BALLOON_SPEED;		
		Bird.currentMaxBirdSpeed = INITIAL_MAX_BIRD_SPEED;
		Bird.currentBirdSpeed = INITIAL_BIRD_SPEED;
		Bird.currentBirdFrequency = INITIAL_BIRD_FREQUENCY;		
		Bird.newBird = false;
		Bird.redBirdProb = 0.5f;
		Bird.whiteBirdProb = 0.4f;
		Bird.blackBirdProb = 0.1f;
		Ship.shipSpeed = INITIAL_SHIP_SPEED;
		Waves.speed = INITIAL_SHIP_SPEED;
		gameOverPopup.detachChildren();
		initGameOverPopup();	
		sky.setColor(org.andengine.util.color.Color.WHITE);
	}
	
	@SuppressLint("NewApi") @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
		}
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);		
		engineOptions.getAudioOptions().setNeedsSound(true);
		engineOptions.getAudioOptions().setNeedsMusic(true);		
		return engineOptions;	
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("sprites/");
		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 1024, 512, TextureOptions.NEAREST);
		this.longBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 2048, 1024, TextureOptions.NEAREST);
		
		// load the textures
		try {
			// load the ship texture
			this.shipTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/clementa.png");
				}
			});
			this.shipTexture.load();
			this.shipTextureRegion = TextureRegionFactory.extractFromTexture(this.shipTexture);

			// load the waves texture
			this.wavesTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/waves.png");
				}
			});
			this.wavesTexture.load();
			this.wavesTextureRegion = TextureRegionFactory.extractFromTexture(this.wavesTexture);

			// load the waves-front texture
			this.wavesFrontTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/wavesfront.png");
				}
			});
			this.wavesFrontTexture.load();
			this.wavesFrontTextureRegion = TextureRegionFactory.extractFromTexture(this.wavesFrontTexture);

			// load the sky texture
			this.skyTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/sky.jpg");
				}
			});
			this.skyTexture.load();
			this.skyTextureRegion = TextureRegionFactory.extractFromTexture(this.skyTexture);
			
			
			// load the cannon-ball texture
			this.cannonBallTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/cannon_ball.png");
				}
			});
			this.cannonBallTexture.load();
			this.cannonBallTextureRegion = TextureRegionFactory.extractFromTexture(this.cannonBallTexture);
			
			// load the orange-balloon texture
			this.balloonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/orange_balloon.png");
				}
			});
			this.balloonTexture.load();
			this.balloonTextureRegion.add(TextureRegionFactory.extractFromTexture(this.balloonTexture));
			
			
			// load the green-balloon texture
			this.balloonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/green_balloon.png");
				}
			});
			this.balloonTexture.load();
			this.balloonTextureRegion.add(TextureRegionFactory.extractFromTexture(this.balloonTexture));

			// load the yellow-balloon texture
			this.balloonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/yellow_balloon.png");
				}
			});
			this.balloonTexture.load();
			this.balloonTextureRegion.add(TextureRegionFactory.extractFromTexture(this.balloonTexture));

			// load the blue-balloon texture
			this.balloonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/blue_balloon.png");
				}
			});
			this.balloonTexture.load();
			this.balloonTextureRegion.add(TextureRegionFactory.extractFromTexture(this.balloonTexture));

			// load the pink-balloon texture
			this.balloonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/pink_balloon.png");
				}
			});
			this.balloonTexture.load();
			this.balloonTextureRegion.add(TextureRegionFactory.extractFromTexture(this.balloonTexture));

			
			// load the red-balloon texture
			this.balloonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/red_balloon.png");
				}
			});
			this.balloonTexture.load();
			this.balloonTextureRegion.add(TextureRegionFactory.extractFromTexture(this.balloonTexture));
			
			// load the replay button texture
			this.replayTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/replay.png");
				}
			});
			this.replayTexture.load();
			this.replayTextureRegion = TextureRegionFactory.extractFromTexture(this.replayTexture);		
			
			
			// load the share button texture
			this.shareTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/share.png");
				}
			});
			this.shareTexture.load();
			this.shareTextureRegion = TextureRegionFactory.extractFromTexture(this.shareTexture);		
			
			
			// load the close button texture
			this.closeTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/close.png");
				}
			});
			this.closeTexture.load();
			this.closeTextureRegion = TextureRegionFactory.extractFromTexture(this.closeTexture);
			
			// load the pause button texture
			this.pauseTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/pause.png");
				}
			});
			this.pauseTexture.load();
			this.pauseTextureRegion = TextureRegionFactory.extractFromTexture(this.pauseTexture);
			
			
			// load the play button texture
			this.playTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sprites/play.png");
				}
			});
			this.playTexture.load();
			this.playTextureRegion = TextureRegionFactory.extractFromTexture(this.playTexture);
			
		} catch (IOException e1) {
			Log.e("exception: :", "Exception loading textures");
			e1.printStackTrace();
		}

		// load the bird texture
		this.seagullTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.longBitmapTextureAtlas, this,
				"seagull_big.png", 9, 1);
		

		try {
			this.mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
			this.mBitmapTextureAtlas.load();
			
			this.longBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
			this.longBitmapTextureAtlas.load();
			
			
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		// load sounds
		try {
			this.cannonSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/cannon.wav");
			cannonSound.setVolume(0.3f);
			this.splashSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/splash.wav");
			splashSound.setVolume(0.4f);
			this.goodScreamSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/birdcry.wav");
			this.badScreamSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/angrybird.wav");
			badScreamSound.setVolume(0.4f);
			this.popSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/pop2.wav");
			this.gameOverSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/game_over.ogg");
			this.gameBackground = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "sounds/background_music.ogg");
			gameBackground.setVolume(1.0f);
			this.gameBackground.setLooping(true);
			this.whoopSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "sounds/whoops.wav");
			whoopSound.setVolume(1.0f);			
						
		} catch (final IOException e) {
			Debug.e(e);
		}
		
		// load fonts
		final ITexture droidFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture plokFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.droidFont = FontFactory.createFromAsset(this.getFontManager(), droidFontTexture, this.getAssets(), "fonts/Droid.ttf", FONT_SIZE_MEDIUM, true, Color.WHITE);
		this.droidFont.load();
		
		this.mPlokFont = FontFactory.createFromAsset(this.getFontManager(), plokFontTexture, this.getAssets(), "fonts/Plok.ttf", FONT_SIZE_LARGE, true, Color.BLACK);
		this.mPlokFont.load();		
	}
	void initGameOverPopup() {
		gameOverPopup = new Scene();
		gameOverPopup.setBackground(new Background(0.09804f, 0.6274f, 0.9784f));	
		int maxTextLength = 100;
		
		final Text gameOverText = new Text(CAMERA_WIDTH * 0.4f, CAMERA_HEIGHT * 0.1f, droidFont, "GAME OVER", maxTextLength, getVertexBufferObjectManager());	
		gameOverPopup.setColor((float)Math.random(), (float)Math.random(),(float)Math.random());
		
		final ButtonSprite closeButton = new ButtonSprite(CAMERA_WIDTH * 0.25f, CAMERA_HEIGHT * 0.5f, closeTextureRegion, mEngine.getVertexBufferObjectManager(), new OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                    float pTouchAreaLocalY) {
            	gameOverSound.stop();
            	resetGame();
            	Intent closeIntent = new Intent(GameActivity.this, MainActivity.class);
				closeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(closeIntent);						
            }
        });
		ButtonSprite shareButton = new ButtonSprite(CAMERA_WIDTH * 0.45f, CAMERA_HEIGHT * 0.5f, shareTextureRegion, mEngine.getVertexBufferObjectManager(), new OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                    float pTouchAreaLocalY) {
            	  Intent shareIntent=new Intent(Intent.ACTION_SEND);
	        	  shareIntent.setType("text/plain");
	        	  shareIntent.putExtra(Intent.EXTRA_TEXT,"Beat my score in Cannon Craft : " + scoreCount + "\nLink to the game : https://play.google.com/store/apps/details?id=com.cannon.craft");
	        	  shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cannon craft");
	        	  startActivity(Intent.createChooser(shareIntent, "Share Your Score Via..."));            	
            }
        });
		ButtonSprite replayButton = new ButtonSprite(CAMERA_WIDTH * 0.65f, CAMERA_HEIGHT * 0.5f, replayTextureRegion, mEngine.getVertexBufferObjectManager(), new OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                    float pTouchAreaLocalY) {
            	gameOverSound.stop();
            	resetGame();
            	mEngine.setScene(scene);
            }
        });
		

		gameOverText.setColor(org.andengine.util.color.Color.BLACK);
		gameOverPopup.registerTouchArea(replayButton);
		gameOverPopup.registerTouchArea(shareButton);
		gameOverPopup.registerTouchArea(closeButton);
		gameOverPopup.attachChild(gameOverText);	
		gameOverPopup.attachChild(replayButton);
		gameOverPopup.attachChild(shareButton);
		gameOverPopup.attachChild(closeButton);	
	}
	
	@Override
	public Scene onCreateScene() {

		// check if sound on/off
		if(MainActivity.soundOn == false) {
			this.getEngine().getMusicManager().setMasterVolume(0);
			this.getEngine().getSoundManager().setMasterVolume(0);
		}
		else  {
			this.getEngine().getMusicManager().setMasterVolume(1);
			this.getEngine().getSoundManager().setMasterVolume(1);
		}
		
		gameBackground.play();
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.scene = new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		initGameOverPopup();
			
		
		
		this.mPauseScene = new CameraScene(camera);
		/* Make the 'PAUSED'-label centered on the camera. */
		final Text pausedText = new Text(CAMERA_WIDTH * 0.4f, CAMERA_HEIGHT * 0.4f, mPlokFont, "PAUSED", 100, getVertexBufferObjectManager());
		
		ButtonSprite playButton = new ButtonSprite(0, 0, playTextureRegion, mEngine.getVertexBufferObjectManager(), new OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                    float pTouchAreaLocalY) {
            		gameBackground.play();
    				scene.clearChildScene();
    				scene.setIgnoreUpdate(false);    				    			
            }
        });		
		this.mPauseScene.attachChild(pausedText);
		this.mPauseScene.registerTouchArea(playButton);
		this.mPauseScene.attachChild(playButton);
		/* Makes the paused Game look through. */
		this.mPauseScene.setBackgroundEnabled(false);
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		scene.setOnSceneTouchListener(this);

		final float centerX = (CAMERA_WIDTH - this.shipTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.shipTextureRegion.getHeight()) / 2;

		sky = new Sky(0, 0, this.skyTextureRegion, this.getVertexBufferObjectManager());
		scene.attachChild(sky);
		
		
		ButtonSprite pauseButton = new ButtonSprite(0, 0, pauseTextureRegion, mEngine.getVertexBufferObjectManager(), new OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                    float pTouchAreaLocalY) {   
            		gameBackground.pause();
    				scene.setChildScene(mPauseScene, false, true, true);
    				scene.setIgnoreUpdate(true);    						 	
            }
        });			
		scene.registerTouchArea(pauseButton);
		scene.attachChild(pauseButton);
		
		final Waves waveBack1 = new Waves(0, (float) (centerY * 1.8), this.wavesTextureRegion, this.getVertexBufferObjectManager());
		scene.attachChild(waveBack1);
		final Waves waveBack2 = new Waves(this.wavesTextureRegion.getWidth(), (float) (centerY * 1.8), this.wavesTextureRegion,
				this.getVertexBufferObjectManager());
		scene.attachChild(waveBack2);
		ship = new Ship(centerX/2, centerY * 2, this.shipTextureRegion, this.getVertexBufferObjectManager(), mPhysicsWorld);
		scene.attachChild(ship);
		final Waves waveFront1 = new Waves(0, (float) (centerY * 1.6), this.wavesFrontTextureRegion, this.getVertexBufferObjectManager());
		scene.attachChild(waveFront1);
		final Waves waveFront2 = new Waves(wavesFrontTextureRegion.getWidth(), (float) (centerY * 1.6), this.wavesFrontTextureRegion,
				this.getVertexBufferObjectManager());
		scene.attachChild(waveFront2);		
		scene.registerUpdateHandler(this.mPhysicsWorld);
		
		// attach the score board
		int maxTextLength = 200;
		final Text scoreText = new Text(CAMERA_WIDTH * 0.5f, CAMERA_HEIGHT * 0.1f, droidFont, "Score : ", maxTextLength, getVertexBufferObjectManager());
		scene.attachChild(scoreText);
		
		// timer to generate birds
	    final TimerHandler birdTimer = new TimerHandler(Bird.currentBirdFrequency, true, new ITimerCallback() {
	        public void onTimePassed(TimerHandler pTimerHandler) {
				bird.add(new Bird(CAMERA_WIDTH, (float) Math.random() * CAMERA_HEIGHT * 0.7f, seagullTextureRegion, getVertexBufferObjectManager(), mPhysicsWorld,
						scene));				
				Bird.newBird = true; 
	        }
	    });
	    
	    final TimerHandler skyTimer = new TimerHandler(1, true, new ITimerCallback() {
	        public void onTimePassed(TimerHandler pTimerHandler) {
				if(isSkyRed) {
					sky.setColor(org.andengine.util.color.Color.WHITE);
					isSkyRed = false;
				}
	        }
	    });
	 // timer to generate balloons
	    final TimerHandler balloonTimer = new TimerHandler(Balloon.currentBalloonFrequency, true, new ITimerCallback() {
	        public void onTimePassed(TimerHandler pTimerHandler) {				
				Random rand = new Random();				
				int maxBalloonTypes = balloonTextureRegion.size()-1;
				int minBalloonTypes = 0;				
				int randomNum = rand.nextInt((maxBalloonTypes - minBalloonTypes) + 1) + minBalloonTypes;				 
				balloon.add(new Balloon((float) (CAMERA_WIDTH +  (Math.random() * 300f)), (float)(Math.random()* CAMERA_HEIGHT), 
						balloonTextureRegion.get(randomNum), getVertexBufferObjectManager(), mPhysicsWorld, scene));		
			
	        }
	    });
	    scene.registerUpdateHandler(balloonTimer);	    
	    scene.registerUpdateHandler(birdTimer);	    
	    scene.registerUpdateHandler(skyTimer);
	    
		// reset the game if cannons less than 0
		if (totalCannonLeftCount <= 0) {
			resetGame();
		}
		scene.registerUpdateHandler(new IUpdateHandler() {			 
			public void reset() {
			}
			// main game loop
			public void onUpdate(float pSecondsElapsed) {
				
				if(totalCannonLeftCount <= -1) {
					gameOver();
					gameOverSound.play();
					gameOverSound.setVolume(1.0f);
					gameOverSound.setLooping(false);
				}
				// set text color red if cannons left less than 5
				if(totalCannonLeftCount < 5) {
					scoreText.setColor(org.andengine.util.color.Color.RED);
				}
				else {
					scoreText.setColor(org.andengine.util.color.Color.BLACK);
				}
				scoreText.setText("Score : "+scoreCount + "  Cannons Left : " + totalCannonLeftCount);
				// randomize the frequency of bird
				if (Bird.newBird) {									
					float maxFrequency = Bird.currentBirdFrequency;
					float minFrequency = 0;				
					float birdFrequency = (float) (Math.random() * (maxFrequency-minFrequency) + minFrequency);			
					birdTimer.setTimerSeconds(birdFrequency);
					Bird.newBird = false;
				}
				
				Iterator<Balloon> balloon_itr;
				Iterator<Bird> bird_itr;
				Iterator<CannonBall> ball_itr = cannonBall.iterator();
			    while (ball_itr.hasNext()) {			    	
			    	CannonBall thisBall = ball_itr.next();
			    	boolean ballLost = false;
			    	// ball goes in the water
			        if (thisBall.cannonBall.getY() > CAMERA_HEIGHT) {
			        	splashSound.play();
			        	ball_itr.remove();
			        	ballLost = true;
			        	if(totalCannonLeftCount == 0 && !ball_itr.hasNext())
			        		gameOver();
			        }
			        
			        // ball goes out of screen horizontally
			        else if (thisBall.cannonBall.getX() < 0 || thisBall.cannonBall.getX() > CAMERA_WIDTH) {			        	
			        	ball_itr.remove();
			        	ballLost = true;
			        	if(totalCannonLeftCount == 0 && !ball_itr.hasNext())
			        		gameOver();
			        }
			        
			        bird_itr = bird.iterator();
			        // cannon collides with bird
			        while(bird_itr.hasNext() && !ballLost) {
			        	Bird thisBird = bird_itr.next();			        	
			        
			        	if((thisBall.cannonBall.collidesWith(thisBird.bird))) {
			        		// if red bird, increase cannons
			        		if(thisBird.type == "red") {
			        			totalCannonLeftCount = totalCannonLeftCount + 4;
			        			badScreamSound.play();
			        		}
			        		// if white bird, decrease cannons			        		
			        		if(thisBird.type == "white") {
			        			totalCannonLeftCount = totalCannonLeftCount - 4;
			        			goodScreamSound.play();
			        		}
			        		// if black bird
			        		else if(thisBird.type == "black") {
			        			badScreamSound.play();
			        		}			
							// remove bird from array and scene
							bird_itr.remove();	
							birdsToClear.add(thisBird);		        		
						}
			        }      
			        // cannon collided with balloon
					balloon_itr = balloon.iterator();					
					while (balloon_itr.hasNext()) {
						Balloon currentBalloon = balloon_itr.next();
						if(currentBalloon.balloon.collidesWith(thisBall.cannonBall)) {
							scoreCount = scoreCount + 5;							 
							currentBalloon.balloon.detachSelf();
							balloon_itr.remove();
							popSound.play();
							if(Balloon.currentMaxBalloonSpeed < currentBalloon.maxBalloonSpeed)
								Balloon.currentMaxBalloonSpeed = Balloon.currentMaxBalloonSpeed + 5;
						}
					}
			    }
			    bird_itr = bird.iterator();
				while (bird_itr.hasNext()) {
					Bird thisBird = bird_itr.next();				
					// bird steals the cannon : functionality where red birds steal cannon passing the ship
					int cannonBirdY_Offset = 28;
					if(thisBird.bird.getX() < ship.getX() + ship.width/2 && thisBird.bird.getY() > ship.getY() && !thisBird.isCarryingCannon && thisBird.type == "red") {
						Sprite flyingCannon = new Sprite(thisBird.bird.getX(), thisBird.bird.getY()+ cannonBirdY_Offset, cannonBallTextureRegion,
								getVertexBufferObjectManager());
						
						PhysicsHandler flyingCannonPhysicsHandler = new PhysicsHandler(flyingCannon);
						flyingCannon.registerUpdateHandler(flyingCannonPhysicsHandler);
						flyingCannonPhysicsHandler.setVelocity(Bird.currentBirdSpeed * PhysicsHandler_Box2D_velocityRatio + Ship.shipSpeed, 0);
						scene.attachChild(flyingCannon);
						thisBird.isCarryingCannon = true;
						totalCannonLeftCount--;
					}
					
					// bird goes out of screen
					if (thisBird.bird.getX() + thisBird.bird.getWidth()  < 0 || thisBird.bird.getX() > CAMERA_WIDTH || thisBird.bird.getY() + thisBird.bird.getHeight() < 0
							|| thisBird.bird.getY()> CAMERA_HEIGHT) {
						bird_itr.remove();						
						scene.detachChild(thisBird.bird);						
						
						// make sky red if black bird goes out of screen
						if(thisBird.bird.getX() + thisBird.bird.getWidth()  < 0 && (thisBird.bird.getY() + thisBird.bird.getHeight() > 0
							&& thisBird.bird.getY() + thisBird.bird.getHeight() < CAMERA_HEIGHT)) {
							if(thisBird.type == "black") {
								totalCannonLeftCount = totalCannonLeftCount - 5;
								sky.setColor(org.andengine.util.color.Color.RED);
								isSkyRed = true;							
							}
						}
					}				
				}
				
				// remove balloon when goes out of screen
				balloon_itr = balloon.iterator();					
				while (balloon_itr.hasNext()) {
					Balloon currentBalloon = balloon_itr.next();
					if(currentBalloon.balloon.getX() < 0 || currentBalloon.balloon.getY() + currentBalloon.balloon.getHeight() < 0
							|| currentBalloon.balloon.getY() > CAMERA_HEIGHT) {
						balloon_itr.remove();
					}
				}
			}
		});
		return scene;
	}
	
	public void gameOver() {
		gameBackground.pause();
		this.mEngine.setScene(gameOverPopup);
		int maxTextLength = 300;
		final Text scoreText = new Text(CAMERA_WIDTH * 0.35f, CAMERA_HEIGHT * 0.3f, droidFont, "\nFinal Score : " + scoreCount, maxTextLength, getVertexBufferObjectManager());
		scoreText.setColor(org.andengine.util.color.Color.BLACK);
		gameOverPopup.attachChild(scoreText); 
		
		if(scoreCount > MainActivity.highScore) {
			whoopSound.play();
			final Text recordBrokenText = new TickerText(CAMERA_WIDTH*0.15f, CAMERA_HEIGHT*0.8f, droidFont, "\nYou Broke Record !!!  previous record was: " + MainActivity.highScore, new TickerTextOptions(HorizontalAlign.CENTER, 30), this.getVertexBufferObjectManager());
			recordBrokenText.setColor(org.andengine.util.color.Color.BLACK);
			recordBrokenText.registerEntityModifier(
				new SequenceEntityModifier(
					new ParallelEntityModifier(
						new AlphaModifier(2, 0.0f, 1.0f),
						new ScaleModifier(2, 0.5f, 1.0f)
					),
					new RotationModifier(0.2f, -10, 10),
					new RotationModifier(0.2f, 10, -10),
					new RotationModifier(0.2f, -10, 10),
					new RotationModifier(0.2f, 10, -10),
					new RotationModifier(0.2f, -10, 10),
					new RotationModifier(0.2f, 10, -10),
					new RotationModifier(0.2f, -10, 0)
					
				)
			);
			recordBrokenText.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			MainActivity.highScore = scoreCount;	
			gameOverPopup.attachChild(recordBrokenText);			
		
			//setting highScore record
			SharedPreferences prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
			Editor editor = prefs.edit();
			editor.putInt("highScore", scoreCount);
			editor.commit();
		}
	}
	
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		
		if (pSceneTouchEvent.isActionDown()) {
			screen_touchX = pSceneTouchEvent.getX();
			screen_touchY = pSceneTouchEvent.getY()-CAMERA_HEIGHT;
			float cannon_x = ship.x + ship.width*0.9f;
			float cannon_y = ship.y + ship.height/2;
			
			if(totalCannonLeftCount > 0) {
				cannonBall.add(new CannonBall(cannon_x, cannon_y, this.cannonBallTextureRegion,
						this.getVertexBufferObjectManager(), mPhysicsWorld, scene));				
				cannonSound.play();
			}
			return true;
		}
		return false;
	}

	private static class Ship extends Sprite {
		private final PhysicsHandler mPhysicsHandler;		
		private static float shipSpeed = INITIAL_SHIP_SPEED;
		private static float maxShipSpeed = MAX_SHIP_SPEED;
		private static float shipSpeedGain = SHIP_SPEED_GAIN;
		private double angle = 0;
		private double rockSpeed = 0.1;
		private int rockHeight = 2;
		private float x, y;
		private float width, height;	
		public Ship(final float pX, final float pY, final ITextureRegion shipTextureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld) {
			super(pX, pY, shipTextureRegion, pVertexBufferObjectManager);
			this.x = pX;
			this.y = pY;
			this.width = shipTextureRegion.getWidth();
			this.height = shipTextureRegion.getHeight();			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);			
						
			/* Ship as a physics body : on hold for now
			 * final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0f, 0);
			final Body body;
			body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, objectFixtureDef);
			body.setGravityScale(0);
			body.setType(BodyType.StaticBody);
			physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
			this.setUserData(body);*/		
			//if(this.collidesWith(en))
			
		}	
		public float getX() {
			return x;
		}
		public float getY() {
			return y;
		}
		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			// rocking motion of the ship
			this.setRotation(((float) Math.sin(angle)) * rockHeight);
			angle = angle + rockSpeed;
			if(shipSpeed > Ship.maxShipSpeed)
				shipSpeed = shipSpeed - shipSpeedGain;
			super.onManagedUpdate(pSecondsElapsed);	
			
		}
	}

	private static class Waves extends Sprite {
		private final PhysicsHandler mPhysicsHandler;
		private double angle = 0;
		private double rockSpeed = 0.08;
		private int rockHeight = 1;
		private int overlap = 150;
		private static float speed = Ship.shipSpeed;
		public Waves(final float pX, final float pY, final ITextureRegion shipTextureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager) {
			super(pX, pY, shipTextureRegion, pVertexBufferObjectManager);
			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			this.mPhysicsHandler.setVelocity(speed, 0);		
		}
		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			// rocking motion of waves
			this.setRotation(((float) Math.sin(angle)) * rockHeight);
			angle = angle + rockSpeed;
			if(speed > Ship.maxShipSpeed)
				speed = speed - Ship.shipSpeedGain;
			this.mPhysicsHandler.setVelocity(speed, 0);
			if (this.mX + this.mWidth < 0) {
				this.mX = this.mWidth - overlap;
			}
			super.onManagedUpdate(pSecondsElapsed);
		}
	}

	private static class Sky extends Sprite {
		public Sky(final float pX, final float pY, final ITextureRegion shipTextureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager) {
			super(pX, pY, shipTextureRegion, pVertexBufferObjectManager);
		}
		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			super.onManagedUpdate(pSecondsElapsed);
		}
	}
	
	static String birdTypeSelector(float redBirdProb, float whiteBirdProb, float blackBirdProb) {		
		class Bird {
			float probability;
			String color;
			
			Bird(float prob, String col) {
				probability = prob;
				color = col;
			}
		};
		List<Bird> birds = new ArrayList<Bird>();		
		birds.add(new Bird(redBirdProb, "red"));
		birds.add(new Bird(whiteBirdProb, "white"));
		birds.add(new Bird(blackBirdProb, "black"));			
		double p = Math.random();
		double cumulativeProbability = 0.0;
		for (Bird bird : birds) {
		    cumulativeProbability += bird.probability;
		    if (p <= cumulativeProbability) {
		    	birds.clear();
		        return bird.color;
		    }
		}
		birds.clear();
		return "white";
	}
	private static class Bird{
		private boolean isCarryingCannon = false;
		private static float currentMaxBirdSpeed = INITIAL_MAX_BIRD_SPEED;
		private static float currentBirdSpeed = INITIAL_BIRD_SPEED;
		private static float currentBirdFrequency = INITIAL_BIRD_FREQUENCY;
		private static float redBirdProb = 0.5f, whiteBirdProb = 0.4f, blackBirdProb = 0.1f;
		
		private static boolean newBird = false;
		private float maxBirdSpeed = -100;	
		private float maxBirdFrequency = 3;
		private float birdFrequencyDrop = 0.2f;	
		private float redToWhiteBirdSpeedRatio = 0.8f;
		
		float finalRedBirdProb = 0.2f;		
		float finalBlackBirdProb = 0.4f;
		
		private AnimatedSprite bird;
		final Body body;
		String type;
		public Bird(final float pX, final float pY, final TiledTextureRegion textureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, Scene scene) {
			
			float maxBirdSpeed = Bird.currentMaxBirdSpeed;
			float minBirdSpeed = -2;
			Bird.currentBirdSpeed = (float) (Math.random() * (maxBirdSpeed-minBirdSpeed) + minBirdSpeed);
			if(Bird.currentMaxBirdSpeed < this.maxBirdSpeed)
				Bird.currentMaxBirdSpeed = Bird.currentMaxBirdSpeed - 1;
			
			if(Bird.currentBirdFrequency > maxBirdFrequency)
				Bird.currentBirdFrequency = currentBirdFrequency - birdFrequencyDrop;
			
			bird = new AnimatedSprite (pX, pY, textureRegion, pVertexBufferObjectManager);
			bird.animate(100);
			
			// select bird type based on bird probabilities
			type = birdTypeSelector(redBirdProb, whiteBirdProb, blackBirdProb);
			
			// increase game difficulty
			if(redBirdProb > finalRedBirdProb) {
				redBirdProb = redBirdProb - 0.02f;
			}
			if(blackBirdProb < finalBlackBirdProb) {
				blackBirdProb = blackBirdProb + 0.02f;
			}
			
			if(type == "red") {
				bird.setColor(org.andengine.util.color.Color.RED);
			}
			if(type == "black") {
				bird.setColor(org.andengine.util.color.Color.CYAN);
			}
			
			final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(0.01f, 0.5f, 0.5f);
			objectFixtureDef.filter.groupIndex = GROUP_SEAGULL;
			
			body = PhysicsFactory.createBoxBody(physicsWorld, pX+textureRegion.getWidth()/2, pY + textureRegion.getHeight()/2, textureRegion.getWidth(), textureRegion.getHeight(), BodyType.DynamicBody, objectFixtureDef);
			if(type=="red")
				body.setLinearVelocity(currentBirdSpeed + Ship.shipSpeed/PhysicsHandler_Box2D_velocityRatio, 0);
			else if(type=="white")
				body.setLinearVelocity((currentBirdSpeed + Ship.shipSpeed) *redToWhiteBirdSpeedRatio/PhysicsHandler_Box2D_velocityRatio, 0);
			else if(type=="black")
				body.setLinearVelocity((float) (((currentBirdSpeed + Ship.shipSpeed) *redToWhiteBirdSpeedRatio/PhysicsHandler_Box2D_velocityRatio) - Math.random()*5f), 0);
			
			body.setGravityScale(0);	
			
		
			
			physicsWorld.registerPhysicsConnector(new PhysicsConnector(bird, body, true, true));
			
			bird.setUserData(body);
			scene.attachChild(bird);			
		}		
	}
	
	private static class CannonBall{
		Sprite cannonBall;
		private float cannonBall_init_x = 0;
		private float cannonBall_init_y = 0;	
		final Body body;
		private CannonBall(final float pX, final float pY, final ITextureRegion textureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, Scene scene) {			
			final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10, 0.5f, 0.5f);
			objectFixtureDef.filter.groupIndex = GROUP_CANNON;
			
			cannonBall = new Sprite(pX, pY, textureRegion, pVertexBufferObjectManager);		
			body = PhysicsFactory.createCircleBody(physicsWorld, cannonBall, BodyType.DynamicBody, objectFixtureDef);
			physicsWorld.registerPhysicsConnector(new PhysicsConnector(cannonBall, body, true, true));			
			cannonBall.setUserData(body);
			scene.attachChild(cannonBall);
			cannonBall_init_x = pX;
			cannonBall_init_y = pY;
			this.shootCannonBall();
			
		}	
		private void shootCannonBall() {
			final Body cannonBallBody = (Body) cannonBall.getUserData();
			cannonBallBody.setLinearVelocity(new Vector2((screen_touchX - cannonBall_init_x) / 20, (screen_touchY) / 20));
			totalCannonLeftCount--;
		}
	};
	
	private static class Balloon{
		private static float currentMaxBalloonSpeed = INITIAL_MAX_BALLOON_SPEED;
		private static float currentBalloonFrequency = INITIAL_BALOON_FREQUENCY;
		
		private Sprite balloon;
		private float speedX;
		private float speedY;
		private float maxBalloonSpeed;		
		private float  maxScale;
		private float minScale;
		private float x_y_speedRatio;
		
		PhysicsHandler physicsHandler;
		private Balloon(final float pX, final float pY, final ITextureRegion textureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, Scene scene) {		
			balloon = new Sprite(pX, pY, textureRegion, pVertexBufferObjectManager);	
			
			maxBalloonSpeed = 1000;
			maxScale = 0.7f;
			minScale = 0.3f;
			x_y_speedRatio = 0.6f;
			
			float scale = (float) (Math.random() * (maxScale-minScale) + minScale);			
			float maxSpeedY = currentMaxBalloonSpeed * x_y_speedRatio;
			float minSpeedY = -currentMaxBalloonSpeed * x_y_speedRatio;			
			speedY = (float) (Math.random() * (maxSpeedY-minSpeedY) + minSpeedY);	
			
			float maxSpeedX = -currentMaxBalloonSpeed;
			float minSpeedX = currentMaxBalloonSpeed/2;			
			speedX = (float) (Math.random() * (maxSpeedX-minSpeedX) + minSpeedX);
			
			balloon.setScale(scale);
			this.physicsHandler = new PhysicsHandler(balloon);
			balloon.registerUpdateHandler(physicsHandler);
			physicsHandler.setVelocityX(speedX);
			physicsHandler.setVelocityY(speedY);			
			scene.attachChild(balloon);			
		}	
	};

	@Override
	public final void onPause() {
		super.onPause();
		this.getEngine().getMusicManager().setMasterVolume(0);
		this.getEngine().getSoundManager().onPause();
		gameBackground.pause();
		scene.setChildScene(mPauseScene, false, true, true);
		scene.setIgnoreUpdate(true);		
		this.mEngine.stop();
	}

	@Override
	public final void onResume() {
		super.onResume();	
		this.mEngine.start();
		this.getEngine().getMusicManager().setMasterVolume(1);
		this.getEngine().getSoundManager().onResume();		
	}	
}


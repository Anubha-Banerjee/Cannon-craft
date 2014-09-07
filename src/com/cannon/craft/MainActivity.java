package com.cannon.craft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cannon.craft.R;

public class MainActivity extends Activity {
	public int screen_width, screen_height;
	public static boolean soundOn = true;
	public static int highScore = 0;
	public TextView record;
	
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// don't want the status bar on top
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// don't dim the screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//getting high score
		SharedPreferences prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
		highScore = prefs.getInt("highScore", 0); //0 is the default value
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screen_width = metrics.widthPixels;
		screen_height = metrics.heightPixels;		
		
		record = (TextView)findViewById(R.id.record);
		record.setText("HI-SCORE\n" + "     " + highScore);
		record.setRotation(-30f);
		final ImageView soundImage = (ImageView)findViewById(R.id.sound);	
		soundImage.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				
				int action = event.getAction();
				if (action == MotionEvent.ACTION_UP && soundOn) {
					soundImage.setImageResource(R.drawable.nosound);
					soundOn = false;					
				}
				else if (action == MotionEvent.ACTION_UP && !soundOn) {
					soundImage.setImageResource(R.drawable.soundon);
					soundOn = true;
				}
				return true;
			}
		});		
	}
	

    public void startGame(View view) {
    	this.startActivity(new Intent(this, GameActivity.class));
    }
    public void showHelp(View view) {
    	this.startActivity(new Intent(this, HelpActivity.class));
    }
    public void showCredits(View view) {
    	this.startActivity(new Intent(this, CreditsActivity.class));
    }
    public void endGame(View view) {
    	  finish();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public final void onPause() {
		super.onPause();		
	}

	@Override
	public final void onResume() {
		super.onResume();		
		record.setText("HI-SCORE\n" + "     " + highScore);
	}
	

}

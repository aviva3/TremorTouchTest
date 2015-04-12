package idoelad.finalproject.tremortest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class TestActivity extends Activity implements OnTouchListener {

	public String LOG_TAG = "Test Activity";
	private ShapeCountDownTimer timeCounter;


	private TextView timeTextView;
	private TextView shapeNumberTextView;
	private RelativeLayout testZoneLayout;
	private ImageView touchPoint;


	private OutputFileWriter outputFileWriter;
	private String userOutputDirPath;
	private String userOutputFilePath;

	private int timePerShape;
	private boolean writeResults;
	public static long currShapeStartTime;
	private int numberOfShapesPassed;
	private int currentShapeId;
	private int touchId=0;
	private int minPointerId=1;

	private boolean inTest;

	private Iterator<CircleView> circleViewIterator;
	private CircleViewFactory circleViewFactory;
	private String testType;
	private String circlesFilePath;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		Log.i(LOG_TAG, "Test initialized");

		//Get UI components
		shapeNumberTextView = (TextView) findViewById(R.id.shapeNumberTextView);
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		testZoneLayout = (RelativeLayout) findViewById(R.id.testZoneLayout);
		touchPoint = (ImageView) findViewById(R.id.touchPoint);

		//Get information from the intent (test chooser)
		userOutputDirPath = getIntent().getStringExtra("userOutputDirPath");
		testType = getIntent().getStringExtra("testType");
		writeResults = getIntent().getBooleanExtra("writeResults", writeResults);
		timePerShape = getIntent().getIntExtra("timePerShape",10);
		Log.i(LOG_TAG, "Time per shape was set to : "+timePerShape);

		//Create the output file
		int testNumber = 1;
		String userOutputDirName = new File(userOutputDirPath).getName();
		String outputFileName = userOutputDirName+"_results_"+testType+"_"+testNumber+".csv";
		File userOutputFile = new File(userOutputDirPath,outputFileName);
		while (userOutputFile.exists()){
			testNumber++;
			outputFileName = userOutputDirName+"_results_"+testType+"_"+testNumber+".csv";
			userOutputFile = new File(userOutputDirPath,outputFileName);
		}
		try {
			userOutputFile.createNewFile();
		} catch (IOException e) {
			Log.e(LOG_TAG,"Error while creating test output file: '"+userOutputFile.getAbsolutePath()+"'",e);
			throw new RuntimeException("Error while creating test output file: '"+userOutputFile.getAbsolutePath()+"'",e);	
		}
		userOutputFilePath = userOutputFile.getAbsolutePath();
		try {
			outputFileWriter = new OutputFileWriter(userOutputFilePath,timePerShape);
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, "Error while creating outputFileWriter",e);
			throw new RuntimeException("Error while creating outputFileWriter",e);
		}

		//Create all circles
		circlesFilePath = getIntent().getStringExtra("circlesFilePath");
		circleViewFactory = new CircleViewFactory(this,circlesFilePath);
		circleViewIterator = circleViewFactory.iterator();



		//Start test
		updateShapeCounter(0);
		numberOfShapesPassed=0;
		startTest();
	}


	private void startTest() {
		Log.i(LOG_TAG,"Test started");
		inTest = true;
		testZoneLayout.setOnTouchListener(this);
		if (circleViewFactory.getNumberOfCircles() >0){
			nextShape();
		}
		else{
			testEnded(true);
		}
	}


	private void nextShape() {
		if (testZoneLayout.getChildCount() >0){
			testZoneLayout.removeAllViews();
		}
		numberOfShapesPassed++;
		touchId=0;
		startShapeTime();
		updateShapeCounter(numberOfShapesPassed);
		CircleView currentCircle = circleViewIterator.next();
		currentShapeId = currentCircle.getShapeId();
		testZoneLayout.addView(currentCircle);

	}


	public long getLastShapeStartTime() {
		return currShapeStartTime;
	}


	private void shapeTimeEnded(){
		Log.d(LOG_TAG, "Shape Time Ended");
		if (numberOfShapesPassed < circleViewFactory.getNumberOfCircles() && inTest){
			nextShape();
		}
		else{
			testEnded(true);	
		}
	}


	private void testEnded(boolean timeEnded){
		Log.i(LOG_TAG,"Test Ended");
		inTest = false;
		this.outputFileWriter.closeFile(timeEnded);
		if (!writeResults){
			this.outputFileWriter.deleteFile();
			File circlesFile = new File(circlesFilePath);
			circlesFile.delete();
		}
		setResult(Activity.RESULT_OK);
		finish();
	}


	@Override
	public void onBackPressed() {
		testEnded(false);
	}

	private void startShapeTime(){
		timeCounter = new ShapeCountDownTimer((timePerShape)*1000, 1000);
		timeCounter.start();
		currShapeStartTime = System.currentTimeMillis();
	}

	private void updateShapeCounter(int num){
		shapeNumberTextView.setText(String.valueOf(num));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (inTest){
			// get masked (not specific to a pointer) action
			int maskedAction = event.getActionMasked();

			switch (maskedAction) {
			case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN: case MotionEvent.ACTION_MOVE:

				if ((event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) && event.getPointerCount()==1){
					touchId++;
				}
				touchPoint.setImageResource(R.drawable.touch_hand);

				break;

			case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP:
				touchPoint.setImageResource(R.drawable.no_touch_hand);
			default:
				break;
			}
			if (touchId != 0){ //If touchId==0 the touch is a continues touch from previous shape
				outputFileWriter.writeTouchToFile(currentShapeId,touchId,event);
			}
		}
		return true;
	}


	public class ShapeCountDownTimer {
		private long millisInFuture;
		private long countDownInterval;
		private boolean status;
		public ShapeCountDownTimer(long pMillisInFuture, long pCountDownInterval) {
			this.millisInFuture = pMillisInFuture;
			this.countDownInterval = pCountDownInterval;
			status = false;
			Initialize();
		}

		public void Stop() {
			status = false;
		}

		public long getCurrentTime() {
			return millisInFuture;
		}

		public void start() {
			status = true;
		}
		public void Initialize() 
		{
			timeTextView.setText(Long.toString(millisInFuture/1000));
			millisInFuture -= countDownInterval;
			final Handler handler = new Handler();
			final Runnable counter = new Runnable(){
				public void run(){
					long sec = millisInFuture/1000;
					if(status) {
						if(millisInFuture <= 0) {
							shapeTimeEnded();	
						} else {
							timeTextView.setText(Long.toString(sec));
							millisInFuture -= countDownInterval;
							handler.postDelayed(this, countDownInterval);
						}
					} else {
						handler.postDelayed(this, countDownInterval);
					}
				}
			};
			handler.postDelayed(counter, countDownInterval);
		}
	}
}

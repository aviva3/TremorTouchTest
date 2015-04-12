package idoelad.finalproject.tremortest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class ChooseTestActivity extends Activity {
	public String LOG_TAG = "ChooseTest activity";

	private EditText timePerShapeForm;
	private ImageButton fixedButton;
	private ImageButton diffSizeButton;
	private ImageButton edgesButton;

	private ImageButton endTestButton;
	private ImageButton endTestAndSendResultsButton;

	private ImageButton metronomButton; 

	private ImageButton longPressButton;
	private ImageButton strongPressButton;
	private ImageButton swipeAndStrongButton;
	private ImageButton swipeAndLongButton;
	private ImageButton swipeButton;
	private ImageButton liftButton;
	
	private CheckBox writeResultsBox;
	

	private String userOutputDirPath;
	private int timePerShape;

	private String testerName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_test);

		init();

		fixedButton.setOnClickListener(new TestChooserOnClickListener("fixed"));
		diffSizeButton.setOnClickListener(new TestChooserOnClickListener("diffSizes"));
		edgesButton.setOnClickListener(new TestChooserOnClickListener("edges"));

		longPressButton.setOnClickListener(new TestChooserOnClickListener("long"));
		strongPressButton.setOnClickListener(new TestChooserOnClickListener("strong"));
		liftButton.setOnClickListener(new TestChooserOnClickListener("lift"));

		swipeAndLongButton.setOnClickListener(new TestChooserOnClickListener("swipe_long"));
		swipeAndStrongButton.setOnClickListener(new TestChooserOnClickListener("swipe_strong"));
		swipeButton.setOnClickListener(new TestChooserOnClickListener("swipe"));
		

		final MediaPlayer tickPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tick);

		endTestButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tickPlayer.isPlaying()){
					tickPlayer.pause();
				}
				endTestWithoutSending();
			}
		});

		endTestAndSendResultsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tickPlayer.isPlaying()){
					tickPlayer.pause();
				}
				sendMail();
				endTest();
			}
		});

		metronomButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tickPlayer.isPlaying()){
					tickPlayer.pause();
				}
				else{
					tickPlayer.start();
					tickPlayer.setLooping(true);
				}
			}
		});

	}


	class TestChooserOnClickListener implements OnClickListener{
		private File circlesFile;
		private String testType;
		private boolean writeResults;


		public TestChooserOnClickListener(String testType){
			super();
			this.testType = testType;
			this.circlesFile = new File(Consts.externalStorageDir+"/"+Consts.mainAppDirName+"/"+Consts.circlesInputDir,"circles_"+testType+".csv");
		}

		@Override
		public void onClick(View v) {
			if (timePerShapeForm.getText().length()==0){
				Toast.makeText(getApplicationContext(), "Time per shape is empty!", Toast.LENGTH_SHORT).show();
				return;
			}
			timePerShape = Integer.parseInt(timePerShapeForm.getText().toString());


			//Copy circles file and info file to the user's output folder
			String circlesFilePathInUserDir = userOutputDirPath+"/"+circlesFile.getName();
			try {
				copyFile(circlesFile.getAbsolutePath(), circlesFilePathInUserDir);
			} catch (IOException e) {
				Log.e(LOG_TAG,"Error while copying files to user's output dir",e);
				throw new RuntimeException("Error while copying files to user's output dir",e);
			}
			
			v.setSelected(true);
			//Start test
			writeResults = writeResultsBox.isChecked();
			if (!writeResults){
				Toast.makeText(getApplicationContext(), "Results will not be written to the file!!", Toast.LENGTH_SHORT).show();
			}
			startTest(circlesFilePathInUserDir,testType,writeResults);
		}

	}



	private void init() {
		userOutputDirPath = getIntent().getStringExtra("userOutputDirPath");
		testerName = getIntent().getStringExtra("testerName");
		timePerShapeForm = (EditText) findViewById(R.id.timePerShape);
		fixedButton = (ImageButton) findViewById(R.id.fixedSizeButton);
		diffSizeButton = (ImageButton) findViewById(R.id.diffSizeButton);
		edgesButton = (ImageButton) findViewById(R.id.edgesButton);
		longPressButton = (ImageButton) findViewById(R.id.pressLongButton);
		strongPressButton = (ImageButton) findViewById(R.id.pressStrongButton);
		swipeAndStrongButton = (ImageButton) findViewById(R.id.swipeStrongButton);
		swipeAndLongButton = (ImageButton) findViewById(R.id.swipeLongButton);
		swipeButton = (ImageButton) findViewById(R.id.swipeButton);
		liftButton = (ImageButton) findViewById(R.id.liftButton);
		endTestButton = (ImageButton) findViewById(R.id.endtestButton);
		endTestAndSendResultsButton = (ImageButton) findViewById(R.id.endTestAndSendButton);
		metronomButton = (ImageButton) findViewById(R.id.metronomButton);
		writeResultsBox = (CheckBox) findViewById(R.id.writeResultBox);
		
		writeResultsBox.setChecked(true);
	}


	private static void copyFile(String sourcePath, String destPath) throws IOException {
		File sourceFile = new File(sourcePath);
		File destFile = new File(destPath);

		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}


	protected void startTest(String circlesFilePathInUserDir, String testType, boolean writeResults){
		//Create the Test Activity
		Intent startTestActivityIntent = new Intent(ChooseTestActivity.this, TestActivity.class);
		startTestActivityIntent.putExtra("userOutputDirPath", userOutputDirPath);
		startTestActivityIntent.putExtra("testType", testType);
		startTestActivityIntent.putExtra("circlesFilePath", circlesFilePathInUserDir);
		startTestActivityIntent.putExtra("timePerShape", timePerShape);
		startTestActivityIntent.putExtra("writeResults", writeResults);
		startActivityForResult(startTestActivityIntent,0);
	}

	private void sendMail(){
		Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"idoelad@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "New TremorTouchTest results for '"+testerName+"'");
		i.putExtra(Intent.EXTRA_TEXT   , "Files are attached");

		File userOutputDir = new File(userOutputDirPath);
		ArrayList<Uri> uris = new ArrayList<Uri>();
		File[] allFiles = userOutputDir.listFiles();
		for (File attachment : allFiles){
			if (!attachment.exists() || !attachment.canRead()) {
				Toast.makeText(this, "Attachment Error: "+attachment.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			}
			Uri fileUri = Uri.fromFile(attachment);
			uris.add(fileUri);
		}
		i.putExtra(Intent.EXTRA_STREAM, uris);

		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
			Log.i(LOG_TAG, "Sending Mails");
			return;
		} catch (android.content.ActivityNotFoundException ex) {
			Log.e(LOG_TAG,"Error while sending mails",ex);
			Toast.makeText(this, "Error while sending mails", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	private void endTest(){
		setResult(Activity.RESULT_OK);
		finish();
	}

	protected void endTestWithoutSending() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Warning!");

		// set dialog message
		alertDialogBuilder
		.setMessage("End test without sending results?")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				endTest();
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				sendMail();
				endTest();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}




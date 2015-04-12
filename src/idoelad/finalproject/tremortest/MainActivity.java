package idoelad.finalproject.tremortest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;


public class MainActivity extends Activity {
	public String LOG_TAG = "Main activity";

	private EditText testerNameForm;
	private EditText testerAgeForm;
	private Spinner testerGenderForm;
	private EditText testerTremorEstimationForm;
	private EditText testerTremorTimeForm;
	private EditText testerMedicalForm;

	private EditText testerNotesForm;
	private ImageButton startTestButtonForm;

	public static String appDirPath;
	private File userOutputDir;
	private String testerName;

	private Spinner testerHandForm; 



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(LOG_TAG, "Application initialized");

		setFormVariables();

		//Create app dir
		createAppDir();

		startTestButtonForm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {	

				testerName = testerNameForm.getText().toString();

				//Create user output dir		
				userOutputDir = createNewOutputDir(testerName);;

				//Create user info file
				createUserInfoFile();
				
				//Start test
				startTestChooser();
			}

		});
	}

	private void setFormVariables() {
		testerNameForm = (EditText) findViewById(R.id.testerName);
		testerAgeForm = (EditText) findViewById(R.id.testerAge);
		testerGenderForm = (Spinner) findViewById(R.id.testerGender);
		testerHandForm = (Spinner) findViewById(R.id.testerHand);

		testerTremorTimeForm = (EditText) findViewById(R.id.testerTremorTime);
		testerTremorEstimationForm = (EditText) findViewById(R.id.testerTremorEstimation);
		testerMedicalForm = (EditText) findViewById(R.id.testerMedical);
		testerNotesForm = (EditText) findViewById(R.id.testerNotes);
		startTestButtonForm = (ImageButton) findViewById(R.id.startTestButton);
	}


	private void createAppDir(){
		File newFolder = new File(Consts.externalStorageDir, Consts.mainAppDirName);
		if (!newFolder.exists()) {
			newFolder.mkdir();
			Log.d(LOG_TAG, "App dir created: "+newFolder.getAbsolutePath());
		}
		else{
			Log.d(LOG_TAG, "App dir '"+newFolder.getAbsolutePath()+"' already exist.");
		}

		appDirPath = newFolder.getAbsolutePath();
	}


	private File createNewOutputDir(String testerName) {
		try {
			File mainOutputsFolder = new File(appDirPath, Consts.mainOutputDirName);
			if (!mainOutputsFolder.exists()) {
				mainOutputsFolder.mkdir();
				Log.d(LOG_TAG, "Output dir created: "+mainOutputsFolder.getAbsolutePath());
			}
			else{
				Log.d(LOG_TAG, "Output dir '"+mainOutputsFolder.getAbsolutePath()+"' already exist. File will be created in that dir");
			}

			Date date = new Date() ;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			File userDir = new File(mainOutputsFolder,dateFormat.format(date) + "_"+testerName) ;				        
			userDir.mkdir();
			Log.i(LOG_TAG, "Output dir created: "+userDir.getAbsolutePath());
			return userDir;				    
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error occured while creating output dir",e);
			throw new RuntimeException("Error occured while creating output dir",e);
		}
	}

	private String createNewCSVFileInUserDir(String filePrefix){
		String fileName = filePrefix+".csv";
		File outputFile = new File(userOutputDir,fileName);
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error occured while creating output dir",e);
			throw new RuntimeException("Error occured while creating output dir",e);
		}
		return outputFile.getAbsolutePath();
	}


	private void createUserInfoFile() {
		LinkedHashMap<String, String> userInfos = new LinkedHashMap<String, String>();
		userInfos.put("Name", testerName);
		userInfos.put("Age", testerAgeForm.getText().toString());
		userInfos.put("Gender", testerGenderForm.getSelectedItem().toString());
		userInfos.put("Hand", testerHandForm.getSelectedItem().toString());
		userInfos.put("Years of Tremor", testerTremorTimeForm.getText().toString());
		userInfos.put("Tremor Estimation", testerTremorEstimationForm.getText().toString());
		userInfos.put("Medical treatment", testerMedicalForm.getText().toString());
		userInfos.put("Notes", testerNotesForm.getText().toString());

		String userInfoFilePath = createNewCSVFileInUserDir(testerName+"_UserInfo");
		try {
			FileWriter writer = new FileWriter(userInfoFilePath); 
			for(String headline : userInfos.keySet()) {
				writer.write(headline + ": "+userInfos.get(headline)+"\n");
			}
			writer.close();

		} catch (IOException e) {
			Log.e(LOG_TAG, "Error while parsing user Info",e);
			throw new RuntimeException("Error while parsing user Info",e);
		}
	}

	private void startTestChooser(){
		//Create the Choose Test Activity
		Intent startTestActivityIntent = new Intent(MainActivity.this, ChooseTestActivity.class);
		startTestActivityIntent.putExtra("userOutputDirPath", userOutputDir.getAbsolutePath());
		startTestActivityIntent.putExtra("testerName", testerName);
		startActivityForResult(startTestActivityIntent,0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(LOG_TAG,"New Test");
		testerNameForm.setText(null);
		testerAgeForm.setText(null);
		testerTremorEstimationForm.setText(null);
		testerNotesForm.setText(null);

	}

}

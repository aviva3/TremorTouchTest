package idoelad.finalproject.tremortest;

import android.os.Environment;

public class Consts {
	public static String externalStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String mainAppDirName = "TremorTouchTest";
	public static String mainOutputDirName = "testOutputs";
	public static String circlesInputDir = "circles";

	public static String[] outputFileHeaders = {"ShapaId","TouchID","NumOfPointers","PointerID","Action","TimeSinceFirstTouch[ms]","X[px]","Y[px]","Pressure[0-1]","Size[0-1]",};
	public static long waitBetweenShapesMs = 2000; 


}

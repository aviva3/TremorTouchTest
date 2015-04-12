package idoelad.finalproject.tremortest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;

public class CircleViewFactory implements Iterable<CircleView> {
	public String LOG_TAG = "Circles Factory";

	private ArrayList<CircleView> cvArrayList;

	public CircleViewFactory(Context context, String circlesFilePath) {
		cvArrayList = new ArrayList<CircleView>();
		
		File circlesInputFile = new File(circlesFilePath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(circlesInputFile));
			String line;
			while ((line = br.readLine()) != null) {
				String[] strings = line.split(",");
				cvArrayList.add(new CircleView(context,Integer.valueOf(strings[0]), Float.valueOf(strings[1]), Float.valueOf(strings[2]), Float.valueOf(strings[3])));
			}
			
		} catch (IOException e) {
				Log.e(LOG_TAG,"Error while reading from circles file",e);
				throw new RuntimeException("Error while reading from circles file",e);
		}
		
		try {
			br.close();
		} catch (IOException e) {
			Log.e(LOG_TAG,"Error while closing circles file",e);

		}
	}


	@Override
	public Iterator<CircleView> iterator() {
		return cvArrayList.iterator();
	}
	
	public int getNumberOfCircles(){
		return cvArrayList.size();
	}

}

package idoelad.finalproject.tremortest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

public class OutputFileWriter {
	public String LOG_TAG = "Output File Writer";

	private File outputFile;
	private PrintWriter printWriter;
	private int timePerShpae;
	private int currFingerId;
	private int currTouchId;
	private int currShapeId;
	
	private SparseArray<TouchInfo> touchInfos;



	public OutputFileWriter(String outputFilePath,int timePerShape) throws FileNotFoundException {
		this.outputFile = new File(outputFilePath);
		Log.d(LOG_TAG, "Output file path was set to: "+this.outputFile.getAbsolutePath());
		this.printWriter = new PrintWriter(outputFile);
		Log.d(LOG_TAG, "Output file is now open for writing");
		this.timePerShpae = timePerShape;
		writeHeaders();
		touchInfos = new SparseArray<TouchInfo>();
		currFingerId = 1;
		currTouchId = -1;
		currShapeId = -1;

	}

	public void writeTouchToFile(int shapeId, int touchId, MotionEvent event){

		long timeSinceFirstTouch = event.getEventTime() - event.getDownTime();

		int count = event.getPointerCount();
		// get pointer index from the event object
		int pointerIndex = event.getActionIndex();
		// get pointer ID
		int pointerId = event.getPointerId(pointerIndex);

		// get masked (not specific to a pointer) action
		int maskedAction = event.getActionMasked();

		switch (maskedAction) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {

			TouchInfo ti = new TouchInfo();
			ti.x = event.getX(pointerIndex);
			ti.y = event.getY(pointerIndex);
			ti.action = "DOWN";
			ti.pressure = event.getPressure(pointerIndex);
			ti.size = event.getSize(pointerIndex);

			if (currTouchId != touchId || currShapeId != shapeId){
				currFingerId = 1;
				currTouchId = touchId;
				currShapeId = shapeId;
			}
			else{
				currFingerId++;
			}
			ti.fingerId = currFingerId;

			touchInfos.put(pointerId, ti);

			break;
		}

		case MotionEvent.ACTION_MOVE: { // a pointer was moved
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				TouchInfo ti = touchInfos.get(event.getPointerId(i));
				if (ti != null) {
					ti.x = event.getX(i);
					ti.y = event.getY(i);
					ti.action = "MOVE";
					ti.pressure = event.getPressure(i);
					ti.size = event.getSize(i);
				}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
			TouchInfo ti = touchInfos.get(pointerId);
			writeEvent(shapeId, touchId, count, ti.fingerId, "UP", timeSinceFirstTouch, ti.x, ti.y, ti.pressure, ti.size);
			touchInfos.put(pointerId,null);
			break;
		}
		}

		for (int i = 0; i < touchInfos.size(); i++) {
			TouchInfo ti = touchInfos.valueAt(i);
			if (ti != null){
				writeEvent(shapeId,touchId,count,ti.fingerId,ti.action,timeSinceFirstTouch,ti.x,ti.y,ti.pressure,ti.size);
			}

		}

	}

	private void writeEvent(int shapeId, int touchId, int count, int fingerId,
			String action, long timeSinceFirstTouch, float x, float y, float pressure, float size) {
		this.printWriter.println(shapeId+","+touchId+","+count+","+fingerId+","+action+","+timeSinceFirstTouch+","+x+","+y+","+pressure+","+size);	
		Log.d(LOG_TAG,shapeId+","+touchId+","+count+","+fingerId+","+action+","+timeSinceFirstTouch+","+x+","+y+","+pressure+","+size);
	}

	public void closeFile(boolean timeEnded){
		printWriter.println("TPS: "+timePerShpae);
		if (!timeEnded){
			printWriter.println("Test was not completed");
		}
		this.printWriter.close();
	}

	public void deleteFile(){
		if(outputFile.delete()){
			Log.i(LOG_TAG,"Example file was deleted");
		}else{
			Log.e(LOG_TAG,"Unable to delete example file");
		}

	}


	private void writeHeaders(){
		Log.d(LOG_TAG, "Writing headers to file");
		String[] headers = Consts.outputFileHeaders;
		for (int i=0;i<headers.length-1;i++){
			printWriter.write(headers[i]);
			printWriter.write(",");
		}
		printWriter.println(headers[headers.length-1]);
	}

	public String getOutputFileName(){
		return outputFile.getName();
	}

	public String getOutputFilePath(){
		return outputFile.getAbsolutePath();
	}

}

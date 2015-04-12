package idoelad.finalproject.tremortest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

public class CircleView extends View{
	private int shapeId;
	private Paint paint;
	private float x;
	private float y;
	private float radius;


	public CircleView(Context context,int shapeId, float x, float y, float radius) {
		super(context);
		this.shapeId=shapeId;
		this.x=x;
		this.y=y;
		this.radius = radius;
		init();
	}

	private void init()
	{
		paint = new Paint();
		paint.setColor(Color.MAGENTA);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawCircle(x, y, radius, paint);
	}

	public int getShapeId() {
		return shapeId;
	}



}

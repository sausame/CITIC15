/**
 * 
 */
package curso.citic15.camara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author Song Bi
 * 
 */
public class BitmapSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {
	private final static String TAG = "BitmapSurfaceView";

	public BitmapSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getHolder().addCallback(this);
	}

	public BitmapSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
	}

	public BitmapSurfaceView(Context context) {
		super(context);
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
//		dummyRender();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {

	}

	public void render(Bitmap bm) {
		Rect dest = new Rect(0, 0, getWidth(), getHeight());
		Paint paint = new Paint();
		paint.setFilterBitmap(true);

		Canvas canvas = null;

		try {
			canvas = getHolder().lockCanvas();
//			canvas.scale(1.0f, 1.0f);
			canvas.drawBitmap(bm, null, dest, paint);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (canvas != null) {
				getHolder().unlockCanvasAndPost(canvas);
			}
		}
	}
	
	// -----------------------------------------------------------------------
	// Dummy
	// -----------------------------------------------------------------------
	private int mCount = 0;
	
	public void dummyRender() {
		int resId;
		
		if (0 == (++mCount / 10) % 2) {
			resId = R.drawable.icon;
		} else {
			resId = R.drawable.ic_launcher;
		}

		Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(),
				resId);

		render(bm);
	}
	
}
	


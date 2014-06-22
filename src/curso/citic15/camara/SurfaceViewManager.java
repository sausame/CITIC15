package curso.citic15.camara;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;

public class SurfaceViewManager extends Thread implements
		Camera.PreviewCallback {
	private static final String TAG = "SurfaceViewManager";

	private Context mContext;
	private ArrayList<BitmapSurfaceView> mSurfaceViewList = new ArrayList<BitmapSurfaceView>();
	private YuvToRgbHelper mYuvToRgbHelper = new YuvToRgbHelper();

	private byte mYuv[] = null;

	private boolean mIsTransforming = false;
	private boolean mIsStopping = false;

	public SurfaceViewManager(Context ctx) {
		mContext = ctx;
	}

	public void addBitmapSurfaceView(BitmapSurfaceView surfaceView) {
		mSurfaceViewList.add(surfaceView);
	}

	private void init(byte[] frameByte, Camera camera) {
		if (mYuvToRgbHelper.isInitialized()) {
			return;
		}

		Camera.Parameters p = camera.getParameters();
		Size previewSize = p.getPreviewSize();

		mYuvToRgbHelper.setWidth(previewSize.width);
		mYuvToRgbHelper.setHeight(previewSize.height);
		mYuvToRgbHelper.setLength(frameByte.length);
		mYuvToRgbHelper.setContext(mContext);
		mYuvToRgbHelper.setRotation(-90);

		mYuvToRgbHelper.init();

		start();
	}

	public void input(byte[] buffer) {
		synchronized (this) {
			if (mIsTransforming)
				return;
		}

		mYuv = Arrays.copyOf(buffer, buffer.length);

		synchronized (this) {
			mIsTransforming = true;
			notifyAll(); // Tell the thread there is new work to do.
		}
	}

	private void transform() {
		long startTime = System.currentTimeMillis();

		Bitmap bm = mYuvToRgbHelper.transform(mYuv);

		long endTime = System.currentTimeMillis();

		for (int i = 0; i < mSurfaceViewList.size(); i++) {
			BitmapSurfaceView bsv = mSurfaceViewList.get(i);
			if (null != bsv)
				bsv.render(bm);
		}

		synchronized (this) {
			mIsTransforming = false;
		}

		Log.v(TAG, "Transform: " + (endTime - startTime) + "ms, Render: "
				+ (System.currentTimeMillis() - endTime) + "ms");
	}

	@Override
	public void onPreviewFrame(byte[] frameByte, Camera camera) {
		long startTime = System.currentTimeMillis();

		init(frameByte, camera);
		input(frameByte);

		long endTime = System.currentTimeMillis();

		Log.v(TAG, "Preview Frame: " + (endTime - startTime) + "ms");
	}

	// Runs in saver thread
	@Override
	public void run() {
		while (!mIsStopping) {
			synchronized (this) {
				while (!mIsTransforming && !mIsStopping) {
					try {
						wait();
					} catch (InterruptedException ex) {
						// ignore.
					}
				}
			}

			transform();
		}

		synchronized (this) {
			notifyAll(); // notify main thread in waitDone
		}
	}

	// Runs in main thread
	public void finish() {
		// waitDone();
		synchronized (this) {
			mIsStopping = true;

			notifyAll();
		}

		try {
			join();
		} catch (InterruptedException ex) {
		}
	}
}

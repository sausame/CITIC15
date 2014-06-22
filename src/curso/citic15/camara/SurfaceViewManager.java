package curso.citic15.camara;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;

public class SurfaceViewManager implements Camera.PreviewCallback {
	private static final String TAG = "SurfaceViewManager";

	private Context mContext;
	private ArrayList<BitmapSurfaceView> mSurfaceViewList = new ArrayList<BitmapSurfaceView>();
	private YuvToRgbHelper mYuvToRgbHelper = new YuvToRgbHelper();

	public SurfaceViewManager(Context ctx) {
		mContext = ctx;
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
	}

	public void finish() {
		mYuvToRgbHelper.finish();
	}

	public void addBitmapSurfaceView(BitmapSurfaceView surfaceView) {
		mSurfaceViewList.add(surfaceView);
	}

	@Override
	public void onPreviewFrame(byte[] frameByte, Camera camera) {
		init(frameByte, camera);

		Bitmap bm = mYuvToRgbHelper.getOutputBitmap();

		for (int i = 0; i < mSurfaceViewList.size(); i++) {
			BitmapSurfaceView bsv = mSurfaceViewList.get(i);
			if (null != bsv)
				bsv.render(bm);
		}

		mYuvToRgbHelper.input(frameByte);
	}

}

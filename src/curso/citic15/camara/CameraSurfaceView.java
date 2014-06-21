package curso.citic15.camara;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	private Camera.PreviewCallback mPreviewCallback = null;
	private Camera mCamera;
	
	public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getHolder().addCallback(this);
	}

	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
	}

	public CameraSurfaceView(Context context) {
		super(context);
		getHolder().addCallback(this);
	}

	public void setPreviewCallback(Camera.PreviewCallback callback) {
		mPreviewCallback = callback;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open(1);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			Camera.Size selected = sizes.get(0);
			params.setPreviewSize(selected.width, selected.height);
			mCamera.setParameters(params);
			mCamera.setDisplayOrientation(90); // Front Camera: 90
			mCamera.setPreviewCallback(mPreviewCallback);

			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}

			mCamera.startPreview();
	}
}

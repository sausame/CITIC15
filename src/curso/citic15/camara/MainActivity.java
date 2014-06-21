package curso.citic15.camara;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback,
		Camera.ShutterCallback, Camera.PictureCallback, Camera.PreviewCallback {

	private SurfaceView preview;
	Camera camera;
	Bitmap photo;
	BitmapSurfaceView bsv1 = null;
	BitmapSurfaceView bsv2 = null;
	CanvasView cv = null;
	YuvToRgbHelper mYuvToRgbHelper = new YuvToRgbHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FrameLayout surfaceViewFrame = (FrameLayout) findViewById(R.id.surfaceviewFrame);
		cv = new CanvasView(this);
		// surfaceViewFrame.addView(cv);

		bsv1 = (BitmapSurfaceView) findViewById(R.id.bsv1);
		bsv2 = (BitmapSurfaceView) findViewById(R.id.bsv2);

		preview = (SurfaceView) findViewById(R.id.surfCamara);
		preview.getHolder().addCallback(this);
		Button shutter = (Button) findViewById(R.id.button1);
		shutter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				camera.takePicture(MainActivity.this, null, null,
						MainActivity.this);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		camera.release();
	}

	@Override
	protected void onPause() {
		super.onPause();
		camera.stopPreview();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onShutter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			Camera.Size selected = sizes.get(0);
			params.setPreviewSize(selected.width, selected.height);
			camera.setParameters(params);
			camera.setDisplayOrientation(90); // Back Camera: 90
			camera.setPreviewCallback(this);

			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}

			camera.startPreview();
		} else {
			Toast.makeText(this, "No hay camara o hay alg�n error.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open(1);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("", "Destroyed");
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 6;

		photo = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		camera.startPreview();
	}

	@Override
	public void onPreviewFrame(byte[] frameByte, Camera camera) {
		if (!mYuvToRgbHelper.isInitialized()) {

			Camera.Parameters p = camera.getParameters();
			Size previewSize = p.getPreviewSize();

			mYuvToRgbHelper.setWidth(previewSize.width);
			mYuvToRgbHelper.setHeight(previewSize.height);
			mYuvToRgbHelper.setLength(frameByte.length);
			mYuvToRgbHelper.setContext(this);

			mYuvToRgbHelper.init();
		}

		Bitmap bm = mYuvToRgbHelper.getOutputBitmap();

		if (null != bm) {
//			Log.e("", "Bitmap: " + bm);
			if (null != bsv1) {
				bsv1.render(bm);
//				bsv1.dummyRender();
			}

			if (null != bsv2) {
				bsv2.render(bm);
//				 bsv2.dummyRender();
			}
		}

		mYuvToRgbHelper.input(frameByte);
	}

}

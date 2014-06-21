package curso.citic15.camara;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	private SurfaceViewManager mSurfaceViewManager = new SurfaceViewManager(
			this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		CameraSurfaceView csv = (CameraSurfaceView) findViewById(R.id.surfCamara);
		csv.setPreviewCallback(mSurfaceViewManager);

		BitmapSurfaceView bsv1 = (BitmapSurfaceView) findViewById(R.id.bsv1);
		BitmapSurfaceView bsv2 = (BitmapSurfaceView) findViewById(R.id.bsv2);

		mSurfaceViewManager.addBitmapSurfaceView(bsv1);
		mSurfaceViewManager.addBitmapSurfaceView(bsv2);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSurfaceViewManager.finish();
	}
}

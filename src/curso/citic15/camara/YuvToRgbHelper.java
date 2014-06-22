package curso.citic15.camara;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class YuvToRgbHelper extends Thread {
	private static final String TAG = "YuvToRgbHelper";

	private static final boolean IS_TO_DUMP = false;

	private static final int INVALID_INT = -1;

	private int mWidth = INVALID_INT;
	private int mHeight = INVALID_INT;
	private int mLength = INVALID_INT;

	private boolean mIsInitialized = false;

	private Matrix mMatrix = null;

	private Bitmap mBitmap = null;
	private Bitmap mOutputBitmap = null;

	/** Scripts **/
	private RenderScript mRenderScript = null;
	private ScriptIntrinsicYuvToRGB mScript = null;

	/** RenderScript buffers **/
	private Allocation mInput = null;
	private Allocation mOutput = null;

	private boolean mIsTransforming = false;
	private boolean mIsStopping = false;

	private Context mContext = null;

	private byte mYuv[] = null;

	public void setWidth(int width) {
		mWidth = width;
	}

	public void setHeight(int height) {
		mHeight = height;
	}

	public void setLength(int length) {
		mLength = length;
	}

	public void setContext(Context ctx) {
		mContext = ctx;
	}

	public void setRotation(int degree) {
		mMatrix = new Matrix();

		mMatrix.postRotate(degree);
	}

	public boolean isInitialized() {
		return mIsInitialized;
	}

	public synchronized void init() {
		if (mIsInitialized || INVALID_INT == mWidth || INVALID_INT == mHeight
				|| INVALID_INT == mLength || null == mContext) {
			return;
		}

		initParams();

		mIsInitialized = true;

		start();
	}

	private void initParams17() {
		Log.v(TAG, "[" + mWidth + ", " + mHeight + "] " + mLength);

		mRenderScript = RenderScript.create(mContext);
		mScript = ScriptIntrinsicYuvToRGB.create(mRenderScript,
				Element.U8_4(mRenderScript));

		mInput = Allocation.createSized(mRenderScript,
				Element.U8(mRenderScript), mLength);
		mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		mOutput = Allocation.createFromBitmap(mRenderScript, mBitmap);
	}

	@SuppressLint("NewApi")
	private void initParams18() {
		Log.v(TAG, "[" + mWidth + ", " + mHeight + "] " + mLength);

		mRenderScript = RenderScript.create(mContext);
		mScript = ScriptIntrinsicYuvToRGB.create(mRenderScript,
				Element.U8_4(mRenderScript));

		Type.Builder tb = new Type.Builder(mRenderScript, Element.createPixel(
				mRenderScript, Element.DataType.UNSIGNED_8,
				Element.DataKind.PIXEL_YUV));
		tb.setX(mWidth);
		tb.setY(mHeight);
		tb.setMipmaps(false);
		tb.setYuvFormat(ImageFormat.NV21);

		mInput = Allocation.createTyped(mRenderScript, tb.create(),
				Allocation.USAGE_SCRIPT);

		Type.Builder tb2 = new Type.Builder(mRenderScript,
				Element.RGBA_8888(mRenderScript));
		tb2.setX(mWidth);
		tb2.setY(mHeight);
		tb2.setMipmaps(false);

		mOutput = Allocation.createTyped(mRenderScript, tb2.create(),
				Allocation.USAGE_SCRIPT & Allocation.USAGE_SHARED);
		mOutput.setSurface(null);

		mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
	}

	private void initParams() {
		final int SDK_INT = android.os.Build.VERSION.SDK_INT;

		if (SDK_INT <= 17) {
			initParams17();
		} else {
			initParams18();
		}
	}

	public void input(byte[] buffer) {
		synchronized (this) {
			if (mIsTransforming)
				return;
		}

		if (IS_TO_DUMP) {
			mYuv = Arrays.copyOf(buffer, buffer.length);
		}

		mInput.copyFrom(buffer);

		synchronized (this) {
			mIsTransforming = true;

			notifyAll(); // Tell the thread there is new work to do.
		}
	}

	public Bitmap getOutputBitmap() {
		synchronized (this) {
			if (mIsTransforming)
				return null;

			return mOutputBitmap;
		}
	}

	private void transform() {
		// Starting a transform.

		mScript.setInput(mInput);
		mScript.forEach(mOutput);

		mOutput.copyTo(mBitmap);

		if (null != mMatrix) {
			mOutputBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
					mBitmap.getWidth(), mBitmap.getHeight(), mMatrix, true);
		} else {
			mOutputBitmap = mBitmap;
		}

		if (IS_TO_DUMP) {
			saveBitmap(mBitmap, mYuv);
		}

		// A transform is completed.

		synchronized (this) {
			mIsTransforming = false;
		}
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

	// -----------------------------------------------------------------------
	// Dummy
	// -----------------------------------------------------------------------
	private static void saveBitmap(Bitmap bm, byte[] yuvs) {
		File path = Environment.getExternalStorageDirectory();
		File yuv2rgbDir = new File(path + "/Yub2Rgb");
		yuv2rgbDir.mkdirs();

		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.US);
		String base = sdf.format(now);

		String fileName = yuv2rgbDir.getAbsolutePath() + "/" + base;

		saveBitmap(fileName + ".jpg", bm);
		writeFile(fileName + ".yuv", yuvs);
	}

	private static void saveBitmap(String fileName, Bitmap bm) {
		try {
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			bm.compress(CompressFormat.JPEG, 100, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(String fileName, byte[] bytes) {
		if (fileName != null) {
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			FileOutputStream fo;
			try {
				fo = new FileOutputStream(file);
				fo.write(bytes);
				fo.flush();
				fo.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

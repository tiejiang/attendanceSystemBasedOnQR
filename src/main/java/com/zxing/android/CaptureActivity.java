package com.zxing.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zxing.android.camera.CameraManager;
import com.zxing.android.database.DatabaseCreate;
import com.zxing.android.decoding.CaptureActivityHandler;
import com.zxing.android.decoding.InactivityTimer;
import com.zxing.android.sms.SMS;
import com.zxing.android.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

public class CaptureActivity extends Activity implements Callback {
	public static final String QR_RESULT = "RESULT";

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private SurfaceView surfaceView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	// private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	CameraManager cameraManager;
	private SQLiteDatabase database;
	private String stuCollege = "";
	private String stuClass = "";
	private Vector allStudents;  // load all students in the class
    private Vector mTemVector;   // load attendance students
    private Vector stuAbsence;  // load absence students

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_capture);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderview);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		Intent mIntent = getIntent();
		Bundle mBundle = mIntent.getExtras();
		stuCollege = mBundle.getString("college");
		stuClass = mBundle.getString("class");
		//init Vector
		mTemVector = new Vector();
		stuAbsence = new Vector();
		//load student from database
		loadStudent(stuCollege, stuClass);
		Log.d("TIEJIANG", "CaptureActivity stuCollege= " + stuCollege + ", stuClass= " + stuClass);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		// CameraManager.init(getApplication());
		cameraManager = new CameraManager(getApplication());

		viewfinderView.setCameraManager(cameraManager);

		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		cameraManager.closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
        if (allStudents != null){
            allStudents.removeAllElements();
        }
        if (mTemVector != null){
            mTemVector.removeAllElements();
        }
        if (stuAbsence != null){
            stuAbsence.removeAllElements();
        }
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			// CameraManager.get().openDriver(surfaceHolder);
			cameraManager.openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}
	//load student from database
	private void loadStudent(String stu_college, final String stu_class){
		new Thread(new Runnable() {
			@Override
			public void run() {
				allStudents = new Vector();
				Cursor mCursor;
				if (stu_class.equals("10")){
					database = SQLiteDatabase.openOrCreateDatabase(DatabaseCreate.DATABASE_PATH + DatabaseCreate.dbName, null);
					String sql = "SELECT * FROM class_10 ";
					mCursor = database.rawQuery(sql, null);
					if (mCursor.moveToFirst()){
						do {
							allStudents.add(mCursor.getString(mCursor.getColumnIndex("student_name")));
						}while (mCursor.moveToNext());
					}
					// test code begin
//					for (int i = 0; i < allStudents.size(); i ++){
//						Log.d("TIEJIANG", "class_10 student_name= " + allStudents.get(i));
//					}
					// test code end
				}else if (stu_class.equals("11")){
					database = SQLiteDatabase.openOrCreateDatabase(DatabaseCreate.DATABASE_PATH + DatabaseCreate.dbName, null);
					String sql = "SELECT * FROM class_11 ";
					mCursor = database.rawQuery(sql, null);
					if (mCursor.moveToFirst()){
						do {
							allStudents.add(mCursor.getString(mCursor.getColumnIndex("student_name")));
						}while (mCursor.moveToNext());
					}
					// test code begin
//					for (int i = 0; i < allStudents.size(); i ++){
//						Log.d("TIEJIANG", "class_11 student_name= " + allStudents.get(i));
//					}
					// test code end
				}

			}
		}).start();
	}
	/**
	 * get the QR code scanning result
	 * Result obj
	 * "结果：" + obj.getText()
	 * "类型:" + obj.getBarcodeFormat()
	 * */
	public void handleDecode(Result obj, Bitmap barcode) {
		String stuName = "不存在";
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();

		stuName = inquireData(obj.getText());
		showResult(obj, barcode, stuName);
	}
	//inquire
    public String inquireData(String stu_id){

		Cursor mCursor;
		String queryStr = "";
        String ID = stu_id;
		String startYear = "";
		String stuCollege = "";
		String stuClass = "";
		String stuID = "";
		String stuName = "";
		if (ID.length() == 10){
			startYear = ID.substring(0, 4);
			stuCollege = ID.substring(4, 6);
			stuClass = ID.substring(6, 8);
			if (ID.substring(8, 9).equals("0")){
				stuID = ID.substring(9, 10);
			}else {
				stuID = ID.substring(8, 10);
			}
		}else{
			Toast.makeText(this, "此ID不存在", Toast.LENGTH_SHORT).show();
		}
		database = SQLiteDatabase.openOrCreateDatabase(DatabaseCreate.DATABASE_PATH + DatabaseCreate.dbName, null);
		if (stuClass.equals("10")){
			queryStr = "SELECT * FROM class_10 WHERE start_year = '" + startYear + "' AND college = '" + stuCollege + "' AND class = '" + stuClass + "' AND student_id = '" + stuID + "'";
		}else if (stuClass.equals("11")){
			queryStr = "SELECT * FROM class_11 WHERE start_year = '" + startYear + "' AND college = '" + stuCollege + "' AND class = '" + stuClass + "' AND student_id = '" + stuID + "'";
		}

//		String queryStr = "SELECT * FROM class_10 WHERE student_name = '张剑'";
		mCursor = database.rawQuery(queryStr, null);
		if (mCursor.moveToFirst()){
			stuName = mCursor.getString(mCursor.getColumnIndex("student_name"));

		}
		Log.d("TIEJIANG", "query--startYear = " + startYear + ", stuCollege = " + stuCollege + ", stuClass = " + stuClass + ", stuID = " + stuID);
		Log.d("TIEJIANG", "query--stuName = " + stuName);
		mCursor.close();
		return stuName!=null?stuName:"不存在";
    }


	private void showResult(final Result rawResult, Bitmap barcode, final String name) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		Drawable drawable = new BitmapDrawable(barcode);
		builder.setIcon(drawable);

//		builder.setTitle("类型:" + rawResult.getBarcodeFormat() + "\n学号:" + rawResult.getText() + "\r\n姓名:" + name);
		builder.setTitle("学号:" + rawResult.getText() + "\n姓名:" + name);
		builder.setPositiveButton("结束考勤", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				mTemVector.add(name); // add the last student into the vector
				//一次扫描完成
				Intent intent = new Intent();
				intent.putExtra("result", rawResult.getText());
				setResult(RESULT_OK, intent);
//				finish();
				int result = checkoutAttendance(allStudents, mTemVector);
				if (result == 1){
					noOneAbsence();
				}else if (result == 0){
					sendToHeadTeacher(stuAbsence);
				}
			}
		});
		builder.setNegativeButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
                mTemVector.add(name);
				Log.d("确定：TIEJIANG", "mTemVector= " + mTemVector);
				restartPreviewAfterDelay(0L);
			}
		});
//		builder.setNeutralButton("结束考勤", new OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//				//进行数据库的查询匹配并得出考勤结果
//
//
//			}
//		});
		builder.setCancelable(false);
		builder.create().show();

		// Intent intent = new Intent();
		// intent.putExtra(QR_RESULT, rawResult.getText());
		// setResult(RESULT_OK, intent);
		// finish();
	}

	public void noOneAbsence(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("本次考勤没有学生缺席");
		builder.setMessage("谢谢使用二维码考勤系统");
		builder.setNegativeButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				finish();
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}

	public void sendToHeadTeacher(final Vector absence_stu){


		LayoutInflater mLayoutInflater = getLayoutInflater();
		View absenceView = mLayoutInflater.inflate(R.layout.stu_absence_txt, null);
		TextView absence_txt = (TextView) absenceView.findViewById(R.id.stu_absence);
		//统计缺席学生名单（为显示做准备）
		for (int i = 0; i < absence_stu.size(); i ++){
			if (i > 0){
				absence_txt.append(", ");
			}
			absence_txt.append(String.valueOf(absence_stu.get(i)));
		}
		final String sendMsg = "班主任老师, " + "你好,你们班缺席xx课的学生名单如下：" + "\n" + absence_txt.getText().toString();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("缺席学生名单");
		builder.setView(absenceView);
		builder.setNegativeButton("发送到班主任", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				new SMS(CaptureActivity.this).send("18845292770", sendMsg);
				finish();
			}
		});
        builder.setPositiveButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
		builder.setCancelable(false);
		builder.create().show();
	}
	public int checkoutAttendance(Vector all_students, Vector temp_vector){
        String tempStu;
			//test code begin
//			for (int j = 0; j < all_students.size(); j ++){
//				Log.d("TIEJIANG", "all_students value= " + all_students.get(j));
//			}
//			Log.d("TIEJIANG", "temp_vector.size= " + temp_vector.size());
//            for (int j = 0; j < temp_vector.size(); j ++){
//                Log.d("TIEJIANG", "temp_vector value= " + temp_vector.get(j));
//            }

        if (all_students.size() == temp_vector.size()){
            return 1;  //full attendance
        }else {
            for (int i = 0; i < all_students.size(); i ++){
                tempStu = String.valueOf(all_students.get(i));
                if (!temp_vector.contains(tempStu)){
                    stuAbsence.add(tempStu);
                }
            }
            //test code begin
            for (int j = 0; j < stuAbsence.size(); j ++){
                Log.d("TIEJIANG", "stuAbsence value= " + stuAbsence.get(j));
            }
            //test code end
            return 0;
        }
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(MessageIDs.restart_preview, delayMS);
		}
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			try {
				AssetFileDescriptor fileDescriptor = getAssets().openFd("qrbeep.ogg");
				this.mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
						fileDescriptor.getLength());
				this.mediaPlayer.setVolume(0.1F, 0.1F);
				this.mediaPlayer.prepare();
			} catch (IOException e) {
				this.mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
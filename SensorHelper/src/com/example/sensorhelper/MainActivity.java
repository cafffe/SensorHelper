package com.example.sensorhelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.R.menu;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat") public class MainActivity extends Activity implements SensorEventListener,OnClickListener{
	private SensorManager sensorManager;
	
	private StringBuffer sb_acc=null;
	private StringBuffer sb_mag=null;
	private StringBuffer sb_gyo=null;
	private StringBuffer sb_ori=null;
	private StringBuffer sb_lin=null;
	
	private static final String path=Environment.getExternalStorageDirectory().getPath().toString()+"/Sensorhelper";
	
	private String path_acc;
	private String path_mag;
	private String path_gyo;
	private String path_ori;
	private String path_lin;
	
	private File file_acc;
	private File file_mag;
	private File file_gyo;
	private File file_ori;
	private File file_lin;
	
	private int[] count;
	
	private Button start;
	private Button stop;
	private Button delete;
	private TextView log;
	
	private boolean isRecord=false;
	private StringBuffer logSb;
	
	private SimpleDateFormat formatter;
	private SimpleDateFormat newFormat;
	private FileHelper fileHelper;
	private Date date;
	private int num;
	private Message numMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);   
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        setContentView(R.layout.activity_main);
        
        initView();
        
        sensorManager=(SensorManager)getSystemService(Service.SENSOR_SERVICE);
        //加速度
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
        //陀螺仪
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_GAME);
        //方向
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        //磁力计
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
        //线性加速度
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),SensorManager.SENSOR_DELAY_GAME);
        
        fileHelper=new FileHelper();
        
        File file=new File(path);
        if(!file.exists()){
        	file.mkdir();
        }
        
        sb_acc=new StringBuffer();
        sb_gyo=new StringBuffer();
        sb_mag=new StringBuffer();
        sb_ori=new StringBuffer();
        sb_lin=new StringBuffer();
        
        count=new int[5];
        logSb=new StringBuffer();
        
        formatter=new SimpleDateFormat("MM:dd HH:mm:ss");  
        newFormat=new SimpleDateFormat("ss");
		Date curDate=new Date(System.currentTimeMillis());//获取当前时间     
		String str=formatter.format(curDate);
		str=str.replace("/", ".");
		str=str.replace(" ", "_");
		str=str.replace(":", "_");
		
		path_acc=path+File.separator+str.trim().toString()+"acc"+".txt";
		path_gyo=path+File.separator+str.trim().toString()+"gyo"+".txt";
		path_ori=path+File.separator+str.trim().toString()+"ori"+".txt";
		path_mag=path+File.separator+str.trim().toString()+"mag"+".txt";
		path_lin=path+File.separator+str.trim().toString()+"lin"+".txt";
		
		file_acc=new File(path_acc);
		file_gyo=new File(path_gyo);
		file_ori=new File(path_ori);
		file_mag=new File(path_mag);
		file_lin=new File(path_lin);
		
		try {
			file_acc.createNewFile();
			file_gyo.createNewFile();
			file_ori.createNewFile();
			file_mag.createNewFile();
			file_lin.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(MainActivity.this, "文件创建失败！", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}finally{
			logSb.append("加速度文件创建成功，文件位于：").append(path_acc).append("\n");
			logSb.append("陀螺仪文件创建成功，文件位于：").append(path_gyo).append("\n");
			logSb.append("方向传感器文件创建成功，文件位于：").append(path_ori).append("\n");
			logSb.append("磁力计文件创建成功，文件位于：").append(path_mag).append("\n");
			logSb.append("线性加速度文件创建成功，文件位于：").append(path_lin).append("\n");
			Message msg_stop=new Message();
			msg_stop.what=3;
			handler.handleMessage(msg_stop);
		}
    }
    private void initView(){
    	start=(Button)findViewById(R.id.start);
    	stop=(Button)findViewById(R.id.stop);
    	delete=(Button)findViewById(R.id.delete);
    	stop.setClickable(false);
    	stop.setBackgroundResource(R.color.gray);
    	start.setOnClickListener(this);
    	stop.setOnClickListener(this);
    	delete.setOnClickListener(this);
    	
    	log=(TextView)findViewById(R.id.log);
    	log.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
	@Override
	public void onAccuracyChanged(Sensor sensor, int value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
	synchronized (this) {
		Date curDate=new Date(System.currentTimeMillis());
		String str=newFormat.format(curDate);
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			if(isRecord){
				num++;
				if(num%50==0){
					logSb.append("已记录数据：").append(num).append("\n");
					numMsg=new Message();
			        numMsg.what=4;
					handler.handleMessage(numMsg);
				}
				event.values.clone();
				if(count[0]>50){
					count[0]=0;
					fileHelper.fileWriter(sb_acc, path_acc);
					sb_acc.setLength(0);
				}else{
					count[0]++;
					sb_acc.append(str+" ").append(event.values[0]+" ").append(event.values[1]+" ").append(event.values[2]+"\n");
				}
			}
			break;
		case Sensor.TYPE_GYROSCOPE:
			if(isRecord){
				event.values.clone();
				if(count[1]>50){
					count[1]=0;
					fileHelper.fileWriter(sb_gyo, path_gyo);
					sb_gyo.setLength(0);
				}else{
					count[1]++;
					sb_gyo.append(str+" ").append(event.values[0]+" ").append(event.values[1]+" ").append(event.values[2]+"\n");
				}
			}
			break;
		case Sensor.TYPE_ORIENTATION:
			if(isRecord){
				event.values.clone();
				if(count[2]>50){
					count[2]=0;
					fileHelper.fileWriter(sb_ori, path_ori);
					sb_ori.setLength(0);
				}else{
					count[2]++;
					sb_ori.append(str+" ").append(event.values[0]+" ").append(event.values[1]+" ").append(event.values[2]+"\n");
				}
			}
			break;
		case Sensor.TYPE_LINEAR_ACCELERATION:
			if(isRecord){
				event.values.clone();
				if(count[3]>50){
					count[3]=0;
					fileHelper.fileWriter(sb_lin, path_lin);
					sb_lin.setLength(0);
				}else{
					count[3]++;
					sb_lin.append(str+" ").append(event.values[0]+" ").append(event.values[1]+" ").append(event.values[2]+"\n");
				}
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			if(isRecord){
				event.values.clone();
				if(count[4]>50){
					count[4]=0;
					fileHelper.fileWriter(sb_mag, path_mag);
					sb_mag.setLength(0);
				}else{
					count[4]++;
					sb_mag.append(str+" ").append(event.values[0]+" ").append(event.values[1]+" ").append(event.values[2]+"\n");
				}
			}
			break;
		default:
			break;
		}
	}
	}
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.start:
			isRecord=true;
			start.setClickable(false);
			start.setBackgroundResource(R.color.gray);
			stop.setBackgroundResource(R.color.black);
			stop.setClickable(true);
			Message msg_start=new Message();
			msg_start.what=1;
			handler.handleMessage(msg_start);			
			break;
		case R.id.stop:
			isRecord=false;
			stop.setClickable(false);
			stop.setBackgroundResource(R.color.gray);
			start.setBackgroundResource(R.color.black);
			start.setClickable(true);
			Message msg_stop=new Message();
			msg_stop.what=2;
			handler.handleMessage(msg_stop);
			break;
		case R.id.delete:
			Message msg_del=new Message();
			msg_del.what=5;
			handler.handleMessage(msg_del);
			break;
		default:
			break;
		}
	}
	
	@SuppressLint("HandlerLeak") Handler handler=new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what) {
			case 1:
				logSb.append("开始记录；").append("\n");
				log.setText(logSb.toString());
				break;
			case 2:
				logSb.append("停止记录；").append("\n");
				log.setText(logSb.toString());
				break;
			case 3:
				logSb.append("文件创建完毕！").append("\n");
				log.setText(logSb.toString());
				break;
			case 4:
				log.setText(logSb.toString());
				break;
			case 5:
				logSb.setLength(0);
				log.setText(logSb.toString());
				break;
			case 6:
				log.setText(logSb.toString());
				break;
			default:
				break;
			}
		}
	};
	
	private void showTips() {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
		.setTitle("退出程序")
		.setMessage("是否退出程序?")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
		MainActivity.this.finish();
		System.exit(0);
		onDestroy();
		}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		return;
		}
		}).create();

		alertDialog.show();
		}
		public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

		this.showTips();
		return false;
		}
		return false;
	}
		public boolean onCreateOptionsMenu(Menu menu){
			menu.add(0, Menu.FIRST, 0, "关于");
			return true;
		}
		public boolean onOptionsItemSelected(MenuItem item) {  
			switch (item.getItemId()) {
			case Menu.FIRST:
				logSb.setLength(0);
				logSb.append(getResources().getString(R.string.info));
				Message msg_info=new Message();
				msg_info.what=6;
				handler.handleMessage(msg_info);
				break;

			default:
				break;
			}
			return true;
		}

}

package com.zlj.busphone;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.zlj.AndroidToolsClass.AndroidToolsClass;
import com.zlj.BaiDuMap.BaiDuMapHelper;
import com.zlj.busphone.MessageClss.LatLngMsg;
import com.zlj.busphone.MessageClss.NetPacket;
import com.zlj.busphone.MessageClss.RequestPacket;

public class MainActivity extends Activity {
	
	private MapView mMapView = null;
	private BaiDuMapHelper mBaiDuMap;
	private AndroidToolsClass mAndroidToolsClass;
	private long mExitTime = 0;
	private MessageClss msgc;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext  
        SDKInitializer.initialize(getApplicationContext());  
        
        setContentView(R.layout.activity_main);
        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiDuMap = new BaiDuMapHelper(getApplicationContext(), mMapView);
        mBaiDuMap.StartLocate();
        
        mAndroidToolsClass = new AndroidToolsClass(getApplicationContext());
        mAndroidToolsClass.CheckNetworkStatus();//��������
        
		msgc = new MessageClss();
		
		//�����߳�
		DrawLineThread RMThread = new DrawLineThread();
		RMThread.start();
		
		//������־����
		Intent  serviceIntent=new Intent();
		serviceIntent.setAction("com.zlj.busphone.MY_SERVICE");
		serviceIntent.setClass(MainActivity.this, LogService.class);
		startService(serviceIntent);
	
		writeFileData("busPhone.txt", "busPhone11111");
		System.out.println(readLine("busPhone.txt"));
    }
    
    public void writeFileData(String fileName, String message) {  
    
        try {  
        	
            /* ��һ�������������ļ����ƣ�ע��������ļ����Ʋ��ܰ����κε�/����/���ַָ�����ֻ�����ļ��� 
             *          ���ļ��ᱻ������/data/data/Ӧ������/files/chenzheng_java.txt */
            FileOutputStream outputStream = openFileOutput(fileName, Activity.MODE_PRIVATE);  
            outputStream.write(message.getBytes());
            outputStream.write("/r/t".getBytes());
            outputStream.flush();  
            outputStream.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
    }

    
    public String readLine(String fileName) {  
    	String content =null;
        try {  
            FileInputStream inputStream = this.openFileInput(fileName);  
            byte[] bytes = new byte[1024];  
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();  
            while (inputStream.read(bytes) != -1) {  
                arrayOutputStream.write(bytes, 0, bytes.length);  
            }  
            inputStream.close();  
            arrayOutputStream.close();  
            content = new String(arrayOutputStream.toByteArray());  
            //showTextView.setText(content);  
            
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return content;
  
    } 
    
   

    
    
    
    /**
     * ���ݴ����߳�
     * @author zlj
     *
     */
    class DrawLineThread extends Thread
    {
    	int recmsglen = 0;
		byte[] recibuf = new byte[512];
		int pktLen = 0;
		int len;
		
		byte[] pktdata = new byte[512];
		
		RequestPacket RePkt = new RequestPacket();
		NetPacket pkt = new NetPacket();

		@Override
		public void run()
		{
			while(msgc.flag == 1)
			{
				try {
					//ʵ����socket
					msgc.socket = new Socket(msgc.severIP, msgc.severPort);
					msgc.flag = 0;
					System.out.println("���ӷ������ɹ���");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					msgc.flag = 1;
					System.out.println("���ӷ�����ʧ�ܣ�");
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			if(msgc.flag == 0)
			{
				try {//����������
					msgc.writemsg = msgc.socket.getOutputStream();
					msgc.readmsg = msgc.socket.getInputStream();//����
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
			
			while(true)
			{
				if(msgc.flag == 0)
				{
					try {
						
						//��������
						msgc.writemsg.write(msgc.intToByte(RePkt.head));
						msgc.writemsg.write(RePkt.msg);
						msgc.writemsg.flush();
								
						//���ճ���
						recmsglen = msgc.readmsg.read(recibuf);
						pktLen = msgc.bytesToIntBig(recibuf);
						len = pktLen;//վ�����
						
						//����վ����Ϣ
						while(pktLen > 0)
						{
							recmsglen = msgc.readmsg.read(recibuf);
							System.arraycopy(recibuf, 0, pktdata, len-pktLen,  recmsglen);	
							pktLen -= recmsglen;
						}
						
						pkt.head = msgc.bytesToIntLittle(pktdata);
						
						//����λ����Ϣ
						LatLngMsg llm = new LatLngMsg();
						llm.latlngmag = msgc.ChangeLatLngToInt(mBaiDuMap.mLatitude, mBaiDuMap.mLongitude);
						msgc.writemsg.write(msgc.intToByte(llm.len));
						msgc.writemsg.write(llm.head);
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[0]));
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[1]));
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[2]));
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[3]));
						msgc.writemsg.flush();
						
					//	if((Arrays.toString(pkt.data)).equals((Arrays.toString(pktdata))))//�ж����ݰ��Ƿ���ϴ���ͬ
						{
							pkt.data = pktdata;
							
							//�������ݰ�
							msgc.GetSite(len, pkt.data);
							msgc.GetSiteLatLng();
							
							//����
							mBaiDuMap.dNum = msgc.SiteNum;
							mBaiDuMap.dSiteLatlLngList = msgc.SiteLatlLngList;
							mBaiDuMap.ShowLocationMark();
						}				    	
					} catch (Exception  e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}		
    }

    /*
     * ��������onKeyDown
     * ��    ����keyCode��event
     * ����ֵ��boolean
     * ˵    ���������η��ؼ��˳�����
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        mAndroidToolsClass.ToastShow("�ٰ�һ���˳�����");
                        mExitTime = System.currentTimeMillis();

                } else {
                        finish();
                        mBaiDuMap.AllDestroy();
                        msgc.Close();
                        System.exit(0);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }	
    


	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onDestroy();  
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onResume();  
    }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onPause();  
    }  

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
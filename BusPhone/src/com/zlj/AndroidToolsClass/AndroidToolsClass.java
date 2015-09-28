package com.zlj.AndroidToolsClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class AndroidToolsClass {
	
	private Context ATCcontext;
	private ConnectivityManager mConnectivityManager;   
	private NetworkInfo netInfo; 
	
	public AndroidToolsClass(Context context)
	{
		ATCcontext = context;
	}
	
	
	/**
	 * ��ʾ
	 * @param str
	 * @author zlj
	 */
    public void ToastShow(String str)
    {
    	Toast.makeText(ATCcontext, str, Toast.LENGTH_SHORT).show();  
    }
    
    /**
     * �������״̬
     * @author zlj
     */
    public void CheckNetworkStatus()
    {
        /**
         * ����״̬����
         */
        BroadcastReceiver myNetReceiver = new BroadcastReceiver() { 
        	   
        	 @Override 
        	 public void onReceive(Context context, Intent intent) { 
        	     
        	  String action = intent.getAction(); 
        	        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) { 
        	              
        	            mConnectivityManager = (ConnectivityManager)ATCcontext.getSystemService(Context.CONNECTIVITY_SERVICE); 
        	            netInfo = mConnectivityManager.getActiveNetworkInfo();   
        	            if(netInfo != null && netInfo.isAvailable()) { 
        	   
        	                 //�������� 
        	            	ToastShow("����������!"); 
        	            } 
        	            else { 
        	            	//����Ͽ� 
        	            	ToastShow("����Ͽ������������磡");       	   
        	            } 
        	        } 
        	   }  
        };     
       //��̬ע��㲥    	   
       IntentFilter mFilter = new IntentFilter(); 
       mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
       
       ATCcontext.registerReceiver(myNetReceiver, mFilter);       
       //����㲥       
       //if(myNetReceiver!=null){ 
       // unregisterReceiver(myNetReceiver); 
       // }    	
    }
    
    
    
}

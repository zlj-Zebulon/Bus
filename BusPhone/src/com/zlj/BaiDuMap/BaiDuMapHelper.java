package com.zlj.BaiDuMap;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapDrawFrameCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;

import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.zlj.AndroidToolsClass.AndroidToolsClass;
import com.zlj.busphone.MainActivity;
import com.zlj.busphone.R;



/**
 * 
 * @author zlj
 *
 */
@SuppressLint("UseSparseArrays")
public class BaiDuMapHelper implements OnMapDrawFrameCallback{

	private static final String BAIDU_DEFAULT = "bd09ll"; 	//���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
	private static final int LOCATE_INTERVAL = 5000;		//���÷���λ����ļ��ʱ��Ϊ5000ms
	private static final float MAP_SCALE = 15;				//500��
	private static final int MARK_NUM = 10;
	
	public  int Point_Num = 0;					//Ҫ���ĵ�ĸ���	
	public double mLatitude;	//��ǰ����
	public double mLongitude;	//��ǰά��
	public int mLocType;		//error code
	
	public int dNum;//��Ҫ���Ƶ�վ��ĸ���
	public List<Double> dSiteLatlLngList;//��Ҫ����վ��ľ�γ��������Ϣ
	
	/*���ߵ�ͼ*/
	private MKOfflineMap mOffline=null; 
	
	//������ͼ
	private MapView mmMapView;
	private AndroidToolsClass mAndroidToolsClass;
	private BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	private MapLocationListener mListener;
	
	/*����*/
	private static final String LTAG = MainActivity.class.getSimpleName();
//	private int textureId = -1;
//	private Bitmap bitmap; 
	private List<LatLng> latLngPolygon;
	private float[] vertexs;
	private FloatBuffer vertexBuffer;
	
	private HashMap<Integer, Integer> MarkMap = new HashMap<Integer, Integer>();

	/**
	 * @param appcontext
	 * @param mMapView
	 * @description ���캯��
	 */
	public BaiDuMapHelper(Context appcontext, MapView mMapView)
	{   
		mmMapView = mMapView;
		mAndroidToolsClass = new AndroidToolsClass(appcontext);
		//�������ߵ�ͼ����ʼ��
		SDKInitializer.initialize(appcontext);
		mOffline = new MKOfflineMap();
		//��ͼ��ʼ��
        mBaiduMap = mMapView.getMap();
        mListener = new MapLocationListener();
		mLocationClient = new LocationClient(appcontext);	//����LocationClient��	
		mLocationClient.registerLocationListener(mListener);//ע���������
		//����Ϊ���ǵ�ͼģʽ
		mBaiduMap.setMapType(mBaiduMap.MAP_TYPE_SATELLITE);
		//��ʼ����λ����
		InitLocation();
		//�������ߵ�ͼ��
		ImportOffLineMap();
		//���߳�ʼ��
		latLngPolygon = new ArrayList<LatLng>();
        mBaiduMap.setOnMapDrawFrameCallback(this);
		//bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.ground_overlay);//��������
        //ȥ���ٶ�logo
		mMapView.removeViewAt(1);
		//��ʼ�������Ӧ��mark
		IntMarkMap();
	}
	
	void IntMarkMap()
	{
		MarkMap.put(0, R.drawable.icon_marka);
		MarkMap.put(1, R.drawable.icon_markb);
		MarkMap.put(2, R.drawable.icon_markc);
		MarkMap.put(3, R.drawable.icon_markd);
		MarkMap.put(4, R.drawable.icon_marke);
		MarkMap.put(5, R.drawable.icon_markf);
		MarkMap.put(6, R.drawable.icon_markg);
		MarkMap.put(7, R.drawable.icon_markh);
		MarkMap.put(8, R.drawable.icon_marki);
		MarkMap.put(9, R.drawable.icon_markj);		
	}
	
	/**
	 * @return void
	 * @description ��ʼ��λ
	 */
	public void StartLocate()
	{
		mLocationClient.start();		
	}
	
	/**
	 * @return void
	 * @description ������λ
	 */
	public void StopLocate()
	{
		mLocationClient.stop();
	}
	
	/**
	 * @return void
	 * @description ��ʾ��ǰλ��
	 */
    public void ShowBusIcoLocation()
    {         	
        LatLng point = new LatLng(mLatitude, mLongitude);  
        //����Markerͼ��  
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.buslocation);  
        //����MarkerOption�������ڵ�ͼ������Marker  
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap); 
        //�ڵ�ͼ������Marker������ʾ  
        mBaiduMap.addOverlay(option);
                
		//�����ͼ״̬
		MapStatus mMapStatus = new MapStatus.Builder().target(point).build();
		//����MapStatusUpdate�����Ա�������ͼ״̬��Ҫ�����ı仯
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		//�ı��ͼ״̬
		mBaiduMap.setMapStatus(mMapStatusUpdate);			
    }
    
    
    /**
     * @return void
     * @description ��λ��ʼ��
     */
	private void InitLocation()
	{
		LocationClientOption option = new LocationClientOption();
		
		option.setLocationMode(LocationMode.Hight_Accuracy);//���ö�λģʽ
		option.setCoorType(BAIDU_DEFAULT);		//���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
//		option.setScanSpan(LOCATE_INTERVAL);	//���÷���λ����ļ��ʱ��Ϊ5000ms
		option.setIsNeedAddress(true);		//���صĶ�λ���������ַ��Ϣ
		option.setNeedDeviceDirect(true);	//���صĶ�λ��������ֻ���ͷ�ķ���
		mLocationClient.setLocOption(option);
		
		//��ʼ����ͼ����
		MapStatus mMapStatus = new MapStatus.Builder().zoom(MAP_SCALE).build();
		//����MapStatusUpdate�����Ա�������ͼ״̬��Ҫ�����ı仯
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		//�ı��ͼ״̬
		mBaiduMap.setMapStatus(mMapStatusUpdate);
	}
    
    
	/**
	 * ��λ����
	 * @author zlj
	 * @description ʵ��BDLocationListener�ӿ�,
	 * 				BDLocationListener�ӿ���2��������Ҫʵ�֣� 1.�����첽���صĶ�λ�����������BDLocation���Ͳ�����
	 */
    public class MapLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
			{
		        return ;
			}
			else
			{
				mLocType = location.getLocType();
				mLatitude = location.getLatitude();
				mLongitude = location.getLongitude();
			}
			
			ShowLocationMark();
		}
	}
    
    /**
     * ����
     */
    public void AllDestroy()
    {
    	mmMapView.onDestroy();
    	mOffline.destroy();
    }
    
    
    /**
     * �������ߵ�ͼ��
     */
    public void ImportOffLineMap() 
    {  
    	
    	mOffline.init(new MKOfflineMapListener(){
    		   @Override
    		   public void onGetOfflineMapState(int arg0, int arg1) {
    		    // TODO Auto-generated method stub
    		    
    		   }
    		   
    		  });
    	
        int num = mOffline.importOfflineData();  
        String msg = "";  
        if (num == 0) {   
            msg = "û�е������߰�������������߰�����λ�ò���ȷ�������߰��Ѿ��������";  
        } else {  
            msg = String.format("�ɹ����� %d �����߰������������ع����鿴!", num);  
        }  
        mAndroidToolsClass.ToastShow(msg);  
    }
    
    
    
    /**
     * ���ñ�ǩ
     * @param Lat
     * @param Lng
     * @param arg
     */
    void PlaceMark(double Lat, double Lng, int arg)
    {
        LatLng point = new LatLng(Lat, Lng);  
        //����Markerͼ��  
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(arg);  
        //����MarkerOption�������ڵ�ͼ������Marker  
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);  
        //�ڵ�ͼ������Marker������ʾ  
        mBaiduMap.addOverlay(option);	
    }
    
    /**
     *���ƹ���ͼ���·��
     */
    public void ShowLocationMark()
    {
    	mBaiduMap.clear();
    	ShowBusIcoLocation();
    	DrawLine();
    }
    
    /**
     * 
     * @param num
     * @param SiteLatlLngList
     */
    public void DrawLine()
    {
    	int i,j;
    	double latbuf;
    	double lngbuf;
    	double latoffset = 0;
    	double lngoffset = 0;
    	List<Double> LatBufList = new ArrayList<Double>();//��¼�Ѿ����ĵ�
    	
    	Point_Num = dNum;
    	
    //	if(Point_Num >0)
    	{
	    	latLngPolygon.clear();
	    	for(i=0; i<Point_Num; i++)
	    	{	
	    		latbuf = dSiteLatlLngList.get(i*2);//ȡ����γ��
	    		lngbuf = dSiteLatlLngList.get(i*2 + 1);
	    		
	    		LatLng latlng = new LatLng(latbuf, lngbuf);
	    		latLngPolygon.add(latlng);//����
	    		
	    		if(i < MARK_NUM)
	    		{	
	    			for(j=0; j<LatBufList.size(); j++)
	    			{
	    				//�Ѿ��ڸõ����ӹ�mark
	    				if((double)LatBufList.get(j) == latbuf)
	    				{	//����mark
	    					latoffset = 0.001;
	    					break;
	    				}
	    			}
	    			
	    			//�ڵ�ͼ������ͼ��
	    			PlaceMark(latlng.latitude + latoffset, latlng.longitude + lngoffset, MarkMap.get(i));
	    			latoffset = 0;
	    		}
	    		
	    		LatBufList.add(latbuf);
	    	}
    	}
    }
    
    
    
    
    
    
    
    /**
     * @description ��ͼ�ص�����
     */
    @Override
	public void onMapDrawFrame(GL10 gl, MapStatus drawingMapStatus) {
		if (mBaiduMap.getProjection() != null) {
			
			if(!latLngPolygon.isEmpty())
			{
				//�������ߵ� opengl ����
				calPolylinePoint(drawingMapStatus);
				//��������
				drawPolyline(gl, Color.argb(255, 255, 0, 0), vertexBuffer, 5, Point_Num, drawingMapStatus);
			}
			
			//drawTexture(gl, bitmap, drawingMapStatus);//��������
		}
	}
	
    /**
     * 
     * @param mspStatus
     * @description �������� OpenGL ����
     */
	public void calPolylinePoint(MapStatus mspStatus) {
		PointF[] polyPoints = new PointF[latLngPolygon.size()];
		vertexs = new float[3 * latLngPolygon.size()];
		int i = 0;
		for (LatLng xy : latLngPolygon) {
			// ����������ת���� openGL ����
			polyPoints[i] = mBaiduMap.getProjection().toOpenGLLocation(xy, mspStatus);
			vertexs[i * 3] = polyPoints[i].x;
			vertexs[i * 3 + 1] = polyPoints[i].y;
			vertexs[i * 3 + 2] = 0.0f;
			i++;
		}
		for (int j = 0; j < vertexs.length; j++) {
			Log.d(LTAG, "vertexs[" + j + "]: " + vertexs[j]);
		}
		vertexBuffer = makeFloatBuffer(vertexs);
	}
	
	/**
	 * 
	 * @param fs
	 * @return FloatBuffer
	 * @description ����OpenGL����ʱ�Ķ���Buffer
	 */
	private FloatBuffer makeFloatBuffer(float[] fs) {
		ByteBuffer bb = ByteBuffer.allocateDirect(fs.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(fs);
		fb.position(0);
		return fb;
	}

	/**
	 * 
	 * @param gl
	 * @param color
	 * @param lineVertexBuffer
	 * @param lineWidth
	 * @param pointSize
	 * @param drawingMapStatus
	 * @description ���ߺ���
	 */
	private void drawPolyline(GL10 gl, int color, FloatBuffer lineVertexBuffer,
			float lineWidth, int pointSize, MapStatus drawingMapStatus) {

		gl.glEnable(GL10.GL_BLEND);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		float colorA = Color.alpha(color) 	/ 255f;
		float colorR = Color.red(color) 	/ 255f;
		float colorG = Color.green(color) 	/ 255f;
		float colorB = Color.blue(color) 	/ 255f;

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVertexBuffer);
		gl.glColor4f(colorR, colorG, colorB, colorA);
		gl.glLineWidth(lineWidth);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, pointSize);

		gl.glDisable(GL10.GL_BLEND);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
    
    
    /** 
	 * @param gl
	 * @param bitmap
	 * @param drawingMapStatus
	 * @description ʹ��opengl������Ļ���
	 */
/*	public void drawTexture(GL10 gl, Bitmap bitmap, MapStatus drawingMapStatus) {
		PointF p1= mBaiduMap.getProjection().toOpenGLLocation(latlng2,drawingMapStatus);
		PointF p2= mBaiduMap.getProjection().toOpenGLLocation(latlng3,drawingMapStatus);
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 3 * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer vertices = byteBuffer.asFloatBuffer();
		vertices.put(new float[] { p1.x, p1.y, 0.0f, p2.x, p1.y, 0.0f, p1.x,
				p2.y, 0.0f, p2.x, p2.y, 0.0f });

		ByteBuffer indicesBuffer = ByteBuffer.allocateDirect(6 * 2);
		indicesBuffer.order(ByteOrder.nativeOrder());
		ShortBuffer indices = indicesBuffer.asShortBuffer();
		indices.put(new short[] { 0, 1, 2, 1, 2, 3 });

		ByteBuffer textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4);
		textureBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer texture = textureBuffer.asFloatBuffer();
		texture.put(new float[] { 0, 1f, 1f, 1f, 0f, 0f, 1f, 0f });

		indices.position(0);
		vertices.position(0);
		texture.position(0);

		// ��������
		if (textureId == -1) {
			int textureIds[] = new int[1];
			gl.glGenTextures(1, textureIds, 0);
			textureId = textureIds[0];
			Log.d(LTAG, "textureId: " + textureId);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_NEAREST);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		}
	
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		// ������ID
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture);

		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 6, GL10.GL_UNSIGNED_SHORT,
				indices);

		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
	}*/
    
}
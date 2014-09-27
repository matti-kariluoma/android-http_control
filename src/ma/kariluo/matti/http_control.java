package ma.kariluo.matti;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class http_control extends Activity implements ServiceListener
{
	private static final String TAG = "http_control";
	private static final String DISCOVER_SERVICE_TYPE = "_http._tcp.local.";
	// bugs ahoy https://code.google.com/p/android/issues/detail?id=35585
	// thanks https://gist.github.com/icastell/5704165
	// thanks http://stackoverflow.com/a/23854825
	private MulticastLock lock = null;
	private JmDNS jmdns = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupZeroconf();
	}
	
	private void setupZeroconf() 
	{
		Log.d(TAG, "Setting up Zeroconf...");
		Log.d(TAG, "Get WifiManager...");
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		Log.d(TAG, "Create lock...");
		lock = wifi.createMulticastLock(TAG);
		lock.setReferenceCounted(true);
		Log.i(TAG, "Acquiring Multicast lock...");
		lock.acquire();
		if (lock.isHeld())
		{
			Log.d(TAG, "Lock acquired!");
		}
		else
		{
			Log.e(TAG, "Couldn't acquire lock!");
			return;
		}
		
		Log.d(TAG, "Create jmdns...");
		try 
		{
			// Bug http://stackoverflow.com/a/13677686/1143172
			int intaddr = wifi.getConnectionInfo().getIpAddress();			 
			byte[] byteaddr = new byte[] { 
					(byte) (intaddr & 0xff), 
					(byte) (intaddr >> 8 & 0xff),
					(byte) (intaddr >> 16 & 0xff), 
					(byte) (intaddr >> 24 & 0xff) 
				};
			// Need to process UnknownHostException
			InetAddress addr = InetAddress.getByAddress(byteaddr);
			/*
			String ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
			ip = "0.0.0.0";
			InetAddress addr = InetAddress.getByName(ip);
			*/
			Log.d(TAG, "jmdns binding to ("+addr+")...");
			jmdns = JmDNS.create(addr, TAG);
			Log.d(TAG, "jmdns created!");
			Log.d(TAG, "Add listener...");
			jmdns.addServiceListener(DISCOVER_SERVICE_TYPE, http_control.this);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			jmdns = null;
		}
		Log.d(TAG, "Zeroconf started!");
	}
	
	@Override
	public void serviceResolved(ServiceEvent event) 
	{
		Log.d(TAG, "Service resolved: " + event.getInfo().getQualifiedName() 
				+ " port:" + event.getInfo().getPort()
				+ " domain:" + event.getInfo().getDomain()
			);
	}
	@Override
	public void serviceRemoved(ServiceEvent event)
	{
		Log.d(TAG, "Service removed: " + event.getName());
	}
	@Override
	public void serviceAdded(ServiceEvent event) 
	{
		Log.d(TAG, "Service added: " + event.getName());
		jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
	}
	
	protected void onResume() 
	{
		super.onResume();
	}
		 
	protected void onStop() 
	{
		super.onStop();
		if (jmdns != null)
		{
			jmdns.removeServiceListener(DISCOVER_SERVICE_TYPE, http_control.this);
			try
			{
				jmdns.close();
			} 
			catch (IOException e) 
			{
				Log.e(TAG, e.getMessage(), e);
			}
			jmdns = null;
		}
		if (lock != null && lock.isHeld())
		{
			Log.i(TAG, "Releasing Multicast lock...");
			lock.release();
			lock = null;
		}
	}
	
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	
}

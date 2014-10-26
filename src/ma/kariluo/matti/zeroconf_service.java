package ma.kariluo.matti;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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
import javax.jmdns.ServiceTypeListener;

public class zeroconf_service extends IntentService implements ServiceListener, ServiceTypeListener
{
	private static final String TAG = "zeroconf_service";
	private static final String HTTP_SERVICE_TYPE = "_http._tcp.local.";
	// bugs ahoy https://code.google.com/p/android/issues/detail?id=35585
	// thanks https://gist.github.com/icastell/5704165
	// thanks http://stackoverflow.com/a/23854825
	private MulticastLock lock = null;
	private JmDNS jmdns = null;
	private boolean isScanning = true;
	
	public zeroconf_service()
	{
		super(TAG);
	}
	@Override
	protected void onHandleIntent(Intent workIntent) 
	{
		String data = workIntent.getDataString();
		setupZeroconf();
		int i = 0;
		while(isScanning)
		{
			i += 1;
		}
		stopScan();
	}
	@Override
	public void serviceResolved(ServiceEvent event) 
	{
		Log.d(TAG, String.format("Service resolved: %s %s %s:%s",
				event.getType(),
				event.getName(),
				event.getInfo().getInetAddresses()[0],
				event.getInfo().getPort()
			));
		if (HTTP_SERVICE_TYPE.equals(event.getType()))
		{
			Log.i(TAG, String.format("Http Service resolved: %s:%s",
					event.getInfo().getInetAddresses()[0], 
					event.getInfo().getPort()
				));
			isScanning = false; // signal work done
		}
	}
	@Override
	public void serviceRemoved(ServiceEvent event)
	{
		Log.d(TAG, String.format("Service removed: %s", event.getName()));
	}
	@Override
	public void serviceAdded(ServiceEvent event) 
	{
		Log.d(TAG, String.format("Service added: %s %s", event.getType(), event.getName()));
		if (HTTP_SERVICE_TYPE.equals(event.getType()))
		{
			Log.i(TAG, String.format("Http Service found: %s", event.getName()));
			jmdns.requestServiceInfo(event.getType(), event.getName());
		}
	}
	@Override
	public void serviceTypeAdded(final ServiceEvent event)
	{
		Log.d(TAG, String.format("Service type added %s", event.getType()));
		jmdns.addServiceListener(event.getType(), this);
	}
	@Override
	public void subTypeForServiceTypeAdded(ServiceEvent event) 
	{
		Log.d(TAG, String.format("Service subtype added %s", event.getType()));
	}
	
	private void setupZeroconf() 
	{
		isScanning = true;
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
			Log.d(TAG, String.format("jmdns binding to %s", addr));
			jmdns = JmDNS.create(addr, TAG);
			Log.d(TAG, "jmdns created!");
			Log.d(TAG, "Add listener...");
			// thanks http://stackoverflow.com/a/18288491
			jmdns.addServiceTypeListener(zeroconf_service.this);
		} 
		catch (IOException e) 
		{
			Log.e(TAG, e.getMessage(), e);
			jmdns = null;
		}
		Log.d(TAG, "Zeroconf started!");
	}
	
	private void stopScan()
	{
		isScanning = false;
		if (jmdns != null)
		{
			Log.d(TAG, "Stopping Zeroconf...");
			jmdns.unregisterAllServices();
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
		Log.d(TAG, "Zeroconf stopped.");
	}
	
}

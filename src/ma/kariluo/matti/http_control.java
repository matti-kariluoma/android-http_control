package ma.kariluo.matti;

import android.app.Activity;
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

import ma.kariluo.matti.zeroconf_service;

public class http_control extends Activity
{
	private static final String TAG = "http_control";
	private static final String HTTP_SERVICE_TYPE = "_http._tcp.local.";
	// bugs ahoy https://code.google.com/p/android/issues/detail?id=35585
	// thanks https://gist.github.com/icastell/5704165
	// thanks http://stackoverflow.com/a/23854825
	private MulticastLock lock = null;
	private JmDNS jmdns = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	@Override 
	public void onStart()
	{
		Log.i(TAG, "Starting http_control...");
		super.onStart();
		setContentView(R.layout.main);
		Intent mIntent = new Intent(this, zeroconf_service.class);
		//mIntent.setData(Uri);
		this.startService(mIntent);
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	@Override
	protected void onStop() 
	{
		super.onStop();
	}
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
}

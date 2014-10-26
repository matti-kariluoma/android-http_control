package ma.kariluo.matti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import ma.kariluo.matti.zeroconf_service;

public class http_control extends Activity
{
	private static final String TAG = "http_control";
	
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

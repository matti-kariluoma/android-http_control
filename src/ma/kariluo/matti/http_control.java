package ma.kariluo.matti;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;
import java.util.ArrayList;

import ma.kariluo.matti.zeroconf_service;

public class http_control extends Activity implements OnItemClickListener
{
	private static final String TAG = "http_control";
	private ProgressBar scanProgress;
	private ImageButton scanButton;
	private ListView listView;
	private ArrayAdapter listViewAdapter;
	private List<String> servers;
	
	@Override // Activity
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	@Override // Activity
	public void onStart()
	{
		Log.i(TAG, "Starting http_control...");
		super.onStart();
		setContentView(R.layout.main);
		scanProgress = (ProgressBar) findViewById(R.id.scanProgress);
		configureScanButton();
		setScanningProgress(false);
		configureListView();
    configureIntentReceiver();
		startScanning();
	}
	@Override // Activity
	protected void onResume() 
	{
		super.onResume();
	}
	@Override // Activity
	protected void onStop() 
	{
		super.onStop();
	}
	@Override // Activity
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	@Override // OnItemClickListener
	public void onItemClick(AdapterView<?> l, View v, int position, long id)
	{
	}
	
	private void startScanning()
	{
		setScanningProgress(true);
		Intent mIntent = new Intent(this, zeroconf_service.class);
		//mIntent.setData(Uri);
		this.startService(mIntent);
	}
  
	private void setScanningProgress(boolean isScanning) 
	{
		if (isScanning) 
		{
			scanButton.setVisibility(View.GONE);
			scanProgress.setVisibility(View.VISIBLE);
		} 
		else 
		{
			scanButton.setVisibility(View.VISIBLE);
			scanProgress.setVisibility(View.GONE);
		}
	}
	
	private void addServer(String server)
	{
		if (!servers.contains(server))
		{
			servers.add(server);
			listViewAdapter.notifyDataSetChanged();
		}
	}
	
	private void configureScanButton() 
	{
		scanButton = (ImageButton) findViewById(R.id.actionButton);
		scanButton.setBackground(getResources().getDrawable(R.drawable.ic_menu_refresh));
		scanButton.setOnClickListener(
				new android.view.View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						startScanning();
					}
				}
			);
  }
	
	private void configureListView()
	{
		servers = new ArrayList<String>();
		listView = (ListView) findViewById(R.id.listView);
    listView.setOnItemClickListener(this);
    listViewAdapter = new ArrayAdapter(this, R.layout.row, servers);
    listView.setAdapter(listViewAdapter);
	}
	
	private void configureIntentReceiver()
	{
		IntentFilter statusIntentFilter = new IntentFilter(zeroconf_service.BROADCAST_ACTION);
		//statusIntentFilter.addDataScheme("http");
		BroadcastReceiver bReceiver = new zeroconf_receiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, statusIntentFilter);
	}
	
	private class zeroconf_receiver extends BroadcastReceiver
	{
		private zeroconf_receiver() 
		{
    }
    @Override
    public void onReceive(Context context, Intent intent) 
    {
			http_control.this.setScanningProgress(false);
			String server = String.format("http:/%s:%s", 
					intent.getStringExtra(zeroconf_service.EXTRA_HOST),
					intent.getStringExtra(zeroconf_service.EXTRA_PORT)
				);
			http_control.this.addServer(server);
    }
	}
}

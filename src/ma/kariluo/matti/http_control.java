package ma.kariluo.matti;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

import ma.kariluo.matti.zeroconf_service;

public class http_control extends Activity implements OnItemClickListener
{
	private static final String TAG = "http_control";
	private ProgressBar scanProgress;
	private ImageButton scanButton;
	private TextView noneFoundLabel;
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
		noneFoundLabel = (TextView) findViewById(R.id.noServersFoundLabel);
		configureScanButton();
		configureListView();
		configureIntentReceiver();
		setScanningProgress(false);
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
		String server = (String) l.getItemAtPosition(position);
		Log.d(TAG, String.format("Selected %s", server));
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(server));
		startActivity(browserIntent);
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
			noneFoundLabel.setVisibility(View.GONE);
			scanButton.setVisibility(View.GONE);
			scanProgress.setVisibility(View.VISIBLE);
		} 
		else 
		{
			if (servers.size() < 1)
			{
				noneFoundLabel.setVisibility(View.VISIBLE);
			}
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
		IntentFilter broadcastIntentFilter = new IntentFilter(zeroconf_service.BROADCAST_ACTION);
		IntentFilter doneScanningIntentFilter = new IntentFilter(zeroconf_service.SCANOVER_ACTION);
		//statusIntentFilter.addDataScheme("http");
		BroadcastReceiver bReceiver = new zeroconf_receiver();
		BroadcastReceiver sReceiver = new donescanning_receiver();
		LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
		broadcaster.registerReceiver(bReceiver, broadcastIntentFilter);
		broadcaster.registerReceiver(sReceiver, doneScanningIntentFilter);
	}
	
	private class zeroconf_receiver extends BroadcastReceiver
	{
		private zeroconf_receiver() 
		{
    }
    @Override
    public void onReceive(Context context, Intent intent) 
    {
			String server = String.format("http:/%s:%s/", 
					intent.getStringExtra(zeroconf_service.EXTRA_HOST),
					intent.getStringExtra(zeroconf_service.EXTRA_PORT)
				);
			http_control.this.addServer(server);
    }
	}
	private class donescanning_receiver extends BroadcastReceiver
	{
		private donescanning_receiver() 
		{
    }
    @Override
    public void onReceive(Context context, Intent intent) 
		{
			http_control.this.setScanningProgress(false);
    }
	}
}

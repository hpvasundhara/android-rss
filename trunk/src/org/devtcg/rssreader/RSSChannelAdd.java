/*
 * $Id$
 */

package org.devtcg.rssreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.devtcg.rssprovider.RSSReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ContentURI;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class RSSChannelAdd extends Activity
{
	public EditText mTitleText;
	public EditText mURLText;
	
	/* We need this to not block when accessing the RSS feed for validation
	 * and for name downloads. */
	protected ProgressDialog mBusy;	
	final Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.channel_add);
		
		//mTitleText = (EditText)findViewById(R.id.name);
		mURLText = (EditText)findViewById(R.id.url);
		
		Button add = (Button)findViewById(R.id.add);
		add.setOnClickListener(mAddListener);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		Bundle state = mURLText.saveState();

		Integer endpos = mURLText.length();
		state.putInteger("sel-start", endpos);
		state.putInteger("sel-end", endpos);
		
		mURLText.restoreState(state);
	}
	
	private OnClickListener mAddListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			final String rssurl = mURLText.getText().toString();

			mBusy = ProgressDialog.show(RSSChannelAdd.this,
			  "Downloading", "Accessing XML feed...", true, false);

			Thread t = new Thread()
			{
				public void run()
				{
					try
					{
						final long id = (new RSSChannelRefresh(getContentResolver())).
						  syncDB(null, -1, rssurl);
						
				    	mHandler.post(new Runnable() {
				    		public void run()
				    		{
				    			mBusy.dismiss();
				    		
				    			ContentURI uri = RSSReader.Channels.CONTENT_URI.addId(id);
				    			setResult(RESULT_OK, uri.toString());
				    			finish();
				    		}
				    	});
					}
					catch(Exception e)
					{
						final String errmsg = e.getMessage();
						final String errmsgFull = e.toString();

			    		mHandler.post(new Runnable() {
			    			public void run()
			    			{
			    				mBusy.dismiss();
			    				
			    				String errstr = ((errmsgFull != null) ? errmsgFull : errmsg);
			    				
			    				AlertDialog.show(RSSChannelAdd.this,
			    				  "Feed error", "An error was encountered while accessing the feed: " + errstr,
			    				  "OK", true);
			    			}
			    		});
					}			    	
				}
			};

			t.start();
		}
	};
}
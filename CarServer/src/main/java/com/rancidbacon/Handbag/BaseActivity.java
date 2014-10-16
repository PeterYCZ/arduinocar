package com.rancidbacon.Handbag;

import com.rancidbacon.Handbag.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BaseActivity extends HandbagActivity{

	private static final int UI_WIDGET_BUTTON = 0x00;
	private static final int UI_WIDGET_LABEL = 0x01;
	private static final int UI_WIDGET_TEXT_INPUT = 0x02;
	private static final int UI_WIDGET_DIALOG = 0x03;

	public static final byte COMMAND_GOT_EVENT = (byte) 0x80;
	public static final byte EVENT_BUTTON_CLICK = (byte) 0x01;
	public static final byte EVENT_TEXT_INPUT = (byte) 0x02;
	
	private static final int WIDGET_ID_OFFSET = 7200;
	
	private boolean preserveDisplayOnDisconnect = false;

	public BaseActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (usbHandler.getAccessory() != null) { // TODO: Handle better?
			showControls();
        } else {
            hideControls();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Preserve");
		menu.add("Quit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle() == "Preserve") {
			// We preserve the display so we can use ddms to take a screenshot.
			preserveDisplayOnDisconnect = !preserveDisplayOnDisconnect;
		} else if (item.getTitle() == "Quit") {
			finish();
			System.exit(0);
		}
		return true;
	}

	protected void enableControls(boolean enable) {
		if (enable) {
			showControls();
            CarRemoteControlThread x = new CarRemoteControlThread(this);
            x.start();
		} else {
			hideControls();
		}
	}

	protected void hideControls() {
		if (!preserveDisplayOnDisconnect) {
			setContentView(R.layout.no_device);
		}
	}

	
	void addButton(String labelText, final byte widgetId) {
				
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainstage);
		
		// TODO: Do something so we don't repeat this boilerplate everywhere.
		if (layout == null) { 
			// This is really an error but we do this and hope for the
			// best rather than generating a null pointer exception.
			return;
		}		
		
        Button button = new Button(this);
        button.setId(WIDGET_ID_OFFSET + widgetId);
        button.setText(labelText);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(BaseActivity.this, "Click " + widgetId, Toast.LENGTH_LONG).show();
            	sendCommand(COMMAND_GOT_EVENT, EVENT_BUTTON_CLICK, widgetId);
            }
        });        
        
        layout.addView(button);
	};
	
	
	void addTextInput(/* TODO: Add default text? */ final byte widgetId) {
		/*

		 */

		LinearLayout layout = (LinearLayout) findViewById(R.id.mainstage);

		// TODO: Do something so we don't repeat this boilerplate everywhere.
		if (layout == null) { 
			// This is really an error but we do this and hope for the
			// best rather than generating a null pointer exception.
			return;
		}		
		
        // TODO: Do a find by ID in the listener rather than make this final?
        final EditText textInput = new EditText(this);
        layout.addView(textInput);
        
        textInput.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
			            (keyCode == KeyEvent.KEYCODE_ENTER)) {
					
					sendCommand(COMMAND_GOT_EVENT, EVENT_TEXT_INPUT, widgetId);
					sendString(textInput.getText().toString());
					
					return true;
				}
				return false;
			}
        	
        });
	}
	
	void addLabel(String labelText, int fontSize, byte widgetAlignment, final byte widgetId) {
		/*
		 
		   This (now slightly misnamed) method either creates a new label or modifies an existing
		   label.
		   
		   It works for both labels and other widgets that subclass TextView. (e.g. Buttons)
		   
		 */
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainstage);
		
		// TODO: Do something so we don't repeat this boilerplate everywhere.
		if (layout == null) { 
			// This is really an error but we do this and hope for the
			// best rather than generating a null pointer exception.
			return;
		}
		
		// TODO: Check we actually got what we were looking for.
		TextView label = (TextView) layout.findViewById(WIDGET_ID_OFFSET + widgetId);
		
		if (label == null) {
			label = new TextView(this);
			label.setId(WIDGET_ID_OFFSET + widgetId);

			layout.addView(label);
		}

		label.setText(labelText);		
        
        if (fontSize > 0) {
        	label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, fontSize);
        }
        
        if (widgetAlignment > 0) {
        	label.setGravity(widgetAlignment);
        }

	}
	
	void showDialog(String labelText) {
		/*
		 */
		new AlertDialog.Builder(this).setMessage(labelText).show();
	}
	
	protected void showControls() {
		setContentView(R.layout.main);
	}

//    @Override
//    public void run() {
//        final int controlPort = 8000;
//
//        try {
//            ServerSocket server = new ServerSocket(controlPort);
//
//            while(true){
//                Socket remotePerson = server.accept();
//                CarRemoteControlHandler handler = new CarRemoteControlHandler(this, remotePerson);
//                handler.start();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    protected void handleConfigMessage(ConfigMsg c) {
		switch (c.getWidgetType()) {
			case UI_WIDGET_BUTTON:
				addButton(c.getWidgetText(), c.getWidgetId());
				break;
				
			case UI_WIDGET_LABEL:
				addLabel(c.getWidgetText(), c.getFontSize(), c.getWidgetAlignment(), c.getWidgetId());
				break;

			case UI_WIDGET_TEXT_INPUT:
				addTextInput(c.getWidgetId());
				break;	
			
			case UI_WIDGET_DIALOG:
				showDialog(c.getWidgetText());
				break;	
		} 
	}	
	
	protected void handleHandshakeMessage(HandshakeMsg h) {
		// TODO: Do this properly
		new AlertDialog.Builder(this).setMessage("This accessory is not compatible with this version of the Handbag App.").show();
	}
}
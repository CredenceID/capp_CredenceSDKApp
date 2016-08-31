package com.credenceid.sdkapp;

import java.io.ByteArrayInputStream;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.credenceid.biometrics.Biometrics;
import com.credenceid.biometrics.Biometrics.CardReaderStatusListner;
import com.credenceid.biometrics.Biometrics.CloseReasonCode;
import com.credenceid.biometrics.Biometrics.OnCardStatusListener;
import com.credenceid.biometrics.Biometrics.ResultCode;

public class NfcPage extends LinearLayout implements PageView {
	private static final String TAG = NfcPage.class.getName();

	private Biometrics mBiometrics;
	String cardReadDetailText;

	private TextView mCardDetailsTextView;
	private Button mOpenBtn;
	private Button mCloseBtn;
	private ImageView mPhotoView;
	Bitmap bm = null;



	private String fposToString(int pos) {
		switch (pos) {
		case 1:
			return "Right Thumb";
		case 2:
			return "Right Index";
		case 3:
			return "Right Middle";
		case 4:
			return "Right Ring";
		case 5:
			return "Right Little";
		case 6:
			return "Left Thumb";
		case 7:
			return "Left Index";
		case 8:
			return "left Middle";
		case 9:
			return "Left Ring";
		case 10:
			return "Left Little";
		default:
			return "";
		}
	}

	public NfcPage(Context context) {
		super(context);
		initialize();
	}

	public NfcPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public NfcPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	private void initialize() {
		Log.d(TAG, "initialize");
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		li.inflate(R.layout.page_nfc_reader, this, true);
		mOpenBtn = (Button) findViewById(R.id.open_id_btn);
		mOpenBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCapture(v);
			}
		});
		
		mCloseBtn = (Button) findViewById(R.id.close_id_btn);
		mCloseBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("Close Button Listener", "Close");
				mBiometrics.cardCloseCommand();
			}
		});

		mCardDetailsTextView = (TextView) findViewById(R.id.card_details);
		if (mCardDetailsTextView != null) {
			mCardDetailsTextView.setHorizontallyScrolling(false);
			mCardDetailsTextView.setMaxLines(Integer.MAX_VALUE);
		}
		mPhotoView = (ImageView) findViewById(R.id.photo_view);
		
		
			
		
	}

	public String dumpBytes(byte[] buffer) {
		byte[] HEX_CHAR = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		if (buffer == null) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("\nLength ");
		sb.append(Integer.toString(buffer.length));
		sb.append("\n");
		for (int i = 0; i < buffer.length; i++) {
			if (i != 1 && i % 16 == 1)
				sb.append("\n");
			sb.append("0x")
					.append((char) (HEX_CHAR[(buffer[i] & 0x00F0) >> 4]))
					.append((char) (HEX_CHAR[buffer[i] & 0x000F])).append(" ");
		}

		return sb.toString();
	}

	private void onCapture(View v) {
		Log.d(TAG, "OnCapture in NfcPage");
		mCardDetailsTextView.setText("");
		if (bm != null) {
			Log.d(TAG, "Bitmap is non null recycle it");
			bm.recycle(); // This should release bitmap.
		} else
			Log.d(TAG, "Bitmap is null no need to recycle");
		bm = null;

		disableOpenButton();
		mPhotoView.setImageResource(android.R.color.transparent);
		String tempStr;
		cardReadDetailText = "";

		mCardDetailsTextView.setText("Requesting Card Open");

		mBiometrics.cardOpenCommand(new CardReaderStatusListner() {
			
			@Override
			public void onCardReaderOpen(ResultCode arg0) {
				Log.d(TAG, "OnCardOpen:"+arg0.toString());
				if(arg0==ResultCode.OK)
				{
					mCardDetailsTextView.setText("Card Open Success.");
					disableOpenButton();
					enableCloseButton();
				}
				else
				{
					mCardDetailsTextView.setText("Card Open Error:"+arg0.toString());
					enableOpenButton();
					disableCloseButton();

				}

			}
					
			
			@Override
			public void onCardReaderClosed(CloseReasonCode arg0) {
				
				mCardDetailsTextView.setText("Card Closed:"+arg0.toString());
				disableCloseButton();
				enableOpenButton();
				
			}
		});	
	}
	
	private void doCardRead()
	{
		byte[] pin=null;
		if(mBiometrics.getProductName().equalsIgnoreCase("Credence One eKTP"))
		{
			pin = new String(
					"2015BB1218080000000000000000219661CFF281E0F1F921FE375C8C8D64FECA759173761A7859B52B3B4DEC036F41F6").getBytes();
		}
		Log.d("doRead", "REading");
		mBiometrics.ektpCardReadCommand(1, pin, new Biometrics.OnEktpCardReadListener() {
			
			@Override
			public void OnEktpCardRead(ResultCode result, String hint, byte[] data) {
			
				Log.d(TAG, "OnEktpCardRead: Result Code="+result);
				if (result == ResultCode.OK) 
				{	
					cardReadDetailText += "\n Read Completed";
				}
				else if (result == ResultCode.INTERMEDIATE)
				{
					cardReadDetailText +="\n "+hint;
					if(data!=null)
						cardReadDetailText +=" Data Length="+data.length;
					else
						cardReadDetailText +=" Data Null";
						
				}
				else
					cardReadDetailText +="\n"+hint;
				
				mCardDetailsTextView.setText(cardReadDetailText);				
			}
		}); 
	}

	

	@Override
	public String getTitle() {
		return getContext().getResources().getString(
				R.string.title_nfc_card_reader);
	}

	@Override
	public void activate(Biometrics biometrics) {
		mBiometrics = biometrics;

		doResume();
	}

	@Override
	public void doResume() {
		enableOpenButton();
		disableCloseButton();
		mBiometrics.registerCardStatusListener(new OnCardStatusListener() {
			@Override
			public void onCardStatusChange(String ATR, int prevState,
					int currentState) {
				if(currentState == 1)
				{
					Log.d("doResume", "Call doRead");
					mCardDetailsTextView.setText("");
					mCardDetailsTextView.setText("Card Present");
					cardReadDetailText="";
					doCardRead();

				}
				else if (currentState == 0)
				{
					mCardDetailsTextView.setText("");
					mCardDetailsTextView.setText("Card Absent");
				}
				
			}
		});
	}

	@Override
	public void deactivate() {

	}
	
	private void enableOpenButton() {
		mOpenBtn.setEnabled(true);
	}

	private void disableOpenButton() {
		mOpenBtn.setEnabled(false);
	}
	
	private void enableCloseButton() {
		mCloseBtn.setEnabled(true);
	}

	private void disableCloseButton() {
		mCloseBtn.setEnabled(false);
	}

	private void enableButtons() {
		mOpenBtn.setEnabled(true);
		mCloseBtn.setEnabled(true);
	}

	private void disableButtons() {
		mOpenBtn.setEnabled(false);
		mCloseBtn.setEnabled(false);
	}

	private void onLoad(View v) {
		disableButtons();	
	}

	public void DisplayBm() {
		if (mPhotoView != null) {
			mPhotoView.clearAnimation();
			mPhotoView.invalidate();
			if ( bm != null )
				mPhotoView.setImageBitmap(bm);
			else
				mPhotoView.setImageResource(R.drawable.ic_photoid);
		} else
			Log.d(TAG, "mPhotoView is null so not drawing bitmap");
	}
}

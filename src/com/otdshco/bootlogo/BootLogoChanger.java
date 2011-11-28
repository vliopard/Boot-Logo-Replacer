package com.otdshco.bootlogo;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import com.otdshco.bootlogo.R;
import com.otdshco.tools.FileOperations;
import com.otdshco.tools.Logger;
import com.otdshco.tools.Su;

public class BootLogoChanger extends
		ListActivity implements
					OnClickListener
{
	private final String		bootlogoSELECTED_ITEM_KEY	="selected_items";
	public final static String	bootlogoTEXT_KEY_1			="title";
	public final String			bootlogoTEXT_KEY_2			="description";
	public final String			bootlogoTEXT_KEY_P			="path";
	public final String			bootlogoITEM_ID				="id";
	public final String			bootlogoITEM_TYPE			="number";
	public final String			bootlogoIMG_KEY				="img";
	public final String			bootlogoRADIO_KEY			="radio";
	public final String			bootAnimDir					="bootanims";
	public final String			bootlogoPreviewFileName		="/preview.png";
	public final String			bootAnimFileName			="/bootanimation.zip";
	public final String			bootAnimDirectory			="/data/local";
	public final String			bootAnimSystemMedia			="/system/media";
	public final String			bootSoundFileName			="/Bootsound.mp3";
	public final String			bootSoundDirectory			="/system/media/audio/ui";
	private Integer				bootlogoSelectedItem		=-1;
	private RadioButton			bootlogoSelectedRadio;
	private RadioButton			bootlogoRadioSelection;
	private String				bootlogoSelectionPath;
	private String				bootlogoSelectionName;
	private TextView			bootlogoTextView;
	private Thread				bootlogoProcess;
	private FileOperations		fileOperation;

	private boolean isRoot()
	{
		return false;
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			fileOperation=new FileOperations();
		}
		catch(IOException ioe)
		{
			log("I/O Exception "+
				ioe);
		}
		setContentView(R.layout.bootlogo_main);
		List<Map<String,Object>> resourceNames=new ArrayList<Map<String,Object>>();
		int generatedData=generateData(resourceNames);
		MySingleAdapter notes=new MySingleAdapter(	this,
													resourceNames,
													R.layout.bootlogo_row,
													new String[]
													{
															bootlogoTEXT_KEY_1,
															bootlogoTEXT_KEY_2,
															bootlogoTEXT_KEY_P,
															bootlogoIMG_KEY,
															bootlogoRADIO_KEY,
															bootlogoITEM_ID
													},
													new int[]
													{
															R.id.text1,
															R.id.text2,
															R.id.text3,
															R.id.img1,
															R.id.radio
													});
		Button changeButton=(Button)findViewById(R.id.bootlogo_change);
		changeButton.setOnClickListener(this);
		bootlogoTextView=(TextView)findViewById(R.id.textv);
		setListAdapter(notes);
		printMessage("Total Screens: "+
						generatedData);
	}

	private final static Comparator<Map>	sdnc	=new Comparator<Map>()
														{
															private final Collator	collator	=Collator.getInstance();

															public int compare(	Map map1,
																				Map map2)
															{
																return collator.compare(map1.get(bootlogoTEXT_KEY_1),
																						map2.get(bootlogoTEXT_KEY_1));
															}
														};

	private int generateData(List<Map<String,Object>> resourceNames)
	{
		HashMap<String,Object> data;
		int index=0;
		try
		{
			String sdcard=fileOperation.getSDPath(bootAnimDir);
			File directory=new File(sdcard);
			for(File file : directory.listFiles())
			{
				if(file.isDirectory())
				{
					boolean animation=exist(file.getPath()+
											bootAnimFileName);
					boolean sound=exist(file.getPath()+
										bootSoundFileName);
					if(animation||
						sound)
					{
						data=new HashMap<String,Object>();
						data.put(	bootlogoITEM_ID,
									index);
						data.put(	bootlogoTEXT_KEY_1,
									file.getName());
						if(animation&&
							sound)
						{
							data.put(	bootlogoTEXT_KEY_2,
										"Animation and Audio");
						}
						else
						{
							if(animation)
							{
								data.put(	bootlogoTEXT_KEY_2,
											"Animation");
							}
							else
							{
								data.put(	bootlogoTEXT_KEY_2,
											"Audio");
							}
						}
						if(exist(file.getPath()+
									bootlogoPreviewFileName))
						{
							data.put(	bootlogoIMG_KEY,
										file.getPath()+
												bootlogoPreviewFileName);
							data.put(	bootlogoITEM_TYPE,
										0);
						}
						else
						{
							data.put(	bootlogoIMG_KEY,
										R.drawable.listiconsingle);
							data.put(	bootlogoITEM_TYPE,
										1);
						}
						data.put(	bootlogoRADIO_KEY,
									false);
						data.put(	bootlogoTEXT_KEY_P,
									file.getPath());
						resourceNames.add(data);
						index++;
					}
				}
			}
			Collections.sort(	resourceNames,
								sdnc);
			index=dirCount(directory);
		}
		catch(NullPointerException npe)
		{
			data=new HashMap<String,Object>();
			data.put(	bootlogoITEM_ID,
						index);
			data.put(	bootlogoTEXT_KEY_1,
						"No Boot Animation");
			data.put(	bootlogoTEXT_KEY_2,
						"Install at least one");
			data.put(	bootlogoIMG_KEY,
						R.drawable.listiconsingle);
			data.put(	bootlogoITEM_TYPE,
						1);
			data.put(	bootlogoRADIO_KEY,
						false);
			data.put(	bootlogoTEXT_KEY_P,
						"No_Bo?ot_Animation");
			resourceNames.add(data);
			toastMessage("No Boot Animation Found");
			index=0;
		}
		return index;
	}

	public void onClick(View v)
	{
		bootlogoRadioSelection=(RadioButton)v.findViewById(R.id.radio);
		Button buttonOk=(Button)v.findViewById(R.id.bootlogo_change);
		if(bootlogoRadioSelection!=null)
		{
			TextView textView1=(TextView)v.findViewById(R.id.text1);
			TextView textView3=(TextView)v.findViewById(R.id.text3);
			bootlogoSelectionName=textView1.getText()
											.toString();
			bootlogoSelectionPath=textView3.getText()
											.toString();
			boolean checked=bootlogoRadioSelection.isChecked();
			if(checked)
			{
				bootlogoSelectedRadio=null;
				bootlogoSelectedItem=-1;
				printMessage("No selection");
			}
			else
			{
				if(!bootlogoSelectedItem.equals(-1))
				{
					bootlogoSelectedRadio.setChecked(false);
				}
				bootlogoSelectedItem=v.getId();
				bootlogoSelectedRadio=bootlogoRadioSelection;
				printMessage(bootlogoSelectionName+
								" selected");
			}
			bootlogoRadioSelection.setChecked(!checked);
		}
		if(buttonOk!=null)
		{
			if(!bootlogoSelectedItem.equals(-1))
			{
				if(!bootlogoSelectionPath.equals("No_Bo?ot_Animation"))
				{
					printMessage("Installing "+
									bootlogoSelectionName+
									"...");
					if(exist(bootlogoSelectionPath+
								bootSoundFileName))
					{
						bootlogoProcess=new BootLogoThread(	bootlogoSelectionPath,
															bootSoundFileName,
															bootSoundDirectory,
															bootlogoSelectionName,
															isRoot());
						bootlogoProcess.start();
					}
					else
					{
						try
						{
							remove(bootSoundDirectory+
									bootSoundFileName);
						}
						catch(InterruptedException e)
						{
							printMessage("Can't remove");
						}
					}
					if(exist(bootlogoSelectionPath+
								bootAnimFileName))
					{
						bootlogoProcess=new BootLogoThread(	bootlogoSelectionPath,
															bootAnimFileName,
															bootAnimDirectory,
															bootlogoSelectionName,
															isRoot());
						bootlogoProcess.start();
					}
					else
					{
						try
						{
							remove(bootAnimDirectory+
									bootAnimFileName);
						}
						catch(InterruptedException e)
						{
							printMessage("Can't remove");
						}
					}
				}
				else
				{
					toastMessage("Add boot anim to SDCard");
					printMessage("No boot animation found");
				}
			}
			else
			{
				printMessage("No selection");
			}
		}
	}

	public void setSelectedRadio(RadioButton selectedRadio)
	{
		this.bootlogoSelectedRadio=selectedRadio;
	}

	@Override
	protected void onRestoreInstanceState(Bundle state)
	{
		super.onRestoreInstanceState(state);
		bootlogoSelectedItem=state.getInt(	bootlogoSELECTED_ITEM_KEY,
											-1);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(bootlogoSELECTED_ITEM_KEY,
						bootlogoSelectedItem);
	}

	public Integer getSelectedItem()
	{
		return bootlogoSelectedItem;
	}

	class MySingleAdapter extends
			SimpleAdapter
	{
		List<? extends Map<String,?>>	resourceNames;
		BootLogoChanger					context;
		String[]						strKeys;

		public MySingleAdapter(	BootLogoChanger context,
								List<? extends Map<String,?>> data,
								int resource,
								String[] from,
								int[] to)
		{
			super(	context,
					data,
					resource,
					from,
					to);
			this.context=context;
			resourceNames=data;
			strKeys=from;
		}

		@Override
		public View getView(int position,
							View convertView,
							ViewGroup parent)
		{
			ViewHolder holder;
			if(convertView==null)
			{
				holder=new ViewHolder();
				convertView=LayoutInflater.from(parent.getContext())
											.inflate(	R.layout.bootlogo_row,
														null);
				holder.textView1=(TextView)convertView.findViewById(R.id.text1);
				holder.textView2=(TextView)convertView.findViewById(R.id.text2);
				holder.textView3=(TextView)convertView.findViewById(R.id.text3);
				holder.imageView=(ImageView)convertView.findViewById(R.id.img1);
				holder.radioButton=(RadioButton)convertView.findViewById(R.id.radio);
				convertView.setTag(holder);
			}
			else
			{
				holder=(ViewHolder)convertView.getTag();
			}
			Map<String,?> currentData=resourceNames.get(position);
			holder.textView1.setText(currentData.get(context.bootlogoTEXT_KEY_1)
												.toString());
			holder.textView2.setText(currentData.get(context.bootlogoTEXT_KEY_2)
												.toString());
			holder.textView3.setText(currentData.get(context.bootlogoTEXT_KEY_P)
												.toString());
			Integer type=(Integer)currentData.get(context.bootlogoITEM_TYPE);
			switch(type)
			{
				case 0:
					Uri pcir=getPicture((String)currentData.get(context.bootlogoIMG_KEY));
					if(pcir!=null)
					{
						holder.imageView.setImageURI(pcir);
						break;
					}
				default:
					holder.imageView.setImageResource((Integer)currentData.get(context.bootlogoIMG_KEY));
			}
			holder.radioButton.setChecked(context.getSelectedItem()
													.equals((Integer)currentData.get(context.bootlogoITEM_ID)));
			if(holder.radioButton.isChecked())
			{
				context.setSelectedRadio(holder.radioButton);
			}
			convertView.setId((Integer)currentData.get(context.bootlogoITEM_ID));
			convertView.setOnClickListener(context);
			return convertView;
		}
	}

	private void toastMessage(String message)
	{
		Toast.makeText(	BootLogoChanger.this,
						message,
						Toast.LENGTH_SHORT)
				.show();
	}

	private void printMessage(String message)
	{
		bootlogoTextView.setText("[ "+
									message+
									" ]");
	}

	private Uri getPicture(String pictureFile)
	{
		if(exist(pictureFile))
		{
			return Uri.parse(pictureFile);
		}
		else
		{
			return null;
		}
	}

	private boolean exist(String fileName)
	{
		File file=new File(fileName);
		return file.exists();
	}

	private int dirCount(File directory)
	{
		int index=0;
		for(File file : directory.listFiles())
		{
			if(file.isDirectory()&&
				file.canRead()&&
				(exist(file.getPath()+
						bootAnimFileName)||(exist(file.getPath()+
													bootSoundFileName))))
			{
				index++;
			}
		}
		return index;
	}

	class ViewHolder
	{
		TextView	textView1, textView2, textView3;
		ImageView	imageView;
		RadioButton	radioButton;
	}

	private Runnable	handlerThread	=new Runnable()
											{
												public void run()
												{
													if((bootlogoProcess!=null))
													{
														bootlogoTextView.setText(((BootLogoThread)bootlogoProcess).get());
													}
													handler.postDelayed(this,
																		100);
												}
											};

	@Override
	protected void onStop()
	{
		super.onStop();
		handler.removeCallbacks(handlerThread);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		handler.removeCallbacks(handlerThread);
		handler.postDelayed(handlerThread,
							1000);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(handler!=null)
		{
			handler.removeCallbacks(handlerThread);
		}
		handler=null;
		if(bootlogoProcess!=null)
		{
			((BootLogoThread)bootlogoProcess).exit();
			bootlogoProcess=null;
		}
	}

	private void remove(String fileName) throws InterruptedException
	{
		try
		{
			Su su=new Su(isRoot());
			log("removing "+
				fileName);
			su._rm(fileName);
			su._exit();
		}
		catch(IOException ioe)
		{
			printMessage("I/O Exception from Local Remove");
		}
	}

	private Handler	handler	=new Handler();

	private void log(String logMessage)
	{
		Logger.log(	"BootLogo",
					logMessage);
	}
}

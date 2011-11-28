package com.otdshco.tools;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class Utilities
{
	private static final String	LOG_MAIN				="Utilities";
	private final static String	TEXT_KEY_1				="TK1";
	private final static String	TEXT_KEY_2				="TK2";
	private final static String	TEXT_KEY_3				="TK3";
	private final static String	ITEM_ID					="ID1";
	private final static String	IMG_KEY					="IMG";
	private final static int	FLAG_UPDATED_SYS_APP	=0x80;

	public Utilities()
	{
		log("Utilities Started...");
	}

	public static int generateData(	List<Map<String,Object>> resourceNames,
									int sortType,
									ArrayList<Drawable> drawableArray,
									ArrayList<String> applicationsArray,
									String cdir,
									Activity me)
	{
		resourceNames.clear();
		drawableArray.clear();
		applicationsArray.clear();
		ArrayList<String> applicationList=getAllFiles(FileOperations.getSDPath(cdir));
		Map<String,Object> data;
		for(int index=0; index<applicationList.size(); index++)
		{
			PackageProperties packageProperties=new PackageProperties(	applicationList.get(index),
																		me);
			data=new HashMap<String,Object>();
			data.put(	ITEM_ID,
						index);
			data.put(	TEXT_KEY_1,
						packageProperties.getPackage());
			data.put(	TEXT_KEY_2,
						packageProperties.getName()+
								" "+
								packageProperties.getVersion());
			data.put(	TEXT_KEY_3,
						applicationList.get(index));
			data.put(	IMG_KEY,
						index);
			applicationsArray.add(applicationList.get(index));
			drawableArray.add(Convert.rescale(	packageProperties.getIcon(),
												me.getWindowManager()));
			resourceNames.add(data);
		}
		sortList(	resourceNames,
					sortType);
		return applicationList.size();
	}

	public static int generateData(	List<Map<String,Object>> resourceNames,
									int sortType,
									ArrayList<Drawable> drawableArray,
									ArrayList<String> applicationsArray,
									Activity me)
	{
		resourceNames.clear();
		drawableArray.clear();
		applicationsArray.clear();
		ArrayList<ApplicationInfo> applicationList=getAllFiles(me);
		Map<String,Object> data;
		for(int index=0; index<applicationList.size(); index++)
		{
			PackageProperties packageProperties=new PackageProperties(	applicationList.get(index),
																		me);
			data=new HashMap<String,Object>();
			data.put(	ITEM_ID,
						index);
			data.put(	TEXT_KEY_1,
						packageProperties.getPackage());
			data.put(	TEXT_KEY_2,
						packageProperties.getName()+
								" "+
								packageProperties.getVersion());
			data.put(	TEXT_KEY_3,
						packageProperties.getSize()>0	?Convert.decimal(packageProperties.getSize())+
															" bytes"
														:"");
			data.put(	IMG_KEY,
						index);
			applicationsArray.add(packageProperties.getPackage());
			drawableArray.add(Convert.rescale(	packageProperties.getIcon(),
												me.getWindowManager()));
			resourceNames.add(data);
		}
		sortList(	resourceNames,
					sortType);
		return applicationList.size();
	}

	public static void sortList(List<Map<String,Object>> resourceNames,
								int sortType)
	{
		switch(sortType)
		{
			case 1:
				Collections.sort(	resourceNames,
									sortAppAsc);
			break;
			case 2:
				Collections.sort(	resourceNames,
									sortPakAsc);
			break;
			case 3:
				Collections.sort(	resourceNames,
									sortAppDesc);
			break;
			case 4:
				Collections.sort(	resourceNames,
									sortPakDesc);
			break;
			case 5:
				Collections.sort(	resourceNames,
									sortSizAsc);
			break;
			case 6:
				Collections.sort(	resourceNames,
									sortSizDesc);
			break;
		}
	}

	public static ArrayList<String> getAllFiles(String c_dir)
	{
		File dir=new File(c_dir);
		ArrayList<String> ret=new ArrayList<String>();
		try
		{
			for(File file : dir.listFiles())
			{
				if(file.isFile()&&
					(file.getAbsolutePath().endsWith(".apk")))
				{
					ret.add(file.getAbsolutePath());
				}
				else
				{
					if(file.isDirectory())
					{
						ArrayList<String> aux=getAllFiles(file.getAbsolutePath());
						for(int i=0; i<aux.size(); i++)
						{
							ret.add(aux.get(i));
						}
					}
				}
			}
		}
		catch(NullPointerException npe)
		{
			log("Null Pointer Exception "+
				npe);
		}
		return ret;
	}

	public static ArrayList<ApplicationInfo> getAllFiles(Activity me)
	{
		PackageManager mPackMag=me.getPackageManager();
		List<ApplicationInfo> all_apps=mPackMag.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		ArrayList<ApplicationInfo> ret=new ArrayList<ApplicationInfo>();
		for(ApplicationInfo appInfo : all_apps)
		{
			if((appInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0&&
				(appInfo.flags&FLAG_UPDATED_SYS_APP)==0&&
				(appInfo.flags!=0))
			{
				ret.add(appInfo);
			}
		}
		return ret;
	}

	public static int getOrder(	String types,
								String orders)
	{
		int type=Integer.valueOf(types);
		int order=Integer.valueOf(orders);
		switch(order)
		{
			case 1:
				switch(type)
				{
					case 1:
						log("APP ASC = TYPE["+
							types+
							"] ORDER["+
							orders+
							"] = RET[1]");
						return 1;
					case 2:
						log("PAK ASC = TYPE["+
							types+
							"] ORDER["+
							orders+
							"] = RET[2]");
						return 2;
					case 3:
						log("SIZ ASC = TYPE["+
							types+
							"] ORDER["+
							orders+
							"] = RET[5]");
						return 5;
				}
			case 2:
				switch(type)
				{
					case 1:
						log("APP DESC = TYPE["+
							types+
							"] ORDER["+
							orders+
							"] = RET[3]");
						return 3;
					case 2:
						log("PAK DESC = TYPE["+
							types+
							"] ORDER["+
							orders+
							"] = RET[4]");
						return 4;
					case 3:
						log("SIZ DESC = TYPE["+
							types+
							"] ORDER["+
							orders+
							"] = RET[6]");
						return 6;
				}
		}
		log("DEFAULT = TYPE["+
			types+
			"] ORDER["+
			orders+
			"] = RET[2]");
		return 2;
	}

	private final static Comparator<Map>	sortAppAsc	=new Comparator<Map>()
															{
																private final Collator	collator	=Collator.getInstance();

																public int compare(	Map map1,
																					Map map2)
																{
																	return collator.compare(map1.get(TEXT_KEY_2),
																							map2.get(TEXT_KEY_2));
																}
															};
	private final static Comparator<Map>	sortAppDesc	=new Comparator<Map>()
															{
																private final Collator	collator	=Collator.getInstance();

																public int compare(	Map map1,
																					Map map2)
																{
																	return collator.compare(map2.get(TEXT_KEY_2),
																							map1.get(TEXT_KEY_2));
																}
															};
	private final static Comparator<Map>	sortPakAsc	=new Comparator<Map>()
															{
																private final Collator	collator	=Collator.getInstance();

																public int compare(	Map map1,
																					Map map2)
																{
																	return collator.compare(map1.get(TEXT_KEY_1),
																							map2.get(TEXT_KEY_1));
																}
															};
	private final static Comparator<Map>	sortPakDesc	=new Comparator<Map>()
															{
																private final Collator	collator	=Collator.getInstance();

																public int compare(	Map map1,
																					Map map2)
																{
																	return collator.compare(map2.get(TEXT_KEY_1),
																							map1.get(TEXT_KEY_1));
																}
															};
	private final static Comparator<Map>	sortSizAsc	=new Comparator<Map>()
															{
																private final Collator	collator	=Collator.getInstance();

																public int compare(	Map map1,
																					Map map2)
																{
																	String x2=map2.get(TEXT_KEY_3)
																					.toString();
																	String x1=map1.get(TEXT_KEY_3)
																					.toString();
																	x1.replace(	".",
																				"");
																	x2.replace(	".",
																				"");
																	if(x2.length()>x1.length())
																	{
																		for(int i=0; i<x2.length()-
																						x1.length(); i++)
																			x1="0"+
																				x1;
																	}
																	else
																	{
																		if(x2.length()<x1.length())
																		{
																			for(int i=0; i<x1.length()-
																							x2.length(); i++)
																				x2="0"+
																					x2;
																		}
																	}
																	return collator.compare(x1,
																							x2);
																}
															};
	private final static Comparator<Map>	sortSizDesc	=new Comparator<Map>()
															{
																private final Collator	collator	=Collator.getInstance();

																public int compare(	Map map1,
																					Map map2)
																{
																	String x2=map2.get(TEXT_KEY_3)
																					.toString();
																	String x1=map1.get(TEXT_KEY_3)
																					.toString();
																	x1.replace(	".",
																				"");
																	x2.replace(	".",
																				"");
																	if(x2.length()>x1.length())
																	{
																		for(int i=0; i<x2.length()-
																						x1.length(); i++)
																			x1="0"+
																				x1;
																	}
																	else
																	{
																		if(x2.length()<x1.length())
																		{
																			for(int i=0; i<x1.length()-
																							x2.length(); i++)
																				x2="0"+
																					x2;
																		}
																	}
																	return collator.compare(x2,
																							x1);
																}
															};

	private static void log(String logMessage)
	{
		if(logMessage.startsWith(" ")||
			logMessage.startsWith("!"))
		{
			String clazz=Thread.currentThread()
								.getStackTrace()[3].getClassName();
			String metho=Thread.currentThread()
								.getStackTrace()[3].getMethodName();
			logMessage=logMessage+
						" ["+
						clazz.substring(clazz.lastIndexOf(".")+1)+
						"."+
						metho+
						"]";
		}
		Logger.log(	LOG_MAIN,
					logMessage,
					Logger.TOOLS_UTIL);
	}
}

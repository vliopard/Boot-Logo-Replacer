package com.otdshco.tools;
import java.io.IOException;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class PackageProperties
{
	private static final String	LOG_MAIN	="PackageProperties";
	private PackageManager		packageManager;
	private PackageInfo			packageInfo;
	private String				ls			="ls -l ";
	private String				app			="/data/app/";
	private String				priv		="/data/app-private/";
	private String				awk			="* | awk '{print$4}'";
	private Drawable			icon;

	public PackageProperties(	String packageFile,
								Activity main)
	{
		log("Package Properties - String ["+
			packageFile+
			"]");
		packageManager=main.getPackageManager();
		icon=main.getApplicationInfo()
					.loadIcon(packageManager);
		packageInfo=packageManager.getPackageArchiveInfo(	packageFile,
															PackageManager.GET_ACTIVITIES);
		packageInfo.applicationInfo.sourceDir=packageFile;
		packageInfo.applicationInfo.publicSourceDir=packageFile;
	}

	public PackageProperties(	ApplicationInfo packageFile,
								Activity main)
	{
		log("Package Properties - ApplicationInfo ["+
			packageFile.packageName+
			"]");
		packageManager=main.getPackageManager();
		try
		{
			packageInfo=packageManager.getPackageInfo(	packageFile.packageName,
														PackageManager.GET_ACTIVITIES);
		}
		catch(NameNotFoundException nnfe)
		{
			log("Name Not Found Exception: "+
				nnfe);
		}
	}

	public PackageManager getPackageManager()
	{
		return packageManager;
	}

	public PackageInfo getPackageInfo()
	{
		return packageInfo;
	}

	public String getVersion()
	{
		if((packageInfo!=null)&&
			(packageInfo.versionName!=null))
		{
			return packageInfo.versionName;
		}
		return "ERROR";
	}

	public String getPackage()
	{
		if((packageInfo!=null)&&
			(packageInfo.packageName!=null))
		{
			return packageInfo.packageName;
		}
		return "ERROR";
	}

	public String getName()
	{
		if((packageInfo!=null)&&
			(packageInfo.applicationInfo!=null)&&
			(packageManager!=null))
		{
			CharSequence packageLabel=packageManager.getApplicationLabel(packageInfo.applicationInfo);
			if(packageLabel!=null)
			{
				return packageLabel.toString();
			}
		}
		return "ERROR";
	}

	public int getSize()
	{
		int size=0;
		try
		{
			Su su=new Su(false);
			su.setTimeOut(1000);
			log("getSize ["+
				ls+
				app+
				getPackage()+
				awk);
			String restApp=su._run(ls+
									app+
									getPackage()+
									awk);
			log("getSize ["+
				ls+
				priv+
				getPackage()+
				awk);
			String restPriv=su._run(ls+
									priv+
									getPackage()+
									awk);
			su._exit();
			if(restApp!=null)
			{
				log("getSize ["+
					restApp.substring(restApp.lastIndexOf("/")+1)+
					"]");
				size=Integer.valueOf(restApp.substring(restApp.trim()
																.lastIndexOf("/")+1));
			}
			if(restPriv!=null)
			{
				log("getSize ["+
					restPriv.substring(restPriv.lastIndexOf("/")+1)+
					"]");
				size=size+
						Integer.valueOf(restPriv.substring(restPriv.trim()
																	.lastIndexOf("/")+1));
			}
			return size;
		}
		catch(IOException ioe)
		{
			log("getSize [IO Exception "+
				ioe+
				"]");
		}
		catch(InterruptedException ie)
		{
			log("getSize [Interrupted Exception "+
				ie+
				"]");
		}
		log("RETURNING -1");
		return -1;
	}

	public Drawable getIcon()
	{
		try
		{
			log("RETURN APPLICATION ICON");
			return packageManager.getApplicationIcon(packageInfo.applicationInfo);
		}
		catch(NullPointerException npe)
		{
			log("RETURN DEFAULT ICON");
			return icon;
		}
	}

	private void log(String logMessage)
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
					Logger.TOOLS_PACKAGE);
	}
}

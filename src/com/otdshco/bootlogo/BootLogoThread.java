package com.otdshco.bootlogo;
import java.io.File;
import java.io.IOException;
import com.otdshco.tools.Logger;
import com.otdshco.tools.Su;

public class BootLogoThread extends
		Thread
{
	private String	bootlogoMessageBuffer;
	private String	bootlogoIsRunning;
	private String	bootlogoSelectionPath;
	private String	bootlogoBootFileName;
	private String	bootlogoBootDirectory;
	private String	bootlogoSelectionName;
	private Su		su;
	private boolean	bootlogoRequestFromIntent	=false;

	public BootLogoThread(	String selPath,
							String bootFile,
							String bootDir,
							String selection,
							boolean reqFromIntent)
	{
		bootlogoSelectionPath=selPath;
		bootlogoBootFileName=bootFile;
		bootlogoBootDirectory=bootDir;
		bootlogoSelectionName=selection;
		bootlogoRequestFromIntent=reqFromIntent;
	}

	private void set(String text)
	{
		bootlogoMessageBuffer="[ "+
								text+
								" ]";
	}

	public String get()
	{
		return bootlogoMessageBuffer;
	}

	public String isWorking()
	{
		return bootlogoIsRunning;
	}

	public void run()
	{
		bootlogoIsRunning="WORKING";
		set("Installing "+
			bootlogoSelectionName+
			"...");
		try
		{
			if(0!=copy(	bootlogoSelectionPath,
						bootlogoBootFileName,
						bootlogoBootDirectory,
						bootlogoSelectionName))
			{
				set(bootlogoSelectionName+
					" Instalation failure");
			}
		}
		catch(IOException ioe)
		{
			set(bootlogoSelectionName+
				" failure I/O");
		}
		catch(InterruptedException ie)
		{
			set(bootlogoSelectionName+
				" failure I/E");
		}
		bootlogoIsRunning="DONE";
	}

	public int copy(String path,
					String file,
					String directory,
					String selection)	throws IOException,
										InterruptedException
	{
		int errorCode=0;
		su=new Su(bootlogoRequestFromIntent);
		log("BootLogoThread removing "+
			directory+
			file);
		errorCode=su._rm(directory+
							file);
		if((errorCode!=0)&&
			(errorCode!=3))
		{
			return errorCode;
		}
		errorCode=su._cp(	path+
									file,
							directory);
		if(errorCode!=0)
		{
			return errorCode;
		}
		errorCode=su._exit();
		if(errorCode!=0)
		{
			set("Root Access Denied");
			return errorCode;
		}
		if(exist(directory+
					file))
		{
			set("Successfuly Installed "+
				selection);
		}
		else
		{
			set(selection+
				" Installation Failure");
		}
		return errorCode;
	}

	public boolean exist(String fileName)
	{
		File file=new File(fileName);
		return file.exists();
	}

	public void exit()
	{
		try
		{
			su.stopWork();
			su._exit();
		}
		catch(InterruptedException ie)
		{
			log("EXIT Interrupted Exception");
		}
		catch(IOException ioe)
		{
			log("EXIT Input Output Exception");
		}
		catch(NullPointerException npe)
		{
			log("EXIT Null Pointer Exception");
		}
	}

	public void log(String message)
	{
		Logger.log(	message.substring(	0,
										message.indexOf(" ")),
					message.substring(message.indexOf(" ")+1));
	}
}

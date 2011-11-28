package com.otdshco.tools;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CheckStatus
{
	private static final String	LOG_MAIN	="CheckStatus";
	private String				tempDir		="/tmp/";
	private String				localProp	="/data/local.prop";
	private String				buildProp	="/system/build.prop";
	private String				defaultProp	="/system/default.prop";
	private String				homeAppAdj1	="ro.HOME_APP_ADJ=1";
	private String				initUmtsRc	="/system/etc/rootfs/init.mapphone_umts.rc";
	private String				updateMode	="write /sys/devices/omapdss/display0/update_mode 1";
	private String				home0txt	=tempDir+
												"home0.txt";
	private String				home1txt	=tempDir+
												"home1.txt";
	private String				home2txt	=tempDir+
												"home2.txt";
	private String				dsiTxt		=tempDir+
												"dsi.txt";

	public boolean checkHome()	throws IOException,
								InterruptedException
	{
		log("checkHome");
		return checkEnabled(home0txt,
							localProp,
							homeAppAdj1)||
				checkEnabled(	null,
								localProp,
								homeAppAdj1)||
				checkEnabled(	home1txt,
								defaultProp,
								homeAppAdj1)||
				checkEnabled(	null,
								defaultProp,
								homeAppAdj1)||
				checkEnabled(	home2txt,
								buildProp,
								homeAppAdj1)||
				checkEnabled(	null,
								buildProp,
								homeAppAdj1);
	}

	public boolean checkDsi()	throws IOException,
								InterruptedException
	{
		log("checkDsi");
		return checkEnabled(dsiTxt,
							initUmtsRc,
							updateMode)||
				checkEnabled(	null,
								initUmtsRc,
								updateMode);
	}

	public boolean checkInstalled()	throws IOException,
									InterruptedException
	{
		Su su=new Su(false);
		String rest=su._run("lsmod | grep -i dsifix");
		log("checkInstalled ["+
			rest+
			"]");
		// "imageshack.u s/photo/my-images/600/snap20110620144707.png"
		if((rest!=null)&&
			(rest.contains("imageshack.u s/photo/my-images/600/")))
		{
			log("checkInstalled [TRUE]");
			return true;
		}
		log("checkInstalled [FALSE]");
		return false;
	}

	private boolean checkEnabled(	String toFileName,
									String fromFileName,
									String pattern)	throws IOException,
													InterruptedException
	{
		log("checkEnabled");
		File file;
		if(toFileName!=null)
		{
			Su su=new Su(false);
			if(0!=su._cp(	fromFileName,
							toFileName))
			{
				log("checkEnabled [FALSE]");
				return false;
			}
			if(0!=su._exit())
			{
				log("checkEnabled [FALSE]");
				return false;
			}
			file=new File(toFileName);
		}
		else
		{
			file=new File(fromFileName);
		}
		DataInputStream dsi=new DataInputStream(new FileInputStream(file));
		String lineReader="";
		do
		{
			lineReader=dsi.readLine();
			if((lineReader!=null)&&
				(lineReader.contains(pattern)))
			{
				log("checkEnabled [TRUE]");
				return true;
			}
		}
		while(lineReader!=null);
		log("checkEnabled [FALSE]");
		return false;
	}

	public double getTotal(String dir)	throws IOException,
										InterruptedException
	{
		log("getTotal ["+
			dir+
			"]");
		Su su=new Su(false);
		double ret=su._df(	dir,
							"total",
							false);
		su._exit();
		log("getTotal [RETURN: "+
			ret+
			"]");
		return ret;
	}

	public double getUsed(String dir)	throws IOException,
										InterruptedException
	{
		log("getUsed ["+
			dir+
			"]");
		Su su=new Su(false);
		double ret=su._df(	dir,
							"used",
							false);
		su._exit();
		log("getUsed [RETURN: "+
			ret+
			"]");
		return ret;
	}

	public double getFree(String dir)	throws IOException,
										InterruptedException
	{
		log("getFree ["+
			dir+
			"]");
		Su su=new Su(false);
		double ret=su._df(	dir,
							"available",
							false);
		su._exit();
		log("getFree [RETURN: "+
			ret+
			"]");
		return ret;
	}

	public double getUsedP(String dir)	throws IOException,
										InterruptedException
	{
		log("getUsedP ["+
			dir+
			"]");
		Su su=new Su(false);
		double ret=su._df(	dir,
							"used",
							true);
		su._exit();
		log("getUsedP [RETURN: "+
			ret+
			"]");
		return ret;
	}

	public double getFreeP(String dir)	throws IOException,
										InterruptedException
	{
		log("getFreeP ["+
			dir+
			"]");
		Su su=new Su(false);
		double ret=su._df(	dir,
							"available",
							true);
		su._exit();
		log("getFreeP [RETURN: "+
			ret+
			"]");
		return ret;
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
					Logger.TOOLS_STATUS);
	}
}

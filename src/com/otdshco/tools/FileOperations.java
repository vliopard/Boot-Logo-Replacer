package com.otdshco.tools;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.os.Environment;

public class FileOperations
{
	private static final String	LOG_MAIN	="FileOperations";

	public FileOperations() throws IOException
	{
		log("FileOperations Started...");
	}

	public boolean fileExist(String fileName)
	{
		fileName=fileName.trim();
		log("fileExist ["+
			fileName+
			"]");
		File file;
		if(fileName.endsWith("/*"))
		{
			log("fileExist [end with *]");
			file=new File(fileName.substring(	0,
												fileName.length()-2));
		}
		else
		{
			log("fileExist [not end with *]");
			file=new File(fileName);
		}
		log("fileExist ["+
			file.exists()+
			"]");
		log("fileExist isFile ["+
			file.isFile()+
			"]");
		log("fileExist isDirectory ["+
			file.isDirectory()+
			"]");
		log("fileExist isHidden ["+
			file.isHidden()+
			"]");
		log("fileExist canRead ["+
			file.canRead()+
			"]");
		log("fileExist canWrite ["+
			file.canWrite()+
			"]");
		File[] files=file.listFiles();
		if(files!=null)
		{
			log("fileExist filesLenght ["+
				files.length+
				"]");
		}
		return file.exists();
	}

	public boolean isFiles(String fileName)
	{
		log("isFiles "+
			fileName);
		if(fileName.endsWith("/*"))
		{
			return true;
		}
		return false;
	}

	public boolean isFile(String fileName)
	{
		log("isFile "+
			fileName);
		if(fileName.endsWith("/*"))
		{
			return false;
		}
		else
		{
			File file=new File(fileName);
			if(file.exists())
			{
				if(file.isFile())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
	}

	public boolean isDir(String fileName)
	{
		log("isDir "+
			fileName);
		if(fileName.endsWith("/*"))
		{
			return false;
		}
		else
		{
			File file=new File(fileName);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
	}

	public static String getSD()
	{
		File sdCardPath=Environment.getExternalStorageDirectory();
		return sdCardPath.getAbsolutePath();
	}

	public static String getSDPath(String dir)
	{
		File directory=new File(getSD(),
								dir);
		return directory.getAbsolutePath();
	}

	public static DataInputStream readFile(String fileName) throws FileNotFoundException
	{
		log("readFile ["+
			fileName+
			"]");
		File file=new File(fileName);
		if(file.exists())
		{
			log("readFile ["+
				fileName+
				" exist]");
			if(file.isFile())
			{
				log("readFile ["+
					fileName+
					" is file]");
				if(file.canRead())
				{
					log("readFile ["+
						fileName+
						" can be read]");
					FileInputStream fis=new FileInputStream(file);
					DataInputStream dis=new DataInputStream(fis);
					return dis;
				}
			}
		}
		log("readFile [can't read "+
			fileName+
			"]");
		return null;
	}

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
					Logger.TOOLS_FILE);
	}
}

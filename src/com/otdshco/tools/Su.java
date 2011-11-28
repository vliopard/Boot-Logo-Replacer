package com.otdshco.tools;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Su
{
	private static final String	LOG_MAIN						="SuperUser";
	private int					msec							=200;
	private long				timeout							=10000;
	private boolean				notimeout						=false;
	private Process				suProcess;
	private DataOutputStream	outputStream;
	private String				message							="";
	private FileOperations		fileOperation;
	private boolean				massiveCopyEnabled				=false;
	private boolean				working							=true;
	public static int			CODE_OK							=0;
	public static int			CODE_ERROR						=1;
	public static int			CODE_EMPTY						=2;
	public static int			CODE_NOT_FOUND					=3;
	public static int			CODE_FROM_FILE_UNREADABLE		=4;
	public static int			CODE_FROM_FILE_NOT_EXIST		=5;
	public static int			CODE_FROM_DIR_NOT_EXIST			=6;
	public static int			CODE_FROM_FILE_IS_DIR			=7;
	public static int			CODE_FROM_IS_NOT_DIR			=8;
	public static int			CODE_PARENT_FILE_NOT_EXIST		=9;
	public static int			CODE_PARENT_FILE_IS_DIR			=10;
	public static int			CODE_PARENT_FILE_NOT_WRITABLE	=11;
	public static int			CODE_TO_FILE_NOT_WRITABLE		=12;
	public static int			CODE_TARGET_DIR_NOT_EXIST		=13;
	public static int			CODE_TARGET_IS_NOT_DIR			=14;
	public static String[]		CODE_LIST						=
																{
			"Ok", // 0
			"Error", // 1
			"Empty", // 2
			"Not found", // 3
			"Source file unreadable", // 4
			"Source file does not exist", // 5
			"Source directory does not exist ", // 6
			"Source file is directory", // 7
			"Source file is not a directory", // 8
			"Parent file not exist", // 9
			"Parent file is directory", // 10
			"Parent file is not writable", // 11
			"Target file is not writable", // 12
			"Target directory does not exist", // 13
			"Target is not a directory" // 14
																};

	public Su(boolean mce) throws IOException
	{
		fileOperation=new FileOperations();
		massiveCopyEnabled=mce;
		suProcess=Runtime.getRuntime()
							.exec("su");
		outputStream=new DataOutputStream(suProcess.getOutputStream());
	}

	public void setTime(int time)
	{
		msec=time;
	}

	public void setTimeOut(int time)
	{
		timeout=time;
	}

	private void timeOutOff()
	{
		notimeout=true;
	}

	private void timeOutOn()
	{
		notimeout=false;
	}

	private boolean isTimedOut()
	{
		return !notimeout;
	}

	private void setWorking(boolean job)
	{
		log("setWorking ["+
			job+
			"]");
		working=job;
	}

	public void startWork()
	{
		setWorking(true);
	}

	public void stopWork()
	{
		log("stopWork");
		setWorking(false);
	}

	public boolean isWorking()
	{
		log("isWorking ["+
			working+
			"]");
		return working;
	}

	private long getTime()
	{
		Date date=new Date();
		return date.getTime();
	}

	private ArrayList<String> getFileContent(	String fileName,
												String pattern) throws IOException
	{
		log("getFileContent "+
			fileName+
			" PATTERN ["+
			pattern+
			"]");
		ArrayList<String> arrayList=new ArrayList<String>();
		DataInputStream fileReader=FileOperations.readFile(fileName);
		if(fileReader!=null)
		{
			String lineReader="";
			// long start=timeOutStart();
			do
			{
				lineReader=fileReader.readLine();
				if(lineReader!=null)
				{
					arrayList.add(pattern.trim()+
									"/"+
									lineReader.trim());
				}
			}
			while((lineReader!=null)&&
					(isWorking())/*
								 * &&
								 * (!timeOut(start))
								 */);
		}
		return arrayList;
	}

	private long timeOutStart()
	{
		long start=getTime();
		log("timeOutStart ["+
			start+
			"]");
		return start;
	}

	private boolean timeOutStop(long start)
	{
		if(isTimedOut())
		{
			long current=getTime();
			log("timeOut [DIFF "+
				((current-start)/1000)+
				"]");
			if(current>(start+timeout))
			{
				log("timeOut [REACHED, STOPPING...]");
				stopWork();
				return true;
			}
			log("timeOut [NOT REACHED: FALSE...]");
		}
		return false;
	}

	private boolean timeOutBreak(long start)
	{
		long current=getTime();
		log("timeOut [DIFF "+
			((current-start)/1000)+
			"]");
		if(current>(start+timeout))
		{
			log("timeOut [REACHED, STOPPING...]");
			return true;
		}
		log("timeOut [NOT REACHED: FALSE...]");
		return false;
	}

	private boolean isFileAvailable(String fileName,
									String pattern)	throws IOException,
													InterruptedException
	{
		log("isFileAvailable "+
			fileName+
			" pattern["+
			pattern+
			"]");
		DataInputStream availableFile=FileOperations.readFile(fileName);
		if(availableFile!=null)
		{
			log("isFileAvailable ["+
				fileName+
				" is AVAILABLE]");
			String lineReader="";
			long start=timeOutStart();
			do
			{
				lineReader=availableFile.readLine();
				log("isFileAvailable [LINE READ: "+
					lineReader+
					"]");
				if((lineReader!=null)&&
					(lineReader.equals(pattern)))
				{
					log("isFileAvailable ["+
						lineReader+
						"] MATCHES ["+
						pattern+
						"] RET[TRUE]");
					return true;
				}
			}
			while((lineReader!=null)&&
					(isWorking())&&
					(!timeOutBreak(start)));
		}
		log("isFileAvailable ["+
			fileName+
			" is NOT AVAILABLE] RET[FALSE]");
		return false;
	}

	private void waitForFile(String pattern) throws IOException,
											InterruptedException
	{
		log("waitForFile ["+
			pattern+
			"]");
		flushOutputStream();
		long start=timeOutStart();
		while((!isFileAvailable("/tmp/available.tmp",
								pattern))&&
				(isWorking())&&
				(!timeOutStop(start)))
		{
			Thread.sleep(msec);
		}
		outputStream.writeBytes("rm /tmp/available.tmp\n");
		flushOutputStream();
	}

	private int getError(String pattern) throws IOException,
										InterruptedException
	{
		log("getError ["+
			pattern+
			"]");
		waitForFile(pattern);
		ArrayList<String> errorList=getFileContent(	"/tmp/otdshco.tmp",
													pattern);
		if(errorList.size()>0)
		{
			log("getError [ERROR EMPTY]");
			return CODE_EMPTY;
		}
		log("getError [ERROR OK]");
		return CODE_OK;
	}

	private ArrayList<String> readFileContent(	String pattern1,
												String pattern2) throws IOException,
																InterruptedException
	{
		log("readFileContent pattern1["+
			pattern1+
			"] pattern2["+
			pattern2+
			"]");
		log("readFileContent [WAITING FOR FILE]");
		waitForFile(pattern2);
		log("readFileContent [GET FILE CONTENT]");
		ArrayList<String> fileContent=getFileContent(	"/tmp/otdshco.tmp",
														pattern1);
		if(fileContent.size()>0)
		{
			log("readFileContent [OK]");
			return fileContent;
		}
		log("readFileContent [null]");
		return null;
	}

	public int _mkdir(String directory)	throws IOException,
										InterruptedException
	{
		log("_mkdir ["+
			directory+
			"]");
		if(!fileOperation.fileExist(directory))
		{
			return suCommand("mkdir "+
								directory);
		}
		return CODE_OK;
	}

	public int _rm(String fileName)	throws IOException,
									InterruptedException
	{
		log("_rm ["+
			fileName+
			"]");
		if(!fileOperation.fileExist(fileName))
		{
			log("_rm file not found");
			return CODE_NOT_FOUND;
		}
		if(fileOperation.isFile(fileName)||
			fileOperation.isFiles(fileName))
		{
			log("_rm file(s)");
			return suCommand("rm "+
								fileName);
		}
		if(fileOperation.isDir(fileName))
		{
			log("_rm dir");
			return suCommand("rm -r "+
								fileName);
		}
		log("_rm error");
		return CODE_ERROR;
	}

	public int _install(String fileName) throws IOException,
										InterruptedException
	{
		log("_install ["+
			fileName+
			"]");
		if(!fileOperation.fileExist(fileName))
		{
			log("_install file not found");
			return CODE_NOT_FOUND;
		}
		if(fileOperation.isFile(fileName))
		{
			log("_install file: ["+
				fileName+
				"]");
			return suCommand("pm install \'"+
								fileName+
								"\'");
		}
		log("_install error");
		return CODE_ERROR;
	}

	public int _uninstall(String fileName)	throws IOException,
											InterruptedException
	{
		log("_uninstall ["+
			fileName+
			"]");
		return suCommand("pm uninstall \'"+
							fileName+
							"\'");
	}

	public int _mv(	String sourceFile,
					String targetFile)	throws IOException,
										InterruptedException
	{
		log("_mv ["+
			sourceFile+
			"] to ["+
			targetFile+
			"]");
		if(fileOperation.fileExist(sourceFile))
		{
			return suCommand("mv "+
								sourceFile+
								" "+
								targetFile);
		}
		return CODE_NOT_FOUND;
	}

	public int _exit()	throws InterruptedException,
						IOException
	{
		log("_exit");
		stopWork();
		end01_exitShell();
		end02_waitSuProcess();
		if(end03_suExitValue())
		{
			end04_closeOutputStream();
			end05_endSuProcess();
			return CODE_OK;
		}
		return CODE_ERROR;
	}

	private void end01_exitShell() throws IOException
	{
		log("end01_exitShell");
		outputStream.writeBytes("exit\n");
		flushOutputStream();
	}

	private void flushOutputStream() throws IOException
	{
		log("flushOutputStream");
		outputStream.flush();
	}

	private void end04_closeOutputStream() throws IOException
	{
		log("end04_closeOutputStream");
		if(outputStream!=null)
		{
			outputStream.close();
			outputStream=null;
		}
	}

	private void end02_waitSuProcess() throws InterruptedException
	{
		log("end02_waitSuProcess");
		suProcess.waitFor();
	}

	private void end05_endSuProcess()
	{
		log("end05_endSuProcess");
		if(suProcess!=null)
		{
			suProcess.destroy();
			suProcess=null;
		}
	}

	private boolean end03_suExitValue()
	{
		log("end03_suExitValue");
		return suProcess.exitValue()!=255;
	}

	private int suCommand(String command)	throws IOException,
											InterruptedException
	{
		log("suCommand ["+
			command+
			"]");
		String pattern="err"+
						command;
		runCommand(	command,
					pattern);
		return getError(pattern);
	}

	private ArrayList<String> runList(String command)	throws IOException,
														InterruptedException
	{
		log("runList ["+
			command+
			"]");
		if(command.trim()
					.contains(" "))
		{
			String comm=command.substring(	0,
											command.indexOf(" "));
			String param=command.substring(command.indexOf(" "));
			log("runList C1 ["+
				comm+
				param+
				"]");
			runCommand(	comm+
								param,
						param);
			return readFileContent(	param,
									param);
		}
		else
		{
			log("runList C2 ["+
				command+
				"]");
			runCommand(	command,
						"my"+
								command+
								"ResultList");
			return readFileContent(	"my"+
											command+
											"ResultList",
									"my"+
											command+
											"ResultList");
		}
	}

	private void runCommand(String command,
							String pattern) throws IOException
	{
		String patt=new String(pattern.replace(	"$",
												"\\$"));
		log("runCommand ["+
			command+
			"] pattern ["+
			patt+
			"]");
		log("runCommand [rm /tmp/available.tmp; "+
			command+
			" > /tmp/otdshco.tmp 2>&1; echo \""+
			patt+
			"\" > /tmp/available.tmp]");
		outputStream.writeBytes("rm /tmp/available.tmp; "+
								command+
								" > /tmp/otdshco.tmp 2>&1; echo \""+
								patt+
								"\" > /tmp/available.tmp\n");
		log("runCommand [DONE]");
	}

	public int _cp(	String fromFileName,
					String toFileName)	throws IOException,
										NullPointerException,
										InterruptedException
	{
		log("_cp ["+
			fromFileName+
			"] to ["+
			toFileName+
			"]");
		int errorCode=0;
		String fileName;
		if(fromFileName.endsWith("/*")&&
			(!massiveCopyEnabled))
		{
			log("_cp [end with *] listing dir...");
			log("_cp ["+
				"ls ."+
				fromFileName.substring(	0,
										fromFileName.length()-2)+
				"]");
			ArrayList<String> listResult=runList("ls "+
													fromFileName.substring(	0,
																			fromFileName.length()-2));
			if(listResult!=null)
			{
				int totSize=listResult.size();
				log("========================================");
				log("========================================");
				log("========================================");
				log("_cp [end with *] result list SIZE["+
					totSize+
					"]");
				for(int i=0; i<totSize; i++)
				{
					fileName=listResult.get(i);
					log("_cp [FILENAME "+
						fileName+
						"]");
					if(fileOperation.fileExist(fileName))
					{
						log("_cp [FILENAME "+
							fileName+
							" EXIST]");
						addMessage(	fileName,
									i,
									totSize);
						log("_cp [COPY "+
							fileName+
							" TO "+
							toFileName+
							"/"+
							fileName.substring(fileName.lastIndexOf("/")+1)+
							"]");
						errorCode=copy(	fileName,
										toFileName+
												"/"+
												fileName.substring(fileName.lastIndexOf("/")+1));
						if(errorCode!=0)
						{
							return errorCode;
						}
					}
				}
			}
			else
			{
				log("_cp [end with *] result list is empty");
				return CODE_EMPTY;
			}
		}
		else
		{
			log("_cp [copy "+
				fromFileName+
				" TO "+
				toFileName+
				"]");
			return copy(fromFileName,
						toFileName);
		}
		return errorCode;
	}

	public int _size(String command) throws IOException,
									InterruptedException
	{
		log("_size ["+
			command+
			"]");
		runCommand(	"ls "+
							command+
							" | wc -l",
					command);
		ArrayList<String> fileContent=readFileContent(	command,
														command);
		if((fileContent!=null)&&
			(fileContent.size()>0))
		{
			try
			{
				return Integer.valueOf(fileContent.get(0)
													.substring(fileContent.get(0)
																			.lastIndexOf("/")+1)
													.trim());
			}
			catch(NumberFormatException nfe)
			{
				return -1;
			}
		}
		return 0;
	}

	public int _size(	String c_dir,
						String ext)
	{
		File dir=new File(c_dir);
		int ret=0;
		for(File file : dir.listFiles())
		{
			if(file.isFile()&&
				(file.getAbsolutePath().endsWith(ext)))
			{
				ret++;
			}
			else
			{
				if(file.isDirectory())
				{
					int aux=_size(	file.getAbsolutePath(),
									ext);
					ret=ret+
						aux;
				}
			}
		}
		return ret;
	}

	public ArrayList<String> _ls(String path)	throws IOException,
												InterruptedException
	{
		log("_ls ["+
			path+
			"]");
		return runList("ls "+
						path);
	}

	public String _run(String cmd)	throws IOException,
									InterruptedException
	{
		ArrayList<String> rest=runList(cmd);
		if((rest!=null)&&
			(rest.size()>0))
		{
			return rest.get(0);
		}
		return null;
	}

	public double _df(	String dir,
						String usage,
						boolean percent) throws IOException,
										InterruptedException
	{
		log("_df [START]");
		int total=0;
		int used=0;
		int available=0;
		ArrayList<String> list=runList("df");
		log("_df [AFTER RUNLIST]");
		if(list!=null)
		{
			int totSize=list.size();
			log("_df [TOTAL SIZE "+
				totSize+
				"]");
			for(int i=0; i<totSize; i++)
			{
				String res=list.get(i);
				if(res.contains(dir))
				{
					log("_df "+
						i+
						" DIR["+
						dir+
						"] RES["+
						res+
						"]");
					log("_df [:"+
						res.indexOf(":")+
						"]");
					log("_df [ktot "+
						res.indexOf("K total,")+
						"]");
					total=Integer.valueOf(res.substring(res.indexOf(":")+2,
														res.indexOf("K total,"))
												.trim());
					used=Integer.valueOf(res.substring(	res.indexOf("K total,")+9,
														res.indexOf("K used,"))
											.trim());
					available=Integer.valueOf(res.substring(res.indexOf("K used,")+8,
															res.indexOf("K available"))
													.trim());
				}
			}
			if(!percent)
			{
				if(usage.equals("total"))
				{
					return total;
				}
				if(usage.equals("used"))
				{
					return used;
				}
				if(usage.equals("available"))
				{
					return available;
				}
				return -1;
			}
			else
			{
				if(usage.equals("total"))
				{
					return 100.0;
				}
				if(usage.equals("used"))
				{
					return used*
							100.0/
							(total!=0	?total
										:1);
				}
				if(usage.equals("available"))
				{
					return available*
							100.0/
							(total!=0	?total
										:1);
				}
				return -1;
			}
		}
		return -1;
	}

	private int copy(	String fromFileName,
						String toFileName)	throws IOException,
											InterruptedException
	{
		fromFileName=fromFileName.trim();
		log("copy ["+
			fromFileName+
			"] to ["+
			toFileName+
			"]");
		if(!fromFileName.endsWith("/*"))
		{
			log("copy [ENDS WITH *]");
			File fromFile=new File(fromFileName);
			if(!fromFile.exists())
			{
				return CODE_FROM_FILE_NOT_EXIST;
			}
			if(!fromFile.isFile())
			{
				return CODE_FROM_FILE_IS_DIR;
			}
			/*
			 * if(!fromFile.canRead())
			 * {
			 * return CODE_FROM_FILE_UNREADABLE;
			 * }
			 */
			File toFile=new File(toFileName);
			if(!toFile.exists())
			/*
			 * {
			 * if(!toFile.canWrite())
			 * {
			 * return CODE_TO_FILE_NOT_WRITABLE;
			 * }
			 * }
			 * else
			 */
			{
				String parentName=toFile.getParent();
				if(parentName==null)
				{
					parentName=System.getProperty("user.dir");
				}
				File parentFileName=new File(parentName);
				if(!parentFileName.exists())
				{
					return CODE_PARENT_FILE_NOT_EXIST;
				}
				if(parentFileName.isFile())
				{
					return CODE_PARENT_FILE_IS_DIR;
				}
				/*
				 * if(!parentFileName.canWrite())
				 * {
				 * return CODE_PARENT_FILE_NOT_WRITABLE;
				 * }
				 */
			}
			if(toFile.isDirectory())
			{
				toFile=new File(toFile+
								fromFile.getName());
			}
		}
		else
		{
			log("copy [IS A FILE]");
			File fromFile=new File(fromFileName.substring(	0,
															fromFileName.length()-2));
			if(!fromFile.exists())
			{
				return CODE_FROM_DIR_NOT_EXIST;
			}
			if(!fromFile.isDirectory())
			{
				return CODE_FROM_IS_NOT_DIR;
			}
			File toFile=new File(toFileName);
			if(!toFile.exists())
			{
				return CODE_TARGET_DIR_NOT_EXIST;
			}
			if(!toFile.isDirectory())
			{
				return CODE_TARGET_IS_NOT_DIR;
			}
		}
		log("copy =================================");
		log("copy =================================");
		log("copy =================================");
		log("copy [cp "+
			fromFileName+
			" "+
			toFileName+
			"]");
		timeOutOff();
		int ret=suCommand("cp "+
							fromFileName+
							" "+
							toFileName);
		timeOutOn();
		return ret;
	}

	private void addMessage(String status,
							int idx,
							int tsze)
	{
		idx++;
		message=message+
				"\n"+
				status+
				" ["+
				idx+
				"/"+
				tsze+
				"]";
	}

	public String getMessage()
	{
		String returnMessage=new String(message);
		String returnText=returnMessage.replaceAll(	"\n\n",
													"");
		message="";
		return returnText;
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
					Logger.TOOLS_SUPERUSER);
	}
}

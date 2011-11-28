package com.otdshco.tools;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Convert
{
	private static final String	LOG_MAIN	="Convert";

	public Convert()
	{
		log("Convert [CREATED]");
	}

	public static String decimal(double num)
	{
		DecimalFormat df=new DecimalFormat();
		DecimalFormatSymbols dfs=new DecimalFormatSymbols();
		dfs.setGroupingSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		return df.format(num);
	}

	public static String percent(double num)
	{
		DecimalFormat df=new DecimalFormat("######.##");
		DecimalFormatSymbols dfs=new DecimalFormatSymbols();
		dfs.setGroupingSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		return df.format(num);
	}

	public static Drawable rescale(	Drawable pic,
									WindowManager wm)
	{
		DisplayMetrics metrics=new DisplayMetrics();
		wm.getDefaultDisplay()
			.getMetrics(metrics);
		int scal;
		switch(metrics.densityDpi)
		{
			case DisplayMetrics.DENSITY_HIGH:
				scal=72;
			break;
			case DisplayMetrics.DENSITY_MEDIUM:
				scal=48;
			break;
			case DisplayMetrics.DENSITY_LOW:
				scal=36;
			break;
			default:
				scal=80;
		}
		Bitmap bitmap=((BitmapDrawable)pic).getBitmap();
		// if (!((bitmap.getHeight()>scal)||(bitmap.getWidth()>scal)))
		// {
		// return pic;
		// }
		Bitmap scaledImage=Bitmap.createScaledBitmap(	bitmap,
														scal,
														scal,
														true);
		Drawable pic2=new BitmapDrawable(scaledImage);
		return pic2;
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
					Logger.TOOLS_CONVERT);
	}
}

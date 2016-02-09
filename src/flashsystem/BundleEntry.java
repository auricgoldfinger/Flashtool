package flashsystem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.system.OS;

public class BundleEntry {

	private File fileentry = null;
	private JarFile jarfile = null;
	private JarEntry jarentry = null;
	private String _category = "";
	private String _internal = "";

	private static Logger logger = Logger.getLogger(BundleEntry.class);

	private String getExtension() {
		if (fileentry!=null) {
			int extpos = fileentry.getName().lastIndexOf(".");
			if (extpos > -1) {
				return fileentry.getName().substring(extpos);
			}
			return "";
		}
		else {
			int extpos = jarentry.getName().lastIndexOf(".");
			if (extpos > -1) {
				return jarentry.getName().substring(extpos);
			}
			return "";
		}
	}
	
	public BundleEntry(File f) {
		fileentry = f;
		_category = BundleEntry.getShortName(fileentry.getName()).toUpperCase();
		_internal = org.sinfile.parsers.SinFile.getShortName(fileentry.getName())+getExtension();
	}

	public BundleEntry(JarFile jf, JarEntry j) {
		jarentry = j;
		jarfile = jf;
		_category = BundleEntry.getShortName(jarentry.getName()).toUpperCase();
		_internal = org.sinfile.parsers.SinFile.getShortName(jarentry.getName())+getExtension();
	}

	public InputStream getInputStream() throws FileNotFoundException, IOException {
		if (fileentry!=null) {
			logger.info("Streaming from file : "+fileentry.getPath());
			return new FileInputStream(fileentry);
		}
		else {
			logger.debug("Streaming from jar entry : "+jarentry.getName());
			return jarfile.getInputStream(jarentry);
		}
	}

	public String getName() {
		if (this.isJarEntry()) return jarentry.getName();
		return fileentry.getName();
	}

	public String getInternal() {
		return _internal;
	}

	public String getAbsolutePath() {
		return fileentry.getAbsolutePath();
	}

	public boolean isJarEntry() {
		return jarentry!=null;
	}

	public String getMD5() {
		if (fileentry!=null) return OS.getMD5(fileentry);
		else return "";
	}

	public long getSize() {
		if (fileentry!=null) return fileentry.length();
		else return jarentry.getSize();
	}

	public String getCategory() {
		return _category;
	}

	public String getFolder() {
		return new File(getAbsolutePath()).getParent();
	}

	public static String getShortName(String pname) {
		String name = pname;
		int extpos = name.lastIndexOf(".");
		if (name.toUpperCase().endsWith(".TA")) {
			if (extpos!=-1)
				name = name.substring(0,extpos);
			return name;
		}
		if (name.indexOf("_AID")!=-1)
			name = name.substring(0, name.indexOf("_AID"));
		if (name.indexOf("_PLATFORM")!=-1)
			name = name.substring(0, name.indexOf("_PLATFORM"));
		if (name.indexOf("_S1")!=-1)
			name = name.substring(0, name.indexOf("_S1"));
		if (name.startsWith("elabel"))
			name = "elabel";
		if (name.indexOf("-")!=-1)
			name = name.substring(0, name.indexOf("-"));
		extpos = name.lastIndexOf(".");
		if (extpos!=-1) {
			name = name.substring(0,extpos);
		}
		return name;
	}

	public void saveTo(String folder) {
		try {
			if (isJarEntry()) {
				logger.debug("Saving entry "+getName()+" to disk");
				InputStream in = getInputStream();
				String outname = folder+File.separator+getName();
				if (outname.endsWith("tab") || outname.endsWith("sinb")) outname = outname.substring(0, outname.length()-1);
				fileentry=new File(outname);
				new File(outname).getParentFile().mkdirs();
				logger.debug("Writing Entry to "+outname);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(outname));
				byte[] buffer = new byte[10240];
				int len;
				while((len = in.read(buffer)) >= 0)
					out.write(buffer, 0, len);
				in.close();
				out.close();
			}
		}
		catch (Exception e) {}
	}

}
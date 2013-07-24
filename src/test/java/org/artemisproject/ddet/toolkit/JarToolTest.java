package org.artemisproject.ddet.toolkit;

import java.io.File;
import java.io.IOException;

public class JarToolTest {
	
	public static void main(String args[]) throws IOException {

//		String jarname = "D:\\dacapo-9.12-bach.jar";
//		String filename ="D:\\dacapo-9.12-bach";
//		String jarname = "D:\\test\\source\\daytrader.zip";
//		String jarname = "E:\\rebeforetest\\dacapo-9.12-bach\\dat\\daytrader.zip";
//		String filename ="E:\\rebeforetest\\daytrader";
//		String jarname = "E:\\rebeforetest\\dt-ejb.jar";
//		String filename ="E:\\rebeforetest\\dt-ejb";
		String filename ="D:\\test\\ex18\\dt-ejb";
		String jarname = "D:\\test\\ex18\\dt-ejb.jar";
//		String filename ="D:\\test\\source\\dt-ejb";
		
		UnjarTool f = new UnjarTool();
		f.unjar(jarname,filename);
		
	 JarTool jarHelper = new JarTool();
		
//		String jarname = "d:\\test\\dacapo-9.12-bach.jar";
//		String filename ="d:\\test\\dacapo-9.12-bach";

//		String jarname = "D:\\test\\dt-ejb.jar";
//		String filename ="D:\\test\\dt-ejb";
//		String jarname = "D:\\test\\ex18\\daytrader.zip";
//		String filename ="D:\\test\\ex18\\daytrader";
//		String jarname = "D:\\test\\ex18\\dt-ejb.jar";
//		String filename ="D:\\test\\ex18\\dt-ejb";
		String jarname1 = "D:\\test\\ex18\\changed\\dacapo-9.12-bach.jar";
		String filename1 ="D:\\test\\ex18\\dacapo-9.12-bach";
//		File dirOrFile2Jar = new File("D:\\test\\dacapo.jar");
//		File jar2Dir = new File("D:\\test\\dacapo");
		File dirOrFile2Jar = new File(jarname1);
		File jar2Dir = new File(filename1);
		jar2Dir.mkdir();
		jarHelper.jarDir(jar2Dir, dirOrFile2Jar);
	}
	

}

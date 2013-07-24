package org.artemisproject.ddet.toolkit;

import static org.objectweb.asm.Opcodes.ASM4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Logger;

import org.artemisproject.transformer.DyanTransformer;
import org.artemisproject.transformer.QuieTransformer;
import org.artemisproject.transformer.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;


public class Processor {

	private final static Logger LOGGER = Logger.getLogger(Processor.class
			.getName());

	public static Logger getLogger() {
		return LOGGER;
	}

	/**
	 * "conup" or "exp", choice to decide the way to generate transaction id, must be specified
	 */
	private String projectType;
	
	/**
	 * quiescence, tranquility and version-consistency, must be specified
	 */
	private String dynamicCriteria;
	

	private String ddmFullyQuaName = "org/apache/geronimo/samples/daytrader/dacapo/LDDM";

	/**
	 * for conup, or ejb projects, e.g., "Lcn/edu/nju/moon/conup/spi/datamodel/ConupTransaction;"
	 */
	private String conupTxAnnation = "";
	
	/**
	 * for conup, or ejb projects, get neighbor components, e.g., "Lorg/oasisopen/sca/annotation/Reference;"
	 */
	private String referenceAnnatation = "";

	/**
	 * for transactions and neighbor components specified by users
	 */

	private Set<String> transToAnalyze = null;
	private Set<String> neiComs = null;

	public Processor(String criteria, String projectType, String ddmFullyQuaName) {
		this.projectType = projectType;
		this.ddmFullyQuaName = ddmFullyQuaName;
		this.dynamicCriteria  = criteria;
	}

	public Processor(String criteria, String projectType) {
		this.projectType = projectType;
		this.dynamicCriteria  = criteria;
	}

	public Processor(String criteria, String projectType, Set<String> methods,
			Set<String> neiComs, String ddmFullyQuaName) {
		this.dynamicCriteria  = criteria;
		this.projectType = projectType;
		this.transToAnalyze = methods;
		this.neiComs = neiComs;
		this.ddmFullyQuaName = ddmFullyQuaName;
	}

	public Processor(String criteria, String projectType, String conupTxAnnation,
			String referenceAnnatation, String ddmFullyQuaName) {
		this.projectType = projectType;
		this.conupTxAnnation = conupTxAnnation;
		this.referenceAnnatation = referenceAnnatation;
		this.ddmFullyQuaName = ddmFullyQuaName;
		this.dynamicCriteria  = criteria;
	}

	public void setTransToAnaalyze(Set<String> methods) {
		transToAnalyze = methods;
	}

	public void setNeighborComponents(Set<String> coms) {
		neiComs = coms;
	}

	public void setConupTxAnnatation(String ctra) {
		conupTxAnnation = ctra;
	}

	public void setReferenceAnnatation(String ra) {
		referenceAnnatation = ra;
	}

	public void setDdmFullyQuaName(String dfqn) {
		ddmFullyQuaName = dfqn;
	}

	/**
	 * find and analyze class files recursively
	 * 
	 * @param tempFile
	 * @param conupTx
	 */
	public void QuieAnalyzeFile(File tempFile) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				QuieAnalyzeFile(file[i]);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".class")) {
					LOGGER.info("Analyze file:" + tempFile.getName());
					FileInputStream input = new FileInputStream(
							tempFile.getAbsolutePath());
					ClassReader cr = new ClassReader(input);
					ClassNode cn = new ClassNode(ASM4);
					cr.accept(cn, 0);
					LOGGER.info("Need analyze file:" + tempFile.getName());
					
					QuieTransformer qtran = new QuieTransformer(cn,
							projectType, ddmFullyQuaName);
					qtran.setTxAnnotationDesc(conupTxAnnation);
					qtran.setMethodToAnalyze(transToAnalyze);
					qtran.transform();

					ClassWriter cw = new TranClassWriter(
							ClassWriter.COMPUTE_FRAMES);
					cn.accept(cw);
					byte[] b = cw.toByteArray();
					FileOutputStream fout = new FileOutputStream(new File(
							tempFile.getAbsolutePath()));
					fout.write(b);
					fout.close();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * find and analyze class files recursively
	 * 
	 * @param tempFile
	 * @param conupTx
	 */
	public void DynaAnalyzeFile(File tempFile) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				QuieAnalyzeFile(file[i]);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".class")) {
					LOGGER.info("Analyze file:" + tempFile.getName());
					FileInputStream input = new FileInputStream(
							tempFile.getAbsolutePath());
					ClassReader cr = new ClassReader(input);
					ClassNode cn = new ClassNode(ASM4);
					cr.accept(cn, 0);
					LOGGER.info("Need analyze file:" + tempFile.getName());
					
					DyanTransformer dtran = DyanTransformer.getInstance(cn, projectType, ddmFullyQuaName);
					dtran.setTxAnnotationDesc(conupTxAnnation);
					dtran.setMethodToAnalyze(transToAnalyze);
					dtran.setAllServices(neiComs);
					dtran.setMethodToAnalyze(transToAnalyze);
					dtran.transform();
					
					ClassWriter cw = new TranClassWriter(
							ClassWriter.COMPUTE_FRAMES);
					cn.accept(cw);
					byte[] b = cw.toByteArray();
					FileOutputStream fout = new FileOutputStream(new File(
							tempFile.getAbsolutePath()));
					fout.write(b);
					fout.close();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * for unit test, analyze the transactions specified and the neighbor
	 * components are giver
	 * 
	 * @param tempFile
	 * @param methodToAnalyze
	 * @param neiComs
	 * @param outPath
	 */
	public void DynaAnalyzeFile(File tempFile, String outPath) {
		if (tempFile.isDirectory()) {
			File file[] = tempFile.listFiles();
			for (int i = 0; i < file.length; i++) {
				DynaAnalyzeFile(file[i], outPath);
			}
		} else {
			try {
				if (tempFile.getName().endsWith(".class")) {
					LOGGER.info("Analyze file:" + tempFile.getName());
					FileInputStream input = new FileInputStream(
							tempFile.getAbsolutePath());
					ClassReader cr = new ClassReader(input);
					ClassNode cn = new ClassNode(ASM4);
					cr.accept(cn, 0);

					DyanTransformer dtran = DyanTransformer.getInstance(cn, projectType, ddmFullyQuaName);
					dtran.setTxAnnotationDesc(conupTxAnnation);
					dtran.setNeiComAnnatation(referenceAnnatation);
					dtran.setAllServices(neiComs);
					dtran.setMethodToAnalyze(transToAnalyze);
//					System.out.println(dtran.getAllServices());
					dtran.transform();
					
					ClassWriter cw = new TranClassWriter(
							ClassWriter.COMPUTE_FRAMES);
					cn.accept(cw);
					byte[] b = cw.toByteArray();
					FileOutputStream fout = new FileOutputStream(new File(
							outPath + File.separator + tempFile.getName()));
					// FileOutputStream fout = new FileOutputStream(new
					// File(outPath));
					fout.write(b);
					fout.close();

				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}
	/**
	 * for unit test, analyze the transactions specified and the neighbor
	 * components are giver
	 * 
	 * @param tempFile
	 * @param methodToAnalyze
	 * @param neiComs
	 * @param outPath
	 */
//	public void AnalyzeFile(File tempFile) {
//		if (tempFile.isDirectory()) {
//			File file[] = tempFile.listFiles();
//			for (int i = 0; i < file.length; i++) {
//				AnalyzeFile(file[i]);
//			}
//		} else {
//			try {
//				if (tempFile.getName().endsWith(".class")) {
//					LOGGER.info("Analyze file:" + tempFile.getName());
//					FileInputStream input = new FileInputStream(
//							tempFile.getAbsolutePath());					
//					ClassReader cr = new ClassReader(input);
//					ClassNode cn = new ClassNode(ASM4);					
//					cr.accept(cn, 0);
//					DyanTransformer.getInstance(cn).transform(cn, methodToAnalyze,
//							neiComs, "exp", ddmFullyQuaName);
//					ClassWriter cw = new TranClassWriter(
//							ClassWriter.COMPUTE_FRAMES);
//					cn.accept(cw);
//					byte[] b = cw.toByteArray();
//					FileOutputStream fout = new FileOutputStream(new File(
//							tempFile.getAbsolutePath()));
//					fout.write(b);
//					fout.close();
//
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//
//			}
//		}
//
//	}

	/**
	 * @param file
	 *            file path
	 */
	public static void showBytecode(String file) {
		FileInputStream is;
		ClassReader cr;
		try {
			is = new FileInputStream(file);
			cr = new ClassReader(is);
			TraceClassVisitor trace = new TraceClassVisitor(new PrintWriter(
					System.out));
			cr.accept(trace, ClassReader.EXPAND_FRAMES);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * analyze a uncompressed file
	 * 
	 * @param projectLocation
	 */
	public void analyzeSource(String projectLocation) {
		String input = projectLocation;
		File tempFile = new File(input);
		tempFile.mkdir();
		if (dynamicCriteria.equals("quiescence")) {
			QuieAnalyzeFile(tempFile);
		} else {
			if (dynamicCriteria.equals("tanquility") || dynamicCriteria.equals("version-consistency")) {
				DynaAnalyzeFile(tempFile);
			}
		}

	}
	/**
	 * analyze a uncompressed file
	 * 
	 * @param projectLocation
	 */
	public void analyzeSource(String projectLocation, String outPath) {
		String input = projectLocation;
		File tempFile = new File(input);
		tempFile.mkdir();
		if (dynamicCriteria.equals("quiescence")) {
			QuieAnalyzeFile(tempFile);
		} else {
			if (dynamicCriteria.equals("tanquility") || dynamicCriteria.equals("version-consistency")) {
				DynaAnalyzeFile(tempFile,outPath);
			}
		}

	}


	/**
	 * analyze compressed files : .war, .ear, .jar, .zip
	 * 
	 * @param jarLocation
	 * @param tempLocation
	 * @param outputLocation
	 */
	public void analyzeJar(String jarLocation, String tempLocation,
			String outputLocation) {
		UnjarTool jar2temp = new UnjarTool();
		JarTool temp2jar = new JarTool();

		String input = jarLocation;
		String temp = tempLocation;
		String output = outputLocation;
		File tempFile = new File(temp);
		tempFile.mkdir();
		jar2temp.unjar(input, temp);

		analyzeSource(tempLocation);

		File destJar = new File(output);
		try {
			temp2jar.jarDir(tempFile, destJar);
			jar2temp.clean(tempLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * analyze an tuscany application, whether a file or a compressed file. If
	 * it's a compressed one,we will uncompress it in the tempPath, and put the
	 * analyzed one in the target path.
	 * 
	 * @param sourcePath
	 * @param tempPath
	 * @param targetPath
	 */
	public void analyzeApplication(String sourcePath, String tempPath,
			String targetPath) {
		String[] names = sourcePath.split("/");
		String fileName = names[names.length - 1];
		if (fileName.endsWith(".jar") || fileName.endsWith(".war")
				|| fileName.endsWith(".ear") || fileName.endsWith(".zip")) {
			String tempLocation = tempPath
					+ fileName.substring(0, fileName.length() - 4);
			String outputLocation = targetPath + fileName;
			analyzeJar(sourcePath, tempLocation, outputLocation);
		} else {
			analyzeSource(sourcePath);
		}
	}

	/**
	 * target application overwrites the source one.
	 * 
	 * @param sourcePath
	 * @param tempPath
	 */
	public void analyzeApplication(String sourcePath, String tempPath) {
		String[] names = sourcePath.split("/");
		String fileName = names[names.length - 1];
		if (fileName.endsWith(".jar") || fileName.endsWith(".war")
				|| fileName.endsWith(".ear") || fileName.endsWith(".zip")) {
			String tempLocation = tempPath
					+ fileName.substring(0, fileName.length() - 4);
			analyzeJar(sourcePath, tempLocation, sourcePath);
		} else {
			analyzeSource(sourcePath);
		}
	}
}

class TranClassWriter extends ClassWriter {
	// private ClassLoader loader;
	public TranClassWriter(final int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(final String type1, final String type2) {
		Class<?> c, d;
		try {
			c = Class.forName(type1.replace('/', '.'));
			d = Class.forName(type2.replace('/', '.'));
		} catch (Exception e) {
			// System.out.println("!!!!!!Exception, Type: " + type1 + "," +
			// type2);
			// System.out.println("!!!!!!Exception, "+e.toString());
			return "java/lang/Object";
			// throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}

}

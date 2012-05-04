package org.imaginea.botbot.common;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.imaginea.botbot.api.DefaultProperties;



public class TestCaseGenerator {

    HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
    private static String baseFolder="";
    private String dataDrivenClasses = "";
    //private static String baseFolder=new File("runner").getPath()+File.separatorChar;
    public void listDirectory(File f, String directoryName, HashMap<String, ArrayList<String>> fileList,final String fileType, final boolean absolute) {
        char pathSeparator=File.separatorChar;
        String testcaseFolder=getPath(new File(baseFolder+"testcases"+pathSeparator),absolute);
        File[] listOfFiles = f.listFiles();

        if (directoryName.equalsIgnoreCase("") && f.isDirectory()) {
            String path=getPath(f,absolute);
                directoryName=getDirectoryName(testcaseFolder,path);
        } else if (directoryName.equalsIgnoreCase("") && f.isFile()) {
            directoryName = getDirectoryName(testcaseFolder,getPath(new File(f.getParent()),absolute)                 );
        }
        for (int i = 0; i < listOfFiles.length; i++) {
            if (shouldBeFiltered(listOfFiles[i], fileType)) {
                if (fileList.containsKey(directoryName)) {
                    List<String> flist=fileList.get(directoryName);
                    String fPath=getPath(listOfFiles[i],absolute);
                    fPath=filterPath(fPath, absolute);
                    flist.add(fPath);
                } else {
                    ArrayList<String> flist = new ArrayList<String>();
                    String fPath=getPath(listOfFiles[i],absolute);
                    fPath=filterPath(fPath, absolute);
                    flist.add(fPath);
                    fileList.put(directoryName, flist);
                }
            } else if (listOfFiles[i].isDirectory()) {
            	listDirectory(listOfFiles[i],
                        getDirectoryName(testcaseFolder,getPath(listOfFiles[i],absolute)),
                        fileList,fileType,absolute);
            }
        }
    }
    
    private boolean shouldBeFiltered(File f,String filetype){
    	boolean isRequired=false;
    	if(f.isDirectory()){
    		return isRequired;
    	}
    	if(f.getName().startsWith(".")){
    		return isRequired;
    	}
    	if(f.getName().endsWith("."+filetype)){
    		return true;
    	}
    	return isRequired;
    }
    
    private String getPath(File f, boolean absolute){
    	if(absolute){
    		return f.getAbsolutePath();
    	}else{
    		return f.getPath();
    	}
    }
    
    private String filterPath(String fPath,boolean absolute){
    	String path;
    	if(absolute){
    		path=fPath.replace("\\", "\\\\");
        }else{
        	path=fPath.replace("\\", "/");
        }
    	return path;
    }
    
    private String getDirectoryName(String path,String testFolder){
        String directoryName = "";
        if (String.valueOf(File.separatorChar).contentEquals("\\")) {
            List<String> paths = new ArrayList<String>(Arrays.asList(path.split("\\\\")));
            List<String> testCaseFolders = new ArrayList<String>(Arrays
                    .asList(testFolder.split("\\\\")));
            int pathsLength = paths.size();
            for (int i = 0; i < pathsLength && paths.size() != 0; i++) {
                if (!paths.get(0).contentEquals(testCaseFolders.get(0))) {
                    break;
                }
                paths.remove(0);
                testCaseFolders.remove(0);
            }
            Iterator<String> it = testCaseFolders.iterator();
            while (it.hasNext()) {
                directoryName = directoryName + File.separatorChar + it.next();
            }
            directoryName = directoryName.substring(1);
        } else {
            directoryName = testFolder.split(path+File.separatorChar)[1];
        }
        return directoryName;
    }


    public void classGenerator(HashMap<String, ArrayList<String>> hm,String template)
            throws Exception {
        char pathSeparator= File.separatorChar;
        Iterator<String> it = hm.keySet().iterator();
        String testSource=baseFolder+("src:test:".replace(":", String.valueOf(pathSeparator)));
        while (it.hasNext()) {
            String classPath = it.next();
            String packagePath = classPath.toLowerCase();

            packagePath = replaceSpecialChar(packagePath);
            String javaClassFile = new File(packagePath).getName();
            String javaClassName = javaClassFile.substring(0, 1).toUpperCase()
                    + javaClassFile.substring(1);
            
            //checking if folder name is datadriven 
			if (javaClassFile.equalsIgnoreCase("datadriven")&&template.contentEquals("RobotiumTemplate")) {
				try {
					packagePath = packagePath.substring(0,
							packagePath.lastIndexOf(pathSeparator));
				} catch (StringIndexOutOfBoundsException e) {
					System.out.println("Exception out of bounds");
					packagePath = "";
				}
				//creating package folder 
				new File(testSource + packagePath + pathSeparator
						+ javaClassFile).mkdirs();
				
				//For iterating through classes 
				
				ArrayList<String> ar = hm.get(classPath);
				
				

				Iterator<String> dataDrivenIterator = ar.iterator();
				while (dataDrivenIterator.hasNext()) {
					String testFilePath = dataDrivenIterator.next();
					String fileName = new File(testFilePath).getName();
					fileName = fileName.substring(0, fileName.indexOf(".csv"));
					
					//Creating java classes for data driven test cases & ignoring other files 
					if (fileName.endsWith("_datadriven")) {
						String dataDrivenJavaFileName = fileName.replace(
								"_datadriven", "");
						dataDrivenJavaFileName = dataDrivenJavaFileName
								.substring(0, 1).toUpperCase()
								+ dataDrivenJavaFileName.substring(1);
						Writer output = new BufferedWriter(new FileWriter(
								testSource + packagePath + pathSeparator
										+ "datadriven" + pathSeparator
										+ dataDrivenJavaFileName + ".java"));
						String packageString = packagePath + pathSeparator
								+ "datadriven";
						dataDrivenClassWriter(dataDrivenJavaFileName, output,
								"RobotiumDataDrivenTemplate", testFilePath, packageString);
						dataDrivenClasses += "test."
								+ packageString
										.replace(pathSeparator + "", ".")
								+ "."+dataDrivenJavaFileName + ",";
						output.close();

					}

				}
				
				

			}
            
            //Java class generation for non-datadriven cases
            else {
            try{
            packagePath = packagePath
                    .substring(0, packagePath.lastIndexOf(pathSeparator));}
            catch(StringIndexOutOfBoundsException e)
            {
                System.out.println("Exception out of bounds");
                packagePath="";
            }
            new File(testSource + packagePath).mkdirs();
            Writer output = new BufferedWriter(new FileWriter(testSource
                    + packagePath + pathSeparator + javaClassName + ".java"));
            ArrayList<String> ar = hm.get(classPath);

            classWriter(classPath, ar, output,template);
            output.close();
            }
        }

    }
    
    //Class writer for data driven csv
    private void dataDrivenClassWriter(String fileName, Writer output,
			String template,String testFilePath,String packagePath) {
    	
    	try{
    	char pathSeparator=File.separatorChar;
    	FileInputStream fstream;
    	String resourcesPath=baseFolder+"resources"+pathSeparator;
    	fstream = new FileInputStream(resourcesPath+template);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        boolean flag = false;
        String importString=packagePath.replace(pathSeparator+"", ".");
        String javaClass ="package test."+importString+";\n";
        String tempString="";
        while ((tempString = br.readLine()) != null) {
            if (tempString.contains("TestClassName")){
            		javaClass+=tempString.replace("TestClassName", fileName)+"\n";
            }
            else if (tempString.contains("filePath")){
        		javaClass+=tempString.replace("filePath", testFilePath)+"\n";
        		
            }
            else if (tempString.contains("dataFile")){
            	String dataFile=testFilePath.substring(0,testFilePath.indexOf("_datadriven.csv"));
            
            	javaClass+=tempString.replace("dataFile", dataFile+"_data.csv")+"\n";
            }
            else{
            	javaClass+=tempString+"\n";
            }


        }
       
        output.write(javaClass);
    	}
    	catch(IOException e) {
    		
    		System.out.println("Exception  "+e);
    		
    	}
    	
		
	}

	public static void main(String[] args) {
        TestCaseGenerator tc = new TestCaseGenerator();
        HashMap<String, ArrayList<String>> hm1 = new HashMap<String, ArrayList<String>>();
        DefaultProperties prop = DefaultProperties.getDefaultProperty();
        boolean absolute=false;
        boolean isRobotium=false;
        String template ="Template";
        char pathSeparator=File.separatorChar;
        File f =new File("");
        System.out.println(f.getAbsolutePath());
        String testFolderPath=baseFolder+"testcases"+pathSeparator;
        if(prop.getValueFromProperty("FRAMEWORK").equalsIgnoreCase("robotium")){
            isRobotium=true;
            template="RobotiumTemplate";
        }
        if(!isRobotium){
            absolute=true;
        }
        tc.listDirectory(new File(testFolderPath
                + prop.getValueFromProperty("TESTCASE_FOLDER")), "",hm1, "csv", absolute);
        System.out.println(hm1);
        try {
            tc.classGenerator(hm1,template);
            //Creating a properties file specifying data driven classes 
            Writer dataDrivenPropertiesWriter = new BufferedWriter(new FileWriter(baseFolder+"resources"+pathSeparator+"datadriven.properties"));
			dataDrivenPropertiesWriter.write("DATA_CLASSES="+tc.dataDrivenClasses);
			dataDrivenPropertiesWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void classWriter(String classPath, ArrayList<String> ar,
            Writer output,String template) {
        boolean isRobotium=false;
        FileInputStream fstream;
        char pathSeparator=File.separatorChar;
        String resourcesPath=baseFolder+"resources"+pathSeparator;
        if(template.contentEquals("RobotiumTemplate")){
            isRobotium=true;
        }
        try {
            String tempString = "";
            String importString = classPath.replace(String.valueOf(pathSeparator), ".");
            String outputStringClass="";
            importString = replaceSpecialChar(importString);
            try{
            importString = importString.substring(0,
                    importString.lastIndexOf("."));
            }
            catch(StringIndexOutOfBoundsException e)
            {
                System.out.println("Exception out of bounds");
                outputStringClass = "package test;\n";
            }
            if(outputStringClass==""){
            outputStringClass = "package test." + importString + ";\n";
            }
            String outputStringTestCase = "";
            String testCase = "";

            fstream = new FileInputStream(resourcesPath+template);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            boolean flag = false;
            while ((tempString = br.readLine()) != null) {
                if (tempString.equalsIgnoreCase("@Test") || (isRobotium && tempString.contains("testName")))
                    flag = true;

                if (!flag)
                    outputStringClass += tempString + "\n";
                else
                    outputStringTestCase += tempString + "\n";

            }
            String packagePath = classPath.toLowerCase();
            String javaClassFile = new File(packagePath).getName();
            javaClassFile = replaceSpecialChar(javaClassFile);
            String javaClassName = javaClassFile.substring(0, 1).toUpperCase()
                    + javaClassFile.substring(1);

            outputStringClass = outputStringClass.replace("TestClassName",
                    javaClassName);
            output.write(outputStringClass + "\n");

            Iterator<String> arrayIt = ar.iterator();
            while (arrayIt.hasNext()) {
                String testfile = arrayIt.next();
                String fileName = new File(testfile).getName();
                fileName = fileName.substring(0, fileName.indexOf(".csv"));

                fileName = replaceSpecialChar(fileName);
                if(isRobotium){
                    fileName="test"+fileName.substring(0, 1).toUpperCase()
                            + fileName.substring(1);
                }

                testCase = outputStringTestCase.replace("testName", fileName);
                testCase = testCase.replace("filePath", testfile);

                output.write(testCase + "\n");
            }

            output.write("}");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Assert.fail(e.toString());
        }

        catch (IOException e) {
            // TODO Auto-generated catch block
            Assert.fail(e.toString());
        }
    }
    private String replaceSpecialChar(String fileName)
    {
        fileName=fileName.replace(" ", "");
        fileName=fileName.replace("-", "");
        fileName=fileName.replace("_", "");
        return fileName;
    }
}




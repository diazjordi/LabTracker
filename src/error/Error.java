package error;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import main.LabTracker;

public class Error {
	
	private static Error errorInstance;
	private static String errorFileOutputPath;
	private static HashMap<String, String> minorErrors = new HashMap<String, String>();
	
    private Error() {
    }
    
    public static Error geErrorInstance() {
        if (null == errorInstance) {
            errorInstance = new Error();
        }
        return errorInstance;
    }
    
	public void setErrorFileOutputPath(String errorFileOutputPath) {
		Error.errorFileOutputPath = errorFileOutputPath;
	}
	
	public String getErrorFileOutputPath() {
		return errorFileOutputPath;
	}
	
	// Store minor error
	public void MinorError(String className, String errorInfo){
		Error.minorErrors.put(className, errorInfo);
	}
	
	// Log out all minor errors at end of program
	public void logMinorErrors(){
		
	}
	
	// Terminate due to Fatal Error
	public void fatalError(String errorInfo) {
		if(errorFileOutputPath != null){
			try {
				File output = new File(errorFileOutputPath);
				ObjectOutputStream listOutputStream = new ObjectOutputStream(new FileOutputStream(output));
				if (errorInfo.isEmpty()) {
					listOutputStream.writeUTF("Error Detected in LabTracker, please review logs and delete this file to enable next run");
				} else {
					System.out.println(errorInfo);
					listOutputStream.writeUTF(errorInfo);
				}
				listOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else {
			System.out.println("No Error Output Path Set, program cannot continue!");
			System.out.println("Program will now terminate!");
			System.exit(0);
		}		
	}
	
	public void checkForErrorFile(String errorFileOutputPath) {
		// Check for Error File existence, if exists update DB and exit
		File errorFile = new File(errorFileOutputPath);
		// Check for existence of error file
		if (errorFile.exists()) {
			String errorInfo = "LabTracker terminating, Error File detected! Resolve error and remove file to continue with next run!";
			LabTracker.logger.fatal(errorInfo);
			errorInstance.fatalError(errorInfo);
		}
	}

}

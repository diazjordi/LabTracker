package error;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Error {

	private static Error errorInstance;
	private static String errorFileOutputPath;

	private static final Logger logger = LogManager.getLogger("LabTracker");

	private Error() {
	}

	public static Error getErrorInstance() {
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

	// Terminate due to Fatal Error
	public void fatalError(String errorInfo) {
		if (errorFileOutputPath != null) {
			try {
				File output = new File(errorFileOutputPath);
				FileOutputStream fOS = new FileOutputStream(output);
				ObjectOutputStream listOutputStream = new ObjectOutputStream(fOS);
				if (errorInfo.isEmpty()) {
					listOutputStream.writeUTF("Error Detected in LabTracker, please review logs and delete this file to enable next run");
				} else {
					logger.error(errorInfo);
					listOutputStream.writeUTF(errorInfo);
				}
				listOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No Error Output Path Set, program cannot continue!");
			System.out.println("Program will now terminate!");
			//System.exit(0);
		}
		//System.exit(0);
	}

	public void checkForErrorFile(String errorFileOutputPath) {
		// Check for Error File existence, if exists update DB and exit
		File errorFile = new File(errorFileOutputPath);
		// Check for existence of error file
		if (errorFile.exists()) {
			String errorInfo = "LabTracker terminating, Error File detected! Check logs to resolve error and remove file to continue with next run!";
			logger.fatal(errorInfo);
			errorInstance.fatalError(errorInfo);
		}
	}

}

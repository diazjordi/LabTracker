package errors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FatalError {
	
	private static String errorFileOutputPath;
		
	public FatalError() {
		super();
	}
	
	public FatalError(String errorFileOutputPath){
		super();
		FatalError.errorFileOutputPath = errorFileOutputPath;
	}
		
	public static String getErrorFileOutputPath() {
		return errorFileOutputPath;
	}

	public static void setErrorFileOutputPath(String errorFileOutputPath) {
		FatalError.errorFileOutputPath = errorFileOutputPath;
	}

	public static void fatalErrorEncountered(String errorInfo) {
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


	
	
	
	
	

}

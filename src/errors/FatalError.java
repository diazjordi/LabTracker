package errors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FatalError {
	
	private String classType;
	private String errorInfo;
	
	private String errorFileOutputPath;
	
	
	// Take in error info
	
	// Write error info out to error log
		
	// Write error info out to error DB
	
	// Send an email with error info
	
	private void fatalError(String error) {
		try {
			File output = new File(errorFileOutputPath);
			ObjectOutputStream listOutputStream = new ObjectOutputStream(
					new FileOutputStream(output));
			if (error.isEmpty()) {
				listOutputStream
						.writeUTF("Error Detected in HTMLScraper, please review logs and delete this file to enable next run");
			} else {
				System.out.println(error);
				listOutputStream.writeUTF(error);
			}
			listOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

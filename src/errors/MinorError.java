package errors;

import java.util.HashMap;

public class MinorError {
	
	private static HashMap<String, String> errors = new HashMap<String, String>();
	private static String errorFileOutputPath;
	
	public MinorError() {
		super();
	}
	
	public MinorError(String className, String error){
		super();
		MinorError.errors.put(className, error);
	}

	public static HashMap<String, String> getErrors() {
		return errors;
	}

	public static void setErrors(HashMap<String, String> errors) {
		MinorError.errors = errors;
	}

	public static String getErrorFileOutputPath() {
		return errorFileOutputPath;
	}

	public static void setErrorFileOutputPath(String errorFileOutputPath) {
		MinorError.errorFileOutputPath = errorFileOutputPath;
	}
	
	public static void addMinorError(String className, String error){
		MinorError.errors.put(className, error);
	}	

}

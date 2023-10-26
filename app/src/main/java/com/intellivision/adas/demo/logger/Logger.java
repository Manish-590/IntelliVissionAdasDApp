package com.intellivision.adas.demo.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.intellivision.adas.demo.MainApplication;
import com.intellivision.adas.demo.utilities.DateTimeUtils;
import com.intellivision.adas.demo.utilities.DeviceUtils;

/**
 * This class is used to write the error logs to file.
 * 
 */
public class Logger {

	private final static String FILE_NAME = "Logs";
	private final static String FILE_EXTENTION = ".txt";
	public final static String TIMESTAMP_FORMAT = "yyyy_MM_dd_kk_mm_ss";
	public final static String FILE_NAME_FORMAT = "yyyy_MM_dd";

	public static String logTag;
	private static String _packageName;
	private static String _versionCode;
	private static String _versionName;
	private static String _targetSdk;
	private static String _deviceModel;
	private static String _deviceManufacturer;
	private static String _deviceId;

	public static boolean disableConsoleLogs;
	public static boolean disableSDCardLogs = false;

	/**
	 * This method is used to initialize logger
	 * 
	 * @param params
	 *            the {@link LoggerParams} object
	 */
	public static void init(LoggerParams params) {
		String applicationName = params.getApplicationName();
		Logger.logTag = applicationName;
		IVFile.setBaseFolderNameOnSdCard(applicationName);
		disableSDCardLogs = !MainApplication.isDebugBuild;
		_packageName = params.getPackageName();
		_versionCode = params.getApplicationVersionCode();
		_versionName = params.getApplicationVersionName();
		_targetSdk = params.getTargetSdk();
		_deviceModel = params.getDeviceModel();
		_deviceManufacturer = params.getDeviveManufacturer();
		_writeHeaderToFile();
	}

	/**
	 * This method is used to enable or disable displaying logs on Console.
	 * 
	 * @param state
	 *            can be true or false.
	 */
	public void disableConsoleLogs(boolean state) {
		disableConsoleLogs = state;
	}

	/**
	 * This method is used to enable or disable writing logs to file.
	 * 
	 * @param state
	 *            can be true or false.
	 */
	public void disableSDCardLogs(boolean state) {
		disableSDCardLogs = state;
	}

	/**
	 * This function add log message into logger file
	 * 
	 * @param category
	 *            String containing category of a component. One of the category
	 *            from {@link Category}
	 * @param subCategory
	 *            String containing type of log E(ERROR)/I(INFO)
	 * @param message
	 *            String containing messaged to be logged.
	 */
	public static void addEntry(String category, String subCategory,
			String message) {
		if (DeviceUtils.isSdCardPresent()) {
			String logEntry = _composeLogEntry(category, subCategory, message);
			_writeToFile(logEntry);
		}
	}

	/**
	 * This function creates a composed string from specified parameters.
	 * 
	 * @param category
	 *            String specifying category of a component.
	 * @param subCategory
	 *            String specifying type of log INFO/ERROR/DEBUG
	 * @param message
	 *            String containing log message
	 * @return Composed string.
	 */
	private static String _composeLogEntry(String category, String subCategory,
			String message) {
		String logEntry = "";

		try {
			String timeStamp = DateTimeUtils.getFormattedTime(TIMESTAMP_FORMAT);

			logEntry = "[" + timeStamp + "]" + " " + "[" + category + "]"
					+ " [" + subCategory + "] " + message + "\n";
		} catch (Exception e) {
		}

		return logEntry;
	}

	/**
	 * This method will write the messages to file
	 * 
	 * @param message
	 *            message of type {@link String} to be added.
	 */
	private static void _writeToFile(String message) {

		String fileName = getCurrentFileName();
		String folderPath = IVFile.getSdCardFilePath(IVFile.LOGGER);
		if (folderPath == null) {
			return;
		}
		String filePath = folderPath + File.separator + fileName;
		try {
			File file = new File(filePath);
			FileOutputStream fos = new FileOutputStream(file, true);

			long size = file.length();
			if (size == 0) {
				synchronized (fos) {
					_writeHeader(fos);
				}
			}
			synchronized (fos) {
				fos.write(message.getBytes());
			}

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will write the messages to file
	 */
	private static void _writeHeaderToFile() {

		String fileName = getCurrentFileName();
		String folderPath = IVFile.getSdCardFilePath(IVFile.LOGGER);
		if (folderPath == null) {
			return;
		}
		String filePath = folderPath + File.separator + fileName;
		try {
			File file = new File(filePath);
			FileOutputStream fos = new FileOutputStream(file, true);

			synchronized (fos) {
				_writeHeader(fos);
			}

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function write logger header in log file.
	 * 
	 * @param fos
	 *            Object of type {@link IVFile} to which header is to be added.
	 */
	private static void _writeHeader(FileOutputStream fos) {
		if (fos != null) {
			try {
				String separator = "----------------------------------------------------------------------------------------------------\n";
				fos.write(separator.getBytes());

				if (_packageName != null && _packageName.length() > 0) {
					String packageNameLog = "Package Name: " + _packageName
							+ "\n";
					fos.write(packageNameLog.getBytes());
				}

				if (_versionCode != null && _versionCode.length() > 0) {
					String version = "Version code: " + _versionCode + "\n";
					fos.write(version.getBytes());
				}

				if (_versionName != null && _versionName.length() > 0) {
					String versionName = "Version name: " + _versionName + "\n";
					fos.write(versionName.getBytes());
				}

				if (_targetSdk != null && _targetSdk.length() > 0) {
					String targerSdk = "Device sdk: " + _targetSdk + "\n";
					fos.write(targerSdk.getBytes());
				}

				if (_deviceModel != null && _deviceModel.length() > 0) {
					String deviceModel = "Device model: " + _deviceModel + "\n";
					fos.write(deviceModel.getBytes());
				}

				if (_deviceManufacturer != null
						&& _deviceManufacturer.length() > 0) {
					String deviceManufacturer = "Device manufacturer: "
							+ _deviceManufacturer + "\n";
					fos.write(deviceManufacturer.getBytes());
				}

				if (_deviceId != null && _deviceId.length() > 0) {
					String deviceId = "Device id: " + _deviceId + "\n";
					fos.write(deviceId.getBytes());
				}

				fos.write(separator.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method is used to get the filename for error logs file.
	 * 
	 * @return formatted filename (MlLogs_YYYY_MM_DD.txt)
	 */
	public static String getCurrentFileName() {
		String fileName = FILE_NAME + "_";
		String dateString = DateTimeUtils.getFormattedTime(FILE_NAME_FORMAT);
		fileName += dateString + FILE_EXTENTION;
		return fileName;
	}

	/**
	 * This method is used to remove older log files. The files which are older
	 * than days will be deleted from file system.
	 */
	public static void removeOlderFiles() {
		try {
			File loggerDir = new File(IVFile.getSdCardFilePath(IVFile.LOGGER));

			String currentFileName = getCurrentFileName();
			/**
			 * Find first index of _ to remove file prefix, length-4 to remove
			 * file extension.
			 */
			currentFileName = currentFileName.substring(
					currentFileName.indexOf("_") + 1,
					currentFileName.length() - 4);

			if (loggerDir != null && loggerDir.isDirectory()) {
				File[] files = loggerDir.listFiles();
				for (File file : files) {
					if (file != null && file.exists()) {
						String fileName = file.getName();
						fileName = fileName.substring(
								fileName.indexOf("_") + 1,
								fileName.length() - 4);

						int difference = DateTimeUtils.getNoOfDaysBetween(
								currentFileName, fileName);

						if (difference > 3) {
							file.delete();
						}
					}
				}
			}
		} catch (Exception ex) {
			VCLog.error(Category.CAT_GENERAL,
					"Logger: removeOlderFiles: Exception->" + ex.getMessage());
		}
	}
}
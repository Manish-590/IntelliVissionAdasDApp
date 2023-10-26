package com.intellivision.adas.demo.logger;

/**
 * Datamodel class to hold the logger parameters.
 * 
 */
public class LoggerParams {
    private String applicationName;
    private String logTag;
    private String packageName;
    private String applicationVersionCode;
    private String applicationVersionName;
    private String targetSdk;
    private String deviceModel;
    private String deviveManufacturer;

    /**
     * Parameterized Constructor
     * 
     * @param applicationName
     *            the application name
     * @param packageName
     *            the package name
     * @param applicationVersionCode
     *            the application version code
     * @param applicationVersionName
     *            the application version name
     * @param targetSdk
     *            the target sdk
     * @param deviceModel
     *            the device model
     * @param deviveManufacturer
     *            the device manufacturer
     * 
     * @param logTag
     *            the log tag
     */
    public LoggerParams( String applicationName, String packageName, String applicationVersionCode, String applicationVersionName, String targetSdk, String deviceModel, String deviveManufacturer,
            String logTag ) {
        super( );
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.applicationVersionCode = applicationVersionCode;
        this.applicationVersionName = applicationVersionName;
        this.targetSdk = targetSdk;
        this.deviceModel = deviceModel;
        this.deviveManufacturer = deviveManufacturer;
        this.logTag = logTag;
    }

    /**
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @param applicationName
     *            the applicationName to set
     */
    public void setApplicationName( String applicationName ) {
        this.applicationName = applicationName;
    }

    /**
     * @return the logTag
     */
    public String getLogTag() {
        return logTag;
    }

    /**
     * @param logTag
     *            the logTag to set
     */
    public void setLogTag( String logTag ) {
        this.logTag = logTag;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName
     *            the packageName to set
     */
    public void setPackageName( String packageName ) {
        this.packageName = packageName;
    }

    /**
     * @return the applicationVersionCode
     */
    public String getApplicationVersionCode() {
        return applicationVersionCode;
    }

    /**
     * @param applicationVersionCode
     *            the applicationVersionCode to set
     */
    public void setApplicationVersionCode( String applicationVersionCode ) {
        this.applicationVersionCode = applicationVersionCode;
    }

    /**
     * @return the applicationVersionName
     */
    public String getApplicationVersionName() {
        return applicationVersionName;
    }

    /**
     * @param applicationVersionName
     *            the applicationVersionName to set
     */
    public void setApplicationVersionName( String applicationVersionName ) {
        this.applicationVersionName = applicationVersionName;
    }

    /**
     * @return the targetSdk
     */
    public String getTargetSdk() {
        return targetSdk;
    }

    /**
     * @param targetSdk
     *            the targetSdk to set
     */
    public void setTargetSdk( String targetSdk ) {
        this.targetSdk = targetSdk;
    }

    /**
     * @return the deviceModel
     */
    public String getDeviceModel() {
        return deviceModel;
    }

    /**
     * @param deviceModel
     *            the deviceModel to set
     */
    public void setDeviceModel( String deviceModel ) {
        this.deviceModel = deviceModel;
    }

    /**
     * @return the deviveManufacturer
     */
    public String getDeviveManufacturer() {
        return deviveManufacturer;
    }

    /**
     * @param deviveManufacturer
     *            the deviveManufacturer to set
     */
    public void setDeviveManufacturer( String deviveManufacturer ) {
        this.deviveManufacturer = deviveManufacturer;
    }

}

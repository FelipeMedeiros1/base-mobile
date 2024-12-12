package mobile;

public class Device {

    private String platform;
    private String platformVersion;
    private String serial;
    private String remoteConnectUrl;
    private Boolean present;
    private Boolean ready;
    private Boolean using;
    private String owner;

    public Device() {

    }

    public Device(String platform, String platformVersion, String serial, String remoteConnectUrl, Boolean present, Boolean ready, Boolean using, String owner) {
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.serial = serial;
        this.remoteConnectUrl = remoteConnectUrl;
        this.present = present;
        this.ready = ready;
        this.using = using;
        this.owner = owner;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getRemoteConnectUrl() {
        return remoteConnectUrl;
    }

    public void setRemoteConnectUrl(String remoteConnectUrl) {
        this.remoteConnectUrl = remoteConnectUrl;
    }

    public Boolean isPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }

    public Boolean isReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public Boolean isUsing() {
        return using;
    }

    public void setUsing(Boolean using) {
        this.using = using;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

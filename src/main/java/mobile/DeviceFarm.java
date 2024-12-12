package mobile;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import data.InternalPropertiesLoader;
import exceptions.AutomationException;
import io.restassured.RestAssured;
import io.restassured.authentication.AuthenticationScheme;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DeviceFarm {

    static final Logger logger = LogManager.getLogger(DeviceFarm.class);
    private String baseUri = new InternalPropertiesLoader("configuration_core.properties").getValue("mobile.deviceFarm.android.url");
    private String token = new InternalPropertiesLoader("configuration_core.properties").getValue("mobile.deviceFarm.android.token");
    private Device device;
    private static DeviceFarm instance;

    private DeviceFarm() {
        setRequestConfig();
    }

    public static DeviceFarm getInstance() {
        if (instance == null) {
            instance = new DeviceFarm();
        }
        return instance;
    }

    private void setRequestConfig() {
        logger.info("Organizando configurações RestAssured.");
        RestAssured.useRelaxedHTTPSValidation();
        AuthenticationScheme auth = RestAssured.oauth2(token);
        RestAssured.authentication = auth;
        RestAssured.baseURI = baseUri;
    }

    public Device getDevice() {
        if (this.device == null) {
            this.device = connectToDevice();
        }
        return this.device;
    }

    private List<Device> getDevices() {
        JsonArray devicesJa = new JsonArray();
        try {
            devicesJa = given().
                    when().
                    get("/devices").
                    then().
                    statusCode(200).
                    body("success", equalTo(true)).
                    extract().
                    body().as(JsonObject.class, ObjectMapperType.GSON).get("devices").getAsJsonArray();
        } catch (Exception e) {
            String errorMsg = "Não foi possível obter os dispositivos mobile através da API do STF. ";
            throw new AutomationException(errorMsg + e.toString());
        }

        List<Device> devices = new ArrayList<>();
        for (JsonElement dJe : devicesJa) {
            if (dJe.getAsJsonObject().has("platform")) {
                devices.add(buildDevice(dJe));
            }
        }

        return devices;
    }

    private Device getDevice(String deviceSerial) {
        JsonElement deviceJe;
        Response response = given().
                when().
                get("/devices/{deviceSerial}", deviceSerial);

        if (Integer.toString(response.getStatusCode()).equals("404")) {
            String errorMsg = String.format("O dispositivo com serial '%s' não foi encontrado.", deviceSerial);
            throw new AutomationException(errorMsg);
        }

        if (Integer.toString(response.getStatusCode()).equals("200")) {
            deviceJe = response.getBody().as(JsonObject.class, ObjectMapperType.GSON).get("device").getAsJsonObject();
            return buildDevice(deviceJe);
        }

        String errorMsg = "Não foi possível obter os dispositivos mobile através da API do STF. ";
        throw new AutomationException(errorMsg);
    }

    private Device buildDevice(JsonElement devicesJe) {
        JsonObject d = devicesJe.getAsJsonObject();
        Device device = new Device();
        device.setPlatform(d.get("platform").getAsString());
        device.setPlatformVersion(d.get("version").getAsString());
        device.setSerial(d.get("serial").getAsString());
        device.setPresent(d.get("present").getAsBoolean());
        device.setReady(d.get("ready").getAsBoolean());
        device.setUsing(d.get("using").getAsBoolean());

        return device;
    }

    private Device getAvailableDevice() {
        String deviceSerial = new InternalPropertiesLoader("configuration_core.properties").getValue("mobile.devicefarm.android.deviceSerial");

        if (Strings.isNullOrEmpty(deviceSerial)) {
            List<Device> devices = getDevices();
            for (Device device : devices) {
                if (isAvailableDevice(device)) {
                    return device;
                }
            }
            throw new AutomationException("Não há dispositivos mobile disponíveis no Device Farm.");
        }

        Device device = getDevice(deviceSerial);
        if (isAvailableDevice(device)) {
            return device;
        }
        throw new AutomationException(String.format("O dispositivo com o serial '%s' não está disponível.", device.getSerial()));
    }

    private boolean isAvailableDevice(Device device) {
        if (device.isPresent() &&
                device.isReady() &&
                !device.isUsing() &&
                Strings.isNullOrEmpty(device.getOwner())) {
            return true;
        }

        return false;
    }

    private boolean addUserDevice(Device device) {
        JsonObject payload = new JsonObject();
        payload.addProperty("serial", device.getSerial());
        payload.addProperty("timeout", 900000);

        try {
            given().
                    contentType("application/json").
                    body(payload.toString()).
                    when().
                    post("/user/devices").
                    then().
                    statusCode(200).
                    body("success", equalTo(true));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String remoteConnectUserDeviceBySerial(String deviceSerial) {
        try {
            return given().
                    contentType("application/json").
                    when().
                    post("user/devices/{deviceSerial}/remoteConnect", deviceSerial).
                    then().
                    statusCode(200).
                    extract().
                    path("remoteConnectUrl");
        } catch (Exception e) {
            String errorMsg = String.format("Não foi possível se conectar com o dispositivo '%s'", deviceSerial);
            throw new AutomationException(errorMsg);
        }
    }

    private Device connectToDevice() {
        Device device = getAvailableDevice();
        logger.info("Dispositivo mobile selecionado: " + device.getSerial());

        if (addUserDevice(device)) {
            String remoteConnectUrl = remoteConnectUserDeviceBySerial(device.getSerial());
            device.setRemoteConnectUrl(remoteConnectUrl);
        }
        connectAdbToDevice(device);

        return device;
    }

    private void connectAdbToDevice(Device device) {
        try {
            logger.info(String.format("Iniciando conexão do dispositivo com o ADB."));
            String command = String.format("adb connect %s", device.getRemoteConnectUrl());
            String processMessage = this.executeCommand(command);
            if (processMessage.contains("(10060)"))
                throw new AutomationException(String.format("Erro ao conectar no dispositivo '%s' através do adb. ", device.getSerial()));
            logger.info(String.format("Conexão com o ADB realizada com sucesso. Conectado ao dispositivo mobile '%s' através da url '%s'", device.getSerial(), device.getRemoteConnectUrl()));
        } catch (Exception e) {
            String errorMsg = String.format("Erro ao conectar no dispositivo '%s' através do adb. ", device.getSerial());
            throw new AutomationException(errorMsg + e.toString());
        }
    }

    private void disconnectDevice() {
        try {
            RestAssured.reset();
            setRequestConfig();
            given().
                    contentType("application/json").
                    when().
                    delete("/user/devices/{deviceSerial}", this.device.getSerial()).
                    then().
                    statusCode(200).
                    body("success", equalTo(true));
            logger.info(String.format("Desconectado do dispositivo mobile '%s'.", this.device.getSerial()));
        } catch (Exception e) {
            String errorMsg = String.format("Não foi possível desconectar do dispositivo '%s'", this.device.getSerial());
            logger.info(String.format("%s - [%s]", errorMsg, e.getMessage()));
        }
    }

    private void disconnectAdb() {
        try {
            logger.info("Executando comando 'adb disconnect'");
            this.executeCommand("adb disconnect");
            logger.info("Comando 'adb disconnect' executado com sucesso.");
        } catch (Exception e) {
            String errorMsg = String.format("Erro ao desconectar do dispositivo '%s' através do adb. ", this.device.getSerial());
            logger.info(String.format("%s - [%s]", errorMsg, e.getMessage()));
        }
    }

    private String executeCommand(String commandToExecute) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(commandToExecute);
        int exitCode = process.waitFor();

        BufferedReader output = exitCode == 0 ? new BufferedReader(new InputStreamReader(process.getInputStream()))
                : new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuffer stdOut = new StringBuffer();
        String s = null;

        while ((s = output.readLine()) != null) {
            stdOut.append(s);
        }

        output.close();

        if (exitCode == 1) {
            logger.error(String.format("Erro ao executar comando '%s'. Processo retornou 'exit code 1' [%s]", commandToExecute, stdOut.toString()));
            throw new AutomationException("Erro ao executar comando '%s'. Processo retornou 'exit code 1' [%s]", commandToExecute, stdOut.toString());
        }

        logger.info(String.format("Comando '%s' executado com sucesso. Mensagem de retorno [%s]", commandToExecute, stdOut.toString()));
        return stdOut.toString();
    }

    public void disconnect() {
        disconnectDevice();
        disconnectAdb();
    }

}

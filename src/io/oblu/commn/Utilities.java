/*
* * Copyright (C) 2018 GT Silicon Pvt Ltd
 *
 * Licensed under the Creative Commons Attribution 4.0
 * International Public License (the "CCBY4.0 License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://creativecommons.org/licenses/by/4.0/legalcode
 *
 * Note that the CCBY4.0 license is applicable only for the modifications
made
 * by GT Silicon Pvt Ltd
 *
*
* */
package io.oblu.commn;

import com.sun.org.apache.xpath.internal.operations.Equals;
import gnu.io.CommPortIdentifier;
import io.oblu.commn.connectivity.BLECommunication;
import io.oblu.commn.connectivity.Connectivity;
import io.oblu.commn.connectivity.USBComunication;
import io.oblu.commn.connectivity.WiFiCommunication;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import javax.swing.JOptionPane;


public class Utilities {
    public static DataLogger createNewFile(String sample_freq) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        Date now = new Date();
        File root = new File("data");
        //File root = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!root.exists())
        {
            root.mkdirs();
        }
        String logFileName = sample_freq+"Hz_" + formatter.format(now) + ".txt";
        File nonPDRFile = new File(root, logFileName);
        DataLogger dataLogger = new DataLogger(nonPDRFile);
//        System.out.println("File full path: "+nonPDRFile.getAbsolutePath());
        
        dataLogger.start();
        System.out.println("Log file's full path: "+nonPDRFile.getAbsolutePath());
        String header = String.format("%12s \t %12s \t %12s \t %12s \t %12s \t %12s \t %12s \t %12s \n\n\n ", "PKT_No.", "TimeStamp", "ax(m/s^2)", "ay(m/s^2)", "az(m/s^2)", "gx(rad/s)", "gy(rad/s)", "gz(rad/s)");
        writeNonPDRData(nonPDRFile,header);
        
       
        return dataLogger;
    }
      
    public static void writeNonPDRData(File nonPDRFile, String data){
        try
        {
            try (FileWriter writer = new FileWriter(nonPDRFile,true)) {
                writer.append(data);
                writer.flush();
            }
        }
        catch(IOException e)
        {
        }
    }
    
    public static int get_ratedevider(float a){
        int i = (int) a;
        if (i == 1)
            return 0;
        else
            return 1 + get_ratedevider(i/2);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byte2HexStr(byte[] paramArrayOfByte, int paramInt) {

        StringBuilder localStringBuilder1 = new StringBuilder("");
        int i = 0;
        for (; ; ) {
            if (i >= paramInt) {
                String str1 = localStringBuilder1.toString().trim();
                Locale localLocale = Locale.US;
                return str1.toUpperCase(localLocale);
            }
            String str2 = Integer.toHexString(paramArrayOfByte[i] & 0xFF);
            if (str2.length() == 1) {
                str2 = "0" + str2;
            }
            localStringBuilder1.append(str2);
            localStringBuilder1.append(" ");
            i += 1;
        }
    }
    
    public static String byteArrayToString(byte[] byteArr, int len){
        StringBuilder sb = new StringBuilder();
        for (int i =0; i< len; i++) {
            sb.append(String.format("%02X", byteArr[i]));
        }
        return sb.toString().trim();
    }
    
    /**
     * Method to convert hex to byteArray
     * @param result
     * @return 
     */
    public static byte[] convertingTobyteArray(String result) {
        String[] splited = result.split("\\s+");
        byte[] valueByte = new byte[splited.length];
        for (int i = 0; i < splited.length; i++) {
            if (splited[i].length() > 2) {
                String trimmedByte = splited[i].split("x")[1];
                valueByte[i] = (byte) convertstringtobyte(trimmedByte);
            }
        }
        return valueByte;
    }

    public static int convertstringtobyte(String string) {
        return Integer.parseInt(string, 16);
    }

    public static CommPortIdentifier initialize(String txtComPortName) {
        System.setProperty("gnu.io.rxtx.SerialPorts", txtComPortName);        
        CommPortIdentifier portId = null;
        
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if(currPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (currPortId.getName().equals(txtComPortName)) {
                    System.out.println(txtComPortName);
                    portId = currPortId;
                    return portId;
                }
            }
        }
        if (portId == null) {
            
            JOptionPane.showMessageDialog(null," Serial port is not available ","Please connect your device ",JOptionPane.ERROR_MESSAGE);
            System.out.println("Serial port not found!");
            System.exit(1);
        }
          return null;
    }
    
    public static Connectivity get_connectivity(){
        Connectivity connectivity = null;
        
        if (Constants.CONNECTIVITY_TYPE.equals(Constants.USB))
        {
            connectivity = new USBComunication(Constants.SERIAL_PORT);
        }
        
        if (Constants.CONNECTIVITY_TYPE.equals(Constants.BLE))
        {
            connectivity = new BLECommunication(Constants.MAC_ADDRESS);
        }
        if (Constants.CONNECTIVITY_TYPE.equals(Constants.WIFI))
        {
            connectivity = new WiFiCommunication(Constants.IP_ADDRESS);
        }
        return connectivity;
    }
    public static boolean stringToBoolean(String str){
        
        if (str.toLowerCase().equals("true")) {
            return true;
        }
        else{
            return false;
        }
    }
}

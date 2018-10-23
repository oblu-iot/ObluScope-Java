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

public class Constants {
    public static final int DATA_LEN = 34;
    public static final byte BYTE_VAL = 4;
    public static final byte AXIS = 6;
    public static final int SAMPLE_RATE = 250;
    public static float scale_pr_gyro = 57.2958f; //rad/s
    public static float scale_pr_acc = 1.0f;
    public static int BAUDRATE = 115200;
    public static String SHAPE = "CUBE";
    public static String SERIAL_PORT = "/dev/ttyACM0";
    public static String texture = "oblu.bmp";
    public static final String TYPE = "TYPE_OF_PACKET";
    public static final String NON_PDR = "NON_PDR";
    public static float OUTRATE = 0.0f;
    public static String send = "0x41 0x03 0x00 0x43";  // Uncomment in case NON PDR
    public static String DATA_TYPE =  NON_PDR ;           // In case NON PDR , change to PDR to NON_PDR
    public static final String PRO_OFF = "0x32 0x00 0x32";         //stopThread all processing
    public static final String SYS_OFF = "0x22 0x00 0x22";         //stopThread all output
    public static boolean check_plot = true;
    public static boolean check_log = true;
    public static String CONNECTIVITY_TYPE = "usb";
    public static final String USB = "usb";
    public static final String BLE = "ble";
    public static String MAC_ADDRESS;
}

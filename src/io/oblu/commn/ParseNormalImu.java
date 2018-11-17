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

import io.oblu.commn.connectivity.Connectivity;
import io.oblu.cube.JOGL3dCube;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;

public class ParseNormalImu implements Runnable{
 
    public static DecimalFormat df = new DecimalFormat("0.00");
    DecimalFormat dfTime = new DecimalFormat("0.000");
    public static int counterNormal = 0;
    int[] header = new int[4];
    int pktNum;
    int previousPkt;
    int payloadLen;
    int startCode;
    int chkSum = 0;
    double timeStamp;
    float[] inertialData = new float[7];
    public float ax;
    public float ay;
    public float az;
    public float gx;
    public float gy;
    public float gz;
    public static String ble_buffer = "";
    public static boolean normal_imu = false;
    public static int pkt_receive = 0;
    public static String df_input ="";
    float[] filter_data = new float[3];
//    static public ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<>();
    private final MadgwickAHRS madgwick = new MadgwickAHRS();
    private final JOGL3dCube panel = new JOGL3dCube();
    Connectivity connectivity = null;
    public static boolean time_period;
    float thetagz = 0.0f;
    public DataLogger dataLogger;
    private RealTimePlot realTimePlot;

    public ParseNormalImu(Connectivity connectivity_obj)
    {
        if(Constants.DATA_LOG){
            dataLogger = Utilities.createNewFile(Float.toString(Constants.OUTRATE));
        }
        this.connectivity = connectivity_obj;
        if (Constants.REAL_TIME_PLOT) {
            this.realTimePlot = new RealTimePlot();
            this.realTimePlot.execute();
        }
        if (Constants.CUBE_ANIMATION) {
            JFrame window = new JFrame("oblu - an open platform for wearable motion sensing");
            window.setContentPane(panel);
            window.pack();
            window.setLocation(720,50);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setVisible(true);
            panel.requestFocusInWindow();
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    connectivity_obj.send(Constants.PRO_OFF);
                    connectivity_obj.send(Constants.SYS_OFF);
                    connectivity_obj.close();
                    if (Constants.DATA_LOG){
                        dataLogger.stopLogging();
                        try {
                            dataLogger.join();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ParseNormalImu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    System.exit(0);
                }
            } );
        }
    }
    
    public void  get_plot_normal()
    {
        long start = System.currentTimeMillis();
        int lenght, newlen;
        if (normal_imu) {
            byte[] buffer = final_buffer(Constants.DATA_LEN);
            while ((System.currentTimeMillis() - start)/1000 < Constants.RUN_TIME) 
            {
                System.out.println("time: "+ String.format("%.2f",(float)(System.currentTimeMillis() - start)/1000));
                parse_data(buffer);
                String inHex = Utilities.byte2HexStr(buffer,buffer.length).replace(" ","").trim();
//                String patternStr = "AA.{4}1C";
                String patternStr = "AA";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(inHex);
                if (startCode == 0xaa && (cal_chksum(buffer) == chkSum)) 
                {
                    if (previousPkt != pktNum) 
                    {
                        ax = inertialData[0];
                        ay = inertialData[1];
                        az = inertialData[2];
                        gx = inertialData[3];
                        gy = inertialData[4];
                        gz = inertialData[5];
                        if (Constants.CUBE_ANIMATION)
                        {
                            filter_data = madgwick.MadgwickAHRSupdateIMU(gx, gy, gz, ax, ay, az);
                            panel.setQuaterniunData(filter_data);
                        }
                        if (Constants.REAL_TIME_PLOT)
                        {
                            realTimePlot.addData(new AccGyro(pktNum, timeStamp, ax, ay, az, gx, gy, gz));
                        }
                        
                        timeStamp = pkt_receive /Constants.OUTRATE;
                        counterNormal = 0;
                        pkt_receive++;
                        previousPkt = pktNum;
                        if (Constants.DATA_LOG && dataLogger != null) 
                        {
                            dataLogger.addData(new AccGyro(pktNum, timeStamp, ax, ay, az, gx, gy, gz));
                        }
                    }
                    buffer = final_buffer(Constants.DATA_LEN);
                }
                else if(matcher.find())
                {
                    int index = matcher.start();
                    String strrem = (String) inHex.subSequence(index, inHex.length());
                    lenght = strrem.length();
                    if (lenght < (Constants.DATA_LEN*2)) 
                    {
                        newlen = (Constants.DATA_LEN*2) - lenght;
                        buffer = final_buffer(newlen / 2);
                        String buffer_str = strrem + Utilities.byte2HexStr(buffer, buffer.length).replace(" ", "").trim();
                        try 
                        {
                            buffer = Utilities.hexStringToByteArray(buffer_str);
                        }
                        catch (StringIndexOutOfBoundsException e){
                            buffer = final_buffer(Constants.DATA_LEN);
                        }
                    }
                    else
                        buffer = final_buffer(Constants.DATA_LEN);
                }
                else
                {
                    buffer = final_buffer(Constants.DATA_LEN);
                    counterNormal ++;
                    if (counterNormal > 5)
                    {
                        normal_imu = false;
                        break;
                    }
                }
            }
        }
    }

    public byte[] final_buffer(int len)
    {
        byte[] temp = new byte[Constants.DATA_LEN];
        try 
        {
            temp = connectivity.receive(len);
//            System.out.println("read: "+ Utilities.byteArrayToString(temp, temp.length));
//            temp = read_data(len);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            try 
            {
                Thread.sleep(5);
//                temp = read_data(len);
                temp = connectivity.receive(len);
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
        }
        return temp;
    }


    public void parse_data(byte[] buffer)
    {
        byte temp[] = new byte[4];
        int j = 0;
        if (buffer.length == Constants.DATA_LEN) 
        {
            for (int i = 0; i < 4; i++) 
            {
                header[i] = buffer[j++] & 0xFF;             //header define
            }
            startCode = header[0];                        // Receive AA
//            pktNum = (header[1] << 8) | header[2];
            pktNum = (header[1])*255 + header[2];
            payloadLen = header[3];                      // Payload Size

            if (startCode == 0xAA && payloadLen == 28) 
            {
                for (int i = 0; i < 4; i++) 
                {
                    temp[i] = buffer[j++];             //time_stamp define
                }

//            timeStamp = (ByteBuffer.wrap(temp).getFloat())/64e6d;
//                timeStamp = (utility.getUInt32(temp) / 64e6);
                for (int i = 0; i < Constants.AXIS; i++) 
                {
                    for (int k = 0; k < Constants.BYTE_VAL; k++) 
                    {
                        temp[k] = buffer[j++];
                    }
                    inertialData[i] = ByteBuffer.wrap(temp).getFloat();    // x,y,z acc, gyro
                }
                chkSum = ((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xFF);
            }
        }
    }

    private int cal_chksum(byte[] data)
    {
        int checksum = 0;

        for (int i =0; i < data.length - 2; i++)
        {
            checksum += data[i] & 0xFF;
        }
        return checksum;
    }

    @Override
    public void run() {

        if (normal_imu)
            {
                get_plot_normal();
            }

    }
    
}

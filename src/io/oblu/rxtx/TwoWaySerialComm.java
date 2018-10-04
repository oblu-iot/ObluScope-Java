/*
 *   Copyright (C) 2008 X-IO Technologies
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
 * Modifications made by GT Silicon Pvt Ltd are within the following
comments:
 * // BEGIN - Added by GT Silicon - BEGIN //
 * {Code included or modified by GT Silicon}
 * // END - Added by GT Silicon - END //
*
* */

/*
 * Below is a simple program that shows how to open a connection to a serial device and
 * then interact with it (receiving data and sending data). One thing to note is that
 * the package gnu.io is used instead of javax.comm
 */
package io.oblu.rxtx;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class TwoWaySerialComm
{
    // BEGIN - Added by GT Silicon - BEGIN //
    OutputStream mBufferOut = null;
    InputStream mBufferIn = null;
    String configData = "";
    static File nonPDRFile=null;
    // END - Added by GT Silicon - END //
    
    public TwoWaySerialComm(String sample_freq)
    {
        super();
        // BEGIN - Added by GT Silicon - BEGIN //
        nonPDRFile = Utilities.createNewFile(sample_freq);
        // END - Added by GT Silicon - END //
    }
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = Utilities.initialize(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(Constants.BAUDRATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                mBufferIn = serialPort.getInputStream();
                mBufferOut = serialPort.getOutputStream();
                
                (new Thread(new SerialReader(mBufferIn))).start();
                (new Thread(new SerialWriter(mBufferOut))).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }

// BEGIN - Added by GT Silicon - BEGIN //
    public void sendData(byte[] data) {
        try{
            if (mBufferOut != null ) {
                mBufferOut.write(data);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }       
// END - Added by GT Silicon - END //

    /** */
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        @Override
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    // BEGIN - Added by GT Silicon - BEGIN //
                    String strBuf = Utilities.byteArrayToString(buffer, len);
                    ParseData.mQueue.add(strBuf.replace(" ","").trim());
                    // END - Added by GT Silicon - END //
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }
// BEGIN - Added by GT Silicon - BEGIN //
    public static class RunInBackGround implements Runnable
    {
        ParseData start = new ParseData();

        @Override
        public void run()
        {
            if (ParseData.normal_imu)
            {
                start.get_plot_normal();
            }
        }
    }
// END - Added by GT Silicon - END //
    
    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        @Override
        public void run ()
        {
            try
            {                
                int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }
    
    public static void main ( String[] args )
    {
         // BEGIN - Added by GT Silicon - BEGIN //
        try
        {
            Properties prop = new Properties();
            String propFileName = "src/config.properties";
                        
            if (propFileName == null)
            {
                throw new FileNotFoundException("Properties File " + propFileName + " not found in the classpath");
            }
            else
            {
                prop.load(new FileInputStream(propFileName));
            }
            
            System.out.println(prop.values());
            
            Constants.OUTRATE = Float.valueOf(prop.getProperty("com.rotate.outrate","250"));
            Constants.BAUDRATE = Integer.valueOf(prop.getProperty("com.rotate.serialbaudrate","115200"));
            Constants.SHAPE = prop.getProperty("com.rotate.shape","CUBE");
            Constants.SERIAL_PORT = prop.getProperty("com.rotate.serialport","/dev/ttyACM0");
            Constants.texture = prop.getProperty("com.rotate.texture","oblu.bmp");
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
            String samp_freq = prop.getProperty("com.rotate.outrate","250");
            float outrate = 1000 / Float.parseFloat(samp_freq);
            int[] hexval = new int[]{0x01, 0x02, 0x3, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
            int outrate_cmd = hexval[Utilities.get_ratedevider(outrate)];
            int checksum = 0x41 + outrate_cmd;
            String send = String.format("0x41 0x%02x 0x00 0x%02x", outrate_cmd, checksum);//0x40 0x05 0x00 0x45
            System.out.println("Command:"+send);    
            
            ParseData.normal_imu = true;
            new Thread(new RunInBackGround()).start();
            TwoWaySerialComm twoWaySerialComm = new TwoWaySerialComm(samp_freq);
            twoWaySerialComm.connect(Constants.SERIAL_PORT);
            twoWaySerialComm.sendData(Utilities.convertingTobyteArray(send));
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // END - Added by GT Silicon - END //
    }

}
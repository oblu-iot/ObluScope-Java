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
 * Below is a simple program that shows how to init a connection to a serial device and
 * then interact with it (receiving data and sending data). One thing to note is that
 * the package gnu.io is used instead of javax.comm
 */
package io.oblu.commn;

import io.oblu.commn.connectivity.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main
{
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
            
            Constants.CONNECTIVITY_TYPE = prop.getProperty("com.connectivity.type","usb");
            Constants.OUTRATE = Float.valueOf(prop.getProperty("com.rotate.outrate","250"));
            Constants.BAUDRATE = Integer.valueOf(prop.getProperty("com.rotate.serialbaudrate","115200"));
            Constants.SHAPE = prop.getProperty("com.rotate.shape","CUBE");  
            Constants.texture = prop.getProperty("com.rotate.texture","oblu.bmp");
            Constants.SERIAL_PORT = prop.getProperty("com.rotate.serialport","/dev/ttyACM0");
            Constants.MAC_ADDRESS = prop.getProperty("com.rotate.mac.addr");
            String samp_freq = prop.getProperty("com.rotate.outrate","250");
            float outrate = 1000 / Float.parseFloat(samp_freq);
            int[] hexval = new int[]{0x01, 0x02, 0x3, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
            int outrate_cmd = hexval[Utilities.get_ratedevider(outrate)];
            int checksum = 0x41 + outrate_cmd;
            String send = String.format("0x41 0x%02x 0x00 0x%02x", outrate_cmd, checksum);//0x40 0x05 0x00 0x45
            System.out.println("Command:"+send);   
            ParseNormalImu.normal_imu = true;
            Connectivity main = Utilities.get_connectivity();
            if (main == null) {
                return;
            }
            main.open();
            main.send(send);
            Thread thr = new Thread(new ParseNormalImu(main));
            thr.start();
//            thr.join();
        }
        catch ( IOException | NumberFormatException e )
        {
            // TODO Auto-generated catch block

        }
        // END - Added by GT Silicon - END //
    }

}
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
 * Note that the CCBY4.0 license is applicable only for the modifications made
 * by GT Silicon Pvt Ltd
 *
*
* */


package io.oblu.rxtx;


import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class DataLogger extends Thread{
    
    private File logFile;
    private ConcurrentLinkedQueue<AccGyro> mData;
    private boolean bStart = false; 
    private DecimalFormat df = new DecimalFormat("0.00");
    private DecimalFormat dfTime = new DecimalFormat("0.000");
    
    public DataLogger(File logFile) {
        this.logFile = logFile;
        mData = new ConcurrentLinkedQueue<>();
    }
    
    public void addData(AccGyro accGyro){
        mData.add(accGyro);
    }
    
    @Override
    public void run() {
        bStart = true;
        try{
            FileWriter writer = new FileWriter(this.logFile,true);
            
            while(bStart){
                for (AccGyro accGyro = mData.poll(); accGyro != null; accGyro = mData.poll())
                {
                    String toFile = String.format("%12s \t %12s \t %12s \t %12s \t %12s \t %12s \t %12s \t %12s \n ",
                            String.valueOf(accGyro.pkt_num), dfTime.format(accGyro.timestamp), df.format(accGyro.ax), 
                            df.format(accGyro.ay), df.format(accGyro.az), df.format(accGyro.gx), df.format(accGyro.gy), 
                            df.format(accGyro.gz));
                    writer.append(toFile);
                    writer.flush();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DataLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            writer.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
    }
  
    
    public void stopLogging(){
        try {
            Thread.sleep(500);     
        } catch (InterruptedException ex) {
            Logger.getLogger(DataLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        bStart = false;
    }
}

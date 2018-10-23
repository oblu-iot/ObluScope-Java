/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and init the template in the editor.
 */
package io.oblu.commn.connectivity;

import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import io.oblu.commn.Utilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 *
 * @author gts-pc-1
 */


public class BLECommunication extends Connectivity{
    private  final String[] linuxPromptRegEx = new String[]{"\\>","\\[.*\\]>"};
    private Expect4j expect4j = null;
    private boolean isConnected = false;
//    private final String toPharse = "";
    private int waitingTime = 0;
    private String ble_buffer = "";
    public  boolean bleThreadStop = false;
    private final ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<>();
    private enum CMD_PATTERN {GATTTOOL, CONNECTION, MTU, CHARACTERISTIC, NOTIFICATION, ERROR;}
    CMD_PATTERN currentCMD = CMD_PATTERN.GATTTOOL;
    private String mac_addr;
    
    public BLECommunication(String mac_addr){
        this.mac_addr = mac_addr;
        this.init();
    }
    
    @Override
    public void init(){
        try {
            //dataLogger = Utilities.createNewFile(sample_freq);
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "gatttool -I");
            Process pro = builder.start();
            expect4j = new Expect4j(pro);
            expect4j.setDefaultTimeout(3000L);
        } catch (IOException ex) {
            Logger.getLogger(BLECommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void findContent(String data, String bleAdd) throws IOException, InterruptedException{

        switch(currentCMD){
            case GATTTOOL:
                if(data.contains("[                 ][LE]>")){
                    expect4j.send(String.format("connect %s", bleAdd));
                    expect4j.send("\r");
                    currentCMD = CMD_PATTERN.CONNECTION;
                    System.out.println("connecting");
                }
                break;
            case CONNECTION:
                if (data.contains("Connection successful")){
                    expect4j.send("mtu 512");
                    expect4j.send("\r");
                    currentCMD = CMD_PATTERN.MTU;
                    System.out.println("MTU cmd");
                }
                break;
            case MTU:
                if (data.contains("MTU")){
                    expect4j.send("char-write-req 0x000f 01");
                    expect4j.send("\r");
                    currentCMD = CMD_PATTERN.CHARACTERISTIC;
                    System.out.println("Notification Enable");
                }
                break;
            case CHARACTERISTIC:
                if (data.contains("Characteristic")){
//                    send(cmd);
//                    (new Thread(new Main.RunInBackGround())).start();
                    currentCMD = CMD_PATTERN.ERROR;
                    isConnected = true;
                }
                break;
            case ERROR:
                if (data.contains("error")){
                    expect4j.send("exit");
                    expect4j.send("\r");
                    System.err.println("Error : Please Restart a Device");
                }
                break;
            default:
                close();
                break;
        }
    }
    
    @Override
    public void open(){
        List <Match> lstPattern = dataHandaling(this.mac_addr);
//        int waitingTime = 0;
        while(!isConnected)
        {
            initConection(lstPattern);  
        }
//        send(send);
        new Thread(new BleReader(lstPattern)).start();
        
//        return lstPattern;
    }
    
    @Override
    public void close(){
        try {
            expect4j.send("exit");
            expect4j.send("\r");
            bleThreadStop = true;
            System.err.println("Device Disconnected");
            } catch (IOException ex) {
                Logger.getLogger(BLECommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private List dataHandaling(String bleAdd){
        
        List<Match> lstPattern =  new ArrayList<>();
        Closure closure = (ExpectState expectState) -> 
        {
            String toPharse = expectState.getBuffer().trim();
//            System.out.println("receive "+toPharse);
            if (toPharse.contains("Notification")) 
            {
                toPharse = toPharse.replaceAll("Notification handle = 0x000e value:", "")
                                    .replaceAll("\\[.{20,50}>", "")
                                    .replaceAll("\\W", "");
                if (toPharse.length() > 0){
//                    System.out.println(toPharse);
                    mQueue.add(toPharse);
                    waitingTime = 0;
                }
            }
            else
            {
                findContent(toPharse, bleAdd);
            }
        };
        for (String regexElement : linuxPromptRegEx) {
                try {
                    Match mat = new RegExpMatch(regexElement, closure);
                    lstPattern.add(mat);
                }
                 catch(MalformedPatternException e) {
                }
        }
        return lstPattern;
    }
    
    @Override
    public void send(String data){
        try{
            if (data != null ) {
                String bleSend = String.format("char-write-cmd 0x0011 %s", data.replaceAll("0x", "").replaceAll(" ", ""));
                expect4j.send(bleSend);
                expect4j.send("\r");
//                Thread.sleep(5);
                System.out.println("send cmd " +data);
            }
        }catch (IOException ex){
        }
    }       
    
    
    @Override
    public byte[] receive(int len)
    {
        
        byte[] byt = new byte[1];
        len = len * 2;
        while ((ble_buffer.length() < (len*2)))
        {
            String data_from_queue = mQueue.poll();
            if (data_from_queue != null) 
            {
//                System.out.println("data_from_queue : "+data_from_queue);
                ble_buffer += data_from_queue;
            }
       }
        if (ble_buffer.length() > len)
        {
            String take_str =  ble_buffer.substring(0,len);
            byt = Utilities.hexStringToByteArray(take_str);
            try 
            {
                ble_buffer = ble_buffer.substring(len, ble_buffer.length());
            }
            catch (Exception e)
            {
            }
        }
        return byt;
    }

    
    
    
    protected void initConection(List lstPattern){
        try {
            int ret_code = expect4j.expect(lstPattern);
            if (ret_code == -2) {
                waitingTime += 3;
            }
            if(waitingTime > 20){
//                close();
            }
            Thread.sleep(1);
        } catch (Exception ex) {
            Logger.getLogger(BLECommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    class BleReader implements Runnable
    {
//        BLECommunication bleConnication;
        List<Match> lstPattern =  new ArrayList<>();
        
        public BleReader(List pattern) {
            this.lstPattern = pattern;
        }
        @Override
        public void run() {
            while(!bleThreadStop){
                try 
                {
                    int ret_code = expect4j.expect(lstPattern);
                    if (ret_code == Expect4j.RET_TIMEOUT) {
                        waitingTime += 3;
                    }
                    if(waitingTime > 20){
                        close();
                    }
                    Thread.sleep(1);
                } 
                catch (Exception ex) {
                }
            }
        }
    }
}

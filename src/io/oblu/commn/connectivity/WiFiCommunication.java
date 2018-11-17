/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.oblu.commn.connectivity;

import io.oblu.commn.Constants;
import io.oblu.commn.Utilities;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author gts-pc-1
 */
public class WiFiCommunication extends Connectivity{

    private String ipAddr = "";
    private TcpClient mTcpClient;
    private final ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue();
    private String wifi_buffer = "";
    
    
    public WiFiCommunication(String addr) {
        this.ipAddr = addr;
        this.init();
    }
    
    @Override
    public void init() {
        if(mTcpClient != null){
                mTcpClient.stopClient();
                mTcpClient = null;
            }
            mTcpClient = new TcpClient(ipAddr, Constants.SERVERPORT);
    }

    @Override
    public void open() {
//        mTcpClient.setDataQueue(this.mQueue);
        mTcpClient.connectClient();
        boolean isConnected = mTcpClient.isConnected();
        System.out.println(mTcpClient.getDevice_ip() +"  Conection Status : "+isConnected);
        if (!isConnected)
        {
            System.exit(-1);
        }
            mTcpClient.setMessageListener(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(byte[] message1, Integer dataLen) {
                    //this method calls the onProgressUpdate
                    if (message1 != null && message1.length > 0) {
                        String getdata = Utilities.byteArrayToString(message1, message1.length);
                        mQueue.add(getdata);
                    }
//                    System.out.println("messagereceived : "+ Utilities.byteArrayToString(message1, message1.length));
//                    System.out.println("messagereceived : "+getdata);    
                }
            });
            mTcpClient.start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void close() {
        mTcpClient.stopClient();
    }

    @Override
    public void send(String data) {
        byte[] cmd = Utilities.convertingTobyteArray(data);
        mTcpClient.sendData(cmd);
    }

    @Override
    public byte[] receive(int len) {
        byte[] byt = new byte[1];
        len = len * 2;
        while ((wifi_buffer.length() < (len*2)))
        {
            String data_from_queue = mQueue.poll();
            if (data_from_queue != null) 
            {
//                System.out.println("data_from_queue : "+data_from_queue);
                wifi_buffer += data_from_queue;
            }
       }
        if (wifi_buffer.length() > len)
        {
            String take_str =  wifi_buffer.substring(0,len);
            byt = Utilities.hexStringToByteArray(take_str);
            try 
            {
                wifi_buffer = wifi_buffer.substring(len, wifi_buffer.length());
            }
            catch (Exception e)
            {
            }
        }
        return byt;
    }
    
}

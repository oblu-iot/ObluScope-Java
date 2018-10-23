/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.oblu.commn.connectivity;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import io.oblu.commn.Constants;
import io.oblu.commn.Utilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gts-pc-1
 */
public class USBComunication extends Connectivity
{
    // BEGIN - Added by GT Silicon - BEGIN //
    String buffer = "";
    OutputStream mBufferOut = null;
    InputStream mBufferIn = null;
    String configData = "";
    SerialPort serialPort;
    String portName;
    ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<>();
//    static DataLogger dataLogger = null;
    // END - Added by GT Silicon - END //
    
    public USBComunication(String portName)
    {
        super();
        this.portName = portName;
        // BEGIN - Added by GT Silicon - BEGIN //
//            dataLogger = Utilities.createNewFile(sample_freq);
        // END - Added by GT Silicon - END //
    }
    
    @Override
    public void open()
    {
        CommPortIdentifier portIdentifier = Utilities.initialize(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            try {
                CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
                
                if ( commPort instanceof SerialPort )
                {
                    serialPort = (SerialPort) commPort;
                    try {
                        serialPort.setSerialPortParams(Constants.BAUDRATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                    } catch (UnsupportedCommOperationException ex) {
                        Logger.getLogger(USBComunication.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    try {
                        mBufferIn = serialPort.getInputStream();
                    } catch (IOException ex) {
                        Logger.getLogger(USBComunication.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        mBufferOut = serialPort.getOutputStream();
                    } catch (IOException ex) {
                        Logger.getLogger(USBComunication.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    (new Thread(new SerialReader(mBufferIn))).start();
                    (new Thread(new SerialWriter(mBufferOut))).start();
                    
                }
                else
                {
                    System.out.println("Error: Only serial ports are handled by this example.");
                }
            } catch (PortInUseException ex) {
                Logger.getLogger(USBComunication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }     
    }

// BEGIN - Added by GT Silicon - BEGIN //
    @Override
    public void send(String cmd) {
        byte[] data = Utilities.convertingTobyteArray(cmd);
        try{
            if (mBufferOut != null ) {
                mBufferOut.write(data);
            }
        }catch (IOException ex){
        }
    }       
// END - Added by GT Silicon - END //

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
//        serialPort.close();
        System.err.println("Device Disconnected");
    }

    @Override
    public byte[] receive(int len)
    {
        
        byte[] byt = new byte[1];
        len = len * 2;
        while ((buffer.length() < (len*2)))
        {
            String data_from_queue = mQueue.poll();
            if (data_from_queue != null) 
            {
//                System.out.println("data_from_queue : "+data_from_queue);
                buffer += data_from_queue;
            }
       }
        if (buffer.length() > len)
        {
            String take_str =  buffer.substring(0,len);
            byt = Utilities.hexStringToByteArray(take_str);
            try 
            {
                buffer = buffer.substring(len, buffer.length());
            }
            catch (Exception e)
            {
            }
        }
        return byt;
    }


    /** */
    class SerialReader implements Runnable 
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
                    mQueue.add(strBuf.replace(" ","").trim());
                    // END - Added by GT Silicon - END //
                }
            }
            catch ( IOException e )
            {
            }            
        }
    }
    
    /** */
    class SerialWriter implements Runnable 
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
            }            
        }
    }
}
 

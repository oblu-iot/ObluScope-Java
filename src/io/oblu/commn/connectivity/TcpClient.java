package io.oblu.commn.connectivity;



import io.oblu.commn.Constants;
import io.oblu.commn.Utilities;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Queue;

/**
 * Description
 *
 * @author Catalin Prata
 *         Date: 2/12/13
 */
public class TcpClient extends Thread{
    private final static String TAG = TcpClient.class.getSimpleName();
//    public static final String SERVER_IP = "192.168.43.156"; //your computer IP address
//    public static final int SERVER_PORT = 4444;
    // message to send to the server
    private String device_ip;
    private int port;
    private byte[] mServerData;

    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private OutputStream mBufferOut;
    // used to read messages from the server
    private InputStream mBufferIn;
    public boolean bNeedCheckAck = false;
    private Socket mSocket;
    private Queue<String> queue;
    private Queue mQueue;
    

    public TcpClient(String device_ip, int port) {
        this.device_ip = device_ip;
        this.port = port;
    }

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public void setMessageListener(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param data send command to device
     */
    public void sendData(byte[] data) {
        try{
            if (mBufferOut != null ) {
//                mBufferOut.write(TcpUtilities.convertingTobyteArray(data));
                mBufferOut.write(data);
            }
        }catch (Exception ex){
//            Log.getLog(TAG, ex.getMessage());
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        try{
            // send mesage that we are closing the connection
            mRun = false;
            try{
                if (mBufferOut != null) {
                    mBufferOut.flush();
                    mBufferOut.close();
                }
            }catch (Exception ex){
//                Log.i(TAG, ex.getMessage());
            }
            mMessageListener = null;
            mBufferIn = null;
            mBufferOut = null;
            mServerData = null;
            if(mSocket!=null){
                this.mSocket.close();
            }
            mSocket = null;
        }catch (Exception ex){
        }
    }


    public void connectClient(){
        try{
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(device_ip);
            //create a mSocket to make the connection with the server
            this.mSocket = new Socket(serverAddr, port);
            this.mSocket.setSoTimeout(Constants.CONNECTION_TIME_OUT);
        }catch(Exception ex){
//            Log.e(TAG, "C: Error", ex);
            this.mSocket = null;
        }
    }

    public boolean isConnected(){
        return this.mSocket!=null;
    }

    public void run() {
//        try {
//            //here you must put your computer's IP address.
//            InetAddress serverAddr = InetAddress.getByName(device_ip);
//
//            //create a mSocket to make the connection with the server
            boolean isStillActive = true;
            try {
                if(mSocket==null) {
                    return;
                }
                mRun = true;
                //sends the message to the server
                mBufferOut = mSocket.getOutputStream();

                //receives the message which the server sends back
                mBufferIn = mSocket.getInputStream();
//                Log.e(TAG, "Waiting for data");
                mServerData = new byte[Constants.DATA_LEN];
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    try{
                        int rcvdBytes = mBufferIn.read(mServerData);
                        if (mServerData != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            // Check whether it is acknowledgement
                            // if acknowledgement then ignore it
                            // otherwise broadcast it
                            if(mQueue!=null ){
                                mQueue.add(Utilities.byteArrayToString(mServerData, rcvdBytes));
                            }
                            mMessageListener.messageReceived(mServerData,rcvdBytes);
                           
                        }
                    }catch (SocketTimeoutException timeoutEx){
                        System.out.println("Time OUT");
                        isStillActive = isDeviceActive(device_ip);
                        if(!isStillActive){
                            mMessageListener.messageReceived(Constants.disconnection_cmd,Constants.disconnection_cmd.length);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                //the mSocket must be closed. It is not possible to reconnect to this mSocket
                // after it is closed, which means a new mSocket instance has to be created.
                try{
                    if(mSocket!=null)
                        mSocket.close();
                }catch (Exception ex){
//                    Log.e(TAG, " S: Error", ex);
                }
            }
    }

    public Queue<String> getQueue() {
        return queue;
    }

    public String getData(int dataLen) {
        return queue.remove();
    }

    public static boolean isDeviceActive(String ipAddress){
        try {
            InetAddress addr;
            try (Socket sock = new Socket(ipAddress, Constants.SERVERPORT)) {
                sock.setSoTimeout(Constants.CONNECTION_CHK_TIMEOUT);
                addr = sock.getInetAddress();
                System.out.println("Connected to " + addr);
            }
            return true;
        } catch (java.io.IOException e) {
            System.out.println("Can't connect to " + ipAddress);
            System.out.println(e);
        }
        return false;
    }

    public void setDataQueue(Queue queue) {
        this.mQueue = queue;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(byte[] data, Integer dataLen);
    }

    public String getDevice_ip() {
        return device_ip;
    }
}

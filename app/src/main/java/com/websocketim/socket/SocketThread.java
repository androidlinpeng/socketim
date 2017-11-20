package com.websocketim.socket;

import android.util.Log;

import com.websocketim.utils.LogUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2017/9/18.
 */

public class SocketThread {

    private static final String TAG = "SocketThread";

    private Socket socket;
    private String ip;
    private String port;
    private InetSocketAddress isa;
    private DataOutputStream DOS;
    private DataInputStream DIS;
    public SocketMap smMap;

    private static SocketThread mInstance;

    public static SocketThread getInstance() {
        if (mInstance == null) {
            Log.d(TAG, "getInstance: null ");
            mInstance = new SocketThread();
        }
        return mInstance;
    }

    public boolean SocketStart(String myip, String myport, String type) {
        this.ip = myip;
        this.port = myport;
        socket = new Socket();
        isa = new InetSocketAddress(ip, Integer.parseInt(port));
        try {
            socket.connect(isa, 60*1000);
            LogUtil.d(TAG, "连接成功 " + socket);
            smMap = new SocketMap();
            smMap.setSocket(type, socket);
            return true;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "连接超时 " + e.getMessage());
            return false;
        }catch (UnknownHostException e){
            e.printStackTrace();
            LogUtil.d(TAG, "连接失败 " + e.getMessage());
            return false;
        }catch (IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "连接失败 " + e.getMessage());
            return false;
        }
    }

    public DataOutputStream getDOS() throws IOException {
        DOS = new DataOutputStream(this.socket.getOutputStream());
        return DOS;
    }

    public DataInputStream getDIS() throws IOException {
        DIS = new DataInputStream(this.socket.getInputStream());
        return DIS;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setIP(String setip) {
        this.ip = setip;
    }

    public void setPort(String setport) {
        this.port = setport;
    }

    //本地服务器是否连接
    public boolean isConnected() {
        return socket.isConnected();
    }

    public void CloseSocket(String type) {
        smMap.removeMap(type);
    }

    public void AllClose() {
        smMap.clearMap();
    }
}
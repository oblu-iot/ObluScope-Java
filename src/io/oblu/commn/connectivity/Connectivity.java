/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.oblu.commn.connectivity;


/**
 *
 * @author gts-pc-1
 */
public abstract class Connectivity{

    public abstract void init();
    public abstract void open();
    public abstract void close();
    public abstract void send(String data);
    public abstract byte[] receive(int len);
}

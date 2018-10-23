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
package io.oblu.commn;


public class AccGyro {
    int pkt_num;
    double timestamp;
    float ax, ay, az;
    float gx, gy, gz;

    public AccGyro(int pkt_num, double timestamp, float ax, float ay, float az, 
            float gx, float gy, float gz) {
        this.pkt_num = pkt_num;
        this.timestamp = timestamp;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.gx = gx;
        this.gy = gy;
        this.gz = gz;
    }
    
}

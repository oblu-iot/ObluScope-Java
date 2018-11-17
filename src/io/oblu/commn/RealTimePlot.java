/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.oblu.commn;

import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.SwingWorker;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 *
 * @author GTS-2
 */
public class RealTimePlot extends SwingWorker<Boolean, AccGyro[]>{

    XYChart chart;
    SwingWrapper<XYChart> sw;
    static ConcurrentLinkedDeque<AccGyro> mQueue = new ConcurrentLinkedDeque<>();
    LinkedList<AccGyro> fifo = new LinkedList<>();
    List<XYChart> charts = new ArrayList<>();
    
    int numCharts = 2;
    public RealTimePlot() {
        for (int i = 0; i < numCharts; i++) {
            if (i == 0){
                chart = new XYChartBuilder().yAxisTitle("a (m/s^2)").xAxisTitle("time(s)").width(700).height(300).build();
                XYSeries series1 = chart.addSeries("ax", null, new double[] { 0 });
                XYSeries series2 = chart.addSeries("ay", null, new double[] { 0 });
                XYSeries series3 = chart.addSeries("az", null, new double[] { 0 });
                series1.setMarker(SeriesMarkers.NONE);
                series2.setMarker(SeriesMarkers.NONE);
                series3.setMarker(SeriesMarkers.NONE);
            }
            if (i == 1){
                chart = new XYChartBuilder().yAxisTitle("omega (deg/s)").xAxisTitle("timespan(s)").width(700).height(300).build();
                XYSeries series4 = chart.addSeries("gx", null, new double[] { 0 });
                XYSeries series5 = chart.addSeries("gy", null, new double[] { 0 });
                XYSeries series6 = chart.addSeries("gz", null, new double[] { 0 });
                series4.setMarker(SeriesMarkers.NONE);
                series5.setMarker(SeriesMarkers.NONE);
                series6.setMarker(SeriesMarkers.NONE);
            }
            chart.getStyler().setPlotMargin(10);
            chart.getStyler().setLegendFont(new Font(Font.SERIF, Font.PLAIN, 12));
            chart.getStyler().setLegendPosition(LegendPosition.InsideSE);
            chart.getStyler().setLegendSeriesLineLength(12);
            chart.getStyler().setAxisTitleFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
            chart.getStyler().setAxisTickLabelsFont(new Font(Font.SERIF, Font.PLAIN, 11));
            chart.getStyler().setDecimalPattern("#0.00");
            chart.getStyler().setLocale(Locale.US);
            charts.add(chart);
        }
        sw = new SwingWrapper<>(charts,2, 1);
        sw.displayChartMatrix();
    }
    
    
    public void addData(AccGyro accGyro){
        mQueue.add(accGyro);
    }
    
    
    @Override
    protected Boolean doInBackground() throws Exception {
        long milli_second = 1;
        while (!isCancelled()) {
            for (AccGyro accGyro = mQueue.poll(); accGyro != null; accGyro = mQueue.poll())
            {
                fifo.add(accGyro); 
                if (fifo.size() > 500) {
                  fifo.removeFirst();
                }
                AccGyro[] array = new AccGyro[fifo.size()];
                for (int i = 0; i < fifo.size(); i++) {
                  array[i] = fifo.get(i);
                }
                publish(array);
                try {
                  Thread.sleep(milli_second);
                } catch (InterruptedException e) {
                  // eat it. caught when interrupt is called
                  System.out.println("MySwingWorker shut down.");
                }     
            } 
        }
      return true;
    }
    
    @Override
    protected void process(List<AccGyro[]> chunks) {
//        System.out.println("number" + chunks.size());
        AccGyro[] accGyro = chunks.get(chunks.size() - 1);
        double[] ax = new double[accGyro.length];
        double[] ay = new double[accGyro.length];
        double[] az = new double[accGyro.length];
        double[] gx = new double[accGyro.length];
        double[] gy = new double[accGyro.length];
        double[] gz = new double[accGyro.length];
        
        for (int i = 0; i < accGyro.length; i++) {
            ax[i] = accGyro[i].ax;
            ay[i] = accGyro[i].ay;
            az[i] = accGyro[i].az;
            gx[i] = accGyro[i].gx;
            gy[i] = accGyro[i].gy;
            gz[i] = accGyro[i].gz;
        }
        for (int i = 0; i < numCharts; i++) {
            if (i == 0){
                charts.get(i).updateXYSeries("ax", null, ax, null);
                charts.get(i).updateXYSeries("ay", null, ay, null);
                charts.get(i).updateXYSeries("az", null, az, null);
            }
            if (i == 1){
                charts.get(i).updateXYSeries("gx", null, gx, null);
                charts.get(i).updateXYSeries("gy", null, gy, null);
                charts.get(i).updateXYSeries("gz", null, gz, null);
            }
            sw.repaintChart(i);
        }
        long start = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - start;
        try {
          Thread.sleep(40 - duration); // 40 ms ==> 25fps
          // Thread.sleep(400 - duration); // 40 ms ==> 2.5fps
        }
        catch (InterruptedException e) {
            System.err.println("Error: "+e);
        }
    }
}
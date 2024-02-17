package com.example.asan_sensor;
import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Kalman {
    KalmanFilter filter;

    public Kalman(double initial_value){
        //initial value
        double V = initial_value;

        double mn = 0.1;
        double pn = 0.1;

        RealMatrix A = new Array2DRowRealMatrix(new double[] { 1 });
        RealMatrix H = new Array2DRowRealMatrix(new double[] { 1 });
        RealVector x = new ArrayRealVector(new double[] { V });
        RealMatrix Q = new Array2DRowRealMatrix(new double[] { pn });
        RealMatrix P0 = new Array2DRowRealMatrix(new double[] { 1 });
        RealMatrix R = new Array2DRowRealMatrix(new double[] { mn });

        ProcessModel pm = new DefaultProcessModel(A, null, Q, x, P0);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        this.filter = new KalmanFilter(pm, mm);
    }

    public double do_calc(double rssi){
        this.filter.predict();
        double[] result = filter.getStateEstimation();
        this.filter.correct(new double[] {rssi});
        return result[0];
    }
}

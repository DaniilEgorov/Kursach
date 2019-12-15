package pack_work.graph;

import javafx.scene.chart.XYChart;
import pack_log.Connection_database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Trend extends Connection_database {
    ArrayList<Double> y;
    ResultSet rs;
    XYChart.Series<String, Number> trend;
    double A_sr;
    String func;
    public Trend (ArrayList<Double> y, ResultSet rs, XYChart.Series<String, Number> trend){
        this.y=y;
        this.rs=rs;
        this.trend=trend;
    }
    public XYChart.Series<String, Number> linear() throws SQLException {
        Operation_copy operation1 = (double k, int p) -> Math.pow(k,p);
        Operation operation2 = (double k) -> k;
        double [] x = solving(operation1,operation2,2);
        Operation operation = (double k) -> x[0]*k+x[1];
        func = String.format("y = %f*x + %f",x[0],x[1]);
        return setTrend(operation);
    }
    public XYChart.Series<String, Number> quadratic() throws SQLException {
        Operation_copy operation1 = (double k, int p) -> Math.pow(k,p);
        Operation operation2 = (double k) -> k;
        double [] x = solving(operation1,operation2,3);
        Operation operation = (double k) -> x[0]*k*k+x[1]*k+x[2];
        func = String.format("y = %f*x^2 + %f*x + %f",x[0],x[1],x[2]);
        return setTrend(operation);
    }
    public XYChart.Series<String, Number> logarithmic() throws SQLException {
        Operation_copy operation1 = (double k, int p) -> Math.pow(Math.log(k),p);
        Operation operation2 = (double k) -> k;
        double [] x = solving(operation1,operation2,2);
        Operation operation = (double k) -> x[0]*Math.log(k)+x[1];
        func = String.format("y = %f*lnx + %f",x[0],x[1]);
        return setTrend(operation);
    }
    public XYChart.Series<String, Number> exponential() throws SQLException {
        Operation_copy operation1 = (double k, int p) -> Math.pow(k,p);
        Operation operation2 = (double k) -> Math.log(k);
        double [] x = solving(operation1,operation2,2);
        x[1] = Math.exp(x[1]);
        Operation operation = (double k) -> x[1]*Math.exp(x[0]*k);
        func = String.format("y = %.2f*e^(%.2fx)",x[1],x[0]);
        return setTrend(operation);
    }
    public double [] solving(Operation_copy x, Operation y1, int p){
        int k = p-1;
        double [][] A = new double[p][p];
        double [] b = new double[p];
        for (int i = 1; i < y.size()+1; i++) {
            for (int j = 0; j < A.length; j++) {
                A[0][j]+=x.f(i,2*k-j);
                b[j]+=x.f(i,k-j)*y1.f(y.get(i-1));
            }
            for (int j = 1; j < A.length; j++) {
                A[j][k]+=x.f(i,k-j);
            }
        }
        for (int i = 1; i < A.length; i++) {
            for (int j = 0; j < A[i].length-1; j++) {
                A[i][j] = A[i-1][j+1];
            }
        }
        return gauss(A,b);
    }
    public double [] gauss(double[][] A, double [] b){
        for (int i = 0; i < A.length; i++) {
            double k = A[i][i];
            b[i]/=k;
            for (int j = i; j < A[i].length; j++) {
                A[i][j]/=k;
            }
            for (int j = i+1; j < A[i].length; j++) {
                double z = A[j][i];
                for (int l = i; l < A[j].length; l++) {
                    A[j][l]-=A[i][l]*z;
                }
                b[j]-=b[i]*z;
            }
        }
        double [] coefficients = new double[A.length];
        for (int i = coefficients.length-1; i > -1; i--) {
            coefficients[i] = b[i];
            for (int j = coefficients.length-1; j > i; j--) {
                coefficients[i]-=A[i][j]*coefficients[j];
            }
        }
        return coefficients;
    }
    public XYChart.Series<String, Number> setTrend(Operation operation) throws SQLException {
        int k = 0;
        A_sr=0;
        while (rs.next()){
            k++;
            A_sr+=Math.abs(1-(operation.f(k)/y.get(k-1)));
            String d = rs.getString("date");
            trend.getData().add(new XYChart.Data<>(d, operation.f(k)));
        }
        A_sr = A_sr*100/y.size();
        func = String.format("%s\nПроцент ошибки: %.2f%s",func,A_sr,"%");
        return trend;
    }
    public String getFunc() {
        return func;
    }
}


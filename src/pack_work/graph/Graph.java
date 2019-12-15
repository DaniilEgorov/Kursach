package pack_work.graph;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import pack_log.Connection_database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Graph extends Connection_database {
    String user_name;
    ArrayList<Double> list_y;
    @FXML
    ComboBox<String> cb_graph,cb_trend;
    ObservableList<String> list_graph = FXCollections.observableArrayList("Открытие","Максимум","Минимум","Закрытие");
    ObservableList<String> list_trend = FXCollections.observableArrayList("Линейная","Квадратичная","Экспоненциальная","Логарифмическая");
    @FXML
    TextField tick;
    @FXML
    NumberAxis y;
    @FXML
    LineChart<String, Number> chart;
    @FXML
    Label user,formula;
    @FXML
    ColorPicker cp_graph, cp_trend;
    @FXML
    DatePicker start, end;

    @FXML
    void initialize() throws SQLException {
        newConnect();
        cb_graph.setItems(list_graph);
        cb_trend.setItems(list_trend);
    }

    @FXML
    public void draw() throws SQLException {
        list_y = new ArrayList<>();
        ObservableList<XYChart.Series<String, Number>> list = FXCollections.observableArrayList();
        Color color_graph = cp_graph.getValue();
        String s = start.getValue().toString();
        String e = end.getValue().toString();
        String color = color_graph.toString().substring(2,8);
        ps = connection.prepareStatement(String.format("SELECT * FROM rate WHERE date > '%s' AND date < '%s' AND login = '%s' AND ticker = '%s' ORDER BY date", s, e, user_name,tick.getText()));
        ResultSet rs = ps.executeQuery();
        XYChart.Series<String, Number> graph = new XYChart.Series<>();
        String str = cb_graph.getValue();
        graph.setName(str);
        double max = 0;
        double min = Double.MAX_VALUE;
        while (rs.next()) {
            String d = rs.getString("date");
            double g=0;
            switch (str) {
                case "Открытие":
                    g = rs.getDouble("open");
                    break;
                case "Максимум":
                    g = rs.getDouble("high");
                    break;
                case "Минимум":
                    g = rs.getDouble("low");
                    break;
                case "Закрытие":
                    g = rs.getDouble("close");
                    break;
            }
            if(g>max)max=g;
            else if(g<min)min=g;
            list_y.add(g);
            graph.getData().add(new XYChart.Data<>(d, g));
        }
        y.setLowerBound(min);
        y.setUpperBound(max);
        y.setAutoRanging(false);
        y.setTickUnit((max-min)/10);
        list.add(graph);
        chart.setData(list);
        painting(color,graph,0);
    }
    @FXML
    public void drawTrend() throws SQLException {
        ObservableList<XYChart.Series<String, Number>> clear = chart.getData();
        if (clear != null) {
            ObservableList<XYChart.Series<String, Number>> list = FXCollections.observableArrayList();
            XYChart.Series<String, Number> trend = new XYChart.Series<>();
            Color color_trend = cp_trend.getValue();
            String color = color_trend.toString().substring(2,8);
            String s = start.getValue().toString();
            String e = end.getValue().toString();
            ps = connection.prepareStatement(String.format("SELECT * FROM rate WHERE date > '%s' AND date < '%s' AND login = '%s' AND ticker = '%s' ORDER BY date", s, e, user_name,tick.getText()));
            ResultSet rs = ps.executeQuery();
            String str = cb_trend.getValue();
            trend.setName(str);
            Trend tr = new Trend(list_y,rs,trend);
            switch (str) {
                case "Линейная":
                    trend = tr.linear();
                    break;
                case "Квадратичная":
                    trend = tr.quadratic();
                    break;
                case "Экспоненциальная":
                    trend = tr.exponential();
                    break;
                case "Логарифмическая":
                    trend = tr.logarithmic();
                    break;
            }
            list.addAll(chart.getData().get(0),trend);
            chart.setData(list);
            painting(cp_graph.getValue().toString().substring(2,8), chart.getData().get(0), 0);
            painting(color, trend, 1);
            formula.setText(tr.getFunc());
            java.util.Date dateNow = new java.util.Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(dateNow);
            String act ="Получение линии тренда для периода:"+s+" - "+e+"   Тип валюты:"+tick.getText()+"   "+str+":"+tr.getFunc().substring(0,tr.getFunc().indexOf('\n'));
            ps = connection.prepareStatement(String.format("INSERT INTO actions VALUES ('%s','%s','%s')",date,act,user_name));
            ps.executeUpdate();
        }
    }
    public void setUser(String user) {
        this.user.setText(this.user.getText() + user);
        user_name = user;
    }
    public void painting(String color, XYChart.Series<String, Number> series, int i){
        Platform.runLater(() -> {
            Node line = chart.lookup(".default-color"+i+".chart-series-line");
            Node legend = chart.lookup(".default-color"+i+".chart-legend-item-symbol");
            line.setStyle("-fx-stroke: #" + color + ";");
            if(legend!=null)legend.setStyle("-fx-background-color: #" + color + ", white;");
            for (int j = 0; j < series.getData().size(); j++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(j);
                Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                lineSymbol.setStyle("-fx-background-color: #" + color + "; -fx-background-radius: 0; -fx-padding: 1px ;");
            }
        });
    }
}

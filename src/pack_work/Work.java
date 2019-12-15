package pack_work;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pack_log.Connection_database;
import pack_work.graph.Graph;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class Work extends Connection_database {
    String user_name;
    @FXML
    Label label, user, label1;
    @FXML
    TextField field;
    @FXML
    Button chooseButt;
    @FXML
    public void initialize() throws SQLException {
        newConnect();
    }
    @FXML
    void chooseFiles() throws ParseException, SQLException, IOException {
        Stage stage = (Stage) chooseButt.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        List<File> list = chooser.showOpenMultipleDialog(stage);
        if(list != null && !list.isEmpty()){
            for (File file: list) {
                addData(file.getAbsolutePath());
            }
        }
//        ps = connection.prepareStatement("SELECT * FROM rate ORDER BY date");
//        ps.executeUpdate();
        label1.setText("Файл загружен");
    }
    @FXML
    void downloadFile(){
        if(!field.getText().equals("")){
            String urlStr = field.getText();
            try {
                URL url = new URL(urlStr);
                String fileName = urlStr.substring(urlStr.lastIndexOf('/')+1);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream("C:\\Users\\danil\\Desktop\\"+fileName);
                fos.getChannel().transferFrom(rbc,0,Long.MAX_VALUE);
                fos.close();
                rbc.close();
                label.setText("Загурзка завершена");
                addData("C:\\Users\\danil\\Desktop\\"+fileName);
            } catch (MalformedURLException e) {
                label.setText("Некорректная ссылка");
            } catch (IOException e) {
                label.setText("Не удалось загрузить файл");
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else label.setText("Вставьте ссылку");
    }
    void addData(String string) throws IOException, SQLException, ParseException {
        BufferedReader bf = new BufferedReader(new FileReader(new File(string)));
        String line = bf.readLine();
        while((line=bf.readLine())!=null){
            String[] strings = line.split(";");
            long date = new SimpleDateFormat("dd/MM/yy").parse(strings[2]).getTime();
            Date d = new Date(date);
            String tick = strings[0];
            double o = Double.parseDouble(strings[4]);
            double h = Double.parseDouble(strings[5]);
            double l = Double.parseDouble(strings[6]);
            double c = Double.parseDouble(strings[7]);
            ps = connection.prepareStatement(String.format("SELECT * FROM rate WHERE date = '%s' AND login = '%s' AND ticker = '%s'  ORDER BY date",d,user_name,tick));
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ps = connection.prepareStatement("UPDATE rate SET open = ?, high = ?, low = ?, close = ?  WHERE date = ? AND ticker = ? AND login = ?");
                ps.setDouble(1,o);
                ps.setDouble(2,h);
                ps.setDouble(3,l);
                ps.setDate(5,d);
                ps.setDouble(4,c);
                ps.setString(6,tick);
            }
            else {
                ps = connection.prepareStatement("INSERT INTO rate VALUES (?,?,?,?,?,?,?)");
                ps.setDate(1,d);
                ps.setString(2,tick);
                ps.setDouble(3,o);
                ps.setDouble(4,h);
                ps.setDouble(5,l);
                ps.setDouble(6,c);
            }
            ps.setString(7,user_name);
            ps.executeUpdate();
        }
        java.util.Date dateNow = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(dateNow);
        String act = "Загрузка данных из " + string.substring(string.lastIndexOf('\\')+1);
        ps = connection.prepareStatement(String.format("INSERT INTO actions VALUES ('%s','%s','%s')",date,act,user_name));
        ps.executeUpdate();
    }
    @FXML
    public void graph() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("graph/Graphic.fxml")) ;
        Stage primaryStage = new Stage();
        Parent root = loader.load();
        Graph graph =loader.getController();
        graph.setUser(user_name);
        primaryStage.setScene(new Scene(root, 800, 650));
        primaryStage.show();
    }
    @FXML
    public void table() throws SQLException {
        TableView<Day> data = new TableView<>();
        TableColumn<Day,String> date = new TableColumn<>("Дата");
        TableColumn<Day,String> ticker = new TableColumn<>("Валюты");
        TableColumn<Day,Double> open = new TableColumn<>("Открытие");
        TableColumn<Day,Double> high = new TableColumn<>("Максимум");
        TableColumn<Day,Double> low = new TableColumn<>("Минимум");
        TableColumn<Day,Double> close = new TableColumn<>("Закрытие");
        ObservableList<Day> list = FXCollections.observableArrayList();
        ps = connection.prepareStatement(String.format("SELECT * FROM rate WHERE login = '%s' ORDER BY date",user_name));
        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            String d = rs.getString("date");
            String tick = rs.getString("ticker");
            double o = rs.getDouble("open");
            double h = rs.getDouble("high");
            double l = rs.getDouble("low");
            double c = rs.getDouble("close");
            list.add(new Day(d,tick,o,h,l,c));
        }
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        ticker.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        open.setCellValueFactory(new PropertyValueFactory<>("open"));
        high.setCellValueFactory(new PropertyValueFactory<>("high"));
        low.setCellValueFactory(new PropertyValueFactory<>("low"));
        close.setCellValueFactory(new PropertyValueFactory<>("close"));
        data.setItems(list);
        data.getColumns().addAll(date,ticker,open,high,low,close);
        Stage stage = new Stage();
        Scene scene  = new Scene(data,800,600);
        stage.setScene(scene);
        stage.show();
    }
    public void setUser(String user){
        this.user.setText(this.user.getText()+user);
        user_name = user;
    }
}

package pack_log;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pack_work.Work;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogIn extends Connection_database {
    String log;
    @FXML
    Label label;
    @FXML
    TextField login;
    @FXML
    PasswordField password;
    @FXML
    Button button;

    @FXML
    void sign_up() throws IOException {
        createScene("Sign_up.fxml",325,400);
        button.getScene().getWindow().hide();
    }
    @FXML
    void show() throws IOException {
        if(enter()) {
            createScene("WorkPane.fxml", 600, 400);
            button.getScene().getWindow().hide();
        }
    }
    void createScene(String string, int v, int v1) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(string)) ;
        Stage primaryStage = new Stage();
        Parent root = loader.load();
        if(string.equals("WorkPane.fxml")){
            Work work =loader.getController();
            work.setUser(log);
        }
        primaryStage.setScene(new Scene(root, v, v1));
        primaryStage.show();
    }
    boolean enter(){
        log = login.getText();
        String pass = password.getText();
        if(!log.equals("") && !pass.equals("")){
            try {
                ps = connection.prepareStatement("SELECT * FROM users WHERE login = '"+log+"'");
                ResultSet rs = ps.executeQuery();
                rs.next();
                if(pass.equals(rs.getString("password").trim())){
                    Date dateNow = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = sdf.format(dateNow);
                    ps = connection.prepareStatement(String.format("INSERT INTO actions VALUES ('%s','%s','%s')",date,"Вход",log));
                    ps.executeUpdate();
                    return true;
                }
                else {
                    label.setText("Неверный пароль");
                    return false;
                }
            } catch (SQLException e) {
                label.setText("Пользователь отсутствует");
                return false;
            }
        }else{
            label.setText("Заполните все поля");
            return false;
        }
    }
    @FXML
    void initialize() throws SQLException {
        newConnect();
    }
}

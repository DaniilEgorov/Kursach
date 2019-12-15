package pack_reg;

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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Registration extends Connection_database {
    String login;
    @FXML
    Label label;
    @FXML
    PasswordField pass_new,pass_new_copy;
    @FXML
    TextField login_new;
    @FXML
    Button butt;
    @FXML
    void log() throws IOException {
        createScene("Log.fxml",325,400);
        butt.getScene().getWindow().hide();
    }
    @FXML
    void initialize() throws SQLException {
        newConnect();
    }
    @FXML
    void show() throws IOException {
        if(addUser()) {
            createScene("WorkPane.fxml", 600, 400);
            butt.getScene().getWindow().hide();
        }
    }
    void createScene(String string, int v, int v1) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(string)) ;
        Stage primaryStage = new Stage();
        Parent root = loader.load();
        if(string.equals("WorkPane.fxml")){
            Work work =loader.getController();
            work.setUser(login);
        }
        primaryStage.setScene(new Scene(root, v, v1));
        primaryStage.show();
    }
    boolean addUser()  {
        String pass = pass_new.getText();
        String pass_copy = pass_new_copy.getText();
        login = login_new.getText();
        if(!login.equals("") && !pass.equals("") && !pass_copy.equals("")) {
            if (pass.equals(pass_copy)) {
                try {
                    ps = connection.prepareStatement(String.format("INSERT INTO users VALUES ('%s','%s')", login, pass));
                    ps.executeUpdate();
                    Date dateNow = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = sdf.format(dateNow);
                    ps = connection.prepareStatement(String.format("INSERT INTO actions VALUES ('%s','%s','%s')",date,"Регистрация",login));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    label.setText("Логин занят");
                    return false;
                }
                return true;
            } else {
                label.setText("Пароли не совпадают");
                return false;
            }
        }else{
            label.setText("Заполните все поля");
            return false;
        }
    }
}
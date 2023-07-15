package Snapp.Controller;

import Snapp.Account;
import Snapp.SnapApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AdminLogin {
    public Label errorLabel;
    public TextField usernameField;
    public TextField passwordField;
    public Label qLabel;
    public TextField AnswerField;
    public Button f2;

    public void checkLogin() throws IOException {
        errorLabel.setOpacity(0.0);
        try
        {
            Account account = Account.login(usernameField.getText(), passwordField.getText());
            if (account.isIsadmin())
            {
                Account.setActiveUser(account);
                SnapApplication.changeScene("admin-home.fxml");
            }
            else
                throw new Account.UsernameNotExists();
        }
        catch (Account.UsernameNotExists | Account.IncorrectPasswordException e)
        {
            errorLabel.setOpacity(1.00);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    @FXML
    public void forgotpass1(){
        try {
            qLabel.setText(Account.getQuestion(usernameField.getText()));
        } catch (Account.UsernameNotExists e) {
            errorLabel.setOpacity(1.00);
        }
        AnswerField.setOpacity(1);
        qLabel.setOpacity(1);
        f2.setOpacity(1);
    }
    @FXML
    public void forgotpass2(){
        try
        {
            Account account = Account.forgotpass(usernameField.getText(), passwordField.getText(),AnswerField.getText());
            if (account.isIsadmin())
            {
                Account.setActiveUser(account);
                SnapApplication.changeScene("admin-home.fxml");
            }
        }
        catch (Account.UsernameNotExists | Account.IncorrectPasswordException e)
        {
            errorLabel.setOpacity(1.00);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openAdminSignUp() throws IOException {
        SnapApplication.changeScene("admin-sign-up.fxml");
    }
    public void openLogin() throws IOException {
        SnapApplication.changeScene("login.fxml");
    }

}

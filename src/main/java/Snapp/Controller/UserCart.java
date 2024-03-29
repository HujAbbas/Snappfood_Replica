package Snapp.Controller;

import Snapp.DiscountCard;
import Snapp.SnappApplication;
import Snapp.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserCart implements Initializable {
    public Label totalCostLabel;

    public GridPane gridPane;
    public ComboBox<String> comboBox;
    public Label payedLabel;

    public void pay()
    {
        payedLabel.setOpacity(0.0);
        try
        {
            User.getActiveUser().getCart().buy();
            payedLabel.setOpacity(1.0);
            int disc = DiscountCard.giveDiscountCardToUser(User.getActiveUser(), User.getActiveUser().getCart().price());
            if (disc > 0)
            {
                //TODO "you have a new discount card"
            }
            SnappApplication.changeScene("admin-home.fxml");
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    public void reset() {
        if (User.getActiveUser().getCart().getFoods().size() != 0) {
            User.getActiveUser().getCart().getFoods().clear();
            gridPane.getChildren().clear();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (User.getActiveUser().getCart().price() != 0)
            totalCostLabel.setText(String.valueOf(User.getActiveUser().getCart().price()));

        //discount card
        if (User.getActiveUser().getDiscountCards().size() != 0) {
            ObservableList<String> list = FXCollections.observableArrayList();
            comboBox.setItems(list);
            for (int i = 0; i < User.getActiveUser().getDiscountCards().size(); i++){
                list.add(User.getActiveUser().getDiscountCards().get(i).getId()+". "+User.getActiveUser().getDiscountCards().get(i).getDiscount());
            }
            comboBox.setOnAction(event -> {
                try {
                    User.getActiveUser().getCart().setDiscountCard(User.getActiveUser().getDiscountCards().get(Integer.parseInt((comboBox.getValue().split("\\."))[0])));
                    if (User.getActiveUser().getCart().price() != 0)
                        totalCostLabel.setText(String.valueOf(User.getActiveUser().getCart().price()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        //foods
        if (User.getActiveUser().getCart().getFoods().size() != 0) {
            gridPane.setPrefHeight(135 * User.getActiveUser().getCart().getFoods().size());
            Button[] buttons = new Button[User.getActiveUser().getCart().getFoods().size()];
            //Food 0
            buttons[0] = new Button(User.getActiveUser().getCart().getFoods().get(0).getName()+"\n"+User.getActiveUser().getCart().getFoods().get(0).getPrice());
            ImageView imageView = new ImageView(new Image(SnappApplication.class.getResourceAsStream(User.getActiveUser().getCart().getFoods().get(0).getImageURL())));
            imageView.setFitWidth(80);
            imageView.setFitWidth(80);
            buttons[0].setGraphic(imageView);
            gridPane.add(buttons[0], 0, 0);
            buttons[0].setOnAction(e -> {
                User.getActiveUser().getCart().getFoods().remove(User.getActiveUser().getCart().getFoods().get(0));
                gridPane.getChildren().clear();
                initialize(null,null);
            });
            //Food 1->
            for (int i = 1; i < User.getActiveUser().getCart().getFoods().size(); i++) {
                buttons[i] = new Button(User.getActiveUser().getCart().getFoods().get(i).getName()+"\n"+User.getActiveUser().getCart().getFoods().get(i).getPrice());
                imageView = new ImageView(new Image(SnappApplication.class.getResourceAsStream(User.getActiveUser().getCart().getFoods().get(i).getImageURL())));
                imageView.setFitWidth(80);
                imageView.setFitWidth(80);
                buttons[i].setGraphic(imageView);
                gridPane.addRow(i, buttons[i]);
                int k = i;
                buttons[i].setOnAction(e -> {
                    User.getActiveUser().getCart().getFoods().remove(User.getActiveUser().getCart().getFoods().get(k));
                    gridPane.getChildren().clear();
                    initialize(null,null);
                });
            }
            gridPane.setVgap(10);

        }
    }

    public void openHome() throws IOException {
        SnappApplication.changeScene("user-home.fxml");
    }
}

package app.controllers;

import app.backend.NameEntry;
import javafx.scene.control.ListCell;

public class NameCell extends ListCell<NameEntry> {

    @Override
    public void updateItem(NameEntry nameEn, boolean empty)
    {
        super.updateItem(nameEn, empty);
        if(nameEn != null && !empty)
        {
            setText(nameEn.getName());
        } else {
            setGraphic(null);
        }
    }

};

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
            double avRating = nameEn.averageRating();
            String text;
            int length = nameEn.getName().length();

            char[] whiteChars = new char[50];
            int spaces = 50 - length;
            for(int i = 0; i < 50; i++) {
                whiteChars[i] = ' ';
            }
            String whitespace = new String(whiteChars, 0, spaces);

            if(avRating < 0) {
                text = nameEn.getName() + whitespace + "Avg. Rating: Unrated";
                System.out.println(text);
            } else {
                text = String.format("%s%sAvg. Rating: %.1f",nameEn.getName(), whitespace, avRating);
                System.out.println(text);
            }

            setText(text);
        } else {
            setGraphic(null);
        }
    }

};

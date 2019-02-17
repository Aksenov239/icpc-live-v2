package org.icpclive.webadmin.mainscreen.picture;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import org.icpclive.webadmin.mainscreen.MainScreenData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Meepo on 11/28/2018.
 */
public class MainScreenPictureView extends CustomComponent implements View {
    public static String NAME = "mainscreen-picture";

    MainScreenData mainScreenData;

    Label status;

    Table pictures;

    TextField captionAdd;
    Upload pictureAdd;

    TextField caption;
    Embedded picture;

    Button show;
    Button hide;

    Button edit;

    Picture lastPicture;

    public Table createTable() {
        pictures = new Table();
        pictures.setContainerDataSource(mainScreenData.pictureData.getContainer());

        pictures.setVisibleColumns("caption");

        pictures.setSelectable(true);
        pictures.setEditable(false);
        pictures.setSizeFull();

        pictures.addValueChangeListener(event -> {
            if (pictures.getValue() == null) {
                caption.setVisible(false);
                picture.setVisible(false);
                edit.setVisible(false);
                return;
            }
            lastPicture = (Picture) pictures.getValue();
            caption.setValue(lastPicture.getCaption());
            caption.setVisible(true);
            picture.setSource(new FileResource(new File(lastPicture.getPath())));
            picture.setVisible(true);
            edit.setVisible(true);
        });

        return pictures;
    }

    class PictureUploader implements Upload.Receiver, Upload.SucceededListener {
        public synchronized OutputStream receiveUpload(String filename, String mimeType) {
            FileOutputStream fos = null;
            String file;
            try {
                file = "tmp/" + System.nanoTime() + "-" + filename;
                fos = new FileOutputStream(new File(file));
            } catch (FileNotFoundException e) {
                new Notification("Could not open file", e.getMessage(), Notification.Type.ERROR_MESSAGE)
                        .show(Page.getCurrent());
                return null;
            }
            Picture picture = new Picture(captionAdd.getValue(), file);
            mainScreenData.pictureData.addPicture(picture);
            return fos;
        }

        public void uploadSucceeded(Upload.SucceededEvent event) {

        }
    }

    public Component createUpload() {
        captionAdd = new TextField("Caption to picture");

        PictureUploader receiver = new PictureUploader();
        pictureAdd = new Upload("Upload picture here", receiver);
        pictureAdd.setButtonCaption("Upload");
        pictureAdd.addSucceededListener(receiver);

        Component component = new VerticalLayout(captionAdd, pictureAdd);
        return component;
    }

    public Component editUpload() {
        caption = new TextField("Caption");
        caption.setVisible(false);
        picture = new Embedded("Picture");
        picture.setVisible(false);

        picture.setSizeFull();

        show = new Button("Show");
        show.addClickListener(event -> {
            if (pictures.getValue() == null) {
                Notification.show("You should choose a picture", Notification.Type.WARNING_MESSAGE);
                return;
            }
            String error = mainScreenData.pictureData.setVisible((Picture) pictures.getValue());
            if (error != null) {
                Notification.show(error, Notification.Type.WARNING_MESSAGE);
                return;
            }
        });
        hide = new Button("Hide");
        hide.addClickListener(event -> {
            mainScreenData.pictureData.hide();
        });
        edit = new Button("Edit");
        edit.addClickListener(event -> {
            if (pictures.getValue() == null) {
                return;
            }
            mainScreenData.pictureData.setNewCaption(pictures.getValue(), caption.getValue());
            pictures.refreshRowCache();
        });
        edit.setVisible(false);

        HorizontalLayout buttons = new HorizontalLayout(show, hide, edit);
        buttons.setSpacing(true);

        Component component = new VerticalLayout(buttons, caption, picture);
        return component;
    }

    public MainScreenPictureView() {
        mainScreenData = MainScreenData.getMainScreenData();

        createTable();

        status = new Label(mainScreenData.pictureData.toString());

        HorizontalLayout mainPanel = new HorizontalLayout(
                new VerticalLayout(status, createUpload(), editUpload()),
                createTable()
        );

        mainPanel.setSizeFull();
        setCompositionRoot(mainPanel);
    }

    public void refresh() {
        status.setValue(mainScreenData.pictureData.toString());
        pictures.refreshRowCache();
    }

    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}

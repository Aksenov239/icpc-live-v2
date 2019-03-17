package org.icpclive.webadmin.mainscreen.video;

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

public class MainScreenVideoView extends CustomComponent implements View {
    public static String NAME = "mainscreen-video";

    MainScreenData mainScreenData;

    Label status;

    Table videos;

    TextField captionAdd;
    Upload videoAdd;

    TextField caption;
    com.vaadin.ui.Video video;

    Button show;
    Button hide;

    Button edit;

    Video lastVideo;

    public Table createTable() {
        videos = new Table();
        videos.setContainerDataSource(mainScreenData.videoData.getContainer());

        videos.setVisibleColumns("caption");

        videos.setSelectable(true);
        videos.setEditable(false);
        videos.setSizeFull();

        videos.addValueChangeListener(event -> {
            if (videos.getValue() == null) {
                caption.setVisible(false);
                video.setVisible(false);
                edit.setVisible(false);
                return;
            }
            lastVideo = (Video) videos.getValue();
            caption.setValue(lastVideo.getCaption());
            caption.setVisible(true);
            video.setSource(new FileResource(new File(lastVideo.getPath())));
            video.setVisible(true);
            edit.setVisible(true);
        });

        return videos;
    }

    class VideoUploader implements Upload.Receiver, Upload.SucceededListener {
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
            Video video = new Video(captionAdd.getValue(), file);
            mainScreenData.videoData.addVideo(video);
            return fos;
        }

        public void uploadSucceeded(Upload.SucceededEvent event) {
            captionAdd.clear();
        }
    }

    public Component createUpload() {
        captionAdd = new TextField("Caption to video");

        VideoUploader receiver = new VideoUploader();
        videoAdd = new Upload("Upload video here", receiver);
        videoAdd.setButtonCaption("Upload");
        videoAdd.addSucceededListener(receiver);

        Component component = new VerticalLayout(captionAdd, videoAdd);
        return component;
    }

    public Component editUpload() {
        caption = new TextField("Caption");
        caption.setVisible(false);
        video = new com.vaadin.ui.Video("Video");
        video.setVisible(false);

        video.setSizeFull();

        show = new Button("Show");
        show.addClickListener(event -> {
            if (videos.getValue() == null) {
                Notification.show("You should choose a video", Notification.Type.WARNING_MESSAGE);
                return;
            }
            String error = mainScreenData.videoData.setVisible((Video) videos.getValue());
            if (error != null) {
                Notification.show(error, Notification.Type.WARNING_MESSAGE);
                return;
            }
        });
        hide = new Button("Hide");
        hide.addClickListener(event -> {
            mainScreenData.videoData.hide();
        });
        edit = new Button("Edit");
        edit.addClickListener(event -> {
            if (videos.getValue() == null) {
                return;
            }
            mainScreenData.videoData.setNewCaption(videos.getValue(), caption.getValue());
            videos.refreshRowCache();
        });
        edit.setVisible(false);

        HorizontalLayout buttons = new HorizontalLayout(show, hide, edit);
        buttons.setSpacing(true);

        Component component = new VerticalLayout(buttons, caption, video);
        return component;
    }

    public MainScreenVideoView() {
        mainScreenData = MainScreenData.getMainScreenData();

        createTable();

        status = new Label(mainScreenData.videoData.toString());

        HorizontalLayout mainPanel = new HorizontalLayout(
                new VerticalLayout(status, createUpload(), editUpload()),
                createTable()
        );

        mainPanel.setSizeFull();
        setCompositionRoot(mainPanel);
    }

    public void refresh() {
        status.setValue(mainScreenData.videoData.toString());
        videos.refreshRowCache();
    }

    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}

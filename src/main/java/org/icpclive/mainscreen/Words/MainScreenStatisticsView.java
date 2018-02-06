package org.icpclive.mainscreen.Words;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.mainscreen.MainScreenData;

import static org.icpclive.mainscreen.Utils.createGroupLayout;
import static org.icpclive.mainscreen.Utils.setPanelDefaults;

/**
 * Created by Aksenov239 on 14.05.2017.
 */
public class MainScreenStatisticsView extends CustomComponent implements View {
    public static String NAME = "statistics";

    private static final Logger log = LogManager.getLogger(MainScreenStatisticsView.class);

    /* Words statistics */
    Label wordStatus;

    Table words;

    Button saveWord;
    Button removeWord;
    Button cancelWord;

    Button showWord;

    TextField wordText;
    TextField wordName;
    TextField wordPicture;

    WordStatistics chosenWord;
    WordStatisticsData wordStatisticsData;

    public String getWordStatus() {
        return MainScreenData.getMainScreenData().wordStatisticsData.toString();
    }

    public Component getWordStatisticsController() {
        wordStatisticsData = WordStatisticsData.getInstance();

        wordStatus = new Label(getWordStatus());

        showWord = new Button("Show word");
        showWord.addClickListener(e -> {
            WordStatistics word = (WordStatistics)words.getValue();
            if (word == null) {
                Notification.show("You should choose the word to show", Notification.Type.WARNING_MESSAGE);
                return;
            }
            String result = MainScreenData.getMainScreenData().wordStatisticsData.setWordVisible(word);
            if (result != null) {
                Notification.show(result, Notification.Type.WARNING_MESSAGE);
            }
        });

        words = new Table();
        words.setContainerDataSource(wordStatisticsData.wordsList.getContainer());
        words.setVisibleColumns(new Object[] { "wordName", "word", "picture", "count" });

        words.addValueChangeListener(e -> {
            setWord((WordStatistics) words.getValue());
        });

        saveWord = new Button("Save word");
        saveWord.addClickListener(e -> {
            if (chosenWord != null) {
                wordStatisticsData.setValue(chosenWord, "wordName", wordName.getValue());
                wordStatisticsData.setValue(chosenWord, "word", wordText.getValue());
                wordStatisticsData.setValue(chosenWord, "picture", wordPicture.getValue());
            } else {
                WordStatisticsData.getInstance().addWord(
                        new WordStatistics(wordName.getValue(), wordText.getValue(), wordPicture.getValue()));
            }
            words.setValue(null);
            setWord(null);
        });
        saveWord.setStyleName(ValoTheme.BUTTON_PRIMARY);
        saveWord.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        removeWord = new Button("Remove word");
        removeWord.addClickListener(e -> {
            if (chosenWord == null) {
                return;
            }
            WordStatisticsData.getInstance().removeWord(chosenWord);
            words.setValue(null);
            setWord(null);
        });

        cancelWord = new Button("Cancel");
        cancelWord.addClickListener(e -> {
            words.setValue(null);
            setWord(null);
        });

        wordName = new TextField("Name");
        wordText = new TextField("Text");
        wordPicture = new TextField("Picture");

        CssLayout actions = createGroupLayout(saveWord, removeWord, cancelWord);

        VerticalLayout form = new VerticalLayout(wordName, wordText, wordPicture);

        VerticalLayout controller = new VerticalLayout(wordStatus, showWord, actions, form, words);
        controller.setSpacing(true);
        controller.setMargin(true);

        return controller;
    }

    public void setWord(WordStatistics word) {
        chosenWord = word;
        if (word == null) {
            wordName.setValue("");
            wordText.setValue("");
            wordPicture.setValue("");
        } else {
            wordName.setValue(word.getWordName());
            wordText.setValue(word.getWord());
            wordPicture.setValue(word.getPicture());
        }
    }


        /* Statistics */

    final String[] statisticsStatuses = new String[]{"Statistics is shown", "Statistics isn't shown"};
    Label statisticsStatus;
    Button statisticsShow;
    Button statisticsHide;

    public Component getStatisticsController() {
        statisticsStatus = new Label(getStatisticsStatus());

        statisticsStatus.addStyleName("large");

        statisticsShow = createStatisticsButton("Show statistics", true, 0);
        statisticsHide = createStatisticsButton("Hide statistics", false, 1);

        CssLayout group = createGroupLayout(statisticsShow, statisticsHide);

        VerticalLayout panel = new VerticalLayout(statisticsStatus, group);
        setPanelDefaults(panel);
        return panel;
    }

    public String getStatisticsStatus() {
        boolean status = mainScreenData.statisticsData.isVisible();
        return status ? statisticsStatuses[0] : statisticsStatuses[1];
    }

    private Button createStatisticsButton(String name, boolean visibility, int status) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            String outcome = mainScreenData.statisticsData.setVisible(visibility);
            if (outcome != null) {
                Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                return;
            }

            statisticsStatus.setValue(statisticsStatuses[status]);
        });

        return button;
    }

    /* Frame rate controller */

    private Label frameRateStatus;
    private Button showFrameRate;
    private Button hideFrameRate;

    public Component getFrameRateController() {
        frameRateStatus = new Label();
        frameRateStatus.addStyleName("large");
        frameRateStatus.setValue(getFrameRateStatus());

        showFrameRate = new Button("Show Frame Rate");
        showFrameRate.addClickListener(e -> {
            mainScreenData.frameRateData.setFrameRateVisible(true);
        });

        hideFrameRate = new Button("Hide Frame Rate");
        hideFrameRate.addClickListener(e -> {
            mainScreenData.frameRateData.setFrameRateVisible(false);
        });

        VerticalLayout controller = new VerticalLayout(
                frameRateStatus,
                createGroupLayout(showFrameRate, hideFrameRate)
        );
        setPanelDefaults(controller);
        return controller;
    }

    public String getFrameRateStatus() {
        return mainScreenData.frameRateData.getStatus();
    }

    MainScreenData mainScreenData;

    public MainScreenStatisticsView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component wordStatisticsController = getWordStatisticsController();

        Component statisticsController = getStatisticsController();

        Component frameRateController = getFrameRateController();

        VerticalLayout rightPart = new VerticalLayout(
                frameRateController,
                statisticsController
        );

        HorizontalLayout mainPanel = new HorizontalLayout(wordStatisticsController, rightPart);
        mainPanel.setSizeFull();

        setCompositionRoot(mainPanel);
    }


    public void refresh() {
        wordStatus.setValue(getWordStatus());
        frameRateStatus.setValue(getFrameRateStatus());
        statisticsStatus.setValue(getStatisticsStatus());
    }

    public void enter(ViewChangeListener.ViewChangeEvent e) {

    }
}

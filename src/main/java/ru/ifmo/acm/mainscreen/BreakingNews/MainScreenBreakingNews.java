package ru.ifmo.acm.mainscreen.BreakingNews;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.backend.player.widgets.ClockWidget;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.WF.WFContestInfo;
import ru.ifmo.acm.events.WF.WFRunInfo;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.Utils;
import ru.ifmo.acm.utils.SynchronizedBeanItemContainer;

import java.util.ArrayList;
import java.util.List;

public class MainScreenBreakingNews extends CustomComponent implements View {
    private static final Logger log = LogManager.getLogger(MainScreenBreakingNews.class);

    public final static String NAME = "mainscreen-breaking-news";

    public MainScreenBreakingNews() {
        mainScreenData = MainScreenData.getMainScreenData();

        breakingNewsList = createBreakingNewsTable(container);

        breakingNewsForm = new BreakingNewsForm(this);

        VerticalLayout left = new VerticalLayout(breakingNewsList);
        left.setSizeFull();
        breakingNewsList.setSizeFull();
        left.setExpandRatio(breakingNewsList, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, breakingNewsForm);
        mainLayout.setSizeFull();
        mainLayout.setExpandRatio(left, 1);
        mainLayout.setExpandRatio(breakingNewsForm, 1.6f);

        setCompositionRoot(mainLayout);
    }


    private Table createBreakingNewsTable(BeanItemContainer<BreakingNews> container) {
        Table table = new Table();

        table.setContainerDataSource(container);

        table.addGeneratedColumn("time", (Table source, Object itemId, Object columnId) -> {
            BreakingNews news = (BreakingNews) itemId;
            Label label = new Label();
            label.setValue(ClockWidget.getTimeString(news.getTimestamp() / 1000));

            return label;
        });

        String[] columns = {"teamId", "team", "problem", "outcome", "time"};

        table.setVisibleColumns(columns);
        table.setSelectable(true);
        table.setMultiSelect(false);

        table.setImmediate(true);

        table.addValueChangeListener(event -> {
            BreakingNews value = (BreakingNews) breakingNewsList.getValue();
            breakingNewsForm.update(value);
        });

        table.addStyleName("breakingnews-table");

        table.setCellStyleGenerator((Table.CellStyleGenerator) (source, itemId, propertyId) -> {
            if (propertyId == null) {
                BreakingNews item = (BreakingNews) itemId;
                return item.getOutcome().toLowerCase();
            }

            return null;
        });

        return table;
    }

    public void refresh() {
        breakingNewsForm.refresh();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    }

    MainScreenData mainScreenData;

    final static BeanItemContainer<BreakingNews> container = new SynchronizedBeanItemContainer<>(BreakingNews.class);;
    Table breakingNewsList;

    final BreakingNewsForm breakingNewsForm;

    private static int lastShowedRun = 0;

    public static Utils.StoppedThread getUpdaterThread() {
        Utils.StoppedThread tableUpdater = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            @Override
            public void run() {
                while (!stop) {
                    // final BackUp<BreakingNews> backUp = MainScreenData.getProperties().backupBreakingNews;
                    WFContestInfo contestInfo = null;
                    while (contestInfo == null) {
                        contestInfo = (WFContestInfo) EventsLoader.getInstance().getContestData();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.error("error", e);
                        }
                    }

                    while (lastShowedRun <= contestInfo.getMaxRunId()) {
                        WFRunInfo run = contestInfo.getRun(lastShowedRun);
                        if (run != null) {
                            container.addItemAt(0,
                                    new BreakingNews(run.getResult(), "" + (char) (run.getProblemNumber() + 'A'), run.getTeamId() + 1, run.getTime(), run.getId()));
                        }
                        lastShowedRun++;
                    }

                    List<BreakingNews> toDelete = new ArrayList<>();
                    int runsNumber = MainScreenData.getProperties().breakingNewsRunsNumber;
                    for (int i = runsNumber; i < container.getItemIds().size(); i++) {
                        toDelete.add(container.getItemIds().get(i));
                    }

                    toDelete.forEach(msg -> container.removeItem(msg));

                    for (int i = 0; i < container.getItemIds().size(); i++) {
                        int runId = container.getItemIds().get(i).getRunId();
                        WFRunInfo run = contestInfo.getRun(runId);
                        container.getItemIds().get(i).update(run);
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("error", e);
                    }
                }
            }
        });

        return tableUpdater;
    }
}

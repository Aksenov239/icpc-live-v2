package ru.ifmo.acm.mainscreen.BreakingNews;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.Utils;

public class MainScreenBreakingNews extends CustomComponent implements View {
    public final static String NAME = "mainscreen-breaking-news";

    public MainScreenBreakingNews() {
        mainScreenData = MainScreenData.getMainScreenData();
        lastShowedRun = 0;

        container = MainScreenData.getProperties().backupBreakingNews.getContainer();
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
            label.setValue("" + (System.currentTimeMillis() - news.timestamp) / 1000);

            return label;
        });

        String[] columns = {"team", "problem", "outcome", "time"};

        table.setVisibleColumns(columns);
        table.setSelectable(true);
        table.setMultiSelect(false);

        table.setImmediate(true);

        table.addValueChangeListener(event -> {
            BreakingNews value = (BreakingNews) breakingNewsList.getValue();
            breakingNewsForm.update(value);
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

    final BeanItemContainer<BreakingNews> container;
    Table breakingNewsList;

    final BreakingNewsForm breakingNewsForm;

    private static int lastShowedRun;

    private static Utils.StoppedThread tableUpdater = null;
}

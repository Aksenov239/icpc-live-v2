package ru.ifmo.acm.backend.player.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import com.vaadin.shared.Position;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFRunInfo;

public class QueueWidget extends Widget {

	ContestInfo info;
	ArrayList<RunInfo> queue;
	int yShift = 0;
	boolean[] nowInQueue;

	public QueueWidget(long updateWait) {
		super(updateWait);
		queue = new ArrayList<>();
		nowInQueue = new boolean[1000000];
	}

	@Override
	protected void updateImpl(Data data) {
		calculateQueue();
		lastUpdate = System.currentTimeMillis();
	}

	static final int HEIGHT = 20;

	@Override
	public void paintImpl(Graphics2D g, int width, int height) {
		update();

		if (yShift > 0) {
			yShift = (int) (yShift * 0.8);
		}

		g.setFont(Font.decode("Open Sans Italic " + 20));
		drawTextInRect(g, "Current queue size: " + queue.size(), 50, 50, 400, 20, POSITION_CENTER, Color.RED,
				Color.BLACK, opacity);
		for (int i = 0; i < queue.size(); i++) {
			WFRunInfo wfr = (WFRunInfo) queue.get(i);

			TeamInfo team = info.getParticipant(wfr.getTeam());
			Color teamNameColor = Color.BLUE;

			String rank = String.valueOf(team.getRank());
			String teamName = team.getName();
			String problemName = String.valueOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(wfr.getProblemNumber() - 1));
			String status = "";
			if (wfr.judged) {
				if (wfr.isAccepted()) {
					drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, Color.GREEN, opacity);
					status = "AC";
					teamNameColor = Color.GREEN;
				} else {
					drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, Color.RED, opacity);
					status = wfr.getResult();
				}
			} else {
				drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, Color.LIGHT_GRAY, opacity);
				drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift,
						(int) (100 * 1.0 * wfr.getPassedTestsNumber() / wfr.getTotalTestsNumber()), HEIGHT,
						Color.YELLOW, opacity);
				status = String.valueOf(wfr.getPassedTestsNumber());
			}

			drawTextInRect(g, rank, 50, 50 + (HEIGHT * (i + 1)) + yShift, 30, HEIGHT, POSITION_CENTER, Color.GRAY,
					Color.WHITE, opacity);
			drawTextInRect(g, teamName, 50 + 32, 50 + (HEIGHT * (i + 1)) + yShift, 400, HEIGHT, POSITION_CENTER,
					teamNameColor, Color.WHITE, opacity);
			drawTextInRect(g, problemName, 50 + 434, 50 + (HEIGHT * (i + 1)) + yShift, 30, HEIGHT, POSITION_CENTER,
					Color.DARK_GRAY, Color.WHITE, opacity);

			drawTextInRect(g, status, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, POSITION_CENTER,
					new Color(0, 0, 0, 0), Color.WHITE, opacity);

		}

	}

	public void calculateQueue() {
		info = Preparation.eventsLoader.getContestData();

		WFRunInfo[] runs = (WFRunInfo[]) info.getRuns();
		queue.clear();
		for (WFRunInfo r : runs) {
			if (r == null)
				continue;
			if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - 10000) {
				queue.add(r);
				nowInQueue[r.getId()] = true;
			} else {
				if (nowInQueue[r.getId()]) {
					nowInQueue[r.getId()] = false;
					yShift += HEIGHT;
				}
			}
		}
	}

}

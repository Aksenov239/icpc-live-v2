package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.List;

/**
 * @author egor@egork.net
 */
public class RotatableWidget extends Widget {
    private int x;
    private int y;
    private BufferedImage unmovable;
    private BufferedImage movable;
    private int margin;
    private int[] shifts;
    private int showTime;
    private int shiftSpeed;
    private int fadeTime;
    private long lastDraw;

    static enum State {
        FADING_IN, SHOWING, MOVING, FADING_OUT, HIDDEN
    }

    //State
    private int currentShift;
    private State state;
    private int lastAt;
    private int timeRemaining;


    public RotatableWidget(long updateWait, int x, int y, int margin, int[] shifts, int showTime, int shiftSpeed, int fadeTime) {
        super(updateWait);
        this.x = x;
        this.y = y;
        this.margin = margin;
        this.shifts = shifts;
        this.showTime = showTime;
        this.shiftSpeed = shiftSpeed;
        this.fadeTime = fadeTime;
        currentShift = shifts[0];
        state = State.HIDDEN;
        lastAt = 0;
        timeRemaining = fadeTime;
    }

    private double opacity() {
        double result;
        switch (state) {
            case FADING_IN:
                result = 1 - (double)timeRemaining / fadeTime;
                break;
            case FADING_OUT:
                result = (double) timeRemaining / fadeTime;
                break;
            case HIDDEN:
                result = 0;
                break;
            case MOVING:
            case SHOWING:
                result = 1;
                break;
            default:
                throw new IllegalStateException();
        }
        return 3 * result * result - 2 * result * result * result;
    }

    @Override
    protected void paintImpl(Graphics2D g, int width, int height) {
        if (state == State.HIDDEN) {
            return;
        }
        prepare();
        g.clip(new Rectangle(x, y, unmovable.getWidth(), movable.getHeight() + unmovable.getHeight() + margin));
        double opacity = opacity();
        drawImage(g, unmovable, x, y, opacity);
        drawImage(g, movable, x - currentShift, y + unmovable.getHeight() + margin, opacity);
    }

    private void drawImage(Graphics2D g, BufferedImage image, int x, int y, double opacity) {
        if (opacity == 0) {
            return;
        }
        if (opacity == 1) {
            g.drawImage(image, x, y, null);
            return;
        }
        g.drawImage(image, new AffineTransform(1, 0, 0, opacity, x, y + image.getHeight() / 2 * (1 - opacity)), null);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data;
    }

    protected void hide() {
        state = State.FADING_OUT;
        timeRemaining = fadeTime;
    }
    protected void setFaded(){
        state = State.SHOWING;
        timeRemaining = showTime;
    }

    private void prepare() {
        if (state == State.HIDDEN) {
            return;
        }
        int sinceUpdate = (int) (System.currentTimeMillis() - lastDraw);
        if (state == State.FADING_IN) {
            timeRemaining -= sinceUpdate;
            if (timeRemaining <= 0) {
                timeRemaining = showTime;
                state = State.SHOWING;
                lastAt = 0;
            }
        } else if (state == State.FADING_OUT) {
            timeRemaining -= sinceUpdate;
            if (timeRemaining <= 0) {
                state = State.HIDDEN;
            }
        } else if (state == State.SHOWING) {
            timeRemaining -= sinceUpdate;
            if (timeRemaining <= 0) {
                if (lastAt != shifts.length - 1) {
                    state = State.MOVING;
                } else {
                    state = State.FADING_OUT;
                    timeRemaining = fadeTime;
                }
            }
        } else {
            int target = shifts[lastAt + 1];
            if (target > shifts[lastAt]) {
                currentShift = Math.min(currentShift + sinceUpdate * shiftSpeed / 1000, target);
            } else {
                currentShift = Math.max(currentShift - sinceUpdate * shiftSpeed / 1000, target);
            }
            if (currentShift == target) {
                lastAt++;
                state = State.SHOWING;
                timeRemaining = showTime;
            }
        }
        lastDraw += sinceUpdate;
    }

    public void start() {
        currentShift = shifts[0];
        state = State.FADING_IN;
        lastAt = 0;
        timeRemaining = fadeTime;
        lastDraw = System.currentTimeMillis();
    }

    public BufferedImage getUnmovable() {
        return unmovable;
    }

    public void setUnmovable(BufferedImage unmovable) {
        this.unmovable = unmovable;
    }

    public BufferedImage getMovable() {
        return movable;
    }

    public void setMovable(BufferedImage movable) {
        this.movable = movable;
    }

    public void close() {
        if (state == State.FADING_OUT || state == State.HIDDEN) {
            return;
        }
        state = State.FADING_OUT;
        timeRemaining = fadeTime;
        lastDraw = System.currentTimeMillis();
    }
}

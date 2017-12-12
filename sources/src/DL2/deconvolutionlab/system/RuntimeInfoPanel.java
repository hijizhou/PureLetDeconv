package DL2.deconvolutionlab.system;

import DL2.deconvolutionlab.Config;
import DL2.deconvolutionlab.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RuntimeInfoPanel
        extends JPanel
{
    private Timer timer = new Timer();
    private TimerTask updater = new Updater();
    private MemoryMeter memory;
    private ProcessorMeter processor;
    private int width = Constants.widthGUI;
    private ArrayList<AbstractMeter> meters = new ArrayList();
    private long rate = 0L;

    public RuntimeInfoPanel(long rate)
    {
        this.rate = rate;
        this.memory = new MemoryMeter(100);
        this.processor = new ProcessorMeter(100);

        this.meters.add(this.memory);
        this.meters.add(this.processor);

        JPanel meters = new JPanel(new GridLayout(1, 3));
        meters.add(this.memory);
        meters.add(this.processor);
        restart();

        JPanel pnCompact = new JPanel();
        pnCompact.setPreferredSize(new Dimension(this.width, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.add(meters, "Center");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, "North");

        add(panel);
        setMinimumSize(new Dimension(this.width, 70));
        Rectangle rect = Config.getDialog("System.Frame");
        if ((rect.x > 0) && (rect.y > 0)) {
            setLocation(rect.x, rect.y);
        }
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void update()
    {
        this.processor.update();
        this.memory.update();
    }

    public void restart()
    {
        long refreshTime = this.rate;
        if (this.updater != null)
        {
            this.updater.cancel();
            this.updater = null;
        }
        this.updater = new Updater();
        this.timer.schedule(this.updater, 0L, refreshTime);
    }

    private class Updater
            extends TimerTask
    {
        private Updater() {}

        public void run()
        {
            RuntimeInfoPanel.this.update();
        }
    }
}

package DL2.deconvolution.capsule;

import DL2.deconvolution.Deconvolution;
import DL2.deconvolutionlab.system.*;
import DL2.fft.FFTPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ResourcesCapsule
        extends AbstractCapsule
        implements ActionListener
{
    private ArrayList<AbstractMeter> meters;
    private JPanel cards;
    private MemoryMeter memory;
    private ProcessorMeter processor;
    private SignalMeter signal;
    private FFTMeter fft;
    private JavaMeter java;
    private FileMeter file;
    private JPanel buttons;
    private JButton bnMemory;
    private JButton bnProcessor;
    private JButton bnSignal;
    private JButton bnFFT;
    private JButton bnJava;
    private JButton bnFile;

    public ResourcesCapsule(Deconvolution deconvolution)
    {
        super(deconvolution);
        this.bnMemory = new JButton("Memory");
        this.bnProcessor = new JButton("Processor");
        this.bnSignal = new JButton("Signal");
        this.bnFFT = new JButton("FFT");
        this.bnJava = new JButton("Java");
        this.bnFile = new JButton("File");

        this.buttons = new JPanel(new GridLayout(6, 1));
        this.buttons.add(this.bnMemory);
        this.buttons.add(this.bnProcessor);
        this.buttons.add(this.bnSignal);
        this.buttons.add(this.bnFFT);
        this.buttons.add(this.bnJava);
        this.buttons.add(this.bnFile);

        int width = 100;
        this.memory = new MemoryMeter(width / 3);
        this.processor = new ProcessorMeter(width / 3);
        this.signal = new SignalMeter(width / 3);
        this.fft = new FFTMeter(width / 3);
        this.java = new JavaMeter(width / 3);
        this.file = new FileMeter(width / 3);

        this.meters = new ArrayList();
        this.meters.add(this.processor);
        this.meters.add(this.memory);
        this.meters.add(this.signal);
        this.meters.add(this.fft);
        this.meters.add(this.java);
        this.meters.add(this.file);

        this.cards = new JPanel(new CardLayout());
        this.cards.add(this.memory.getMeterName(), this.memory.getPanel(400, 200));
        this.cards.add(this.processor.getMeterName(), this.processor.getPanel(400, 200));
        this.cards.add(this.signal.getMeterName(), this.file.getPanel(400, 200));
        this.cards.add(this.fft.getMeterName(), new FFTPanel(400, 200));
        this.cards.add(this.java.getMeterName(), this.java.getPanel(400, 200));
        this.cards.add(this.file.getMeterName(), this.file.getPanel(400, 200));

        this.bnFile.addActionListener(this);
        this.bnFFT.addActionListener(this);
        this.bnJava.addActionListener(this);
        this.bnSignal.addActionListener(this);
        this.bnProcessor.addActionListener(this);
        this.bnMemory.addActionListener(this);

        this.split = new JSplitPane(1, this.buttons, this.cards);
        this.split.setDividerLocation(0.2D);
    }

    public void update() {}

    public String getID()
    {
        return "Resources";
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this.bnJava) {
            ((CardLayout)this.cards.getLayout()).show(this.cards, this.java.getMeterName());
        }
        if (e.getSource() == this.bnMemory) {
            ((CardLayout)this.cards.getLayout()).show(this.cards, this.memory.getMeterName());
        }
        if (e.getSource() == this.bnProcessor) {
            ((CardLayout)this.cards.getLayout()).show(this.cards, this.processor.getMeterName());
        }
        if (e.getSource() == this.bnFFT) {
            ((CardLayout)this.cards.getLayout()).show(this.cards, this.fft.getMeterName());
        }
        if (e.getSource() == this.bnSignal) {
            ((CardLayout)this.cards.getLayout()).show(this.cards, this.signal.getMeterName());
        }
        if (e.getSource() == this.bnFile) {
            ((CardLayout)this.cards.getLayout()).show(this.cards, this.file.getMeterName());
        }
    }
}

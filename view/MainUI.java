package view;

import controller.CAUtil;
import model.ImagePanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainUI {
    private JPanel optionPanel;
    private ImagePanel imgPanel;
    private final CAUtil util;
    boolean isRunning;

    public MainUI() throws IOException {
        JFrame frame = new JFrame("Lattice Gas Automata");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        initImagePanel();
        frame.add(imgPanel);

        initOptionPanel();
        initOptionButton();
        frame.add(optionPanel, BorderLayout.EAST);

        isRunning = false;
        util = new CAUtil(imgPanel.getImage());

        frame.pack();
        frame.setVisible(true);
    }

    private void initImagePanel() {
        imgPanel = new ImagePanel();
        imgPanel.setPreferredSize(new Dimension(500, 400));
    }

    private void initOptionButton() {
        var simulateButton = new JButton("Start / stop");
        simulateButton.addActionListener(e -> {
            isRunning = !isRunning;
        });
        optionPanel.add(simulateButton);
    }

    private void initOptionPanel() {
        optionPanel = new JPanel();
        optionPanel.setPreferredSize(new Dimension(100, 400));
    }

    public boolean ifClicked() {
        return isRunning;
    }

    public void simulate() throws InterruptedException, IOException {
        while (isRunning) {
            TimeUnit.MILLISECONDS.sleep(100);
            imgPanel.setImage(util.simulate());
        }
    }
}

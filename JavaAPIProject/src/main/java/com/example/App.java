package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;

// i hated looking through this -> https://www.youtube.com/watch?v=Kmgo00avvEw&ab_channel=BroCode

public class App {
    // main application entry point: sets up GUI, menus, event listeners, and handles user interaction
    public static void main(String[] args) {
        // asks for API Key -> mainly for me because I went through so many API Keys testing
        String key = JOptionPane.showInputDialog(null, "Enter your key:", "Key Input", JOptionPane.PLAIN_MESSAGE);

        // initializes main window frame for graph display and menu options
        JFrame frame = new JFrame("Population vs AQI Regression");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setIconImage(new ImageIcon("JavaAPIProject/src/logo.png").getImage());

        // creates and adds a custom JPanel
        GraphPanel graphPanel = new GraphPanel();
        frame.add(graphPanel, BorderLayout.CENTER);

        // constructs menu bar with Data, View, Analysis, and Export menus
        JMenuBar menuBar = new JMenuBar();

        JMenu dataMenu = new JMenu("Data");
        JMenuItem inputSamples = new JMenuItem("Input Samples");
        dataMenu.add(inputSamples);

        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem toggleGrid = new JCheckBoxMenuItem("Toggle Gridlines", true);
        JCheckBoxMenuItem toggleRegression = new JCheckBoxMenuItem("Toggle Regression Line", true);
        viewMenu.add(toggleGrid);
        viewMenu.add(toggleRegression);

        JMenu analysisMenu = new JMenu("Analysis");
        JMenuItem summary = new JMenuItem("Regression Summary");
        JMenuItem detectOutliers = new JMenuItem("Detect Outliers");
        analysisMenu.add(summary);
        analysisMenu.add(detectOutliers);

        JMenu exportMenu = new JMenu("Export");
        JMenuItem exportImage = new JMenuItem("Export Graph as PNG");
        exportMenu.add(exportImage);

        // chatgpted for debugging -> didn't know how to access a regression instance within a try-catch block without an error popping up
        // this is one of the solutions they provided
        final RegressionGraph[] current = new RegressionGraph[1];

        //asks for input sample size to generate regression data and update graph
        inputSamples.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Enter number of samples:");
                try {
                    int num = Integer.parseInt(input);
                    RegressionGraph rg = new RegressionGraph(num, key);
                    current[0] = rg;
                    graphPanel.setData(rg.getPopulations(), rg.getAQIs(), rg.getSlope(), rg.getIntercept());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to generate regression data: " + ex.getMessage());
                }
            }
        });

        // toggles gridline visibility on the graph by repainting panel
        toggleGrid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphPanel.showGrid = toggleGrid.isSelected();
                graphPanel.repaint();
            }
        });

        // toggles regression line visibility on the graph by repainting panel
        toggleRegression.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphPanel.showRegression = toggleRegression.isSelected();
                graphPanel.repaint();
            }
        });

        // displays regression summary dialog if data is there
        summary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (graphPanel.hasData() && current[0] != null) {
                    JOptionPane.showMessageDialog(frame, current[0].getSummary());
                } else {
                    JOptionPane.showMessageDialog(frame, "No data loaded. Please input samples first.");
                }
            }
        });

        // displays list of outlier cities if data is there
        detectOutliers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (graphPanel.hasData() && current[0] != null) {
                    JOptionPane.showMessageDialog(frame, "Outlier Cities:\n" + String.join(", ", current[0].getOutliers()));
                } else {
                    JOptionPane.showMessageDialog(frame, "No data loaded. Please input samples first.");
                }
            }
        });

        // allows exporting current graph view as PNG image via file chooser dialog
        // https://www.youtube.com/watch?v=rEsHS0ov3fw&ab_channel=BrandonGrasley
        exportImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!graphPanel.hasData()) {
                    JOptionPane.showMessageDialog(frame, "No data to export. Please input samples first.");
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Graph as PNG");
                if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    String filename = file.getAbsolutePath();
                    if (!filename.toLowerCase().endsWith(".png")) filename += ".png";

                    try {
                        // creates buffered image of the current graph panel content
                        BufferedImage img = new BufferedImage(graphPanel.getWidth(), graphPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = img.createGraphics();
                        graphPanel.paintAll(g2d);
                        g2d.dispose();

                        // writes image to PNG file
                        ImageIO.write(img, "png", new File(filename));
                        JOptionPane.showMessageDialog(frame, "Graph saved as: " + filename);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving image: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // adds all constructed menus to the menu bar and puts the menubar on the frame
        menuBar.add(dataMenu);
        menuBar.add(viewMenu);
        menuBar.add(analysisMenu);
        menuBar.add(exportMenu);
        frame.setJMenuBar(menuBar);

        // makes the main window visible
        frame.setVisible(true);
    }
}

// responsible for rendering scatter plot and regression line with optional gridlines
class GraphPanel extends JPanel {
    // lists holding population and AQI values for current dataset
    ArrayList<Integer> populations = new ArrayList<>();
    ArrayList<Integer> aqis = new ArrayList<>();

    // checkboxes controlling visibility of grid and regression line
    boolean showGrid = true;
    boolean showRegression = true;

    // default slope and intercept values
    double slope = 0, intercept = 0;

    // returns true if there is data within the population and aqi ArrayList 
    boolean hasData() {
        return !populations.isEmpty() && !aqis.isEmpty();
    }

    // sets new data and regression parameters, then repaints the graph
    void setData(ArrayList<Integer> pops, ArrayList<Integer> aqis, double slope, double intercept) {
        this.populations = pops;
        this.aqis = aqis;
        this.slope = slope;
        this.intercept = intercept;
        repaint();
    }

    // clears existing data and repaints empty graph
    void clearData() {
        populations.clear();
        aqis.clear();
        repaint();
    }

    // similar to an overly; painting method to draw axes, gridlines, data points, and regression line as needed
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth(), h = getHeight();

        // clears background with white fill
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        int padding = 50;                          // padding around graph area for axis labels
        int graphWidth = w - 2 * padding;          // usable width for plotting data
        int graphHeight = h - 2 * padding;         // usable height for plotting data

        // draws gridlines if enabled
        if (showGrid) {
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 10; i++) {
                int x = padding + i * graphWidth / 10;
                int y = h - padding - i * graphHeight / 10;
                g2.drawLine(x, padding, x, h - padding);  // vertical grid lines
                g2.drawLine(padding, y, w - padding, y);  // horizontal grid lines
            }
        }

        // draws axes with thicker black lines
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(padding, padding, padding, h - padding);   // Y axis
        g2.drawLine(padding, h - padding, w - padding, h - padding); // X axis

        // labels axes
        g2.drawString("Population", w / 2 - 30, h - 10);
        g2.drawString("AQI", 10, h / 2);

        // stops if no data to draw
        if (!hasData()) return;

        // determine max values to scale points appropriately
        int maxPop = 1;
        for (int pop : populations) {
            if (pop > maxPop) maxPop = pop;
        }

        int maxAQI = 1;
        for (int aqi : aqis) {
            if (aqi > maxAQI) maxAQI = aqi;
        }

        // plots each data point as a blue circle scaled to graph size
        g2.setColor(Color.BLUE);
        for (int i = 0; i < populations.size(); i++) {
            int x = padding + (int) ((double) populations.get(i) / maxPop * graphWidth);
            int y = h - padding - (int) ((double) aqis.get(i) / maxAQI * graphHeight);
            g2.fillOval(x - 4, y - 4, 8, 8);
        }

        // draws regression line if enabled
        if (showRegression) {
            g2.setColor(Color.RED);
            int x1 = padding;
            int x2 = w - padding;
            int y1 = h - padding - (int) ((slope * 0 + intercept) / maxAQI * graphHeight);
            int y2 = h - padding - (int) ((slope * maxPop + intercept) / maxAQI * graphHeight);
            g2.drawLine(x1, y1, x2, y2);
        }

        // draws tick marks and labels for X and Y axes
        g2.setColor(Color.BLACK);
        for (int i = 0; i <= 10; i++) {
            int x = padding + i * graphWidth / 10;
            int popVal = (int) (i * (maxPop / 10.0));
            g2.drawLine(x, h - padding, x, h - padding + 5);
            g2.drawString(String.valueOf(popVal), x - 10, h - padding + 20);

            int y = h - padding - i * graphHeight / 10;
            int aqiVal = (int) (i * (maxAQI / 10.0));
            g2.drawLine(padding - 5, y, padding, y);
            g2.drawString(String.valueOf(aqiVal), padding - 40, y + 5);
        }
    }
}
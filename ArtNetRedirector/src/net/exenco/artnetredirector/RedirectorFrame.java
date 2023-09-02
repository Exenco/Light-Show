package net.exenco.artnetredirector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class RedirectorFrame extends JFrame {

    private final JLabel logLabel;
    private final JLabel countLabel;
    private final JCheckBox modeCheckbox;

    public RedirectorFrame(ArtNetHandler artNetHandler) {
        setTitle("Art-Net Redirector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(390, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        Font font = new Font("Consolas", Font.PLAIN, 15);
        Font smallFont = font.deriveFont(Font.PLAIN, 13);

        JIpSelector readSelector = new JIpSelector(10, 8, font, "Read address", 6454);
        add(readSelector);

        JIpSelector writeSelector = new JIpSelector(10, 55, font, "Write address", 6454);
        add(writeSelector);

        JLabel keyLabel = new JLabel("Key");
        keyLabel.setFont(smallFont);
        keyLabel.setBounds(10, 127, 50, 20);
        add(keyLabel);
        JTextField keyField = new JTextField();
        keyField.setFont(smallFont);
        keyField.setText("Change me!!!");
        keyField.setBounds(45, 127, 142, 20);
        add(keyField);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBounds(195, 10, 170, 183);
        list.setFont(smallFont);
        list.registerKeyboardAction(e -> {
            if (list.getSelectedIndex() != -1) {
                model.remove(list.getSelectedIndex());
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0, true), JComponent.WHEN_FOCUSED);
        add(scrollPane);

        JButton addButton = new JButton("Add");
        addButton.setFont(font);
        addButton.setBounds(10, 103, 176, 20);
        Insets insets = addButton.getMargin();
        insets.bottom = 0;
        addButton.setMargin(insets);
        addButton.addActionListener(e -> model.add(model.getSize(), writeSelector.getIp() + ":" + writeSelector.getPort()));
        add(addButton);

        modeCheckbox = new JCheckBox("External machine");
        modeCheckbox.setFont(font);
        modeCheckbox.setBounds(10, 147, 176, 20);
        add(modeCheckbox);

        logLabel = new JLabel("");
        logLabel.setFont(font);
        logLabel.setBounds(10, 200, 300, 30);
        add(logLabel);

        countLabel = new JLabel("0");
        countLabel.setFont(font);
        countLabel.setBounds(355, 200, 10, 30);
        add(countLabel);

        JButton startButton = new JButton();
        startButton.setFont(font);
        startButton.setText("Start");
        startButton.setBounds(10, 170, 176, 20);
        startButton.addActionListener(e -> {
            String text = startButton.getText();
            if(text.equals("Start")) {
                List<InetSocketAddress> writeAddressList = new ArrayList<>();
                for(int i = 0; i < model.getSize(); i++) {
                    String address = model.get(i);
                    String[] args = address.split(":");
                    writeAddressList.add(resolveAddress(args[0], Integer.parseInt(args[1])));
                }

                boolean external = modeCheckbox.isSelected();
                int code = artNetHandler.start(resolveAddress(readSelector.getIp(), readSelector.getPort()), external, writeAddressList, keyField.getText(), logLabel, countLabel);
                if(code != -1)
                    startButton.setText("Stop");
            } else if(text.equals("Stop")) {
                artNetHandler.stop();
                startButton.setText("Start");
            }
        });
        add(startButton);
    }

    private InetSocketAddress resolveAddress(String address, int port) {
        if(address == null || address.equals(""))
            address = "127.0.0.1";
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return new InetSocketAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            logLabel.setText("Invalid Ip!");
            e.printStackTrace();
        }
        return null;
    }
}

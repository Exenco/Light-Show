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

    public RedirectorFrame(ArtNetHandler artNetHandler) {
        setTitle("Art-Net Redirector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(390, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        Font font = new Font("Consolas", Font.PLAIN, 15);
        Font smallFont = font.deriveFont(Font.PLAIN, 13);

        JIpSelector readSelector = new JIpSelector(10, 10, font, "Read address", 6454);
        add(readSelector);

        JIpSelector writeSelector = new JIpSelector(10, 70, font, "Write address", 6454);
        add(writeSelector);

        JLabel keyLabel = new JLabel("Key");
        keyLabel.setFont(smallFont);
        keyLabel.setBounds(10, 155, 50, 20);
        add(keyLabel);
        JTextField keyField = new JTextField();
        keyField.setFont(smallFont);
        keyField.setText("ABCDEFGH");
        keyField.setBounds(45, 155, 142, 20);
        add(keyField);

        JLabel ivLabel = new JLabel("IV");
        ivLabel.setFont(smallFont);
        ivLabel.setBounds(10, 175, 50, 20);
        add(ivLabel);
        JTextField ivField = new JTextField();
        ivField.setFont(smallFont);
        ivField.setText("HGFEDCBA");
        ivField.setBounds(45, 175, 142, 20);
        add(ivField);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBounds(195, 10, 170, 185);
        list.setFont(smallFont);
        list.registerKeyboardAction(e -> model.remove(list.getSelectedIndex()), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0, true), JComponent.WHEN_FOCUSED);
        add(scrollPane);

        JButton addButton = new JButton("Add");
        addButton.setFont(font);
        addButton.setBounds(10, 123, 176, 20);
        Insets insets = addButton.getMargin();
        insets.bottom = 0;
        addButton.setMargin(insets);
        addButton.addActionListener(e -> model.add(model.getSize(), writeSelector.getIp() + ":" + writeSelector.getPort()));
        add(addButton);

        logLabel = new JLabel("");
        logLabel.setFont(font);
        logLabel.setBounds(195, 200, 176, 30);
        add(logLabel);

        JButton startButton = new JButton();
        startButton.setFont(font);
        startButton.setText("Start");
        startButton.setBounds(10, 200, 176, 30);
        startButton.addActionListener(e -> {
            String text = startButton.getText();
            if(text.equals("Start")) {
                List<InetSocketAddress> writeAddressList = new ArrayList<>();
                for(int i = 0; i < model.getSize(); i++) {
                    String address = model.get(i);
                    String[] args = address.split(":");
                    writeAddressList.add(resolveAddress(args[0], Integer.parseInt(args[1])));
                }
                int code = artNetHandler.start(resolveAddress(readSelector.getIp(), readSelector.getPort()), writeAddressList, keyField.getText(), ivField.getText(), logLabel);
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
        if(address == null || address.isBlank())
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

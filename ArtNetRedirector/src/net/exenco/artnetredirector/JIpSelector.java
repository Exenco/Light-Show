package net.exenco.artnetredirector;

import javax.swing.*;
import java.awt.*;

class JIpSelector extends JComponent {
    private final JTextField portField;
    private final JTextField ipField;
    JIpSelector(int x, int y, Font font, String text, int defaultPort) {
        setSize(x + 190, y + 60);

        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setBounds(x, y, 190, 25);
        this.ipField = new JTextField();
        this.ipField.setFont(font);
        this.ipField.setBounds(x, y + 23, 127, 25);
        JLabel colonLabel = new JLabel(":");
        colonLabel.setFont(font);
        colonLabel.setBounds(x + 124, y + 22, 10, 25);
        this.portField = new JTextField();
        this.portField.setFont(font);
        this.portField.setText(defaultPort + "");
        this.portField.setBounds(x + 130, y + 23, 47, 25);

        this.add(label);
        this.add(ipField);
        this.add(colonLabel);
        this.add(portField);
    }

    int getPort() {
        return portField.getText().isEmpty() ? 6454 : Integer.parseInt(portField.getText());
    }

    String getIp() {
        return ipField.getText().isEmpty() ? "127.0.0.1" : ipField.getText();
    }
}

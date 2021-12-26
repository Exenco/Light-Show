package net.exenco.artnetredirector;

import javax.swing.*;
import java.awt.*;

class JListElement extends JComponent {
    JListElement(Font font, String ip, int port, JList<?> list) {
        int x = this.getX();
        int y = this.getY();
        setSize(175, 25);

        JLabel ipLabel = new JLabel(ip + ":" + port);
        ipLabel.setFont(font);
        ipLabel.setBounds(x, y, 155, 25);
        add(ipLabel);

        JButton button = new JButton("X");
        button.setFont(font);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBounds(x + 155, y, 19, 20);
        add(button);
    }
}

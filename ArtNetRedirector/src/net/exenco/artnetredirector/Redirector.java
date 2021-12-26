package net.exenco.artnetredirector;

public class Redirector {
    public static void main(String[] args) {
        ArtNetHandler artNetHandler = new ArtNetHandler();
        RedirectorFrame redirectorFrame = new RedirectorFrame(artNetHandler);
        redirectorFrame.setVisible(true);
    }
}

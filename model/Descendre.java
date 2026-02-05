package model;

public class Descendre extends Thread {

    public static final int DELAY = 40;
    private final Position maPosition;
    private final Parcours parcours;

    public Descendre(Position p, Parcours parcours) {
        this.maPosition = p;
        this.parcours = parcours;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            maPosition.fall(); // Ne fera rien si gameOver est true

            // On vérifie la collision et on met à jour l'état si nécessaire
            if (maPosition.checkCollision(parcours)) {
                maPosition.setGameOver(true);
            }

            try {
                sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}
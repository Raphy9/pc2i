package model;

public class Avancer extends Thread {

    public static final int DELAY = 40;
    private final Position position;
    private final int vitesseX;
    private final Parcours parcours;

    public Avancer(Position position, int vitesseX, Parcours parcours) {
        this.position = position;
        this.vitesseX = vitesseX;
        this.parcours = parcours;
        setDaemon(true);
        start();
    }

    // Le constructeur Avancer(Position, int) n'est plus utilisé dans Main, mais on le laisse par cohérence.
    public Avancer(Position position, int vitesseX) {
        this(position, vitesseX, null);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            position.advanceBy(vitesseX); // Ne fera rien si gameOver est true

            if (parcours != null) {
                // On vérifie la collision et on met à jour l'état si nécessaire
                if (position.checkCollision(parcours)) {
                    position.setGameOver(true);
                }
                parcours.onAdvance();
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
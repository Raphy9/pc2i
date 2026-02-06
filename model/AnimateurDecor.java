package model;

/**
 * Thread qui gère l'évolution du décor, c'est-à-dire le déplacement des éléments du parcours
 * (obstacles, pièces, etc.) pour simuler l'avancement du joueur.
 */
public class AnimateurDecor extends Thread {

    // Le décor évolue en fonction du parcours, donc on lui donne une référence au MondeDecor pour qu'il puisse faire évoluer les éléments du décor.
    private final MondeDecor monde;
    // 30 FPS pour le décor (suffisant et fluide)
    private static final int DELAY = 33;

    /** Le constructeur crée un thread AnimateurDecor associé au MondeDecor donné. */
    public AnimateurDecor(MondeDecor monde) {
        this.monde = monde;
        setDaemon(true); // S'arrête avec le programme
        start();
    }

    @Override
    /** Boucle principale : fait évoluer le décor. */
    public void run() {
        while (!isInterrupted()) {
            monde.evoluer();
            try {
                sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}
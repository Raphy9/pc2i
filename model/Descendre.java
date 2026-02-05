package model;

/**
 * Thread qui gère la descente du personnage (gravité)
 */
public class Descendre extends Thread {

    /** Délai entre deux descentes (en ms). */
    public static final int DELAY = 40;

    /** Position associée à ce thread, qui représente l'état du joueur. */
    private final Position maPosition;

    /** Parcours associé à ce thread, nécessaire pour vérifier les collisions. */
    private final Parcours parcours;

    /** Crée un thread Descendre associé à la position et au parcours donnés. */
    public Descendre(Position p, Parcours parcours) {
        this.maPosition = p;
        this.parcours = parcours;
        setDaemon(true);
        start();
    }

    @Override
    /** Boucle principale : fait descendre le personnage et gère les collisions. */
    public void run() {
        // La gravité est simulée en faisant descendre le personnage à chaque itération de la boucle.
        while (!isInterrupted()) {
            // On fait descendre le personnage
            maPosition.fall();

            // Si collision, on demande à Position de gérer
            if (maPosition.checkCollision(parcours)) {
                maPosition.gererCollision(parcours);
            }
            // On notifie le parcours que le personnage a avancé, pour mettre à jour les éléments du parcours
            try {
                sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}
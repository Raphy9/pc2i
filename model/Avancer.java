package model;

/**
 * Thread qui gère l'avancement du personnage (défilement du parcours)
 */
public class Avancer extends Thread {

    /** Délai entre deux avancées (en ms). */
    public static final int DELAY = 40;
    /** Position associée à ce thread, qui représente l'état du joueur. */
    private final Position position;
    /** Vitesse d'avancement en pixels par itération. */
    private final int vitesseX;
    /** Parcours associé à ce thread, nécessaire pour vérifier les collisions et mettre à jour le parcours. */
    private final Parcours parcours;

    /** Crée un thread Avancer associé à la position, à la vitesse d'avancement et au parcours donnés. */
    public Avancer(Position position, int vitesseX, Parcours parcours) {
        this.position = position;
        this.vitesseX = vitesseX;
        this.parcours = parcours;
        setDaemon(true);
        start();
    }

    /** Constructeur de commodité pour créer un thread Avancer sans parcours (par exemple, pour les tests). */
    public Avancer(Position position, int vitesseX) {
        this(position, vitesseX, null);
    }

    @Override
    /** Boucle principale : fait avancer le personnage et gère les collisions. */
    public void run() {
        // La progression est simulée en avançant le personnage à chaque itération de la boucle.
        while (!isInterrupted()) {
            position.advanceBy(vitesseX);

            // Si un parcours est associé, on vérifie les collisions et on notifie le parcours de l'avancement
            if (parcours != null) {
                // Si collision, on demande à Position de gérer (perte de vie ou fin)
                if (position.checkCollision(parcours)) {
                    position.gererCollision(parcours);
                }
                parcours.onAdvance();
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
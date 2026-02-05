package model;

/**
 * Modèle représentant la position verticale de l'ovale et son avancement horizontal.
 * Respecte les invariants : HAUTEUR_MIN <= hauteur <= HAUTEUR_MAX - HAUTEUR_OVALE
 */
public class Position {

    /** Constantes du modèle */
    public static final int POS_DEPART = 100;
    public static final int IMPULSION = 15; // saut augmente la hauteur

    /** Hauteur de l'ovale (pour les calculs de collision) */
    public static final int HAUTEUR_OVALE = 50;
    public static final int LARGEUR_OVALE = 20; // Ajout pour la précision
    public static final int HAUTEUR_MIN = 0;
    public static final int HAUTEUR_MAX = 200;

    /** Position horizontale du premier point du parcours (avant l'écran) */
    public static final int BEFORE = 20;
    public static final int AFTER = 100;

    /** Vitesse de descente (en unités modèle par appel de fall) */
    private final int vitesse = 2;
    private int hauteur = POS_DEPART;

    /** État du jeu : en cours ou terminé */
    private boolean gameOver = false; // Le booléen pour l'état du jeu

    /** Avancement horizontal (en unités modèle) */
    private int avancement = 0;

    /**
     * Retourne la hauteur actuelle (valeur modèle).
     */
    public synchronized int getPosition() {
        return hauteur;
    }

    /**
     * Retourne true si la partie est terminée.
     */
    public synchronized boolean isGameOver() {
        return gameOver;
    }

    /**
     * Met à jour l'état de la partie.
     * Appelé par les threads Avancer et Descendre.
     */
    public synchronized void setGameOver(boolean status) {
        this.gameOver = status;
    }

    /**
     * Effectue un saut : augmente la hauteur en respectant la borne haute utile.
     */
    public synchronized void jump() {
        if (gameOver) return;
        int hauteurMaxUtile = HAUTEUR_MAX - HAUTEUR_OVALE;
        if (hauteur < hauteurMaxUtile) {
            hauteur += IMPULSION;
            if (hauteur > hauteurMaxUtile) {
                hauteur = hauteurMaxUtile;
            }
        }
    }

    /**
     * Fait descendre l'objet progressivement (appelé par un thread du modèle).
     */
    public synchronized void fall() {
        if (gameOver) return;
        if (hauteur > HAUTEUR_MIN) {
            hauteur -= vitesse;
            if (hauteur < HAUTEUR_MIN) {
                hauteur = HAUTEUR_MIN;
            }
        }
    }

    /**
     * Vérifie si l'ovale est en collision avec le parcours.
     * @param parcours Le parcours de la ligne.
     * @return true en cas de collision, false sinon.
     */
    public synchronized boolean checkCollision(Parcours parcours) {
        // Coordonnée X absolue du centre de l'ovale
        int xCentreOvale = this.avancement + (LARGEUR_OVALE / 2);

        // Hauteur du sol à la position du centre de l'ovale
        int hauteurLigne = parcours.getHauteur(xCentreOvale);

        // Le bas de l'ovale est à 'hauteur'. Collision si sa hauteur est <= à celle de la ligne.
        return this.hauteur <= hauteurLigne;
    }


    /**
     * Retourne l'avancement horizontal (en unités modèle).
     */
    public synchronized int getAvancement() {
        return avancement;
    }

    /**
     * Avance la valeur d'avancement de dx unités (peut être négatif).
     */
    public synchronized void advanceBy(int dx) {
        if (gameOver) return;
        avancement += dx;
    }
}

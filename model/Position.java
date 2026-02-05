package model;

/** La classe Position représente l'état du joueur dans le jeu, incluant sa hauteur, ses vies, son avancement,
 * et l'état global du jeu (menu ou jeu en cours). Elle gère les actions du joueur (saut, chute) et les collisions
 * avec le parcours. Les méthodes sont synchronisées pour garantir la cohérence des données lors de l'accès
 * depuis différents threads (vue, contrôleur).
 */
public class Position {

    /**
     * L'état global du jeu : Menu ou Jeu en cours (incluant Game Over).
     * Cela permet de différencier les comportements et les affichages.
     */
    public enum Etat {
        MENU, JEU
    }
    /**
     * L'état actuel du jeu. Par défaut, on commence sur le menu.
     * Cet état est utilisé pour contrôler les actions possibles et l'affichage.
     */
    private Etat etatCurrent = Etat.MENU; // On commence sur le menu

    /** --- CONSTANTES DE JEU ---
     * POS_DEPART : hauteur de départ du joueur (position initiale).
     * IMPULSION : force du saut, c'est-à-dire l'augmentation de la hauteur lors d'un saut.
     * HAUTEUR_OVALE : hauteur de l'ovale représentant le joueur, utilisée pour les calculs de collision.
     * HAUTEUR_MIN et HAUTEUR_MAX : limites de hauteur pour le joueur, pour éviter de sortir du parcours.
     * BEFORE et AFTER : distances avant et après le joueur pour calculer la position horizontale dans le parcours lors des collisions.
     */
    public static final int POS_DEPART = 100;
    public static final int IMPULSION = 15;
    public static final int HAUTEUR_OVALE = 50;
    public static final int HAUTEUR_MIN = 0;
    public static final int HAUTEUR_MAX = 200;
    public static final int BEFORE = 20;
    public static final int AFTER = 100;

    /** Délai d'invulnérabilité après une collision (en ms).
    Pendant ce temps, le joueur ne peut pas perdre de vie supplémentaire. */
    private static final int DELAI_INVULNERABILITE = 500;
    private long tempsDernierChoc = 0;

    /** --- VARIABLES DE JEU ---
     * vies : nombre de vies restantes du joueur. Le joueur commence avec 3 vies.
     * vitesse : vitesse de chute du joueur (constante).
     * hauteur : hauteur actuelle du joueur, initialisée à la position de départ.
     * gameOver : indique si le joueur est mort (plus de vies). Initialement false
     * avancement : distance parcourue par le joueur, utilisée pour calculer la position horizontale dans le parcours.
     */
    private int vies = 3;
    private final int vitesse = 2;
    private int hauteur = POS_DEPART;
    private boolean gameOver = false;
    private int avancement = 0;

    /** --- GESTION DE L'ÉTAT GLOBAL (Menu/Jeu) ---
     * Ces méthodes permettent de gérer l'état global du jeu, qui influence
     * les comportements (saut, chute, collisions) et les affichages (menu, jeu, game over).
     * Elles sont synchronisées pour garantir la cohérence des données lors de l'accès
     * depuis différents threads (vue, contrôleur).
     */

    public synchronized Etat getEtat() {
        return etatCurrent;
    }

    public synchronized void setEtat(Etat e) {
        this.etatCurrent = e;
    }

    /** La méthode reset() remet le jeu à son état initial pour une nouvelle partie.
     * Elle réinitialise les vies, la hauteur, l'avancement, le statut de game over,
     * et le temps du dernier choc pour garantir une partie propre.
     */
    public synchronized void reset() {
        this.vies = 3;
        this.hauteur = POS_DEPART;
        this.avancement = 0;
        this.gameOver = false;
        this.tempsDernierChoc = 0;
        this.etatCurrent = Etat.JEU;
    }

    /** --- GETTERS (Safe) ---

     * Ces méthodes sont synchronisées pour garantir la cohérence des données
     * lorsqu'elles sont appelées depuis différents threads (vue, contrôleur).
     */

    public synchronized int getPosition() {
        return hauteur;
    }

    public synchronized boolean isGameOver() {
        return gameOver;
    }

    public synchronized int getAvancement() {
        return avancement;
    }

    public synchronized int getVies() {
        return vies;
    }

    /** La méthode jump() permet au joueur de sauter, en augmentant sa hauteur.
     * Le saut n'est possible que si le jeu est en cours et que le joueur n'est pas mort.
     * La hauteur maximale du saut est limitée pour éviter de dépasser les limites du parcours.
     */
    public synchronized void jump() {
        // Le saut n'est permis que si on joue et qu'on n'est pas mort
        if (etatCurrent != Etat.JEU || gameOver) return;

        // La hauteur maximale utile est calculée pour éviter de dépasser le plafond du parcours
        int hauteurMaxUtile = HAUTEUR_MAX - HAUTEUR_OVALE;

        // On augmente la hauteur du joueur, mais on la limite à hauteurMaxUtile
        if (hauteur < hauteurMaxUtile) {
            hauteur += IMPULSION;
            // Si la hauteur dépasse la limite maximale utile, on la ramène à cette limite
            if (hauteur > hauteurMaxUtile) {
                hauteur = hauteurMaxUtile;
            }
        }
    }

    /** La méthode fall() fait tomber le joueur progressivement, en diminuant sa hauteur.
     * La chute n'est possible que si le jeu est en cours et que le joueur n'est pas mort.
     * La hauteur minimale est limitée pour éviter de descendre sous le sol du parcours.
     */
    public synchronized void fall() {
        // La chute n'est permise que si on joue et qu'on n'est pas mort
        if (etatCurrent != Etat.JEU || gameOver) return;

        // On diminue la hauteur du joueur, mais on la limite à HAUTEUR_MIN
        if (hauteur > HAUTEUR_MIN) {
            hauteur -= vitesse;
            // Si la hauteur descend en dessous de la limite minimale, on la ramène à cette limite
            if (hauteur < HAUTEUR_MIN) {
                hauteur = HAUTEUR_MIN;
            }
        }
    }

    /** La méthode advanceBy() fait avancer le joueur horizontalement, en augmentant son avancement.
     * L'avancement n'est possible que si le jeu est en cours et que le joueur n'est pas mort.
     * Cette méthode est appelée périodiquement pour simuler le mouvement du joueur à travers le parcours.
     */
    public synchronized void advanceBy(int dx) {
       // L'avancement n'est permis que si on joue et qu'on n'est pas mort
        if (etatCurrent != Etat.JEU || gameOver) return;
        avancement += dx;
    }

    /** --- COLLISIONS ---

     * La méthode checkCollision vérifie si le joueur est en collision avec le parcours.
     * Elle prend en compte l'état du jeu et le délai d'invulnérabilité pour éviter
     * les multiples collisions en un court laps de temps.
     * La méthode gerer Collision doit être appelée lorsque checkCollision retourne true,
     * pour mettre à jour l'état du jeu (perte de vie, repositionnement, etc.)
     */
    public synchronized boolean checkCollision(Parcours parcours) {
        // Pas de collision dans le menu
        if (etatCurrent != Etat.JEU) return false;

        // Si on est dans la période d'invulnérabilité après un choc, on ignore les collisions
        if (System.currentTimeMillis() - tempsDernierChoc < DELAI_INVULNERABILITE) {
            return false;
        }

        // On calcule la position horizontale du centre de l'ovale du joueur pour vérifier la collision
        int xCentreOvale = this.avancement + BEFORE;
        int hauteurLigne = parcours.getHauteur(xCentreOvale);
        int basDeLOvale = this.hauteur;
        int hautDeLOvale = this.hauteur + HAUTEUR_OVALE;

        // La collision se produit si la ligne du parcours est à l'intérieur de l'ovale du joueur
        boolean ligneDanslOvale = (hauteurLigne <= hautDeLOvale) && (hauteurLigne >= basDeLOvale);
        return !ligneDanslOvale;
    }

    /** La méthode gererCollision gère les conséquences d'une collision détectée par checkCollision.
     * Elle diminue le nombre de vies du joueur, vérifie si le joueur est mort (game over),
     * et repositionne le joueur sur la ligne du parcours si il a encore des vies restantes.
     * La méthode est synchronisée pour garantir la cohérence des données lors de l'accès
     * depuis différents threads (vue, contrôleur).
     */
    public synchronized void gererCollision(Parcours parcours) {
       // La gestion de collision n'est permise que si on joue et qu'on n'est pas mort
        if (etatCurrent != Etat.JEU || gameOver) return;

        // On vérifie le délai d'invulnérabilité pour éviter les multiples collisions en un court laps de temps
        long maintenant = System.currentTimeMillis();
        if (maintenant - tempsDernierChoc < DELAI_INVULNERABILITE) {
            return;
        }
        // On met à jour le temps du dernier choc pour activer l'invulnérabilité
        tempsDernierChoc = maintenant;

        // On perd une vie
        vies--;

        // Si le joueur n'a plus de vies, c'est la fin du jeu (game over)
        if (vies <= 0) {
            vies = 0;
            gameOver = true;
            // On reste en état JEU pour afficher le Game Over par dessus
        } else { // Si le joueur a encore des vies, on le repositionne sur la ligne du parcours pour éviter une mort immédiate
            int xCentreOvale = this.avancement + BEFORE;
            int hauteurLigne = parcours.getHauteur(xCentreOvale);
            this.hauteur = hauteurLigne - (HAUTEUR_OVALE / 2);
            // On s'assure que la hauteur reste dans les limites du parcours
            if (this.hauteur < HAUTEUR_MIN) this.hauteur = HAUTEUR_MIN;
            if (this.hauteur > HAUTEUR_MAX - HAUTEUR_OVALE) {
                this.hauteur = HAUTEUR_MAX - HAUTEUR_OVALE;
            }
        }
    }

    /**
     * Retourne vrai si le joueur est actuellement invulnérable (vient de toucher).
     * Utile pour le feedback visuel (clignotement).
     */
    public synchronized boolean estInvulnerable() {
        return (System.currentTimeMillis() - tempsDernierChoc) < DELAI_INVULNERABILITE;
    }
}
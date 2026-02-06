package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Le parcours est défini par une liste de points (x, y) formant une ligne continue.
 * La hauteur du parcours à une position x donnée est calculée par interpolation linéaire
 * entre les deux points encadrant x.
 *
 * Le parcours est généré de manière procédurale, avec des segments de longueur aléatoire
 * et des hauteurs aléatoires dans une bande définie pour éviter les extrêmes.
 *
 * Le parcours se réinitialise à chaque nouvelle partie, et s'adapte à l'avancement du joueur
 * en ajoutant de nouveaux points au fur et à mesure que le joueur avance.
 */
public class Parcours {

    /** Les constantes définissent les paramètres de génération du parcours :
     * - X_MIN et X_MAX définissent la longueur minimale et maximale des segments horizontaux
     * - BAND_DIV définit la division de la hauteur pour créer une bande de génération
     * - END_MARGIN définit une marge supplémentaire pour générer des points au-delà de la zone visible
     * La génération du parcours utilise un objet Random pour créer des segments et des hauteurs variés,
     * tout en respectant les contraintes définies par les constantes.
     */
    private static final Random RNG = new Random();
    public static final int X_MIN = 10;
    public static final int X_MAX = 40;
    private static final int BAND_DIV = 3;
    private static final int END_MARGIN = 100;

    /** La liste de points du parcours est protégée par une synchronisation pour garantir la cohérence
     * lors de l'accès depuis différents threads (vue, contrôleur). La position du joueur est également
     * utilisée pour adapter le parcours à l'avancement du joueur, en générant de nouveaux points au fur
     * et à mesure que le joueur avance.
     */
    private final ArrayList<Point> points = new ArrayList<>();
    private final Position position;
    private final ArrayList<Item> items = new ArrayList<>();

    /** Le constructeur initialise le parcours en générant les points de départ, en fonction de la position initiale du joueur.
     * Il utilise la méthode initPoints() pour créer une liste de points formant le parcours, en respectant les contraintes
     * définies par les constantes et en adaptant les hauteurs à la position initiale du joueur.
     */
    public Parcours(Position position) {
        this.position = position;
        initPoints();
    }

    /**
     * Réinitialise le parcours pour une nouvelle partie
     */
    public synchronized void reset() {
        points.clear();
        initPoints();
        items.clear();
    }

    /** La méthode getHauteur(int xAbsolu) calcule la hauteur du parcours à une position x donnée en effectuant une interpolation linéaire
     * entre les deux points encadrant x. Si x est en dehors de la plage des points, elle retourne la hauteur minimale du parcours.
     * La synchronisation garantit que les données du parcours sont cohérentes lors de l'accès depuis différents threads.
     */
    public synchronized int getHauteur(int xAbsolu) {
       // On cherche les deux points encadrant xAbsolu
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            // Si xAbsolu est entre p1.x et p2.x, on fait une interpolation linéaire pour trouver la hauteur correspondante
            if (xAbsolu >= p1.x && xAbsolu <= p2.x) {
                double x1 = p1.x, y1 = p1.y;
                double x2 = p2.x, y2 = p2.y;
                // Si les deux points ont la même x (ce qui ne devrait pas arriver), on retourne simplement y1
                if (x1 == x2) return (int) y1;
                double hauteur = y1 + (xAbsolu - x1) * (y2 - y1) / (x2 - x1);
                return (int) hauteur;
            }
        }
        return Position.HAUTEUR_MIN;
    }

    /** La méthode initPoints() génère les points du parcours de manière procédurale, en respectant les contraintes définies par les constantes.
     * Elle crée une liste de points formant une ligne continue, avec des segments de longueur aléatoire et des hauteurs aléatoires dans une bande définie.
     * La synchronisation garantit que les données du parcours sont cohérentes lors de l'accès depuis différents threads.
     */
    private void initPoints() {
        points.clear();
        int demiHauteur = Position.HAUTEUR_OVALE / 2;
        // Si on reset, position.getPosition() est remis à POS_DEPART (100)
        int yDepart = position.getPosition() + demiHauteur;

        // Point de départ avant la zone visible
        int x = -Position.BEFORE;
        points.add(new Point(x, yDepart));

        // Point de sécurité pour éviter les obstacles trop proches du départ
        int xSecurite = 50;
        points.add(new Point(xSecurite, yDepart));

        // Génération des points jusqu'à la fin de la zone visible + marge
        int lastX = xSecurite;
        int totalRange = Position.HAUTEUR_MAX - Position.HAUTEUR_MIN;
        int minY = Position.HAUTEUR_MIN + totalRange / BAND_DIV;
        int maxY = Position.HAUTEUR_MIN + totalRange * (BAND_DIV - 1) / BAND_DIV;
        int spanY = Math.max(1, maxY - minY + 1);

        // On génère des points jusqu'à la fin de la zone visible + marge
        while (lastX <= Position.AFTER + END_MARGIN) {
            int dx = X_MIN + RNG.nextInt(X_MAX - X_MIN + 1);
            lastX += dx;
            int newY = minY + RNG.nextInt(spanY);
            points.add(new Point(lastX, newY));
        }
    }

    /** La méthode getPoints() retourne une copie de la liste de points du parcours, avec les coordonnées x ajustées en fonction de l'avancement du joueur.
     * Cela permet à la vue d'afficher le parcours de manière relative à la position du joueur, en décalant les points pour simuler le mouvement.
     * La synchronisation garantit que les données du parcours sont cohérentes lors de l'accès depuis différents threads.
     */
    public List<Point> getPoints() {
        // L'avancement du joueur est utilisé pour ajuster les coordonnées x des points, afin de simuler le mouvement du parcours par rapport au joueur.
        int adv = position != null ? position.getAvancement() : 0;
        synchronized (this) {
            // On crée une nouvelle liste de points avec les coordonnées x ajustées en fonction de l'avancement du joueur
            List<Point> res = new ArrayList<>(points.size());
            for (Point p : points) {
                res.add(new Point(p.x - adv, p.y));
            }
            return res;
        }
    }

    /** La méthode getItems() retourne une liste des items présents sur le parcours, avec les coordonnées x ajustées en fonction de l'avancement du joueur.
     * Seuls les items non ramassés sont retournés, et ils sont créés comme des objets temporaires pour l'affichage.
     * La synchronisation garantit que les données du parcours sont cohérentes lors de l'accès depuis différents threads.
     */
    public List<Item> getItems() {
        int adv = position != null ? position.getAvancement() : 0;
        synchronized (this) {
            List<Item> res = new ArrayList<>();
            for (Item i : items) {
                // On ne renvoie que les items non ramassés
                if (!i.isRamasse()) {
                    // On crée un item temporaire décalé pour l'affichage
                    res.add(new Item(i.getX() - adv, i.getY(), i.getType()));
                }
            }
            return res;
        }
    }

    /** La méthode onAdvance() est appelée à chaque fois que le joueur avance, pour mettre à jour le parcours en fonction de l'avancement du joueur.
     * Elle supprime les points qui sont passés (dont la coordonnée x ajustée est inférieure à -Position.BEFORE), et génère de nouveaux points au-delà de la zone visible + marge.
     * La synchronisation garantit que les données du parcours sont cohérentes lors de l'accès depuis différents threads, et que les modifications du parcours sont atomiques par
     * rapport à l'avancement du joueur. Cette méthode est essentielle pour maintenir un parcours dynamique qui s'adapte à la progression du joueur, en assurant que le parcours
     * reste cohérent et fluide au fur et à mesure que le joueur avance.
     */
    public void onAdvance() {
        // L'avancement du joueur est utilisé pour ajuster les coordonnées x des points, afin de déterminer quels points sont passés et où générer de nouveaux points.
        int adv = position.getAvancement();
        synchronized (this) {
            // On supprime les points qui sont passés (dont la coordonnée x ajustée est inférieure à -Position.BEFORE)
            if (points.size() < 2) return;
            Point second = points.get(1);
            int secondXInView = second.x - adv;
            // Tant que le deuxième point (le premier point restant) est passé, on supprime le premier point
            if (secondXInView < -Position.BEFORE) {
                points.remove(0);
            }

            Iterator<Item> it = items.iterator();
            while (it.hasNext()) {
                Item i = it.next();
                if (i.getX() - adv < -Position.BEFORE) {
                    it.remove();
                }
            }

            // On génère de nouveaux points au-delà de la zone visible + marge
            int lastX = points.get(points.size() - 1).x;
            int target = adv + Position.AFTER + END_MARGIN;
            int totalRange = Position.HAUTEUR_MAX - Position.HAUTEUR_MIN;
            int minY = Position.HAUTEUR_MIN + totalRange / BAND_DIV;
            int maxY = Position.HAUTEUR_MIN + totalRange * (BAND_DIV - 1) / BAND_DIV;
            int spanY = Math.max(1, maxY - minY + 1);
            // Tant que le dernier point généré est avant la cible (avancement + zone visible + marge), on génère un nouveau point
            while (lastX < target) {
                int dx = X_MIN + RNG.nextInt(X_MAX - X_MIN + 1);
                lastX += dx;
                int newY = minY + RNG.nextInt(spanY);
                points.add(new Point(lastX, newY));
                if (RNG.nextDouble() < 0.3) { // 30% de chance d'avoir un item par segment
                    Item.Type t = RNG.nextBoolean() ? Item.Type.PIECE : Item.Type.OBSTACLE;
                    // On place l'item un peu au-dessus de la ligne (y - 30)
                    items.add(new Item(lastX, newY - 30, t));
                }
            }
        }
    }

    /** La méthode getItemsReels() retourne la liste réelle des items du parcours, avec leurs coordonnées absolues (non ajustées).
     * Cette méthode est principalement utilisée pour les tests, afin de vérifier l'état réel des items sur le parcours.
     * La synchronisation garantit que les données du parcours sont cohérentes lors de l'accès depuis différents threads.
     */
    public synchronized List<Item> getItemsReels() {
        return items;
    }

}
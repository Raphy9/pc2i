package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Générateur minimal de parcours (ligne brisée) pour l'exercice 4.
 * - deux premiers points ont la même Y que la position initiale
 * - Y entre HAUTEUR_MIN et HAUTEUR_MAX
 * - X croissant, premier point en -BEFORE, dernier au-delà d'AFTER
 */
public class Parcours {

    private static final Random RNG = new Random();

    // écart min/max en X entre deux points consécutifs (en unités modèles)
    public static final int X_MIN = 10;
    public static final int X_MAX = 40;

    // Contrôle la bande verticale centrée
    private static final int BAND_DIV = 3;

    private final ArrayList<Point> points = new ArrayList<>();
    private final Position position;

    // marge pour générer un point supplémentaire devant la fenêtre
    private static final int END_MARGIN = 100;

    public Parcours(Position position) {
        this.position = position;
        initPoints();
    }

    /**
     * Calcule et retourne la hauteur de la ligne à une abscisse absolue donnée
     * par interpolation linéaire.
     * @param xAbsolu La coordonnée X absolue.
     * @return La hauteur Y correspondante sur la ligne.
     */
    public synchronized int getHauteur(int xAbsolu) {
        // Trouve le segment [p1, p2] qui contient xAbsolu
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);

            if (xAbsolu >= p1.x && xAbsolu <= p2.x) {
                // Interpolation linéaire pour trouver le y
                double x1 = p1.x, y1 = p1.y;
                double x2 = p2.x, y2 = p2.y;

                if (x1 == x2) return (int) y1; // Segment vertical

                double hauteur = y1 + (xAbsolu - x1) * (y2 - y1) / (x2 - x1);
                return (int) hauteur;
            }
        }
        // Si on est en dehors du parcours généré, on retourne une valeur sûre.
        return Position.HAUTEUR_MIN;
    }


    /** Initialisation complète de la liste de points. */
    private void initPoints() {
        points.clear();
        // premier point avant l'horizon
        int x = -Position.BEFORE;
        int y = position.getPosition();
        points.add(new Point(x, y));
        // deuxième point proche du premier, même hauteur
        x += X_MIN;
        points.add(new Point(x, y));

        int lastX = x;
        // générer jusqu'à dépasser AFTER
        int totalRange = Position.HAUTEUR_MAX - Position.HAUTEUR_MIN;
        // calculer la bande centrale selon BAND_DIV
        int minY = Position.HAUTEUR_MIN + totalRange / BAND_DIV;
        int maxY = Position.HAUTEUR_MIN + totalRange * (BAND_DIV - 1) / BAND_DIV;
        int spanY = Math.max(1, maxY - minY + 1);

        while (lastX <= Position.AFTER + END_MARGIN) {
            int dx = X_MIN + RNG.nextInt(X_MAX - X_MIN + 1);
            lastX += dx;
            int newY = minY + RNG.nextInt(spanY);
            points.add(new Point(lastX, newY));
        }
    }

    /** Retourne une copie de la liste de points (X croissants) décalés par l'avancement. */
    public synchronized List<Point> getPoints() {
        int adv = position != null ? position.getAvancement() : 0;
        List<Point> res = new ArrayList<>(points.size());
        for (Point p : points) {
            res.add(new Point(p.x - adv, p.y));
        }
        return res;
    }

    /**
     * Mise à jour incrémentale de la liste lorsqu'on avance :
     * - supprimer le 2ème point si il sortement laissé de la fenêtre
     * - ajouter des points à droite si le dernier entre dans la fenêtre
     */
    public synchronized void onAdvance() {
        if (points.size() < 2) return;
        int adv = position.getAvancement();
        // vérifier le deuxième point (index 1)
        Point second = points.get(1);
        int secondXInView = second.x - adv;
        if (secondXInView < -Position.BEFORE) {
            // supprimer le premier point (on supprime l'élément 0)
            points.remove(0);
        }
        // vérifier le dernier point et ajouter si nécessaire pour dépasser AFTER
        int lastX = points.get(points.size() - 1).x;
        int target = adv + Position.AFTER + END_MARGIN;
        int totalRange = Position.HAUTEUR_MAX - Position.HAUTEUR_MIN;
        int minY = Position.HAUTEUR_MIN + totalRange / BAND_DIV;
        int maxY = Position.HAUTEUR_MIN + totalRange * (BAND_DIV - 1) / BAND_DIV;
        int spanY = Math.max(1, maxY - minY + 1);
        while (lastX < target) {
            int dx = X_MIN + RNG.nextInt(X_MAX - X_MIN + 1);
            lastX += dx;
            int newY = minY + RNG.nextInt(spanY);
            points.add(new Point(lastX, newY));
        }
    }

    /** Test rapide : affiche les points générés. */
    public static void main(String[] args) {
        Position p = new Position();
        Parcours parc = new Parcours(p);
        for (Point pt : parc.getPoints()) {
            System.out.println(pt);
        }
    }
}

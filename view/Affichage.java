package view;

import model.Position;
import model.Parcours;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

/**
 * Vue : dessine l'ovale et la ligne brisée fournie par un Parcours.
 */
public class Affichage extends JPanel {
    // Largeur de l'ovale (vue)
    public static final int LARGEUR = 50;

    // Ratios pour transformation modèle -> vue (entiers comme demandé)
    public static final int RATIO_X = 5;
    public static final int RATIO_Y = 2;

    // Calculs basés sur les constantes du modèle
    public static final int POSITION_X = Position.BEFORE * RATIO_X - LARGEUR / 2;
    public static final int VIEW_ZONE_X = (Position.BEFORE + Position.AFTER) * RATIO_X;
    public static final int VIEW_ZONE_Y = (Position.HAUTEUR_MAX - Position.HAUTEUR_MIN) * RATIO_Y;

    private final Position maPosition;
    private final Parcours monParcours;

    // Constructeur principal qui accepte un Parcours (pour dessiner la ligne brisée)
    public Affichage(Position p, Parcours parcours) {
        setPreferredSize(new Dimension(VIEW_ZONE_X, VIEW_ZONE_Y));
        maPosition = p;
        monParcours = parcours;
    }

    // Constructeur simple (compatibilité) : pas de Parcours
    public Affichage(Position p) {
        this(p, null);
    }

@Override
   protected void paintComponent(Graphics g) {
       super.paintComponent(g);
       // utiliser Graphics2D pour pouvoir régler l'épaisseur des traits
       java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
       java.awt.Stroke oldStroke = g2.getStroke();
       java.awt.Color oldColor = g2.getColor();

       // dessiner la ligne brisée si présente avec une épaisseur
       g2.setStroke(new java.awt.BasicStroke(3f)); // épaisseur de la ligne brisée
       g2.setColor(java.awt.Color.BLACK);
       drawParcours(g2);

       // dessiner l'ovale avec un trait plus large et en orange
       g2.setStroke(new java.awt.BasicStroke(5f));
       g2.setColor(java.awt.Color.ORANGE);
       int posY = (Position.HAUTEUR_MAX - maPosition.getPosition() - Position.HAUTEUR_OVALE) * RATIO_Y;
       int ovalHeight = Position.HAUTEUR_OVALE * RATIO_Y;
       g2.drawOval(POSITION_X, posY, LARGEUR, ovalHeight);

       g2.setStroke(oldStroke);
       g2.setColor(oldColor);
       g2.dispose();
   }

    /** Dessine la ligne brisée fournie par le modèle Parcours. */
    private void drawParcours(Graphics g) {
        if (monParcours == null) return;
        List<Point> pts = monParcours.getPoints();
        if (pts.size() < 2) return;
        Point prev = modelToView(pts.get(0));
        for (int i = 1; i < pts.size(); i++) {
            Point cur = modelToView(pts.get(i));
            g.drawLine(prev.x, prev.y, cur.x, cur.y);
            prev = cur;
        }
    }

    // transforme un point du modèle (xM,yM) -> point de la vue (xV,yV)
    private Point modelToView(Point pm) {
        int xV = pm.x * RATIO_X;
        int yV = (Position.HAUTEUR_MAX - pm.y) * RATIO_Y;
        return new Point(xV, yV);
    }
}

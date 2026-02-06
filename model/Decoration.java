package model;

import java.awt.Color;
import java.util.Random;

/**
 * La classe Decoration représente un élément décoratif du parcours, comme un nuage ou un oiseau.
 * Ces éléments sont purement visuels et n'ont pas d'impact sur le gameplay, mais ils ajoutent de la vie au décor.
 * Ils apparaissent à gauche de l'écran et se déplacent vers la droite à une vitesse qui dépend de leur type.
 */
public class Decoration {

    /** Le type de décoration, qui peut être un nuage ou un oiseau. Le type détermine la vitesse et la taille de la décoration. */
    public enum Type {
        NUAGE,  // Flotte doucement
        OISEAU  // Vole plus vite
    }

    /** Les coordonnées (x, y) représentent la position de la décoration à l'écran. La vitesse en x détermine à quelle vitesse elle se déplace vers la droite. */
    private float x, y;
    private float vitesseX;
    private final Type type;
    private final int taille; // Facteur d'échelle
    private boolean aDetruire = false;

    /** La classe utilise une instance de Random pour générer des caractéristiques aléatoires pour chaque décoration, comme le type, la position verticale, la vitesse et la taille. */
    private static final Random RND = new Random();

    /** Le constructeur crée une décoration avec des caractéristiques aléatoires en fonction de la hauteur de l'écran. */
    public Decoration(int hauteurEcran) {
        // Type aléatoire (70% Nuage, 30% Oiseau)
        this.type = RND.nextDouble() < 0.6 ? Type.NUAGE : Type.OISEAU;

        // Apparition à gauche (hors écran)
        this.x = -100;

        // Caractéristiques aléatoires en fonction du type
        if (this.type == Type.NUAGE) {
            // Nuage : Haut du ciel, lent, gros
            this.y = RND.nextInt(hauteurEcran / 3); // Tiers supérieur
            this.vitesseX = 0.5f + RND.nextFloat(); // Vitesse entre 0.5 et 1.5
            this.taille = 50 + RND.nextInt(30);
        } else {
            // Oiseau : Milieu du ciel, rapide, petit
            this.y = RND.nextInt(hauteurEcran / 2);
            this.vitesseX = 2.0f + RND.nextFloat() * 2.0f; // Vitesse entre 2 et 4
            this.taille = 20 + RND.nextInt(10);
        }
    }

    /** La méthode avancer() est appelée à chaque itération de la boucle de jeu pour faire avancer la décoration vers la droite en fonction de sa vitesse. */
    public void avancer() {
        this.x += vitesseX;
    }

    /** Getters pour les coordonnées, le type et la taille de la décoration. */
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public Type getType() { return type; }
    public int getTaille() { return taille; }

    /** Méthodes pour marquer la décoration à détruire lorsqu'elle sort de l'écran. */
    public void setADetruire(boolean b) { this.aDetruire = b; }
    public boolean isADetruire() { return aDetruire; }
}
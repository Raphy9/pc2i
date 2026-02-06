package model;

import java.awt.Point;

/**
 * Un item représente un élément bonus ou malus placé sur le parcours à une position (x, y).
 */
public class Item {

    /** Le type de l'item, qui peut être un bonus (PIECE) ou un malus (OBSTACLE). */
    public enum Type {
        PIECE,   // Bonus : Donne du score
        OBSTACLE // Malus : Enlève une vie
    }

    /** Les coordonnées (x, y) sont absolues par rapport au parcours, c'est-à-dire qu'elles ne changent pas
     * en fonction de l'avancement du joueur. Le joueur doit atteindre ces coordonnées pour ramasser l'item.
     */
    private final int x; // Position absolue en X
    private final int y; // Position absolue en Y
    private final Type type;
    private boolean ramasse = false; // Pour ne pas le ramasser deux fois

    /** Le constructeur crée un item à une position (x, y) donnée et d'un type spécifié. */
    public Item(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /** Getters pour les coordonnées et le type de l'item. */
    public int getX() { return x; }
    public int getY() { return y; }
    public Type getType() { return type; }

    /** Getters et setters pour l'état de ramassage de l'item. */
    public boolean isRamasse() { return ramasse; }
    public void setRamasse(boolean ramasse) { this.ramasse = ramasse; }
}
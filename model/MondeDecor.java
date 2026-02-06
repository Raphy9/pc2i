package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/****
 * La classe MondeDecor gère une collection de décorations (nuages, oiseaux) qui apparaissent à l'écran.
 * Elle est responsable de faire évoluer ces décorations (les déplacer vers la droite) et de les générer
 * aléatoirement à gauche de l'écran. Les décorations sont purement visuelles et n'ont pas d'impact sur le gameplay,
 * mais elles ajoutent de la vie au décor.
 */
public class MondeDecor {

    /** La liste des décorations actuellement à l'écran. */
    private final List<Decoration> elements = new ArrayList<>();
    private final int largeurEcran; // Pour savoir quand supprimer
    private final int hauteurEcran; // Pour savoir où générer
    private static final Random RND = new Random();

    /** Le constructeur crée un MondeDecor avec les dimensions de l'écran pour gérer la génération et la suppression des décorations. */
    public MondeDecor(int largeur, int hauteur) {
        this.largeurEcran = largeur;
        this.hauteurEcran = hauteur;
    }

    /** Appelé par le thread, met à jour positions et cycle de vie */
    public synchronized void evoluer() {
        // 1. Déplacer et nettoyer
        Iterator<Decoration> it = elements.iterator();
        while (it.hasNext()) {
            Decoration d = it.next();
            d.avancer();

            // Si sort à droite de l'écran -> suppression
            if (d.getX() > largeurEcran + 100) {
                it.remove();
            }
        }

        // 2. Génération aléatoire (faible probabilité pour ne pas surcharger)
        // Environ 1 chance sur 100 par cycle (donc un objet toutes les 2-3 secondes)
        if (elements.size() < 10 && RND.nextInt(100) == 0) {
            elements.add(new Decoration(hauteurEcran));
        }
    }

    /** Retourne une copie safe pour l'affichage */
    public synchronized List<Decoration> getElements() {
        return new ArrayList<>(elements);
    }
}
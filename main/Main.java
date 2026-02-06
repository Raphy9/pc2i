package main;

import control.ReactionClic;
import model.Avancer;
import model.Descendre;
import model.Parcours;
import model.Position;
import view.Raffraichir;
import view.Affichage;

import javax.swing.*;

/**
 * Classe principale pour lancer le jeu
 */
public class Main {
    public static void main(String [] args) {
        JFrame maFenetre = new JFrame("PC2i - CIRCLE");
        Position p = new Position();
        Parcours parcours = new Parcours(p);

        // Création du Monde Décor
        model.MondeDecor decor = new model.MondeDecor(view.Affichage.VIEW_ZONE_X, view.Affichage.VIEW_ZONE_Y);

        // On passe le decor à l'affichage
        Affichage a = new Affichage(p, parcours, decor);
        maFenetre.add(a);

        // Lancement des Threads
        new Raffraichir(a);
        new Descendre(p, parcours);
        new Avancer(p, 1, parcours);
        new view.AnimChoc(p, a);

        // Lancement du thread décor
        new model.AnimateurDecor(decor);

        new ReactionClic(a, p, parcours);

        // Configuration de la fenêtre
        maFenetre.pack();
        maFenetre.setResizable(false);
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        maFenetre.setLocationRelativeTo(null);
        maFenetre.setVisible(true);
    }
}
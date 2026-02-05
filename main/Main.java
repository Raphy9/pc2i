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
        // Création de la fenêtre et des composants
        JFrame maFenetre = new JFrame("PC2i - CIRCLE");
        Position p = new Position();
        // Le parcours doit être créé avant les threads qui en ont besoin
        Parcours parcours = new Parcours(p);
        // L'affichage doit être créé avant les threads qui en ont besoin
        Affichage a = new Affichage(p, parcours);
        maFenetre.add(a);
        // Création des threads
        new Raffraichir(a);
        new Descendre(p, parcours);
        new Avancer(p, 1, parcours);
        // Création du thread de réaction au clic et clavier (doit être créé après l'affichage et le parcours)
        new ReactionClic(a, p, parcours);
        // Configuration de la fenêtre
        maFenetre.pack();
        maFenetre.setResizable(false);
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        maFenetre.setLocationRelativeTo(null);
        maFenetre.setVisible(true);
    }
}
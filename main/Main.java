package main;

import control.ReactionClic;
import model.Avancer;
import model.Descendre;
import model.Parcours;
import model.Position;
import view.Raffraichir;
import view.Affichage;

import javax.swing.*;

/** La classe principale de ce projet */
public class Main {
    /** La méthode de lancement du programme */
    public static void main(String [] args) {
        JFrame maFenetre = new JFrame("Exercice 1");
        Position p = new Position();
        // modèle : parcours
        Parcours parcours = new Parcours(p);
        // vue : affichage lié au modèle
        Affichage a = new Affichage(p, parcours);
        maFenetre.add(a);
        // threads : rafraîchissement et dynamique du modèle
        new Raffraichir(a);
        new Descendre(p, parcours); // Passer le parcours au constructeur
        new Avancer(p, 1, parcours);
        // fait défiler la ligne et notifie le parcours
        // contrôleur : réaction au clic
        new ReactionClic(a,p);
        maFenetre.pack();
        maFenetre.setResizable(false);
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        maFenetre.setLocationRelativeTo(null);
        maFenetre.setVisible(true);
    }
}
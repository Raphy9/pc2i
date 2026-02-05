package control;

import model.Parcours;
import model.Position;
import view.Affichage;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Ce thread gère les interactions de l'utilisateur : clics de souris et appuis sur la barre d'espace.
 * Il réagit en fonction de l'état du jeu (menu, en jeu, game over) pour lancer le jeu, faire sauter le personnage,
 * ou proposer des options après un game over.
 */
public class ReactionClic implements MouseListener, KeyListener {
    // Références nécessaires pour gérer les interactions et mettre à jour l'état du jeu
    private final Affichage monAffichage;
    // La position est nécessaire pour faire sauter le personnage, vérifier l'état du jeu, et réinitialiser le jeu
    private final Position maPosition;
    // On a besoin du parcours pour pouvoir le reset aussi
    private final Parcours monParcours;

    /** Crée un thread de réaction au clic et clavier associé à l'affichage, à la position et au parcours donnés. */
    public ReactionClic(Affichage a, Position p, Parcours parcours) {
        // On stocke les références nécessaires pour gérer les interactions
        monAffichage = a;
        maPosition = p;
        monParcours = parcours;

        // On ajoute les listeners à l'affichage pour capturer les événements de souris et de clavier
        a.addMouseListener(this);
        a.addKeyListener(this);
        a.setFocusable(true);
        a.requestFocusInWindow();
    }

    /** Gère les actions de l'utilisateur en fonction de l'état du jeu. */
    private void gererAction(int x, int y, boolean isMouse) {

        // MENU PRINCIPAL
        if (maPosition.getEtat() == Position.Etat.MENU) {
            // Si c'est un clic de souris, on vérifie si le clic est sur le bouton "Jouer"
            if (isMouse && Affichage.BTN_JOUER.contains(x, y)) {
                // Lancer le jeu
                demarrerJeu();
            }
            // Si c'est clavier (Espace), on lance aussi
            else if (!isMouse) {
                demarrerJeu();
            }
        }

        // EN JEU (VIVANT)
        else if (maPosition.getEtat() == Position.Etat.JEU && !maPosition.isGameOver()) {
            // Sauter
            maPosition.jump();
            monAffichage.repaint();
        }

        // GAME OVER
        else if (maPosition.isGameOver()) {
            if (isMouse) {
                // Si c'est un clic de souris, on vérifie si le clic est sur les boutons "Rejouer" ou "Menu"
                if (Affichage.BTN_REJOUER.contains(x, y)) {
                    demarrerJeu();
                }
                // Le bouton "Menu" est aussi cliquable pour retourner au menu principal
                else if (Affichage.BTN_MENU.contains(x, y)) {
                    retourMenu();
                }
            }
        }
    }

    /** Démarre une nouvelle partie en réinitialisant la position et le parcours.
     */
    private void demarrerJeu() {
        maPosition.reset();
        monParcours.reset();
        // On force le repaint immédiat pour éviter un flash noir
        monAffichage.repaint();
    }

    /** Retourne au menu principal en changeant l'état de la position et en rafraîchissant l'affichage. */
    private void retourMenu() {
        maPosition.setEtat(Position.Etat.MENU);
        monAffichage.repaint();
    }

    /** --- SOURIS --- */
    @Override
    public void mouseClicked(MouseEvent e) {
        gererAction(e.getX(), e.getY(), true);
    }

    /** Les autres méthodes de MouseListener sont vides car on ne les utilise pas, mais elles doivent être présentes pour implémenter l'interface. */
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    /** --- CLAVIER --- */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // Pour le clavier, les coordonnées x,y n'importent pas (sauf pour les boutons cliquables)
            gererAction(0, 0, false);
        }
    }

    /** Les autres méthodes de KeyListener sont vides car on ne les utilise pas, mais elles doivent être présentes pour implémenter l'interface. */
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
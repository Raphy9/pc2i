package control;

import model.Parcours;
import model.Position;
import view.Affichage;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ReactionClic implements MouseListener, KeyListener {
    private final Affichage monAffichage;
    private final Position maPosition;
    // On a besoin du parcours pour pouvoir le reset aussi
    private final Parcours monParcours;

    public ReactionClic(Affichage a, Position p, Parcours parcours) {
        monAffichage = a;
        maPosition = p;
        monParcours = parcours;

        a.addMouseListener(this);
        a.addKeyListener(this);
        a.setFocusable(true);
        a.requestFocusInWindow();
    }

    // --- LOGIQUE CENTRALE DE CLIC/ACTION ---
    private void gererAction(int x, int y, boolean isMouse) {

        // 1. CAS : MENU PRINCIPAL
        if (maPosition.getEtat() == Position.Etat.MENU) {
            if (isMouse && Affichage.BTN_JOUER.contains(x, y)) {
                // Lancer le jeu
                demarrerJeu();
            }
            // Si c'est clavier (Espace), on lance aussi
            else if (!isMouse) {
                demarrerJeu();
            }
        }

        // 2. CAS : EN JEU (VIVANT)
        else if (maPosition.getEtat() == Position.Etat.JEU && !maPosition.isGameOver()) {
            // Sauter
            maPosition.jump();
            monAffichage.repaint();
        }

        // 3. CAS : GAME OVER
        else if (maPosition.isGameOver()) {
            if (isMouse) {
                if (Affichage.BTN_REJOUER.contains(x, y)) {
                    demarrerJeu();
                } else if (Affichage.BTN_MENU.contains(x, y)) {
                    retourMenu();
                }
            }
        }
    }

    private void demarrerJeu() {
        maPosition.reset();
        monParcours.reset();
        // On force le repaint immédiat pour éviter un flash noir
        monAffichage.repaint();
    }

    private void retourMenu() {
        maPosition.setEtat(Position.Etat.MENU);
        monAffichage.repaint();
    }

    // --- SOURIS ---
    @Override
    public void mouseClicked(MouseEvent e) {
        gererAction(e.getX(), e.getY(), true);
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- CLAVIER ---
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // Pour le clavier, les coordonnées x,y n'importent pas (sauf pour les boutons cliquables)
            gererAction(0, 0, false);
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
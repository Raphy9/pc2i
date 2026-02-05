package control;

import model.Position;
import view.Affichage;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/** Contrôleur qui réagit au clic et déclenche un saut dans le modèle. */
public class ReactionClic implements MouseListener {
    private final Affichage monAffichage;
    private final Position maPosition;

    /** Crée un contrôleur lié à l'affichage et à la position donnée. */
    public ReactionClic(Affichage a, Position p) {
        monAffichage = a;
        maPosition = p;
        a.addMouseListener(this);
    }


    @Override
    /** Au clic, déclenche un saut dans la position et rafraîchit l'affichage. */
    public void mouseClicked(MouseEvent e) {
        maPosition.jump();
        monAffichage.revalidate();
        monAffichage.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

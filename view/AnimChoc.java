package view;

import model.Position;

/**
 * Thread dédié à l'animation visuelle du choc (clignotement rouge et point d'exclamation).
 * Il alterne un booléen dans Affichage tant que le joueur est invulnérable.
 */
public class AnimChoc extends Thread {

    private final Position position;
    private final Affichage affichage;
    private static final int VITESSE_CLIGNOTEMENT = 100; // Changement d'état toutes les 100ms (rapide)

    public AnimChoc(Position position, Affichage affichage) {
        this.position = position;
        this.affichage = affichage;
        setDaemon(true); // S'arrête quand l'appli quitte
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            // Si le joueur est dans la période d'invulnérabilité (vient de toucher)
            if (position.estInvulnerable()) {
                // On inverse l'état visible du choc (Vrai -> Faux -> Vrai...)
                affichage.toggleChocVisible();

                // On force un repaint pour voir le changement immédiatement
                affichage.repaint();

                try {
                    sleep(VITESSE_CLIGNOTEMENT);
                } catch (InterruptedException e) {
                    interrupt();
                }
            } else {
                // Si pas invulnérable, on s'assure que l'affichage est "éteint"
                if (affichage.isChocVisible()) {
                    affichage.setChocVisible(false);
                    affichage.repaint();
                }

                // On dort un peu pour ne pas surcharger le CPU inutilement quand tout va bien
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        }
    }
}
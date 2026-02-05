package view;

/** Thread qui gère le rafraîchissement de l'affichage. */
public class Raffraichir extends Thread {

    /** Délai entre deux rafraîchissements (en ms). */
    public static final int DELAY = 50;
    /** Affichage associé à ce thread. */
    private final Affichage monAffichage;

    /** Crée un thread Raffraichir associé à l'affichage donné. */
    public Raffraichir(Affichage a) {
        monAffichage = a;
        setDaemon(true);
        start();
    }

    @Override
    /** Boucle principale : rafraîchit l'affichage. */
    public void run() {
        while (!isInterrupted()) {
            monAffichage.repaint();
            try {
                sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}

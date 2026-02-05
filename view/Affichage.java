package view;

import model.Position;
import model.Parcours;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Affichage est la classe responsable de dessiner le jeu à l'écran. Elle hérite de JPanel et utilise les données de Position et Parcours
 * pour représenter visuellement le personnage, le parcours, et les différents écrans (Menu, Jeu, Game Over). Elle définit également des zones
 * pour les boutons interactifs, qui sont utilisées par le contrôleur pour détecter les clics. L'affichage est rafraîchi régulièrement par un thread dédié,
 * ce qui permet d'animer le jeu en temps réel.
 */
public class Affichage extends JPanel {
    /** --- CONSTANTES DE L'AFFICHAGE ---
     * LARGEUR : largeur de l'ovale représentant le personnage, utilisée pour les calculs de positionnement.
     * RATIO_X et RATIO_Y : ratios de conversion entre les unités du modèle (
     * position et parcours) et les pixels de l'affichage, pour adapter la taille du jeu à la fenêtre.
     * POSITION_X : position horizontale fixe du personnage à l'écran, calculée à partir de la position de départ et du ratio.
     * VIEW_ZONE_X et VIEW_ZONE_Y : dimensions de la zone de jeu visible à l
     */
    public static final int LARGEUR = 50;
    public static final int RATIO_X = 5;
    public static final int RATIO_Y = 2;

    public static final int POSITION_X = Position.BEFORE * RATIO_X - LARGEUR / 2;
    public static final int VIEW_ZONE_X = (Position.BEFORE + Position.AFTER) * RATIO_X;
    public static final int VIEW_ZONE_Y = (Position.HAUTEUR_MAX - Position.HAUTEUR_MIN) * RATIO_Y;

    /** ZONES DE BOUTONS (en pixels, utilisées pour détecter les clics) ---
     * Ces rectangles définissent les zones interactives pour les boutons du menu et du game over. Ils sont positionnés de manière à être centrés à l'écran,
     * et ont des dimensions suffisantes pour être facilement cliquables. Le contrôleur utilise ces rectangles pour vérifier si un clic de souris ou une pression sur la touche Espace correspond à une action de jeu (démarrer une partie, rejouer, retourner au menu).
     */
    // Bouton Jouer (Menu)
    public static final Rectangle BTN_JOUER = new Rectangle(VIEW_ZONE_X/2 - 60, VIEW_ZONE_Y/2, 120, 40);

    // Boutons Game Over
    public static final Rectangle BTN_REJOUER = new Rectangle(VIEW_ZONE_X/2 - 130, VIEW_ZONE_Y/2 + 20, 120, 40);
    public static final Rectangle BTN_MENU = new Rectangle(VIEW_ZONE_X/2 + 10, VIEW_ZONE_Y/2 + 20, 120, 40);

    // Références au modèle
    private final Position maPosition;
    private final Parcours monParcours;

    /** Constructeur de l'affichage, qui prend en paramètre la position et le parcours du jeu. Il initialise les références au modèle et configure la taille préférée du panneau. */
    public Affichage(Position p, Parcours parcours) {
        setPreferredSize(new Dimension(VIEW_ZONE_X, VIEW_ZONE_Y));
        maPosition = p;
        monParcours = parcours;
    }

    /** Constructeur de commodité pour créer un affichage sans parcours (par exemple, pour le menu). */
    public Affichage(Position p) {
        this(p, null);
    }

    @Override
    /** La méthode paintComponent est appelée à chaque fois que le panneau doit être redessiné. Elle vérifie l'état du jeu (menu ou jeu en cours) et appelle les méthodes de dessin appropriées pour afficher le menu, le jeu, et éventuellement l'écran de game over. Elle utilise des techniques d'amélioration visuelle (antialiasing) pour rendre les éléments graphiques plus lisses. */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Amélioration visuelle (antialiasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Affichage en fonction de l'état du jeu
        if (maPosition.getEtat() == Position.Etat.MENU) {
            drawMenu(g2);
        } else {
            // État JEU (En cours ou Game Over)
            drawJeu(g2);
            if (maPosition.isGameOver()) {
                drawGameOver(g2);
            }
        }

        g2.dispose();
    }

    /** la méthode drawMenu dessine l'écran de menu, avec un fond blanc, un titre centré, un bouton "Jouer" vert, et une instruction pour le joueur. Elle utilise des polices différentes pour le titre et les instructions,
     * et centre les éléments à l'écran pour une présentation claire et attrayante. */
    private void drawMenu(Graphics2D g2) {
        // Fond
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Titre
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        String titre = "CIRCLE";
        int strWidth = g2.getFontMetrics().stringWidth(titre);
        g2.drawString(titre, (getWidth() - strWidth) / 2, getHeight() / 3);

        // Bouton Jouer
        g2.setColor(Color.GREEN);
        g2.fill(BTN_JOUER);
        g2.setColor(Color.BLACK);
        g2.draw(BTN_JOUER);

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String btnText = "JOUER";
        int btnStrW = g2.getFontMetrics().stringWidth(btnText);
        // Centrage vertical approximatif
        g2.drawString(btnText, BTN_JOUER.x + (BTN_JOUER.width - btnStrW)/2, BTN_JOUER.y + 27);

        // Instruction
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("Clic ou Espace pour commencer", (getWidth()-150)/2, getHeight()-30);
    }

    /** la méthode drawJeu dessine le jeu en cours, avec la ligne brisée du parcours, l'ovale représentant le personnage, et un HUD affichant le score et les vies restantes. Elle utilise des couleurs et des épaisseurs de trait différentes pour distinguer les éléments du jeu,
     * et calcule les positions à partir des données du modèle pour assurer une représentation fidèle de l'état du jeu. */
    private void drawJeu(Graphics2D g2) {
        java.awt.Stroke oldStroke = g2.getStroke();
        java.awt.Color oldColor = g2.getColor();

        // Dessin de la Ligne brisée
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Color.BLACK);
        drawParcours(g2);

        // Calcul de la position Y de l'ovale (nécessaire pour placer le "!")
        int posY = (Position.HAUTEUR_MAX - maPosition.getPosition() - Position.HAUTEUR_OVALE) * RATIO_Y;
        int ovalHeight = Position.HAUTEUR_OVALE * RATIO_Y;

        // Dessin de l'Ovale
        g2.setStroke(new BasicStroke(5f));

        // Si on est touché, l'ovale devient ROUGE au lieu d'ORANGE
        if (maPosition.estInvulnerable()) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.ORANGE);
        }
        g2.drawOval(POSITION_X, posY, LARGEUR, ovalHeight);

        // Dessin du "!" Clignotant
        if (maPosition.estInvulnerable()) {
            // Astuce pour faire clignoter : on change la visibilité toutes les 200ms
            long temps = System.currentTimeMillis();
            // Si le reste de la division par 400 est < 200, on dessine (ON/OFF)
            if (temps % 400 < 200) {
                g2.setColor(Color.RED);
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                // On le place un peu au-dessus de l'ovale (posY - 10)
                // On centre le "!" par rapport à l'ovale (+ 18 pour centrer horizontalement)
                g2.drawString("!", POSITION_X + 18, posY - 10);
            }
        }

        // 4. HUD (Score et Vies)
        g2.setColor(Color.BLUE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Score : " + maPosition.getAvancement(), 10, 20);
        g2.setColor(Color.RED);
        g2.drawString("Vies : " + maPosition.getVies(), 10, 40);

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    /** la méthode drawGameOver dessine l'écran de game over, avec un fond semi-transparent, un message "GAME OVER" centré en rouge, et deux boutons "Rejouer" et "Menu" pour permettre au joueur de choisir son action. Elle utilise des couleurs vives pour les boutons et le message,
     * et centre les éléments à l'écran pour une présentation claire et impactante. */
    private void drawGameOver(Graphics2D g2) {
        // Fond semi-transparent
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String msg = "GAME OVER";
        int w = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, (getWidth() - w)/2, getHeight()/3);

        // Bouton Rejouer
        g2.setColor(Color.CYAN);
        g2.fill(BTN_REJOUER);
        g2.setColor(Color.BLACK);
        g2.draw(BTN_REJOUER);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("REJOUER", BTN_REJOUER.x + 20, BTN_REJOUER.y + 25);

        // Bouton Menu
        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(BTN_MENU);
        g2.setColor(Color.BLACK);
        g2.draw(BTN_MENU);
        g2.drawString("MENU", BTN_MENU.x + 35, BTN_MENU.y + 25);
    }

    /** la méthode drawParcours dessine la ligne brisée du parcours en connectant les points du parcours avec des segments de ligne. Elle convertit les coordonnées du modèle en coordonnées de vue à l'aide de la méthode modelToView,
     * et utilise une boucle pour dessiner chaque segment entre les points successifs. */
    private void drawParcours(Graphics g) {
        if (monParcours == null) return;
        List<Point> pts = monParcours.getPoints();
        if (pts.size() < 2) return;
        Point prev = modelToView(pts.get(0));
        for (int i = 1; i < pts.size(); i++) {
            Point cur = modelToView(pts.get(i));
            g.drawLine(prev.x, prev.y, cur.x, cur.y);
            prev = cur;
        }
    }

    /** la méthode modelToView convertit les coordonnées d'un point du modèle (position et parcours) en coordonnées de vue (pixels à l'écran). Elle applique les ratios de conversion définis par RATIO_X et RATIO_Y,
     * et ajuste la coordonnée y pour correspondre à l'orientation de l'affichage (y croissant vers le bas). */
    private Point modelToView(Point pm) {
        int xV = pm.x * RATIO_X;
        int yV = (Position.HAUTEUR_MAX - pm.y) * RATIO_Y;
        return new Point(xV, yV);
    }
}
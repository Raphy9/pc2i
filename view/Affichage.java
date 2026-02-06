package view;

import model.Position;
import model.Parcours;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/** Affichage est la classe responsable de tout le rendu graphique du jeu. Elle hérite de JPanel et redéfinit la méthode paintComponent
 * pour dessiner le fond, le parcours, les items, le personnage, et les différents écrans (menu, jeu, game over) en fonction de l'état du jeu.
 * Elle gère également le chargement des ressources graphiques (images) et fournit des méthodes pour afficher ou cacher les effets de choc.
 */
public class Affichage extends JPanel {

    /** --- CONSTANTES DE L'AFFICHAGE --- */
    public static final int LARGEUR = 50; // Largeur visuelle du joueur
    public static final int RATIO_X = 5;
    public static final int RATIO_Y = 2;

    public static final int POSITION_X = Position.BEFORE * RATIO_X - LARGEUR / 2;
    public static final int VIEW_ZONE_X = (Position.BEFORE + Position.AFTER) * RATIO_X;
    public static final int VIEW_ZONE_Y = (Position.HAUTEUR_MAX - Position.HAUTEUR_MIN) * RATIO_Y;

    /** Rectangles pour les boutons du menu et de l'écran de game over, utilisés pour la détection de clics. */
    public static final Rectangle BTN_JOUER = new Rectangle(VIEW_ZONE_X/2 - 60, VIEW_ZONE_Y/2, 120, 40);
    public static final Rectangle BTN_REJOUER = new Rectangle(VIEW_ZONE_X/2 - 130, VIEW_ZONE_Y/2 + 20, 120, 40);
    public static final Rectangle BTN_MENU = new Rectangle(VIEW_ZONE_X/2 + 10, VIEW_ZONE_Y/2 + 20, 120, 40);

    /** --- RÉFÉRENCES AUX DONNÉES DU JEU --- */
    private final Position maPosition;
    private final Parcours monParcours;
    private final model.MondeDecor mondeDecor;

    /** --- ÉTAT DE L'AFFICHAGE --- */
    private boolean chocVisible = false;

    /** --- RESSOURCES GRAPHIQUES --- */
    private BufferedImage imgFond;
    private BufferedImage imgPerso;
    private BufferedImage imgPersoTouche; // Version rouge du perso
    private BufferedImage imgNuage;
    private BufferedImage imgOiseau;
    private BufferedImage imgPiece;
    private BufferedImage imgObstacle;

    /** Le constructeur d'Affichage prend en paramètre la position, le parcours et le décor pour pouvoir les dessiner.
     * Il charge également les ressources graphiques nécessaires pour le rendu du jeu.
     */
    public Affichage(Position p, Parcours parcours, model.MondeDecor decor) {
        setPreferredSize(new Dimension(VIEW_ZONE_X, VIEW_ZONE_Y));
        maPosition = p;
        monParcours = parcours;
        mondeDecor = decor;

        // Chargement des ressources graphiques
        chargerImages();
    }

    /** Constructeur simplifié pour les tests, avec juste la position. */
    public Affichage(Position p) {
        this(p, null, null);
    }

    /** --- MÉTHODES DE CHARGEMENT DES IMAGES --- */
    private void chargerImages() {
        try {
            // Assurez-vous que vos images sont dans src/images/
            imgFond = chargerImage("/images/ciel.png");
            imgPerso = chargerImage("/images/perso.png");
            imgPersoTouche = chargerImage("/images/perso_touche.png");
            imgNuage = chargerImage("/images/nuage.png");
            imgOiseau = chargerImage("/images/oiseau.png");
            imgPiece = chargerImage("/images/piece.png");
            imgObstacle = chargerImage("/images/obstacle.png");
        } catch (Exception e) {
            System.err.println("ERREUR : Impossible de charger une ou plusieurs images.");
            System.err.println("Vérifiez que le dossier 'src/images' existe et contient les fichiers .png");
            e.printStackTrace();
        }
    }

    /** Méthode utilitaire pour charger une image et gérer les erreurs de manière centralisée. */
    private BufferedImage chargerImage(String chemin) {
        try {
            return ImageIO.read(getClass().getResource(chemin));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Image introuvable : " + chemin);
            return null; // Retourne null si pas trouvé, on gérera ça au dessin
        }
    }

    /** --- GETTERS/SETTERS --- */
    public void setChocVisible(boolean visible) { this.chocVisible = visible; }
    public boolean isChocVisible() { return chocVisible; }
    public void toggleChocVisible() { this.chocVisible = !this.chocVisible; }

    @Override
    /** La méthode paintComponent est redéfinie pour dessiner tous les éléments du jeu en fonction de l'état actuel de la position.
     * Elle dessine d'abord le fond, puis les éléments décoratifs, ensuite le parcours et les items, et enfin le personnage et le HUD.
     * Si le jeu est en menu ou en game over, elle dessine les écrans correspondants.
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Antialiasing (Lissage)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. FOND
        if (imgFond != null) {
            // On étire l'image de fond pour couvrir toute la fenêtre
            g2.drawImage(imgFond, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback si pas d'image : Bleu ciel
            g2.setColor(new Color(200, 230, 255));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. DÉCOR
        drawDecor(g2);

        // 3. JEU OU MENU
        if (maPosition.getEtat() == Position.Etat.MENU) {
            drawMenu(g2);
        } else {
            drawJeu(g2);
            if (maPosition.isGameOver()) {
                drawGameOver(g2);
            }
        }

        // On libère les ressources graphiques utilisées pour ce dessin
        g2.dispose();
    }

    /** La méthode drawDecor dessine les éléments décoratifs du monde, comme les nuages et les oiseaux. Elle utilise les images correspondantes si elles sont disponibles,
     * sinon elle dessine des formes géométriques de base en guise de fallback. Les décorations sont dessinées avant le parcours et le personnage pour qu'elles apparaissent
     * en arrière-plan.
     */
    private void drawDecor(Graphics2D g2) {
        // Si le décor est null, on ne dessine rien (pas d'erreur, juste pas de décor)
        if (mondeDecor == null) return;

        // On parcourt les décorations et on dessine chacune d'elles
        for (model.Decoration d : mondeDecor.getElements()) {
            BufferedImage imgToDraw = null;
            // Choix de l'image en fonction du type de décoration
            if (d.getType() == model.Decoration.Type.NUAGE) imgToDraw = imgNuage;
            else if (d.getType() == model.Decoration.Type.OISEAU) imgToDraw = imgOiseau;

            // Si l'image est disponible, on la dessine à la position de la décoration
            if (imgToDraw != null) {
                // On dessine l'image à la place des formes géométriques
                g2.drawImage(imgToDraw, d.getX(), d.getY(), d.getTaille(), d.getTaille(), null);
            } else {
                // Fallback vectoriel si image manquante
                if (d.getType() == model.Decoration.Type.NUAGE) {
                    g2.setColor(new Color(255, 255, 255, 150));
                    g2.fillOval(d.getX(), d.getY(), d.getTaille(), d.getTaille()/2);
                }
            }
        }
    }

    /** La méthode drawJeu est responsable de dessiner tous les éléments du jeu pendant la partie, y compris le parcours, les items, le personnage et le HUD.
     * Elle utilise les données de la position et du parcours pour déterminer où dessiner chaque élément. Les items sont dessinés en fonction de leur type (bonus ou malus),
     * et le personnage change d'apparence s'il est touché. Le HUD affiche le score et les vies restantes.
     */
    private void drawJeu(Graphics2D g2) {
        // 1. DESSIN DE LA LIGNE
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(50, 50, 50)); // Gris foncé presque noir
        drawParcours(g2);

        // 2. DESSIN DES ITEMS
        for (model.Item item : monParcours.getItems()) {
            // On ne dessine que les items qui ne sont pas encore ramassés
            Point p = modelToView(new Point(item.getX(), item.getY()));
            int size = 40; // Taille visuelle des items

            // Choix de l'image en fonction du type d'item
            if (item.getType() == model.Item.Type.PIECE) {
                // Si l'image de la pièce est disponible, on la dessine, sinon on dessine un cercle jaune
                if (imgPiece != null) {
                    g2.drawImage(imgPiece, p.x - size/2, p.y - size/2, size, size, null);
                } else { // Fallback vectoriel : Cercle jaune
                    g2.setColor(Color.YELLOW);
                    g2.fillOval(p.x - size/2, p.y - size/2, size, size);
                }
            } else { // OBSTACLE
                if (imgObstacle != null) {
                    // Si l'image de l'obstacle est disponible, on la dessine, sinon on dessine un carré gris foncé
                    g2.drawImage(imgObstacle, p.x - size/2, p.y - size/2, size, size, null);
                } else { // Fallback vectoriel : Carré gris foncé
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(p.x - size/2, p.y - size/2, size, size);
                }
            }
        }

        // 3. DESSIN DU PERSONNAGE
        int posY = (Position.HAUTEUR_MAX - maPosition.getPosition() - Position.HAUTEUR_OVALE) * RATIO_Y;
        int ovalHeight = Position.HAUTEUR_OVALE * RATIO_Y;

        // Choix de l'image (Normal ou Touché)
        BufferedImage persoCourant = (chocVisible && imgPersoTouche != null) ? imgPersoTouche : imgPerso;

        if (persoCourant != null) {
            // Dessin de l'image du perso
            g2.drawImage(persoCourant, POSITION_X, posY, LARGEUR, ovalHeight, null);
        } else {
            // Fallback vectoriel
            g2.setColor(chocVisible ? Color.RED : Color.ORANGE);
            g2.fillOval(POSITION_X, posY, LARGEUR, ovalHeight);
        }

        // Point d'exclamation si touché (on le garde en vectoriel car c'est une info HUD)
        if (chocVisible) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            g2.drawString("!", POSITION_X + 18, posY - 10);
        }

        // 4. HUD (Interface Score/Vies)
        drawHUD(g2);
    }

    /** La méthode drawHUD dessine une interface semi-transparente en haut à gauche de l'écran pour afficher le score total et le nombre de vies restantes du joueur.
     * Elle utilise des couleurs contrastées pour que les informations soient lisibles sur le fond, et elle affiche un fond arrondi pour améliorer l'esthétique du HUD.
     */
    private void drawHUD(Graphics2D g2) {
        // Fond semi-transparent pour le score pour la lisibilité
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillRoundRect(5, 5, 150, 50, 15, 15);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Score
        g2.setColor(new Color(0, 100, 200)); // Bleu
        g2.drawString("Score : " + maPosition.getScoreTotal(), 15, 25);

        // Vies
        g2.setColor(new Color(200, 0, 0)); // Rouge
        g2.drawString("Vies : " + maPosition.getVies(), 15, 48);
    }

    /** La méthode drawMenu dessine l'écran de menu principal du jeu, avec un titre stylisé et un bouton "Jouer". Le titre utilise une police
     * large et un effet d'ombre pour le faire ressortir, et le bouton "Jouer" est dessiné avec une couleur vive pour attirer l'attention.
     * Un message d'instruction est également affiché en bas de l'écran pour informer le joueur de la touche à utiliser pour sauter.
     * Le menu est dessiné par-dessus le fond, mais avant les éléments de jeu, pour que le décor soit visible en arrière-plan du menu.
     */
    private void drawMenu(Graphics2D g2) {
        // On profite de l'image de fond déjà dessinée dans paintComponent

        // Titre avec effet d'ombre
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        String titre = "CIRCLE";
        int w = g2.getFontMetrics().stringWidth(titre);

        // Ombre
        g2.setColor(new Color(0, 0, 0, 190));
        g2.drawString(titre, (getWidth() - w)/2 + 3, getHeight()/3 + 3);
        // Texte
        g2.setColor(Color.WHITE);
        g2.drawString(titre, (getWidth() - w)/2, getHeight()/3);

        // Bouton Jouer
        g2.setColor(new Color(250, 230, 250));
        g2.fill(BTN_JOUER);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(BTN_JOUER);

        // Texte du bouton centré
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String btnText = "JOUER";
        int btnW = g2.getFontMetrics().stringWidth(btnText);
        g2.drawString(btnText, BTN_JOUER.x + (BTN_JOUER.width - btnW)/2, BTN_JOUER.y + 27);

        // Instruction pour sauter
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Appuyez sur ESPACE pour sauter", (getWidth()-220)/2, getHeight()-50);
    }

    /** La méthode drawGameOver dessine l'écran de fin de partie lorsque le joueur perd toutes ses vies. Elle affiche un message "GAME OVER" avec une grande police,
     * le score final du joueur, et deux boutons "REJOUER" et "MENU" pour permettre au joueur de recommencer ou de retourner au menu principal.
     * Un overlay sombre est dessiné par-dessus le jeu pour faire ressortir le message de fin de partie.
     */
    private void drawGameOver(Graphics2D g2) {
        // Overlay sombre
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Message "GAME OVER"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        String msg = "GAME OVER";
        int w = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, (getWidth() - w)/2, getHeight()/3);

        // Score final
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String scoreMsg = "Score Final : " + maPosition.getScoreTotal();
        int ws = g2.getFontMetrics().stringWidth(scoreMsg);
        g2.drawString(scoreMsg, (getWidth() - ws)/2, getHeight()/3 + 40);

        // Boutons
        drawButton(g2, BTN_REJOUER, "REJOUER", new Color(200, 200, 255, 255));
        drawButton(g2, BTN_MENU, "MENU", Color.LIGHT_GRAY);
    }

    /** La méthode drawButton est une méthode utilitaire pour dessiner un bouton avec un rectangle de fond coloré, une bordure noire, et du texte centré.
     * Elle est utilisée pour dessiner les boutons "REJOUER" et "MENU" sur l'écran de game over, et peut être réutilisée pour d'autres boutons si nécessaire.
     */
    private void drawButton(Graphics2D g2, Rectangle rect, String text, Color color) {
        g2.setColor(color);
        g2.fill(rect);
        g2.setColor(Color.BLACK);
        g2.draw(rect);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, rect.x + (rect.width - w)/2, rect.y + 25);
    }

    /** La méthode drawParcours dessine la ligne du parcours en reliant les points du parcours avec des segments. Elle utilise une couleur sombre et un trait épais pour que la ligne soit bien visible.
     * Si le parcours n'a pas assez de points pour dessiner une ligne, elle ne fait rien.
     */
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

    /** La méthode modelToView convertit les coordonnées du modèle (position absolue sur le parcours) en coordonnées de vue (pixels à l'écran).
     * Elle applique les ratios de conversion pour l'échelle et inverse l'axe Y pour que le haut du parcours soit en haut de l'écran.
     * Cette méthode est utilisée pour dessiner les éléments du jeu à la bonne position à l'écran en fonction de leur position dans le modèle.
     */
    private Point modelToView(Point pm) {
        int xV = pm.x * RATIO_X;
        int yV = (Position.HAUTEUR_MAX - pm.y) * RATIO_Y;
        return new Point(xV, yV);
    }
}
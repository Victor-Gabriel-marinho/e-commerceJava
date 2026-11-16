package gui;

import java.awt.*;
import java.awt.geom.*;

/**
 * Ilustrações genéricas de padaria desenhadas em Java2D (sem arquivos de imagem).
 * O desenho é escolhido pelo nome do produto; nomes desconhecidos usam um pão.
 * Cada desenho usa uma grade 100x100.
 */
public final class Ilustracoes {

    private Ilustracoes() {}

    public enum Tipo { PAO_FORMA, PAO_FRANCES, CROISSANT, BOLO, ROSQUINHA, CAFE, LEITE, SUCO, QUEIJO, PAO_DE_QUEIJO, SALGADO, BISCOITO }

    /** Escolhe o desenho pelo nome do produto (ordem importa: "pão de queijo" antes de "queijo" e "pão"). */
    public static Tipo tipoDe(String nome) {
        String n = Tema.normalizar(nome);
        if (tem(n, "pao de queijo", "chipa")) return Tipo.PAO_DE_QUEIJO;
        if (tem(n, "croissant", "croassant")) return Tipo.CROISSANT;
        if (tem(n, "sonho", "donut", "rosquinha", "rosca")) return Tipo.ROSQUINHA;
        if (tem(n, "frances", "baguete", "bisnaga", "careca", "paozinho")) return Tipo.PAO_FRANCES;
        if (tem(n, "coxinha", "salgado", "pastel", "esfiha", "empada", "enroladinho", "risole", "kibe")) return Tipo.SALGADO;
        if (tem(n, "biscoito", "cookie", "bolacha")) return Tipo.BISCOITO;
        if (tem(n, "bolo", "torta", "cupcake", "brownie")) return Tipo.BOLO;
        if (tem(n, "cafe", "cappuccino", "capuccino", "expresso", "espresso")) return Tipo.CAFE;
        if (tem(n, "leite", "iogurte", "achocolatado")) return Tipo.LEITE;
        if (tem(n, "suco", "refrigerante", "agua", "vitamina", "guarana", "limonada")) return Tipo.SUCO;
        if (tem(n, "queijo", "requeijao")) return Tipo.QUEIJO;
        return Tipo.PAO_FORMA;
    }

    private static boolean tem(String texto, String... chaves) {
        for (String c : chaves) if (texto.contains(c)) return true;
        return false;
    }

    /** Desenha o item centralizado em (cx, cy), ocupando um quadrado de lado "lado". */
    public static void desenhar(Graphics2D g0, Tipo tipo, double cx, double cy, double lado) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.antialias(g);
        g.translate(cx - lado / 2, cy - lado / 2);
        g.scale(lado / 100.0, lado / 100.0);

        // sombra no chão
        g.setColor(new Color(120, 80, 30, 38));
        g.fill(new Ellipse2D.Double(14, 80, 72, 10));

        switch (tipo) {
            case PAO_FRANCES:   paoFrances(g); break;
            case CROISSANT:     croissant(g); break;
            case BOLO:          bolo(g); break;
            case ROSQUINHA:     rosquinha(g); break;
            case CAFE:          cafe(g); break;
            case LEITE:         leite(g); break;
            case SUCO:          suco(g); break;
            case QUEIJO:        queijo(g); break;
            case PAO_DE_QUEIJO: paoDeQueijo(g); break;
            case SALGADO:       salgado(g); break;
            case BISCOITO:      biscoito(g); break;
            default:            paoForma(g); break;
        }
        g.dispose();
    }

    // ------------------------------------------------------------ helpers
    private static final Color CRUST = new Color(0xC98230);
    private static final Color CRUST_CLARO = new Color(0xE8AE55);
    private static final Color MIGALHA = new Color(0xFBE3B0);

    private static Paint vertical(double y0, double y1, Color a, Color b) {
        return new GradientPaint(0, (float) y0, a, 0, (float) y1, b);
    }

    private static void contorno(Graphics2D g, Shape s, Paint fill, Color borda) {
        g.setPaint(fill);
        g.fill(s);
        g.setColor(borda);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    // ------------------------------------------------------------ desenhos
    private static void paoForma(Graphics2D g) {
        // corpo + "cogumelo" no topo
        Area pao = new Area(new RoundRectangle2D.Double(17, 42, 66, 38, 10, 10));
        pao.add(new Area(new RoundRectangle2D.Double(12, 24, 76, 34, 34, 34)));
        contorno(g, pao, vertical(24, 80, CRUST_CLARO, CRUST), new Color(0x9C5E1A));
        // fatia mostrando o miolo
        g.setColor(new Color(0xFFF1CF));
        g.fill(new RoundRectangle2D.Double(70, 44, 10, 32, 4, 4));
        // cortes
        g.setColor(new Color(0xA9651E));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(28, 34, 36, 46));
        g.draw(new Line2D.Double(42, 32, 50, 44));
        g.draw(new Line2D.Double(56, 33, 63, 44));
        // brilho
        g.setColor(new Color(255, 255, 255, 70));
        g.fill(new Ellipse2D.Double(22, 28, 24, 7));
    }

    private static void paoFrances(Graphics2D g) {
        Graphics2D r = (Graphics2D) g.create();
        r.rotate(Math.toRadians(-18), 50, 52);
        Shape corpo = new Ellipse2D.Double(10, 36, 80, 34);
        contorno(r, corpo, vertical(36, 70, CRUST_CLARO, CRUST), new Color(0x9C5E1A));
        // cortes claros característicos
        r.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        r.setColor(MIGALHA);
        for (int i = 0; i < 4; i++) {
            double x = 26 + i * 15;
            r.draw(new Line2D.Double(x, 42, x + 9, 62));
        }
        r.setColor(new Color(255, 255, 255, 60));
        r.fill(new Ellipse2D.Double(20, 40, 30, 6));
        r.dispose();
    }

    private static void croissant(Graphics2D g) {
        // 5 gomos em arco, do mais externo para o central
        int[] ordem = {-2, 2, -1, 1, 0};
        for (int k : ordem) {
            double t = k / 2.0;
            double cx = 50 + t * 27;
            double cy = 58 - (1 - t * t) * 12;
            double w = 36 - Math.abs(t) * 8, h = 46 - Math.abs(t) * 16;
            Graphics2D r = (Graphics2D) g.create();
            r.rotate(Math.toRadians(t * 38), cx, cy);
            Shape gomo = new Ellipse2D.Double(cx - w / 2, cy - h / 2, w, h);
            contorno(r, gomo, vertical(cy - h / 2, cy + h / 2, new Color(0xF0B95C), new Color(0xCF8A32)), new Color(0xA86A1E));
            r.setColor(new Color(0xB4721F));
            r.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            r.draw(new Arc2D.Double(cx - w / 2 + 3, cy - h / 2 + 5, w - 6, h - 10, 200, 130, Arc2D.OPEN));
            r.dispose();
        }
    }

    private static void bolo(Graphics2D g) {
        Color massa = new Color(0x7A4A24), creme = new Color(0xFFF0D2);
        Shape base = new RoundRectangle2D.Double(16, 44, 68, 36, 8, 8);
        g.setPaint(massa);
        g.fill(base);
        g.setColor(creme);
        g.fill(new Rectangle2D.Double(16, 55, 68, 7));
        g.setColor(new Color(0x9A6236));
        g.fill(new Rectangle2D.Double(16, 69, 68, 3));
        g.setColor(new Color(0x5E3517));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(base);
        // cobertura com escorrido
        Area cob = new Area(new RoundRectangle2D.Double(13, 35, 74, 14, 12, 12));
        for (double x : new double[]{24, 44, 66}) cob.add(new Area(new RoundRectangle2D.Double(x - 4, 40, 8, 17 + (x % 5), 8, 8)));
        contorno(g, cob, new Color(0xF4A3B8), new Color(0xD6728F));
        // cereja
        g.setColor(new Color(0x7A1F2B));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(50, 28, 52, 20, 58, 16));
        contorno(g, new Ellipse2D.Double(43, 25, 14, 14), new Color(0xD7263D), new Color(0x9E1A2D));
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Double(46, 28, 4, 4));
    }

    private static void rosquinha(Graphics2D g) {
        Area massa = new Area(new Ellipse2D.Double(12, 22, 76, 60));
        massa.subtract(new Area(new Ellipse2D.Double(39, 43, 22, 18)));
        contorno(g, massa, new Color(0xE2A55A), new Color(0xB37425));
        Area cobertura = new Area(new Ellipse2D.Double(16, 24, 68, 52));
        cobertura.subtract(new Area(new Ellipse2D.Double(37, 42, 26, 20)));
        g.setPaint(new Color(0xF48FB1));
        g.fill(cobertura);
        g.setColor(new Color(0xD66A92));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cobertura);
        // confeitos
        Color[] cores = {Color.WHITE, new Color(0xFFE066), new Color(0x7BD3EA), new Color(0xA7F3B0)};
        double[][] pts = {{28, 38, 20}, {50, 30, -30}, {68, 40, 50}, {72, 56, 10}, {58, 66, -40}, {34, 62, 30}, {24, 52, -10}, {44, 34, 70}};
        for (int i = 0; i < pts.length; i++) {
            Graphics2D r = (Graphics2D) g.create();
            r.rotate(Math.toRadians(pts[i][2]), pts[i][0], pts[i][1]);
            r.setColor(cores[i % cores.length]);
            r.fill(new RoundRectangle2D.Double(pts[i][0] - 3.5, pts[i][1] - 1.2, 7, 2.4, 2.4, 2.4));
            r.dispose();
        }
    }

    private static void cafe(Graphics2D g) {
        // vapor
        g.setColor(new Color(150, 130, 110, 110));
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            double x = 38 + i * 12;
            Path2D.Double v = new Path2D.Double();
            v.moveTo(x, 36);
            v.curveTo(x - 6, 30, x + 6, 24, x, 16);
            g.draw(v);
        }
        // pires
        contorno(g, new Ellipse2D.Double(16, 68, 68, 14), new Color(0xF3EFEA), new Color(0xB9B1A8));
        // alça
        g.setColor(new Color(0xB9B1A8));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(62, 44, 18, 20, -90, 180, Arc2D.OPEN));
        g.setColor(new Color(0xFFFFFF));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(62, 44, 18, 20, -90, 180, Arc2D.OPEN));
        // xícara
        Path2D.Double x = new Path2D.Double();
        x.moveTo(24, 42); x.lineTo(70, 42); x.curveTo(70, 70, 60, 76, 47, 76); x.curveTo(34, 76, 24, 70, 24, 42); x.closePath();
        contorno(g, x, new Color(0xFFFFFF), new Color(0xB9B1A8));
        // café
        contorno(g, new Ellipse2D.Double(24, 38, 46, 9), new Color(0x5B3A1E), new Color(0xB9B1A8));
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new Ellipse2D.Double(30, 40, 14, 3));
    }

    private static void leite(Graphics2D g) {
        Color azul = new Color(0x4A90D9);
        Shape corpo = new Rectangle2D.Double(32, 36, 36, 44);
        contorno(g, corpo, Color.WHITE, new Color(0x9DB4CC));
        Path2D.Double topo = new Path2D.Double();
        topo.moveTo(32, 36); topo.lineTo(38, 22); topo.lineTo(62, 22); topo.lineTo(68, 36); topo.closePath();
        contorno(g, topo, new Color(0xE9F1FA), new Color(0x9DB4CC));
        g.setColor(azul);
        g.fill(new Rectangle2D.Double(33, 50, 34, 14));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(42, 52, 10, 10));
        g.setColor(azul);
        g.fill(new Ellipse2D.Double(45, 56, 6, 5));
        g.setColor(new Color(0x9DB4CC));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Line2D.Double(44, 22, 44, 27));
    }

    private static void suco(Graphics2D g) {
        Path2D.Double copo = new Path2D.Double();
        copo.moveTo(30, 30); copo.lineTo(70, 30); copo.lineTo(64, 80); copo.lineTo(36, 80); copo.closePath();
        // canudo atrás
        g.setColor(new Color(0xE63946));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(60, 14, 54, 58));
        g.setColor(new Color(255, 255, 255, 140));
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 5f}, 0f));
        g.draw(new Line2D.Double(60, 14, 54, 58));
        // líquido
        Graphics2D c = (Graphics2D) g.create();
        c.clip(copo);
        c.setPaint(vertical(40, 80, new Color(0xFFB347), new Color(0xFF8C1A)));
        c.fill(new Rectangle2D.Double(20, 42, 60, 40));
        c.setColor(new Color(255, 255, 255, 90));
        c.fill(new Rectangle2D.Double(33, 42, 5, 40));
        c.dispose();
        g.setColor(new Color(0x9FB8C8));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(copo);
        // rodela de laranja na borda
        contorno(g, new Ellipse2D.Double(60, 20, 22, 22), new Color(0xFF9F1C), new Color(0xD97706));
        g.setColor(new Color(0xFFE3A3));
        g.setStroke(new BasicStroke(1.4f));
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(i * 45);
            g.draw(new Line2D.Double(71 - Math.cos(a) * 8, 31 - Math.sin(a) * 8, 71 + Math.cos(a) * 8, 31 + Math.sin(a) * 8));
        }
    }

    private static void queijo(Graphics2D g) {
        Path2D.Double frente = new Path2D.Double();
        frente.moveTo(12, 72); frente.lineTo(88, 72); frente.lineTo(88, 54); frente.lineTo(12, 44); frente.closePath();
        Path2D.Double topo = new Path2D.Double();
        topo.moveTo(12, 44); topo.lineTo(88, 54); topo.lineTo(76, 38); topo.lineTo(30, 30); topo.closePath();
        contorno(g, topo, new Color(0xFFE27A), new Color(0xC9961C));
        contorno(g, frente, vertical(44, 72, new Color(0xFFD24D), new Color(0xF0B429)), new Color(0xC9961C));
        g.setColor(new Color(0xE0A21F));
        g.fill(new Ellipse2D.Double(24, 56, 12, 10));
        g.fill(new Ellipse2D.Double(52, 60, 9, 8));
        g.fill(new Ellipse2D.Double(70, 56, 8, 8));
        g.setColor(new Color(0xF2BC3A));
        g.fill(new Ellipse2D.Double(40, 38, 6, 4));
    }

    private static void paoDeQueijo(Graphics2D g) {
        double[][] bolas = {{36, 58, 17}, {64, 58, 17}, {50, 38, 17}};
        for (double[] b : bolas) {
            Shape s = new Ellipse2D.Double(b[0] - b[2], b[1] - b[2], b[2] * 2, b[2] * 2);
            contorno(g, new Ellipse2D.Double(b[0] - b[2], b[1] - b[2], b[2] * 2, b[2] * 2),
                    new RadialGradientPaint(new Point2D.Double(b[0] - 5, b[1] - 6), (float) (b[2] * 1.4),
                            new float[]{0f, 1f}, new Color[]{new Color(0xFFD98A), new Color(0xE0A13B)}),
                    new Color(0xB8791F));
            g.setColor(new Color(0xCB8B2E));
            g.fill(new Ellipse2D.Double(b[0] + 3, b[1] + 2, 3, 3));
            g.fill(new Ellipse2D.Double(b[0] - 7, b[1] + 5, 2.5, 2.5));
            g.fill(new Ellipse2D.Double(b[0] + 6, b[1] - 6, 2.5, 2.5));
        }
    }

    private static void salgado(Graphics2D g) {
        Path2D.Double s = new Path2D.Double();
        s.moveTo(50, 16);
        s.curveTo(55, 30, 82, 44, 77, 66);
        s.curveTo(74, 84, 26, 84, 23, 66);
        s.curveTo(18, 44, 45, 30, 50, 16);
        s.closePath();
        contorno(g, s, new RadialGradientPaint(new Point2D.Double(42, 50), 50f,
                new float[]{0f, 1f}, new Color[]{new Color(0xF0B358), new Color(0xC9812A)}), new Color(0x9C5E1A));
        g.setColor(new Color(255, 255, 255, 80));
        g.fill(new Ellipse2D.Double(34, 44, 12, 18));
        g.setColor(new Color(0xB8741F));
        g.fill(new Ellipse2D.Double(58, 62, 3, 3));
        g.fill(new Ellipse2D.Double(46, 70, 3, 3));
        g.fill(new Ellipse2D.Double(62, 50, 2.5, 2.5));
    }

    private static void biscoito(Graphics2D g) {
        contorno(g, new Ellipse2D.Double(18, 24, 64, 56), new Color(0xE0A863), new Color(0xA8691F));
        g.setColor(new Color(0x5A3416));
        double[][] gotas = {{34, 40, 7}, {56, 36, 8}, {66, 54, 7}, {42, 58, 8}, {52, 68, 6}, {30, 62, 5}};
        for (double[] p : gotas) g.fill(new RoundRectangle2D.Double(p[0], p[1], p[2], p[2] * 0.8, 3, 3));
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new Ellipse2D.Double(26, 30, 22, 8));
    }
}

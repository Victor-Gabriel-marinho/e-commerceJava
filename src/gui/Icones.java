package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Ícones vetoriais desenhados em Java2D (sem arquivos de imagem). Grade de 24x24. */
public final class Icones {

    private Icones() {}

    private abstract static class Base implements Icon {
        final int tamanho;
        final Color cor;
        Base(int tamanho, Color cor) { this.tamanho = tamanho; this.cor = cor; }
        @Override public int getIconWidth() { return tamanho; }
        @Override public int getIconHeight() { return tamanho; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            g2.translate(x, y);
            float s = tamanho / 24f;
            g2.scale(s, s);
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            desenhar(g2);
            g2.dispose();
        }
        abstract void desenhar(Graphics2D g2);
    }

    public static Icon lupa(int tamanho, Color cor) {
        return new Base(tamanho, cor) {
            @Override void desenhar(Graphics2D g) {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 12, 12));
                g.drawLine(14, 14, 20, 20);
            }
        };
    }

    public static Icon carrinho(int tamanho, Color cor) {
        return new Base(tamanho, cor) {
            @Override void desenhar(Graphics2D g) {
                Path2D.Double alca = new Path2D.Double();
                alca.moveTo(1.5, 3.5); alca.lineTo(4.5, 3.5); alca.lineTo(7.4, 15.5); alca.lineTo(19.5, 15.5);
                g.draw(alca);
                Path2D.Double cesto = new Path2D.Double();
                cesto.moveTo(5.2, 6.8); cesto.lineTo(21.5, 6.8); cesto.lineTo(19.6, 12.8); cesto.lineTo(6.6, 12.8);
                g.draw(cesto);
                g.fill(new Ellipse2D.Double(7.6, 18.2, 3.4, 3.4));
                g.fill(new Ellipse2D.Double(16, 18.2, 3.4, 3.4));
            }
        };
    }

    /** Círculo preenchido com um "check" branco (confirmação de compra). */
    public static Icon check(int tamanho, Color cor) {
        return new Base(tamanho, cor) {
            @Override void desenhar(Graphics2D g) {
                g.fill(new Ellipse2D.Double(1, 1, 22, 22));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Double v = new Path2D.Double();
                v.moveTo(6.8, 12.4); v.lineTo(10.6, 16.2); v.lineTo(17.4, 8.2);
                g.draw(v);
            }
        };
    }
}

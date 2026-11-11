package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Componentes Swing customizados que dão a identidade visual da loja. */
public final class Componentes {

    private Componentes() {}

    // ================================================================ BOTÃO
    public static class Botao extends JButton {
        public enum Estilo { PRIMARIO, SECUNDARIO, LINK }

        private final Estilo estilo;
        private boolean sobre;

        public Botao(String texto, Estilo estilo) {
            super(texto);
            this.estilo = estilo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Tema.fonte(Font.BOLD, 14f));
            setBorder(estilo == Estilo.LINK ? new EmptyBorder(4, 4, 4, 4) : new EmptyBorder(11, 20, 11, 20));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { sobre = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { sobre = false; repaint(); }
            });
        }

        public Botao compacto() {
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setFont(Tema.fonte(Font.BOLD, 13f));
            return this;
        }

        private Color corFundo() {
            if (!isEnabled()) return estilo == Estilo.LINK ? null : new Color(0xE9E9E9);
            switch (estilo) {
                case PRIMARIO:   return sobre ? Tema.AZUL_HOVER : Tema.AZUL;
                case SECUNDARIO: return sobre ? new Color(0xD3E3FA) : Tema.AZUL_CLARO;
                default:         return null;
            }
        }

        @Override public Color getForeground() {
            if (estilo == null) return super.getForeground();   // chamado pelo construtor de JButton
            if (!isEnabled()) return new Color(0xA0A0A0);
            switch (estilo) {
                case PRIMARIO: return Color.WHITE;
                default:       return sobre ? Tema.AZUL_HOVER : Tema.AZUL;
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Color fundo = corFundo();
            if (fundo != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.antialias(g2);
                g2.setColor(fundo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /** Botão de texto simples para a barra de navegação (sobre o fundo amarelo). */
    public static JButton linkNavegacao(String texto, Runnable acao) {
        JButton b = new JButton(texto);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(new Color(0x444444));
        b.setFont(Tema.fonte(Font.PLAIN, 13f));
        b.setBorder(new EmptyBorder(4, 10, 4, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> acao.run());
        return b;
    }

    // ============================================================== PAINEL
    /** Cartão branco com cantos arredondados e sombra suave. */
    public static class Painel extends JPanel {
        private final int raio;
        private boolean destaque;

        public Painel(LayoutManager layout) { this(layout, 10); }

        public Painel(LayoutManager layout, int raio) {
            super(layout);
            this.raio = raio;
            setOpaque(false);
            setBorder(new EmptyBorder(16, 16, 18, 16));
        }

        public void setDestaque(boolean d) {
            if (destaque != d) { destaque = d; repaint(); }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            g2.setColor(new Color(0, 0, 0, destaque ? 45 : 22));
            g2.fillRoundRect(0, 1, getWidth(), getHeight() - 1, raio, raio);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 2, raio, raio);
            g2.dispose();
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 2, raio, raio));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    // =========================================================== MINIATURA
    /** Placeholder de imagem: círculo colorido com a inicial do produto. */
    public static class Miniatura extends JPanel {
        private final String nome;
        private final int raio;

        public Miniatura(String nome, int largura, int altura, int raio) {
            this.nome = nome == null ? "?" : nome;
            this.raio = raio;
            setOpaque(false);
            setPreferredSize(new Dimension(largura, altura));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            g2.setColor(new Color(0xF4F4F4));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), raio, raio);

            int d = (int) (Math.min(getWidth(), getHeight()) * 0.55);
            int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
            g2.setColor(Tema.corDoProduto(nome));
            g2.fillOval(x, y, d, d);

            String inicial = nome.trim().isEmpty() ? "?" : nome.trim().substring(0, 1).toUpperCase();
            g2.setColor(Color.WHITE);
            g2.setFont(Tema.fonte(Font.BOLD, d * 0.5f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(inicial, x + (d - fm.stringWidth(inicial)) / 2, y + (d - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ============================================================== CAMPOS
    /** Campo de texto com texto de dica (placeholder). */
    public static class CampoHint extends JTextField {
        private final String hint;

        public CampoHint(String hint) {
            this.hint = hint;
            setFont(Tema.fonte(Font.PLAIN, 15f));
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.antialias(g2);
                g2.setColor(new Color(0xA0A0A0));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    // ========================================================== BANNER
    /** Faixa promocional em degradê azul. */
    public static class Banner extends JPanel {
        public Banner(String titulo, String subtitulo) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(new EmptyBorder(30, 36, 30, 36));
            JLabel t = new JLabel(titulo);
            t.setFont(Tema.fonte(Font.BOLD, 28f));
            t.setForeground(Color.WHITE);
            JLabel s = new JLabel(subtitulo);
            s.setFont(Tema.fonte(Font.PLAIN, 16f));
            s.setForeground(new Color(0xE3EDFB));
            t.setAlignmentX(LEFT_ALIGNMENT);
            s.setAlignmentX(LEFT_ALIGNMENT);
            add(Box.createVerticalGlue());
            add(t);
            add(Box.createVerticalStrut(8));
            add(s);
            add(Box.createVerticalGlue());
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, 130);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            g2.setPaint(new GradientPaint(0, 0, new Color(0x3483FA), getWidth(), getHeight(), new Color(0x1F58C7)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(new Color(255, 255, 255, 28));
            g2.fillOval(getWidth() - 210, -70, 280, 280);
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillOval(getWidth() - 330, 40, 200, 200);
            g2.dispose();
        }
    }

    // ============================================================== GRADE
    /** Grade responsiva: o número de colunas muda conforme a largura disponível. */
    public static class Grade extends JPanel {
        private final int larguraMinima, altura, espaco;
        int larguraAlvo = 1000;

        public Grade(int larguraMinima, int altura, int espaco) {
            super(null);
            this.larguraMinima = larguraMinima;
            this.altura = altura;
            this.espaco = espaco;
            setOpaque(false);
        }

        private int colunas(int largura) {
            return Math.max(1, Math.min(6, (largura + espaco) / (larguraMinima + espaco)));
        }

        @Override public Dimension getPreferredSize() {
            int n = getComponentCount();
            if (n == 0) return new Dimension(larguraAlvo, 0);
            int linhas = (n + colunas(larguraAlvo) - 1) / colunas(larguraAlvo);
            return new Dimension(larguraAlvo, linhas * altura + (linhas - 1) * espaco);
        }

        @Override public void doLayout() {
            int cols = colunas(getWidth());
            int larguraCard = (getWidth() - (cols - 1) * espaco) / cols;
            for (int i = 0; i < getComponentCount(); i++) {
                getComponent(i).setBounds((i % cols) * (larguraCard + espaco),
                        (i / cols) * (altura + espaco), larguraCard, altura);
            }
        }
    }

    // ============================================================= PÁGINA
    /** Página rolável: empilha os filhos numa coluna centralizada com largura máxima. */
    public static class Pagina extends JPanel implements Scrollable {
        public Pagina() {
            super(new Coluna(1180));
            setBackground(Tema.FUNDO);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(40, r.height - 40); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static class Coluna implements LayoutManager {
        private static final int MARGEM = 24, ESPACO = 16;
        private final int larguraMaxima;

        Coluna(int larguraMaxima) { this.larguraMaxima = larguraMaxima; }

        private int larguraDisponivel(Container p) {
            Container pai = p.getParent();
            int w = (pai instanceof JViewport) ? pai.getWidth() : p.getWidth();
            return w <= 0 ? 1000 : w;
        }

        private int larguraConteudo(int total) {
            return Math.max(240, Math.min(total - 2 * MARGEM, larguraMaxima));
        }

        @Override public Dimension preferredLayoutSize(Container p) {
            int total = larguraDisponivel(p), w = larguraConteudo(total), y = MARGEM;
            for (Component c : p.getComponents()) {
                if (!c.isVisible()) continue;
                if (c instanceof Grade) ((Grade) c).larguraAlvo = w;
                y += c.getPreferredSize().height + ESPACO;
            }
            return new Dimension(total, y - ESPACO + MARGEM);
        }

        @Override public Dimension minimumLayoutSize(Container p) { return preferredLayoutSize(p); }

        @Override public void layoutContainer(Container p) {
            int total = p.getWidth(), w = larguraConteudo(total), x = (total - w) / 2, y = MARGEM;
            for (Component c : p.getComponents()) {
                if (!c.isVisible()) continue;
                if (c instanceof Grade) ((Grade) c).larguraAlvo = w;
                int h = c.getPreferredSize().height;
                c.setBounds(x, y, w, h);
                y += h + ESPACO;
            }
        }

        @Override public void addLayoutComponent(String n, Component c) {}
        @Override public void removeLayoutComponent(Component c) {}
    }

    // =============================================================== TOAST
    /** Aviso temporário desenhado sobre a janela (usado como glass pane; não bloqueia cliques). */
    public static class Toast extends JComponent {
        private String mensagem;
        private boolean sucesso;
        private final Timer timer;

        public Toast() {
            setOpaque(false);
            timer = new Timer(3000, e -> { mensagem = null; repaint(); });
            timer.setRepeats(false);
        }

        @Override public boolean contains(int x, int y) { return false; }

        public void mostrar(String texto, boolean ok) {
            mensagem = texto;
            sucesso = ok;
            timer.restart();
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (mensagem == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            g2.setFont(Tema.fonte(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(mensagem) + 48, h = 44, x = (getWidth() - w) / 2, y = 128;
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(x, y + 3, w, h, 12, 12);
            g2.setColor(sucesso ? Tema.VERDE : Tema.VERMELHO);
            g2.fillRoundRect(x, y, w, h, 12, 12);
            g2.setColor(Color.WHITE);
            g2.drawString(mensagem, x + 24, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ================================================================ UTIL
    public static JLabel rotulo(String texto, int estilo, float tamanho, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fonte(estilo, tamanho));
        l.setForeground(cor);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static void alinharEsquerda(JComponent... componentes) {
        for (JComponent c : componentes) c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}

package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
    /**
     * Imagem do produto. Se o vendedor enviou uma foto, ela é exibida; caso contrário usa uma
     * ilustração genérica desenhada em Java2D, com o aviso "imagens apenas ilustrativas".
     */
    public static class Miniatura extends JPanel {
        private static final java.util.Map<String, java.awt.image.BufferedImage> CACHE = new java.util.HashMap<>();

        private final Ilustracoes.Tipo tipo;
        private final java.awt.image.BufferedImage foto;     // null = ilustração genérica
        private final int raio;
        private java.awt.image.BufferedImage fotoEscalada;
        private int escaladaL, escaladaA;

        public Miniatura(model.Produto produto, int largura, int altura, int raio) {
            this(produto.getNome(), carregarFoto(produto.getImagem()), largura, altura, raio);
        }

        /** Só a ilustração genérica (usado quando ainda não há produto, ex.: prévia). */
        public Miniatura(String nome, int largura, int altura, int raio) {
            this(nome, null, largura, altura, raio);
        }

        private Miniatura(String nome, java.awt.image.BufferedImage foto, int largura, int altura, int raio) {
            this.tipo = Ilustracoes.tipoDe(nome);
            this.foto = foto;
            this.raio = raio;
            setOpaque(false);
            setPreferredSize(new Dimension(largura, altura));
        }

        /** Lê a foto do disco (com cache). Arquivo ausente ou ilegível = null (cai na ilustração). */
        private static java.awt.image.BufferedImage carregarFoto(String caminho) {
            if (caminho == null) return null;
            java.io.File arquivo = new java.io.File(caminho);
            if (!arquivo.isFile()) return null;
            String chave = caminho + "@" + arquivo.lastModified();   // trocar a imagem invalida o cache
            synchronized (CACHE) {
                if (!CACHE.containsKey(chave)) {
                    try {
                        CACHE.put(chave, javax.imageio.ImageIO.read(arquivo));
                    } catch (java.io.IOException | RuntimeException e) {
                        CACHE.put(chave, null);
                    }
                }
                return CACHE.get(chave);
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            if (foto != null) pintarFoto(g2); else pintarIlustracao(g2);
            g2.dispose();
        }

        private void pintarFoto(Graphics2D g2) {
            int w = getWidth(), h = getHeight();
            Shape forma = new RoundRectangle2D.Float(0, 0, w, h, raio, raio);
            g2.setColor(Color.WHITE);
            g2.fill(forma);
            g2.setColor(new Color(0xEDEDED));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, raio, raio));

            // "contain": a foto inteira aparece, centralizada, sem cortes
            int margem = Math.max(4, Math.min(w, h) / 24);
            double escala = Math.min((w - 2.0 * margem) / foto.getWidth(), (h - 2.0 * margem) / foto.getHeight());
            int fw = Math.max(1, (int) Math.round(foto.getWidth() * escala));
            int fh = Math.max(1, (int) Math.round(foto.getHeight() * escala));
            if (fotoEscalada == null || escaladaL != fw || escaladaA != fh) {
                fotoEscalada = redimensionar(foto, fw, fh);
                escaladaL = fw;
                escaladaA = fh;
            }
            g2.clip(forma);
            g2.drawImage(fotoEscalada, (w - fw) / 2, (h - fh) / 2, null);
        }

        private static java.awt.image.BufferedImage redimensionar(java.awt.image.BufferedImage origem, int w, int h) {
            java.awt.image.BufferedImage atual = origem;
            while (atual.getWidth() / 2 > w && atual.getHeight() / 2 > h) {   // reduz em etapas p/ manter nitidez
                atual = passo(atual, atual.getWidth() / 2, atual.getHeight() / 2);
            }
            return passo(atual, w, h);
        }

        private static java.awt.image.BufferedImage passo(java.awt.image.BufferedImage o, int w, int h) {
            java.awt.image.BufferedImage d = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = d.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(o, 0, 0, w, h, null);
            g.dispose();
            return d;
        }

        private void pintarIlustracao(Graphics2D g2) {
            int w = getWidth(), h = getHeight();

            // fundo creme em degradê suave
            Shape forma = new RoundRectangle2D.Float(0, 0, w, h, raio, raio);
            g2.setPaint(new GradientPaint(0, 0, new Color(0xFFF8EC), 0, h, new Color(0xFBE9CC)));
            g2.fill(forma);

            // aviso (texto curto quando a imagem é pequena)
            boolean pequena = w < 120;
            String aviso = pequena ? "Ilustrativa" : "Imagens apenas ilustrativas";
            float tamFonte = pequena ? Math.max(7f, w / 9f) : Math.max(9f, Math.min(11f, w / 22f));
            g2.setFont(Tema.fonte(Font.PLAIN, tamFonte));
            FontMetrics fm = g2.getFontMetrics();
            int larguraAviso = fm.stringWidth(aviso) + 12, alturaAviso = fm.getHeight() + 2;

            // ilustração centralizada na área acima do aviso
            double areaAltura = h - alturaAviso - 6;
            double lado = Math.min(w, areaAltura) * 0.86;
            Ilustracoes.desenhar(g2, tipo, w / 2.0, areaAltura / 2.0 + 3, lado);

            // pílula translúcida com o aviso, no canto inferior
            int x = (w - larguraAviso) / 2, y = h - alturaAviso - 5;
            g2.setColor(new Color(255, 255, 255, 190));
            g2.fillRoundRect(x, y, larguraAviso, alturaAviso, alturaAviso, alturaAviso);
            g2.setColor(new Color(0x8A6D3B));
            g2.drawString(aviso, x + 6, y + (alturaAviso - fm.getHeight()) / 2 + fm.getAscent());
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

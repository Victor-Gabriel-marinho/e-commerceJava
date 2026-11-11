package gui;

import exception.DadosInvalidosException;
import exception.EstoqueInsuficienteException;
import exception.ProdutoInexistenteException;
import model.Carrinho;
import model.Compra;
import model.ItemCarrinho;
import model.Produto;
import service.CompraService;
import service.ProdutoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Janela principal da loja: cabeçalho amarelo (busca, navegação, carrinho) + páginas. */
public class TelaLoja extends JFrame {

    private static final String LOJA = "loja", CARRINHO = "carrinho", HISTORICO = "historico";

    private final ProdutoService produtoService;
    private final CompraService compraService;
    private final Carrinho carrinho = new Carrinho();
    private String comprador;

    private final CardLayout layoutPaginas = new CardLayout();
    private final JPanel paginas = new JPanel(layoutPaginas);
    private PainelProdutos painelProdutos;
    private PainelCarrinho painelCarrinho;
    private PainelHistorico painelHistorico;

    private Componentes.CampoHint campoBusca;
    private JButton botaoUsuario;
    private BotaoCarrinho botaoCarrinho;
    private final Componentes.Toast toast = new Componentes.Toast();

    public TelaLoja(ProdutoService produtoService, CompraService compraService) {
        super("E-Commerce");
        this.produtoService = produtoService;
        this.compraService = compraService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);

        painelProdutos = new PainelProdutos(produtoService, this::adicionarAoCarrinho, this::abrirProduto);
        painelCarrinho = new PainelCarrinho(carrinho, this::atualizarContador, this::iniciarCheckout,
                this::mostrarLoja, msg -> toast.mostrar(msg, false));
        painelHistorico = new PainelHistorico(compraService, c -> DialogoComprovante.exibir(this, c, false));
        paginas.add(painelProdutos, LOJA);
        paginas.add(painelCarrinho, CARRINHO);
        paginas.add(painelHistorico, HISTORICO);
        add(paginas, BorderLayout.CENTER);

        setGlassPane(toast);
        toast.setVisible(true);

        setMinimumSize(new Dimension(820, 560));
        setSize(1240, 820);
        setLocationRelativeTo(null);
    }

    // ================================================================ CABEÇALHO
    private JComponent criarCabecalho() {
        JPanel cab = new JPanel(new GridBagLayout());
        cab.setBackground(Tema.AMARELO);
        cab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 35)),
                new EmptyBorder(12, 28, 4, 28)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;

        // Logo
        JButton logo = new JButton("e-commerce", Icones.carrinho(30, Tema.LOGO));
        logo.setFont(Tema.fonte(Font.BOLD | Font.ITALIC, 24f));
        logo.setForeground(Tema.LOGO);
        logo.setIconTextGap(8);
        logo.setContentAreaFilled(false);
        logo.setBorderPainted(false);
        logo.setFocusPainted(false);
        logo.setOpaque(false);
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.addActionListener(e -> { campoBusca.setText(""); painelProdutos.filtrar(""); mostrarLoja(); });
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        cab.add(logo, c);

        // Busca
        Componentes.Painel busca = new Componentes.Painel(new BorderLayout(), 6);
        busca.setBorder(new EmptyBorder(0, 12, 0, 0));
        campoBusca = new Componentes.CampoHint("Buscar produtos");
        campoBusca.addActionListener(e -> buscar());
        JButton lupa = new JButton(Icones.lupa(20, new Color(0x666666)));
        lupa.setContentAreaFilled(false);
        lupa.setFocusPainted(false);
        lupa.setOpaque(false);
        lupa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Tema.BORDA), new EmptyBorder(0, 14, 0, 14)));
        lupa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lupa.addActionListener(e -> buscar());
        busca.add(campoBusca, BorderLayout.CENTER);
        busca.add(lupa, BorderLayout.EAST);
        busca.setPreferredSize(new Dimension(300, 42));
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 36, 0, 24);
        cab.add(busca, c);

        // Carrinho
        botaoCarrinho = new BotaoCarrinho();
        botaoCarrinho.addActionListener(e -> mostrarCarrinho());
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        cab.add(botaoCarrinho, c);

        // Navegação
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        nav.setOpaque(false);
        botaoUsuario = Componentes.linkNavegacao("Entre", this::entrar);
        nav.add(Componentes.linkNavegacao("Início", this::mostrarLoja));
        nav.add(Componentes.linkNavegacao("Minhas compras", this::mostrarHistorico));
        nav.add(Componentes.linkNavegacao("Vender", this::abrirVendedor));
        nav.add(botaoUsuario);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 0, 0);
        cab.add(nav, c);
        return cab;
    }

    /** Ícone do carrinho com o contador de unidades. */
    private static class BotaoCarrinho extends JButton {
        private int quantidade;

        BotaoCarrinho() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(54, 44));
            setToolTipText("Carrinho");
        }

        void setQuantidade(int q) { this.quantidade = q; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.antialias(g2);
            Icones.carrinho(28, new Color(0x333333)).paintIcon(this, g2, 8, 10);
            if (quantidade > 0) {
                String t = quantidade > 99 ? "99+" : String.valueOf(quantidade);
                g2.setFont(Tema.fonte(Font.BOLD, 11f));
                FontMetrics fm = g2.getFontMetrics();
                int w = Math.max(18, fm.stringWidth(t) + 10), x = getWidth() - w - 2, y = 2;
                g2.setColor(Tema.AZUL);
                g2.fillRoundRect(x, y, w, 18, 18, 18);
                g2.setColor(Color.WHITE);
                g2.drawString(t, x + (w - fm.stringWidth(t)) / 2, y + (18 - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ============================================================== NAVEGAÇÃO
    private void mostrarLoja() { layoutPaginas.show(paginas, LOJA); }

    private void mostrarCarrinho() {
        painelCarrinho.atualizar();
        layoutPaginas.show(paginas, CARRINHO);
    }

    private void mostrarHistorico() {
        if (!exigirComprador()) return;
        painelHistorico.atualizar(comprador);
        layoutPaginas.show(paginas, HISTORICO);
    }

    private void buscar() {
        painelProdutos.filtrar(campoBusca.getText());
        mostrarLoja();
    }

    // ================================================================ AÇÕES
    private void abrirProduto(Produto p) {
        new DialogoProduto(this, p, (produto, qtd, comprarAgora) -> {
            if (adicionar(produto, qtd) && comprarAgora) iniciarCheckout();
        }).setVisible(true);
    }

    private void adicionarAoCarrinho(Produto p) {
        adicionar(p, 1);
    }

    private boolean adicionar(Produto p, int quantidade) {
        try {
            carrinho.adicionar(p, quantidade);
            atualizarContador();
            toast.mostrar("Adicionado ao carrinho: " + p.getNome(), true);
            return true;
        } catch (EstoqueInsuficienteException | DadosInvalidosException | ProdutoInexistenteException e) {
            toast.mostrar(e.getMessage(), false);
            return false;
        }
    }

    private void iniciarCheckout() {
        if (carrinho.estaVazio()) {
            toast.mostrar("Seu carrinho está vazio.", false);
            return;
        }
        if (!exigirComprador()) return;

        DialogoPagamento dialogo = new DialogoPagamento(this, compraService, carrinho, comprador);
        dialogo.setVisible(true);

        Compra compra = dialogo.getResultado();
        if (compra != null) {                 // o carrinho só é esvaziado se a compra foi concluída
            carrinho.limpar();
            atualizarContador();
            painelProdutos.atualizar();
            painelCarrinho.atualizar();
            mostrarLoja();
            DialogoComprovante.exibir(this, compra, true);
        }
    }

    private void abrirVendedor() {
        new DialogoVendedor(this, produtoService).setVisible(true);
        // Produtos removidos pelo vendedor não podem continuar no carrinho
        for (ItemCarrinho item : carrinho.getItens()) {
            try {
                produtoService.buscarPorId(item.getProduto().getId());
            } catch (ProdutoInexistenteException e) {
                carrinho.remover(item.getProduto().getId());
            }
        }
        atualizarContador();
        painelProdutos.atualizar();
        painelCarrinho.atualizar();
    }

    // ================================================================ USUÁRIO
    private void entrar() { pedirNome(); }

    private boolean exigirComprador() {
        return comprador != null || pedirNome();
    }

    private boolean pedirNome() {
        Object resposta = JOptionPane.showInputDialog(this,
                "Como podemos te chamar?\nSeu nome identifica o histórico das suas compras.",
                "Entre", JOptionPane.PLAIN_MESSAGE, null, null, comprador == null ? "" : comprador);
        if (resposta == null || resposta.toString().trim().isEmpty()) return false;
        comprador = resposta.toString().trim();
        botaoUsuario.setText("Olá, " + comprador);
        return true;
    }

    private void atualizarContador() {
        int unidades = 0;
        for (ItemCarrinho i : carrinho.getItens()) unidades += i.getQuantidade();
        botaoCarrinho.setQuantidade(unidades);
    }
}

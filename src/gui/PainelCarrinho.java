package gui;

import exception.DadosInvalidosException;
import exception.EstoqueInsuficienteException;
import exception.ProdutoInexistenteException;
import model.Carrinho;
import model.CartaoDebito;
import model.ItemCarrinho;
import model.Pix;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Página do carrinho: lista de itens à esquerda e resumo da compra à direita. */
public class PainelCarrinho extends JScrollPane {

    private final Carrinho carrinho;
    private final Runnable aoAlterar;       // atualiza contador do cabeçalho
    private final Runnable aoFinalizar;
    private final Runnable aoContinuarComprando;
    private final java.util.function.Consumer<String> aoErro;

    private final Componentes.Pagina pagina = new Componentes.Pagina();

    public PainelCarrinho(Carrinho carrinho, Runnable aoAlterar, Runnable aoFinalizar,
                          Runnable aoContinuarComprando, java.util.function.Consumer<String> aoErro) {
        this.carrinho = carrinho;
        this.aoAlterar = aoAlterar;
        this.aoFinalizar = aoFinalizar;
        this.aoContinuarComprando = aoContinuarComprando;
        this.aoErro = aoErro;

        setBorder(null);
        getViewport().setBackground(Tema.FUNDO);
        getVerticalScrollBar().setUnitIncrement(18);
        setViewportView(pagina);
        atualizar();
    }

    public void atualizar() {
        pagina.removeAll();

        JLabel titulo = new JLabel("Carrinho");
        titulo.setFont(Tema.fonte(Font.BOLD, 22f));
        titulo.setForeground(Tema.TEXTO);
        pagina.add(titulo);

        if (carrinho.estaVazio()) {
            pagina.add(criarVazio());
        } else {
            JPanel conteudo = new JPanel(new BorderLayout(16, 0));
            conteudo.setOpaque(false);
            JPanel esquerda = new JPanel(new BorderLayout());   // prende a lista ao topo (sem esticar as linhas)
            esquerda.setOpaque(false);
            esquerda.add(criarItens(), BorderLayout.NORTH);
            conteudo.add(esquerda, BorderLayout.CENTER);

            JPanel direita = new JPanel(new BorderLayout()) {
                @Override public Dimension getPreferredSize() {   // largura fixa, altura natural
                    Dimension d = super.getPreferredSize();
                    d.width = 340;
                    return d;
                }
            };
            direita.setOpaque(false);
            direita.add(criarResumo(), BorderLayout.NORTH);
            conteudo.add(direita, BorderLayout.EAST);
            pagina.add(conteudo);
        }
        pagina.revalidate();
        pagina.repaint();
    }

    // ------------------------------------------------------------ vazio
    private JComponent criarVazio() {
        Componentes.Painel p = new Componentes.Painel(new BorderLayout(0, 16), 10);
        p.setBorder(new EmptyBorder(48, 24, 48, 24));
        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel icone = new JLabel(Icones.carrinho(56, new Color(0xBBBBBB)));
        JLabel t = new JLabel("Seu carrinho está vazio");
        t.setFont(Tema.fonte(Font.BOLD, 20f));
        t.setForeground(Tema.TEXTO);
        JLabel s = new JLabel("Veja os produtos e adicione o que você quiser comprar.");
        s.setFont(Tema.fonte(Font.PLAIN, 14f));
        s.setForeground(Tema.TEXTO_SUAVE);
        Componentes.Botao b = new Componentes.Botao("Descobrir produtos", Componentes.Botao.Estilo.PRIMARIO);
        b.addActionListener(e -> aoContinuarComprando.run());
        for (JComponent c : new JComponent[]{icone, t, s, b}) c.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(icone);
        centro.add(Box.createVerticalStrut(12));
        centro.add(t);
        centro.add(Box.createVerticalStrut(6));
        centro.add(s);
        centro.add(Box.createVerticalStrut(20));
        centro.add(b);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------ itens
    private JComponent criarItens() {
        Componentes.Painel card = new Componentes.Painel(new BorderLayout(), 10);
        card.setBorder(new EmptyBorder(4, 20, 6, 20));
        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));

        java.util.List<ItemCarrinho> itens = carrinho.getItens();
        for (int i = 0; i < itens.size(); i++) {
            lista.add(criarLinha(itens.get(i), i < itens.size() - 1));
        }
        card.add(lista, BorderLayout.CENTER);
        return card;
    }

    private JComponent criarLinha(ItemCarrinho item, boolean separador) {
        JPanel linha = new JPanel(new GridBagLayout());
        linha.setOpaque(false);
        linha.setBorder(BorderFactory.createCompoundBorder(
                separador ? BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDA) : BorderFactory.createEmptyBorder(),
                new EmptyBorder(16, 0, 16, 0)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;

        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 16);
        linha.add(new Componentes.Miniatura(item.getProduto().getNome(), 76, 76, 10), c);

        // nome + unitário + remover
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel nome = Componentes.rotulo(item.getProduto().getNome(), Font.PLAIN, 16f, Tema.TEXTO);
        JLabel unit = Componentes.rotulo(Tema.moeda(item.getProduto().getPreco()) + " cada", Font.PLAIN, 12f, Tema.TEXTO_SUAVE);
        Componentes.Botao remover = new Componentes.Botao("Excluir", Componentes.Botao.Estilo.LINK);
        remover.setFont(Tema.fonte(Font.PLAIN, 13f));
        remover.setAlignmentX(Component.LEFT_ALIGNMENT);
        remover.addActionListener(e -> executar(() -> carrinho.remover(item.getProduto().getId())));
        info.add(nome);
        info.add(unit);
        info.add(Box.createVerticalStrut(4));
        info.add(remover);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 12);
        linha.add(info, c);

        // quantidade
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        linha.add(criarSeletorQuantidade(item), c);

        // subtotal
        JLabel subtotal = new JLabel(Tema.moeda(item.getSubtotal()), SwingConstants.RIGHT);
        subtotal.setFont(Tema.fonte(Font.PLAIN, 18f));
        subtotal.setForeground(Tema.TEXTO);
        subtotal.setPreferredSize(new Dimension(120, 24));
        c.gridx = 3;
        c.insets = new Insets(0, 8, 0, 0);
        linha.add(subtotal, c);
        return linha;
    }

    private JComponent criarSeletorQuantidade(ItemCarrinho item) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        Componentes.Botao menos = new Componentes.Botao("−", Componentes.Botao.Estilo.SECUNDARIO).compacto();
        Componentes.Botao mais = new Componentes.Botao("+", Componentes.Botao.Estilo.SECUNDARIO).compacto();
        menos.setEnabled(item.getQuantidade() > 1);
        JLabel qtd = new JLabel(String.valueOf(item.getQuantidade()), SwingConstants.CENTER);
        qtd.setFont(Tema.fonte(Font.BOLD, 15f));
        qtd.setPreferredSize(new Dimension(28, 24));
        String id = item.getProduto().getId();
        menos.addActionListener(e -> executar(() -> carrinho.alterarQuantidade(id, item.getQuantidade() - 1)));
        mais.addActionListener(e -> executar(() -> carrinho.alterarQuantidade(id, item.getQuantidade() + 1)));
        p.add(menos);
        p.add(qtd);
        p.add(mais);
        return p;
    }

    // ----------------------------------------------------------- resumo
    private JComponent criarResumo() {
        Componentes.Painel card = new Componentes.Painel(new BorderLayout(), 10);
        card.setBorder(new EmptyBorder(20, 20, 22, 20));
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        int unidades = 0;
        for (ItemCarrinho i : carrinho.getItens()) unidades += i.getQuantidade();
        double subtotal = carrinho.getSubtotal();
        double noPix = new Pix("-").calcularValorFinal(subtotal);
        double noDebito = new CartaoDebito("", "").calcularValorFinal(subtotal);

        JLabel titulo = Componentes.rotulo("Resumo da compra", Font.BOLD, 18f, Tema.TEXTO);
        col.add(titulo);
        col.add(Box.createVerticalStrut(16));
        col.add(linhaValor("Produtos (" + unidades + ")", Tema.moeda(subtotal), Font.PLAIN, 14f, Tema.TEXTO));
        col.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setForeground(Tema.BORDA);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        col.add(sep);
        col.add(Box.createVerticalStrut(10));
        col.add(linhaValor("Total", Tema.moeda(subtotal), Font.BOLD, 20f, Tema.TEXTO));
        col.add(Box.createVerticalStrut(12));
        col.add(Componentes.rotulo("No Pix: " + Tema.moeda(noPix) + " (" + Tema.percentualDesconto(new Pix("-")) + "% OFF)",
                Font.BOLD, 13f, Tema.VERDE));
        col.add(Box.createVerticalStrut(2));
        col.add(Componentes.rotulo("No débito: " + Tema.moeda(noDebito) + " (" + Tema.percentualDesconto(new CartaoDebito("", "")) + "% OFF)",
                Font.BOLD, 13f, Tema.VERDE));
        col.add(Box.createVerticalStrut(20));

        Componentes.Botao continuar = new Componentes.Botao("Continuar a compra", Componentes.Botao.Estilo.PRIMARIO);
        continuar.addActionListener(e -> aoFinalizar.run());
        Componentes.Botao voltar = new Componentes.Botao("Continuar comprando", Componentes.Botao.Estilo.SECUNDARIO);
        voltar.addActionListener(e -> aoContinuarComprando.run());
        JPanel botoes = new JPanel(new GridLayout(2, 1, 0, 8));
        botoes.setOpaque(false);
        botoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        botoes.add(continuar);
        botoes.add(voltar);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(botoes);

        card.add(col, BorderLayout.CENTER);
        return card;
    }

    private JComponent linhaValor(String rotulo, String valor, int estilo, float tamanho, Color cor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel a = new JLabel(rotulo), b = new JLabel(valor);
        a.setFont(Tema.fonte(estilo, tamanho));
        b.setFont(Tema.fonte(estilo, tamanho));
        a.setForeground(cor);
        b.setForeground(cor);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.EAST);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    // ----------------------------------------------------------- ações
    private void executar(Runnable operacao) {
        try {
            operacao.run();
        } catch (EstoqueInsuficienteException | DadosInvalidosException | ProdutoInexistenteException e) {
            aoErro.accept(e.getMessage());
        }
        atualizar();
        aoAlterar.run();
    }
}

package gui;

import exception.*;
import model.*;
import service.CompraService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Checkout: escolha da forma de pagamento, dados e resumo com desconto em tempo real. */
public class DialogoPagamento extends JDialog {

    private final CompraService compraService;
    private final Carrinho carrinho;
    private final String comprador;
    private Compra resultado;

    private final JRadioButton credito = opcao("Cartão de crédito", "Valor integral, sem desconto", Tema.TEXTO_SUAVE);
    private final JRadioButton debito;
    private final JRadioButton pix;

    private final CardLayout camposLayout = new CardLayout();
    private final JPanel campos = new JPanel(camposLayout);
    private final JTextField numero = new JTextField();
    private final JPasswordField cvv = new JPasswordField();
    private final JTextField chavePix = new JTextField();

    private final JLabel lblSubtotal = new JLabel(), lblDesconto = new JLabel(), lblTotal = new JLabel();
    private final JLabel lblErro = new JLabel(" ");

    public DialogoPagamento(Window dono, CompraService compraService, Carrinho carrinho, String comprador) {
        super(dono, "Finalizar compra", ModalityType.APPLICATION_MODAL);
        this.compraService = compraService;
        this.carrinho = carrinho;
        this.comprador = comprador;

        debito = opcao("Cartão de débito", Tema.percentualDesconto(new CartaoDebito("", "")) + "% de desconto", Tema.VERDE);
        pix = opcao("Pix", Tema.percentualDesconto(new Pix("-")) + "% de desconto", Tema.VERDE);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(credito);
        grupo.add(debito);
        grupo.add(pix);
        pix.setSelected(true);

        JPanel raiz = new JPanel(new BorderLayout(20, 0));
        raiz.setBackground(Tema.FUNDO);
        raiz.setBorder(new EmptyBorder(20, 20, 20, 20));
        raiz.add(criarLadoPagamento(), BorderLayout.CENTER);
        raiz.add(criarResumo(), BorderLayout.EAST);
        setContentPane(raiz);

        credito.addActionListener(e -> aoTrocarForma());
        debito.addActionListener(e -> aoTrocarForma());
        pix.addActionListener(e -> aoTrocarForma());
        aoTrocarForma();

        pack();
        setMinimumSize(new Dimension(780, getHeight()));
        setLocationRelativeTo(dono);
        getRootPane().setDefaultButton(null);
    }

    /** Compra concluída, ou null se o usuário cancelou / o pagamento não foi feito. */
    public Compra getResultado() { return resultado; }

    private static JRadioButton opcao(String titulo, String detalhe, Color corDetalhe) {
        String hex = String.format("#%06X", corDetalhe.getRGB() & 0xFFFFFF);
        JRadioButton r = new JRadioButton("<html><b style='font-size:13px'>" + titulo
                + "</b><br><span style='color:" + hex + "'>" + detalhe + "</span></html>");
        r.setOpaque(false);
        r.setFont(Tema.fonte(Font.PLAIN, 13f));
        r.setBorder(new EmptyBorder(8, 0, 8, 0));
        r.setFocusPainted(false);
        r.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        return r;
    }

    private JComponent criarLadoPagamento() {
        Componentes.Painel card = new Componentes.Painel(new BorderLayout(), 10);
        card.setBorder(new EmptyBorder(20, 22, 22, 22));
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(Componentes.rotulo("Escolha como pagar", Font.BOLD, 18f, Tema.TEXTO));
        col.add(Box.createVerticalStrut(8));
        col.add(pix);
        col.add(debito);
        col.add(credito);
        col.add(Box.createVerticalStrut(10));

        JPanel cartao = new JPanel(new GridLayout(0, 1, 0, 6));
        cartao.setOpaque(false);
        cartao.add(rotuloCampo("Número do cartão (16 dígitos)"));
        cartao.add(estilizar(numero));
        cartao.add(rotuloCampo("CVV (3 dígitos)"));
        cartao.add(estilizar(cvv));

        JPanel painelPix = new JPanel(new GridLayout(0, 1, 0, 6));
        painelPix.setOpaque(false);
        painelPix.add(rotuloCampo("Chave Pix"));
        painelPix.add(estilizar(chavePix));

        campos.setOpaque(false);
        campos.add(cartao, "cartao");
        campos.add(painelPix, "pix");
        campos.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(campos);

        card.add(col, BorderLayout.NORTH);
        return card;
    }

    private JLabel rotuloCampo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Tema.fonte(Font.PLAIN, 12f));
        l.setForeground(Tema.TEXTO_SUAVE);
        return l;
    }

    private JTextField estilizar(JTextField campo) {
        campo.setFont(Tema.fonte(Font.PLAIN, 15f));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D0D0), 1, true), new EmptyBorder(7, 10, 7, 10)));
        return campo;
    }

    private JComponent criarResumo() {
        Componentes.Painel card = new Componentes.Painel(new BorderLayout(), 10) {
            @Override public Dimension getPreferredSize() {   // largura fixa, altura natural
                Dimension d = super.getPreferredSize();
                d.width = 300;
                return d;
            }
        };
        card.setBorder(new EmptyBorder(20, 22, 22, 22));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(Componentes.rotulo("Resumo da compra", Font.BOLD, 18f, Tema.TEXTO));
        col.add(Box.createVerticalStrut(16));
        col.add(linha("Subtotal", lblSubtotal, Tema.TEXTO, 14f, Font.PLAIN));
        col.add(Box.createVerticalStrut(6));
        col.add(linha("Desconto", lblDesconto, Tema.VERDE, 14f, Font.PLAIN));
        col.add(Box.createVerticalStrut(12));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        col.add(sep);
        col.add(Box.createVerticalStrut(12));
        col.add(linha("Você paga", lblTotal, Tema.TEXTO, 20f, Font.BOLD));
        col.add(Box.createVerticalStrut(18));

        lblErro.setForeground(Tema.VERMELHO);
        lblErro.setFont(Tema.fonte(Font.PLAIN, 12f));
        lblErro.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(lblErro);
        col.add(Box.createVerticalStrut(8));

        Componentes.Botao confirmar = new Componentes.Botao("Confirmar pagamento", Componentes.Botao.Estilo.PRIMARIO);
        Componentes.Botao cancelar = new Componentes.Botao("Cancelar", Componentes.Botao.Estilo.SECUNDARIO);
        confirmar.addActionListener(e -> confirmar());
        cancelar.addActionListener(e -> dispose());
        JPanel botoes = new JPanel(new GridLayout(2, 1, 0, 8));
        botoes.setOpaque(false);
        botoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        botoes.add(confirmar);
        botoes.add(cancelar);
        col.add(botoes);

        card.add(col, BorderLayout.NORTH);
        return card;
    }

    private JComponent linha(String rotulo, JLabel valor, Color cor, float tamanho, int estilo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel a = new JLabel(rotulo);
        a.setFont(Tema.fonte(estilo, tamanho));
        a.setForeground(cor);
        valor.setFont(Tema.fonte(estilo, tamanho));
        valor.setForeground(cor);
        p.add(a, BorderLayout.WEST);
        p.add(valor, BorderLayout.EAST);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    // ------------------------------------------------------------ lógica
    /** Monta a forma de pagamento com o que está nos campos (a validação real acontece em processar()). */
    private FormaPagamento formaSelecionada() {
        if (credito.isSelected()) return new CartaoCredito(numero.getText(), new String(cvv.getPassword()));
        if (debito.isSelected()) return new CartaoDebito(numero.getText(), new String(cvv.getPassword()));
        return new Pix(chavePix.getText());
    }

    private void aoTrocarForma() {
        camposLayout.show(campos, pix.isSelected() ? "pix" : "cartao");
        lblErro.setText(" ");
        FormaPagamento forma = formaSelecionada();
        double subtotal = carrinho.getSubtotal();
        lblSubtotal.setText(Tema.moeda(subtotal));
        double desconto = forma.calcularDesconto(subtotal);
        lblDesconto.setText(desconto > 0 ? "− " + Tema.moeda(desconto) : Tema.moeda(0));
        lblTotal.setText(Tema.moeda(forma.calcularValorFinal(subtotal)));
    }

    private void confirmar() {
        try {
            resultado = compraService.finalizar(comprador, carrinho.paraMapaDeItens(), formaSelecionada());
            dispose();
        } catch (PagamentoRecusadoException e) {
            lblErro.setText("<html>Pagamento recusado: " + e.getMessage() + "</html>");
        } catch (DadosInvalidosException | ProdutoInexistenteException | EstoqueInsuficienteException e) {
            lblErro.setText("<html>" + e.getMessage() + "</html>");
        } catch (PersistenciaException e) {
            lblErro.setText("<html>Erro ao acessar os arquivos: " + e.getMessage() + "</html>");
        }
        pack();
    }
}

package gui;

import model.Pix;
import model.Produto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Detalhe do produto: imagem grande, preço, quantidade e ações de compra. */
public class DialogoProduto extends JDialog {

    public interface Acao {
        void executar(Produto produto, int quantidade, boolean comprarAgora);
    }

    public DialogoProduto(Window dono, Produto produto, Acao acao) {
        super(dono, produto.getNome(), ModalityType.APPLICATION_MODAL);

        int estoque = produto.getQuantidadeEmEstoque();
        boolean esgotado = estoque <= 0;

        JPanel raiz = new JPanel(new BorderLayout(28, 0));
        raiz.setBackground(Color.WHITE);
        raiz.setBorder(new EmptyBorder(28, 28, 28, 28));
        raiz.add(new Componentes.Miniatura(produto.getNome(), 280, 280, 14), BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nome = Componentes.rotulo(produto.getNome(), Font.BOLD, 24f, Tema.TEXTO);
        JLabel status = esgotado
                ? Componentes.rotulo("Esgotado", Font.BOLD, 14f, Tema.VERMELHO)
                : Componentes.rotulo(estoque <= 5 ? "Últimas " + estoque + " unidades!" : estoque + " unidades disponíveis",
                        Font.PLAIN, 14f, estoque <= 5 ? Tema.LARANJA : Tema.TEXTO_SUAVE);
        JLabel preco = Componentes.rotulo(Tema.moeda(produto.getPreco()), Font.PLAIN, 38f, Tema.TEXTO);
        JLabel pix = Componentes.rotulo("ou " + Tema.moeda(new Pix("-").calcularValorFinal(produto.getPreco()))
                + " no Pix (" + Tema.percentualDesconto(new Pix("-")) + "% OFF)", Font.BOLD, 15f, Tema.VERDE);

        JSpinner quantidade = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, estoque), 1));
        quantidade.setEnabled(!esgotado);
        quantidade.setFont(Tema.fonte(Font.PLAIN, 15f));
        quantidade.setMaximumSize(new Dimension(90, 34));
        quantidade.setPreferredSize(new Dimension(90, 34));
        JPanel linhaQtd = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaQtd.setOpaque(false);
        JLabel lq = new JLabel("Quantidade:  ");
        lq.setFont(Tema.fonte(Font.PLAIN, 14f));
        linhaQtd.add(lq);
        linhaQtd.add(quantidade);

        Componentes.Botao comprar = new Componentes.Botao("Comprar agora", Componentes.Botao.Estilo.PRIMARIO);
        Componentes.Botao adicionar = new Componentes.Botao("Adicionar ao carrinho", Componentes.Botao.Estilo.SECUNDARIO);
        comprar.setEnabled(!esgotado);
        adicionar.setEnabled(!esgotado);
        comprar.addActionListener(e -> { dispose(); acao.executar(produto, (Integer) quantidade.getValue(), true); });
        adicionar.addActionListener(e -> { dispose(); acao.executar(produto, (Integer) quantidade.getValue(), false); });

        JPanel botoes = new JPanel(new GridLayout(2, 1, 0, 10));
        botoes.setOpaque(false);
        botoes.setMaximumSize(new Dimension(320, 110));
        botoes.add(comprar);
        botoes.add(adicionar);

        Componentes.alinharEsquerda(linhaQtd, botoes);
        info.add(nome);
        info.add(Box.createVerticalStrut(6));
        info.add(status);
        info.add(Box.createVerticalStrut(20));
        info.add(preco);
        info.add(Box.createVerticalStrut(4));
        info.add(pix);
        info.add(Box.createVerticalStrut(24));
        info.add(linhaQtd);
        info.add(Box.createVerticalStrut(20));
        info.add(botoes);
        info.add(Box.createVerticalGlue());
        raiz.add(info, BorderLayout.CENTER);

        setContentPane(raiz);
        pack();
        setResizable(false);
        setLocationRelativeTo(dono);
    }
}

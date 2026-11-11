package gui;

import model.CartaoDebito;
import model.Pix;
import model.Produto;
import service.ProdutoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/** Página inicial: banner promocional + grade de produtos (com filtro de busca). */
public class PainelProdutos extends JScrollPane {

    private final ProdutoService produtoService;
    private final Consumer<Produto> aoAdicionar;
    private final Consumer<Produto> aoAbrir;

    private final Componentes.Pagina pagina = new Componentes.Pagina();
    private final JLabel titulo = new JLabel();
    private final Componentes.Grade grade = new Componentes.Grade(230, 345, 16);
    private final Componentes.Painel vazio = new Componentes.Painel(new BorderLayout(), 10);
    private String filtro = "";

    public PainelProdutos(ProdutoService produtoService, Consumer<Produto> aoAdicionar, Consumer<Produto> aoAbrir) {
        this.produtoService = produtoService;
        this.aoAdicionar = aoAdicionar;
        this.aoAbrir = aoAbrir;

        setBorder(null);
        getViewport().setBackground(Tema.FUNDO);
        getVerticalScrollBar().setUnitIncrement(18);
        setViewportView(pagina);

        int pix = Tema.percentualDesconto(new Pix("-"));
        int debito = Tema.percentualDesconto(new CartaoDebito("", ""));
        pagina.add(new Componentes.Banner("Pague com Pix e ganhe " + pix + "% de desconto",
                "Débito: " + debito + "% de desconto  ·  Crédito: valor integral"));

        titulo.setFont(Tema.fonte(Font.BOLD, 20f));
        titulo.setForeground(Tema.TEXTO);
        titulo.setBorder(new EmptyBorder(6, 2, 0, 0));
        pagina.add(titulo);
        pagina.add(grade);

        JLabel msg = new JLabel("Nenhum produto encontrado", SwingConstants.CENTER);
        msg.setFont(Tema.fonte(Font.BOLD, 18f));
        msg.setForeground(Tema.TEXTO);
        JLabel dica = new JLabel("Revise a ortografia ou tente uma busca mais genérica.", SwingConstants.CENTER);
        dica.setFont(Tema.fonte(Font.PLAIN, 14f));
        dica.setForeground(Tema.TEXTO_SUAVE);
        vazio.setBorder(new EmptyBorder(40, 16, 40, 16));
        vazio.add(msg, BorderLayout.CENTER);
        vazio.add(dica, BorderLayout.SOUTH);
        pagina.add(vazio);

        atualizar();
    }

    public void filtrar(String texto) {
        this.filtro = texto == null ? "" : texto.trim();
        atualizar();
        SwingUtilities.invokeLater(() -> getVerticalScrollBar().setValue(0));
    }

    /** Reconstrói os cards a partir do estado atual do serviço (estoque, preços, produtos novos). */
    public void atualizar() {
        grade.removeAll();
        String busca = Tema.normalizar(filtro);
        int total = 0;
        for (Produto p : produtoService.listar()) {
            if (busca.isEmpty() || Tema.normalizar(p.getNome()).contains(busca)) {
                grade.add(new CartaoProduto(p, aoAdicionar, aoAbrir));
                total++;
            }
        }
        if (busca.isEmpty()) {
            titulo.setText("Todos os produtos");
        } else {
            titulo.setText(total + (total == 1 ? " resultado para “" : " resultados para “") + filtro + "”");
        }
        vazio.setVisible(total == 0);
        titulo.setVisible(true);
        pagina.revalidate();
        pagina.repaint();
    }
}

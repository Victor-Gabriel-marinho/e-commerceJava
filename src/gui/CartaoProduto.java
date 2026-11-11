package gui;

import model.Pix;
import model.Produto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Card de produto da vitrine (imagem, preço, preço no Pix, estoque e botão de compra). */
public class CartaoProduto extends Componentes.Painel {

    public CartaoProduto(Produto produto, Consumer<Produto> aoAdicionar, Consumer<Produto> aoAbrir) {
        super(new BorderLayout(), 10);
        setBorder(new EmptyBorder(0, 0, 16, 0));

        int estoque = produto.getQuantidadeEmEstoque();
        boolean esgotado = estoque <= 0;

        add(new Componentes.Miniatura(produto.getNome(), 100, 160, 0), BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(12, 16, 0, 16));

        JLabel nome = Componentes.rotulo(produto.getNome(), Font.PLAIN, 15f, Tema.TEXTO);
        JLabel preco = Componentes.rotulo(Tema.moeda(produto.getPreco()), Font.PLAIN, 24f, Tema.TEXTO);
        double noPix = new Pix("-").calcularValorFinal(produto.getPreco());
        JLabel pix = Componentes.rotulo("ou " + Tema.moeda(noPix) + " no Pix", Font.BOLD, 12f, Tema.VERDE);

        JLabel status;
        if (esgotado) {
            status = Componentes.rotulo("Esgotado", Font.BOLD, 12f, Tema.VERMELHO);
        } else if (estoque <= 5) {
            status = Componentes.rotulo("Últimas " + estoque + " unidades!", Font.BOLD, 12f, Tema.LARANJA);
        } else {
            status = Componentes.rotulo(estoque + " disponíveis", Font.PLAIN, 12f, Tema.TEXTO_SUAVE);
        }

        info.add(nome);
        info.add(Box.createVerticalStrut(6));
        info.add(preco);
        info.add(Box.createVerticalStrut(2));
        info.add(pix);
        info.add(Box.createVerticalStrut(6));
        info.add(status);
        add(info, BorderLayout.CENTER);

        Componentes.Botao botao = new Componentes.Botao(esgotado ? "Indisponível" : "Adicionar ao carrinho",
                Componentes.Botao.Estilo.SECUNDARIO);
        botao.setEnabled(!esgotado);
        botao.addActionListener(e -> aoAdicionar.accept(produto));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setBorder(new EmptyBorder(0, 16, 0, 16));
        rodape.add(botao);
        add(rodape, BorderLayout.SOUTH);

        // Hover + clique no card (fora do botão) abre os detalhes
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setDestaque(true); }
            @Override public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), CartaoProduto.this);
                setDestaque(CartaoProduto.this.contains(p));
            }
            @Override public void mouseClicked(MouseEvent e) { aoAbrir.accept(produto); }
        };
        registrar(this, mouse, botao);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void registrar(Component c, MouseAdapter mouse, Component ignorado) {
        if (c == ignorado) return;
        c.addMouseListener(mouse);
        if (c instanceof Container) {
            for (Component filho : ((Container) c).getComponents()) registrar(filho, mouse, ignorado);
        }
    }
}

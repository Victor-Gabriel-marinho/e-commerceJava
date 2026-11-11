package gui;

import model.Compra;
import model.ItemCompra;
import service.CompraService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** "Minhas compras": histórico do comprador, da mais recente para a mais antiga. */
public class PainelHistorico extends JScrollPane {

    private final CompraService compraService;
    private final Consumer<Compra> aoVerDetalhes;
    private final Componentes.Pagina pagina = new Componentes.Pagina();

    public PainelHistorico(CompraService compraService, Consumer<Compra> aoVerDetalhes) {
        this.compraService = compraService;
        this.aoVerDetalhes = aoVerDetalhes;
        setBorder(null);
        getViewport().setBackground(Tema.FUNDO);
        getVerticalScrollBar().setUnitIncrement(18);
        setViewportView(pagina);
    }

    public void atualizar(String comprador) {
        pagina.removeAll();
        List<Compra> historico = new ArrayList<>(compraService.historicoDoComprador(comprador));
        Collections.reverse(historico);

        JPanel cab = new JPanel();
        cab.setOpaque(false);
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        cab.add(Componentes.rotulo("Minhas compras", Font.BOLD, 22f, Tema.TEXTO));
        cab.add(Componentes.rotulo(historico.isEmpty()
                ? "Olá, " + comprador
                : "Olá, " + comprador + " · " + historico.size() + (historico.size() == 1 ? " compra" : " compras")
                  + " · total gasto " + Tema.moeda(compraService.totalGasto(historico)),
                Font.PLAIN, 14f, Tema.TEXTO_SUAVE));
        pagina.add(cab);

        if (historico.isEmpty()) {
            Componentes.Painel vazio = new Componentes.Painel(new BorderLayout(), 10);
            vazio.setBorder(new EmptyBorder(40, 16, 40, 16));
            JLabel t = new JLabel("Você ainda não fez nenhuma compra", SwingConstants.CENTER);
            t.setFont(Tema.fonte(Font.BOLD, 18f));
            t.setForeground(Tema.TEXTO);
            vazio.add(t, BorderLayout.CENTER);
            pagina.add(vazio);
        } else {
            for (Compra c : historico) pagina.add(criarCard(c));
        }
        pagina.revalidate();
        pagina.repaint();
        SwingUtilities.invokeLater(() -> getVerticalScrollBar().setValue(0));
    }

    private JComponent criarCard(Compra c) {
        Componentes.Painel card = new Componentes.Painel(new BorderLayout(16, 0), 10);
        card.setBorder(new EmptyBorder(16, 20, 18, 20));

        StringBuilder resumo = new StringBuilder();
        for (ItemCompra i : c.getItens()) {
            if (resumo.length() > 0) resumo.append(", ");
            resumo.append(i.getQuantidade()).append("× ").append(i.getNome());
        }

        JPanel esquerda = new JPanel();
        esquerda.setOpaque(false);
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.add(Componentes.rotulo(c.getDataHoraFormatada(), Font.BOLD, 15f, Tema.TEXTO));
        esquerda.add(Box.createVerticalStrut(4));
        esquerda.add(Componentes.rotulo(resumo.toString(), Font.PLAIN, 14f, Tema.TEXTO));
        esquerda.add(Box.createVerticalStrut(2));
        esquerda.add(Componentes.rotulo("Pago com " + c.getFormaPagamento(), Font.PLAIN, 12f, Tema.TEXTO_SUAVE));
        card.add(esquerda, BorderLayout.CENTER);

        JPanel direita = new JPanel();
        direita.setOpaque(false);
        direita.setLayout(new BoxLayout(direita, BoxLayout.Y_AXIS));
        JLabel valor = Componentes.rotulo(Tema.moeda(c.getValorFinal()), Font.PLAIN, 20f, Tema.TEXTO);
        valor.setAlignmentX(Component.RIGHT_ALIGNMENT);
        Componentes.Botao detalhes = new Componentes.Botao("Ver detalhes", Componentes.Botao.Estilo.SECUNDARIO).compacto();
        detalhes.setAlignmentX(Component.RIGHT_ALIGNMENT);
        detalhes.addActionListener(e -> aoVerDetalhes.accept(c));
        direita.add(valor);
        direita.add(Box.createVerticalStrut(8));
        direita.add(detalhes);
        card.add(direita, BorderLayout.EAST);
        return card;
    }
}

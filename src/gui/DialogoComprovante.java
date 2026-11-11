package gui;

import model.Compra;
import model.ItemCompra;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Comprovante de uma compra (usado após o pagamento e no histórico). */
public final class DialogoComprovante {

    private DialogoComprovante() {}

    public static void exibir(Window dono, Compra c, boolean recemConcluida) {
        JDialog d = new JDialog(dono, recemConcluida ? "Compra realizada" : "Detalhes da compra",
                Dialog.ModalityType.APPLICATION_MODAL);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBackground(Color.WHITE);
        raiz.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Cabeçalho
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topo.setOpaque(false);
        if (recemConcluida) topo.add(new JLabel(Icones.check(40, Tema.VERDE)));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Componentes.rotulo(recemConcluida ? "Pagamento aprovado!" : "Compra de " + c.getDataHoraFormatada(),
                Font.BOLD, 20f, Tema.TEXTO));
        textos.add(Componentes.rotulo("Pedido " + c.getId(), Font.PLAIN, 12f, Tema.TEXTO_SUAVE));
        topo.add(textos);
        raiz.add(topo, BorderLayout.NORTH);

        // Itens
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Produto", "Qtd", "Unitário", "Total"}, 0) {
            @Override public boolean isCellEditable(int l, int col) { return false; }
        };
        for (ItemCompra i : c.getItens()) {
            modelo.addRow(new Object[]{i.getNome(), i.getQuantidade(),
                    Tema.moeda(i.getPrecoUnitario()), Tema.moeda(i.getSubtotal())});
        }
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(30);
        tabela.setFont(Tema.fonte(Font.PLAIN, 14f));
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(Tema.BORDA);
        tabela.setFocusable(false);
        tabela.setRowSelectionAllowed(false);
        tabela.getTableHeader().setFont(Tema.fonte(Font.BOLD, 13f));
        tabela.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int col = 1; col < 4; col++) tabela.getColumnModel().getColumn(col).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(240);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(50);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        rolagem.setPreferredSize(new Dimension(520, Math.min(220, 34 + 30 * Math.max(1, c.getItens().size()))));
        raiz.add(rolagem, BorderLayout.CENTER);

        // Totais
        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));
        rodape.add(linha("Forma de pagamento", c.getFormaPagamento(), Font.PLAIN, 14f, Tema.TEXTO));
        rodape.add(linha("Subtotal", Tema.moeda(c.getSubtotal()), Font.PLAIN, 14f, Tema.TEXTO));
        rodape.add(linha("Desconto", "− " + Tema.moeda(c.getDesconto()), Font.PLAIN, 14f, Tema.VERDE));
        rodape.add(Box.createVerticalStrut(6));
        rodape.add(linha("Total pago", Tema.moeda(c.getValorFinal()), Font.BOLD, 20f, Tema.TEXTO));
        rodape.add(Box.createVerticalStrut(16));
        Componentes.Botao ok = new Componentes.Botao(recemConcluida ? "Voltar à loja" : "Fechar",
                Componentes.Botao.Estilo.PRIMARIO);
        ok.addActionListener(e -> d.dispose());
        JPanel botao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botao.setOpaque(false);
        botao.add(ok);
        rodape.add(botao);
        raiz.add(rodape, BorderLayout.SOUTH);

        d.setContentPane(raiz);
        d.pack();
        d.setLocationRelativeTo(dono);
        d.setVisible(true);
    }

    private static JComponent linha(String rotulo, String valor, int estilo, float tamanho, Color cor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel a = new JLabel(rotulo), b = new JLabel(valor);
        a.setFont(Tema.fonte(estilo, tamanho));
        b.setFont(Tema.fonte(estilo, tamanho));
        a.setForeground(cor);
        b.setForeground(cor);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 4));
        return p;
    }
}

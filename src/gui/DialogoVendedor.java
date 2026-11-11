package gui;

import exception.DadosInvalidosException;
import exception.PersistenciaException;
import exception.ProdutoInexistenteException;
import model.Produto;
import service.ProdutoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/** Área do vendedor/administrador: cadastrar, editar e remover produtos (equivale ao MenuAdministrador). */
public class DialogoVendedor extends JDialog {

    private final ProdutoService servico;
    private final ModeloProdutos modelo = new ModeloProdutos();
    private final JTable tabela = new JTable(modelo);

    public DialogoVendedor(Window dono, ProdutoService servico) {
        super(dono, "Área do vendedor", ModalityType.APPLICATION_MODAL);
        this.servico = servico;

        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(Tema.FUNDO);
        raiz.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.add(Componentes.rotulo("Seus produtos", Font.BOLD, 20f, Tema.TEXTO));
        topo.add(Componentes.rotulo("Cadastre, edite ou remova produtos da loja.", Font.PLAIN, 13f, Tema.TEXTO_SUAVE));
        raiz.add(topo, BorderLayout.NORTH);

        tabela.setRowHeight(32);
        tabela.setFont(Tema.fonte(Font.PLAIN, 14f));
        tabela.getTableHeader().setFont(Tema.fonte(Font.BOLD, 13f));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(Tema.BORDA);
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(SwingConstants.RIGHT);
        tabela.getColumnModel().getColumn(1).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(2).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(300);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        rolagem.setPreferredSize(new Dimension(640, 320));
        raiz.add(rolagem, BorderLayout.CENTER);

        Componentes.Botao novo = new Componentes.Botao("Novo produto", Componentes.Botao.Estilo.PRIMARIO).compacto();
        Componentes.Botao editar = new Componentes.Botao("Editar", Componentes.Botao.Estilo.SECUNDARIO).compacto();
        Componentes.Botao remover = new Componentes.Botao("Remover", Componentes.Botao.Estilo.SECUNDARIO).compacto();
        Componentes.Botao fechar = new Componentes.Botao("Fechar", Componentes.Botao.Estilo.LINK);
        novo.addActionListener(e -> formulario(null));
        editar.addActionListener(e -> { Produto p = selecionado(); if (p != null) formulario(p); });
        remover.addActionListener(e -> remover());
        fechar.addActionListener(e -> dispose());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(novo);
        botoes.add(editar);
        botoes.add(remover);
        JPanel sul = new JPanel(new BorderLayout());
        sul.setOpaque(false);
        sul.add(botoes, BorderLayout.WEST);
        sul.add(fechar, BorderLayout.EAST);
        raiz.add(sul, BorderLayout.SOUTH);

        setContentPane(raiz);
        pack();
        setLocationRelativeTo(dono);
    }

    private Produto selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela.", "Atenção", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return modelo.get(linha);
    }

    private void formulario(Produto existente) {
        JTextField nome = new JTextField(existente == null ? "" : existente.getNome(), 20);
        JTextField preco = new JTextField(existente == null ? "" : String.format("%.2f", existente.getPreco()).replace('.', ','));
        JTextField estoque = new JTextField(existente == null ? "" : String.valueOf(existente.getQuantidadeEmEstoque()));
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Nome"));
        p.add(nome);
        p.add(new JLabel("Preço (R$)"));
        p.add(preco);
        p.add(new JLabel("Quantidade em estoque"));
        p.add(estoque);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, p, existente == null ? "Novo produto" : "Editar produto",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            try {
                double valor = Double.parseDouble(preco.getText().trim().replace(',', '.'));
                int qtd = Integer.parseInt(estoque.getText().trim());
                if (existente == null) servico.cadastrar(nome.getText(), valor, qtd);
                else servico.editar(existente.getId(), nome.getText(), valor, qtd);
                persistir();
                return;
            } catch (NumberFormatException e) {
                erro("Preço e estoque precisam ser números válidos (estoque inteiro).");
            } catch (DadosInvalidosException | ProdutoInexistenteException e) {
                erro(e.getMessage());
            }
        }
    }

    private void remover() {
        Produto p = selecionado();
        if (p == null) return;
        int r = JOptionPane.showConfirmDialog(this, "Remover “" + p.getNome() + "” da loja?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            servico.remover(p.getId());
            persistir();
        } catch (ProdutoInexistenteException e) {
            erro(e.getMessage());
        }
    }

    private void persistir() {
        try {
            servico.salvar();
        } catch (PersistenciaException e) {
            erro("Erro ao salvar os produtos: " + e.getMessage());
        }
        modelo.recarregar();
    }

    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private class ModeloProdutos extends AbstractTableModel {
        private final String[] colunas = {"Produto", "Preço", "Estoque"};
        private List<Produto> dados = servico.listar();

        void recarregar() { dados = servico.listar(); fireTableDataChanged(); }
        Produto get(int linha) { return dados.get(linha); }

        @Override public int getRowCount() { return dados.size(); }
        @Override public int getColumnCount() { return colunas.length; }
        @Override public String getColumnName(int c) { return colunas[c]; }
        @Override public Object getValueAt(int l, int c) {
            Produto p = dados.get(l);
            switch (c) {
                case 0:  return p.getNome();
                case 1:  return Tema.moeda(p.getPreco());
                default: return p.getQuantidadeEmEstoque();
            }
        }
    }
}

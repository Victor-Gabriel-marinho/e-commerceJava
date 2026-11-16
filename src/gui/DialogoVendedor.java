package gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import exception.DadosInvalidosException;
import exception.PersistenciaException;
import exception.ProdutoInexistenteException;
import model.Produto;
import repository.ImagemRepository;
import service.ProdutoService;

/** Área do vendedor/administrador: cadastrar (com imagem), editar e remover produtos. */
public class DialogoVendedor extends JDialog {

    private static File ultimaPasta;   // o seletor de arquivos lembra a última pasta usada

    private final ProdutoService servico;
    private final ImagemRepository imagens = new ImagemRepository();
    private final ModeloProdutos modelo = new ModeloProdutos();
    private final JTable tabela = new JTable(modelo);

    public DialogoVendedor(Window dono, ProdutoService servico) {
        super(dono, "Área do vendedor", ModalityType.APPLICATION_MODAL);
        this.servico = servico;
        modelo.recarregar();

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
        tabela.getColumnModel().getColumn(0).setPreferredWidth(280);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        rolagem.setPreferredSize(new Dimension(700, 320));
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

    // ----------------------------------------------------------- formulário
    private void formulario(Produto existente) {
        JTextField nome = new JTextField(existente == null ? "" : existente.getNome(), 22);
        JTextField preco = new JTextField(existente == null ? "" : String.format("%.2f", existente.getPreco()).replace('.', ','));
        JTextField estoque = new JTextField(existente == null ? "" : String.valueOf(existente.getQuantidadeEmEstoque()));
        SeletorImagem seletor = new SeletorImagem(existente == null ? null : existente.getImagem());

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 0, 4, 12);
        String[] rotulos = {"Nome", "Preço (R$)", "Estoque", "Imagem"};
        JComponent[] campos = {nome, preco, estoque, seletor};
        for (int i = 0; i < rotulos.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            JLabel l = new JLabel(rotulos[i]);
            l.setBorder(new EmptyBorder(i == 3 ? 6 : 0, 0, 0, 0));
            p.add(l, c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.insets = new Insets(4, 0, 4, 0);
            p.add(campos[i], c);
            c.insets = new Insets(4, 0, 4, 12);
        }

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, p, existente == null ? "Novo produto" : "Editar produto",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;

            Produto criado = null;
            try {
                double valor = Double.parseDouble(preco.getText().trim().replace(',', '.'));
                int qtd = Integer.parseInt(estoque.getText().trim());
                if (existente == null) {
                    criado = servico.cadastrar(nome.getText(), valor, qtd);
                    aplicarImagem(criado, seletor);
                } else {
                    servico.editar(existente.getId(), nome.getText(), valor, qtd);
                    aplicarImagem(existente, seletor);
                }
                persistir();
                return;
            } catch (NumberFormatException e) {
                erro("Preço e estoque precisam ser números válidos (estoque inteiro).");
            } catch (DadosInvalidosException | ProdutoInexistenteException e) {
                desfazerCadastro(criado);
                erro(e.getMessage());
            } catch (PersistenciaException e) {
                desfazerCadastro(criado);
                erro("Erro ao salvar a imagem: " + e.getMessage());
            }
        }
    }

    /** Copia a nova imagem para o projeto (ou apaga a atual, se o vendedor pediu) e liga ao produto. */
    private void aplicarImagem(Produto produto, SeletorImagem seletor) {
        if (seletor.novoArquivo() != null) {
            String caminho = imagens.salvar(seletor.novoArquivo(), produto.getId());
            servico.definirImagem(produto.getId(), caminho);
        } else if (seletor.removerAtual()) {
            imagens.remover(produto.getImagem());
            servico.definirImagem(produto.getId(), null);
        }
    }

    /** Se o cadastro falhou na imagem, o produto não pode ficar pela metade (senão duplicaria ao tentar de novo). */
    private void desfazerCadastro(Produto criado) {
        if (criado == null) return;
        try {
            servico.remover(criado.getId());
            imagens.remover(criado.getImagem());
        } catch (RuntimeException ignorada) {
            // nada a desfazer
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
            try {
                imagens.remover(p.getImagem());
            } catch (PersistenciaException ignorada) {
                // um arquivo de imagem sobrando não impede a remoção do produto
            }
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

    // ------------------------------------------------------ seletor de imagem
    /** Linha do formulário: prévia + "Escolher imagem…" + "Remover imagem". */
    private class SeletorImagem extends JPanel {
        private static final int LADO = 96;

        private final String caminhoAtual;
        private File novoArquivo;
        private boolean removerAtual;
        private Image previa;
        private final JLabel info = new JLabel();
        private final Componentes.Botao btnRemover = new Componentes.Botao("Remover imagem", Componentes.Botao.Estilo.LINK);

        SeletorImagem(String caminhoAtual) {
            super(new BorderLayout(14, 0));
            this.caminhoAtual = caminhoAtual;
            setOpaque(false);

            if (caminhoAtual != null && new File(caminhoAtual).isFile()) {
                try {
                    previa = ajustar(imagens.ler(new File(caminhoAtual)));
                } catch (RuntimeException ignorada) {
                    previa = null;
                }
            }

            JPanel quadro = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Tema.antialias(g2);
                    g2.setColor(new Color(0xF4F4F4));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.setColor(new Color(0xD0D0D0));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    if (previa != null) {
                        g2.drawImage(previa, (getWidth() - previa.getWidth(null)) / 2,
                                (getHeight() - previa.getHeight(null)) / 2, null);
                    } else {
                        g2.setColor(new Color(0x999999));
                        g2.setFont(Tema.fonte(Font.PLAIN, 12f));
                        FontMetrics fm = g2.getFontMetrics();
                        String t = "Sem imagem";
                        g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, getHeight() / 2 + fm.getAscent() / 2);
                    }
                    g2.dispose();
                }
            };
            quadro.setOpaque(false);
            quadro.setPreferredSize(new Dimension(LADO, LADO));
            add(quadro, BorderLayout.WEST);

            Componentes.Botao escolher = new Componentes.Botao("Escolher imagem…", Componentes.Botao.Estilo.SECUNDARIO).compacto();
            escolher.addActionListener(e -> escolher());
            btnRemover.setFont(Tema.fonte(Font.PLAIN, 13f));
            btnRemover.addActionListener(e -> remover());
            info.setFont(Tema.fonte(Font.PLAIN, 12f));
            info.setForeground(Tema.TEXTO_SUAVE);

            JPanel lado = new JPanel();
            lado.setOpaque(false);
            lado.setLayout(new BoxLayout(lado, BoxLayout.Y_AXIS));
            Componentes.alinharEsquerda(escolher, btnRemover, info);
            lado.add(escolher);
            lado.add(Box.createVerticalStrut(4));
            lado.add(btnRemover);
            lado.add(Box.createVerticalStrut(6));
            lado.add(info);
            add(lado, BorderLayout.CENTER);
            atualizarTextos();
        }

        File novoArquivo() { return novoArquivo; }
        boolean removerAtual() { return removerAtual; }

        private void escolher() {
            JFileChooser chooser = new JFileChooser(ultimaPasta);
            chooser.setDialogTitle("Escolha a imagem do produto");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileNameExtensionFilter("Imagens (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));
            if (chooser.showOpenDialog(DialogoVendedor.this) != JFileChooser.APPROVE_OPTION) return;

            File arquivo = chooser.getSelectedFile();
            ultimaPasta = arquivo.getParentFile();
            try {
                BufferedImage lida = imagens.ler(arquivo);   // valida já na escolha
                previa = ajustar(lida);
                novoArquivo = arquivo;
                removerAtual = false;
                atualizarTextos();
            } catch (DadosInvalidosException | PersistenciaException e) {
                erro(e.getMessage());
            }
        }

        private void remover() {
            novoArquivo = null;
            previa = null;
            removerAtual = caminhoAtual != null;
            atualizarTextos();
        }

        private void atualizarTextos() {
            if (novoArquivo != null) {
                info.setText("<html>" + escapar(novoArquivo.getName()) + "</html>");
            } else if (previa != null) {
                info.setText("Imagem atual do produto");
            } else {
                info.setText("<html>Sem imagem: será usada uma<br>ilustração genérica.</html>");
            }
            btnRemover.setEnabled(previa != null);
            repaint();
        }

        private Image ajustar(BufferedImage img) {
            double escala = Math.min((LADO - 8.0) / img.getWidth(), (LADO - 8.0) / img.getHeight());
            int w = Math.max(1, (int) Math.round(img.getWidth() * escala));
            int h = Math.max(1, (int) Math.round(img.getHeight() * escala));
            return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        }

        private String escapar(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    // ------------------------------------------------------------- tabela
    private class ModeloProdutos extends AbstractTableModel {
        private final String[] colunas = {"Produto", "Preço", "Estoque", "Imagem"};
        private List<Produto> dados = new java.util.ArrayList<>();   // preenchido por recarregar() no construtor

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
                case 2:  return p.getQuantidadeEmEstoque();
                default: return p.getImagem() == null ? "Ilustração" : "Enviada";
            }
        }
    }
}

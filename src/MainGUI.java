import java.awt.Font;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import exception.PersistenciaException;
import gui.TelaLoja;
import repository.CompraRepository;
import repository.ProdutoRepository;
import service.CompraService;
import service.ProdutoService;

/** Ponto de entrada da interface gráfica (o Main.java continua sendo a versão de console). */
public class MainGUI {

    public static void main(String[] args) {
        System.setProperty("swing.boldMetal", "false");
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            try {
                ProdutoService produtoService = new ProdutoService(new ProdutoRepository());
                CompraService compraService = new CompraService(new CompraRepository(), produtoService);
                new TelaLoja(produtoService, compraService).setVisible(true);
            } catch (PersistenciaException e) {
                JOptionPane.showMessageDialog(null, "Não foi possível carregar os dados:\n" + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static void configurarAparencia() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // mantém o visual padrão
        }
        Enumeration<Object> chaves = UIManager.getDefaults().keys();
        while (chaves.hasMoreElements()) {
            Object chave = chaves.nextElement();
            if (UIManager.get(chave) instanceof FontUIResource) {
                UIManager.put(chave, new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13));
            }
        }
    }
}
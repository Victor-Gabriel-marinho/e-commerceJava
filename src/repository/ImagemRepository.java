package repository;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import exception.DadosInvalidosException;
import exception.PersistenciaException;

/**
 * Guarda as imagens dos produtos em src/dados/imagens (ao lado do Produtos.json).
 * A imagem escolhida é validada, reduzida (máx. 800 px) e COPIADA para o projeto como JPG,
 * então o produto não depende do local original do arquivo.
 */
public class ImagemRepository {

    private static final String PASTA = "src/dados/imagens";
    private static final int LADO_MAXIMO = 800;

    /** Lê o arquivo e garante que é uma imagem (JPG, PNG, GIF ou BMP). */
    public BufferedImage ler(File origem) {
        try {
            BufferedImage imagem = ImageIO.read(origem);
            if (imagem == null) {
                throw new DadosInvalidosException("O arquivo escolhido não é uma imagem válida (use JPG, PNG, GIF ou BMP).");
            }
            return imagem;
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao ler a imagem: " + e.getMessage(), e);
        }
    }

    /**
     * Copia a imagem para o projeto e devolve o caminho relativo a ser guardado no produto.
     * O nome do arquivo é o id do produto, então trocar a imagem sobrescreve a anterior.
     */
    public String salvar(File origem, String idProduto) {
        BufferedImage pronta = prepararParaSalvar(ler(origem));
        File destino = new File(PASTA, idProduto + ".jpg");
        try {
            Files.createDirectories(destino.getParentFile().toPath());
            if (!ImageIO.write(pronta, "jpg", destino)) {
                throw new PersistenciaException("Não foi possível gravar a imagem.", null);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao salvar a imagem: " + e.getMessage(), e);
        }
        return PASTA + "/" + destino.getName();
    }

    /** Apaga a imagem de um produto (só mexe em arquivos dentro da pasta de imagens). */
    public void remover(String caminho) {
        if (caminho == null) return;
        File arquivo = new File(caminho);
        File pasta = new File(PASTA);
        try {
            if (arquivo.getCanonicalPath().startsWith(pasta.getCanonicalPath() + File.separator)) {
                Files.deleteIfExists(arquivo.toPath());
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao remover a imagem: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------ auxiliares
    /** Fundo branco (JPG não tem transparência) e redução em etapas para manter a qualidade. */
    private BufferedImage prepararParaSalvar(BufferedImage origem) {
        BufferedImage atual = paraRgb(origem);
        int maior = Math.max(atual.getWidth(), atual.getHeight());
        if (maior <= LADO_MAXIMO) return atual;

        double fator = (double) LADO_MAXIMO / maior;
        int alvoL = Math.max(1, (int) Math.round(atual.getWidth() * fator));
        int alvoA = Math.max(1, (int) Math.round(atual.getHeight() * fator));
        while (atual.getWidth() / 2 > alvoL && atual.getHeight() / 2 > alvoA) {
            atual = redimensionar(atual, atual.getWidth() / 2, atual.getHeight() / 2);
        }
        return redimensionar(atual, alvoL, alvoA);
    }

    private BufferedImage paraRgb(BufferedImage origem) {
        BufferedImage rgb = new BufferedImage(origem.getWidth(), origem.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(origem, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private BufferedImage redimensionar(BufferedImage origem, int largura, int altura) {
        BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origem, 0, 0, largura, altura, null);
        g.dispose();
        return destino;
    }
}

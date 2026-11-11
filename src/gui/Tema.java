package gui;

import model.FormaPagamento;

import java.awt.*;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Locale;

/** Cores, fontes e formatações compartilhadas por toda a interface (visual inspirado no Mercado Livre). */
public final class Tema {

    private Tema() {}

    public static final Color AMARELO     = new Color(0xFFE600);
    public static final Color AZUL        = new Color(0x3483FA);
    public static final Color AZUL_HOVER  = new Color(0x2968C8);
    public static final Color AZUL_CLARO  = new Color(0xE3EDFB);
    public static final Color FUNDO       = new Color(0xEBEBEB);
    public static final Color BORDA       = new Color(0xE6E6E6);
    public static final Color TEXTO       = new Color(0x333333);
    public static final Color TEXTO_SUAVE = new Color(0x777777);
    public static final Color VERDE       = new Color(0x00A650);
    public static final Color LARANJA     = new Color(0xE8590C);
    public static final Color VERMELHO    = new Color(0xF23D4F);
    public static final Color LOGO        = new Color(0x2D3277);

    private static final Color[] PALETA = {
            new Color(0x3483FA), new Color(0x00A650), new Color(0xFF7733),
            new Color(0x8E44AD), new Color(0xE91E63), new Color(0x16A085)
    };

    private static final NumberFormat MOEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    public static Font fonte(int estilo, float tamanho) {
        return new Font(Font.SANS_SERIF, estilo, 12).deriveFont(estilo, tamanho);
    }

    public static String moeda(double valor) {
        return MOEDA.format(valor).replace('\u00A0', ' ');
    }

    /** Percentual de desconto de uma forma de pagamento, obtido da própria regra de negócio. */
    public static int percentualDesconto(FormaPagamento forma) {
        return (int) Math.round(forma.calcularDesconto(100.0));
    }

    /** Minúsculas e sem acento, para busca tolerante ("cafe" encontra "Café"). */
    public static String normalizar(String texto) {
        String n = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    /** Cor estável para o produto (usada no placeholder de imagem). */
    public static Color corDoProduto(String nome) {
        return PALETA[Math.abs(normalizar(nome).hashCode() % PALETA.length)];
    }

    public static void antialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}

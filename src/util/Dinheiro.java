package util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Arredondamentos monetários (2 casas, HALF_UP) usando BigDecimal. */
public final class Dinheiro {

    private Dinheiro() {}

    public static double arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Calcula {@code percentual}% de {@code valor}, arredondado em 2 casas. */
    public static double percentual(double valor, int percentual) {
        return BigDecimal.valueOf(valor)
                .multiply(BigDecimal.valueOf(percentual))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
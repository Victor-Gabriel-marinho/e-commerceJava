package model;

import exception.PagamentoRecusadoException;
import util.Dinheiro;

/** Estratégia de pagamento: cada subclasse define seu desconto e como processar. */
public abstract class FormaPagamento {

    public abstract String getNome();

    /** Desconto (em R$) que esta forma de pagamento aplica sobre o subtotal. */
    public abstract double calcularDesconto(double subtotal);

    /** Simula o processamento; lança PagamentoRecusadoException se os dados forem inválidos. */
    public abstract void processar(double valorFinal);

    public double calcularValorFinal(double subtotal) {
        return Dinheiro.arredondar(subtotal - calcularDesconto(subtotal));
    }

    /** Validação simulada de cartão, compartilhada por crédito e débito. */
    protected static void exigirCartaoValido(String numero, String cvv) {
        String n = (numero == null) ? "" : numero.replaceAll("\\s", "");
        if (!n.matches("\\d{16}")) {
            throw new PagamentoRecusadoException("Número de cartão inválido (são necessários 16 dígitos).");
        }
        if (cvv == null || !cvv.trim().matches("\\d{3}")) {
            throw new PagamentoRecusadoException("CVV inválido (são necessários 3 dígitos).");
        }
    }
}
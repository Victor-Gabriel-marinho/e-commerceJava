package model;

public class CartaoCredito extends FormaPagamento {
    private final String numero;
    private final String cvv;

    public CartaoCredito(String numero, String cvv) {
        this.numero = numero;
        this.cvv = cvv;
    }

    @Override public String getNome() { return "Cartão de crédito"; }

    @Override public double calcularDesconto(double subtotal) { return 0.0; }   // valor integral

    @Override public void processar(double valorFinal) { exigirCartaoValido(numero, cvv); }
}
package model;

import util.Dinheiro;

public class CartaoDebito extends FormaPagamento {
    private final String numero;
    private final String cvv;

    public CartaoDebito(String numero, String cvv) {
        this.numero = numero;
        this.cvv = cvv;
    }

    @Override public String getNome() { return "Cartão de débito"; }

    @Override public double calcularDesconto(double subtotal) { return Dinheiro.percentual(subtotal, 10); }

    @Override public void processar(double valorFinal) { exigirCartaoValido(numero, cvv); }
}
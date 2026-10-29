package model;

import exception.PagamentoRecusadoException;
import util.Dinheiro;

public class Pix extends FormaPagamento {
    private final String chave;

    public Pix(String chave) {
        this.chave = chave;
    }

    @Override public String getNome() { return "Pix"; }

    @Override public double calcularDesconto(double subtotal) { return Dinheiro.percentual(subtotal, 5); }

    @Override
    public void processar(double valorFinal) {
        if (chave == null || chave.trim().isEmpty()) {
            throw new PagamentoRecusadoException("Chave Pix inválida.");
        }
    }
}
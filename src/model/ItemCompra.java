package model;

import util.Dinheiro;

/** Item de uma compra já realizada: guarda uma "foto" do produto no momento da compra. */
public class ItemCompra {
    private String produtoId;
    private String nome;
    private double precoUnitario;
    private int quantidade;

    public ItemCompra(String produtoId, String nome, double precoUnitario, int quantidade) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public String getProdutoId() { return produtoId; }
    public String getNome() { return nome; }
    public double getPrecoUnitario() { return precoUnitario; }
    public int getQuantidade() { return quantidade; }

    public double getSubtotal() {
        return Dinheiro.arredondar(precoUnitario * quantidade);
    }
}
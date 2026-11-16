package model;

import java.util.UUID;

public class Produto {
    private String id;
    private String nome;
    private double preco;
    private int quantidadeEmEstoque;
    private String imagem;      // opcional; ausente no JSON quando não há imagem

    public Produto (String nome, double preco, int quantidadeEmEstoque){
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = quantidadeEmEstoque;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public void setQuantidadeEmEstoque(int quantidadeEmEstoque) {
        this.quantidadeEmEstoque = quantidadeEmEstoque;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /** Caminho (relativo ao projeto) da imagem enviada pelo vendedor; null = usa a ilustração genérica. */
    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

}

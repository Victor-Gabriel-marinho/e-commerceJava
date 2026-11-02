package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import exception.DadosInvalidosException;
import exception.EstoqueInsuficienteException;
import exception.ProdutoInexistenteException;
import util.Dinheiro;

public class Carrinho {

    private final Map<String, ItemCarrinho> itens = new LinkedHashMap<>();

    public void adicionar(Produto produto, int quantidade) {
        if (produto == null) {
            throw new ProdutoInexistenteException("Produto não encontrado.");
        }
        validarQuantidadePositiva(quantidade);

        ItemCarrinho existente = itens.get(produto.getId());
        int totalDesejado = quantidade + (existente == null ? 0 : existente.getQuantidade());
        validarEstoque(produto, totalDesejado);

        if (existente == null) {
            itens.put(produto.getId(), new ItemCarrinho(produto, quantidade));
        } else {
            existente.setQuantidade(totalDesejado);
        }
    }

    public void remover(String produtoId) {
        if (itens.remove(produtoId) == null) {
            throw new ProdutoInexistenteException("Esse produto não está no carrinho.");
        }
    }

    public void alterarQuantidade(String produtoId, int novaQuantidade) {
        ItemCarrinho item = itens.get(produtoId);
        if (item == null) {
            throw new ProdutoInexistenteException("Esse produto não está no carrinho.");
        }
        validarQuantidadePositiva(novaQuantidade);
        validarEstoque(item.getProduto(), novaQuantidade);
        item.setQuantidade(novaQuantidade);
    }

    public List<ItemCarrinho> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens.values()));
    }

    public boolean estaVazio() {
        return itens.isEmpty();
    }

    public double getSubtotal() {
        double total = 0;
        for (ItemCarrinho i : itens.values()) {
            total += i.getSubtotal();
        }
        return Dinheiro.arredondar(total);
    }

    public Map<String, Integer> paraMapaDeItens() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (ItemCarrinho i : itens.values()) {
            mapa.put(i.getProduto().getId(), i.getQuantidade());
        }
        return mapa;
    }

    public void limpar() {
        itens.clear();
    }

    private void validarQuantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade deve ser maior que zero.");
        }
    }

    private void validarEstoque(Produto produto, int quantidadeDesejada) {
        if (quantidadeDesejada > produto.getQuantidadeEmEstoque()) {
            throw new EstoqueInsuficienteException("Estoque insuficiente para '" + produto.getNome()
                    + "': solicitado " + quantidadeDesejada
                    + ", disponível " + produto.getQuantidadeEmEstoque() + ".");
        }
    }
}
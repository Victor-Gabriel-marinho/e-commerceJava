package service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import exception.DadosInvalidosException;
import exception.ProdutoDuplicadoException;
import exception.ProdutoInexistenteException;
import model.Produto;
import repository.ProdutoRepository;

public class ProdutoService {

    private final ProdutoRepository repository;
    private final List<Produto> produtos;

    /** Carrega os produtos do arquivo */
    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
        this.produtos = new ArrayList<>(repository.carregarTodos());
    }

    /** Grava os produtos no arquivo. Chamar ao encerrar o programa. */
    public void salvar() {
        repository.salvarTodos(produtos);
    }

    // ---------------------------------------------------------------- CREATE
    public Produto cadastrar(String nome, double preco, int estoque) {
        validar(nome, preco, estoque);

        Produto produto = new Produto(nome.trim(), preco, estoque);
        produtos.add(produto);
        return produto;
    }

    // ------------------------------------------------------------------ READ
    /** Devolve uma cópia da lista, para quem chama não alterar a lista interna. */
    public List<Produto> listar() {
        return new ArrayList<>(produtos);
    }

    public Produto buscarPorId(String id) {
        for (Produto p : produtos) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        throw new ProdutoInexistenteException("Produto não encontrado.");
    }

    // ---------------------------------------------------------------- UPDATE
    public void editar(String id, String nome, double preco, int estoque) {
        Produto produto = buscarPorId(id);
        validar(nome, preco, estoque);

        produto.setNome(nome.trim());
        produto.setPreco(preco);
        produto.setQuantidadeEmEstoque(estoque);
    }

    // ---------------------------------------------------------------- DELETE
    public void remover(String id) {
        Produto produto = buscarPorId(id);
        produtos.remove(produto);
    }

    // --------------------------------------------------------------- AUXILIAR
    private boolean existe(String id) {
        for (Produto p : produtos) {
            if (p.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void validar(String nome, double preco, int estoque) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DadosInvalidosException("O nome do produto não pode ser vazio.");
        }
        if (Double.isNaN(preco) || Double.isInfinite(preco) || preco < 0) {
            throw new DadosInvalidosException("O preço não pode ser negativo.");
        }
        if (estoque < 0) {
            throw new DadosInvalidosException("A quantidade em estoque não pode ser negativa.");
        }
    }
}
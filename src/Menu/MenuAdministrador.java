package Menu;

import java.util.List;

import exception.DadosInvalidosException;
import exception.PersistenciaException;
import exception.ProdutoInexistenteException;
import model.Produto;
import service.ProdutoService;
import util.LeitorEntrada;

public class MenuAdministrador {

    private final ProdutoService produtoService;
    private final LeitorEntrada entrada;

    public MenuAdministrador(ProdutoService produtoService, LeitorEntrada entrada) {
        this.entrada = entrada;
        this.produtoService = produtoService;
    }

    /** Laço principal do menu: roda até o administrador escolher voltar. */
    public void exibir() {
        int opcao;
        do {
            imprimirMenu();
            opcao = entrada.lerInt("Escolha uma opção: ");
            System.out.println();

            try {
                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> listar();
                    case 3 -> consultar();
                    case 4 -> editar();
                    case 5 -> remover();
                    case 0 -> System.out.println("Voltando ao menu principal...");
                    default -> System.out.println("Opção inválida. Escolha um número do menu.");
                }
            } catch (ProdutoInexistenteException | DadosInvalidosException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (PersistenciaException e) {
                System.out.println("Erro ao acessar os arquivos: " + e.getMessage());
            }
            System.out.println();
        } while (opcao != 0);

    }

    private void imprimirMenu() {
        System.out.println("========== MENU ADMINISTRADOR ==========");
        System.out.println("1 - Cadastrar produto");
        System.out.println("2 - Listar produtos");
        System.out.println("3 - Consultar produto");
        System.out.println("4 - Editar produto");
        System.out.println("5 - Remover produto");
        System.out.println("0 - Voltar");
        System.out.println("========================================");
    }

    // ---------------------------------------------------------------- CREATE
    private void cadastrar() {
        System.out.println("--- Cadastrar produto ---");
        String nome = entrada.lerTexto("Nome: ");
        double preco = entrada.lerDouble("Preço: ");
        int estoque = entrada.lerInt("Quantidade em estoque: ");

        // O service gera o UUID e valida (preço negativo, quantidade inválida)
        produtoService.cadastrar(nome, preco, estoque);
        System.out.println("Produto cadastrado com sucesso!");
    }

    // ------------------------------------------------------------------ READ
    private void listar() {
        System.out.println("--- Produtos cadastrados ---");
        List<Produto> produtos = produtoService.listar();

        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        imprimirListaNumerada(produtos);
    }

    private void consultar() {
        System.out.println("--- Consultar produto ---");
        Produto p = selecionarProduto("consultar");
        if (p != null) {
            imprimirDetalhes(p);
        }
    }

    // ---------------------------------------------------------------- UPDATE
    private void editar() {
        System.out.println("--- Editar produto ---");
        Produto atual = selecionarProduto("editar");
        if (atual == null) {
            return;
        }

        System.out.println("\nDados atuais:");
        imprimirDetalhes(atual);
        System.out.println("(Pressione Enter para manter o valor atual)");

        String nome = entrada.lerTextoOpcional("Novo nome: ");
        Double preco = entrada.lerDoubleOpcional("Novo preço: ");
        Integer estoque = entrada.lerIntOpcional("Nova quantidade em estoque: ");

        String novoNome = nome.isEmpty() ? atual.getNome() : nome;
        double novoPreco = (preco == null) ? atual.getPreco() : preco;
        int novoEstoque = (estoque == null) ? atual.getQuantidadeEmEstoque() : estoque;

        produtoService.editar(atual.getId(), novoNome, novoPreco, novoEstoque);
        System.out.println("Produto atualizado com sucesso!");
    }

    // ---------------------------------------------------------------- DELETE
    private void remover() {
        System.out.println("--- Remover produto ---");
        Produto p = selecionarProduto("remover");
        if (p == null) {
            return;
        }

        System.out.println("\nProduto selecionado:");
        imprimirDetalhes(p);
        if (entrada.confirmar("Confirma a remoção?")) {
            produtoService.remover(p.getId());
            System.out.println("Produto removido com sucesso!");
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    // --------------------------------------------------------------- SELEÇÃO
    /**
     * Mostra a lista numerada e deixa o usuário escolher pelo número.
     * Retorna o produto escolhido, ou null se a lista estiver vazia
     * ou o usuário cancelar (0).
     */
    private Produto selecionarProduto(String acao) {
        List<Produto> produtos = produtoService.listar();

        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return null;
        }

        imprimirListaNumerada(produtos);
        System.out.println("0 - Cancelar");

        while (true) {
            int escolha = entrada.lerInt("Número do produto que deseja " + acao + ": ");
            if (escolha == 0) {
                System.out.println("Operação cancelada.");
                return null;
            }
            if (escolha >= 1 && escolha <= produtos.size()) {
                return produtos.get(escolha - 1);
            }
            System.out.println("Número fora da lista. Escolha entre 1 e " + produtos.size() + ", ou 0 para cancelar.");
        }
    }

    // --------------------------------------------------------------- AUXILIAR
    private void imprimirListaNumerada(List<Produto> produtos) {
        System.out.printf("%-4s %-25s %12s %10s%n", "Nº", "Nome", "Preço (R$)", "Estoque");
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            System.out.printf("%-4d %-25s %12.2f %10d%n",
                    i + 1, p.getNome(), p.getPreco(), p.getQuantidadeEmEstoque());
        }
    }

    private void imprimirDetalhes(Produto p) {
        System.out.println("Nome:    " + p.getNome());
        System.out.printf("Preço:   R$ %.2f%n", p.getPreco());
        System.out.println("Estoque: " + p.getQuantidadeEmEstoque());
    }
}
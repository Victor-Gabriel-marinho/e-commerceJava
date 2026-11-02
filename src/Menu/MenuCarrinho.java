package Menu;

import java.util.ArrayList;
import java.util.List;

import exception.DadosInvalidosException;
import exception.EstoqueInsuficienteException;
import exception.ProdutoInexistenteException;
import model.Carrinho;
import model.ItemCarrinho;
import model.Produto;
import service.ProdutoService;
import util.LeitorEntrada;

public class MenuCarrinho {

    private final ProdutoService produtoService;
    private final MenuCompra menuCompra;
    private final LeitorEntrada entrada;
    private final String comprador;
    private final Carrinho carrinho = new Carrinho();

    public MenuCarrinho(ProdutoService produtoService, MenuCompra menuCompra,
                        LeitorEntrada entrada, String comprador) {
        this.produtoService = produtoService;
        this.menuCompra = menuCompra;
        this.entrada = entrada;
        this.comprador = comprador;
    }

    public void exibir() {
        int opcao;
        do {
            imprimirMenu();
            opcao = entrada.lerInt("Escolha uma opção: ");
            System.out.println();

            try {
                switch (opcao) {
                    case 1 -> listarProdutosDisponiveis();
                    case 2 -> adicionarAoCarrinho();
                    case 3 -> removerDoCarrinho();
                    case 4 -> alterarQuantidade();
                    case 5 -> visualizarCarrinho();
                    case 6 -> finalizar();
                    case 7 -> menuCompra.exibirHistorico(comprador);
                    case 0 -> System.out.println("Saindo...");
                    default -> System.out.println("Opção inválida. Escolha um número do menu.");
                }
            } catch (ProdutoInexistenteException | DadosInvalidosException | EstoqueInsuficienteException e) {
                System.out.println("Erro: " + e.getMessage());
            }
            System.out.println();
        } while (opcao != 0);
    }

    private void imprimirMenu() {
        System.out.println("=========== MENU COMPRADOR ===========");
        System.out.println("Comprador: " + comprador);
        System.out.println("1 - Ver produtos disponíveis");
        System.out.println("2 - Adicionar produto ao carrinho");
        System.out.println("3 - Remover produto do carrinho");
        System.out.println("4 - Alterar quantidade de um produto");
        System.out.println("5 - Ver carrinho e subtotal");
        System.out.println("6 - Finalizar compra");
        System.out.println("7 - Histórico de compras");
        System.out.println("0 - Sair");
        System.out.println("======================================");
    }

    private void listarProdutosDisponiveis() {
        System.out.println("--- Produtos disponíveis ---");
        List<Produto> disponiveis = produtosDisponiveis();
        if (disponiveis.isEmpty()) {
            System.out.println("Nenhum produto disponível no momento.");
            return;
        }
        imprimirProdutos(disponiveis);
    }

    private void adicionarAoCarrinho() {
        System.out.println("--- Adicionar ao carrinho ---");
        List<Produto> disponiveis = produtosDisponiveis();
        if (disponiveis.isEmpty()) {
            System.out.println("Nenhum produto disponível no momento.");
            return;
        }
        imprimirProdutos(disponiveis);
        System.out.println("0 - Cancelar");

        int escolha = lerEscolha("Número do produto: ", disponiveis.size());
        if (escolha == 0) {
            System.out.println("Operação cancelada.");
            return;
        }
        Produto produto = disponiveis.get(escolha - 1);
        int quantidade = entrada.lerInt("Quantidade desejada: ");

        carrinho.adicionar(produto, quantidade);
        System.out.println("'" + produto.getNome() + "' adicionado ao carrinho.");
        System.out.printf("Subtotal atual: R$ %.2f%n", carrinho.getSubtotal());
    }

    private void removerDoCarrinho() {
        System.out.println("--- Remover do carrinho ---");
        ItemCarrinho item = selecionarItemDoCarrinho("remover");
        if (item == null) {
            return;
        }
        carrinho.remover(item.getProduto().getId());
        System.out.println("'" + item.getProduto().getNome() + "' removido do carrinho.");
        System.out.printf("Subtotal atual: R$ %.2f%n", carrinho.getSubtotal());
    }

    private void alterarQuantidade() {
        System.out.println("--- Alterar quantidade ---");
        ItemCarrinho item = selecionarItemDoCarrinho("alterar");
        if (item == null) {
            return;
        }
        int nova = entrada.lerInt("Nova quantidade (atual: " + item.getQuantidade() + "): ");
        carrinho.alterarQuantidade(item.getProduto().getId(), nova);
        System.out.println("Quantidade atualizada.");
        System.out.printf("Subtotal atual: R$ %.2f%n", carrinho.getSubtotal());
    }

    private void visualizarCarrinho() {
        System.out.println("--- Seu carrinho ---");
        if (carrinho.estaVazio()) {
            System.out.println("O carrinho está vazio.");
            return;
        }
        imprimirItens(carrinho.getItens());
        System.out.printf("SUBTOTAL: R$ %.2f%n", carrinho.getSubtotal());
    }

    private void finalizar() {
        if (carrinho.estaVazio()) {
            System.out.println("O carrinho está vazio. Adicione produtos antes de finalizar.");
            return;
        }
        boolean concluida = menuCompra.finalizarCompra(comprador, carrinho.paraMapaDeItens());
        if (concluida) {
            carrinho.limpar();
        }
    }

    private List<Produto> produtosDisponiveis() {
        List<Produto> disponiveis = new ArrayList<>();
        for (Produto p : produtoService.listar()) {
            if (p.getQuantidadeEmEstoque() > 0) {
                disponiveis.add(p);
            }
        }
        return disponiveis;
    }

    private ItemCarrinho selecionarItemDoCarrinho(String acao) {
        if (carrinho.estaVazio()) {
            System.out.println("O carrinho está vazio.");
            return null;
        }
        List<ItemCarrinho> itens = carrinho.getItens();
        imprimirItens(itens);
        System.out.println("0 - Cancelar");

        int escolha = lerEscolha("Número do item que deseja " + acao + ": ", itens.size());
        if (escolha == 0) {
            System.out.println("Operação cancelada.");
            return null;
        }
        return itens.get(escolha - 1);
    }

    private int lerEscolha(String mensagem, int max) {
        while (true) {
            int escolha = entrada.lerInt(mensagem);
            if (escolha >= 0 && escolha <= max) {
                return escolha;
            }
            System.out.println("Número fora da lista. Escolha entre 1 e " + max + ", ou 0 para cancelar.");
        }
    }

    private void imprimirProdutos(List<Produto> produtos) {
        System.out.printf("%-4s %-25s %12s %10s%n", "Nº", "Nome", "Preço (R$)", "Estoque");
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            System.out.printf("%-4d %-25s %12.2f %10d%n",
                    i + 1, p.getNome(), p.getPreco(), p.getQuantidadeEmEstoque());
        }
    }

    private void imprimirItens(List<ItemCarrinho> itens) {
        System.out.printf("%-4s %-25s %5s %12s %12s%n", "Nº", "Produto", "Qtd", "Unit. (R$)", "Total (R$)");
        for (int i = 0; i < itens.size(); i++) {
            ItemCarrinho it = itens.get(i);
            System.out.printf("%-4d %-25s %5d %12.2f %12.2f%n", i + 1, it.getProduto().getNome(),
                    it.getQuantidade(), it.getProduto().getPreco(), it.getSubtotal());
        }
    }
}

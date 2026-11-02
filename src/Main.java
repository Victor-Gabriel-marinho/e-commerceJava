import Menu.MenuAdministrador;
import Menu.MenuCompra;
import repository.CompraRepository;
import repository.ProdutoRepository;
import service.CompraService;
import service.ProdutoService;
import util.LeitorEntrada;
import Menu.MenuCarrinho;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static Scanner scan = new Scanner(System.in);
    private static LeitorEntrada entrada = new LeitorEntrada(scan);

    public static void main (String[] args){
        int opcao= 0;

        System.out.println("=== E-Commerce ===");
        System.out.println("DIGITE A OPÇÃO DE USUÁRIO");
        System.out.println("1 - Adimnistrador");
        System.out.println("2 - Comprador");
        System.out.println("0 - Sair");

        opcao =entrada.lerInt("DIGITE A OPÇÃO DE USUÁRIO");

        switch (opcao){
            case (1): {

                ProdutoRepository repo = new ProdutoRepository();
                ProdutoService produtoService = new ProdutoService(repo);
                MenuAdministrador menu = new MenuAdministrador(produtoService, entrada);
                menu.exibir();
                produtoService.salvar();

                break;
            }
            case (2): {
                ProdutoRepository repo = new ProdutoRepository();
                ProdutoService produtoService = new ProdutoService(repo);
                CompraService compraService = new CompraService(new CompraRepository(), produtoService);
                MenuCompra menuCompra = new MenuCompra(compraService, entrada);

                String comprador = entrada.lerTexto("Seu nome: ");

                new MenuCarrinho(produtoService, menuCompra, entrada, comprador).exibir();
                break;
            }
            case (0): {
                break;
            }
        }

    }


}
package Menu;

import java.util.List;
import java.util.Map;

import exception.DadosInvalidosException;
import exception.EstoqueInsuficienteException;
import exception.PagamentoRecusadoException;
import exception.PersistenciaException;
import exception.ProdutoInexistenteException;
import model.CartaoCredito;
import model.CartaoDebito;
import model.Compra;
import model.FormaPagamento;
import model.ItemCompra;
import model.Pix;
import service.CompraService;
import util.LeitorEntrada;

public class MenuCompra {

    private final CompraService compraService;
    private final LeitorEntrada entrada;

    public MenuCompra(CompraService compraService, LeitorEntrada entrada) {
        this.compraService = compraService;
        this.entrada = entrada;
    }

    /**
     * Fluxo de finalização. Retorna true se a compra foi concluída
     * (o carrinho só deve ser esvaziado nesse caso).
     */
    public boolean finalizarCompra(String comprador, Map<String, Integer> itens) {
        System.out.println("--- Finalizar compra ---");
        FormaPagamento forma = escolherFormaPagamento();
        if (forma == null) {
            System.out.println("Compra cancelada.");
            return false;
        }

        try {
            Compra compra = compraService.finalizar(comprador, itens, forma);
            imprimirResumo(compra);
            return true;
        } catch (DadosInvalidosException | ProdutoInexistenteException | EstoqueInsuficienteException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (PagamentoRecusadoException e) {
            System.out.println("Pagamento recusado: " + e.getMessage());
        } catch (PersistenciaException e) {
            System.out.println("Erro ao acessar os arquivos: " + e.getMessage());
        }
        System.out.println("A compra não foi realizada e o estoque não foi alterado.");
        return false;
    }

    public void exibirHistorico(String comprador) {
        System.out.println("--- Histórico de compras de " + comprador + " ---");
        List<Compra> historico = compraService.historicoDoComprador(comprador);

        if (historico.isEmpty()) {
            System.out.println("Nenhuma compra realizada.");
            return;
        }
        for (Compra c : historico) {
            System.out.println("Compra:     " + c.getId());
            System.out.println("Data/hora:  " + c.getDataHoraFormatada());
            System.out.println("Pagamento:  " + c.getFormaPagamento());
            System.out.printf("Valor pago: R$ %.2f%n", c.getValorFinal());
            System.out.println("----------------------------------------");
        }
        System.out.printf("TOTAL GASTO: R$ %.2f%n", compraService.totalGasto(historico));
    }

    // --------------------------------------------------------------- AUXILIAR
    /** Única parte que "conhece" os tipos concretos: só monta o objeto; a lógica da compra não. */
    private FormaPagamento escolherFormaPagamento() {
        System.out.println("Forma de pagamento:");
        System.out.println("1 - Cartão de crédito (valor integral)");
        System.out.println("2 - Cartão de débito (10% de desconto)");
        System.out.println("3 - Pix (5% de desconto)");
        System.out.println("0 - Cancelar");

        while (true) {
            int opcao = entrada.lerInt("Escolha: ");
            switch (opcao) {
                case 1:
                    return new CartaoCredito(entrada.lerTexto("Número do cartão (16 dígitos): "),
                            entrada.lerTexto("CVV (3 dígitos): "));
                case 2:
                    return new CartaoDebito(entrada.lerTexto("Número do cartão (16 dígitos): "),
                            entrada.lerTexto("CVV (3 dígitos): "));
                case 3:
                    return new Pix(entrada.lerTexto("Chave Pix: "));
                case 0:
                    return null;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void imprimirResumo(Compra c) {
        System.out.println();
        System.out.println("========== RESUMO DA COMPRA ==========");
        System.out.println("Compra:    " + c.getId());
        System.out.println("Data/hora: " + c.getDataHoraFormatada());
        System.out.println("Comprador: " + c.getComprador());
        System.out.printf("%-25s %5s %12s %12s%n", "Produto", "Qtd", "Unit. (R$)", "Total (R$)");
        for (ItemCompra i : c.getItens()) {
            System.out.printf("%-25s %5d %12.2f %12.2f%n",
                    i.getNome(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
        System.out.println("--------------------------------------");
        System.out.println("Pagamento:  " + c.getFormaPagamento());
        System.out.printf("Subtotal:   R$ %.2f%n", c.getSubtotal());
        System.out.printf("Desconto:   R$ %.2f%n", c.getDesconto());
        System.out.printf("Valor final: R$ %.2f%n", c.getValorFinal());
        System.out.println("======================================");
    }
}
package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import exception.DadosInvalidosException;
import exception.EstoqueInsuficienteException;
import exception.PersistenciaException;
import model.Compra;
import model.FormaPagamento;
import model.ItemCompra;
import model.Produto;
import repository.CompraRepository;
import util.Dinheiro;

public class CompraService {

    private final CompraRepository repository;
    private final ProdutoService produtoService;
    private final List<Compra> compras;

    public CompraService(CompraRepository repository, ProdutoService produtoService) {
        this.repository = repository;
        this.produtoService = produtoService;
        this.compras = new ArrayList<>(repository.carregarTodas());
    }

    /**
     * Finaliza uma compra.
     * @param itensSolicitados id do produto -> quantidade (vindo do carrinho)
     */
    public Compra finalizar(String comprador, Map<String, Integer> itensSolicitados, FormaPagamento pagamento) {

        // 1. Validações básicas (carrinho com produtos)
        if (comprador == null || comprador.trim().isEmpty()) {
            throw new DadosInvalidosException("O nome do comprador não pode ser vazio.");
        }
        if (pagamento == null) {
            throw new DadosInvalidosException("Escolha uma forma de pagamento.");
        }
        if (itensSolicitados == null || itensSolicitados.isEmpty()) {
            throw new DadosInvalidosException("O carrinho está vazio.");
        }

        // 2. Revalida o estoque e monta os itens (nada é alterado ainda)
        Map<Produto, Integer> aBaixar = new LinkedHashMap<>();
        List<ItemCompra> itens = new ArrayList<>();
        double subtotal = 0;

        for (Map.Entry<String, Integer> e : itensSolicitados.entrySet()) {
            Produto p = produtoService.buscarPorId(e.getKey());   // ProdutoInexistenteException
            int qtd = (e.getValue() == null) ? 0 : e.getValue();

            if (qtd <= 0) {
                throw new DadosInvalidosException("Quantidade inválida para o produto '" + p.getNome() + "'.");
            }
            if (qtd > p.getQuantidadeEmEstoque()) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para '" + p.getNome()
                        + "': solicitado " + qtd + ", disponível " + p.getQuantidadeEmEstoque() + ".");
            }

            ItemCompra item = new ItemCompra(p.getId(), p.getNome(), p.getPreco(), qtd);
            itens.add(item);
            aBaixar.put(p, qtd);
            subtotal += item.getSubtotal();
        }

        // 3. Subtotal, desconto e valor final (polimorfismo: sem if/switch por tipo)
        subtotal = Dinheiro.arredondar(subtotal);
        double desconto = pagamento.calcularDesconto(subtotal);
        double valorFinal = pagamento.calcularValorFinal(subtotal);

        // 4. Pagamento: se for recusado, nada foi alterado
        pagamento.processar(valorFinal);

        // 5. Atualiza estoque + registra compra, com rollback em caso de falha
        Compra compra = new Compra(comprador.trim(), itens, pagamento.getNome(), subtotal, desconto, valorFinal);
        Map<Produto, Integer> estoqueAnterior = new LinkedHashMap<>();

        try {
            for (Map.Entry<Produto, Integer> e : aBaixar.entrySet()) {
                Produto p = e.getKey();
                estoqueAnterior.put(p, p.getQuantidadeEmEstoque());
                p.setQuantidadeEmEstoque(p.getQuantidadeEmEstoque() - e.getValue());
            }
            compras.add(compra);
            repository.salvarTodas(compras);   // 1º arquivo
            produtoService.salvar();           // 2º arquivo
        } catch (RuntimeException ex) {
            // Desfaz tudo: memória e arquivo de compras
            for (Map.Entry<Produto, Integer> e : estoqueAnterior.entrySet()) {
                e.getKey().setQuantidadeEmEstoque(e.getValue());
            }
            compras.remove(compra);
            try {
                repository.salvarTodas(compras);
            } catch (PersistenciaException falhaRollback) {
                throw new PersistenciaException("Compra não concluída e o histórico não pôde ser restaurado: "
                        + falhaRollback.getMessage(), falhaRollback);
            }
            if (ex instanceof PersistenciaException) {
                throw new PersistenciaException("Compra não concluída; nenhuma alteração foi mantida. "
                        + ex.getMessage(), ex);
            }
            throw ex;
        }

        return compra;
    }

    public List<Compra> historicoDoComprador(String comprador) {
        List<Compra> resultado = new ArrayList<>();
        for (Compra c : compras) {
            if (c.getComprador().equalsIgnoreCase(comprador.trim())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public double totalGasto(List<Compra> historico) {
        double total = 0;
        for (Compra c : historico) {
            total += c.getValorFinal();
        }
        return Dinheiro.arredondar(total);
    }
}
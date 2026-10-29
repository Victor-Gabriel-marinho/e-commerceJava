package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Compra {
    private String id;
    private String dataHora;          // ISO-8601 (String para o Gson serializar sem reflexão em java.time)
    private String comprador;
    private List<ItemCompra> itens;
    private String formaPagamento;
    private double subtotal;
    private double desconto;          // em R$
    private double valorFinal;

    public Compra(String comprador, List<ItemCompra> itens, String formaPagamento,
                  double subtotal, double desconto, double valorFinal) {
        this.id = UUID.randomUUID().toString();
        this.dataHora = LocalDateTime.now().withNano(0).toString();
        this.comprador = comprador;
        this.itens = new ArrayList<>(itens);
        this.formaPagamento = formaPagamento;
        this.subtotal = subtotal;
        this.desconto = desconto;
        this.valorFinal = valorFinal;
    }

    public String getId() { return id; }
    public String getComprador() { return comprador; }
    public List<ItemCompra> getItens() { return Collections.unmodifiableList(itens); }
    public String getFormaPagamento() { return formaPagamento; }
    public double getSubtotal() { return subtotal; }
    public double getDesconto() { return desconto; }
    public double getValorFinal() { return valorFinal; }

    public String getDataHoraFormatada() {
        return LocalDateTime.parse(dataHora).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
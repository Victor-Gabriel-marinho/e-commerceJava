package util;

import java.util.Scanner;


public class LeitorEntrada {

    private final Scanner scanner;

    public LeitorEntrada(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Lê um texto obrigatório (não aceita vazio). */
    public String lerTexto(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Entrada vazia. Tente novamente.");
        }
    }

    /** Lê um texto que pode ficar vazio (vazio = manter valor atual). */
    public String lerTextoOpcional(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    /** Lê um inteiro obrigatório. */
    public int lerInt(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    /** Lê um inteiro opcional; retorna null se o usuário apenas apertar Enter. */
    public Integer lerIntOpcional(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro ou Enter para manter.");
            }
        }
    }

    /** Lê um número decimal obrigatório (aceita vírgula ou ponto). */
    public double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número (ex.: 19,90).");
            }
        }
    }

    /** Lê um decimal opcional; retorna null se o usuário apenas apertar Enter. */
    public Double lerDoubleOpcional(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim().replace(',', '.');
            if (texto.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número ou Enter para manter.");
            }
        }
    }

    /** Pergunta sim/não. */
    public boolean confirmar(String mensagem) {
        while (true) {
            System.out.print(mensagem + " (s/n): ");
            String texto = scanner.nextLine().trim().toLowerCase();
            if (texto.equals("s")) return true;
            if (texto.equals("n")) return false;
            System.out.println("Responda com 's' ou 'n'.");
        }
    }
}
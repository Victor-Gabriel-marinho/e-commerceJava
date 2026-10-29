package exception;

public class ProdutoInexistenteException extends RuntimeException {
    public ProdutoInexistenteException(String message) {
        super(message);
    }
}

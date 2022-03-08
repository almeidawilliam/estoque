package br.com.alura.estoque.repository;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.BaseCallback;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private ProdutoService produtoService;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
    }

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {
        buscaProdutosInternos(callback);
        produtoService = new EstoqueRetrofit().getProdutoService();
    }

    private void buscaProdutosInternos(DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(
                dao::buscaTodos,
                resultado -> {
                    callback.quandoSucesso(resultado);
                    buscaProdutosNaApi(callback);
                }).execute();
    }

    private void buscaProdutosNaApi(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> call = produtoService.buscaTodos();

        call.enqueue(new BaseCallback<>(
                new BaseCallback.RespostaCallback<List<Produto>>() {
                    @Override
                    public void quandoSucesso(List<Produto> produtosNovos) {
                        atualizaInterno(produtosNovos, callback);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        callback.quandofalha(erro);
                    }
                }));
    }

    private void atualizaInterno(List<Produto> produtos, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(
                () -> {
                    dao.salva(produtos);
                    callback.quandoSucesso(dao.buscaTodos());
                    return dao.buscaTodos();
                },
                callback::quandoSucesso
        ).execute();
    }

    public void salva(Produto produto,
                      DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = produtoService.salva(produto);
        salvaNaApi(callback, call);
    }

    private void salvaNaApi(DadosCarregadosCallback<Produto> callback,
                            Call<Produto> call) {
        call.enqueue(
                new BaseCallback<>(
                        new BaseCallback.RespostaCallback<Produto>() {
                            @Override
                            public void quandoSucesso(Produto produtoSalvo) {
                                salvaInterno(produtoSalvo, callback);
                            }

                            @Override
                            public void quandoFalha(String erro) {
                                callback.quandofalha(erro);
                            }
                        }
                )
        );
    }

    private void salvaInterno(Produto produto,
                              DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(
                () -> {
                    long id = dao.salva(produto);
                    return dao.buscaProduto(id);
                },
                callback::quandoSucesso
        ).execute();
    }

    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);

        void quandofalha(String erro);
    }
}

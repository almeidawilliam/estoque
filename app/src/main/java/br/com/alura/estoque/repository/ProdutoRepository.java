package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private ProdutoService produtoService;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
    }

    public void buscaProdutos(DadosCarregadosListener<List<Produto>> listener) {
        buscaProdutosInternos(listener);
        produtoService = new EstoqueRetrofit().getProdutoService();
    }

    private void buscaProdutosInternos(DadosCarregadosListener<List<Produto>> listener) {
        new BaseAsyncTask<>(
                dao::buscaTodos,
                resultado -> {
                    listener.quandoCarregados(resultado);
                    buscaProdutosNaApi(listener);
                }).execute();
    }

    private void buscaProdutosNaApi(DadosCarregadosListener<List<Produto>> listener) {

        Call<List<Produto>> call = produtoService.buscaTodos();
        new BaseAsyncTask<>(
                () -> {
                    try {
                        Response<List<Produto>> resposta = call.execute();
                        List<Produto> produtosNovos = resposta.body();
                        dao.salva(produtosNovos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return dao.buscaTodos();
                },
                listener::quandoCarregados
        ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void salva(Produto produto,
                      DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = produtoService.salva(produto);
        salvaNaApi(callback, call);
    }

    private void salvaNaApi(DadosCarregadosCallback<Produto> callback,
                            Call<Produto> call) {
        call.enqueue(new Callback<Produto>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Produto> call,
                                   Response<Produto> response) {
                if (response.isSuccessful()) {
                    Produto produtoSalvo = response.body();
                    if (produtoSalvo != null) {
                        salvaInterno(produtoSalvo, callback);
                    }
                } else {
                    callback.quandofalha("Resposta não sucedida");
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Produto> call,
                                  Throwable t) {
                callback.quandofalha("Falha de comunicação: " + t.getMessage());
            }
        });
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

    public interface DadosCarregadosListener<T> {
        void quandoCarregados(T resultado);
    }

    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);

        void quandofalha(String erro);
    }
}

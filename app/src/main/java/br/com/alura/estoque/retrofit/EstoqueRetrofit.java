package br.com.alura.estoque.retrofit;

import androidx.annotation.NonNull;

import br.com.alura.estoque.retrofit.service.ProdutoService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstoqueRetrofit {

    public static final String URL_BASE = "http://192.168.15.90:8080/";
    private final ProdutoService produtoService;

    public EstoqueRetrofit() {
        OkHttpClient client = configuraClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        produtoService = retrofit.create(ProdutoService.class);
    }

    @NonNull
    private OkHttpClient configuraClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public ProdutoService getProdutoService() {
        return produtoService;
    }
}

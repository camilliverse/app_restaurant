package br.com.edu.unifaj.aula8restauranteapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Lista local de pedidos que está sendo montada antes de enviar
    private HashMap<String, ArrayList<JSONObject>> pedidosPorMesa = new HashMap<>();

    private ArrayList<JSONObject> getPedidos(String idMesa) {
        if (!pedidosPorMesa.containsKey(idMesa)) {
            pedidosPorMesa.put(idMesa, new ArrayList<>());
        }
        return pedidosPorMesa.get(idMesa);
    }
    // Adapter e lista de strings para mostrar na ListView
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private static final String BASE_URL = "http://192.168.16.1:8080/api/restaurant/mesa/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView listView = findViewById(R.id.listView_mesa);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
    }

    // ──────────────────────────────────────────────
    // ADD – adiciona item na lista local (sem enviar)
    // ──────────────────────────────────────────────
    public void adicionarItem(View v) {
        EditText produtoET    = findViewById(R.id.editText_Produto);
        EditText quantidadeET = findViewById(R.id.editText_quantidade);
        EditText valorET      = findViewById(R.id.editText_Valor);

        String nome = produtoET.getText().toString().trim();
        String qtdStr = quantidadeET.getText().toString().trim();
        String valStr = valorET.getText().toString().trim();

        if (nome.isEmpty() || qtdStr.isEmpty() || valStr.isEmpty()) {
            Toast.makeText(this, "Preencha Produto, Quantidade e Valor", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int qtd = Integer.parseInt(qtdStr);
            double val = Double.parseDouble(valStr);

            JSONObject pedido = new JSONObject();
            pedido.put("nomeProduto", nome);
            pedido.put("quantidade", qtd);
            pedido.put("valor", val);

            String idMesa = ((EditText) findViewById(R.id.editText_idMesa))
                    .getText().toString().trim();

            getPedidos(idMesa).add(pedido);

            // Mostra na ListView
            listItems.add(nome + " | " + qtd + " | R$" + val);
            adapter.notifyDataSetChanged();

            // Limpa os campos de produto
            produtoET.setText("");
            quantidadeET.setText("");
            valorET.setText("");

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ──────────────────────────────────────────────
    // ATUALIZAR (PUT) – envia pedidos acumulados sem sobrescrever os do servidor
    // ──────────────────────────────────────────────
    public void AtualizarJson(View v) {
        EditText clienteET = findViewById(R.id.editText_Cliente);
        EditText idET      = findViewById(R.id.editText_idMesa);

        String cliente = clienteET.getText().toString().trim();
        String id      = idET.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Informe o ID da Mesa", Toast.LENGTH_SHORT).show();
            return;
        }

        String idMesa = idET.getText().toString().trim();
        ArrayList<JSONObject> pedidosLocais = getPedidos(idMesa);

        if (pedidosLocais.isEmpty()) {
            Toast.makeText(this, "Adicione ao menos um produto (ADD)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1º: faz GET para buscar pedidos existentes no servidor
        String url = BASE_URL + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest getRequest = new StringRequest(
                Request.Method.GET, url,
                existingJson -> {
                    // Conseguiu buscar – mescla os pedidos
                    try {
                        JSONArray pedidosExistentes = new JSONArray();
                        try {
                            JSONObject mesaExistente = new JSONObject(existingJson);
                            pedidosExistentes = mesaExistente.optJSONArray("pedido");
                            if (pedidosExistentes == null) pedidosExistentes = new JSONArray();
                        } catch (Exception ignored) { }

                        // Adiciona os pedidos locais aos existentes
                        for (JSONObject p : pedidosLocais) {
                            pedidosExistentes.put(p);
                        }

                        enviarPUT(queue, url, cliente, id, pedidosExistentes);

                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao mesclar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // Mesa ainda não existe (404) – envia só os novos
                    try {
                        JSONArray pedidosArray = new JSONArray();
                        for (JSONObject p : pedidosLocais) {
                            pedidosArray.put(p);
                        }
                        enviarPUT(queue, url, cliente, id, pedidosArray);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(getRequest);
    }

    /** Monta o JSON final e dispara o PUT */
    private void enviarPUT(RequestQueue queue, String url, String cliente, String id, JSONArray pedidos) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("cliente", cliente);
            obj.put("pedido", pedidos);
            String jsonBody = obj.toString();

            StringRequest putRequest = new StringRequest(
                    Request.Method.PUT, url,
                    response -> {
                        Toast.makeText(this, "Mesa atualizada!", Toast.LENGTH_SHORT).show();
                        getPedidos(id).clear(); // limpa lista local após envio
                        tratarResposta(response);
                    },
                    error -> tratarErro(error)
            ) {
                @Override public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override public byte[] getBody() {
                    try { return jsonBody.getBytes("utf-8"); }
                    catch (Exception e) { return null; }
                }
            };
            queue.add(putRequest);

        } catch (Exception e) {
            Toast.makeText(this, "Erro PUT: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ──────────────────────────────────────────────
    // ABRIR MESA (GET) – busca e exibe tudo na ListView
    // ──────────────────────────────────────────────
    public void abrirMesa(View v) {
        EditText idET = findViewById(R.id.editText_idMesa);
        String id = idET.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Informe o ID da Mesa", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> tratarResposta(response),
                error -> tratarErro(error)
        );
        queue.add(request);
    }

    // ──────────────────────────────────────────────
    // FECHAR MESA (DELETE)
    // ──────────────────────────────────────────────
    public void fecharMesa(View v) {
        EditText idET = findViewById(R.id.editText_idMesa);
        String id = idET.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Informe o ID da Mesa para fechar", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Mesa " + id + " fechada!", Toast.LENGTH_SHORT).show();
                    // Limpa a tela
                    listItems.clear();
                    adapter.notifyDataSetChanged();
                    pedidosPorMesa.remove(id);
                    ((EditText) findViewById(R.id.editText_Cliente)).setText("");
                    idET.setText("");
                },
                error -> tratarErro(error)
        );
        queue.add(deleteRequest);
    }
    private void tratarResposta(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            EditText clienteET = findViewById(R.id.editText_Cliente);
            EditText idET = findViewById(R.id.editText_idMesa);

            String idMesa = idET.getText().toString().trim();
            clienteET.setText(obj.optString("cliente", ""));

            listItems.clear();
            JSONArray pedidoArray = obj.optJSONArray("pedido");
            double total = 0;

            ArrayList<JSONObject> lista = getPedidos(idMesa);
            lista.clear(); // sincroniza estado local com servidor

            if (pedidoArray != null) {
                for (int i = 0; i < pedidoArray.length(); i++) {
                    JSONObject pedido = pedidoArray.getJSONObject(i);

                    // salva também no estado local da mesa
                    lista.add(pedido);

                    String nome = pedido.optString("nomeProduto", "?");
                    int qtd = pedido.optInt("quantidade", 0);
                    double val = pedido.optDouble("valor", 0);

                    total += qtd * val;
                    listItems.add(nome + " | " + qtd + " | R$" + val);
                }
            }

            listItems.add("─────────────────");
            listItems.add("Total: R$" + String.format("%.2f", total));

            adapter.notifyDataSetChanged();

        } catch (Exception ex) {
            Toast.makeText(this, "Erro resposta: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void tratarErro(VolleyError erro) {
        String mensagem = "Erro de conexão";
        if (erro.networkResponse != null) {
            mensagem = "HTTP " + erro.networkResponse.statusCode
                    + ": " + new String(erro.networkResponse.data);
        }
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

} // fim da classe

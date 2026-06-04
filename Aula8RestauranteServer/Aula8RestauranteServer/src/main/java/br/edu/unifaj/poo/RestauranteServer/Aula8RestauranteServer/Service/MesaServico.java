package br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.Service;

import br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.model.Mesa;
import br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.model.Pedido;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MesaServico {

    private final Map<Integer, Mesa> mesaMap = new TreeMap<>();
    private int nexid = 1;

    @PostConstruct
    public void CriarMesas() {
        for (int i = 1; i <= 6; i++) {
            Mesa mesa = new Mesa(nexid, true, "", new ArrayList<>(), 0.0);
            mesaMap.put(nexid, mesa);
            nexid++;
        }
    }

    public Mesa BuscarMesa(Integer id) {
        return mesaMap.get(id);
    }

    public List<Mesa> BuscarTodas() {
        return new ArrayList<>(mesaMap.values());
    }

    public Mesa AdicionarMesa(Mesa mesa) {
        if (mesa == null) return null;

        mesa.setId(nexid++);
        mesaMap.put(mesa.getId(), mesa);

        return mesa;
    }

    public Mesa AtualizarMesa(Integer id, Mesa mesaAtualizada) {
        Mesa mesa = BuscarMesa(id);

        if (mesa == null) {
            return null;
        }

        // Mesa ocupada quando atualiza
        mesa.setEstado(false);

        if (mesaAtualizada.getCliente() != null) {
            mesa.setCliente(mesaAtualizada.getCliente());
        }

        if (mesaAtualizada.getPedido() != null) {
            mesa.setPedido(mesaAtualizada.getPedido());

            double total = mesaAtualizada.getPedido().stream()
                    .mapToDouble(p -> p.getValor() * p.getQuantidade())
                    .sum();

            mesa.setValorConta(total);
        }

        return mesa;
    }

    public Mesa LimparMesa(Integer id) {
        Mesa mesa = BuscarMesa(id);

        if (mesa == null) {
            return null;
        }

        mesa.setEstado(true);
        mesa.setCliente("");
        mesa.setPedido(new ArrayList<>());
        mesa.setValorConta(0.0);

        return mesa;
    }

    public Mesa AdicionarPedido(Integer id, Pedido pedido) {

        Mesa mesa = BuscarMesa(id);

        if (mesa == null || pedido == null) {
            return null;
        }

        // garante que a lista não quebre se estiver nula
        if (mesa.getPedido() == null) {
            mesa.setPedido(new ArrayList<>());
        }

        mesa.getPedido().add(pedido);

        double total = mesa.getPedido().stream()
                .filter(p -> p.getValor() != null && p.getQuantidade() != null)
                .mapToDouble(p -> p.getValor() * p.getQuantidade())
                .sum();

        mesa.setValorConta(total);

        return mesa;
    }
}
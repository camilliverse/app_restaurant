package br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mesa {
    private Integer id;
    private boolean estado;
    private String cliente;
    private List<Pedido> pedido;
    private Double valorConta;
}

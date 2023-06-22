/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joaod
 */
public class Jogador {
    private int idJogador;
    private String nomeJogador;
    private List<Peca> pecas;

    public Jogador(int idJogador, String nomeJogador) {
        this.idJogador = idJogador;
        this.nomeJogador = nomeJogador;
        this.pecas = new ArrayList<>();
    }

    public List<Peca> getPecas() {
        return pecas;
    }

    public void adicionarPeca(Peca peca) {
        pecas.add(peca);
    }

    public Peca getPecaById(int idPeca) {
        for (Peca peca : pecas) {
            if (peca.getIdPeca() == idPeca) {
                return peca;
            }
        }
        return null; // Retorna null se a peça não for encontrada
    }

    public void removerPeca(Peca peca) {
        pecas.remove(peca);
    }

    // Outros métodos e atributos da classe Jogador...
}

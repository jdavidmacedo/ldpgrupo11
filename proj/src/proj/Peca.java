/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj;

/**
 *
 * @author joaod
 */
public class Peca {
    
    public int idPeca;
    public int numEsq;
    public int numDir;

    public Peca(int idPeca, int numEsq, int numDir) {
        this.idPeca = idPeca;
        this.numEsq = numEsq;
        this.numDir = numDir;
    }

    public int getIdPeca() {
        return idPeca;
    }

    public int getNumEsq() {
        return numEsq;
    }

    public int getNumDir() {
        return numDir;
    }

    public void setIdPeca(int idPeca) {
        this.idPeca = idPeca;
    }

    public void setNumEsq(int numEsq) {
        this.numEsq = numEsq;
    }

    public void setNumDir(int numDir) {
        this.numDir = numDir;
    }
    
    
}

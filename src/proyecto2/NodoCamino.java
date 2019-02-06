package proyecto2;

import java.util.ArrayList;

public class NodoCamino {
	
	private int x;
	
	private int y;
	
	private double f = 0;
	
	private double g = 0;
	
	private double h = 0;
	
	private NodoCamino padre;
	
	private boolean obstaculo = false;
	
	private ArrayList<NodoCamino> vecinos = new ArrayList<NodoCamino>();
	
	public NodoCamino(int x, int y, boolean isObstaculo) {
		this.x = x;
		this.y = y;
		this.obstaculo = isObstaculo;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public double getF() {
		return f;
	}

	public void setF(double d) {
		this.f = d;
	}

	public double getG() {
		return g;
	}

	public void setG(double g) {
		this.g = g;
	}

	public double getH() {
		return h;
	}

	public void setH(double d) {
		this.h = d;
	}

	/**
	 * @return the vecinos
	 */
	public ArrayList<NodoCamino> getVecinos() {
		return vecinos;
	}

	/**
	 * @param vecinos the vecinos to set
	 */
	public void setVecinos(ArrayList<NodoCamino> vecinos) {
		this.vecinos = vecinos;
	}
	
	/**
	 * @param vecinos the vecinos to set
	 */
	public void addVecino(ArrayList<ArrayList<NodoCamino>> grid, int columnas, int filas) {
		int i = this.x;
		int j = this.y;
		
		if (i<filas-1) {
			this.vecinos.add(grid.get(i+1).get(j));
		}
		if (i<0) {
			this.vecinos.add(grid.get(i-1).get(j));
		}
		if (j< columnas-1) {
			this.vecinos.add(grid.get(i).get(j+1));
		}
		if (j<0) {
			this.vecinos.add(grid.get(i).get(j-1));
		}
	}

	/**
	 * @return the padre
	 */
	public NodoCamino getPadre() {
		return padre;
	}

	/**
	 * @param padre the padre to set
	 */
	public void setPadre(NodoCamino padre) {
		this.padre = padre;
	}

	/**
	 * @return the obstaculo
	 */
	public boolean isObstaculo() {
		return obstaculo;
	}

	/**
	 * @param obstaculo the obstaculo to set
	 */
	public void setObstaculo(boolean obstaculo) {
		this.obstaculo = obstaculo;
	}


}

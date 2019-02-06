package proyecto2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import com.mobilerobots.Aria.ArArgumentParser;
import com.mobilerobots.Aria.ArLaser;
import com.mobilerobots.Aria.ArLaserConnector;
import com.mobilerobots.Aria.ArLog;
import com.mobilerobots.Aria.ArRobot;
import com.mobilerobots.Aria.ArRobotConnector;
import com.mobilerobots.Aria.ArSimpleConnector;
import com.mobilerobots.Aria.ArUtil;
import com.mobilerobots.Aria.Aria;

public class BusquedaCamino {
	//---------------
	//Aria-----------
	//---------------
	static {
		try {
			System.loadLibrary("AriaJava");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("Native code library libAriaJava failed to load. Make sure that its directory is in your library path; See javaExamples/README.txt and the chapter on Dynamic Linking Problems in the SWIG Java documentation (http://www.swig.org) for help.\n" + e);
			System.exit(1);
		}
	}
	
	//---------
	//Variables
	//---------
	
	private static int precision = 10;
	
	private static int escenaFilas = 100;
	
	private static int escenaColumnas = 100;
	
	private static ArrayList<Double> puntosX = new ArrayList<>();
	private static ArrayList<Double> puntosY = new ArrayList<>();
	private static ArrayList<Double> angulos = new ArrayList<>();
	private static ArrayList<Integer> puntosXObs = new ArrayList<>();
	private static ArrayList<Integer> puntosYObs = new ArrayList<>();
	
	private static ArrayList<ArrayList<NodoCamino>> grid = new ArrayList<ArrayList<NodoCamino>>(escenaColumnas);
	
	private static ArrayList<NodoCamino> openSet = new ArrayList<NodoCamino>();
	
	private static ArrayList<NodoCamino> closedSet = new ArrayList<NodoCamino>();
	
	private static NodoCamino inicio;
	
	private static NodoCamino fin;
	
	static ArrayList<NodoCamino> camino = new ArrayList<NodoCamino>();
	
	
	public static void inicializarEspacio() {
		
		//Crea los nodos
		for (int i=0; i<escenaColumnas; i++) {
			ArrayList<NodoCamino> fila = new ArrayList<NodoCamino>(escenaFilas);
			grid.add(fila);
			for (int j = 0; j< escenaFilas; j++) {
			
				boolean esObstaculo = false;

				for (int k=0; k<puntosXObs.size()-2; k+=2) {
					
					if(i>puntosXObs.get(k) && j>escenaFilas-puntosYObs.get(k) && i<puntosXObs.get(k+1) && j<escenaFilas-puntosYObs.get(k+1)) {
						esObstaculo = true;
					}
				}
				
				NodoCamino nodo = new NodoCamino(i,j,esObstaculo);
				fila.add(nodo);
			}
		}
		
		//Agrega vecinos
		for (int i=0; i<escenaColumnas; i++) {
			for (int j = 0; j< escenaFilas; j++) {
				grid.get(i).get(j).addVecino(grid,escenaFilas,escenaColumnas);
			}
		}
		
		inicio = grid.get((int) (puntosX.get(0)/precision)).get((int) (escenaFilas-puntosY.get(0)/precision));
		
		fin = grid.get((int) (puntosX.get(1)/precision)).get((int) (escenaFilas- puntosY.get(1)/precision));
		
		openSet.add(inicio);
		
		iteraListaOpenSet();
		
	}
	
	public static void iteraListaOpenSet() {
		
		while (!openSet.isEmpty()) {
			
			int lowestIndex = 0;
			for(int i = 0; i<openSet.size(); i++) {
				if(openSet.get(i).getF() < openSet.get(lowestIndex).getF()) {
					lowestIndex = i;
				}
			}
			
			NodoCamino actual = openSet.get(lowestIndex);
			
			if(actual.equals(fin)) {
				System.out.println("Terminó algoritmo");
				darCamino(actual);
			}
			openSet.remove(actual);
			closedSet.add(actual);
			
			ArrayList<NodoCamino> vecinos = actual.getVecinos();
			for(int i =0; i<vecinos.size(); i++) {
				NodoCamino vecino = vecinos.get(i);
				
				if(!closedSet.contains(vecino) && !vecino.isObstaculo()) {
					double tempG = actual.getG() + 1;
					
					if (openSet.contains(vecino)) {
						if (tempG < vecino.getG()) {
							vecino.setG(tempG);
						}
					} 
					else {
						vecino.setG(tempG);
						openSet.add(vecino);
					}
					
					vecino.setH(heuristica(vecino, fin));
					vecino.setF(vecino.getG()+vecino.getH());
					vecino.setPadre(actual);
				}
			}
		}
	}
	
	public static ArrayList<NodoCamino> darCamino(NodoCamino nodoFinal) {
		NodoCamino temp = nodoFinal;
		
		camino.add(nodoFinal);
		
		while(temp.getPadre() != null) {
			camino.add(temp.getPadre());
			temp = temp.getPadre();
		}
		
		for(int i= 0; i< camino.size(); i++) {
			System.out.println("Punto " + i + " CoordX: "+ camino.get(i).getX() + " CoordY: " + camino.get(i).getY());
		}
		return camino;
	}
	
	//Metodo que devuelve distancia entre dos nodos
	public static double heuristica(NodoCamino a, NodoCamino b) {
		double distancia = 0;
		
		distancia = Math.sqrt((a.getX() - b.getX())*(a.getX() - b.getY())+(a.getY() - b.getY())*(a.getY() - b.getY()));
		
		return distancia;
	}
	
	//Interpreta camino y envía comandos de movimiento a robot
	public static void moverRobot(String argv []) {
		//Inicializar Robot
		//Inicializa el API de Aria
		Aria.init();
		
		//Crea el robot y realiza la configuración inicial.
		ArRobot robot = new ArRobot("robot1", true, true, true);
		ArSimpleConnector conn = new ArSimpleConnector(argv);
		ArArgumentParser parser = new ArArgumentParser(argv);
		parser.loadDefaultArguments();
		ArRobotConnector robotConnector = new ArRobotConnector(parser, robot);
		ArLaserConnector laserConnector = new ArLaserConnector(parser, robot, robotConnector);

		//Si no se inicializó correctamente, devuelve error y cierra API Aria
		if(!robotConnector.connectRobot())
		{
			ArLog.log(ArLog.LogLevel.Terse, "lasersExample: Could not connect to the robot.");
			if(parser.checkHelpAndWarnUnparsed())
			{
				// -help not given
				Aria.logOptions();
				Aria.exit(1);
			}
		}
		if(!Aria.parseArgs())
		{
			Aria.logOptions();
			Aria.exit(1);
		}
		ArLog.log(ArLog.LogLevel.Normal, "lasersExample: Connected to robot.");
		
		if(!laserConnector.connectLasers())
		{
			ArLog.log(ArLog.LogLevel.Terse, "Could not connect to configured lasers. Exiting.");
			Aria.exit(3);
		}
		
		robot.runAsync(true);
		ArUtil.sleep(500);
		
		robot.setAbsoluteMaxTransVel(500);
		ArLog.log(ArLog.LogLevel.Normal, "Connected to all lasers.");
		ArLaser laser = robot.findLaser(1);
		robot.addRangeDevice(laser);	
		robot.enableMotors();
		//Termina Inicializar Robot
		
		ArrayList<NodoCamino> aRecorrer = new ArrayList<NodoCamino>();
		aRecorrer = camino;
		
			int moverRecto = 0;
			for (int i=aRecorrer.size()-1; i>-1; i--) {
				int puntoAnteriorX= 0;
				int puntoAnteriorY= 0;
				int puntoActualX= aRecorrer.get(i).getX();
				int puntoActualY= aRecorrer.get(i).getY();
				int puntoSiguienteX= 0;
				int puntoSiguienteY= 0;
				
				if(i==aRecorrer.size()-1) {
					puntoAnteriorX= puntoActualX;
					puntoAnteriorY= puntoActualY;
				}
				else {
					puntoAnteriorX= aRecorrer.get(i+1).getX();
					puntoAnteriorY= aRecorrer.get(i+1).getY();
				}
				if(i==0) {
					puntoSiguienteX= puntoActualX;
					puntoSiguienteY= puntoActualY;
				}
				else {
					puntoSiguienteX= aRecorrer.get(i-1).getX();
					puntoSiguienteY= aRecorrer.get(i-1).getY();
				}
				puntoActualX= aRecorrer.get(i).getX();
				puntoActualY= aRecorrer.get(i).getY();
				
				if ( (puntoActualX == puntoAnteriorX && puntoActualX == puntoSiguienteX) || (puntoActualY == puntoAnteriorY && puntoActualY == puntoSiguienteY) ) {
					//System.out.println("Suma mover recto "+ moverRecto);
					//System.out.println("ActualX " + puntoActualX +" ActualY " + puntoActualX +" AnteriorX " + puntoAnteriorX +" AnteriorY " + puntoAnteriorY +" SiguienteX " + puntoSiguienteX +" SiguienteY " + puntoSiguienteY  );
					moverRecto++;
				}
				else if (puntoActualX == puntoAnteriorX && puntoActualX<puntoSiguienteX) {
					//Mover Recto
					robot.lock();
					robot.move(moverRecto*precision);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Mueve recto: "+ moverRecto);
					//Girar derecha
					robot.lock();
					robot.setHeading(90);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Girar derecha");
					moverRecto = 0;
				}
				else if (puntoActualX == puntoAnteriorX && puntoActualX>puntoSiguienteX) {
					//Mover Recto
					robot.lock();
					robot.move(moverRecto*precision);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Mueve recto: "+ moverRecto);
					//Girar izquierda
					robot.lock();
					robot.setHeading(-90);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Girar izquierda");
					moverRecto = 0;
				}
				else if (puntoActualY == puntoAnteriorY && puntoActualY<puntoSiguienteY) {
					//Mover Recto
					robot.lock();
					robot.move(moverRecto*precision);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Mueve recto: "+ moverRecto);
					//Girar arriba
					robot.lock();
					robot.setHeading(0);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Girar arriba");
					moverRecto = 0;
				}
				else if (puntoActualY == puntoAnteriorY && puntoActualY>puntoSiguienteY) {
					//Mover Recto
					robot.lock();
					robot.move(moverRecto*precision);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Mueve recto: "+ moverRecto);
					//Girar abajo
					robot.lock();
					robot.setHeading(180);
					robot.unlock();
					ArUtil.sleep(5000);
					System.out.println("Girar abajo");
					moverRecto = 0;
				}
			}
			
			robot.lock();
			robot.move(moverRecto*precision);
			robot.unlock();
			ArUtil.sleep(5000);
			System.out.println("Ultimo mueve recto: "+ moverRecto);
			robot.stopRunning(true);
			robot.lock();
			robot.disconnect();
			robot.unlock();
			Aria.exit(0);
			
	}

	public static void main (String argv[]) throws IOException{
		
		System.out.println("PLANIFICACIÓN DE CAMINOS GEOMÉTRICOS, "	+ "EJECUCIÓN (MODO AUTÓNOMO) "	+ "Y LOCALIZACIÓN");
		System.out.println("-------------------------------------------------------------");
		System.out.println();
		Scanner inUser = new Scanner(System.in);
		String filename = "";
		System.out.println("Ingrese el nombre del archivo de texto");
		filename = inUser.nextLine();
		if(!filename.contains(".txt")){
			filename += ".txt";
		}
		System.out.println("¿Se obtuvo algún archivo de configuración?" );
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			System.out.println("Sí. El nombre del archivo es: " + filename);
			String line;
			while((line = in.readLine()) != null)
			{
				if(line.contains("q")){
					puntosX.add(Double.parseDouble(line.split(" ")[1].split(",")[0]));
					puntosY.add(Double.parseDouble(line.split(" ")[2].split(",")[0]));
					angulos.add(Double.parseDouble(line.split(" ")[3]));
				}
				else if(line.contains("Tamano")){
					escenaColumnas = (int) Double.parseDouble(line.split(" ")[1].split(",")[0])/precision;
					escenaFilas = (int) Double.parseDouble(line.split(" ")[2])/precision;
				}
				else if(line.contains("Pto")){
					puntosXObs.add((int) Double.parseDouble(line.split(" ")[1].split(",")[0]));
					puntosYObs.add((int)Double.parseDouble(line.split(" ")[2]));
				}
			}
			in.close();
			inUser.close();
		} catch (FileNotFoundException e) {
			System.out.println("El archivo especificado no existe.");
			System.out.println("Saliendo del programa...");
			System.out.println("Programa detenido.");
			inUser.close();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		double anguloInic = angulos.get(0);
		if(anguloInic<0){
			for(int i = 0; i<angulos.size();i++){
				angulos.set(i, angulos.get(i)+Math.abs(anguloInic));
				//System.out.println(angulos.get(i));
			}
		}
		else if(anguloInic>0){
			for(int i = 0; i<angulos.size();i++){
				angulos.set(i, angulos.get(i)-anguloInic);
			}
		}
		
		inicializarEspacio();
		
		moverRobot(argv);
		
		//C:\Users\asus\Documents\GitHub\Proyecto 2 Robotica Movil a.sandoval sf.munera\escenarios\Escenario-Planificacion1
	}
	
}


/**
 *
 * @author AlberPc
 */
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//VAlores con los que funciona "bien": 
//Alpha = 0.1,0.1,0,1
//Gamma =0.9,0.9,0.5
//reward  = 10,1,5
//penalty = -100,-50,-50//No muy allá... 
public class psi15_InteligentAgent extends Agent {

    private Agent agent;
    private int ID, turn, mybet, mycoins;
    private String order, betsString;
    private int drawcount = 0;
    private String[] IDs, bets;
    private final double alpha = 0.1;     //Learning rate
    private final double gamma = 0.90; //Discount factor that determines the importance of future rewards
    int statesCount;
    private final int reward = 10;      //Recompensa por ganar. Esto actualizará el valor de Q
    private final int penalty = -100; //Penalizacion por empatar o perder. Nuestro bot es ambicioso, por lo que se le va a penalizar por perder y empatar
    HashMap<Integer, Double> hmap = new HashMap<Integer, Double>(); //Esto al final no lo uso 
    private ArrayList<Integer> betlist = new ArrayList<Integer>(); //Arraylist con las apuestas de los demas
    double[][] Q1; //Filas = stage. Stage es el numero de jugadores que hay en la ronda, lo cual tambien es la "fase" en la que nos encontramos. Segun esta fase jugara de una forma u otra segun ese numero de jugadores totales. 
    //Columnas = bet. Esta bet es el numero de coins a jugar. El valor que guarda esta matriz es el Q segun la fase y la apuesta. Seleccionamos el mayor, que ha sido el que por lo general mas veces ganó
    double[][] Q2;//Filas = stage. Columnas es el numero de monedas. Q guarda el valor entre la fase y la apuesta, buscando cual es el que mas veces ha dado lugar a victoria.
    //double[][] Q3; relacion entre Q1 y Q2 -> Posible mejora

    protected void setup() {

        agent = this;
        //JADE STUFF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setName(getLocalName());
        sd.setType("Player");       //Setting Player as type
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        //agent creation confirmation
        System.out.println("Hello! Random-Agent: " + getAID().getName() + " is ready.");
        GameBehaviourRandom gameBehaviour = new GameBehaviourRandom(this);
        addBehaviour(gameBehaviour);

    }

    public class GameBehaviourRandom extends SimpleBehaviour {

        public GameBehaviourRandom(Agent a) {
            super(a);
        }

        @Override
        public void action() { //If tree for seeking what is the message recived and responding.

            ACLMessage acl = blockingReceive();//Waiting for message
            String message = acl.getContent();  //Get content of recived
            betlist = new ArrayList<Integer>(); //Inicializacion de la lista de apuestas

            //Stage 1: ID assigment
            if (message.startsWith("Id#")) {
                ID = Integer.parseInt(message.split("#")[1]);
            } //Stage 2: Peticion de coins.
            else if (message.startsWith("GetCoins#")) {
                drawcount++;
                if (drawcount > 5) {           //Comprobamos cuantas veces ha empatado ya
                    updateQ(false, mybet); //Penalizacion doble si empata mas de 5 veces. @mybet ha sido la apuesta anterior

                }
                //GetCoins#[IDS]#Turno
                order = message.split("#")[1]; //String con el orden y las IDs de los demas -> se usa para ver el numero de jugadores que hay con IDs
                IDs = order.split(",");
                turn = Integer.parseInt(message.split("#")[2]); //Comprobacion de turno
                if (hmap.isEmpty()) {//Inicializacion de las matrices segun el numero de jugadores maximo
                    initializescores(); //Inicializacion de las matrices y Qs. 
                    int playermax = IDs.length; //Maximo de jugadores para ver el maximo de la matriz.
                    //  System.out.println("INICIANDO LAS MATRICES:" + playermax);
                    Q1 = new double[playermax][playermax * 3 + 1];
                    Q2 = new double[playermax][4];
                    setZeroes(Q2);
                    setZeroes(Q1);
                    //DEBUG:
                    //    System.out.println("MATRIX:");
                    //printmatrix(Q2);
                }
                //Enviamos nuestras monedas
                mycoins = giveCoins();
                //DEBUG:
                // System.out.println("GIVECOINS:" + mycoins);
                String auxMes = "MyCoins#" + mycoins;
                sendReply(acl, auxMes);
            } else if (message.startsWith("GuessCoins#")) {
                if (message.equals("GuessCoins#")) {
                    mybet = calculatebet();

                    // System.out.println("RANDOMBET:"+mybet); FOR DEBUGGING
                    String auxMes = "MyBet#" + mybet;
                    sendReply(acl, auxMes);

                } else {
                    //GuessCoins#[apuestas de los demas]
                    betsString = message.split("#")[1];
                    bets = betsString.split(","); //Apuestas de los demas para ver las apuestas posibles
                    int[] intbets = new int[99]; //Conversion a integer de los bets de los demas
                    for (int i = 0; i < bets.length; i++) {
                        intbets[i] = Integer.parseInt(bets[i]);
                    }
                    Integer[] what = Arrays.stream(intbets).boxed().toArray(Integer[]::new);
                    //  System.out.println("////////////////////////BETS:"+Arrays.toString(bets));  //Debug
                    betlist = new ArrayList<Integer>(Arrays.asList(what));
                    mybet = calculatebet(); //Calculamos nuestras apuestas
                    String auxMes = "MyBet#" + mybet;
                    sendReply(acl, auxMes);//Enviamos mensaje
                }

            } //Stage 4: Comprobacion del resultado de la ronda
            else if (message.startsWith("Result#")) {
                boolean isWin = false;
                String[] messageSplitted = message.split("#");
                if (messageSplitted[1].equals(String.valueOf(ID))) {
                    isWin = true;
                    drawcount = 0;
                }
                int winbet = Integer.parseInt(messageSplitted[2]);
                updateQ(isWin, winbet);
            } else {
                System.out.println("MENSAJE DESCONOCIDO " + message);
            }
        }

        @Override
        public boolean done() {
            return false;
        }

        private void initializescores() {//empezamos con todos los scores a 0 ya que no tenemos informacion anterior
            for (int i = 0; i < IDs.length * 3; i++) {
                double score = 1 / IDs.length * 3;
                hmap.put(i, score);
            }
        }

        private void updateQ(boolean winner, int bet) {
            int state = IDs.length - 1; //Numero de jugadores (Las matrices empiezan en 0)
            double oldbet = Q1[state][bet];//En ese numero de jugadores, actualizamos el valor de Q para la apuesta realizada
            double oldmycoins = Q2[state][mycoins]; //Con ese numero de jugadores actualizamos el valor de Q para las monedas apostadas
            // System.out.println("MYCOINS IA" + mycoins); //FOR DEBUGGING
            if (winner) { //Si gana lo recompensamos actualizando el valor
                Q2[state][mycoins] += alpha * (reward + gamma * getMaxValue(Q2) - oldmycoins);
                Q1[state][bet] += alpha * (reward + gamma * getMaxValue(Q1) - oldbet);
            } else { //Si pierde o empata lo penalizamos y premiamos la apuesta ganadora
                double oldmybet = Q1[state][bet];
                Q1[state][mybet] += alpha * (penalty + gamma * getMaxValue(Q1) - oldmybet);
                Q1[state][bet] += alpha * (reward + gamma * getMaxValue(Q1) - oldbet);
                Q2[state][mycoins] += alpha * (penalty + gamma * getMaxValue(Q2) - oldmycoins);

            }
            //DEBUG: Comprobacion de valores de Q
//       System.out.println("Q1/////////////////////////////////");
//         printmatrix(Q1);
//      System.out.println("Q2/////////////////////////////////");
//       printmatrix(Q2);
        }

    }

    public void sendReply(ACLMessage acl, String content) {
        ACLMessage reply = acl.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(content);
        send(reply);
    }

    public int giveCoins() //Give random amount of coins (0 to 3) TODO: Modificaciones para que las de de forma inteligente
    {//QLearning algorithm personalizado
        //  Random randomGenerator = new Random();

        int state = IDs.length - 1; //El array empieza en 0
        int[] actionsFromState = {0, 1, 2, 3}; //Posibles coins que puede dar
        double maxValue = -999999999; //No funciona el double.min
        int policyGotoState = state;
        for (int nextState : actionsFromState) { //Recorremos los posibles valores Q, escogiendo el mayor de todas las posibles acciones segun el numero de jugadores que haya
            double value = Q2[state][nextState];
//          System.out.println("//Q2 NextState: "+ nextState);
//           System.out.println("//Q2 VALUEEEEEEEE : " + value);
//           System.out.println("//Q2 MAXVALUEEEEEEEE : " + value);
            if (value > maxValue) {
//                System.out.println("//Q2 VALUEEEEEEEE : " + value);
//                System.out.println("//Q2 MAXVALUEEEEEEEE : " + value);
                maxValue = value;
                policyGotoState = nextState;

            }
        }
        //System.out.println("/////////////////////////////COINS IA:     "+policyGotoState);
        if (policyGotoState == 4) { //A veces devolvia 4 
            policyGotoState = 3;
        }
        return policyGotoState;

    }

//Calcula aleatoriamente la apuesta, excluyendo los numeros que ya han salido
//    public int calculaterandombet() { //Calcular de forma inteligente, QLEARNING
//
//        while (true) {
//            Random randomGenerator = new Random();
//            int randomInt = mycoins + randomGenerator.nextInt(IDs.length * 2);
//
//            if (bets == null) {
//                return randomInt;
//            } else {
//
//                if (!(Arrays.asList(bets).contains(String.valueOf(randomInt)))) {
//                    return randomInt;
//                }
//            }
//        }
//
//    }
    //Algoritmo de QLearning -> Estado inicial random (primera apuesta random) -> Si gana le aplicamos el reward, si pierde lo penalizamos..., si empata,  aplicamos la penalizacion tambien
    //Ver las posibles bets y elegir cual es el que tiene mayor Q despues de la criba
    int calculatebet() {

        //Metodo que devuelve el la apuesta con el mayor Q1 de todos
        int state = IDs.length - 1;
        ArrayList<Integer> actionsFromState = possibleActionsFromState();
        //System.out.println("///////////////POSIBLE ACTIONS: :" + Arrays.toString(actionsFromState.toArray())); DEBUGGING
        double maxValue = -99999999;
        int policyGotoState = state;
        for (int nextState : actionsFromState) {
         //   System.out.println("NEXTSTATE:" + nextState); //Debug
            double value = Q1[state][nextState];
            if (value > maxValue) {
                System.out.println("//Q1 VALUEEEEEEEE : " + value);

                maxValue = value;
                //  System.out.println("//Q1 MaxValueeeeeeeeeeeee" + maxValue); //DEBUGGING
                policyGotoState = nextState;
            }
        }

        return policyGotoState;
    }

//metodo que devuelve las posibles acciones dadas un numero de jugadores y las monedas que escondes
    private ArrayList<Integer> possibleActionsFromState() {
        ArrayList<Integer> possiblebets = new ArrayList<Integer>();
        //  System.out.println("///////////////BETLIST: :" + Arrays.toString(betlist.toArray()));
        int j = 0;
        for (int i = 0; i < IDs.length * 3; i++) {
            if (!((mybet == i) || (betlist.contains(i)))) {
                if (i >= mycoins) {
                    possiblebets.add(i);
                }
            }

        }
        return possiblebets;
    }
    //Metodo para obtener el valor maximo de una matriz bidimensional

    public static double getMaxValue(double[][] numbers) {
        double maxValue = numbers[0][0];
        for (int j = 0; j < numbers.length; j++) {
            for (int i = 0; i < numbers[j].length; i++) {
                if (numbers[j][i] > maxValue) {
                    maxValue = numbers[j][i];
                }
            }
        }
        return maxValue;
    }

    //Metodo para inicializar las matrices bidimensionales a 0
    public void setZeroes(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        boolean[] rowzero = new boolean[rows];
        boolean[] colzero = new boolean[cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (matrix[r][c] == 0) {
                    rowzero[r] = true;
                    colzero[c] = true;
                }
            }
        }
        for (int r = 0; r < rows; r++) {
            if (rowzero[r]) {
                for (int c = 0; c < cols; c++) {
                    matrix[r][c] = 0;
                }
            }
        }
        for (int c = 0; c < cols; c++) {
            if (colzero[c]) {
                for (int r = 0; r < rows; r++) {
                    matrix[r][c] = 0;
                }
            }
        }
    }

    //Metodo para el debug de las matrices Q
    public void printmatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

}


import java.util.TreeMap;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@SuppressWarnings("serial")
public class psi15_mainAgent extends Agent {

    @SuppressWarnings("unused")
    private int ID = 0;
    psi15_GUIFrame GUI = null;
    AMSAgentDescription[] agents = null;
    static psi15_mainAgent ma;
    private TreeMap<Integer, AID> players = new TreeMap<Integer, AID>();
    private ArrayList<psi15_Player> listOfPlayers = new ArrayList<psi15_Player>();
    private ArrayList<psi15_Player> listOfWinners = new ArrayList<psi15_Player>();
    private ArrayList<AMSAgentDescription> agentlist = new ArrayList<AMSAgentDescription>();
    private ArrayList<String> coinList = new ArrayList<String>();
    private ArrayList<String> IDList = new ArrayList<String>();
    private ArrayList<String> betList = new ArrayList<String>();
    boolean paused = false, newGame = false, gameBeggined = false, delay = false;
    static boolean needToReset = false;
    int resultado, draw, played;
    psi15_Player winner;
    int numerodepartidas;
    int initnumerodepartidas;

    protected void setup() {

        System.out.println("Hello! Main Agent: " + getAID().getName() + " is ready.");

        new Thread() {
            @Override
            public void run() {

            }
        }.start();

        ma = this;
        GUI = psi15_GUIFrame.waitForStartUpTest();

        MainAgentBehaviour mainAgentBehaviour = new MainAgentBehaviour(this);
        addBehaviour(mainAgentBehaviour);

        GameBehaviour gameBehaviour = new GameBehaviour(this);
        addBehaviour(gameBehaviour);

    }

    protected void takeDown() {
        System.out.println("Main-Agent " + getAID().getName() + " terminating.");
    }

    public class MainAgentBehaviour extends SimpleBehaviour {

        public MainAgentBehaviour(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            GUI = new psi15_GUIFrame();//Creating GUI and maximizing
            GUI.setVisible(true);
            GUI.setExtendedState(GUI.getExtendedState() | GUI.MAXIMIZED_BOTH);
            System.out.println("Starting behaviour for main-agent. Just looking for other agents to play...");
            //Inicio de la comunicacion con agentes
            try {
                SearchConstraints restricciones = new SearchConstraints();
                restricciones.setMaxResults(new Long(-1));
                agents = AMSService.search(ma, new AMSAgentDescription(), restricciones);
            } catch (Exception e) {//Agent Searching...
                GUI.log("Error searching agents: " + e.getMessage());
                System.out.println("ERROR:" + e.getMessage());
            }

            GUI.log("Agents' search finished.\n");
            GUI.log("Number of agents found: " + agents.length + "\n");//If found, log it

            for (AMSAgentDescription agent : agents) {//Player searching (randomAgent or FixedAgent)
                if (!(agent.getName().toString().contains("ams")
                        || agent.getName().toString().contains("df")
                        || agent.getName().equals(ma.getName())
                        || agent.getName().toString().contains("rma"))) {
                    //If found we asign a new ID, add player to playerlist and agent to the agent list
                    GUI.log("\nNew player: " + agent.getName() + ". ID asigned: " + ID);
                    players.put(ID, agent.getName());
                    agentlist.add(agent);
                    psi15_Player newPlayer = new psi15_Player(agent);
                    String nameAux = agent.getName().toString().split("@")[0].split("name")[1];
                    newPlayer.setName(nameAux);
                    listOfPlayers.add(newPlayer);
                    newPlayer.setId(ID);
                    GUI.setTable(listOfPlayers);
                    ID++;
                }
            }

            GUI.log("\nNumber of players: " + players.size() + "\n");//Log number of players
            GUI.addPlayers(agentlist);//Refresh GUI

        }

        @Override
        public boolean done() {
            return true;
        }

    }

    public class GameBehaviour extends SimpleBehaviour {

        public GameBehaviour(Agent a) {
            super(a);
        }

        //lisfOfPlayers = list of the players that are playing, removing the winners.
        @Override
        public void action() {

            while (true) {//Always until close

                System.out.println("Principio del behavorial del main agent...");
                if (!newGame) {//Waiting to hit "New" in GUI
                    ma.doWait();
                }

                numerodepartidas = initnumerodepartidas;
                for (int o = 0; o < listOfPlayers.size(); o++) { //
                    psi15_Player aux = listOfPlayers.get(o);
                    aux.resetGame();
                    aux.resetLocalGame();

                }

                needToReset = false;
                newGame = true;
                gameBeggined = true;
                for (int i = 0; i < players.size(); i++) {
                    AID aidAux = null;
                    aidAux = players.get(i);
                    String message = "Id#" + i;
                    // sleepAgent(10); //Sleeping the thread for no insta-games
                    sendACL(ACLMessage.INFORM, aidAux, message, i);//Sending: ID#[Unique ID]
                }
                for (int numpar = 0; numpar < numerodepartidas; numpar++) { //Numero de partidas

                    while (listOfPlayers.size() > 1) {//Mientras quede mas de 1 jugador
                        //Step 1
                        //  sleepAgent(10); //Sleeping the thread for no insta-games
                        if (paused) {//Is it paused?
                            ma.doWait();
                            paused = false;
                        }
                        String turns = getTurns(); //Method to get turns using the IDList

                        //Step 2 //Request de las coins, generacion de los turnos
                        coinList = new ArrayList();
                        if (paused) {//Comprobe if paused
                            ma.doWait();
                            paused = false;
                        }
                        for (int i = 0; i < listOfPlayers.size(); i++) {
                            psi15_Player playerAux = listOfPlayers.get(i);
                            int playerID = playerAux.getId();

                            String message = "GetCoins#" + turns + "#";
                            int auxTurn = getTurn(playerID);
                            sendACL(ACLMessage.REQUEST, players.get(Integer.parseInt(IDList.get(i))), message + auxTurn, playerID); //Esperamos la respuesta
                            ACLMessage replay1 = blockingReceive();
                            int hisCoins = Integer.valueOf(replay1.getContent().split("#")[1]);//Cogemos la jugada

                            playerAux.setCoins(hisCoins);//Set the player coins in class Player
                            coinList.add(String.valueOf(hisCoins));//Adding the value to the list

                        }

                        //Step 3 Ver las apuestas
                        //    sleepAgent(10); //Sleeping the thread for no insta-games
                        if (paused) {//Is it paused?
                            ma.doWait();
                            paused = false;
                        }
                        //Empezamos con el primero
                        betList = new ArrayList();
                        String message1 = "GuessCoins#";
                        sendACL(ACLMessage.REQUEST, players.get(Integer.parseInt(IDList.get(0))), message1, Integer.parseInt(IDList.get(0))); //cogemos el primero
                        ACLMessage replay1 = blockingReceive();             //Esperamos a que vuelva la respuesta
                        String auxbet = replay1.getContent().split("#")[1]; //Cogemos la parte del mensaje que nos interesa
                        for (int i = 0; i < listOfPlayers.size(); i++) {             //Recorremos la lista de jugadores
                            psi15_Player playerAux = listOfPlayers.get(i);
                            if (Integer.parseInt(IDList.get(0)) == playerAux.getId()) { //Asignamos la apuesta al player que ha sido (en la lista)
                                playerAux.setBet(auxbet); //Le asignamos la apuesta a su jugador correspondiente
                            }
                        }
                        betList.add(auxbet);
                        //sucesivos jugadores
                        for (int i = 1; i < listOfPlayers.size(); i++) {

                            if (paused) {
                                ma.doWait();
                                paused = false;
                            }
                            String message2 = "GuessCoins#" + String.join(",", betList);
                            sendACL(ACLMessage.REQUEST, players.get(Integer.parseInt(IDList.get(i))), message2, Integer.parseInt(IDList.get(i))); //enviamos a sucesivos
                            ACLMessage replay2 = blockingReceive();
                            String auxbet2 = replay2.getContent().split("#")[1];

                            for (int k = 0; k < listOfPlayers.size(); k++) {
                                psi15_Player playerAux = listOfPlayers.get(k);
                                if (Integer.parseInt(IDList.get(i)) == playerAux.getId()) {
                                    playerAux.setBet(auxbet2);

                                }//Le asignamos la apuesta a su jugador correspondiente
                            }
                            betList.add(auxbet2);
                        }
                        //Step 4: Comprobar el ganador
                        //sleepAgent(10); //Sleeping the thread for no insta-games
                        if (paused) {
                            ma.doWait();
                            paused = false;
                        }
                        resultado = 0;
                        for (int i = 0; i < coinList.size(); i++) {
                            resultado += Integer.parseInt(coinList.get(i)); //El resultado es la suma de los elementos de la coinList el cual contiene el numero de monedas escondidas por cada jugador
                        }
                        winner = comprobeWinner(); //metodo para comprobar cuale es el ganador
                        if (winner == null) {
                            System.out.println("EMPATE--->");//Si no hay ganador consideramos empate
                            GUI.log("Empate-->");
                            draw++;
                            coinList = new ArrayList();
                            GUI.log("Apuestas:" + String.join(",", betList) + "Total de monedas=" + resultado + "\n");
                            for (int i = 0; i < players.size(); i++) {

                                String message = "Result#" + "#" + resultado + "#" + String.join(",", betList) + "#" + String.join(",", coinList); //Informamos del resultado a todos los jugadores

                                sendACL(ACLMessage.INFORM, players.get(i), message, 99);
                            }
                        } else {                                   //Tenemos ganador
                            winner.win++;
                            GUI.actualizawins(winner); //Actualizamos la GUI

                            for (int i = 0; i < players.size(); i++) {

                                String message = "Result#" + winner.getId() + "#" + resultado + "#" + String.join(",", betList) + "#" + String.join(",", coinList); //Informamos del resultado a todos los jugadores

                                sendACL(ACLMessage.INFORM, players.get(i), message, 99);
                            }
                            StringBuilder sb = new StringBuilder();
                            for (String s : IDList) {
                                sb.append(s);
                                sb.append(",");
                            }
                            GUI.log("Hay ganador--> ID=" + winner.getId() + " Apuesta ganadora=" + resultado + " Lista de apuestas=" + String.join(",", betList) + " Lista de monedas=" + String.join(",", coinList) + " IDs--->" + sb.toString() + " \n"); //LOG
                            if (listOfPlayers.size() == 1) {
                                GUI.log("PERDEDOR ID#" + listOfPlayers.get(0).getId() + "\n"); //Cuando solo queda un jugador, hemos encontrado el looser. :(
                            }
                        }

                    }

                    //Acabamos la partida, se ha decidido el jugador perdedor
                    psi15_Player looser = listOfPlayers.get(0);
                    looser.lost++;
                    GUI.setLooser(looser);
                    resetRound();
                    played++;
                    GUI.actualizaPlayedLabel(played); //Actualizamos la informacion
                    //Actualizamos informacion
                    GUI.log("\nFIN DE LA PARTIDA\n"); //Fin de la ronda
                }
                GUI.log("\n FIN DE LAS RONDAS \n");
                //Fin del for de numero de partidas
                numerodepartidas = 0;
                newGame = false;
            }
        }

        @Override
        public boolean done() {
            return true;
        }

        private void resetRound() { //RESETEAMOS LAS LISTAS
            IDList = new ArrayList<String>();
            betList = new ArrayList<String>();
            coinList = new ArrayList<String>();
            for (int i = 0; i < listOfWinners.size(); i++) {
                listOfPlayers.add(listOfWinners.get(i));
            }
            listOfWinners = new ArrayList<psi15_Player>();
        }

    }

    public void resetLocalGameParameters() { //Reseteamos los parametros de los players
        for (int w = 0; w < listOfPlayers.size(); w++) {
            listOfPlayers.get(w).resetGame();
            listOfPlayers.get(w).resetLocalGame();
        }
    }

    public static psi15_mainAgent getInstance() { //method for obtain an Instance of the mainAgent
        return ma;
    }

    public void unblockAgent() { //Method for unblock Agent
        ACLMessage acl = new ACLMessage(ACLMessage.INFORM);
        acl.addReceiver(this.getAID());
        send(acl);
    }

    private void sleepAgent(int time) { //method for sleep the agent
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getPaused() { //Return if the game is paused
        this.paused = true;
    }

    public void resetStats() { //Reset the players stats

        for (int o = 0; o < listOfPlayers.size(); o++) {
            psi15_Player aux = listOfPlayers.get(o);
            aux.resetStatistics();
            aux.resetGame();
            aux.resetLocalGame();

        }

    }

    public void setNumberOfPlays(int play) { //Setter
        initnumerodepartidas = play;
    }

    public boolean areIPaused() { //getter
        return paused;
    }

    public void setNewGameTrue() { //Method for begin a new game
        newGame = true;
        ma.doWake();
    }

    public void sendACL(int performative, AID receiver, String content, int id) { //method for send the ACL message.
        ACLMessage acl = new ACLMessage(performative);
        acl.addReceiver(receiver);
        acl.setContent(content);
        System.out.println("SENDING: " + content + " to ID:-----> " + id);
        send(acl);
    }

    private String getTurns() { //Method that returns the turns string.
        IDList = new ArrayList();
        for (int i = 0; i < listOfPlayers.size(); i++) {
            IDList.add(String.valueOf(listOfPlayers.get(i).getId()));

        }
        Collections.shuffle(IDList, new Random()); //Shuffles randomly the IDList (turnlist) 
        String cosa = String.join(",", IDList); //list To string
        return cosa;

    }

    private int getTurn(int ID) {//metodo para asignar turno
        for (int i = 1; i < IDList.size() + 1; i++) {
            String IDaux = IDList.get(i - 1);
            if (IDaux.equals(String.valueOf(ID))) { //Comprobes if the ID is the correct in that position of the list and return the position

                return i;
            }

        }
        return -1;
    }

    private psi15_Player comprobeWinner() { //comprobes winner viewing the bet list and  checking if the player did the bet == to the sum of the hidden coins (resultado). It removes the winner of the current player list too
        for (int i = 0; i < listOfPlayers.size(); i++) {
            psi15_Player playerAux = listOfPlayers.get(i);
            //System.out.println("BET:"+playerAux.getBet()+"#"+playerAux.getName());
            if (playerAux.getBet() == resultado) { //if it is the winner...
                listOfPlayers.remove(i); //Removing from the current player list
                GUI.actualizaRemaningPlayers(listOfPlayers.size()); //Update GUI info
                listOfWinners.add(playerAux); //Winnerlist 
                return playerAux;
            }

        }
        return null; //If no winner found return null
    }
}

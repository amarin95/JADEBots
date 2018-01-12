/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author AlberPc
 */
public class psi15_RandomAgent extends Agent{
 
private int myPay;
    private Agent agent;
    private int ID, numberOfPlayers,rounds,turn,mybet,mycoins;
    private String order,betsString;
    
    private String[] IDs,bets;
    
    
    protected void setup(){
		
		agent = this;
                                    //Shitty JADE STUFF
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();

		sd.setName(getLocalName());
		sd.setType("Player");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		System.out.println("Hello! Random-Agent: " + getAID().getName() + " is ready.");
		GameBehaviourRandom gameBehaviour = new GameBehaviourRandom(this);
		addBehaviour(gameBehaviour);
		
	}
    
    
    	public class GameBehaviourRandom extends SimpleBehaviour {

		public GameBehaviourRandom (Agent a){
			super(a);
		}
		
		@Override
		public void action() { //If tree for seeking what is the message recived and responding.
			
			ACLMessage acl = blockingReceive();
			String message = acl.getContent();
			//System.out.println("RANDOM MESSAGE: "+ message);
			if (message.startsWith("Id#")){
				ID = Integer.parseInt(message.split("#")[1]);
				
                                                       }
                   
                                                      else if(message.startsWith("GetCoins#")||message.contains("Get")){
                                                           
                                                           //GetCoins#[IDS]#Turno
                                                            order = message.split("#")[1];
                                                            IDs = order.split(",");
                                                            turn = Integer.parseInt(message.split("#")[2]);
                                                            //System.out.println("IDS LENGTH RANDOM ////////////////////////////////// = "+IDs.length+ "CONTENT: "+ Arrays.toString(IDs));
                                                            //Enviamos nuestras monedas
                                                            mycoins = giveCoins();
                                                           // System.out.println("GIVECOINS:"+mycoins);
                                                            String auxMes = "MyCoins#" + mycoins;
                                                            sendReply(acl, auxMes);
                                                       }
                                                     else if(message.startsWith("GuessCoins#")){
                                                            if(message.equals("GuessCoins#")){
                                                              mybet = calculatebet();
                                                              // System.out.println("RANDOMBET:"+mybet); FOR DEBUGGING
                                                              String auxMes="MyBet#"+mybet;
                                                              sendReply(acl, auxMes);
                                                          }else{
                                                          //GuessCoins#[apuestas de los demas]
                                                          betsString = message.split("#")[1];
                                                          bets = betsString.split(",");
                                                          //Calculamos nuestras apuestas
                                                          mybet = calculatebet();
                                                       
                                                         String auxMes="MyBet#"+mybet;
                                                        //Enviamos mensaje
                                                          sendReply(acl, auxMes);
                                                          }
                                                          
                                                      }else if(message.startsWith("Result#")){
                                                          
                                                          //TODO: Comprobar ganadores (Done in main)
                                                         
                                                      }
                                                      else{
                                                          System.out.println("MENSAJE DESCONOCIDO"+ message);
                                                      }
                }

        @Override
        public boolean done() {
          return false;
        }
        }
	
	
	public void sendReply(ACLMessage acl, String content) {
		ACLMessage reply = acl.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		reply.setContent(content);
		send(reply);
	}
	

	
	
	




public int giveCoins () //Give random amount of coins (0 to 3)
	{
	    Random randomGenerator = new Random();
                       int randomInt = randomGenerator.nextInt(4);
                       return randomInt;
	}

//Calcula aleatoriamente la apuesta, excluyendo los numeros que ya han salido
public int calculatebet(){
    
    
    while(true){ 
         Random randomGenerator = new Random();
         int randomInt = mycoins + randomGenerator.nextInt(IDs.length*2);
       //  System.out.println("IDS LENGTH RANDOM ////////////////////////////////// = "+IDs.length+ "CONTENT: "+ Arrays.toString(IDs));
        
         
        if(bets == null){
            return randomInt;
        }else{
        
         if(!(Arrays.asList(bets).contains(String.valueOf(randomInt))))
            return randomInt;
        }
    }
    
    

}
}

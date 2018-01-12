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
public class psi15_FixedAgent extends Agent{
 
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
		
		System.out.println("Hello! Fixed-Agent: " + getAID().getName() + " is ready.");
		
		GameBehaviourRandom gameBehaviour = new GameBehaviourRandom(this);
		addBehaviour(gameBehaviour);
		
	}
    
    
    	public class GameBehaviourRandom extends SimpleBehaviour {

		public GameBehaviourRandom (Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			
			ACLMessage acl = blockingReceive();
			String message = acl.getContent();
			
			if (message.startsWith("Id#")){
				ID = Integer.parseInt(message.split("#")[1]);
				
                                                       }
                                                       if(message.startsWith("GetCoins#")){
                                                           
                                                           //GetCoins#[IDS]#Turno
                                                            order = message.split("#")[1];
                                                            IDs = order.split(",");
                                                            turn = Integer.parseInt(message.split("#")[2]);
                                                            
                                                            //Enviamos nuestras monedas
                                                            mycoins = giveCoins();
                                                            String auxMes = "MyCoins#" + mycoins;
                                                            sendReply(acl, auxMes);
                                                       }
                                                      if(message.startsWith("GuessCoins#")){
                                                          if(message.equals("GuessCoins#")){
                                                              mybet = calculatebet();
                                                              
                                                              
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
                                                      }if(message.startsWith("Result#")){
                                                          
                                                          //TODO: Comprobar ganadores
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
	

	
	
	




public int giveCoins ()
	{
	    
                       return 1;
	}

//Calcula aleatoriamente la apuesta, excluyendo los numeros que ya han salido
public int calculatebet(){
    
   
    while(true){
             if(bets == null){
              
            return 1;
        }else{
               
         for(int i=1;i>0;i++){ //Check the bet, it can't be repeated
             
         if(!(Arrays.asList(bets).contains(String.valueOf(i)))){
             
              return i;
         }
         }
             }
         }
    }
    
    

}


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import java.io.Serializable;

/**
 *
 * @author AlberPc
 */
public class psi15_Player implements Serializable
{
protected String name;
protected String type;
protected int iId = -1;
public int win;
public int  lost;
int pay;
int coins;
int turn;
int bet;



public psi15_Player(AMSAgentDescription agent){

		this.win = 0;
		this.coins = 0;
		this.lost = 0;
		this.pay = 0;
                                    this.turn = 0;
                                    this.coins = 0;
		

		
		
		this.name = agent.getName().getName().toString().split("@")[0];
		setType(this.name);

	}


/////////////////////////////GETTERS Y SETTERS//////////////////////////////

public String getName ()
	{
	return name;
	}

public void setId (int iIdAux)
	{
	iId = iIdAux;
	}



public int getId ()
	{
	return iId;
	}



public void setName (String sAux)
	{
	name = new String (sAux);
	}




public int getTurn(){
    return this.turn;
}
/**
	* Este método devuelve la estrategia elegida por este jugador.
	*
	*	@param iNumStrats	Es el número de estrategias totales que tenemos.
	*
	*	@return Un entero con la estrategia elegida por este jugador.
	*/
public int giveJugada (int iNumPartida, int iNumStrats)
	{
	return 0;							// Siempre escoge la estrategia cero
	}
	
	
/**
	Devuelve el mensaje para informar del ganador.
	*
	*	@param idWinner                          Id del ganador  
	*	@param coins                                Monedas totales
	*	@param bets                                 Apuestas realizadas
	*	@param hiddencoins                      Monedas escondidas por los jugadores
	*/
public String giveresult (int idWinner, int coins, String bets, String hiddencoins)
	{
            String result;
            result = "Result"+"#"+idWinner+"#"+coins+"#"+bets+"#"+hiddencoins;
            return result;
            
        }

/*public String giveresult (int iNumPartida, int iMiJugada, int iMiPago, int iSuJugada, int iSuPago)
	{
            String result;
            result = "Result"+"#"+
            
        }
*/
    public void setTurn(int turn){
        this.turn = turn;
    }
       private void setType(String typeAux){
		if (typeAux.contains("fixed")) this.type = "Fixed";
		else if (typeAux.contains("rand")) this.type = "Random";
		else if (typeAux.contains("Inte")) this.type = "Intelligent";
		else this.type = "N/A";
	}

    public void setCoins(int hisCoins) {
      this.coins = hisCoins;
    }

    public void setBet(String auxbet) {
      this.bet=Integer.parseInt(auxbet);
    }

    public int getBet() {
      return this.bet;
    }

    public void resetStatistics() {
        this.win = 0;
        this.lost = 0;
        this.bet = 0;
    }

    public void resetGame() {
       
    }

    public void resetLocalGame() {
     
    }
	
	
} // de la clase Jugador

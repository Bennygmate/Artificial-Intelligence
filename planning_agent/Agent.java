
/***********************************************
 *  Agent.java 								   *
 *  Sample Agent for Text-Based Adventure Game *                                *
************************************************/

/**********************************************************************************	            		  *
 *  Algorithms Used: was AStar Search with Manhanttan Distance Heuristic 		  *
 *  (manipulated to suit different costs of agent environment)					  *
 *  and seed fill to check if specific locations are reachable or not			  *
 *  Data Structures employed: 													  *
 *  - Hash map for storing the agent environment and perception					  *
 *  - Linked Lists for path storing, tool/obstacle storing                        *
 *  - Double Ended Queue for open states (State-Based Search)	    			  *   
 *  - Hash Set for closed states (State-Based Search)	                          *
 *  Design decisions: Used different classes to try and represent a logical agent * 
 *  World Model Class - Stores knowledge base, and agent perceptions through a    *
 *  map,some logical inferences on agent moves and whether or not is it possible  *
 *  Agent Planner Class - Handles the goals/utilites through different priorities *
 *  AStar Class - Helps agent plan paths (State-Based Search) through different   *
 *  goals/utilties as well.							 * 
 *  Agent Reach Class - Helps the agent plan (State-Based Search) whether it is   *
 *  logical to reach a certain state (makes program bit slow because of this)     *
 *  Agent Explorer Class - Tries to expand the agent's perception/knowledge base  *
 *  through exploring the hidden environment					 *
***********************************************************************************/

import java.util.*;
import java.io.*;
import java.net.*;

public class Agent {
	private AgentPlanner agentplanner;
	public Agent() {
		agentplanner = new AgentPlanner();
	}
   public char get_action( char view[][] ) {
	   return agentplanner.agentPerception(view);
   }
   
// Other Code
   void print_view( char view[][] ) {
      int i,j;
      System.out.println("\n+-----+");
      for( i=0; i < 5; i++ ) {
         System.out.print("|");
         for( j=0; j < 5; j++ ) {
            if(( i == 2 )&&( j == 2 )) {
               System.out.print('^');
            }
            else {
               System.out.print( view[i][j] );
            }
         }
         System.out.println("|");
      }
      System.out.println("+-----+");
   }

   public static void main( String[] args )
   {
      InputStream in  = null;
      OutputStream out= null;
      Socket socket   = null;
      Agent  agent    = new Agent();
      char   view[][] = new char[5][5];
      char   action   = 'F';
      int port;
      int ch;
      int i,j;

      if( args.length < 2 ) {
         System.out.println("Usage: java Agent -p <port>\n");
         System.exit(-1);
      }

      port = Integer.parseInt( args[1] );

      try { // open socket to Game Engine
         socket = new Socket( "localhost", port );
         in  = socket.getInputStream();
         out = socket.getOutputStream();
      }
      catch( IOException e ) {
         System.out.println("Could not bind to port: "+port);
         System.exit(-1);
      }

      try { // scan 5-by-5 wintow around current location
         while( true ) {
            for( i=0; i < 5; i++ ) {
               for( j=0; j < 5; j++ ) {
                  if( !(( i == 2 )&&( j == 2 ))) {
                     ch = in.read();
                     if( ch == -1 ) {
                        System.exit(-1);
                     }
                     view[i][j] = (char) ch;
                  }
               }
            }
            //agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION
            action = agent.get_action( view );
            out.write( action );
         }
      }
      catch( IOException e ) {
         System.out.println("Lost connection to port: "+ port );
         System.exit(-1);
      }
      finally {
         try {
            socket.close();
         }
         catch( IOException e ) {}
      }
   }
}

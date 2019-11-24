
/***********************************************
 *  Agent.java 								   *
 *  Sample Agent for Text-Based Adventure Game *
 *  COMP3411 Artificial Intelligence		   *
 *  UNSW Session 1, 2017					   *
 *  z3460693                                   *
************************************************/

/**********************************************************************************
 *  Honestly, I am quite new to Java and object orientated programming but wanted *
 *  to practise in this assignment so might be a bit messy. The program has got a *
 *  few bugs, if rotate agent in different directions, not too sure how to fix    *
 *  sometimes it passes the tests (going have to rely on luck for some) 		  *
 *  The program is also a bit slow, not too sure how to look at time complexities *
 *  in java (seems to be bit slow compared to C)			            		  *
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
 *  goals/utilties as well.										     			  * 
 *  Agent Reach Class - Helps the agent plan (State-Based Search) whether it is   *
 *  logical to reach a certain state (makes program bit slow because of this)     *
 *  Agent Explorer Class - Tries to expand the agent's perception/knowledge base  *
 *  through exploring the hidden environment								      *
 *  In the end, it is the agent planner who feeds to the action of the agent      *
 *  (would be helpful if world model could feed as well)			   		      *
 *  (world model, agent planner, agent action, agent perception) to store 		  *
 *  different knowledge bases within that the agent knows will be a priority      *
 *  Along the way, I tried to make different data structures for storing          *
 *  (redesigning whole program) but kept getting confused, as it is a             *
 *  bit slow and buggy I tried to play around with different g costs for          *
 *  different environmental obstacles within the AStar algorithm but it           *
 *  seemed to be quite confusing as the agent has to handle a lot of different    *
 *  inferences such as conservation of trees for a raft, blasting dynamites       *
 *  at the right objects, gaining dynamites, spatial orientations, would be great *
 *  if I used a strategy pattern, so it wouldn't be as confusing with many        *
 *  different functions but me myself was trying to reduce the time as sometimes  *
 *  the program runs out of heap space and then also couldn't pass some tests     *
 *  so I redesigned the assignment for 2 weeks and kept getting frustrated.       *
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

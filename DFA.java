import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Andrew Coleman
 * Provides a Java implementation for a deterministic finite automata with the ability to minimize a machine.
 */
public class DFA {

	private int numstates, numterminals, numfinalstates;
	private char terminals[];
	private int path[][];
	private boolean finalstates[];
	
	// constructs a new DFA object from a file as per the in class specs
	DFA ( String filename )	{
		BufferedReader infile = null;
		numstates = 0;
		numterminals = 0;
		try	{
			infile = new BufferedReader ( new FileReader ( filename ) );
			
			numterminals = Integer.parseInt ( infile.readLine() );
			String strterms[] = infile.readLine().split ( " " );
			terminals = new char[numterminals];
			for ( int slen = 0; slen < numterminals; slen++ )	{
				terminals[slen] = strterms[slen].charAt ( 0 );
			}
			
			numstates = Integer.parseInt ( infile.readLine() );
			
			path = new int[numstates][numterminals];
			for ( int i = 0; i < (numstates * numterminals); i++ )	{
				String pathstr[] = infile.readLine().split ( " " );
				int first = Integer.parseInt ( pathstr[0] );
				int second = Integer.parseInt ( pathstr[2] );
				for ( int t = 0; t < numterminals; t ++ )	{
					if ( terminals[t] == pathstr[1].charAt ( 0 ) )	{
						path[first][t] = second;
						break;
					}
				}
			}
			
			numfinalstates = Integer.parseInt ( infile.readLine() );
			finalstates = new boolean[numstates];
			String[] fs = infile.readLine().trim().split ( " " );
			for ( int tmp = 0; tmp < numfinalstates; tmp++ )	{
				finalstates[Integer.parseInt ( fs[tmp] )] = true;
			}
			
			infile.close();
		}
		catch ( IOException exception )	{
			exception.printStackTrace();
		}
		catch ( Exception exception )	{
			System.out.println ( "Malformed input file!" );
			exception.printStackTrace();
		}
	}

	// returns a string that is compatible with our input file specification	
	public String toString()	{
		StringBuffer buf = new StringBuffer();
		buf.append ( numterminals + "\n" );
		for ( int i = 0; i < numterminals; i++ )
			buf.append ( terminals[i] + " " );
		buf.deleteCharAt ( buf.length() - 1 );
		buf.append ( "\n" + numstates + "\n" );
		for ( int i = 0; i < numstates; i++ )	{
			for ( int j = 0; j < numterminals; j++ )	{
				buf.append ( i + " " + terminals[j] + " " + path[i][j] + "\n" );
			}
		}
		buf.append ( numfinalstates + "\n" );
		for ( int i = 0; i < numstates; i++ )
			if ( finalstates[i] == true )
				buf.append ( i + " " );
		buf.deleteCharAt ( buf.length() - 1 );
		return buf.toString();
	}

	public void minimize()	{
		// distinguishable marks
		boolean mark[][] = new boolean[numstates][numstates];
		// the list of reachable states
		boolean reachable[] = new boolean[numstates];
		// a flag for various uses throughout the method
		boolean flag = true;
		
		// remove all unreachable states from consideration altogether
		// assuming we can always reach q0
		reachable[0] = true;
		boolean visited[] = new boolean[numstates];
		// queue-less bfs!
		while ( flag )	{
			flag = false;
			for ( int state = 0; state < numstates; state++ )	{
				if ( reachable[state] && !visited[state] )	{
					visited[state] = true;
					flag = true;
					for ( int term = 0; term < numterminals; term++ )	{
						reachable[ path[state][term] ] = true;
					}
				}// end if
			}// endfor state
		}// end while
		
		// find all pairs of states (p,q) such that final[p] != final[q]
		for ( int statenum = 0; statenum < numstates; statenum++ )	{
			//if ( !reachable[statenum] )	continue;
			for ( int statenum2 = 0; statenum2 < numstates; statenum2++ )	{
				//if ( !reachable[statenum2] )	continue;
				if ( finalstates[statenum] != finalstates[statenum2] )
					mark[statenum][statenum2] = true;
				else
					mark[statenum][statenum2] = false;
			}
		}
		
		// find all pairs (p,q) and mark them as distinguishable
		// i am looking at path[p] foreach terminal and path[q], if both
		// states are distinct, then mark these
		flag = true;
		// continue until no mark has been made
		while ( flag )	{
			flag = false;
			for ( int x = 0; x < numstates; x++ )	{
				//if ( !reachable[x] )	continue;
				for ( int y = 0; y < numstates; y++ )	{
					if ( x == y )	continue;
					for ( int t = 0; t < numterminals; t++ )	{
						if ( mark[ path[x][t] ][ path[y][t] ] && !mark[x][y] )	{
							mark[x][y] = true;
							flag = true;
						}
					}// end terminal
				}// endfor second state
			}// endfor first state
		}// end while
		
		int numminimized = numstates;
		int numminfinal = numfinalstates;
		int minstates[] = new int[numstates];
		for ( int state = 0; state < numstates; state++ )	{
			visited[state] = false;
			if ( !reachable[state] )
				minstates[state] = -1;
			else
				minstates[state] = state;
		}
		// extract the distinguishable states
		for ( int state = 0; state < numstates; state++ )	{
			if ( !reachable[state] || visited[state] )	continue;
			for ( int p = 0; p < numstates; p++ )	{
				if ( p == state )	continue;
				if ( !mark[state][p] && !visited[p] )	{
					minstates[p] = state;
					visited[p] = true;
					if ( finalstates[p] )
						numminfinal--;
					else
						numminimized--;
				}
			}// endfor p
		}// endfor state
		
		// update paths between states
		for ( int u = 0; u < numstates; u++ )	{
			for ( int t = 0; t < numterminals; t++ )	{
				path[u][t] = minstates[path[u][t]];
			}// endfor t
		}// endfor u
		
		// make new data variables and update them to the proper values
		int newpath[][] = new int[numminimized][numterminals];
		boolean newfinal[] = new boolean[numminimized];
		int mrow = 0;
		for ( int row = 0; row < numstates; row++ )	{
			if ( visited[row] )	continue;
			for ( int t = 0; t < numterminals; t++ )	{
				newpath[mrow][t] = path[row][t];
			}
			newfinal[mrow] = finalstates[row];
			mrow++;
		}
		path = newpath;
		numstates = numminimized;
		numfinalstates = numminfinal;
		finalstates = newfinal;
	}
}

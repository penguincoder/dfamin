import java.io.*;

/**
 * @author Andrew Coleman
 * Driver class for the DFA minimization object.
 */
public class dfamin {

	public static void main ( String[] args ) {
		if ( args.length < 1 )	{
			System.out.println ( "Usage: java dfamin [inputfile] <outputfile>");
			System.exit ( 0 );
		}
		DFA dfa = new DFA ( args[0] );
		
		dfa.minimize();
		if ( args.length > 1 )	
		{
			try	
			{
				PrintWriter out = new PrintWriter ( new FileWriter ( args[1] ) );
				out.println ( dfa );
				out.flush();
				out.close();
			}
			catch ( Exception exception )	
			{
				System.out.println ( "Exception!" );
				exception.printStackTrace();
			}
		}
		else	{
			System.out.println ( dfa );
		}
	}
}


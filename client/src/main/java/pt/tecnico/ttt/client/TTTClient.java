package pt.tecnico.ttt.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.ttt.*;
import pt.tecnico.ttt.PlayResponse.PlayResult;

import java.util.Scanner;

public class TTTClient {

	public static void main(String[] args) {
		System.out.println(TTTClient.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s host port%n", TTTClient.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String target = host + ":" + port;

		// Channel is the abstraction to connect to a service endpoint
		// Let us use plaintext communication because we do not have certificates
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// It is up to the client to determine whether to block the call
		// Here we create a blocking stub, but an async stub,
		// or an async stub with Future are always possible.
		TTTGrpc.TTTBlockingStub stub = TTTGrpc.newBlockingStub(channel);

		playGame(stub);

	}

	private static void playGame(TTTGrpc.TTTBlockingStub stub) {

		int player = 0;                              /* Player number - 0 or 1               */
		int go = 0;                                  /* Square selection number for turn     */
		int row = 0;                                 /* Row index for a square               */
		int column = 0;                              /* Column index for a square            */
		int winner = -1;                              /* The winning player                   */
		PlayResult play_res;

		/* The main game loop. The game continues for up to 9 turns */
		/* As long as there is no winner                            */
		do {
			/* Get valid player square selection */
			do {
				/* Print current board */
				System.out.println(stub.currentBoard(CurrentBoardRequest.getDefaultInstance()).getBoard());

				System.out.printf("\nPlayer %d, please enter the number of the square " +
						"where you want to place your %c (or 0 to refresh the board): ", player, (player==1)?'X':'O');
				go = readPlay();

				if (go == 0){
					play_res = PlayResult.UNKNOWN;
					continue;
				}

				row = --go/3;                                 /* Get row index of square      */
				column = go%3;                                /* Get column index of square   */

				PlayRequest playRequest = PlayRequest.newBuilder().setRow(row).setColumn(column).setPlayer(player).build();
				play_res = stub.play(playRequest).getResult();
				if (play_res != PlayResult.SUCCESS) {
					displayResult(play_res);
				}
			} while(play_res != PlayResult.SUCCESS);

			winner = stub.checkWinner(CheckWinnerRequest.getDefaultInstance()).getResult();
			player = (player+1)%2;                           /* Select player */

			System.out.println("player " + player);

		} while (winner == -1);

		/* Game is over so display the final board */
		System.out.println(stub.currentBoard(CurrentBoardRequest.getDefaultInstance()).getBoard());

		/* Display result message */
		if(winner == 2)
			System.out.println("\nHow boring, it is a draw");
		else
			System.out.println("\nCongratulations, player " + winner + ", YOU ARE THE WINNER!");
	}

	private static void displayResult(PlayResult play_res) {
		switch (play_res) {
			case OUT_OF_BOUNDS:
				System.out.print("Position outside board.");
				break;
			case SQUARE_TAKEN:
				System.out.print("Square already taken.");
				break;
			case WRONG_TURN:
				System.out.print("Not your turn.");
				break;
			case GAME_FINISHED:
				System.out.print("Game has finished.");
				break;
		}
		System.out.println(" Try again...");
	}

	private static int readPlay() {
		Scanner scanner = new Scanner(System.in);
		return scanner.nextInt();
	}
}

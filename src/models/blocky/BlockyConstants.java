package models.blocky;


public interface BlockyConstants {

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;

	public static final int RUNNING = -1;
	public static final int REACHED_GOAL = 0;
	public static final int WALL_CRASH = 1;
	public static final int INFINITE_LOOP = 2;

	public static final int MAX_INSTRUCTIONS = 400;
	public static final int INFINITY = 999999999;
	
	public static final int NUM_DYNAMIC_VARS = 4;

}

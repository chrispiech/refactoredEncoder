package models.blocky;

@SuppressWarnings("serial")
public class BlockyException extends RuntimeException {

	String msg;
	
	public BlockyException(String msg) {
		this.msg = msg;
	}

	

}

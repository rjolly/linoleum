package linoleum;

public class Version9 {
	public static void main(final String args[]) {
		final String s = System.getProperty("java.version");
		System.exit(Integer.parseInt(s.substring(0, s.indexOf("."))) < 9?1:0);
	}
}

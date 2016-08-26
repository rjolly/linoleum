package linoleum.application;

import javax.activation.MimeTypeParseException;

public class MimeType extends javax.activation.MimeType implements Comparable<MimeType> {
	public MimeType(final String str) throws MimeTypeParseException {
		super(str);
		assert(getSubType().equals("*") || !getPrimaryType().equals("*"));
	}

	public boolean match(final MimeType that) {
		return this.getPrimaryType().equals("*")
			|| that.getPrimaryType().equals("*")
			|| super.match(that);
	}

	@Override
	public int compareTo(final MimeType that) {
		final int p = compare(this.getPrimaryType(), that.getPrimaryType());
		return p != 0?p:compare(this.getSubType(), that.getSubType());
	}

	private static int compare(final String s1, final String s2) {
		return s1.equals("*") && !s2.equals("*")?1:!s1.equals("*") && s2.equals("*")?-1:s1.compareTo(s2);
	}
}

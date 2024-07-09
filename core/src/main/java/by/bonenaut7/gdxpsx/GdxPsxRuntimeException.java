package by.bonenaut7.gdxpsx;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class GdxPsxRuntimeException extends GdxRuntimeException {
	private static final long serialVersionUID = -7126767490612431702L;

	public GdxPsxRuntimeException(String message) {
		super(message);
	}

	public GdxPsxRuntimeException(Throwable t) {
		super(t);
	}

	public GdxPsxRuntimeException(String message, Throwable t) {
		super(message, t);
	}
}

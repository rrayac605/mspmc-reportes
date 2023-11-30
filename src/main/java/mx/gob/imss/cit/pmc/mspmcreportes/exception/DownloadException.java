package mx.gob.imss.cit.pmc.mspmcreportes.exception;

public class DownloadException extends Exception {
	private static final long serialVersionUID = -3307656865699875168L;

	public DownloadException() {
		super();
	}

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	public DownloadException(Throwable cause) {
		super(cause);
	}

}

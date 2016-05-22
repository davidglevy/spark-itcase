package levy.david.spark.itcase.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transfer.TransferResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletOutTransferListener extends AbstractTransferListener {

	private static final Logger logger = LoggerFactory
			.getLogger(ServletOutTransferListener.class);

	private OutputStream out;

	TransferListener delegate;

	private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

	private int lastLength;

	public ServletOutTransferListener(OutputStream out,
			TransferListener delegate) {
		super();
		this.out = out;
		this.delegate = delegate;
	}

	public void transferInitiated(TransferEvent event)
			throws TransferCancelledException {
		delegate.transferInitiated(event);
	}

	public void transferStarted(TransferEvent event)
			throws TransferCancelledException {
		delegate.transferStarted(event);
	}

	public void transferProgressed(TransferEvent event)
			throws TransferCancelledException {
		TransferResource resource = event.getResource();
        downloads.put( resource, Long.valueOf( event.getTransferredBytes() ) );

        StringBuilder buffer = new StringBuilder( 64 );

        for ( Map.Entry<TransferResource, Long> entry : downloads.entrySet() )
        {
            long total = entry.getKey().getContentLength();
            long complete = entry.getValue().longValue();

            buffer.append( getStatus( complete, total ) ).append( "  " );
        }

        int pad = lastLength - buffer.length();
        lastLength = buffer.length();
        pad( buffer, pad );
        buffer.append( '\r' );

        writeToServletOut( buffer.toString() );
		
		delegate.transferProgressed(event);
	}

	public void transferCorrupted(TransferEvent event)
			throws TransferCancelledException {
		delegate.transferCorrupted(event);

		
		try (ByteArrayOutputStream temp = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(temp)) {
			event.getException().printStackTrace(ps);
			String stackTraceText = temp.toString();
			writeToServletOut("Error transfering [" + event.getResource().getRepositoryUrl() + "]: " + event.getException().getMessage() + "\n" + stackTraceText);
		} catch (IOException e) {
			logger.error("Unable to print error to servlet output stream: " + e.getMessage(), e);
		}

	}

	public void transferSucceeded(TransferEvent event) {
		delegate.transferSucceeded(event);

		transferCompleted(event);

		TransferResource resource = event.getResource();
		long contentLength = event.getTransferredBytes();
		if (contentLength >= 0) {
			String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded"
					: "Downloaded");
			String len = contentLength >= 1024 ? toKB(contentLength) + " KB"
					: contentLength + " B";

			String throughput = "";
			long duration = System.currentTimeMillis()
					- resource.getTransferStartTime();
			if (duration > 0) {
				long bytes = contentLength - resource.getResumeOffset();
				DecimalFormat format = new DecimalFormat("0.0",
						new DecimalFormatSymbols(Locale.ENGLISH));
				double kbPerSec = (bytes / 1024.0) / (duration / 1000.0);
				throughput = " at " + format.format(kbPerSec) + " KB/sec";
			}

			writeToServletOut(type + ": " + resource.getRepositoryUrl()
					+ resource.getResourceName() + " (" + len + throughput
					+ ")");
		}
	}

	public void transferFailed(TransferEvent event) {
		delegate.transferFailed(event);
	}

	private void transferCompleted(TransferEvent event) {
		downloads.remove(event.getResource());

		StringBuilder buffer = new StringBuilder(64);
		pad(buffer, lastLength);
		buffer.append('\r');
		String stringToWrite = buffer.toString();
		writeToServletOut(stringToWrite);
	}

    private String getStatus( long complete, long total )
    {
        if ( total >= 1024 )
        {
            return toKB( complete ) + "/" + toKB( total ) + " KB ";
        }
        else if ( total >= 0 )
        {
            return complete + "/" + total + " B ";
        }
        else if ( complete >= 1024 )
        {
            return toKB( complete ) + " KB ";
        }
        else
        {
            return complete + " B ";
        }
    }
	
	private void writeToServletOut(String message) {

		try {
			out.write(message.getBytes("UTF-8"));
			out.flush();
		} catch (UnsupportedEncodingException e) {
			logger.error(
					"Unable to write message to servlet stream: "
							+ e.getMessage(), e);
		} catch (IOException e) {
			logger.error(
					"Unable to write message to servlet stream: "
							+ e.getMessage(), e);
		}
	}

	private void pad(StringBuilder buffer, int spaces) {
		String block = "                                        ";
		while (spaces > 0) {
			int n = Math.min(spaces, block.length());
			buffer.append(block, 0, n);
			spaces -= n;
		}
	}

	protected long toKB(long bytes) {
		return (bytes + 1023) / 1024;
	}

}
